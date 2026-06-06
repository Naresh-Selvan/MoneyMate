package com.moneymate.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.moneymate.app.data.local.dao.PersonDao
import com.moneymate.app.data.local.entity.EditPermissionScope
import com.moneymate.app.data.local.entity.Person
import com.moneymate.app.utils.FirestorePathProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonRepository @Inject constructor(
    private val personDao: PersonDao,
    private val paths: FirestorePathProvider
) {
    fun getPersonsByFile(fileId: String): Flow<List<Person>> =
        personDao.getPersonsByFile(fileId)

    fun getPersonsByFileSortedByDate(fileId: String): Flow<List<Person>> =
        personDao.getPersonsByFileSortedByDate(fileId)

    /** Returns every person in the file with no status filter — for full cloud backup. */
    suspend fun getAllPersonsInFile(fileId: String): List<Person> =
        personDao.getAllPersonsInFile(fileId)

    fun getPersonsByFileSortedByMode(fileId: String): Flow<List<Person>> =
        personDao.getPersonsByFileSortedByMode(fileId)

    fun getDeletedPersons(): Flow<List<Person>> =
        personDao.getDeletedPersons()

    fun getDeletedCompletedPersons(): Flow<List<Person>> =
        personDao.getDeletedCompletedPersons()

    fun getAllDeletedPersons(): Flow<List<Person>> =
        personDao.getAllDeletedPersons()

    fun getAllPersonsIncludingDeleted(): Flow<List<Person>> =
        personDao.getAllPersonsIncludingDeleted()

    fun getCompletedPersonsByFile(fileId: String): Flow<List<Person>> =
        personDao.getCompletedPersonsByFile(fileId)

    fun getPendingNewLoanPersonsByFile(fileId: String): Flow<List<Person>> =
        personDao.getPendingNewLoanPersonsByFile(fileId)

    suspend fun findDuplicateByName(fileId: String, name: String): List<Person> =
        personDao.findDuplicateByName(fileId, name)

    suspend fun findAllNamesInFile(fileId: String): List<String> =
        personDao.findAllNamesInFile(fileId)

    suspend fun findDuplicateByNameAndPlace(fileId: String, name: String, place: String): List<Person> =
        personDao.findDuplicateByNameAndPlace(fileId, name, place)

    /** Returns all loan records for a person by their ID — for loan history screen. */
    fun getLoanHistoryByPersonId(personId: String): Flow<List<Person>> =
        personDao.getLoanHistoryByPersonId(personId)

    suspend fun shiftSortOrdersAfter(fileId: String, afterSortOrder: Int) =
        personDao.shiftSortOrdersAfter(fileId, afterSortOrder)

    /** BUG 6 FIX: Closes the gap left by a person being moved by decrementing all
     * sortOrders strictly above [currentSortOrder]. */
    suspend fun shiftSortOrdersDown(fileId: String, currentSortOrder: Int) =
        personDao.shiftSortOrdersDown(fileId, currentSortOrder)

    suspend fun getPersonById(id: String): Person? =
        personDao.getPersonById(id)

    /** Reactive variant — emits updates whenever the person row changes. */
    fun getPersonByIdFlow(id: String): Flow<Person?> =
        personDao.getPersonByIdFlow(id)

    suspend fun insertPerson(person: Person) =
        personDao.insertPerson(person)

    suspend fun updatePerson(person: Person) =
        personDao.updatePerson(person)

    suspend fun updateNameAndPlace(id: String, name: String, place: String?) =
        personDao.updateNameAndPlace(id, name, place)

    suspend fun softDeletePerson(id: String, deletedAt: Long) {
        personDao.softDeletePerson(id, deletedAt)
        val person = personDao.getPersonById(id)
        if (person != null) {
            try {
                val db = FirebaseFirestore.getInstance()
                val docRef = db.collection(paths.personsCollection(person.fileId)).document(id)
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
                Log.e("PersonRepository", "Firestore softDeletePerson failed for $id", e)
            }
        }
    }

    suspend fun softDeleteCompletedPerson(id: String, deletedAt: Long) {
        personDao.softDeleteCompletedPerson(id, deletedAt)
        val person = personDao.getPersonById(id)
        if (person != null) {
            try {
                val db = FirebaseFirestore.getInstance()
                val docRef = db.collection(paths.personsCollection(person.fileId)).document(id)
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
                Log.e("PersonRepository", "Firestore softDeleteCompletedPerson failed for $id", e)
            }
        }
    }

    suspend fun restorePerson(id: String) {
        personDao.restorePerson(id)
        val person = personDao.getPersonById(id)
        if (person != null) {
            try {
                val db = FirebaseFirestore.getInstance()
                val docRef = db.collection(paths.personsCollection(person.fileId)).document(id)
                docRef.set(
                    mapOf(
                        "isDeleted" to false,
                        "deletedAt" to null
                    ),
                    SetOptions.merge()
                ).await()
            } catch (e: Exception) {
                Log.e("PersonRepository", "Firestore restorePerson failed for $id", e)
            }
        }
    }

    suspend fun hardDeletePerson(id: String) {
        val person = personDao.getPersonById(id)
        personDao.hardDeletePerson(id)
        if (person != null) {
            try {
                val db = FirebaseFirestore.getInstance()
                val docRef = db.collection(paths.personsCollection(person.fileId)).document(id)
                docRef.set(
                    mapOf(
                        "isDeleted" to true,
                        "permanentlyDeleted" to true,
                        "permanentlyDeletedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                ).await()
            } catch (e: Exception) {
                Log.e("PersonRepository", "Firestore hardDeletePerson failed for $id", e)
            }
        }
    }

    suspend fun purgeExpiredPersons(cutoff: Long) =
        personDao.purgeExpiredPersons(cutoff)

    suspend fun purgeExpiredCompletedPersons(cutoff: Long) =
        personDao.purgeExpiredCompletedPersons(cutoff)

    suspend fun purgeExpiredDeletedCompletedPersons(cutoff: Long) =
        personDao.purgeExpiredDeletedCompletedPersons(cutoff)

    suspend fun markAllUploadedInFile(fileId: String, uploadedAt: Long) =
        personDao.markAllUploadedInFile(fileId, uploadedAt)

    suspend fun setEditPermission(id: String, granted: Boolean, scope: EditPermissionScope) =
        personDao.setEditPermission(id, granted, scope)

    suspend fun getTotalGivenInFile(fileId: String): Double =
        personDao.getTotalGivenInFile(fileId) ?: 0.0

    suspend fun getTotalGivenCashInFile(fileId: String): Double =
        personDao.getTotalGivenCashInFile(fileId) ?: 0.0

    suspend fun getTotalGivenUpiInFile(fileId: String): Double =
        personDao.getTotalGivenUpiInFile(fileId) ?: 0.0

    suspend fun updateSortOrder(id: String, sortOrder: Int) =
        personDao.updateSortOrder(id, sortOrder)

    /**
     * Marks [person] as completed and produces two new rows — a white active card
     * (amountGiven = 0.0, isPendingNewLoan = false) that stays visible in the main
     * list, and a pink indicator card (isPendingNewLoan = true) — BUT only if those
     * rows don't already exist for this name + fileId.
     */
    suspend fun markAsCompletedAndCreatePlaceholder(person: Person): String {
        val now       = System.currentTimeMillis()
        val activeId  = UUID.randomUUID().toString()
        val pinkId    = UUID.randomUUID().toString()

        // 1. Mark the original record as completed — it moves to the Completed section.
        personDao.markAsCompleted(person.id, now, activeId)

        // 2. Insert the white active card only if one doesn't already exist.
        if (personDao.countZeroActiveCards(person.name, person.fileId) == 0) {
            val whiteCard = person.copy(
                id                    = activeId,
                amountGiven           = 0.0,
                dateGiven             = now,
                isPendingNewLoan      = false,
                isCompleted           = false,
                completedAt           = null,
                linkedNewPersonId     = null,
                previousPersonId      = person.id,
                uploadedAt            = null,
                editPermissionGranted = false,
                editPermissionScope   = EditPermissionScope.NONE,
                interestRate          = 0.0,
                interestType          = "PERCENTAGE",
                interestAmount        = 0.0,
                totalRepayment        = 0.0,
                loanType              = "MONTHLY",
                numberOfInstallments  = 10,
                perInstallmentAmount  = 0.0,
                isDurationBased       = false,
                durationDays          = null
            )
            personDao.insertPerson(whiteCard)
        }

        // 3. Insert the pink indicator card only if one doesn't already exist.
        if (personDao.countPendingClones(person.name, person.fileId) == 0) {
            val pinkCard = person.copy(
                id                    = pinkId,
                amountGiven           = 0.0,
                dateGiven             = now,
                isPendingNewLoan      = true,
                isCompleted           = false,
                completedAt           = null,
                linkedNewPersonId     = null,
                previousPersonId      = person.id,
                uploadedAt            = null,
                editPermissionGranted = false,
                editPermissionScope   = EditPermissionScope.NONE,
                interestRate          = 0.0,
                interestType          = "PERCENTAGE",
                interestAmount        = 0.0,
                totalRepayment        = 0.0,
                loanType              = "MONTHLY",
                numberOfInstallments  = 10,
                perInstallmentAmount  = 0.0,
                isDurationBased       = false,
                durationDays          = null
            )
            personDao.insertPerson(pinkCard)
        }

        return activeId
    }

    /** Converts a pending-new-loan placeholder into a real active record once the amount is set. */
    suspend fun activatePendingNewLoan(id: String, amount: Double) =
        personDao.activatePendingNewLoan(id, amount, System.currentTimeMillis())

    /**
     * Called when boss taps the white ₹0.0 active card and enters the new loan amount.
     * Updates amount + dateGiven to TODAY on the white card, then deletes the pink clone.
     */
    suspend fun activateZeroActiveCard(person: Person, amount: Double) {
        personDao.updateAmountAndDateResetInterest(person.id, amount, System.currentTimeMillis())
        personDao.deleteZeroCloneByNameAndFile(person.name, person.fileId)
    }

    /**
     * Called when the active card for a person is soft-deleted.
     * Removes the orphaned pink indicator card for the same name + fileId.
     */
    suspend fun deleteZeroCloneByNameAndFile(name: String, fileId: String) =
        personDao.deleteZeroCloneByNameAndFile(name, fileId)

    // ── Insights ───────────────────────────────────────────────────────────────
    suspend fun getTotalGivenToday(fileId: String, startOfDay: Long, endOfDay: Long): Double =
        personDao.getTotalGivenToday(fileId, startOfDay, endOfDay) ?: 0.0

    suspend fun getActiveLoanCount(fileId: String): Int =
        personDao.getActiveLoanCount(fileId)

    suspend fun getCompletedLoanCount(fileId: String): Int =
        personDao.getCompletedLoanCount(fileId)

    suspend fun getTotalOutstanding(fileId: String): Double =
        personDao.getTotalOutstanding(fileId)

    /** Remove any duplicate pending-new-loan pink cards, keeping only one per person. */
    suspend fun removeDuplicatePendingClones() =
        personDao.removeDuplicatePendingClones()
}