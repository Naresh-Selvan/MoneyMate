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

    /** Reactive variant — emits a new value whenever the row changes (BUG 5 fix). */
    @Query("SELECT * FROM persons WHERE id = :id")
    fun getPersonByIdFlow(id: String): kotlinx.coroutines.flow.Flow<Person?>

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

    /** BUG 6 FIX: Decrements sortOrder for all rows strictly above [currentSortOrder],
     * closing the gap when a person is removed from its current list position. */
    @Query("UPDATE persons SET sortOrder = sortOrder - 1 WHERE fileId = :fileId AND sortOrder > :currentSortOrder AND isDeleted = 0")
    suspend fun shiftSortOrdersDown(fileId: String, currentSortOrder: Int)

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

    // Fix: Exclude completed persons — they belong in getDeletedCompletedPersons().
    // Without this filter a deleted completed person appears in BOTH deletedPersons
    // AND deletedCompletedPersons, causing the same person to show up on two screens.
    @Query("SELECT * FROM persons WHERE isDeleted = 1 AND isCompleted = 0 AND isPendingNewLoan = 0 ORDER BY deletedAt DESC")
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

    // Mark person as completed
    @Query("UPDATE persons SET isCompleted = 1, completedAt = :completedAt, linkedNewPersonId = :linkedNewPersonId WHERE id = :id")
    suspend fun markAsCompleted(id: String, completedAt: Long, linkedNewPersonId: String)

    // Update amount AND date together when entering a new loan amount
    @Query("UPDATE persons SET amountGiven = :amount, dateGiven = :dateGiven WHERE id = :id")
    suspend fun updateAmountAndDate(id: String, amount: Double, dateGiven: Long)

    // Update amount, date AND reset all interest fields so old loan data never bleeds into new loan
    @Query("""
        UPDATE persons SET
            amountGiven      = :amount,
            dateGiven        = :dateGiven,
            interestRate     = 0.0,
            interestType     = 'PERCENTAGE',
            interestAmount   = 0.0,
            totalRepayment   = 0.0,
            loanType         = 'MONTHLY',
            numberOfInstallments = 10,
            perInstallmentAmount = 0.0,
            isDurationBased  = 0,
            durationDays     = NULL
        WHERE id = :id
    """)
    suspend fun updateAmountAndDateResetInterest(id: String, amount: Double, dateGiven: Long)

    // Update the pending-new-loan fields when the amount is filled in (legacy path)
    // Also resets all interest fields so old loan data never bleeds into new loan
    @Query("""
        UPDATE persons SET
            isPendingNewLoan  = 0,
            amountGiven       = :amount,
            dateGiven         = :dateGiven,
            interestRate      = 0.0,
            interestType      = 'PERCENTAGE',
            interestAmount    = 0.0,
            totalRepayment    = 0.0,
            loanType          = 'MONTHLY',
            numberOfInstallments = 10,
            perInstallmentAmount = 0.0,
            isDurationBased   = 0,
            durationDays      = NULL
        WHERE id = :id
    """)
    suspend fun activatePendingNewLoan(id: String, amount: Double, dateGiven: Long)

    @Query("SELECT SUM(amountGiven) FROM persons WHERE fileId = :fileId AND isDeleted = 0 AND isCompleted = 0")
    suspend fun getTotalGivenInFile(fileId: String): Double?

    @Query("SELECT SUM(amountGiven) FROM persons WHERE fileId = :fileId AND isDeleted = 0 AND isCompleted = 0 AND mode = 'CASH'")
    suspend fun getTotalGivenCashInFile(fileId: String): Double?

    @Query("SELECT SUM(amountGiven) FROM persons WHERE fileId = :fileId AND isDeleted = 0 AND isCompleted = 0 AND mode = 'UPI'")
    suspend fun getTotalGivenUpiInFile(fileId: String): Double?

    // ── Insights queries ───────────────────────────────────────────────────────
    @Query("SELECT SUM(amountGiven) FROM persons WHERE fileId = :fileId AND isDeleted = 0 AND isCompleted = 0 AND dateGiven >= :startOfDay AND dateGiven < :endOfDay")
    suspend fun getTotalGivenToday(fileId: String, startOfDay: Long, endOfDay: Long): Double?

    @Query("SELECT COUNT(*) FROM persons WHERE fileId = :fileId AND isDeleted = 0 AND isCompleted = 0")
    suspend fun getActiveLoanCount(fileId: String): Int

    @Query("SELECT COUNT(*) FROM persons WHERE fileId = :fileId AND isDeleted = 0 AND isCompleted = 1")
    suspend fun getCompletedLoanCount(fileId: String): Int

    @Query("SELECT COALESCE(SUM(totalRepayment), 0) FROM persons WHERE fileId = :fileId AND isDeleted = 0 AND isCompleted = 0")
    suspend fun getTotalOutstanding(fileId: String): Double

    // Updated version that includes ALL interest and loan fields
    @Query("""
        UPDATE persons SET
            amountGiven            = :amount,
            dateGiven              = :dateGiven,
            interestRate           = :interestRate,
            interestType           = :interestType,
            interestAmount         = :interestAmount,
            totalRepayment         = :totalRepayment,
            loanType               = :loanType,
            numberOfInstallments   = :numberOfInstallments,
            perInstallmentAmount   = :perInstallmentAmount,
            isDurationBased        = :isDurationBased,
            durationDays           = :durationDays
        WHERE id = :id
    """)
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
    )

    // Delete the pending-new-loan pink card for a given name + fileId.
    @Query("DELETE FROM persons WHERE name = :name AND fileId = :fileId AND amountGiven = 0.0 AND isCompleted = 0 AND isPendingNewLoan = 1")
    suspend fun deleteZeroCloneByNameAndFile(name: String, fileId: String)

    // Guard — count existing pending-new-loan pink cards for this name in this file.
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

    // Guard — count existing zero-amount active cards for this name in this file.
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

    // ── Loan history queries ───────────────────────────────────────────────
    /** Fetch all loan records for a person by their ID (all cycles, oldest first for numbering). */
    @Query("""
        SELECT * FROM persons
        WHERE fileId = (SELECT fileId FROM persons WHERE id = :personId)
          AND LOWER(name) = LOWER((SELECT name FROM persons WHERE id = :personId))
          AND isDeleted = 0
          AND isPendingNewLoan = 0
        ORDER BY dateGiven ASC
    """)
    fun getLoanHistoryByPersonId(personId: String): Flow<List<Person>>

    // One-time cleanup — keeps only the most-recently-inserted pending clone per (name, fileId)
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

    // ── Interest field updates ─────────────────────────────────────────────────

    /**
     * Update all interest-related fields on a single person row.
     * Called after the user confirms the loan amount dialog with interest details.
     */
    @Query("""
        UPDATE persons SET
            interestRate         = :interestRate,
            interestType         = :interestType,
            interestAmount       = :interestAmount,
            totalRepayment       = :totalRepayment,
            loanType             = :loanType,
            numberOfInstallments = :numberOfInstallments,
            perInstallmentAmount = :perInstallmentAmount,
            isDurationBased      = :isDurationBased,
            durationDays         = :durationDays
        WHERE id = :id
    """)
    suspend fun updateInterestFields(
        id: String,
        interestRate: Double,
        interestType: String = "PERCENTAGE",
        interestAmount: Double,
        totalRepayment: Double,
        loanType: String,
        numberOfInstallments: Int,
        perInstallmentAmount: Double,
        isDurationBased: Boolean,
        durationDays: Int?
    )

    @Query("SELECT * FROM persons WHERE isDeleted = 1 AND deletedAt < :cutoff")
    suspend fun getExpiredDeletedPersons(cutoff: Long): List<Person>

    @Query("SELECT * FROM persons WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getAllDeletedPersons(): Flow<List<Person>>

    @Query("SELECT * FROM persons")
    fun getAllPersonsIncludingDeleted(): Flow<List<Person>>
}