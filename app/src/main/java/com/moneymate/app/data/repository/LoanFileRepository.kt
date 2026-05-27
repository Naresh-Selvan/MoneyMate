package com.moneymate.app.data.repository

import com.moneymate.app.data.local.dao.FileDao
import com.moneymate.app.data.local.entity.LoanFile
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanFileRepository @Inject constructor(
    private val fileDao: FileDao
) {
    fun getAllFiles(): Flow<List<LoanFile>> = fileDao.getAllFiles()

    suspend fun getAllFilesOnce(): List<LoanFile> = fileDao.getAllFilesOnce()

    fun getTrashedFiles(): Flow<List<LoanFile>> = fileDao.getTrashedFiles()

    suspend fun insertFile(file: LoanFile) = fileDao.insertFile(file)

    suspend fun updateFile(file: LoanFile) = fileDao.updateFile(file)

    suspend fun softDeleteFile(id: String, deletedAt: Long) =
        fileDao.softDeleteFile(id, deletedAt)

    suspend fun restoreFile(id: String) = fileDao.restoreFile(id)

    suspend fun hardDeleteFile(id: String) = fileDao.hardDeleteFile(id)

    suspend fun purgeExpiredFiles(cutoff: Long) = fileDao.purgeExpiredFiles(cutoff)
    suspend fun markSynced(id: String, synced: Boolean, uploadedAt: Long) =
        fileDao.markSynced(id, synced, uploadedAt)

    suspend fun updateSortOrder(id: String, sortOrder: Int) =
        fileDao.updateSortOrder(id, sortOrder)
}