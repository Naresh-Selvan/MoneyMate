package com.moneymate.app.data.local.dao

import androidx.room.*
import com.moneymate.app.data.local.entity.ExpenseCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseCategoryDao {

    @Query("SELECT * FROM expense_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<ExpenseCategory>>

    @Query("SELECT * FROM expense_categories ORDER BY name ASC")
    suspend fun getAllCategoriesOnce(): List<ExpenseCategory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: ExpenseCategory): Long

    @Query("DELETE FROM expense_categories WHERE id = :id AND isDefault = 0")
    suspend fun deleteNonDefaultById(id: Long)

    @Query("SELECT COUNT(*) FROM expense_categories WHERE LOWER(name) = LOWER(:name)")
    suspend fun countByName(name: String): Int
}
