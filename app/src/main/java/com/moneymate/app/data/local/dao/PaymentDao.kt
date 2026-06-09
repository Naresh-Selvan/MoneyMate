package com.moneymate.app.data.local.dao

import androidx.room.*
import com.moneymate.app.data.local.entity.EditPermissionScope
import com.moneymate.app.data.local.entity.Payment
import kotlinx.coroutines.flow.Flow

data class PersonTotalPaid(val personId: String, val totalPaid: Double)

data class BadLoanResult(
    val personId: String,
    val personName: String,
    val fileId: String,
    val daysOverdue: Long,
    val balance: Double
)

@Dao
interface PaymentDao {

    @Query("SELECT personId, SUM(amount) as totalPaid FROM payments WHERE personId IN (:personIds) AND isDeleted = 0 GROUP BY personId")
    suspend fun getTotalPaidByPersonIds(personIds: List<String>): List<PersonTotalPaid>

    @Query("SELECT * FROM payments WHERE personId = :personId AND isDeleted = 0 ORDER BY date DESC")
    fun getPaymentsForPerson(personId: String): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE personId = :personId AND isDeleted = 0 ORDER BY mode ASC")
    fun getPaymentsForPersonSortedByMode(personId: String): Flow<List<Payment>>

    // ── Full backup — every payment for a person, including deleted ───────────
    @Query("SELECT * FROM payments WHERE personId = :personId ORDER BY date ASC")
    suspend fun getAllPaymentsForPerson(personId: String): List<Payment>

    @Query("SELECT * FROM payments WHERE personId = :personId AND isDeleted = 0 ORDER BY date ASC")
    fun getPaymentsForPersonSortedByDate(personId: String): Flow<List<Payment>>

