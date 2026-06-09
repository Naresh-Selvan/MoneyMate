package com.moneymate.app.data.local.dao

import androidx.room.*
import com.moneymate.app.data.local.entity.InvestmentType
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentTypeDao {

    @Query("SELECT * FROM investment_types ORDER BY name ASC")
    fun getAllTypes(): Flow<List<InvestmentType>>

    @Query("SELECT * FROM investment_types ORDER BY name ASC")
    suspend fun getAllTypesOnce(): List<InvestmentType>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(type: InvestmentType): Long

    @Query("DELETE FROM investment_types WHERE id = :id AND isDefault = 0")
    suspend fun deleteNonDefaultById(id: Long)

    @Query("SELECT COUNT(*) FROM investment_types WHERE LOWER(name) = LOWER(:name)")
    suspend fun countByName(name: String): Int
}
