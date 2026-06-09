package com.moneymate.app.data.local.dao

import androidx.room.*
import com.moneymate.app.data.local.entity.Expense
import kotlinx.coroutines.flow.Flow

data class ExpenseSummary(
    val onlineTotal: Double = 0.0,
    val cashTotal: Double = 0.0,
    val grandTotal: Double = 0.0
)

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Query("UPDATE expenses SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("SELECT * FROM expenses WHERE fileId = :fileId AND isDeleted = 0 ORDER BY date DESC")
    fun getExpensesByFile(fileId: String): Flow<List<Expense>>

    @Query("""
        SELECT * FROM expenses 
        WHERE fileId = :fileId AND isDeleted = 0 
          AND date >= :from AND date <= :to 
        ORDER BY date DESC
    """)
    fun getExpensesByFileBetweenDates(fileId: String, from: Long, to: Long): Flow<List<Expense>>

    @Query("""
        SELECT 
            COALESCE(SUM(CASE WHEN isOnline = 1 THEN amount ELSE 0 END), 0) as onlineTotal,
            COALESCE(SUM(CASE WHEN isOnline = 0 THEN amount ELSE 0 END), 0) as cashTotal,
            COALESCE(SUM(amount), 0) as grandTotal
        FROM expenses 
        WHERE fileId = :fileId AND isDeleted = 0 
          AND date >= :from AND date <= :to
    """)
    suspend fun getExpenseSummary(fileId: String, from: Long, to: Long): ExpenseSummary

    @Query("SELECT * FROM expenses WHERE fileId = :fileId AND isDeleted = 0 ORDER BY date DESC")
    suspend fun getAllNonDeletedExpenses(fileId: String): List<Expense>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getById(id: Long): Expense?

    // Report 6 — Category Summary (grouped by category)
    @Query("""
        SELECT category as category,
               COALESCE(SUM(CASE WHEN isOnline = 0 THEN amount ELSE 0 END), 0) as cashTotal,
               COALESCE(SUM(CASE WHEN isOnline = 1 THEN amount ELSE 0 END), 0) as onlineTotal,
               COALESCE(SUM(amount), 0) as grandTotal
        FROM expenses
        WHERE fileId = :fileId AND isDeleted = 0
          AND date >= :from AND date <= :to
        GROUP BY category
        ORDER BY grandTotal DESC
    """)
    fun getCategorySummary(fileId: String, from: Long, to: Long): kotlinx.coroutines.flow.Flow<List<CategorySummary>>

    // Site-level: total expenses across ALL files
    @Query("SELECT COALESCE(SUM(amount), 0) FROM expenses WHERE isDeleted = 0 AND date >= :from AND date <= :to")
    suspend fun getSiteTotalExpenses(from: Long, to: Long): Double
}
