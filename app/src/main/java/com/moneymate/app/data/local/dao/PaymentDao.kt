package com.moneymate.app.data.local.dao

import androidx.room.*
import com.moneymate.app.data.local.entity.EditPermissionScope
import com.moneymate.app.data.local.entity.Payment
import kotlinx.coroutines.flow.Flow

data class PersonTotalPaid(val personId: String, val totalPaid: Double)

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
}