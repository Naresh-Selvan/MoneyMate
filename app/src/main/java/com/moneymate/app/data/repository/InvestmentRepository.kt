package com.moneymate.app.data.repository

import com.moneymate.app.data.local.dao.*
import com.moneymate.app.data.local.dao.InvestmentDao
import com.moneymate.app.data.local.dao.InvestmentSummary
import com.moneymate.app.data.local.dao.InvestmentTypeDao
import com.moneymate.app.data.local.entity.Investment
import com.moneymate.app.data.local.entity.InvestmentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvestmentRepository @Inject constructor(
    private val investmentDao: InvestmentDao,
    private val typeDao: InvestmentTypeDao
) {
    fun getInvestmentsByFile(fileId: String): Flow<List<Investment>> =
        investmentDao.getInvestmentsByFile(fileId)

    fun getInvestmentsByFileBetweenDates(fileId: String, from: Long, to: Long): Flow<List<Investment>> =
        investmentDao.getInvestmentsByFileBetweenDates(fileId, from, to)

    fun getAllTypes(): Flow<List<InvestmentType>> =
        typeDao.getAllTypes()

    suspend fun getAllTypesOnce(): List<InvestmentType> =
        typeDao.getAllTypesOnce()

    suspend fun getInvestmentSummary(fileId: String, from: Long, to: Long): InvestmentSummary =
        investmentDao.getInvestmentSummary(fileId, from, to)

    suspend fun getAllNonDeletedInvestments(fileId: String): List<Investment> =
        investmentDao.getAllNonDeletedInvestments(fileId)

    suspend fun insert(investment: Investment): Long = withContext(Dispatchers.IO) {
        investmentDao.insert(investment)
    }

    suspend fun update(investment: Investment) = withContext(Dispatchers.IO) {
        investmentDao.update(investment)
    }

    suspend fun softDelete(id: Long) = withContext(Dispatchers.IO) {
        investmentDao.softDelete(id)
    }

    suspend fun addType(name: String): Long {
        if (typeDao.countByName(name) > 0) return -1L
        return typeDao.insert(InvestmentType(name = name, isDefault = false))
    }

    suspend fun deleteType(id: Long) {
        typeDao.deleteNonDefaultById(id)
    }

    suspend fun getInvestmentById(id: Long): Investment? = investmentDao.getById(id)

    fun getTypeSummary(fileId: String, from: Long, to: Long): kotlinx.coroutines.flow.Flow<List<InvestmentCategorySummary>> =
        investmentDao.getTypeSummary(fileId, from, to)
}
