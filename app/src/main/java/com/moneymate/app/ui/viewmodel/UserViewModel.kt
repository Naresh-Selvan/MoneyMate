package com.moneymate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymate.app.auth.AuditLogger
import com.moneymate.app.data.local.dao.AppUserDao
import com.moneymate.app.data.local.entity.AppUser
import com.moneymate.app.data.local.entity.AuditAction
import com.moneymate.app.data.local.entity.UserRole
import com.moneymate.app.utils.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

sealed class UserOperationResult {
    data object Success : UserOperationResult()
    data class Error(val message: String) : UserOperationResult()
    data object PlanLimitReached : UserOperationResult()
}

@HiltViewModel
class UserViewModel @Inject constructor(
    private val appUserDao: AppUserDao,
    private val auditLogger: AuditLogger,
    private val appPreferences: AppPreferences
) : ViewModel() {

    val allActiveUsers: StateFlow<List<AppUser>> = appUserDao.getAllActiveUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInactiveUsers: StateFlow<List<AppUser>> = appUserDao.getAllInactiveUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getUserById(id: Long): AppUser? = appUserDao.getUserById(id)

    /**
     * Add a new user with license plan enforcement.
     */
    fun addUser(
        email: String,
        displayName: String,
        role: UserRole,
        assignedFileIds: String,
        pinHash: String?,
        createdByUserId: Long,
        onResult: (UserOperationResult) -> Unit = {}
    ) = viewModelScope.launch {
        // Check plan limits
        val plan = appPreferences.activationPlan
        val activeCount = allActiveUsers.value.size
        val maxUsers = when (plan.lowercase()) {
            "starter" -> 1
            "growth" -> 5
            "enterprise" -> Int.MAX_VALUE
            else -> Int.MAX_VALUE // No plan = unlimited
        }
        if (activeCount >= maxUsers) {
            onResult(UserOperationResult.PlanLimitReached)
            return@launch
        }

        // Check duplicate email
        val existing = appUserDao.getUserByEmail(email)
        if (existing != null && !existing.isDeleted) {
            onResult(UserOperationResult.Error("Email already in use"))
            return@launch
        }

        val user = AppUser(
            email = email,
            displayName = displayName,
            role = role,
            assignedFileIds = assignedFileIds,
            isActive = true,
            createdAt = System.currentTimeMillis(),
            createdByUserId = createdByUserId,
            pinHash = pinHash
        )
        val id = appUserDao.insert(user)

        auditLogger.log(
            action = AuditAction.ADD_USER,
            targetType = "AppUser",
            targetId = id.toString(),
            targetLabel = displayName
        )

        appPreferences.setActiveUserCount(allActiveUsers.value.size + 1)
        onResult(UserOperationResult.Success)
    }

    /**
     * Update an existing user's details.
     */
    fun updateUser(
        userId: Long,
        email: String,
        displayName: String,
        role: UserRole,
        assignedFileIds: String,
        pinHash: String?,
        onResult: (UserOperationResult) -> Unit = {}
    ) = viewModelScope.launch {
        val user = appUserDao.getUserById(userId) ?: run {
            onResult(UserOperationResult.Error("User not found"))
            return@launch
        }

        val changes = mutableMapOf<String, String>()
        if (user.email != email) changes["email"] = "${user.email} → $email"
        if (user.displayName != displayName) changes["displayName"] = "${user.displayName} → $displayName"
        if (user.role != role) changes["role"] = "${user.role} → $role"
        if (user.assignedFileIds != assignedFileIds) changes["assignedFileIds"] = "${user.assignedFileIds} → $assignedFileIds"

        appUserDao.update(
            user.copy(
                email = email,
                displayName = displayName,
                role = role,
                assignedFileIds = assignedFileIds,
                pinHash = pinHash ?: user.pinHash
            )
        )

        auditLogger.log(
            action = AuditAction.EDIT_USER,
            targetType = "AppUser",
            targetId = userId.toString(),
            targetLabel = displayName,
            details = if (changes.isNotEmpty()) changes else null
        )
        onResult(UserOperationResult.Success)
    }

    /**
     * Soft-deactivate a user — sets isActive = false, isDeleted = true.
     */
    fun deactivateUser(
        userId: Long,
        adminUserId: Long,
        onResult: (UserOperationResult) -> Unit = {}
    ) = viewModelScope.launch {
        val user = appUserDao.getUserById(userId) ?: run {
            onResult(UserOperationResult.Error("User not found"))
            return@launch
        }
        appUserDao.softDelete(userId)
        auditLogger.log(
            action = AuditAction.DEACTIVATE_USER,
            targetType = "AppUser",
            targetId = userId.toString(),
            targetLabel = user.displayName
        )
        appPreferences.setActiveUserCount(allActiveUsers.value.size - 1)
        onResult(UserOperationResult.Success)
    }

    /**
     * Change a user's role (ADMIN only).
     */
    fun changeUserRole(
        userId: Long,
        newRole: UserRole,
        adminUserId: Long,
        onResult: (UserOperationResult) -> Unit = {}
    ) = viewModelScope.launch {
        val user = appUserDao.getUserById(userId) ?: run {
            onResult(UserOperationResult.Error("User not found"))
            return@launch
        }
        val oldRole = user.role
        appUserDao.update(user.copy(role = newRole))
        auditLogger.log(
            action = AuditAction.CHANGE_ROLE,
            targetType = "AppUser",
            targetId = userId.toString(),
            targetLabel = user.displayName,
            details = mapOf("from" to oldRole.name, "to" to newRole.name)
        )
        onResult(UserOperationResult.Success)
    }

    fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(pin.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    /**
     * Get the maximum allowed users for the current plan.
     */
    fun getMaxUsersForPlan(): Int {
        return when (appPreferences.activationPlan.lowercase()) {
            "starter" -> 1
            "growth" -> 5
            "enterprise" -> Int.MAX_VALUE
            else -> Int.MAX_VALUE
        }
    }
}
