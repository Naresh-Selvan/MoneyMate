package com.moneymate.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class CalculationMode {
    FLAT,
    DURATION
}

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
    val lastUploadedAt: Long? = null,

    // ── Interest defaults (Part 1 & 2) ────────────────────────────────────────
    // Default interest rate for all new persons in this file (e.g. 25.0 = 25%)
    val defaultInterestRate: Double = 25.0,
    // FLAT = simple flat rate; DURATION = duration-based (days/365) calculation
    val defaultCalculationMode: CalculationMode = CalculationMode.FLAT
)