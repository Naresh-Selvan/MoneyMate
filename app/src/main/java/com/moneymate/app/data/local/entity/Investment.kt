package com.moneymate.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "investments",
    foreignKeys = [
        ForeignKey(
            entity = LoanFile::class,
            parentColumns = ["id"],
            childColumns = ["fileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("fileId")]
)
data class Investment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileId: String,
    val type: String,
    val amount: Double,
    val isOnline: Boolean = false,
    val date: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
