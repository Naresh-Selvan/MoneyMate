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

    // ── DAO pass-throughs for ViewModel orchestration ───────────────────────────
    suspend fun markAsCompleted(id: String, completedAt: Long, linkedNewPersonId: String) =
        personDao.markAsCompleted(id, completedAt, linkedNewPersonId)

    suspend fun countZeroActiveCards(name: String, fileId: String): Int =
        personDao.countZeroActiveCards(name, fileId)

    suspend fun countPendingClones(name: String, fileId: String): Int =
        personDao.countPendingClones(name, fileId)

    suspend fun updateAmountAndDateResetInterest(id: String, amount: Double, dateGiven: Long) =
        personDao.updateAmountAndDateResetInterest(id, amount, dateGiven)

    suspend fun activateZeroActiveCardWithInterest(
        id: String,
        amount: Double,
        dateGiven: Long,
        interestRate: Double,
        interestType: String,
        interestAmount: Double,
        totalRepayment: Double,
        loanType: String,
        numberOfInstallments: Int,
        perInstallmentAmount: Double,
        isDurationBased: Boolean,
        durationDays: Int?
    ) = personDao.activateZeroActiveCardWithInterest(id, amount, dateGiven, interestRate, interestType, interestAmount, totalRepayment, loanType, numberOfInstallments, perInstallmentAmount, isDurationBased, durationDays)

    /** Converts a pending-new-loan placeholder into a real active record once the amount is set. */
    suspend fun activatePendingNewLoan(id: String, amount: Double) =
        personDao.activatePendingNewLoan(id, amount, System.currentTimeMillis())

    /**
     * Called when the active card for a person is soft-deleted.
     * Removes the orphaned pink indicator card for the same name + fileId.
     */
    suspend fun deleteZeroCloneByNameAndFile(name: String, fileId: String) =
        personDao.deleteZeroCloneByNameAndFile(name, fileId)

    // ── Insights ───────────────────────────────────────────────────────────────
    suspend fun getTotalGivenToday(fileId: String, startOfDay: Long, endOfDay: Long): Double =
        personDao.getTotalGivenToday(fileId, startOfDay, endOfDay) ?: 0.0

    suspend fun getTotalGivenInWeek(fileId: String, weekStart: Long, weekEnd: Long): Double =
        personDao.getTotalGivenInWeek(fileId, weekStart, weekEnd) ?: 0.0

    suspend fun getActiveLoanCount(fileId: String): Int =
        personDao.getActiveLoanCount(fileId)

    suspend fun getCompletedLoanCount(fileId: String): Int =
        personDao.getCompletedLoanCount(fileId)

    suspend fun getTotalOutstanding(fileId: String): Double =
        personDao.getTotalOutstanding(fileId)

    /** Remove any duplicate pending-new-loan pink cards, keeping only one per person. */
    suspend fun removeDuplicatePendingClones() =
        personDao.removeDuplicatePendingClones()

    // ── Phase 2 Collection Screen ─────────────────────────────────────────────
    fun getLendingPersonsByFile(fileId: String): Flow<List<Person>> =
        personDao.getLendingPersonsByFile(fileId)

    fun getBorrowingPersonsByFile(fileId: String): Flow<List<Person>> =
        personDao.getBorrowingPersonsByFile(fileId)

    suspend fun getNewLoansToday(fileId: String, startOfDay: Long, endOfDay: Long): Double =
        personDao.getNewLoansToday(fileId, startOfDay, endOfDay)

    // ════════════════════════════════════════════════════════════════════════
    // Phase 4 — Reports
    // ════════════════════════════════════════════════════════════════════════

    fun getAboutToCloseLoans(fileId: String): kotlinx.coroutines.flow.Flow<List<Person>> =
        personDao.getAboutToCloseLoans(fileId)

    fun getMissingCustomers(fileId: String, from: Long, to: Long): kotlinx.coroutines.flow.Flow<List<Person>> =
        personDao.getMissingCustomers(fileId, from, to)

    fun getMonthlyInterestPending(fileId: String, monthStart: Long, monthEnd: Long): kotlinx.coroutines.flow.Flow<List<Person>> =
        personDao.getMonthlyInterestPending(fileId, monthStart, monthEnd)

    fun getNonPerformingLoans(fileId: String, cutoffDate: Long): kotlinx.coroutines.flow.Flow<List<Person>> =
        personDao.getNonPerformingLoans(fileId, cutoffDate)

    fun getBadLoans(fileId: String, cutoffDate: Long): kotlinx.coroutines.flow.Flow<List<Person>> =
        personDao.getBadLoans(fileId, cutoffDate)

    fun getNewBadLoansByDate(fileId: String, from: Long, to: Long): kotlinx.coroutines.flow.Flow<List<Person>> =
        personDao.getNewBadLoansByDate(fileId, from, to)

    fun getNewCustomers(fileId: String, from: Long, to: Long): kotlinx.coroutines.flow.Flow<List<Person>> =
        personDao.getNewCustomers(fileId, from, to)

    fun getLoanNotTaken(fileId: String, from: Long, to: Long): kotlinx.coroutines.flow.Flow<List<Person>> =
        personDao.getLoanNotTaken(fileId, from, to)

    suspend fun getSiteActiveLoanCount(): Int = personDao.getSiteActiveLoanCount()

    suspend fun getSiteTotalOutstanding(): Double = personDao.getSiteTotalOutstanding()

    suspend fun getSiteTotalNewLoans(from: Long, to: Long): Double = personDao.getSiteTotalNewLoans(from, to)
}