package com.moneymate.app.ui.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymate.app.auth.AuditLogger
import com.moneymate.app.data.local.entity.AuditAction
import com.moneymate.app.data.local.entity.EditPermissionScope
import com.moneymate.app.data.local.entity.Payment
import com.moneymate.app.data.local.entity.Person
import com.moneymate.app.data.repository.PaymentRepository
import com.moneymate.app.data.repository.PersonRepository
import com.moneymate.app.notifications.NotificationHelper
import com.moneymate.app.utils.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val repository: PaymentRepository,
    private val personRepository: PersonRepository,
    private val notificationHelper: NotificationHelper,
    private val preferences: AppPreferences,
    private val auditLogger: AuditLogger,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _currentPersonId = MutableStateFlow<String?>(null)

    val payments: StateFlow<List<Payment>> = _currentPersonId
        .flatMapLatest { personId ->
            if (personId != null) repository.getPaymentsForPerson(personId)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedPayments: StateFlow<List<Payment>> = repository.getDeletedPayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadPaymentsForPerson(personId: String) {
        _currentPersonId.value = personId
    }

    fun insertPayment(payment: Payment) = viewModelScope.launch {
        repository.insertPayment(payment)
        notifyAndSendSms(payment)
        auditLogger.log(
            action = AuditAction.ADD_PAYMENT,
            targetType = "Payment",
            targetId = payment.id,
            targetLabel = "₹${payment.amount}",
            fileId = null
        )
    }

    /**
     * Suspend variant that awaits the Room write to complete.
     * Use this instead of [insertPayment] when the caller needs to guarantee
     * the payment is committed before proceeding (e.g., before checking whether
     * the balance is zero and marking a loan as completed).
     */
    suspend fun insertPaymentAwait(payment: Payment) {
        repository.insertPayment(payment)
        notifyAndSendSms(payment)
    }

    /**
     * After inserting a payment: fire notification + SMS if enabled.
     */
    private suspend fun notifyAndSendSms(payment: Payment) {
        val person = personRepository.getPersonById(payment.personId) ?: return

        // Notification
        if (preferences.paymentConfirmationEnabled) {
            val totalPaid = repository.getTotalPaidByPerson(payment.personId)
            val repayment = if (person.totalRepayment > 0) person.totalRepayment else person.amountGiven
            val remaining = (repayment - totalPaid).coerceAtLeast(0.0)
            notificationHelper.showPaymentConfirmation(person.name, payment.amount, remaining)
        }

        // SMS — fire-and-forget
        if (person.sendSms && !person.mobileNumber.isNullOrBlank()) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
                try {
                    withContext(Dispatchers.IO) {
                        val totalPaid = repository.getTotalPaidByPerson(payment.personId)
                        val repayment = if (person.totalRepayment > 0) person.totalRepayment else person.amountGiven
                        val remaining = (repayment - totalPaid).coerceAtLeast(0.0)
                        val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(Date(payment.date))
                        val message = "Dear ${person.name}, payment of ₹%.2f received on $dateStr. Outstanding balance: ₹%.2f. - MoneyMate"
                            .format(payment.amount, remaining)
                        SmsManager.getDefault().sendTextMessage(
                            person.mobileNumber, null, message, null, null
                        )
                    }
                } catch (_: Exception) {
                    // SMS is fire-and-forget — log but never crash
                }
            }
        }
    }

    fun updatePayment(payment: Payment) = viewModelScope.launch {
        repository.updatePayment(payment)
        auditLogger.log(
            action = AuditAction.EDIT_PAYMENT,
            targetType = "Payment",
            targetId = payment.id,
            targetLabel = "₹${payment.amount}",
            fileId = null
        )
    }

    fun softDeletePayment(id: String) = viewModelScope.launch {
        repository.softDeletePayment(id, System.currentTimeMillis())
        auditLogger.log(
            action = AuditAction.DELETE_PAYMENT,
            targetType = "Payment",
            targetId = id,
            targetLabel = "Payment #$id",
            fileId = null
        )
    }

    fun restorePayment(id: String) = viewModelScope.launch {
        repository.restorePayment(id)
    }

    fun hardDeletePayment(id: String) = viewModelScope.launch {
        repository.hardDeletePayment(id)
    }

    fun purgeExpiredPayments() = viewModelScope.launch {
        val cutoff = System.currentTimeMillis() - (180L * 24 * 60 * 60 * 1000)
        repository.purgeExpiredPayments(cutoff)
    }

    fun setEditPermission(id: String, granted: Boolean, scope: EditPermissionScope) = viewModelScope.launch {
        repository.setEditPermission(id, granted, scope)
    }

    fun markAllUploadedForPerson(personId: String) = viewModelScope.launch {
        repository.markAllUploadedForPerson(personId, System.currentTimeMillis())
    }

    private val _currentFileId = MutableStateFlow<String?>(null)

    val filePayments: StateFlow<List<Payment>> = _currentFileId
        .flatMapLatest { fileId ->
            if (fileId != null) repository.getPaymentsForFile(fileId)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Includes completed persons — used for totals so marking complete doesn't drop received
    val filePaymentsWithCompleted: StateFlow<List<Payment>> = _currentFileId
        .flatMapLatest { fileId ->
            if (fileId != null) repository.getPaymentsForFileIncludingCompleted(fileId)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadPaymentsForFile(fileId: String) {
        _currentFileId.value = fileId
    }

    /** Returns total paid per person for a list of IDs — used by LoanHistoryScreen. */
    suspend fun getTotalPaidByPersonIds(personIds: List<String>): Map<String, Double> {
        return repository.getTotalPaidByPersonIds(personIds)
    }

    /** Returns the timestamp of the latest payment for a person, or null if none. */
    suspend fun getLatestPaymentTimestamp(personId: String): Long? =
        repository.getLatestPaymentTimestamp(personId)
}