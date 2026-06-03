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

    // ── Pending-new-loan placeholders (pink indicator cards) ──────────────────
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

    // Soft-delete a completed person (moves it to Recently Deleted in TrashScreen)
    @Query("UPDATE persons SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id AND isCompleted = 1")
    suspend fun softDeleteCompletedPerson(id: String, deletedAt: Long)

    // Returns all soft-deleted completed persons (for TrashScreen "Deleted Completed Persons" section)
    @Query("SELECT * FROM persons WHERE isCompleted = 1 AND isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedCompletedPersons(): Flow<List<Person>>

    // Auto-purge completed persons older than 180 days
    @Query("DELETE FROM persons WHERE isCompleted = 1 AND isDeleted = 0 AND completedAt < :cutoff")
    suspend fun purgeExpiredCompletedPersons(cutoff: Long)

    // Auto-purge soft-deleted completed persons older than 180 days
    @Query("DELETE FROM persons WHERE isCompleted = 1 AND isDeleted = 1 AND deletedAt < :cutoff")
    suspend fun purgeExpiredDeletedCompletedPersons(cutoff: Long)

    @Query("UPDATE persons SET uploadedAt = :uploadedAt WHERE fileId = :fileId AND isDeleted = 0")
    suspend fun markAllUploadedInFile(fileId: String, uploadedAt: Long)

    @Query("UPDATE persons SET editPermissionGranted = :granted, editPermissionScope = :scope WHERE id = :id")
    suspend fun setEditPermission(id: String, granted: Boolean, scope: EditPermissionScope)

    // Mark person as completed — sets isCompleted + completedAt + stores the linked active-card ID.
    @Query("UPDATE persons SET isCompleted = 1, completedAt = :completedAt, linkedNewPersonId = :linkedNewPersonId WHERE id = :id")
    suspend fun markAsCompleted(id: String, completedAt: Long, linkedNewPersonId: String)

    // ── Fix 2: Update amount AND date together when boss enters a new loan amount
    // on the white active card. dateGiven = today so the new loan cycle starts correctly.
    @Query("UPDATE persons SET amountGiven = :amount, dateGiven = :dateGiven WHERE id = :id")
    suspend fun updateAmountAndDate(id: String, amount: Double, dateGiven: Long)

    // Update the pending-new-loan fields when the amount is filled in (legacy path, kept for safety)
    @Query("UPDATE persons SET isPendingNewLoan = 0, amountGiven = :amount, dateGiven = :dateGiven WHERE id = :id")
    suspend fun activatePendingNewLoan(id: String, amount: Double, dateGiven: Long)

    @Query("SELECT SUM(amountGiven) FROM persons WHERE fileId = :fileId AND isDeleted = 0")
    suspend fun getTotalGivenInFile(fileId: String): Double?

    @Query("SELECT SUM(amountGiven) FROM persons WHERE fileId = :fileId AND isDeleted = 0 AND mode = 'CASH'")
    suspend fun getTotalGivenCashInFile(fileId: String): Double?

    @Query("SELECT SUM(amountGiven) FROM persons WHERE fileId = :fileId AND isDeleted = 0 AND mode = 'UPI'")
    suspend fun getTotalGivenUpiInFile(fileId: String): Double?

    // Delete the pending-new-loan pink card for a given name + fileId.
    // Called when: (a) the white active card gets its amount set, or
    //              (b) the parent active card is soft-deleted.
    @Query("DELETE FROM persons WHERE name = :name AND fileId = :fileId AND amountGiven = 0.0 AND isCompleted = 0 AND isPendingNewLoan = 1")
    suspend fun deleteZeroCloneByNameAndFile(name: String, fileId: String)

    // Guard — count existing pending-new-loan pink cards for this name in this file.
    // Used before inserting a new pink card so only one ever exists at a time.
    @Query("""
        SELECT COUNT(*) FROM persons
        WHERE name = :name
          AND fileId = :fileId
          AND amountGiven = 0.0
          AND isCompleted = 0
          AND isPendingNewLoan = 1
          AND isDeleted = 0
    """)
    suspend fun countPendingClones(name: String, fileId: String): Int

    // Guard — count existing zero-amount active cards (isPendingNewLoan = 0) for
    // this name in this file. Used to avoid inserting a second white card.
    @Query("""
        SELECT COUNT(*) FROM persons
        WHERE name = :name
          AND fileId = :fileId
          AND amountGiven = 0.0
          AND isCompleted = 0
          AND isPendingNewLoan = 0
          AND isDeleted = 0
    """)
    suspend fun countZeroActiveCards(name: String, fileId: String): Int

    // One-time cleanup — keeps only the most-recently-inserted pending clone
    // per (name, fileId) pair and deletes all older duplicates.
    @Query("""
        DELETE FROM persons
        WHERE isPendingNewLoan = 1
          AND isCompleted = 0
          AND isDeleted = 0
          AND id NOT IN (
              SELECT MAX(id)
              FROM persons
              WHERE isPendingNewLoan = 1
                AND isCompleted = 0
                AND isDeleted = 0
              GROUP BY name, fileId
          )
    """)
    suspend fun removeDuplicatePendingClones()
}