package com.moneymate.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * A template person for a given NLR file name (e.g. "NLR 1").
 * When a new file is created, all DefaultPersons for that nlrKey are
 * auto-inserted as real Persons (amount = 0 first time, then snapshotted from upload).
 */
@Entity(tableName = "default_persons")
data class DefaultPerson(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    /** "NLR 1" | "NLR 2" | "NLR 3" | "NLR 4" */
    val nlrKey: String,
    val name: String,
    val place: String? = null,
    val mobileNumber: String? = null,
    val amountGiven: Double = 0.0,
    val mode: PaymentMode = PaymentMode.CASH,
    val sortOrder: Int = 0,
    val recordType: LoanType = LoanType.LENDING,
    /** true = came from hardcoded seed, false = updated by upload snapshot */
    val isSeeded: Boolean = true
)