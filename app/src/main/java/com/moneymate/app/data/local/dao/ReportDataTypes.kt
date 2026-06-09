package com.moneymate.app.data.local.dao

/** Report 1 — Plan Entry */
data class PlanEntry(
    val personName: String,
    val loanAmount: Double,
    val installmentAmount: Double,
    val paidCount: Int,
    val totalInstallments: Int,
    val collectedToday: Double,
    val balance: Double,
    val place: String?
)

/** Report 2 — Daily Summary Entry */
data class DailySummaryEntry(
    val personName: String,
    val installAmount: Double,
    val paidAmount: Double,
    val paymentMode: String,
    val place: String?
)

/** Report 3 — Line Summary Entry (grouped by date) */
data class LineSummaryEntry(
    val date: Long,
    val totalCollected: Double,
    val totalOnline: Double,
    val totalCash: Double,
    val totalExpense: Double,
    val netBalance: Double
)

/** Report 4 — Online Collection Entry */
data class OnlineCollectionEntry(
    val personName: String,
    val date: Long,
    val amount: Double,
    val paymentMode: String
)

/** Report 6 — Category Summary (expenses grouped by category) */
data class CategorySummary(
    val category: String,
    val cashTotal: Double = 0.0,
    val onlineTotal: Double = 0.0,
    val grandTotal: Double = 0.0
)

/** Report 7/8 — Investment Summary grouped by type */
data class InvestmentCategorySummary(
    val type: String,
    val cashTotal: Double = 0.0,
    val onlineTotal: Double = 0.0,
    val grandTotal: Double = 0.0
)

/** Report 9 — Book Excess Loss Entry */
data class ExcessEntry(
    val personName: String,
    val loanAmount: Double,
    val totalPaid: Double,
    val excessAmount: Double
)

/** Report 10 — Loan Summary Entry */
data class LoanSummaryEntry(
    val name: String,
    val loanAmount: Double,
    val interest: Double,
    val installAmount: Double,
    val totalInstallments: Int,
    val paidCount: Int,
    val balance: Double,
    val startDate: Long,
    val endDate: Long,
    val status: String,
    val personId: String
)

/** Report 14 — Completed Loan Entry */
data class CompletedLoanEntry(
    val name: String,
    val loanAmount: Double,
    val totalCollected: Double,
    val completionDate: Long,
    val durationDays: Long
)

/** Report 19 — Loan Analysis Entry (grouped by date) */
data class LoanAnalysisEntry(
    val date: Long,
    val activeLoans: Int,
    val completedLoans: Int,
    val totalDisbursed: Double,
    val totalCollected: Double
) {
    val newLoans: Int get() = 0
}

/** Report 19 — Loan Not Taken Entry */
data class LoanNotTakenEntry(
    val personId: String,
    val personName: String,
    val loanAmount: Double,
    val dateGiven: Long,
    val place: String?
)

/** Report 20 — Ledger Entry */
data class LedgerEntry(
    val date: Long,
    val type: String,
    val amount: Double,
    val mode: String,
    val paymentId: String
)

/** Report 5 — Site Dashboard */
data class SiteDashboard(
    val totalActiveLoans: Int = 0,
    val totalOutstanding: Double = 0.0,
    val totalCollectedThisMonth: Double = 0.0,
    val totalNewLoansThisMonth: Double = 0.0,
    val totalExpensesThisMonth: Double = 0.0
)
