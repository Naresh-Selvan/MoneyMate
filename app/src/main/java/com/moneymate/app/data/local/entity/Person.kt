package com.moneymate.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "persons",
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
data class Person(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val fileId: String,
    val name: String,
    val place: String? = null,
    val mobileNumber: String? = null,
    val amountGiven: Double,
    val mode: PaymentMode = PaymentMode.CASH,
    val dateGiven: Long = System.currentTimeMillis(),
    val sortOrder: Int = 0,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val uploadedAt: Long? = null,
    val editPermissionGranted: Boolean = false,
    val editPermissionScope: EditPermissionScope = EditPermissionScope.NONE,
    // LENDING = I gave money to this person, BORROWING = I borrowed from this person
    val recordType: LoanType = LoanType.LENDING,

    // ── Completion / rollover fields ──────────────────────────────────────────
    // isCompleted = true when this person has fully repaid and is moved to the Completed section.
    // completedAt = timestamp when marked complete (drives 30-day auto-delete from Completed section).
    // linkedNewPersonId = ID of the zero-placeholder record created when this one is completed.
    // isPendingNewLoan = true on the zero-placeholder; excluded from amount totals, shown as "Pending New Loan".
    // previousPersonId = back-link from placeholder to the completed record (shown on upload).
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val linkedNewPersonId: String? = null,
    val isPendingNewLoan: Boolean = false,
    val previousPersonId: String? = null
)

enum class PaymentMode {
    CASH, UPI
}

enum class EditPermissionScope {
    NONE,
    THIS_RECORD,
    ALL_LOCKED
}

enum class LoanType {
    LENDING,   // I lent money — I gave, they owe me
    BORROWING  // I borrowed money — they gave, I owe them
}
