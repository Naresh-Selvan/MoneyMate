package com.moneymate.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Immutable audit trail entry recording every mutation in the system.
 */
@Entity(
    tableName = "audit_logs",
    indices = [
        androidx.room.Index("userId"),
        androidx.room.Index("fileId"),
        androidx.room.Index("targetType", "targetId"),
        androidx.room.Index("timestamp")
    ]
)
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userEmail: String,
    val action: AuditAction,
    val targetType: String,
    val targetId: String,
    val targetLabel: String,
    val details: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val fileId: String? = null
)
