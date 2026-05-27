package com.moneymate.app.data.repository

import com.moneymate.app.data.local.dao.PaymentDao
import com.moneymate.app.data.local.entity.EditPermissionScope
import com.moneymate.app.data.local.entity.Payment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val paymentDao: PaymentDao
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

    suspend fun softDeletePayment(id: String, deletedAt: Long) =
        paymentDao.softDeletePayment(id, deletedAt)

    suspend fun restorePayment(id: String) =
        paymentDao.restorePayment(id)

    suspend fun hardDeletePayment(id: String) =
        paymentDao.hardDeletePayment(id)

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
}