package com.moneymate.app.data.local.entity

/**
 * Defines the three user tiers in MoneyMate.
 * - ADMIN: full access, can manage users, see all files, all settings
 * - BOSS: elevated — all collection + reports, cannot manage users or license
 * - USER: collection only — assigned files only, no reports, no settings
 */
enum class UserRole {
    ADMIN,
    BOSS,
    USER
}
