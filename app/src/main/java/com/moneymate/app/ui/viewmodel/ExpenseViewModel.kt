package com.moneymate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymate.app.data.local.dao.ExpenseSummary
import com.moneymate.app.data.local.entity.Expense
import com.moneymate.app.data.local.entity.ExpenseCategory
import com.moneymate.app.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val repository: ExpenseRepository
) : ViewModel() {

    private val _selectedFileId = MutableStateFlow<String?>(null)
    private val _fromDate = MutableStateFlow(defaultMonthStart())
    private val _toDate = MutableStateFlow(System.currentTimeMillis())
    private val _onlineOnly = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")

    val selectedFileId: StateFlow<String?> = _selectedFileId
    val fromDate: StateFlow<Long> = _fromDate
    val toDate: StateFlow<Long> = _toDate
    val onlineOnly: StateFlow<Boolean> = _onlineOnly
    val searchQuery: StateFlow<String> = _searchQuery

    val categories: StateFlow<List<ExpenseCategory>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val expenses: StateFlow<List<Expense>> = combine(
        _selectedFileId, _fromDate, _toDate, _onlineOnly, _searchQuery
    ) { fileId, from, to, online, query -> FilterParams(fileId, from, to, online, query) }
        .flatMapLatest { params ->
            if (params.fileId == null) flowOf(emptyList())
            else repository.getExpensesByFileBetweenDates(params.fileId, params.from, params.to)
        }
        .map { list ->
            var filtered = list
            if (_onlineOnly.value) filtered = filtered.filter { it.isOnline }
            if (_searchQuery.value.isNotBlank()) {
                val q = _searchQuery.value.trim().lowercase()
                filtered = filtered.filter {
                    it.category.lowercase().contains(q) ||
                            it.notes?.lowercase()?.contains(q) == true
                }
            }
            filtered
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summary: StateFlow<ExpenseSummary> = combine(
        _selectedFileId, _fromDate, _toDate
    ) { fileId, from, to -> Triple(fileId, from, to) }
        .flatMapLatest { (fileId, from, to) ->
            if (fileId == null) flowOf(ExpenseSummary())
            else flow { emit(repository.getExpenseSummary(fileId, from, to)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ExpenseSummary())

    fun setSelectedFile(fileId: String) { _selectedFileId.value = fileId }
    fun setFromDate(ms: Long) { _fromDate.value = ms }
    fun setToDate(ms: Long) { _toDate.value = ms }
    fun setOnlineOnly(enabled: Boolean) { _onlineOnly.value = enabled }
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun addExpense(expense: Expense) = viewModelScope.launch {
        repository.insert(expense)
    }

    fun updateExpense(expense: Expense) = viewModelScope.launch {
        repository.update(expense)
    }

    fun deleteExpense(id: Long) = viewModelScope.launch {
        repository.softDelete(id)
    }

    fun addCategory(name: String, onResult: (Boolean) -> Unit = {}) = viewModelScope.launch {
        val id = repository.addCategory(name)
        onResult(id > 0)
    }

    fun deleteCategory(id: Long) = viewModelScope.launch {
        repository.deleteCategory(id)
    }

    private data class FilterParams(
        val fileId: String?,
        val from: Long,
        val to: Long,
        val online: Boolean,
        val query: String
    )

    companion object {
        fun defaultMonthStart(): Long {
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, 1)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }
    }
}
