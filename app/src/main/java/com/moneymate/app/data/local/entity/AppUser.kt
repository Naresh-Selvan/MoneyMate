package com.moneymate.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a registered user of the MoneyMate system.
 * Users have roles (ADMIN/BOSS/USER) and can be assigned to specific files.
 */
@Entity(
    tableName = "app_users",
    indices = [androidx.room.Index(value = ["email"], unique = true)]
)
data class AppUser(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val displayName: String,
    val role: UserRole,
    /** Comma-separated fileIds, empty string means all files (for ADMIN/BOSS). */
    val assignedFileIds: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val createdByUserId: Long,
    val pinHash: String? = null,
    val lastLoginAt: Long? = null,
    val isDeleted: Boolean = false
)
