package com.moneymate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymate.app.data.local.dao.InvestmentSummary
import com.moneymate.app.data.local.entity.Investment
import com.moneymate.app.data.local.entity.InvestmentType
import com.moneymate.app.data.repository.InvestmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InvestmentViewModel @Inject constructor(
    private val repository: InvestmentRepository
) : ViewModel() {

    private val _selectedFileId = MutableStateFlow<String?>(null)
    private val _fromDate = MutableStateFlow(ExpenseViewModel.defaultMonthStart())
    private val _toDate = MutableStateFlow(System.currentTimeMillis())
    private val _onlineOnly = MutableStateFlow(false)
    private val _searchQuery = MutableStateFlow("")

    val selectedFileId: StateFlow<String?> = _selectedFileId
    val fromDate: StateFlow<Long> = _fromDate
    val toDate: StateFlow<Long> = _toDate
    val onlineOnly: StateFlow<Boolean> = _onlineOnly
    val searchQuery: StateFlow<String> = _searchQuery

    val types: StateFlow<List<InvestmentType>> = repository.getAllTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val investments: StateFlow<List<Investment>> = combine(
        _selectedFileId, _fromDate, _toDate, _onlineOnly, _searchQuery
    ) { fileId, from, to, online, query -> FilterParams(fileId, from, to, online, query) }
        .flatMapLatest { params ->
            if (params.fileId == null) flowOf(emptyList())
            else repository.getInvestmentsByFileBetweenDates(params.fileId, params.from, params.to)
        }
        .map { list ->
            var filtered = list
            if (_onlineOnly.value) filtered = filtered.filter { it.isOnline }
            if (_searchQuery.value.isNotBlank()) {
                val q = _searchQuery.value.trim().lowercase()
                filtered = filtered.filter {
                    it.type.lowercase().contains(q) ||
                            it.notes?.lowercase()?.contains(q) == true
                }
            }
            filtered
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val summary: StateFlow<InvestmentSummary> = combine(
        _selectedFileId, _fromDate, _toDate
    ) { fileId, from, to -> Triple(fileId, from, to) }
        .flatMapLatest { (fileId, from, to) ->
            if (fileId == null) flowOf(InvestmentSummary())
            else flow { emit(repository.getInvestmentSummary(fileId, from, to)) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InvestmentSummary())

    fun setSelectedFile(fileId: String) { _selectedFileId.value = fileId }
    fun setFromDate(ms: Long) { _fromDate.value = ms }
    fun setToDate(ms: Long) { _toDate.value = ms }
    fun setOnlineOnly(enabled: Boolean) { _onlineOnly.value = enabled }
    fun setSearchQuery(query: String) { _searchQuery.value = query }

    fun addInvestment(investment: Investment) = viewModelScope.launch {
        repository.insert(investment)
    }

    fun updateInvestment(investment: Investment) = viewModelScope.launch {
        repository.update(investment)
    }

    fun deleteInvestment(id: Long) = viewModelScope.launch {
        repository.softDelete(id)
    }

    fun addType(name: String, onResult: (Boolean) -> Unit = {}) = viewModelScope.launch {
        val id = repository.addType(name)
        onResult(id > 0)
    }

    fun deleteType(id: Long) = viewModelScope.launch {
        repository.deleteType(id)
    }

    private data class FilterParams(
        val fileId: String?,
        val from: Long,
        val to: Long,
        val online: Boolean,
        val query: String
    )
}