    @Query("SELECT * FROM payments WHERE id = :id")
    suspend fun getPaymentById(id: String): Payment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment)

    @Update
    suspend fun updatePayment(payment: Payment)

    @Query("UPDATE payments SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeletePayment(id: String, deletedAt: Long)

    @Query("UPDATE payments SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restorePayment(id: String)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun hardDeletePayment(id: String)

    @Query("SELECT * FROM payments WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedPayments(): Flow<List<Payment>>

    @Query("DELETE FROM payments WHERE isDeleted = 1 AND deletedAt < :cutoff")
    suspend fun purgeExpiredPayments(cutoff: Long)

    @Query("UPDATE payments SET uploadedAt = :uploadedAt WHERE personId = :personId AND isDeleted = 0")
    suspend fun markAllUploadedForPerson(personId: String, uploadedAt: Long)

    @Query("UPDATE payments SET editPermissionGranted = :granted, editPermissionScope = :scope WHERE id = :id")
    suspend fun setEditPermission(id: String, granted: Boolean, scope: EditPermissionScope)

    @Query("SELECT SUM(amount) FROM payments WHERE personId = :personId AND isDeleted = 0")
    suspend fun getTotalPaidByPerson(personId: String): Double?

    @Query("SELECT SUM(amount) FROM payments WHERE personId = :personId AND isDeleted = 0 AND mode = 'CASH'")
    suspend fun getTotalPaidCashByPerson(personId: String): Double?

    @Query("SELECT SUM(amount) FROM payments WHERE personId = :personId AND isDeleted = 0 AND mode = 'UPI'")
    suspend fun getTotalPaidUpiByPerson(personId: String): Double?

    @Query("""
        SELECT SUM(p.amount) FROM payments p
        INNER JOIN persons pr ON p.personId = pr.id
        WHERE pr.fileId = :fileId AND p.isDeleted = 0 AND pr.isDeleted = 0
    """)
    suspend fun getTotalReceivedInFile(fileId: String): Double?

    @Query("""
        SELECT SUM(p.amount) FROM payments p
        INNER JOIN persons pr ON p.personId = pr.id
        WHERE pr.fileId = :fileId AND p.isDeleted = 0 AND p.mode = 'CASH' AND pr.isDeleted = 0
    """)
    suspend fun getTotalReceivedCashInFile(fileId: String): Double?

    @Query("""
        SELECT SUM(p.amount) FROM payments p
        INNER JOIN persons pr ON p.personId = pr.id
        WHERE pr.fileId = :fileId AND p.isDeleted = 0 AND p.mode = 'UPI' AND pr.isDeleted = 0
    """)
    suspend fun getTotalReceivedUpiInFile(fileId: String): Double?

    // Active persons only — used for per-person balance and file-level balance
    @Query("""
        SELECT p.* FROM payments p
        INNER JOIN persons pr ON p.personId = pr.id
        WHERE pr.fileId = :fileId AND p.isDeleted = 0 AND pr.isDeleted = 0 AND pr.isCompleted = 0
    """)
    fun getPaymentsForFile(fileId: String): Flow<List<Payment>>

    // Includes completed persons — used for completed section and file-level received totals
    @Query("""
        SELECT p.* FROM payments p
        INNER JOIN persons pr ON p.personId = pr.id
        WHERE pr.fileId = :fileId AND p.isDeleted = 0 AND pr.isDeleted = 0
    """)
    fun getPaymentsForFileIncludingCompleted(fileId: String): Flow<List<Payment>>

    // ── Insights queries ───────────────────────────────────────────────────────
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0) FROM payments p
        INNER JOIN persons pr ON p.personId = pr.id
        WHERE pr.fileId = :fileId AND p.isDeleted = 0 AND pr.isDeleted = 0
          AND p.date >= :startOfDay AND p.date < :endOfDay
    """)
    suspend fun getTotalReceivedToday(fileId: String, startOfDay: Long, endOfDay: Long): Double

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0) FROM payments p
        INNER JOIN persons pr ON p.personId = pr.id
        WHERE pr.fileId = :fileId AND p.isDeleted = 0 AND pr.isDeleted = 0
          AND p.date >= :weekStart AND p.date < :weekEnd
    """)
    suspend fun getTotalReceivedThisWeek(fileId: String, weekStart: Long, weekEnd: Long): Double

    @Query("SELECT * FROM payments WHERE isDeleted = 1 AND deletedAt < :cutoff")
    suspend fun getExpiredDeletedPayments(cutoff: Long): List<Payment>

    @Query("SELECT MAX(date) FROM payments WHERE personId = :personId AND isDeleted = 0")
    suspend fun getLatestPaymentTimestamp(personId: String): Long?

    // ── Phase 2 Collection Screen ─────────────────────────────────────────────
    /** Returns total payments received today for a set of person IDs. */
    @Query("""
        SELECT personId, COALESCE(SUM(amount), 0) as totalPaid
        FROM payments
        WHERE personId IN (:personIds)
          AND isDeleted = 0
          AND date >= :startOfDay AND date < :endOfDay
        GROUP BY personId
    """)
    suspend fun getTotalPaidTodayByPersonIds(personIds: List<String>, startOfDay: Long, endOfDay: Long): List<PersonTotalPaid>

    /** Returns total collection across the entire file for today. */
    @Query("""
        SELECT COALESCE(SUM(p.amount), 0) FROM payments p
        INNER JOIN persons pr ON p.personId = pr.id
        WHERE pr.fileId = :fileId AND p.isDeleted = 0 AND pr.isDeleted = 0
          AND p.date >= :startOfDay AND p.date < :endOfDay
    """)
    suspend fun getTotalCollectionToday(fileId: String, startOfDay: Long, endOfDay: Long): Double

    // ════════════════════════════════════════════════════════════════════════
    // Phase 4 — Reports
    // ════════════════════════════════════════════════════════════════════════

    // Report 1 — Plan
    @Query("""
        SELECT pr.name as personName, pr.amountGiven as loanAmount, pr.perInstallmentAmount as installmentAmount,
               (SELECT COUNT(*) FROM payments WHERE personId = pr.id AND isDeleted = 0) as paidCount,
               pr.numberOfInstallments as totalInstallments,
               COALESCE((SELECT SUM(amount) FROM payments WHERE personId = pr.id AND isDeleted = 0 AND date >= :from AND date <= :to), 0) as collectedToday,
               (COALESCE(pr.totalRepayment, pr.amountGiven) - COALESCE((SELECT SUM(amount) FROM payments WHERE personId = pr.id AND isDeleted = 0), 0)) as balance,
               pr.place
        FROM persons pr
        WHERE pr.fileId = :fileId AND pr.isDeleted = 0 AND pr.isCompleted = 0 AND pr.isPendingNewLoan = 0
        ORDER BY pr.sortOrder ASC
    """)
    fun getPlanReport(fileId: String, from: Long, to: Long): kotlinx.coroutines.flow.Flow<List<PlanEntry>>

    // Report 2 — Daily Summary
    @Query("""
        SELECT pr.name as personName, pr.perInstallmentAmount as installAmount,
               p.amount as paidAmount, p.mode as paymentMode, pr.place
        FROM payments p
        INNER JOIN persons pr ON p.personId = pr.id
        WHERE pr.fileId = :fileId AND p.isDeleted = 0 AND pr.isDeleted = 0
          AND p.date >= :from AND p.date < :to
        ORDER BY pr.recordType, pr.sortOrder
    """)
    fun getDailySummary(fileId: String, from: Long, to: Long): kotlinx.coroutines.flow.Flow<List<DailySummaryEntry>>

    // Report 3 — Line Summary
    @Query("""
        SELECT p.date as date,
               COALESCE(SUM(p.amount), 0) as totalCollected,
               COALESCE(SUM(CASE WHEN p.mode = 'UPI' THEN p.amount ELSE 0 END), 0) as totalOnline,
               COALESCE(SUM(CASE WHEN p.mode = 'CASH' THEN p.amount ELSE 0 END), 0) as totalCash,
               0.0 as totalExpense,
               0.0 as netBalance
        FROM payments p
        INNER JOIN persons pr ON p.personId = pr.id
        WHERE pr.fileId = :fileId AND p.isDeleted = 0 AND pr.isDeleted = 0
          AND p.date >= :from AND p.date <= :to
        GROUP BY p.date
        ORDER BY p.date ASC
    """)
    fun getLineSummary(fileId: String, from: Long, to: Long): kotlinx.coroutines.flow.Flow<List<LineSummaryEntry>>

    // Report 4 — Online Collection Summary
    @Query("""
        SELECT pr.name as personName, p.date as date, p.amount as amount, p.mode as paymentMode
        FROM payments p
        INNER JOIN persons pr ON p.personId = pr.id
        WHERE pr.fileId = :fileId AND p.isDeleted = 0 AND pr.isDeleted = 0
          AND p.mode = 'UPI' AND p.date >= :from AND p.date <= :to
        ORDER BY p.date DESC
    """)
    fun getOnlineCollections(fileId: String, from: Long, to: Long): kotlinx.coroutines.flow.Flow<List<OnlineCollectionEntry>>

    // Report 10 — Loan Summary
    @Query("""
        SELECT pr.name as name, pr.amountGiven as loanAmount, pr.interestRate as interest,
               pr.perInstallmentAmount as installAmount, pr.numberOfInstallments as totalInstallments,
               (SELECT COUNT(*) FROM payments WHERE personId = pr.id AND isDeleted = 0) as paidCount,
               (COALESCE(pr.totalRepayment, pr.amountGiven) - COALESCE((SELECT SUM(amount) FROM payments WHERE personId = pr.id AND isDeleted = 0), 0)) as balance,
               pr.dateGiven as startDate,
               CASE WHEN pr.numberOfInstallments > 0 AND pr.perInstallmentAmount > 0
                    THEN pr.dateGiven + (pr.numberOfInstallments * CASE WHEN pr.loanType = 'DAILY' THEN 86400000 WHEN pr.loanType = 'WEEKLY' THEN 604800000 ELSE 2592000000 END)
                    ELSE pr.dateGiven END as endDate,
               CASE WHEN (SELECT COALESCE(SUM(amount), 0) FROM payments WHERE personId = pr.id AND isDeleted = 0) >= COALESCE(pr.totalRepayment, pr.amountGiven) AND pr.amountGiven > 0
                    THEN 'Paid' ELSE 'Active' END as status,
               pr.id as personId
        FROM persons pr
        WHERE pr.fileId = :fileId AND pr.isDeleted = 0 AND pr.isCompleted = 0 AND pr.isPendingNewLoan = 0
        ORDER BY pr.sortOrder ASC
    """)
    fun getLoanSummary(fileId: String): kotlinx.coroutines.flow.Flow<List<LoanSummaryEntry>>

    // Report 14 — Completed Loans
    @Query("""
        SELECT pr.name as name, pr.amountGiven as loanAmount,
               COALESCE((SELECT SUM(amount) FROM payments WHERE personId = pr.id AND isDeleted = 0), 0) as totalCollected,
               pr.completedAt as completionDate,
               (CASE WHEN pr.dateGiven > 0 THEN (pr.completedAt - pr.dateGiven) / 86400000 ELSE 0 END) as durationDays
        FROM persons pr
        WHERE pr.fileId = :fileId AND pr.isDeleted = 0 AND pr.isCompleted = 1
          AND pr.completedAt >= :from AND pr.completedAt <= :to
        ORDER BY pr.completedAt DESC
    """)
    fun getCompletedLoans(fileId: String, from: Long, to: Long): kotlinx.coroutines.flow.Flow<List<CompletedLoanEntry>>

    // Report 19 — Loan Analysis
    @Query("""
        SELECT pr.dateGiven as date,
               COUNT(DISTINCT CASE WHEN pr.isCompleted = 0 THEN pr.id END) as activeLoans,
               COUNT(DISTINCT CASE WHEN pr.isCompleted = 1 THEN pr.id END) as completedLoans,
               COALESCE(SUM(CASE WHEN pr.dateGiven >= :from AND pr.dateGiven <= :to THEN pr.amountGiven ELSE 0 END), 0) as totalDisbursed,
               COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.personId IN (SELECT id FROM persons WHERE fileId = :fileId AND isDeleted = 0) AND p.date >= :from AND p.date <= :to AND p.isDeleted = 0), 0) as totalCollected
        FROM persons pr
        WHERE pr.fileId = :fileId AND pr.isDeleted = 0 AND pr.isPendingNewLoan = 0
        GROUP BY pr.dateGiven
        ORDER BY pr.dateGiven ASC
    """)
    fun getLoanAnalysis(fileId: String, from: Long, to: Long): kotlinx.coroutines.flow.Flow<List<LoanAnalysisEntry>>

    // Report 20 — Ledger Entries
    @Query("""
        SELECT p.date as date, 'PAYMENT' as type, p.amount as amount, p.mode as mode, p.id as paymentId
        FROM payments p
        WHERE p.personId = :personId AND p.isDeleted = 0
        ORDER BY p.date ASC
    """)
    fun getLedgerEntries(personId: String): kotlinx.coroutines.flow.Flow<List<LedgerEntry>>

    // Report 9 — Book Excess Loss (overpaid persons)
    @Query("""
        SELECT pr.name as personName, pr.amountGiven as loanAmount,
               COALESCE((SELECT SUM(amount) FROM payments WHERE personId = pr.id AND isDeleted = 0), 0) as totalPaid,
               (COALESCE((SELECT SUM(amount) FROM payments WHERE personId = pr.id AND isDeleted = 0), 0) - COALESCE(pr.totalRepayment, pr.amountGiven)) as excessAmount
        FROM persons pr
        WHERE pr.fileId = :fileId AND pr.isDeleted = 0 AND pr.isCompleted = 1
          AND (COALESCE((SELECT SUM(amount) FROM payments WHERE personId = pr.id AND isDeleted = 0), 0) > COALESCE(pr.totalRepayment, pr.amountGiven))
        ORDER BY excessAmount DESC
    """)
    fun getBookExcessLoss(fileId: String): kotlinx.coroutines.flow.Flow<List<ExcessEntry>>

    // ── Site-level (across ALL files) report queries ───────────────────────────

    @Query("""
        SELECT COALESCE(SUM(p.amount), 0) FROM payments p
        INNER JOIN persons pr ON p.personId = pr.id
        WHERE p.isDeleted = 0 AND pr.isDeleted = 0
          AND p.date >= :from AND p.date <= :to
    """)
    suspend fun getSiteTotalCollected(from: Long, to: Long): Double

    // ── Phase 6 — Notification workers (across ALL files) ───────────────────────

    /**
     * Count of distinct active persons who have NOT made any payment today
     * across ALL files. Used by [DailyCollectionReminderWorker].
     */
    @Query("""
        SELECT COUNT(*) FROM (
            SELECT pr.id FROM persons pr
            WHERE pr.isDeleted = 0 AND pr.isCompleted = 0 AND pr.isPendingNewLoan = 0
              AND pr.id NOT IN (
                  SELECT DISTINCT p.personId FROM payments p
                  WHERE p.isDeleted = 0 AND p.date >= :todayStart AND p.date < :todayEnd
              )
        )
    """)
    suspend fun getPendingCollectionsTodayAllFiles(todayStart: Long, todayEnd: Long): Int

    /**
     * Bad loans across ALL files — persons whose last payment date exceeds
     * their [Person.badLoanDays] threshold. Returns a list of [BadLoanResult].
     * Used by [BadLoanAlertWorker].
     */
    @Query("""
        SELECT pr.id as personId, pr.name as personName, pr.fileId as fileId,
               ((:cutoffDate - (SELECT MAX(p.date) FROM payments p WHERE p.personId = pr.id AND p.isDeleted = 0)) / 86400000) as daysOverdue,
               (COALESCE(pr.totalRepayment, pr.amountGiven) - COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.personId = pr.id AND p.isDeleted = 0), 0)) as balance
        FROM persons pr
        WHERE pr.isDeleted = 0 AND pr.isCompleted = 0 AND pr.isPendingNewLoan = 0
          AND (SELECT MAX(p.date) FROM payments p WHERE p.personId = pr.id AND p.isDeleted = 0) IS NOT NULL
          AND ((:cutoffDate - (SELECT MAX(p.date) FROM payments p WHERE p.personId = pr.id AND p.isDeleted = 0)) / 86400000) >= pr.badLoanDays
        ORDER BY daysOverdue DESC
    """)
    suspend fun getBadLoansAllFiles(cutoffDate: Long): List<BadLoanResult>
}