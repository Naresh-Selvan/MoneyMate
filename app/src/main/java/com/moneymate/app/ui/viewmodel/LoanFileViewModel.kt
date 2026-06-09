package com.moneymate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymate.app.auth.AuditLogger
import com.moneymate.app.data.local.entity.AuditAction
import com.moneymate.app.data.local.entity.CalculationMode
import com.moneymate.app.data.local.entity.LoanFile
import com.moneymate.app.data.repository.LoanFileRepository
import com.moneymate.app.data.repository.MaintenanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoanFileViewModel @Inject constructor(
    private val repository: LoanFileRepository,
    private val maintenanceRepository: MaintenanceRepository,
    private val auditLogger: AuditLogger
) : ViewModel() {

    val allFiles: StateFlow<List<LoanFile>> = repository.getAllFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashedFiles: StateFlow<List<LoanFile>> = repository.getTrashedFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFilesIncludingDeleted: StateFlow<List<LoanFile>> = repository.getAllFilesIncludingDeleted()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Insert a new file. The file is always created completely empty — no names,
     * no template data, no pre-populated persons. The user adds entries manually.
     * The [defaultInterestRate] and [defaultCalculationMode] are captured at
     * creation time from the second dialog shown after the name dialog.
     */
    fun insertFile(file: LoanFile) = viewModelScope.launch {
        repository.insertFile(file)
        auditLogger.log(
            action = AuditAction.ADD_FILE,
            targetType = "LoanFile",
            targetId = file.id,
            targetLabel = file.name
        )
    }

    fun updateFile(file: LoanFile) = viewModelScope.launch { repository.updateFile(file) }
    fun softDeleteFile(id: String) = viewModelScope.launch {
        val file = repository.getAllFilesOnce().find { it.id == id }
        repository.softDeleteFile(id, System.currentTimeMillis())
        auditLogger.log(
            action = AuditAction.DELETE_FILE,
            targetType = "LoanFile",
            targetId = id,
            targetLabel = file?.name ?: id
        )
    }
    fun restoreFile(id: String) = viewModelScope.launch {
        repository.restoreFile(id)
        auditLogger.log(
            action = AuditAction.ADD_FILE,
            targetType = "LoanFile",
            targetId = id,
            targetLabel = "Restored file #$id"
        )
    }
    fun hardDeleteFile(id: String) = viewModelScope.launch { repository.hardDeleteFile(id) }
    
    fun purgeExpiredFiles() = viewModelScope.launch {
        val cutoff = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        repository.purgeExpiredFiles(cutoff)
    }

    fun autoPurge() = viewModelScope.launch {
        maintenanceRepository.autoPurge()
    }

    fun markSynced(id: String) = viewModelScope.launch { repository.markSynced(id, true, System.currentTimeMillis()) }
    fun updateSortOrder(id: String, sortOrder: Int) = viewModelScope.launch { repository.updateSortOrder(id, sortOrder) }

    /**
     * Update only the interest-rate defaults for a file.
     * Called from the "File Interest Settings" dialog in FileDetailScreen.
     * Does NOT retroactively touch any existing Person rows.
     */
    fun updateFileInterestSettings(
        fileId: String,
        defaultInterestRate: Double,
        defaultCalculationMode: CalculationMode
    ) = viewModelScope.launch {
        val file = repository.getAllFilesOnce().find { it.id == fileId } ?: return@launch
        repository.updateFile(
            file.copy(
                defaultInterestRate = defaultInterestRate,
                defaultCalculationMode = defaultCalculationMode
            )
        )
    }
}