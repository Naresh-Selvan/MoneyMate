package com.moneymate.app.data.repository

import com.moneymate.app.data.local.dao.*
import com.moneymate.app.data.local.dao.ExpenseCategoryDao
import com.moneymate.app.data.local.dao.ExpenseDao
import com.moneymate.app.data.local.dao.ExpenseSummary
import com.moneymate.app.data.local.entity.Expense
import com.moneymate.app.data.local.entity.ExpenseCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val categoryDao: ExpenseCategoryDao
) {
    fun getExpensesByFile(fileId: String): Flow<List<Expense>> =
        expenseDao.getExpensesByFile(fileId)

    fun getExpensesByFileBetweenDates(fileId: String, from: Long, to: Long): Flow<List<Expense>> =
        expenseDao.getExpensesByFileBetweenDates(fileId, from, to)

    fun getAllCategories(): Flow<List<ExpenseCategory>> =
        categoryDao.getAllCategories()

    suspend fun getAllCategoriesOnce(): List<ExpenseCategory> =
        categoryDao.getAllCategoriesOnce()

    suspend fun getExpenseSummary(fileId: String, from: Long, to: Long): ExpenseSummary =
        expenseDao.getExpenseSummary(fileId, from, to)

    suspend fun getAllNonDeletedExpenses(fileId: String): List<Expense> =
        expenseDao.getAllNonDeletedExpenses(fileId)

    suspend fun insert(expense: Expense): Long = withContext(Dispatchers.IO) {
        expenseDao.insert(expense)
    }

    suspend fun update(expense: Expense) = withContext(Dispatchers.IO) {
        expenseDao.update(expense)
    }

    suspend fun softDelete(id: Long) = withContext(Dispatchers.IO) {
        expenseDao.softDelete(id)
    }

    suspend fun addCategory(name: String): Long {
        if (categoryDao.countByName(name) > 0) return -1L
        return categoryDao.insert(ExpenseCategory(name = name, isDefault = false))
    }

    suspend fun deleteCategory(id: Long) {
        categoryDao.deleteNonDefaultById(id)
    }

    suspend fun getExpenseById(id: Long): Expense? = expenseDao.getById(id)

    fun getCategorySummary(fileId: String, from: Long, to: Long): kotlinx.coroutines.flow.Flow<List<CategorySummary>> =
        expenseDao.getCategorySummary(fileId, from, to)

    suspend fun getSiteTotalExpenses(from: Long, to: Long): Double =
        expenseDao.getSiteTotalExpenses(from, to)
}
