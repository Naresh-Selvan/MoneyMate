package com.moneymate.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "book_adjustments")
data class BookAdjustment(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val personId: String,
    val fileId: String,
    val discrepancyAmount: Double,
    val type: AdjustmentType,
    val reason: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val approvedByAdmin: Boolean = false
)

enum class AdjustmentType {
    BOOK_PROFIT,
    BOOK_LOSS
}
