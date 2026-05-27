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

    suspend fun shiftSortOrdersAfter(fileId: String, afterSortOrder: Int) =
        personDao.shiftSortOrdersAfter(fileId, afterSortOrder)

    suspend fun getPersonById(id: String): Person? =
        personDao.getPersonById(id)

    suspend fun insertPerson(person: Person) =
        personDao.insertPerson(person)

    suspend fun updatePerson(person: Person) =
        personDao.updatePerson(person)

    suspend fun updateNameAndPlace(id: String, name: String, place: String?) =
        personDao.updateNameAndPlace(id, name, place)

    suspend fun softDeletePerson(id: String, deletedAt: Long) =
        personDao.softDeletePerson(id, deletedAt)

    suspend fun restorePerson(id: String) =
        personDao.restorePerson(id)

    suspend fun hardDeletePerson(id: String) =
        personDao.hardDeletePerson(id)

    suspend fun purgeExpiredPersons(cutoff: Long) =
        personDao.purgeExpiredPersons(cutoff)

    suspend fun purgeExpiredCompletedPersons(cutoff: Long) =
        personDao.purgeExpiredCompletedPersons(cutoff)

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
     * Marks [personId] as completed and immediately creates a zero-amount placeholder
     * record (isPendingNewLoan = true) with all the same contact details.
     * Returns the ID of the newly created placeholder.
     */
    suspend fun markAsCompletedAndCreatePlaceholder(person: Person): String {
        val newId = UUID.randomUUID().toString()
        val now   = System.currentTimeMillis()

        // 1. Mark the original record completed
        personDao.markAsCompleted(person.id, now, newId)

        // 2. Insert the zero-placeholder at the same position
        val placeholder = person.copy(
            id               = newId,
            amountGiven      = 0.0,
            isPendingNewLoan = true,
            isCompleted      = false,
            completedAt      = null,
            linkedNewPersonId = null,
            previousPersonId = person.id,
            uploadedAt       = null,
            editPermissionGranted = false,
            editPermissionScope   = EditPermissionScope.NONE
        )
        personDao.insertPerson(placeholder)
        return newId
    }

    /** Converts a pending-new-loan placeholder into a real active record once the amount is set. */
    suspend fun activatePendingNewLoan(id: String, amount: Double) =
        personDao.activatePendingNewLoan(id, amount, System.currentTimeMillis())
}