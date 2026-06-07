package com.moneymate.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.moneymate.app.data.local.dao.FileDao
import com.moneymate.app.data.local.dao.PersonDao
import com.moneymate.app.data.local.dao.PaymentDao
import com.moneymate.app.data.local.entity.LoanFile
import com.moneymate.app.utils.FirestorePathProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanFileRepository @Inject constructor(
    private val fileDao: FileDao,
    private val personDao: PersonDao,
    private val paymentDao: PaymentDao,
    private val paths: FirestorePathProvider
) {
    fun getAllFiles(): Flow<List<LoanFile>> = fileDao.getAllFiles()

    suspend fun getAllFilesOnce(): List<LoanFile> = fileDao.getAllFilesOnce()

    fun getTrashedFiles(): Flow<List<LoanFile>> = fileDao.getTrashedFiles()

    fun getAllFilesIncludingDeleted(): Flow<List<LoanFile>> = fileDao.getAllFilesIncludingDeleted()

    suspend fun insertFile(file: LoanFile) = fileDao.insertFile(file)

    suspend fun updateFile(file: LoanFile) = fileDao.updateFile(file)

    suspend fun softDeleteFile(id: String, deletedAt: Long) {
        fileDao.softDeleteFile(id, deletedAt)
        try {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection(paths.loanFilesCollection).document(id)
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
            Log.e("LoanFileRepository", "Firestore softDeleteFile failed for $id", e)
        }
    }

    suspend fun restoreFile(id: String) {
        fileDao.restoreFile(id)
        try {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection(paths.loanFilesCollection).document(id)
            docRef.set(
                mapOf(
                    "isDeleted" to false,
                    "deletedAt" to null
                ),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            Log.e("LoanFileRepository", "Firestore restoreFile failed for $id", e)
        }
    }

    suspend fun hardDeleteFile(id: String) {
        fileDao.hardDeleteFile(id)
        try {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection(paths.loanFilesCollection).document(id)
            docRef.set(
                mapOf(
                    "isDeleted" to true,
                    "permanentlyDeleted" to true,
                    "permanentlyDeletedAt" to System.currentTimeMillis()
                ),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            Log.e("LoanFileRepository", "Firestore hardDeleteFile failed for $id", e)
        }
    }

    suspend fun purgeExpiredFiles(cutoff: Long) = fileDao.purgeExpiredFiles(cutoff)

    suspend fun markSynced(id: String, synced: Boolean, uploadedAt: Long) =
        fileDao.markSynced(id, synced, uploadedAt)

    suspend fun updateSortOrder(id: String, sortOrder: Int) =
        fileDao.updateSortOrder(id, sortOrder)

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
                    Log.e("LoanFileRepository", "Failed to update Firestore for purged payment ${payment.id}", e)
                }
                paymentDao.hardDeletePayment(payment.id)
            }
        } catch (e: Exception) {
            Log.e("LoanFileRepository", "Purge payments failed", e)
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
                    Log.e("LoanFileRepository", "Failed to update Firestore for purged person ${person.id}", e)
                }
                personDao.hardDeletePerson(person.id)
            }
        } catch (e: Exception) {
            Log.e("LoanFileRepository", "Purge persons failed", e)
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
                    Log.e("LoanFileRepository", "Failed to update Firestore for purged file ${file.id}", e)
                }
                fileDao.hardDeleteFile(file.id)
            }
        } catch (e: Exception) {
            Log.e("LoanFileRepository", "Purge files failed", e)
        }
    }
}