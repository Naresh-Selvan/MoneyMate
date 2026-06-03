package com.moneymate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymate.app.data.local.entity.LoanFile
import com.moneymate.app.data.repository.LoanFileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoanFileViewModel @Inject constructor(
    private val repository: LoanFileRepository
) : ViewModel() {

    val allFiles: StateFlow<List<LoanFile>> = repository.getAllFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashedFiles: StateFlow<List<LoanFile>> = repository.getTrashedFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Insert a new file. The file is always created completely empty — no names,
     * no template data, no pre-populated persons. The user adds entries manually.
     */
    fun insertFile(file: LoanFile) = viewModelScope.launch {
        repository.insertFile(file)
    }

    fun updateFile(file: LoanFile) = viewModelScope.launch { repository.updateFile(file) }
    fun softDeleteFile(id: String) = viewModelScope.launch { repository.softDeleteFile(id, System.currentTimeMillis()) }
    fun restoreFile(id: String) = viewModelScope.launch { repository.restoreFile(id) }
    fun hardDeleteFile(id: String) = viewModelScope.launch { repository.hardDeleteFile(id) }
    fun purgeExpiredFiles() = viewModelScope.launch {
        val cutoff = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        repository.purgeExpiredFiles(cutoff)
    }
    fun markSynced(id: String) = viewModelScope.launch { repository.markSynced(id, true, System.currentTimeMillis()) }
    fun updateSortOrder(id: String, sortOrder: Int) = viewModelScope.launch { repository.updateSortOrder(id, sortOrder) }
}