package com.moneymate.app.data.local.dao

import androidx.room.*
import com.moneymate.app.data.local.entity.EditPermissionScope
import com.moneymate.app.data.local.entity.Person
import kotlinx.coroutines.flow.Flow

@Dao
interface PersonDao {

    @Query("SELECT * FROM persons WHERE fileId = :fileId AND isDeleted = 0 AND isCompleted = 0 AND isPendingNewLoan = 0 ORDER BY sortOrder ASC, dateGiven ASC")
    fun getPersonsByFile(fileId: String): Flow<List<Person>>

    // Active persons INCLUDING the pending-new-loan placeholder (for totals that need it)
    @Query("SELECT * FROM persons WHERE fileId = :fileId AND isDeleted = 0 AND isCompleted = 0 ORDER BY sortOrder ASC, dateGiven ASC")
    fun getPersonsByFileIncludingPending(fileId: String): Flow<List<Person>>

    @Query("SELECT * FROM persons WHERE fileId = :fileId AND isDeleted = 0 ORDER BY dateGiven ASC")
    fun getPersonsByFileSortedByDate(fileId: String): Flow<List<Person>>

    // ── Full backup — every person in the file, no status filter ─────────────
    @Query("SELECT * FROM persons WHERE fileId = :fileId ORDER BY sortOrder ASC, dateGiven ASC")
    suspend fun getAllPersonsInFile(fileId: String): List<Person>

    @Query("SELECT * FROM persons WHERE fileId = :fileId AND isDeleted = 0 ORDER BY mode ASC")
    fun getPersonsByFileSortedByMode(fileId: String): Flow<List<Person>>

    @Query("SELECT * FROM persons WHERE id = :id")
    suspend fun getPersonById(id: String): Person?

    // ── Completed persons ─────────────────────────────────────────────────────
    @Query("SELECT * FROM persons WHERE fileId = :fileId AND isCompleted = 1 AND isDeleted = 0 ORDER BY completedAt DESC")
    fun getCompletedPersonsByFile(fileId: String): Flow<List<Person>>

    // ── Pending-new-loan placeholders ─────────────────────────────────────────
    @Query("SELECT * FROM persons WHERE fileId = :fileId AND isPendingNewLoan = 1 AND isDeleted = 0 ORDER BY sortOrder ASC")
    fun getPendingNewLoanPersonsByFile(fileId: String): Flow<List<Person>>

    @Query("SELECT * FROM persons WHERE fileId = :fileId AND LOWER(name) = LOWER(:name) AND isDeleted = 0")
    suspend fun findDuplicateByName(fileId: String, name: String): List<Person>

    @Query("SELECT * FROM persons WHERE fileId = :fileId AND LOWER(name) = LOWER(:name) AND LOWER(place) = LOWER(:place) AND isDeleted = 0")
    suspend fun findDuplicateByNameAndPlace(fileId: String, name: String, place: String): List<Person>

    @Query("UPDATE persons SET mobileNumber = :mobileNumber WHERE id = :id")
    suspend fun updateMobileNumber(id: String, mobileNumber: String?)

    @Query("UPDATE persons SET sortOrder = sortOrder + 1 WHERE fileId = :fileId AND sortOrder > :afterSortOrder AND isDeleted = 0")
    suspend fun shiftSortOrdersAfter(fileId: String, afterSortOrder: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerson(person: Person)

    @Update
    suspend fun updatePerson(person: Person)

    @Query("UPDATE persons SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: String, sortOrder: Int)

    @Query("UPDATE persons SET name = :name, place = :place WHERE id = :id")
    suspend fun updateNameAndPlace(id: String, name: String, place: String?)

    @Query("UPDATE persons SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeletePerson(id: String, deletedAt: Long)

    @Query("UPDATE persons SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restorePerson(id: String)

    @Query("DELETE FROM persons WHERE id = :id")
    suspend fun hardDeletePerson(id: String)

    @Query("SELECT name FROM persons WHERE fileId = :fileId AND isDeleted = 0")
    suspend fun findAllNamesInFile(fileId: String): List<String>

    @Query("SELECT * FROM persons WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedPersons(): Flow<List<Person>>

    @Query("DELETE FROM persons WHERE isDeleted = 1 AND deletedAt < :cutoff")
    suspend fun purgeExpiredPersons(cutoff: Long)

    // Auto-purge completed persons older than 30 days
    @Query("DELETE FROM persons WHERE isCompleted = 1 AND completedAt < :cutoff")
    suspend fun purgeExpiredCompletedPersons(cutoff: Long)

    @Query("UPDATE persons SET uploadedAt = :uploadedAt WHERE fileId = :fileId AND isDeleted = 0")
    suspend fun markAllUploadedInFile(fileId: String, uploadedAt: Long)

    @Query("UPDATE persons SET editPermissionGranted = :granted, editPermissionScope = :scope WHERE id = :id")
    suspend fun setEditPermission(id: String, granted: Boolean, scope: EditPermissionScope)

    // Mark person as completed and store timestamp + linked new record ID
    @Query("UPDATE persons SET isCompleted = 1, completedAt = :completedAt, linkedNewPersonId = :linkedNewPersonId WHERE id = :id")
    suspend fun markAsCompleted(id: String, completedAt: Long, linkedNewPersonId: String)

    // Update the pending-new-loan fields when the amount is filled in
    @Query("UPDATE persons SET isPendingNewLoan = 0, amountGiven = :amount, dateGiven = :dateGiven WHERE id = :id")
    suspend fun activatePendingNewLoan(id: String, amount: Double, dateGiven: Long)

    @Query("SELECT SUM(amountGiven) FROM persons WHERE fileId = :fileId AND isDeleted = 0")
    suspend fun getTotalGivenInFile(fileId: String): Double?

    @Query("SELECT SUM(amountGiven) FROM persons WHERE fileId = :fileId AND isDeleted = 0 AND mode = 'CASH'")
    suspend fun getTotalGivenCashInFile(fileId: String): Double?

    @Query("SELECT SUM(amountGiven) FROM persons WHERE fileId = :fileId AND isDeleted = 0 AND mode = 'UPI'")
    suspend fun getTotalGivenUpiInFile(fileId: String): Double?
}