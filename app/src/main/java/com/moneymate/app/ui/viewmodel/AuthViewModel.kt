package com.moneymate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.moneymate.app.utils.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import javax.inject.Inject

enum class UserRole { USER, ADMIN }

enum class AuthState {
    LOADING,
    GOOGLE_SIGN_IN,   // Show Google Sign-In screen (first launch)
    LOGIN,
    ADMIN_LOGIN,
    AUTHENTICATED
}

// Result emitted after Google Sign-In credential is processed
sealed class GoogleSignInResult {
    object Idle : GoogleSignInResult()
    object Loading : GoogleSignInResult()
    object Success : GoogleSignInResult()
    data class Failure(val message: String) : GoogleSignInResult()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val prefs: AppPreferences
) : ViewModel() {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow(AuthState.LOADING)
    val authState: StateFlow<AuthState> = _authState

    private val _currentRole = MutableStateFlow(UserRole.ADMIN)
    val currentRole: StateFlow<UserRole> = _currentRole

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _wrongAttempts = MutableStateFlow(0)
    val wrongAttempts: StateFlow<Int> = _wrongAttempts

    private val _isLocked = MutableStateFlow(false)
    val isLocked: StateFlow<Boolean> = _isLocked

    private val _lockCountdown = MutableStateFlow(0L)
    val lockCountdown: StateFlow<Long> = _lockCountdown

    // ─── Google Sign-In result ────────────────────────────────────────────

    private val _googleSignInResult = MutableStateFlow<GoogleSignInResult>(GoogleSignInResult.Idle)
    val googleSignInResult: StateFlow<GoogleSignInResult> = _googleSignInResult

    /** True if user has already completed Google Sign-In in a previous session. */
    val isGoogleSignedIn: Boolean get() = prefs.isGoogleSignedIn

    /** Stored Firebase UID (available after first successful Google Sign-In). */
    val firebaseUid: String get() = prefs.firebaseUid

    // ─── Existing internals ────────────────────────────────────────────────────

    private var countdownJob: Job? = null
    private var inactivityJob: Job? = null
    private var wentToBackground = false
    private var isInitialized = false

    val pinLength: Int get() = prefs.pinLength

    var biometricEnabled: Boolean
        get() = prefs.biometricEnabled
        set(value) { prefs.biometricEnabled = value }

    init {
        prefs.initDefaultPinIfNeeded()
        restoreLockState()
    }

    private fun restoreLockState() {
        val lockUntil = prefs.lockUntil
        val now = System.currentTimeMillis()
        if (lockUntil > now) {
            _isLocked.value = true
            _wrongAttempts.value = prefs.wrongAttempts
            startLockCountdown(lockUntil - now)
        } else if (lockUntil > 0) {
            prefs.lockUntil = 0L
            prefs.wrongAttempts = 0
        }
    }

    // ─── Session / launch flow ─────────────────────────────────────────────────

    fun checkSessionTimeout() {
        isInitialized = true

        if (prefs.isFirstLaunch && !prefs.isGoogleSignedIn) {
            prefs.isFirstLaunch = false
            _authState.value = AuthState.GOOGLE_SIGN_IN
            return
        }

        if (!prefs.isGoogleSignedIn) {
            _authState.value = AuthState.GOOGLE_SIGN_IN
            return
        }

        if (prefs.isFirstLaunch) {
            prefs.isFirstLaunch = false
        }

        if (prefs.appWasClosedLoggedIn) {
            prefs.appWasClosedLoggedIn = false
            forceLogout()
            return
        }

        if (prefs.isLoggedOut) {
            _authState.value = AuthState.ADMIN_LOGIN
            return
        }

        val savedRole = prefs.currentRole
        val lastActive = prefs.lastActiveTime
        val elapsed = System.currentTimeMillis() - lastActive
        val fiveMinutes = 5 * 60 * 1000L

        when (savedRole) {
            "ADMIN" -> {
                if (elapsed > fiveMinutes) {
                    forceLogout()
                } else {
                    _currentRole.value = UserRole.ADMIN
                    _authState.value = AuthState.AUTHENTICATED
                }
            }
            "USER" -> {
                _currentRole.value = UserRole.USER
                _authState.value = AuthState.AUTHENTICATED
            }
            else -> {
                _authState.value = AuthState.ADMIN_LOGIN
            }
        }
    }

    // ─── Google Sign-In Logic ───────────────────────────────────────────────────

    /**
     * Call this with the [AuthCredential] obtained from the Google Sign-In result.
     */
    fun handleGoogleCredential(credential: AuthCredential) {
        _googleSignInResult.value = GoogleSignInResult.Loading
        viewModelScope.launch {
            try {
                val result = firebaseAuth.signInWithCredential(credential).await()
                val uid = result.user?.uid
                    ?: throw Exception("Sign-in succeeded but UID is null")

                prefs.isGoogleSignedIn = true
                prefs.firebaseUid = uid
                prefs.googleDisplayName = result.user?.displayName ?: ""
                prefs.googleEmail = result.user?.email ?: ""

                _googleSignInResult.value = GoogleSignInResult.Success
            } catch (e: Exception) {
                _googleSignInResult.value = GoogleSignInResult.Failure(
                    e.message ?: "Google Sign-In failed"
                )
            }
        }
    }

    /**
     * Exposes a way for the UI to set a custom Google registration failure.
     */
    fun setGoogleSignInFailure(message: String) {
        _googleSignInResult.value = GoogleSignInResult.Failure(message)
    }

    /**
     * Called after [GoogleSignInResult.Success] has been handled by the UI.
     */
    fun onGoogleSignInHandled() {
        _googleSignInResult.value = GoogleSignInResult.Idle
        _authState.value = AuthState.ADMIN_LOGIN
    }

    fun clearGoogleSignInResult() {
        _googleSignInResult.value = GoogleSignInResult.Idle
    }

    // ─── Existing: background / foreground ────────────────────────────────────

    fun onAppBackground() {
        wentToBackground = true
        prefs.lastActiveTime = System.currentTimeMillis()

        if (_authState.value == AuthState.AUTHENTICATED) {
            prefs.appWasClosedLoggedIn = true
        }

        inactivityJob?.cancel()
        inactivityJob = viewModelScope.launch {
            delay(5 * 60 * 1000L)
            if (_authState.value == AuthState.AUTHENTICATED) {
                forceLogout()
            }
        }
    }

    fun onAppForeground() {
        if (!isInitialized) return
        if (!wentToBackground) return
        wentToBackground = false

        inactivityJob?.cancel()
        inactivityJob = null

        prefs.appWasClosedLoggedIn = false

        if (_authState.value != AuthState.AUTHENTICATED) return

        val savedRole = prefs.currentRole
        val lastActive = prefs.lastActiveTime
        val elapsed = System.currentTimeMillis() - lastActive
        val fiveMinutes = 5 * 60 * 1000L

        when (savedRole) {
            "ADMIN" -> {
                if (elapsed > fiveMinutes) {
                    forceLogout()
                } else {
                    _currentRole.value = UserRole.ADMIN
                    _authState.value = AuthState.AUTHENTICATED
                }
            }
            "USER" -> {
                _currentRole.value = UserRole.USER
                _authState.value = AuthState.AUTHENTICATED
            }
            else -> forceLogout()
        }
    }

    private fun forceLogout() {
        prefs.isLoggedOut = true
        prefs.currentRole = ""
        prefs.appWasClosedLoggedIn = false
        _currentRole.value = UserRole.ADMIN
        _error.value = null
        wentToBackground = false
        _authState.value = AuthState.ADMIN_LOGIN
    }

    // ─── Existing: login helpers ───────────────────────────────────────────────

    fun loginAsUser() {
        prefs.isLoggedOut = false
        _currentRole.value = UserRole.USER
        prefs.currentRole = "USER"
        prefs.lastActiveTime = System.currentTimeMillis()
        _authState.value = AuthState.AUTHENTICATED
    }

    fun prepareAdminLogin() {
        _authState.value = AuthState.ADMIN_LOGIN
        _error.value = null
    }

    fun loginAsAdmin(pin: String) {
        if (checkLocked()) return
        if (pin == "__biometric__") {
            prefs.isLoggedOut = false
            _currentRole.value = UserRole.ADMIN
            prefs.currentRole = "ADMIN"
            prefs.lastActiveTime = System.currentTimeMillis()
            _authState.value = AuthState.AUTHENTICATED
            _error.value = null
            resetAttempts()
            return
        }
        if (hashPin(pin) == prefs.adminPinHash) {
            prefs.isLoggedOut = false
            _currentRole.value = UserRole.ADMIN
            prefs.currentRole = "ADMIN"
            prefs.lastActiveTime = System.currentTimeMillis()
            _authState.value = AuthState.AUTHENTICATED
            _error.value = null
            resetAttempts()
        } else {
            handleWrongAttempt()
        }
    }

    fun changeAdminPin(oldPin: String, newPin: String): Boolean {
        return if (hashPin(oldPin) == prefs.adminPinHash) {
            prefs.adminPinHash = hashPin(newPin)
            true
        } else false
    }

    fun changePinLength(newLength: Int) {
        prefs.pinLength = newLength
    }

    fun updateLastActiveTime() {
        prefs.lastActiveTime = System.currentTimeMillis()
    }

    fun logout() {
        forceLogout()
    }

    fun clearError() {
        _error.value = null
    }

    // ─── Existing: lockout ─────────────────────────────────────────────────────

    private fun handleWrongAttempt() {
        val attempts = _wrongAttempts.value + 1
        _wrongAttempts.value = attempts
        prefs.wrongAttempts = attempts
        if (attempts >= 5) {
            val lockUntil = System.currentTimeMillis() + (2 * 60 * 1000L)
            prefs.lockUntil = lockUntil
            _isLocked.value = true
            startLockCountdown(2 * 60 * 1000L)
            _error.value = "Too many wrong attempts! Locked for 2 minutes."
        } else {
            _error.value = "Wrong PIN! ${5 - attempts} attempts remaining."
        }
    }

    private fun startLockCountdown(durationMs: Long) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            var remaining = durationMs
            while (remaining > 0) {
                _lockCountdown.value = remaining / 1000
                delay(1000)
                remaining -= 1000
            }
            _isLocked.value = false
            _lockCountdown.value = 0
            _error.value = null
            prefs.lockUntil = 0L
            resetAttempts()
        }
    }

    private fun checkLocked(): Boolean {
        if (_isLocked.value) {
            val remaining = (prefs.lockUntil - System.currentTimeMillis()) / 1000
            if (remaining <= 0) {
                _isLocked.value = false
                _lockCountdown.value = 0
                prefs.lockUntil = 0L
                resetAttempts()
                return false
            }
            _error.value = "Locked! Try again in ${remaining}s."
            return true
        }
        return false
    }

    private fun resetAttempts() {
        _wrongAttempts.value = 0
        prefs.wrongAttempts = 0
    }

    // ─── Existing: utils ───────────────────────────────────────────────────────

    fun isPalindrome(pin: String): Boolean = pin == pin.reversed()

    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(pin.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}