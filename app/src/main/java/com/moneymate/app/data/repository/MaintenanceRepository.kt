package com.moneymate.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.moneymate.app.data.local.dao.FileDao
import com.moneymate.app.data.local.dao.PersonDao
import com.moneymate.app.data.local.dao.PaymentDao
import com.moneymate.app.utils.FirestorePathProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for cross-cutting maintenance operations (e.g. purging expired
 * soft-deleted records across all entity types).
 *
 * Owns all three DAOs so [autoPurge] can orchestrate the full cleanup cycle
 * without coupling business repositories to DAOs they don't otherwise need.
 */
@Singleton
class MaintenanceRepository @Inject constructor(
    private val fileDao: FileDao,
    private val personDao: PersonDao,
    private val paymentDao: PaymentDao,
    private val paths: FirestorePathProvider
) {
    suspend fun autoPurge() {
        val db = FirebaseFirestore.getInstance()
        val now = System.currentTimeMillis()

        // 1. Purge Payments (retention: 180 days)
        val paymentCutoff = now - (180L * 24 * 60 * 60 * 1000)
        try {
            val expiredPayments = paymentDao.getExpiredDeletedPayments(paymentCutoff)
            for (payment in expiredPayments) {
                try {
                    val person = personDao.getPersonById(payment.personId)
                    if (person != null) {
                        val path = paths.paymentsCollection(person.fileId, payment.personId)
                        val updateMap = mapOf(
                            "isDeleted" to true,
                            "purged" to true,
                            "purgedAt" to now
                        )
                        db.collection(path).document(payment.id)
                            .set(updateMap, SetOptions.merge())
                            .await()
                    }
                } catch (e: Exception) {
                    Log.e("MaintenanceRepository", "Failed to update Firestore for purged payment ${payment.id}", e)
                }
                paymentDao.hardDeletePayment(payment.id)
            }
        } catch (e: Exception) {
            Log.e("MaintenanceRepository", "Purge payments failed", e)
        }

        // 2. Purge Persons (retention: 180 days)
        val personCutoff = now - (180L * 24 * 60 * 60 * 1000)
        try {
            val expiredPersons = personDao.getExpiredDeletedPersons(personCutoff)
            for (person in expiredPersons) {
                try {
                    val path = paths.personsCollection(person.fileId)
                    val updateMap = mapOf(
                        "isDeleted" to true,
                        "purged" to true,
                        "purgedAt" to now
                    )
                    db.collection(path).document(person.id)
                        .set(updateMap, SetOptions.merge())
                        .await()
                } catch (e: Exception) {
                    Log.e("MaintenanceRepository", "Failed to update Firestore for purged person ${person.id}", e)
                }
                personDao.hardDeletePerson(person.id)
            }
        } catch (e: Exception) {
            Log.e("MaintenanceRepository", "Purge persons failed", e)
        }

        // 3. Purge Files (retention: 180 days)
        val fileCutoff = now - (180L * 24 * 60 * 60 * 1000)
        try {
            val expiredFiles = fileDao.getExpiredDeletedFiles(fileCutoff)
            for (file in expiredFiles) {
                try {
                    val path = paths.loanFilesCollection
                    val updateMap = mapOf(
                        "isDeleted" to true,
                        "purged" to true,
                        "purgedAt" to now
                    )
                    db.collection(path).document(file.id)
                        .set(updateMap, SetOptions.merge())
                        .await()
                } catch (e: Exception) {
                    Log.e("MaintenanceRepository", "Failed to update Firestore for purged file ${file.id}", e)
                }
                fileDao.hardDeleteFile(file.id)
            }
        } catch (e: Exception) {
            Log.e("MaintenanceRepository", "Purge files failed", e)
        }
    }
}
