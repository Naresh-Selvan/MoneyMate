package com.moneymate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.moneymate.app.data.local.entity.LoanFile
import com.moneymate.app.data.repository.DefaultPersonRepository
import com.moneymate.app.data.repository.LoanFileRepository
import com.moneymate.app.data.repository.PersonRepository
import com.moneymate.app.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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
    private val defaultPersonRepository: DefaultPersonRepository
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

    private val db = FirebaseFirestore.getInstance()

    private val nlrKeys = listOf("NLR 1", "NLR 2", "NLR 3", "NLR 4")

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
                    "lastUploadedAt"   to file.lastUploadedAt
                )
                db.collection("boss_data")
                    .document("files")
                    .collection("loan_files")
                    .document(file.id)
                    .set(fileDoc)
                    .await()

                // ── Persons — ALL of them, no status filter ───────────────────
                // This includes: active, completed, pending-new-loan, and soft-deleted
                val persons = personRepository.getAllPersonsInFile(file.id)

                var personCount = 0
                var paymentCount = 0

                for (person in persons) {
                    // ── Person — every field ──────────────────────────────────
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
                        // status flags
                        "isDeleted"             to person.isDeleted,
                        "deletedAt"             to person.deletedAt,
                        "uploadedAt"            to person.uploadedAt,
                        "editPermissionGranted" to person.editPermissionGranted,
                        "editPermissionScope"   to person.editPermissionScope.name,
                        // completion / rollover
                        "isCompleted"           to person.isCompleted,
                        "completedAt"           to person.completedAt,
                        "linkedNewPersonId"     to person.linkedNewPersonId,
                        "isPendingNewLoan"      to person.isPendingNewLoan,
                        "previousPersonId"      to person.previousPersonId,
                        // computed helpers (for quick display without re-querying)
                        "totalReceived"         to activePaymentTotal,
                        "balance"               to (person.amountGiven - activePaymentTotal)
                    )

                    db.collection("boss_data")
                        .document("files")
                        .collection("loan_files")
                        .document(file.id)
                        .collection("persons")
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
                            "editPermissionScope"   to payment.editPermissionScope.name
                        )
                        db.collection("boss_data")
                            .document("files")
                            .collection("loan_files")
                            .document(file.id)
                            .collection("persons")
                            .document(person.id)
                            .collection("payments")
                            .document(payment.id)
                            .set(paymentDoc)
                            .await()
                        paymentCount++
                    }
                }

                // Mark uploaded timestamp locally (only on active, non-deleted persons)
                personRepository.markAllUploadedInFile(file.id, System.currentTimeMillis())

                // ── Snapshot active persons as new template for this NLR ──────
                val nlrKey = nlrKeys.firstOrNull { file.name.equals(it, ignoreCase = true) }
                if (nlrKey != null) {
                    val activePersons = persons.filter { !it.isDeleted && !it.isCompleted && !it.isPendingNewLoan }
                    defaultPersonRepository.snapshotFromPersons(nlrKey, activePersons)
                }

                val ts = java.text.SimpleDateFormat("dd MMM HH:mm", java.util.Locale.getDefault())
                    .format(java.util.Date())
                val templateNote = if (nlrKey != null) " · Template updated" else ""
                _uploadState.value = UploadState.Success(
                    "✓ Uploaded $personCount persons, $paymentCount payments at $ts$templateNote"
                )
            } catch (e: Exception) {
                _uploadState.value = UploadState.Error("Upload failed: ${e.message}")
            }
        }
    }

    /** Verify the last upload by checking Firestore has the expected total person count */
    fun verifyUpload(file: LoanFile) {
        _uploadState.value = UploadState.Uploading
        viewModelScope.launch {
            try {
                val localCount = personRepository.getAllPersonsInFile(file.id).size
                val snapshot = db.collection("boss_data")
                    .document("files")
                    .collection("loan_files")
                    .document(file.id)
                    .collection("persons")
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