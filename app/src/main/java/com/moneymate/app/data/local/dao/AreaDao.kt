package com.moneymate.app.data.local.dao

import androidx.room.*
import com.moneymate.app.data.local.entity.Area
import kotlinx.coroutines.flow.Flow

@Dao
interface AreaDao {

    @Query("SELECT * FROM areas WHERE fileId = :fileId AND isDeleted = 0 ORDER BY sortOrder ASC, name ASC")
    fun getAreasByFile(fileId: String): Flow<List<Area>>

    @Query("SELECT * FROM areas WHERE fileId = :fileId AND isDeleted = 0 ORDER BY sortOrder ASC, name ASC")
    suspend fun getAreasByFileOnce(fileId: String): List<Area>

    @Query("SELECT name FROM areas WHERE fileId = :fileId AND isDeleted = 0 ORDER BY sortOrder ASC, name ASC")
    fun getAreaNames(fileId: String): Flow<List<String>>

    @Query("SELECT name FROM areas WHERE fileId = :fileId AND isDeleted = 0 ORDER BY sortOrder ASC, name ASC")
    suspend fun getAreaNamesOnce(fileId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(area: Area): Long

    @Update
    suspend fun update(area: Area)

    @Query("UPDATE areas SET isDeleted = 1 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("UPDATE areas SET isDeleted = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("DELETE FROM areas WHERE id = :id")
    suspend fun hardDelete(id: Long)

    @Query("UPDATE areas SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: Long, sortOrder: Int)

    @Query("SELECT * FROM areas WHERE fileId = :fileId AND LOWER(name) = LOWER(:name) AND isDeleted = 0 LIMIT 1")
    suspend fun getAreaByName(fileId: String, name: String): Area?
}
