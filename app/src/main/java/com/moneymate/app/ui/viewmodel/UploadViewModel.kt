package com.moneymate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.moneymate.app.data.local.entity.LoanFile
import com.moneymate.app.data.repository.ExpenseRepository
import com.moneymate.app.data.repository.InvestmentRepository
import com.moneymate.app.data.repository.LoanFileRepository
import com.moneymate.app.data.repository.PersonRepository
import com.moneymate.app.data.repository.PaymentRepository
import com.moneymate.app.utils.FirestorePathProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class UploadState {
    object Idle : UploadState()
    object Uploading : UploadState()
    data class Success(val message: String) : UploadState()
    data class Error(val message: String) : UploadState()
}

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val loanFileRepository: LoanFileRepository,
    private val personRepository: PersonRepository,
    private val paymentRepository: PaymentRepository,
    private val expenseRepository: ExpenseRepository,
    private val investmentRepository: InvestmentRepository,
    private val paths: FirestorePathProvider          // ← injected
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

    private val db = FirebaseFirestore.getInstance()

    private val dateFmt = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())

    /**
     * Uploads a single file and ALL its persons + payments to Firestore.
     * Every field of every entity is uploaded — active, completed, deleted, pending —
     * so that a full restore brings the device back to exactly the same state.
     */
    fun uploadFile(file: LoanFile) {
        _uploadState.value = UploadState.Uploading
        viewModelScope.launch {
            try {
                // ── LoanFile — every field ────────────────────────────────────
                val fileDoc = mapOf(
                    "id"               to file.id,
                    "name"             to file.name,
                    "createdAt"        to file.createdAt,
                    "sortOrder"        to file.sortOrder,
                    "isDeleted"        to file.isDeleted,
                    "deletedAt"        to file.deletedAt,
                    "syncedToFirebase" to file.syncedToFirebase,
                    "lastUploadedAt"   to file.lastUploadedAt,
                    "permanentlyDeleted" to false,
                    "permanentlyDeletedAt" to null,
                    "purged"           to false,
                    "purgedAt"         to null
                )
                db.collection(paths.loanFilesCollection)
                    .document(file.id)
                    .set(fileDoc)
                    .await()

                // ── Persons — ALL of them, no status filter ───────────────────
                val persons = personRepository.getAllPersonsInFile(file.id)

                var personCount = 0
                var paymentCount = 0

                for (person in persons) {
                    val payments = paymentRepository.getAllPaymentsForPerson(person.id)
                    val activePaymentTotal = payments.filter { !it.isDeleted }.sumOf { it.amount }

                    val personDoc = mapOf(
                        "id"                    to person.id,
                        "fileId"                to person.fileId,
                        "name"                  to person.name,
                        "place"                 to (person.place ?: ""),
                        "mobileNumber"          to (person.mobileNumber ?: ""),
                        "amountGiven"           to person.amountGiven,
                        "mode"                  to person.mode.name,
                        "dateGiven"             to person.dateGiven,
                        "dateGivenFormatted"    to dateFmt.format(java.util.Date(person.dateGiven)),
                        "sortOrder"             to person.sortOrder,
                        "recordType"            to person.recordType.name,
                        "isDeleted"             to person.isDeleted,
                        "deletedAt"             to person.deletedAt,
                        "uploadedAt"            to person.uploadedAt,
                        "editPermissionGranted" to person.editPermissionGranted,
                        "editPermissionScope"   to person.editPermissionScope.name,
                        "isCompleted"           to person.isCompleted,
                        "completedAt"           to person.completedAt,
                        "linkedNewPersonId"     to person.linkedNewPersonId,
                        "isPendingNewLoan"      to person.isPendingNewLoan,
                        "previousPersonId"      to person.previousPersonId,
                        "interestRate"           to person.interestRate,
                        "interestType"           to person.interestType,
                        "interestAmount"         to person.interestAmount,
                        "totalRepayment"         to person.totalRepayment,
                        "loanType"               to person.loanType,
                        "numberOfInstallments"   to person.numberOfInstallments,
                        "perInstallmentAmount"   to person.perInstallmentAmount,
                        "isDurationBased"        to person.isDurationBased,
                        "durationDays"           to person.durationDays,
                        "photoUri"              to null,  // device-specific local path, not uploaded
                        "alternateMobile"       to (person.alternateMobile ?: ""),
                        "address"               to (person.address ?: ""),
                        "businessType"          to (person.businessType ?: ""),
                        "maxLoanAmount"         to (person.maxLoanAmount ?: 0.0),
                        "guarantorPersonId"     to (person.guarantorPersonId ?: ""),
                        "customerCode"          to (person.customerCode ?: ""),
                        "subCode"               to (person.subCode ?: ""),
                        "badLoanDays"           to person.badLoanDays,
                        "sendSms"               to person.sendSms,
                        "totalReceived"         to activePaymentTotal,
                        "balance"               to ((if (person.totalRepayment > 0) person.totalRepayment else person.amountGiven) - activePaymentTotal).coerceAtLeast(0.0),
                        "permanentlyDeleted"    to false,
                        "permanentlyDeletedAt"  to null,
                        "purged"                to false,
                        "purgedAt"              to null
                    )

                    db.collection(paths.personsCollection(file.id))
                        .document(person.id)
                        .set(personDoc)
                        .await()

                    personCount++

                    // ── Payments — ALL of them, including deleted ─────────────
                    for (payment in payments) {
                        val paymentDoc = mapOf(
                            "id"                    to payment.id,
                            "personId"              to payment.personId,
                            "amount"                to payment.amount,
                            "mode"                  to payment.mode.name,
                            "date"                  to payment.date,
                            "dateFormatted"         to dateFmt.format(java.util.Date(payment.date)),
                            "isDeleted"             to payment.isDeleted,
                            "deletedAt"             to payment.deletedAt,
                            "isRollover"            to payment.isRollover,
                            "uploadedAt"            to payment.uploadedAt,
                            "editPermissionGranted" to payment.editPermissionGranted,
                            "editPermissionScope"   to payment.editPermissionScope.name,
                            "permanentlyDeleted"    to false,
                            "permanentlyDeletedAt"  to null,
                            "purged"                to false,
                            "purgedAt"              to null
                        )
                        db.collection(paths.paymentsCollection(file.id, person.id))
                            .document(payment.id)
                            .set(paymentDoc)
                            .await()
                        paymentCount++
                    }
                }

                // ── Expenses — all non-deleted ───────────────────────────────
                val expenses = expenseRepository.getAllNonDeletedExpenses(file.id)
                var expenseCount = 0
                for (expense in expenses) {
                    val expenseDoc = mapOf(
                        "id"        to expense.id,
                        "fileId"    to expense.fileId,
                        "category"  to expense.category,
                        "amount"    to expense.amount,
                        "isOnline"  to expense.isOnline,
                        "date"      to expense.date,
                        "notes"     to (expense.notes ?: ""),
                        "isDeleted" to expense.isDeleted,
                        "createdAt" to expense.createdAt
                    )
                    db.collection(paths.expensesCollection(file.id))
                        .document(expense.id.toString())
                        .set(expenseDoc)
                        .await()
                    expenseCount++
                }

                // ── Investments — all non-deleted ────────────────────────────
                val investments = investmentRepository.getAllNonDeletedInvestments(file.id)
                var investmentCount = 0
                for (investment in investments) {
                    val investmentDoc = mapOf(
                        "id"        to investment.id,
                        "fileId"    to investment.fileId,
                        "type"      to investment.type,
                        "amount"    to investment.amount,
                        "isOnline"  to investment.isOnline,
                        "date"      to investment.date,
                        "notes"     to (investment.notes ?: ""),
                        "isDeleted" to investment.isDeleted,
                        "createdAt" to investment.createdAt
                    )
                    db.collection(paths.investmentsCollection(file.id))
                        .document(investment.id.toString())
                        .set(investmentDoc)
                        .await()
                    investmentCount++
                }

                personRepository.markAllUploadedInFile(file.id, System.currentTimeMillis())

                val ts = java.text.SimpleDateFormat("dd MMM HH:mm", java.util.Locale.getDefault())
                    .format(java.util.Date())
                _uploadState.value = UploadState.Success(
                    "✓ Uploaded $personCount persons, $paymentCount payments, $expenseCount expenses, $investmentCount investments at $ts"
                )
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error("Upload failed: ${e.message}")
            }
        }
    }

    /** Verify the last upload by checking Firestore has the expected total person count. */
    fun verifyUpload(file: LoanFile) {
        _uploadState.value = UploadState.Uploading
        viewModelScope.launch {
            try {
                val localCount = personRepository.getAllPersonsInFile(file.id).size
                val snapshot = db.collection(paths.personsCollection(file.id))
                    .get()
                    .await()
                val remoteCount = snapshot.size()
                if (remoteCount == 0) {
                    _uploadState.value = UploadState.Error("Firebase has 0 records — upload may not have run yet.")
                } else if (remoteCount >= localCount) {
                    _uploadState.value = UploadState.Success("✓ Firebase verified: $remoteCount/$localCount records synced.")
                } else {
                    _uploadState.value = UploadState.Error("Mismatch: Firebase has $remoteCount, local has $localCount. Try uploading again.")
                }
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error("Verify failed: ${e.message}")
            }
        }
    }

    fun resetState() {
        _uploadState.value = UploadState.Idle
    }
}