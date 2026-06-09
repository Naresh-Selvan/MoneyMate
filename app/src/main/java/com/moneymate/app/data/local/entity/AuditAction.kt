package com.moneymate.app.data.local.entity

/**
 * All tracked audit actions in MoneyMate.
 */
enum class AuditAction {
    ADD_PERSON,
    EDIT_PERSON,
    DELETE_PERSON,
    MOVE_PERSON,
    ADD_PAYMENT,
    EDIT_PAYMENT,
    DELETE_PAYMENT,
    ADD_LOAN,
    EDIT_LOAN,
    CLOSE_LOAN,
    ADD_EXPENSE,
    EDIT_EXPENSE,
    DELETE_EXPENSE,
    ADD_FILE,
    RENAME_FILE,
    DELETE_FILE,
    MOVE_FILE,
    ADD_USER,
    EDIT_USER,
    DEACTIVATE_USER,
    CHANGE_ROLE,
    EXPORT_REPORT,
    LOGIN,
    LOGOUT
}
