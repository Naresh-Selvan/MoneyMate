package com.moneymate.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "loan_files")
data class LoanFile(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val syncedToFirebase: Boolean = false,
    val lastUploadedAt: Long? = null
)