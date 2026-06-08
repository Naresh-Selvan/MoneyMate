package com.moneymate.app.ui.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.moneymate.app.utils.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class UserRole { USER, ADMIN }

enum class AuthState {
    LOADING,
    GOOGLE_SIGN_IN,
    PHONE_LOGIN,
    OTP_VERIFICATION,
    LOGIN,
    ADMIN_LOGIN,
    PIN_SETUP,
    AUTHENTICATED
}

sealed class PhoneSignInResult {
    object Idle : PhoneSignInResult()
    object Loading : PhoneSignInResult()
    object CodeSent : PhoneSignInResult()
    object Success : PhoneSignInResult()
    data class Failure(val message: String) : PhoneSignInResult()
}

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

    // ─── Auth Flow States ──────────────────────────────────────────────────

    private val _googleSignInResult = MutableStateFlow<GoogleSignInResult>(GoogleSignInResult.Idle)
    val googleSignInResult: StateFlow<GoogleSignInResult> = _googleSignInResult

    private val _phoneSignInResult = MutableStateFlow<PhoneSignInResult>(PhoneSignInResult.Idle)
    val phoneSignInResult: StateFlow<PhoneSignInResult> = _phoneSignInResult

    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    val isUserSignedIn: Boolean get() = prefs.isGoogleSignedIn || prefs.firebaseUid.isNotEmpty()
    val firebaseUid: String get() = prefs.firebaseUid

    private var countdownJob: Job? = null
    private var inactivityJob: Job? = null
    private var wentToBackground = false
    private var isInitialized = false

    val pinLength: Int get() = prefs.pinLength

    var biometricEnabled: Boolean
        get() = prefs.biometricEnabled
        set(value) { prefs.biometricEnabled = value }

    init {
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

    // ─── Session Handling ──────────────────────────────────────────────────

    fun checkSessionTimeout() {
        isInitialized = true

        if (prefs.isFirstLaunch && !isUserSignedIn) {
            prefs.isFirstLaunch = false
            _authState.value = AuthState.GOOGLE_SIGN_IN
            return
        }

        if (!isUserSignedIn) {
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
            _authState.value = if (prefs.adminPinHash.isEmpty()) AuthState.PIN_SETUP else AuthState.ADMIN_LOGIN
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
                _authState.value = if (prefs.adminPinHash.isEmpty()) AuthState.PIN_SETUP else AuthState.ADMIN_LOGIN
            }
        }
    }

    // ─── Google Sign-In Logic ──────────────────────────────────────────────

    fun handleGoogleCredential(credential: AuthCredential) {
        _googleSignInResult.value = GoogleSignInResult.Loading
        viewModelScope.launch {
            try {
                val result = firebaseAuth.signInWithCredential(credential).await()
                val uid = result.user?.uid ?: throw Exception("UID is null")

                prefs.isGoogleSignedIn = true
                prefs.firebaseUid = uid
                prefs.googleDisplayName = result.user?.displayName ?: ""
                prefs.googleEmail = result.user?.email ?: ""

                _googleSignInResult.value = GoogleSignInResult.Success
            } catch (e: Exception) {
                _googleSignInResult.value = GoogleSignInResult.Failure(e.message ?: "Google Sign-In failed")
            }
        }
    }

    fun setGoogleSignInFailure(message: String) {
        _googleSignInResult.value = GoogleSignInResult.Failure(message)
    }

    fun onGoogleSignInHandled() {
        _googleSignInResult.value = GoogleSignInResult.Idle
        _authState.value = if (prefs.adminPinHash.isEmpty()) AuthState.PIN_SETUP else AuthState.ADMIN_LOGIN
    }

    fun clearGoogleSignInResult() {
        _googleSignInResult.value = GoogleSignInResult.Idle
    }

    // ─── Firebase Phone OTP Logic ──────────────────────────────────────────

    fun navigateToPhoneLogin() {
        _authState.value = AuthState.PHONE_LOGIN
        _phoneSignInResult.value = PhoneSignInResult.Idle
    }

    fun navigateBackToSelector() {
        _authState.value = AuthState.GOOGLE_SIGN_IN
    }

    fun sendOtpCode(phoneNumber: String, activity: Activity) {
        _phoneSignInResult.value = PhoneSignInResult.Loading

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                _phoneSignInResult.value = PhoneSignInResult.Failure(e.message ?: "Verification failed")
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                storedVerificationId = verificationId
                resendToken = token
                _phoneSignInResult.value = PhoneSignInResult.CodeSent
                _authState.value = AuthState.OTP_VERIFICATION
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtpCode(code: String) {
        val verificationId = storedVerificationId ?: run {
            _phoneSignInResult.value = PhoneSignInResult.Failure("Missing verification ID")
            return
        }
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneCredential(credential)
    }

    private fun signInWithPhoneCredential(credential: PhoneAuthCredential) {
        _phoneSignInResult.value = PhoneSignInResult.Loading
        viewModelScope.launch {
            try {
                val result = firebaseAuth.signInWithCredential(credential).await()
                val uid = result.user?.uid ?: throw Exception("UID is null")

                prefs.firebaseUid = uid
                if (prefs.googleEmail.isEmpty()) {
                    prefs.googleEmail = result.user?.phoneNumber ?: ""
                }

                _phoneSignInResult.value = PhoneSignInResult.Success
            } catch (e: Exception) {
                _phoneSignInResult.value = PhoneSignInResult.Failure(e.message ?: "Authentication failed")
            }
        }
    }

    fun onPhoneSignInHandled() {
        _phoneSignInResult.value = PhoneSignInResult.Idle
        _authState.value = if (prefs.adminPinHash.isEmpty()) AuthState.PIN_SETUP else AuthState.ADMIN_LOGIN
    }

    // ─── Account Linking Hooks & Active Identity Verification ──────────────────

    fun getCurrentUserEmail(): String {
        return firebaseAuth.currentUser?.email ?: prefs.googleEmail.ifEmpty { "Not Linked" }
    }

    fun getCurrentUserPhone(): String {
        return firebaseAuth.currentUser?.phoneNumber ?: "Not Linked"
    }

    fun linkGoogleAccount(credential: AuthCredential, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            onFailure("No active authentication session discovered.")
            return
        }
        viewModelScope.launch {
            try {
                val result = currentUser.linkWithCredential(credential).await()
                prefs.isGoogleSignedIn = true
                prefs.googleEmail = result.user?.email ?: ""
                prefs.googleDisplayName = result.user?.displayName ?: ""
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Identity mapping attachment failed.")
            }
        }
    }

    fun startLinkingPhoneNumber(phoneNumber: String, activity: Activity, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            onFailure("Active system user session terminated.")
            return
        }

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                viewModelScope.launch {
                    try {
                        currentUser.linkWithCredential(credential).await()
                        onSuccess()
                    } catch (e: Exception) {
                        onFailure(e.message ?: "Instant validation attachment failure.")
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                onFailure(e.message ?: "Verification pipeline failure.")
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                storedVerificationId = verificationId
                resendToken = token
                onSuccess()
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyAndLinkPhoneCode(code: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val verificationId = storedVerificationId ?: run {
            onFailure("Transaction tracker state lost.")
            return
        }
        val currentUser = firebaseAuth.currentUser ?: run {
            onFailure("Invalid context instance.")
            return
        }
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        viewModelScope.launch {
            try {
                currentUser.linkWithCredential(credential).await()
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.message ?: "Failed tracking state integration.")
            }
        }
    }

    fun logoutUser() {
        viewModelScope.launch {
            try {
                firebaseAuth.signOut()

                prefs.isGoogleSignedIn = false
                prefs.firebaseUid = ""
                prefs.googleDisplayName = ""
                prefs.googleEmail = ""
                prefs.currentRole = ""
                prefs.isLoggedOut = true
                prefs.appWasClosedLoggedIn = false

                _currentRole.value = UserRole.ADMIN
                _error.value = null
                wentToBackground = false
                _authState.value = AuthState.GOOGLE_SIGN_IN
            } catch (e: Exception) {
                _error.value = "Logout error processing: ${e.message}"
            }
        }
    }

    // ─── Existing Infrastructure ──────────────────────────────────────────

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
        if (!isInitialized || !wentToBackground) return
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
                if (elapsed > fiveMinutes) forceLogout()
                else {
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
        _authState.value = if (prefs.adminPinHash.isEmpty()) AuthState.PIN_SETUP else AuthState.ADMIN_LOGIN
    }

    fun loginAsUser() {
        prefs.isLoggedOut = false
        _currentRole.value = UserRole.USER
        prefs.currentRole = "USER"
        prefs.lastActiveTime = System.currentTimeMillis()
        _authState.value = AuthState.AUTHENTICATED
    }

    fun prepareAdminLogin() {
        _authState.value = if (prefs.adminPinHash.isEmpty()) AuthState.PIN_SETUP else AuthState.ADMIN_LOGIN
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

    fun setupInitialPin(pin: String) {
        prefs.adminPinHash = hashPin(pin)
        prefs.pinLength = pin.length
        _authState.value = AuthState.AUTHENTICATED
        _error.value = null
        prefs.isLoggedOut = false
        _currentRole.value = UserRole.ADMIN
        prefs.currentRole = "ADMIN"
        prefs.lastActiveTime = System.currentTimeMillis()
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

    fun isPalindrome(pin: String): Boolean = pin == pin.reversed()

    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(pin.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}