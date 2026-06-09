package com.moneymate.app.auth

import com.moneymate.app.data.local.entity.Permission
import com.moneymate.app.data.local.entity.UserRole

/**
 * Defines which [UserRole] has access to which [Permission].
 * ADMIN = full access, BOSS = elevated, USER = collection only.
 */
object RolePermissionMap {

    private val permissions: Map<UserRole, Set<Permission>> = mapOf(
        UserRole.ADMIN to Permission.entries.toSet(),
        UserRole.BOSS to setOf(
            Permission.VIEW_ALL_FILES,
            Permission.VIEW_ASSIGNED_FILES,
            Permission.ADD_PERSON,
            Permission.EDIT_PERSON,
            Permission.DELETE_PERSON,
            Permission.MOVE_PERSON,
            Permission.ADD_PAYMENT,
            Permission.EDIT_PAYMENT,
            Permission.VIEW_REPORTS,
            Permission.EXPORT_REPORTS,
            Permission.ADD_EXPENSE,
            Permission.VIEW_EXPENSE,
            Permission.MANAGE_SETTINGS,
            Permission.VIEW_AUDIT_LOG,
            Permission.FORCE_CLOSE_LOAN
        ),
        UserRole.USER to setOf(
            Permission.VIEW_ASSIGNED_FILES,
            Permission.ADD_PERSON,
            Permission.EDIT_PERSON,
            Permission.ADD_PAYMENT,
            Permission.EDIT_PAYMENT
        )
    )

    fun hasPermission(role: UserRole, permission: Permission): Boolean {
        return permissions[role]?.contains(permission) == true
    }

    fun getAllowedRolesForPermission(permission: Permission): Set<UserRole> {
        return permissions.filterValues { it.contains(permission) }.keys
    }

    fun canAccessFile(role: UserRole, assignedFileIds: String, fileId: String): Boolean {
        return when (role) {
            UserRole.ADMIN, UserRole.BOSS -> true
            UserRole.USER -> {
                assignedFileIds.isBlank() || assignedFileIds.split(",").any { it.trim() == fileId }
            }
        }
    }
}
