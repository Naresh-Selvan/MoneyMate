package com.moneymate.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.moneymate.app.data.local.dao.*
import com.moneymate.app.data.local.dao.PaymentDao
import com.moneymate.app.data.local.dao.PersonDao
import com.moneymate.app.data.local.entity.EditPermissionScope
import com.moneymate.app.data.local.entity.Payment
import com.moneymate.app.utils.FirestorePathProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val paymentDao: PaymentDao,
    private val personDao: PersonDao,
    private val paths: FirestorePathProvider
) {
    fun getPaymentsForPerson(personId: String): Flow<List<Payment>> =
        paymentDao.getPaymentsForPerson(personId)

    fun getPaymentsForPersonSortedByMode(personId: String): Flow<List<Payment>> =
        paymentDao.getPaymentsForPersonSortedByMode(personId)

    /** Returns every payment for a person including deleted ones — for full cloud backup. */
    suspend fun getAllPaymentsForPerson(personId: String): List<Payment> =
        paymentDao.getAllPaymentsForPerson(personId)

    fun getPaymentsForPersonSortedByDate(personId: String): Flow<List<Payment>> =
        paymentDao.getPaymentsForPersonSortedByDate(personId)

    fun getDeletedPayments(): Flow<List<Payment>> =
        paymentDao.getDeletedPayments()

    suspend fun getPaymentById(id: String): Payment? =
        paymentDao.getPaymentById(id)

    suspend fun insertPayment(payment: Payment) =
        paymentDao.insertPayment(payment)

    suspend fun updatePayment(payment: Payment) =
        paymentDao.updatePayment(payment)

    suspend fun softDeletePayment(id: String, deletedAt: Long) {
        paymentDao.softDeletePayment(id, deletedAt)
        val payment = paymentDao.getPaymentById(id)
        if (payment != null) {
            val person = personDao.getPersonById(payment.personId)
            if (person != null) {
                try {
                    val db = FirebaseFirestore.getInstance()
                    val docRef = db.collection(paths.paymentsCollection(person.fileId, payment.personId)).document(id)
                    docRef.set(
                        mapOf(
                            "isDeleted" to true,
                            "deletedAt" to deletedAt,
                            "purged" to false,
                            "permanentlyDeleted" to false
                        ),
                        SetOptions.merge()
                    ).await()
                } catch (e: Exception) {
                    Log.e("PaymentRepository", "Firestore softDeletePayment failed for $id", e)
                }
            }
        }
    }

    suspend fun restorePayment(id: String) {
        paymentDao.restorePayment(id)
        val payment = paymentDao.getPaymentById(id)
        if (payment != null) {
            val person = personDao.getPersonById(payment.personId)
            if (person != null) {
                try {
                    val db = FirebaseFirestore.getInstance()
                    val docRef = db.collection(paths.paymentsCollection(person.fileId, payment.personId)).document(id)
                    docRef.set(
                        mapOf(
                            "isDeleted" to false,
                            "deletedAt" to null
                        ),
                        SetOptions.merge()
                    ).await()
                } catch (e: Exception) {
                    Log.e("PaymentRepository", "Firestore restorePayment failed for $id", e)
                }
            }
        }
    }

    suspend fun hardDeletePayment(id: String) {
        val payment = paymentDao.getPaymentById(id)
        paymentDao.hardDeletePayment(id)
        if (payment != null) {
            val person = personDao.getPersonById(payment.personId)
            if (person != null) {
                try {
                    val db = FirebaseFirestore.getInstance()
                    val docRef = db.collection(paths.paymentsCollection(person.fileId, payment.personId)).document(id)
                    docRef.set(
                        mapOf(
                            "isDeleted" to true,
                            "permanentlyDeleted" to true,
                            "permanentlyDeletedAt" to System.currentTimeMillis()
                        ),
                        SetOptions.merge()
                    ).await()
                } catch (e: Exception) {
                    Log.e("PaymentRepository", "Firestore hardDeletePayment failed for $id", e)
                }
            }
        }
    }

    suspend fun purgeExpiredPayments(cutoff: Long) =
        paymentDao.purgeExpiredPayments(cutoff)

    suspend fun markAllUploadedForPerson(personId: String, uploadedAt: Long) =
        paymentDao.markAllUploadedForPerson(personId, uploadedAt)

    suspend fun setEditPermission(id: String, granted: Boolean, scope: EditPermissionScope) =
        paymentDao.setEditPermission(id, granted, scope)

    suspend fun getTotalPaidByPerson(personId: String): Double =
        paymentDao.getTotalPaidByPerson(personId) ?: 0.0

    suspend fun getTotalPaidCashByPerson(personId: String): Double =
        paymentDao.getTotalPaidCashByPerson(personId) ?: 0.0

    suspend fun getTotalPaidUpiByPerson(personId: String): Double =
        paymentDao.getTotalPaidUpiByPerson(personId) ?: 0.0

    suspend fun getTotalReceivedInFile(fileId: String): Double =
        paymentDao.getTotalReceivedInFile(fileId) ?: 0.0

    suspend fun getTotalReceivedCashInFile(fileId: String): Double =
        paymentDao.getTotalReceivedCashInFile(fileId) ?: 0.0

    suspend fun getTotalReceivedUpiInFile(fileId: String): Double =
        paymentDao.getTotalReceivedUpiInFile(fileId) ?: 0.0

    fun getPaymentsForFile(fileId: String): Flow<List<Payment>> =
        paymentDao.getPaymentsForFile(fileId)

    fun getPaymentsForFileIncludingCompleted(fileId: String): Flow<List<Payment>> =
        paymentDao.getPaymentsForFileIncludingCompleted(fileId)

    /** Returns total paid per person for a list of person IDs — for loan history screen. */
    suspend fun getTotalPaidByPersonIds(personIds: List<String>): Map<String, Double> =
        paymentDao.getTotalPaidByPersonIds(personIds).associate { it.personId to it.totalPaid }

    // ── Insights ───────────────────────────────────────────────────────────────
    suspend fun getTotalReceivedToday(fileId: String, startOfDay: Long, endOfDay: Long): Double =
        paymentDao.getTotalReceivedToday(fileId, startOfDay, endOfDay)

    suspend fun getTotalReceivedThisWeek(fileId: String, weekStart: Long, weekEnd: Long): Double =
        paymentDao.getTotalReceivedThisWeek(fileId, weekStart, weekEnd)

    suspend fun getLatestPaymentTimestamp(personId: String): Long? =
        paymentDao.getLatestPaymentTimestamp(personId)

    suspend fun getTotalPaidTodayByPersonIds(personIds: List<String>, startOfDay: Long, endOfDay: Long): List<com.moneymate.app.data.local.dao.PersonTotalPaid> =
        paymentDao.getTotalPaidTodayByPersonIds(personIds, startOfDay, endOfDay)

    suspend fun getTotalCollectionToday(fileId: String, startOfDay: Long, endOfDay: Long): Double =
        paymentDao.getTotalCollectionToday(fileId, startOfDay, endOfDay)

    // ════════════════════════════════════════════════════════════════════════
    // Phase 4 — Reports
    // ════════════════════════════════════════════════════════════════════════

    fun getPlanReport(fileId: String, from: Long, to: Long): kotlinx.coroutines.flow.Flow<List<PlanEntry>> =
        paymentDao.getPlanReport(fileId, from, to)

    fun getDailySummary(fileId: String, from: Long, to: Long): kotlinx.coroutines.flow.Flow<List<DailySummaryEntry>> =
        paymentDao.getDailySummary(fileId, from, to)

    fun getLineSummary(fileId: String, from: Long, to: Long): kotlinx.coroutines.flow.Flow<List<LineSummaryEntry>> =
        paymentDao.getLineSummary(fileId, from, to)

    fun getOnlineCollections(fileId: String, from: Long, to: Long): kotlinx.coroutines.flow.Flow<List<OnlineCollectionEntry>> =
        paymentDao.getOnlineCollections(fileId, from, to)

    fun getLoanSummary(fileId: String): kotlinx.coroutines.flow.Flow<List<LoanSummaryEntry>> =
        paymentDao.getLoanSummary(fileId)

    fun getCompletedLoans(fileId: String, from: Long, to: Long): kotlinx.coroutines.flow.Flow<List<CompletedLoanEntry>> =
        paymentDao.getCompletedLoans(fileId, from, to)

    fun getLoanAnalysis(fileId: String, from: Long, to: Long): kotlinx.coroutines.flow.Flow<List<LoanAnalysisEntry>> =
        paymentDao.getLoanAnalysis(fileId, from, to)

    fun getLedgerEntries(personId: String): kotlinx.coroutines.flow.Flow<List<LedgerEntry>> =
        paymentDao.getLedgerEntries(personId)

    fun getBookExcessLoss(fileId: String): kotlinx.coroutines.flow.Flow<List<ExcessEntry>> =
        paymentDao.getBookExcessLoss(fileId)

    suspend fun getSiteTotalCollected(from: Long, to: Long): Double =
        paymentDao.getSiteTotalCollected(from, to)
}