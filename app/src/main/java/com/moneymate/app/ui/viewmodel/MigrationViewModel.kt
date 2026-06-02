package com.moneymate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.moneymate.app.utils.AppPreferences
import com.moneymate.app.utils.FirestorePathProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

sealed class MigrationState {
    object Idle : MigrationState()
    object Checking : MigrationState()
    object NotNeeded : MigrationState()        // old path is empty or migration already done
    object InProgress : MigrationState()
    data class Progress(val message: String) : MigrationState()
    object Success : MigrationState()
    data class Error(val message: String) : MigrationState()
}

@HiltViewModel
class MigrationViewModel @Inject constructor(
    private val prefs: AppPreferences,
    private val paths: FirestorePathProvider
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _migrationState = MutableStateFlow<MigrationState>(MigrationState.Idle)
    val migrationState: StateFlow<MigrationState> = _migrationState

    // ─── Public entry point ────────────────────────────────────────────────────

    /**
     * Call this once after a successful Google Sign-In.
     * If migration has already been done ([AppPreferences.isMigrationDone] == true),
     * this is a no-op and emits [MigrationState.NotNeeded] immediately.
     */
    fun runMigrationIfNeeded() {
        if (prefs.isMigrationDone) {
            _migrationState.value = MigrationState.NotNeeded
            return
        }
        viewModelScope.launch {
            _migrationState.value = MigrationState.Checking
            try {
                performMigration()
            } catch (e: Exception) {
                _migrationState.value = MigrationState.Error(
                    e.message ?: "Unknown error during migration"
                )
            }
        }
    }

    fun retryMigration() {
        runMigrationIfNeeded()
    }

    // ─── Core migration logic ──────────────────────────────────────────────────

    private suspend fun performMigration() {
        emit("Checking for existing data…")

        // 1. Get all loan files from the OLD path
        val legacyFilesRef = db.collection(paths.legacyLoanFilesCollection)
        val legacyFilesSnapshot = legacyFilesRef.get().await()

        if (legacyFilesSnapshot.isEmpty) {
            // Nothing to migrate — mark done and exit
            prefs.isMigrationDone = true
            _migrationState.value = MigrationState.NotNeeded
            return
        }

        _migrationState.value = MigrationState.InProgress
        emit("Found ${legacyFilesSnapshot.size()} file(s) to migrate…")

        // Collect everything into memory before writing a single byte to the new path.
        // This keeps the migration atomic: copy ALL → verify ALL → delete ALL.
        data class MigrationFile(
            val id: String,
            val data: Map<String, Any>,
            val persons: List<MigrationPerson>
        )
        data class MigrationPerson(
            val id: String,
            val data: Map<String, Any>,
            val payments: List<Pair<String, Map<String, Any>>>
        )

        val allFiles = mutableListOf<MigrationFile>()

        for (fileDoc in legacyFilesSnapshot.documents) {
            emit("Reading file: ${fileDoc.id}")
            val personsSnapshot = db.collection(paths.legacyPersonsCollection(fileDoc.id)).get().await()
            val persons = mutableListOf<MigrationPerson>()

            for (personDoc in personsSnapshot.documents) {
                val paymentsSnapshot = db.collection(
                    paths.legacyPaymentsCollection(fileDoc.id, personDoc.id)
                ).get().await()
                val payments = paymentsSnapshot.documents.map { it.id to (it.data ?: emptyMap()) }
                persons.add(MigrationPerson(personDoc.id, personDoc.data ?: emptyMap(), payments))
            }
            allFiles.add(MigrationFile(fileDoc.id, fileDoc.data ?: emptyMap(), persons))
        }

        // ── PHASE 2: Write to new path ─────────────────────────────────────────

        emit("Writing data to your account…")

        for (file in allFiles) {
            emit("Migrating file: ${file.id} (${file.persons.size} person(s))")

            db.collection(paths.loanFilesCollection)
                .document(file.id)
                .set(file.data)
                .await()

            for (person in file.persons) {
                db.collection(paths.personsCollection(file.id))
                    .document(person.id)
                    .set(person.data)
                    .await()

                for ((paymentId, paymentData) in person.payments) {
                    db.collection(paths.paymentsCollection(file.id, person.id))
                        .document(paymentId)
                        .set(paymentData)
                        .await()
                }
            }
        }

        // ── PHASE 3: Verify ────────────────────────────────────────────────────

        emit("Verifying migration…")

        val newFilesSnapshot = db.collection(paths.loanFilesCollection).get().await()
        if (newFilesSnapshot.size() < legacyFilesSnapshot.size()) {
            throw Exception(
                "Verification failed: expected ${legacyFilesSnapshot.size()} files " +
                        "but found ${newFilesSnapshot.size()} at new path. Old data is untouched."
            )
        }

        // ── PHASE 4: Delete old path ───────────────────────────────────────────

        emit("Cleaning up old data…")

        for (file in allFiles) {
            for (person in file.persons) {
                for ((paymentId, _) in person.payments) {
                    db.collection(paths.legacyPaymentsCollection(file.id, person.id))
                        .document(paymentId)
                        .delete()
                        .await()
                }
                db.collection(paths.legacyPersonsCollection(file.id))
                    .document(person.id)
                    .delete()
                    .await()
            }
            db.collection(paths.legacyLoanFilesCollection)
                .document(file.id)
                .delete()
                .await()
        }

        // ── PHASE 5: Mark done ─────────────────────────────────────────────────

        prefs.isMigrationDone = true
        emit("Migration complete!")
        _migrationState.value = MigrationState.Success
    }

    private fun emit(message: String) {
        _migrationState.value = MigrationState.Progress(message)
    }
}