package com.moneymate.app.data.repository

import com.moneymate.app.data.local.dao.PersonDao
import com.moneymate.app.data.local.entity.EditPermissionScope
import com.moneymate.app.data.local.entity.Person
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonRepository @Inject constructor(
    private val personDao: PersonDao
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

    /** Returns all loan records for a person by name in a file — for loan history screen. */
    fun getLoanHistoryByName(fileId: String, name: String): kotlinx.coroutines.flow.Flow<List<Person>> =
        personDao.getLoanHistoryByName(fileId, name)

    suspend fun shiftSortOrdersAfter(fileId: String, afterSortOrder: Int) =
        personDao.shiftSortOrdersAfter(fileId, afterSortOrder)

    /** BUG 6 FIX: Closes the gap left by a person being moved by decrementing all
     * sortOrders strictly above [currentSortOrder]. */
    suspend fun shiftSortOrdersDown(fileId: String, currentSortOrder: Int) =
        personDao.shiftSortOrdersDown(fileId, currentSortOrder)

    suspend fun getPersonById(id: String): Person? =
        personDao.getPersonById(id)

    /** Reactive variant — emits updates whenever the person row changes. */
    fun getPersonByIdFlow(id: String): kotlinx.coroutines.flow.Flow<Person?> =
        personDao.getPersonByIdFlow(id)

    suspend fun insertPerson(person: Person) =
        personDao.insertPerson(person)

    suspend fun updatePerson(person: Person) =
        personDao.updatePerson(person)

    suspend fun updateNameAndPlace(id: String, name: String, place: String?) =
        personDao.updateNameAndPlace(id, name, place)

    suspend fun softDeletePerson(id: String, deletedAt: Long) =
        personDao.softDeletePerson(id, deletedAt)

    suspend fun softDeleteCompletedPerson(id: String, deletedAt: Long) =
        personDao.softDeleteCompletedPerson(id, deletedAt)

    suspend fun restorePerson(id: String) =
        personDao.restorePerson(id)

    suspend fun hardDeletePerson(id: String) =
        personDao.hardDeletePerson(id)

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
     *
     * Three-step atomic sequence:
     *   1. Set isCompleted = 1 on the original row   → moves to Completed section
     *   2. Insert white active card (₹0.0, normal)   → stays in main active list
     *   3. Insert pink indicator card (pending clone) → shown below as visual indicator
     *
     * Guards prevent duplicates on repeated completion cycles.
     *
     * Returns the ID of the white active card (new loan placeholder).
     */
    suspend fun markAsCompletedAndCreatePlaceholder(person: Person): String {
        val now       = System.currentTimeMillis()
        val activeId  = UUID.randomUUID().toString()
        val pinkId    = UUID.randomUUID().toString()

        // 1. Mark the original record as completed — it moves to the Completed section.
        personDao.markAsCompleted(person.id, now, activeId)

        // 2. Insert the white active card only if one doesn't already exist.
        //    isPendingNewLoan = false → included in getPersonsByFile → visible as white card.
        //    amountGiven = 0.0 + dateGiven = now → tapping it opens the loan-amount dialog.
        if (personDao.countZeroActiveCards(person.name, person.fileId) == 0) {
            val whiteCard = person.copy(
                id                    = activeId,
                amountGiven           = 0.0,
                dateGiven             = now,          // completion date; updated to today when amount is entered
                isPendingNewLoan      = false,        // MUST be false so it appears in the normal active list
                isCompleted           = false,
                completedAt           = null,
                linkedNewPersonId     = null,
                previousPersonId      = person.id,
                uploadedAt            = null,
                editPermissionGranted = false,
                editPermissionScope   = EditPermissionScope.NONE
            )
            personDao.insertPerson(whiteCard)
        }

        // 3. Insert the pink indicator card only if one doesn't already exist.
        //    isPendingNewLoan = true → excluded from getPersonsByFile, included in
        //    getPendingNewLoanPersonsByFile → shown as the pink "Pending New Loan" card.
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
                editPermissionScope   = EditPermissionScope.NONE
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
        personDao.updateAmountAndDate(person.id, amount, System.currentTimeMillis())
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