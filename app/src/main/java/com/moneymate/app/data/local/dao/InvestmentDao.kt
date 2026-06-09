package com.moneymate.app.data.local.dao

import androidx.room.*
import com.moneymate.app.data.local.entity.Investment
import kotlinx.coroutines.flow.Flow

data class InvestmentSummary(
    val onlineTotal: Double = 0.0,
    val cashTotal: Double = 0.0,
    val grandTotal: Double = 0.0
)

@Dao
interface InvestmentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(investment: Investment): Long

    @Update
    suspend fun update(investment: Investment)

    @Query("UPDATE investments SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("SELECT * FROM investments WHERE fileId = :fileId AND isDeleted = 0 ORDER BY date DESC")
    fun getInvestmentsByFile(fileId: String): Flow<List<Investment>>

    @Query("""
        SELECT * FROM investments 
        WHERE fileId = :fileId AND isDeleted = 0 
          AND date >= :from AND date <= :to 
        ORDER BY date DESC
    """)
    fun getInvestmentsByFileBetweenDates(fileId: String, from: Long, to: Long): Flow<List<Investment>>

    @Query("""
        SELECT 
            COALESCE(SUM(CASE WHEN isOnline = 1 THEN amount ELSE 0 END), 0) as onlineTotal,
            COALESCE(SUM(CASE WHEN isOnline = 0 THEN amount ELSE 0 END), 0) as cashTotal,
            COALESCE(SUM(amount), 0) as grandTotal
        FROM investments 
        WHERE fileId = :fileId AND isDeleted = 0 
          AND date >= :from AND date <= :to
    """)
    suspend fun getInvestmentSummary(fileId: String, from: Long, to: Long): InvestmentSummary

    @Query("SELECT * FROM investments WHERE fileId = :fileId AND isDeleted = 0 ORDER BY date DESC")
    suspend fun getAllNonDeletedInvestments(fileId: String): List<Investment>

    @Query("SELECT * FROM investments WHERE id = :id")
    suspend fun getById(id: Long): Investment?

    // Report 7 — Type Summary (grouped by type)
    @Query("""
        SELECT type as type,
               COALESCE(SUM(CASE WHEN isOnline = 0 THEN amount ELSE 0 END), 0) as cashTotal,
               COALESCE(SUM(CASE WHEN isOnline = 1 THEN amount ELSE 0 END), 0) as onlineTotal,
               COALESCE(SUM(amount), 0) as grandTotal
        FROM investments
        WHERE fileId = :fileId AND isDeleted = 0
          AND date >= :from AND date <= :to
        GROUP BY type
        ORDER BY grandTotal DESC
    """)
    fun getTypeSummary(fileId: String, from: Long, to: Long): kotlinx.coroutines.flow.Flow<List<InvestmentCategorySummary>>
}
