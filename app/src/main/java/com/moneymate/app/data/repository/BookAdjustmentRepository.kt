package com.moneymate.app.data.repository

import com.moneymate.app.data.local.dao.BookAdjustmentDao
import com.moneymate.app.data.local.entity.BookAdjustment
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookAdjustmentRepository @Inject constructor(
    private val bookAdjustmentDao: BookAdjustmentDao
) {
    suspend fun insert(adjustment: BookAdjustment) = bookAdjustmentDao.insert(adjustment)
    fun getByFileId(fileId: String): Flow<List<BookAdjustment>> = bookAdjustmentDao.getByFileId(fileId)
    suspend fun getByPersonId(personId: String): List<BookAdjustment> = bookAdjustmentDao.getByPersonId(personId)
    fun getAll(): Flow<List<BookAdjustment>> = bookAdjustmentDao.getAll()
}
