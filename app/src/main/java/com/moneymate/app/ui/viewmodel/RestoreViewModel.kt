package com.moneymate.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.moneymate.app.data.local.entity.EditPermissionScope
import com.moneymate.app.data.local.entity.LoanFile
import com.moneymate.app.data.local.entity.LoanType
import com.moneymate.app.data.local.entity.PaymentMode
import com.moneymate.app.data.local.entity.Person
import com.moneymate.app.data.local.entity.Payment
import com.moneymate.app.data.repository.LoanFileRepository
import com.moneymate.app.data.repository.PersonRepository
import com.moneymate.app.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val TAG = "RestoreViewModel"

sealed class RestoreState {
    object Idle : RestoreState()
    object Checking : RestoreState()
    data class Preview(
        val fileCount: Int,
        val personCount: Int,
        val paymentCount: Int   // now includes payment count so user can verify
    ) : RestoreState()
    object Restoring : RestoreState()
    data class Success(val message: String) : RestoreState()
    data class Error(val message: String) : RestoreState()
}

@HiltViewModel
class RestoreViewModel @Inject constructor(
    private val loanFileRepository: LoanFileRepository,
    private val personRepository: PersonRepository,
    private val paymentRepository: PaymentRepository
) : ViewModel() {

    private val _restoreState = MutableStateFlow<RestoreState>(RestoreState.Idle)
    val restoreState: StateFlow<RestoreState> = _restoreState

    private val db = FirebaseFirestore.getInstance()

    /**
     * Step 1: Walk all of Firestore and count files, persons, and payments
     * so the user can confirm everything is actually there before restoring.
     */
    fun checkFirestore() {
        _restoreState.value = RestoreState.Checking
        viewModelScope.launch {
            try {
                val filesSnapshot = db.collection("boss_data")
                    .document("files")
                    .collection("loan_files")
                    .get()
                    .await()

                if (filesSnapshot.isEmpty) {
                    _restoreState.value = RestoreState.Error("No data found in Firestore.")
                    return@launch
                }

                var totalPersons = 0
                var totalPayments = 0

                for (fileDoc in filesSnapshot.documents) {
                    val personsSnapshot = db.collection("boss_data")
                        .document("files")
                        .collection("loan_files")
                        .document(fileDoc.id)
                        .collection("persons")
                        .get()
                        .await()

                    totalPersons += personsSnapshot.size()

                    for (personDoc in personsSnapshot.documents) {
                        val paymentsSnapshot = db.collection("boss_data")
                            .document("files")
                            .collection("loan_files")
                            .document(fileDoc.id)
                            .collection("persons")
                            .document(personDoc.id)
                            .collection("payments")
                            .get()
                            .await()
                        totalPayments += paymentsSnapshot.size()
                    }
                }

                _restoreState.value = RestoreState.Preview(
                    fileCount = filesSnapshot.size(),
                    personCount = totalPersons,
                    paymentCount = totalPayments
                )
            } catch (e: Exception) {
                Log.e(TAG, "checkFirestore failed", e)
                _restoreState.value = RestoreState.Error("Check failed: ${e.message}")
            }
        }
    }

    /**
     * Step 2: Restore every record from Firestore into the local Room database.
     *
     * - Every field of every entity is restored exactly as uploaded.
     * - Includes active, completed, deleted, and pending-new-loan persons.
     * - Includes all payments for every person, including soft-deleted ones.
     * - Per-record errors are logged and skipped rather than aborting the whole restore,
     *   so one bad document can't block everything else.
     */
    fun restoreFromFirestore() {
        _restoreState.value = RestoreState.Restoring
        viewModelScope.launch {
            try {
                val filesSnapshot = db.collection("boss_data")
                    .document("files")
                    .collection("loan_files")
                    .get()
                    .await()

                var restoredFiles = 0
                var restoredPersons = 0
                var restoredPayments = 0
                var skippedRecords = 0

                for (fileDoc in filesSnapshot.documents) {
                    val d = fileDoc.data
                    if (d == null) { skippedRecords++; continue }

                    // ── LoanFile ──────────────────────────────────────────────
                    try {
                        val file = LoanFile(
                            id               = d.str("id", fileDoc.id),
                            name             = d.str("name"),
                            createdAt        = d.long("createdAt"),
                            sortOrder        = d.int("sortOrder"),
                            isDeleted        = d.bool("isDeleted"),
                            deletedAt        = d.longOrNull("deletedAt"),
                            syncedToFirebase = d.bool("syncedToFirebase"),
                            lastUploadedAt   = d.longOrNull("lastUploadedAt")
                        )
                        loanFileRepository.insertFile(file)
                        restoredFiles++

                        // ── Persons — ALL: active, completed, deleted, pending ─
                        val personsSnapshot = db.collection("boss_data")
                            .document("files")
                            .collection("loan_files")
                            .document(fileDoc.id)
                            .collection("persons")
                            .get()
                            .await()

                        for (personDoc in personsSnapshot.documents) {
                            val pd = personDoc.data
                            if (pd == null) { skippedRecords++; continue }

                            try {
                                val person = Person(
                                    id                    = pd.str("id", personDoc.id),
                                    fileId                = pd.str("fileId", file.id),
                                    name                  = pd.str("name"),
                                    place                 = pd.strOrNull("place"),
                                    mobileNumber          = pd.strOrNull("mobileNumber"),
                                    amountGiven           = pd.double("amountGiven"),
                                    mode                  = enumOf(pd.str("mode"), PaymentMode.CASH),
                                    dateGiven             = pd.long("dateGiven"),
                                    sortOrder             = pd.int("sortOrder"),
                                    recordType            = enumOf(pd.str("recordType"), LoanType.LENDING),
                                    isDeleted             = pd.bool("isDeleted"),
                                    deletedAt             = pd.longOrNull("deletedAt"),
                                    uploadedAt            = pd.longOrNull("uploadedAt"),
                                    editPermissionGranted = pd.bool("editPermissionGranted"),
                                    editPermissionScope   = enumOf(pd.str("editPermissionScope"), EditPermissionScope.NONE),
                                    isCompleted           = pd.bool("isCompleted"),
                                    completedAt           = pd.longOrNull("completedAt"),
                                    linkedNewPersonId     = pd.strOrNull("linkedNewPersonId"),
                                    isPendingNewLoan      = pd.bool("isPendingNewLoan"),
                                    previousPersonId      = pd.strOrNull("previousPersonId")
                                )
                                personRepository.insertPerson(person)
                                restoredPersons++

                                // ── Payments — ALL: active + deleted ──────────
                                val paymentsSnapshot = db.collection("boss_data")
                                    .document("files")
                                    .collection("loan_files")
                                    .document(fileDoc.id)
                                    .collection("persons")
                                    .document(personDoc.id)
                                    .collection("payments")
                                    .get()
                                    .await()

                                for (payDoc in paymentsSnapshot.documents) {
                                    val pay = payDoc.data
                                    if (pay == null) { skippedRecords++; continue }

                                    try {
                                        val payment = Payment(
                                            id                    = pay.str("id", payDoc.id),
                                            personId              = pay.str("personId", person.id),
                                            amount                = pay.double("amount"),
                                            mode                  = enumOf(pay.str("mode"), PaymentMode.CASH),
                                            date                  = pay.long("date"),
                                            isDeleted             = pay.bool("isDeleted"),
                                            deletedAt             = pay.longOrNull("deletedAt"),
                                            isRollover            = pay.bool("isRollover"),
                                            uploadedAt            = pay.longOrNull("uploadedAt"),
                                            editPermissionGranted = pay.bool("editPermissionGranted"),
                                            editPermissionScope   = enumOf(pay.str("editPermissionScope"), EditPermissionScope.NONE)
                                        )
                                        paymentRepository.insertPayment(payment)
                                        restoredPayments++
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Failed to restore payment ${payDoc.id}", e)
                                        skippedRecords++
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Failed to restore person ${personDoc.id}", e)
                                skippedRecords++
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to restore file ${fileDoc.id}", e)
                        skippedRecords++
                    }
                }

                val skipNote = if (skippedRecords > 0) " ($skippedRecords records skipped — check logs)" else ""
                _restoreState.value = RestoreState.Success(
                    "Restored $restoredFiles files · $restoredPersons persons · $restoredPayments payments$skipNote"
                )
            } catch (e: Exception) {
                Log.e(TAG, "restoreFromFirestore failed", e)
                _restoreState.value = RestoreState.Error("Restore failed: ${e.message}")
            }
        }
    }

    fun reset() {
        _restoreState.value = RestoreState.Idle
    }

    // ── Safe Firestore map accessors ──────────────────────────────────────────

    private fun Map<String, Any>.str(key: String, fallback: String = ""): String =
        (this[key] as? String)?.ifEmpty { null } ?: fallback

    private fun Map<String, Any>.strOrNull(key: String): String? =
        (this[key] as? String)?.ifEmpty { null }

    private fun Map<String, Any>.long(key: String, fallback: Long = 0L): Long =
        when (val v = this[key]) {
            is Long   -> v
            is Number -> v.toLong()
            else      -> fallback
        }

    private fun Map<String, Any>.longOrNull(key: String): Long? =
        when (val v = this[key]) {
            is Long   -> v
            is Number -> v.toLong()
            else      -> null
        }

    private fun Map<String, Any>.int(key: String, fallback: Int = 0): Int =
        when (val v = this[key]) {
            is Long   -> v.toInt()
            is Number -> v.toInt()
            else      -> fallback
        }

    private fun Map<String, Any>.double(key: String, fallback: Double = 0.0): Double =
        when (val v = this[key]) {
            is Double -> v
            is Number -> v.toDouble()
            else      -> fallback
        }

    private fun Map<String, Any>.bool(key: String, fallback: Boolean = false): Boolean =
        this[key] as? Boolean ?: fallback

    private inline fun <reified T : Enum<T>> enumOf(value: String, fallback: T): T =
        runCatching { enumValueOf<T>(value) }.getOrDefault(fallback)
}