package com.moneymate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymate.app.data.local.NlrSeedData
import com.moneymate.app.data.local.entity.LoanFile
import com.moneymate.app.data.local.entity.Person
import com.moneymate.app.data.repository.DefaultPersonRepository
import com.moneymate.app.data.repository.LoanFileRepository
import com.moneymate.app.data.repository.PersonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoanFileViewModel @Inject constructor(
    private val repository: LoanFileRepository,
    private val personRepository: PersonRepository,
    private val defaultPersonRepository: DefaultPersonRepository
) : ViewModel() {

    val allFiles: StateFlow<List<LoanFile>> = repository.getAllFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trashedFiles: StateFlow<List<LoanFile>> = repository.getTrashedFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** NLR keys that have a template */
    val nlrKeys = listOf("NLR 1", "NLR 2", "NLR 3", "NLR 4")

    init {
        // Seed default persons on first launch (once per NLR key)
        viewModelScope.launch {
            nlrKeys.forEach { key ->
                if (defaultPersonRepository.countForNlr(key) == 0) {
                    val seed = when (key) {
                        "NLR 1" -> NlrSeedData.NLR1
                        "NLR 2" -> NlrSeedData.NLR2
                        "NLR 3" -> NlrSeedData.NLR3
                        "NLR 4" -> NlrSeedData.NLR4
                        else -> emptyList()
                    }
                    defaultPersonRepository.insertAll(seed)
                }
            }
        }
    }

    /**
     * Insert a new file. If it's an NLR file and we have a template,
     * auto-insert all default persons into it.
     */
    fun insertFile(file: LoanFile) = viewModelScope.launch {
        repository.insertFile(file)

        // Resolve the NLR key from the file name
        val nlrKey = nlrKeys.firstOrNull { file.name.equals(it, ignoreCase = true) }
        if (nlrKey != null) {
            val defaults = defaultPersonRepository.getForNlrOnce(nlrKey)
            val defaultEntryDate = System.currentTimeMillis()
            val persons = defaults.mapIndexed { idx, d ->
                Person(
                    fileId = file.id,
                    name = d.name,
                    place = d.place,
                    mobileNumber = d.mobileNumber,
                    amountGiven = d.amountGiven,
                    mode = d.mode,
                    dateGiven = defaultEntryDate,
                    sortOrder = idx,
                    recordType = d.recordType
                )
            }
            persons.forEach { personRepository.insertPerson(it) }
        }
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

    /**
     * Sync predefined names from the Settings template into all existing NLR files.
     * Only names not already present (by name, case-insensitive) in a file are added.
     * Returns the total count of persons added across all files.
     */
    fun syncTemplateToExistingFiles(onResult: (added: Int) -> Unit) = viewModelScope.launch {
        val allFiles = repository.getAllFilesOnce()
        var totalAdded = 0
        nlrKeys.forEach { key ->
            val defaults = defaultPersonRepository.getForNlrOnce(key)
            if (defaults.isEmpty()) return@forEach
            val matchingFiles = allFiles.filter { it.name.equals(key, ignoreCase = true) }
            matchingFiles.forEach { file ->
                val existing = personRepository.findAllNamesInFile(file.id)
                val existingLower = existing.map { it.lowercase() }.toSet()
                val nowTime = System.currentTimeMillis()
                val startSort = existing.size
                defaults.forEachIndexed { idx, d ->
                    if (d.name.lowercase() !in existingLower) {
                        personRepository.insertPerson(
                            Person(
                                fileId = file.id,
                                name = d.name,
                                place = d.place,
                                mobileNumber = d.mobileNumber,
                                amountGiven = d.amountGiven,
                                mode = d.mode,
                                dateGiven = nowTime,
                                sortOrder = startSort + idx,
                                recordType = d.recordType
                            )
                        )
                        totalAdded++
                    }
                }
            }
        }
        onResult(totalAdded)
    }
}