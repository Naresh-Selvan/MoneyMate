package com.moneymate.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.moneymate.app.data.local.entity.BookAdjustment
import kotlinx.coroutines.flow.Flow

@Dao
interface BookAdjustmentDao {
    @Insert
    suspend fun insert(adjustment: BookAdjustment)

    @Query("SELECT * FROM book_adjustments WHERE fileId = :fileId ORDER BY createdAt DESC")
    fun getByFileId(fileId: String): Flow<List<BookAdjustment>>

    @Query("SELECT * FROM book_adjustments WHERE personId = :personId ORDER BY createdAt DESC")
    suspend fun getByPersonId(personId: String): List<BookAdjustment>

    @Query("SELECT * FROM book_adjustments ORDER BY createdAt DESC")
    fun getAll(): Flow<List<BookAdjustment>>
}
