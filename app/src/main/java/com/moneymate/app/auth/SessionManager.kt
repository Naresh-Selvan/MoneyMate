package com.moneymate.app.auth

import com.moneymate.app.data.local.dao.AppUserDao
import com.moneymate.app.data.local.dao.AuditLogDao
import com.moneymate.app.data.local.entity.AppUser
import com.moneymate.app.data.local.entity.AuditAction
import com.moneymate.app.data.local.entity.AuditLog
import com.moneymate.app.data.local.entity.Permission
import com.moneymate.app.data.local.entity.UserRole
import com.moneymate.app.utils.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

sealed class LoginResult {
    data class Success(val user: AppUser) : LoginResult()
    data object InvalidCredentials : LoginResult()
    data object UserNotFound : LoginResult()
    data object Inactive : LoginResult()
}

@Singleton
class SessionManager @Inject constructor(
    private val appPreferences: AppPreferences,
    private val appUserDao: AppUserDao,
    private val auditLogDao: AuditLogDao
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _currentUser = MutableStateFlow<AppUser?>(null)
    val currentUser: StateFlow<AppUser?> = _currentUser.asStateFlow()

    private val _currentRole = MutableStateFlow(UserRole.ADMIN)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isSoloMode = MutableStateFlow(false)
    val isSoloMode: StateFlow<Boolean> = _isSoloMode.asStateFlow()

    init {
        restoreSession()
    }

    private fun restoreSession() {
        val userId = appPreferences.currentUserId
        if (userId > 0) {
            scope.launch {
                val user = appUserDao.getUserById(userId)
                if (user != null && user.isActive && !user.isDeleted) {
                    _currentUser.value = user
                    _currentRole.value = user.role
                    _isLoggedIn.value = true
                } else {
                    appPreferences.currentUserId = 0
                }
            }
        }
    }

    suspend fun checkSoloMode(): Boolean {
        val count = appUserDao.getActiveUserCount()
        return if (count <= 1) {
            // Auto-login as the only user if one exists
            val users = appUserDao.getAllActiveUsersOnce()
            if (users.size == 1) {
                val user = users[0]
                _currentUser.value = user
                _currentRole.value = user.role
                _isLoggedIn.value = true
                _isSoloMode.value = true
                appPreferences.currentUserId = user.id
                true
            } else {
                _isSoloMode.value = true
                false
            }
        } else {
            _isSoloMode.value = false
            false
        }
    }

    suspend fun login(email: String, pinHash: String? = null): LoginResult {
        val user = appUserDao.getUserByEmail(email) ?: return LoginResult.UserNotFound

        if (!user.isActive || user.isDeleted) return LoginResult.Inactive

        if (pinHash != null) {
            if (user.pinHash == null || user.pinHash != pinHash) {
                return LoginResult.InvalidCredentials
            }
        } else if (appPreferences.firebaseUid.isNotEmpty()) {
            // Google Sign-In path: email must match Firebase auth
            val firebaseEmail = appPreferences.googleEmail
            if (firebaseEmail.isNotEmpty() && !email.equals(firebaseEmail, ignoreCase = true)) {
                return LoginResult.InvalidCredentials
            }
        }

        // Update login timestamp
        appUserDao.updateLastLogin(user.id, System.currentTimeMillis())

        // Store session
        _currentUser.value = user.copy(lastLoginAt = System.currentTimeMillis())
        _currentRole.value = user.role
        _isLoggedIn.value = true
        appPreferences.currentUserId = user.id

        // Log login event
        auditLogDao.insert(
            AuditLog(
                userId = user.id,
                userEmail = user.email,
                action = AuditAction.LOGIN,
                targetType = "Session",
                targetId = user.id.toString(),
                targetLabel = user.displayName
            )
        )

        return LoginResult.Success(user)
    }

    suspend fun logout() {
        val user = _currentUser.value
        if (user != null) {
            auditLogDao.insert(
                AuditLog(
                    userId = user.id,
                    userEmail = user.email,
                    action = AuditAction.LOGOUT,
                    targetType = "Session",
                    targetId = user.id.toString(),
                    targetLabel = user.displayName
                )
            )
        }
        _currentUser.value = null
        _currentRole.value = UserRole.ADMIN
        _isLoggedIn.value = false
        _isSoloMode.value = false
        appPreferences.currentUserId = 0
        appPreferences.currentRole = ""
    }

    fun hasPermission(permission: Permission): Boolean {
        val user = _currentUser.value ?: return true // Fallback for solo mode
        return RolePermissionMap.hasPermission(user.role, permission)
    }

    fun canAccessFile(fileId: String): Boolean {
        val user = _currentUser.value ?: return true // Fallback for solo mode
        return RolePermissionMap.canAccessFile(user.role, user.assignedFileIds, fileId)
    }

    fun getCurrentUserId(): Long = _currentUser.value?.id ?: 0

    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(pin.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        const val MAX_LOGIN_ATTEMPTS = 3
        const val LOCK_DURATION_MS = 60_000L // 60 seconds
    }
}
