package com.moneymate.app.data.local.entity

/**
 * Every sensitive action maps to a permission checked via SessionManager.hasPermission().
 */
enum class Permission {
    VIEW_ALL_FILES,
    VIEW_ASSIGNED_FILES,
    ADD_PERSON,
    EDIT_PERSON,
    DELETE_PERSON,
    MOVE_PERSON,
    ADD_PAYMENT,
    EDIT_PAYMENT,
    DELETE_PAYMENT,
    VIEW_REPORTS,
    EXPORT_REPORTS,
    ADD_EXPENSE,
    VIEW_EXPENSE,
    MANAGE_USERS,
    MANAGE_LICENSE,
    MANAGE_SETTINGS,
    VIEW_AUDIT_LOG,
    FORCE_CLOSE_LOAN
}
