package com.moneymate.app.utils

import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for every Firestore collection path in MoneyMate.
 *
 * OLD (pre-migration) layout:
 *   boss_data/files/loan_files/{fileId}
 *   boss_data/files/loan_files/{fileId}/persons/{personId}
 *   boss_data/files/loan_files/{fileId}/persons/{personId}/payments/{paymentId}
 *
 * NEW (post-migration) layout:
 *   users/{uid}/boss_data/files/loan_files/{fileId}
 *   users/{uid}/boss_data/files/loan_files/{fileId}/persons/{personId}
 *   users/{uid}/boss_data/files/loan_files/{fileId}/persons/{personId}/payments/{paymentId}
 *
 * All ViewModels that touch Firestore must obtain paths exclusively through this
 * class so that the UID is always injected correctly.
 */
@Singleton
class FirestorePathProvider @Inject constructor(
    private val prefs: AppPreferences
) {
    // ─── UID resolution ────────────────────────────────────────────────────────

    /**
     * Returns the current Firebase UID.
     * Prefers the live FirebaseAuth value; falls back to the cached pref value.
     * Throws [IllegalStateException] if neither is available (should never happen
     * after a successful sign-in).
     */
    val uid: String
        get() = FirebaseAuth.getInstance().currentUser?.uid
            ?: prefs.firebaseUid.takeIf { it.isNotEmpty() }
            ?: error("FirestorePathProvider: no Firebase UID available. Has Google Sign-In completed?")

    // ─── Root paths ────────────────────────────────────────────────────────────

    /** Root document path for this user: "users/{uid}" */
    val userRoot: String get() = "users/$uid"

    /** Root collection for loan files: "users/{uid}/boss_data/files/loan_files" */
    val loanFilesCollection: String get() = "$userRoot/boss_data/files/loan_files"

    /** Persons sub-collection for a given file. */
    fun personsCollection(fileId: String): String =
        "$loanFilesCollection/$fileId/persons"

    /** Payments sub-collection for a given person inside a given file. */
    fun paymentsCollection(fileId: String, personId: String): String =
        "${personsCollection(fileId)}/$personId/payments"

    // ─── Old (pre-migration) paths ─────────────────────────────────────────────

    /** Legacy root collection path — used ONLY during migration. */
    val legacyLoanFilesCollection: String get() = "boss_data/files/loan_files"

    fun legacyPersonsCollection(fileId: String): String =
        "${legacyLoanFilesCollection}/$fileId/persons"

    fun legacyPaymentsCollection(fileId: String, personId: String): String =
        "${legacyPersonsCollection(fileId)}/$personId/payments"
}