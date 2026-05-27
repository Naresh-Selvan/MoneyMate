package com.moneymate.app.data.local.dao

import androidx.room.*
import com.moneymate.app.data.local.entity.LoanFile
import kotlinx.coroutines.flow.Flow

@Dao
interface FileDao {

    @Query("SELECT * FROM loan_files WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getTrashedFiles(): Flow<List<LoanFile>>

    @Query("SELECT * FROM loan_files WHERE id = :id")
    suspend fun getFileById(id: String): LoanFile?

    @Query("SELECT * FROM loan_files WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun getFileByName(name: String): LoanFile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: LoanFile)

    @Update
    suspend fun updateFile(file: LoanFile)

    @Query("UPDATE loan_files SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun softDeleteFile(id: String, deletedAt: Long)

    @Query("UPDATE loan_files SET isDeleted = 0, deletedAt = null WHERE id = :id")
    suspend fun restoreFile(id: String)

    @Query("DELETE FROM loan_files WHERE id = :id")
    suspend fun hardDeleteFile(id: String)

    @Query("DELETE FROM loan_files WHERE isDeleted = 1 AND deletedAt < :cutoff")
    suspend fun purgeExpiredFiles(cutoff: Long)

    @Query("UPDATE loan_files SET syncedToFirebase = :synced, lastUploadedAt = :uploadedAt WHERE id = :id")
    suspend fun markSynced(id: String, synced: Boolean, uploadedAt: Long)

    @Query("SELECT * FROM loan_files WHERE isDeleted = 0 ORDER BY sortOrder ASC, createdAt DESC")
    fun getAllFiles(): Flow<List<LoanFile>>

    @Query("SELECT * FROM loan_files WHERE isDeleted = 0 ORDER BY sortOrder ASC, createdAt DESC")
    suspend fun getAllFilesOnce(): List<LoanFile>

    @Query("UPDATE loan_files SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: String, sortOrder: Int)
}