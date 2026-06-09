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
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val linkedNewPersonId: String? = null,
    val isPendingNewLoan: Boolean = false,
    val previousPersonId: String? = null,

    // ── Interest fields (Part 4) ──────────────────────────────────────────────
    // Per-person interest rate (inherited from file default, but user can override)
    val interestRate: Double = 0.0,
    // Interest type: "PERCENTAGE" for flat %, "FIXED_AMOUNT" for custom fixed amount
    val interestType: String = "PERCENTAGE",
    // Calculated flat interest amount = principal × (rate/100) or custom fixed amount
    val interestAmount: Double = 0.0,
    // Total repayment = principal + interestAmount
    val totalRepayment: Double = 0.0,
    // Installment type chosen by user
    val loanType: String = "MONTHLY",  // DAILY / WEEKLY / MONTHLY
    // Number of installments
    val numberOfInstallments: Int = 10,
    // Per installment = totalRepayment / numberOfInstallments
    val perInstallmentAmount: Double = 0.0,
    // Advanced mode: use duration-based calculation instead of flat rate
    val isDurationBased: Boolean = false,
    // Duration in days (nullable; used only when isDurationBased = true)
    val durationDays: Int? = null,

    // ── Phase 1 Customer Enhancements ──────────────────────────────────────────
    /** Local file path for person's photo */
    val photoUri: String? = null,
    /** Alternate mobile number */
    val alternateMobile: String? = null,
    /** Address of the person */
    val address: String? = null,
    /** Type of business */
    val businessType: String? = null,
    /** Maximum loan amount allowed for this person */
    val maxLoanAmount: Double? = null,
    /** ID of the guarantor person (references another Person in same file) */
    val guarantorPersonId: String? = null,
    /** Customer code — unique within file */
    val customerCode: String? = null,
    /** Sub-code for the person */
    val subCode: String? = null,
    /** Number of days after which a loan is considered "bad" if no payment received */
    val badLoanDays: Int = 90,
    /** Whether to send SMS notification when a payment is received */
    val sendSms: Boolean = false
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