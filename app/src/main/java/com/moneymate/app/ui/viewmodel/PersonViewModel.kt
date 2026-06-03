package com.moneymate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymate.app.data.local.entity.EditPermissionScope
import com.moneymate.app.data.local.entity.Person
import com.moneymate.app.data.repository.PersonRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PersonViewModel @Inject constructor(
    private val repository: PersonRepository
) : ViewModel() {

    private val _currentFileId = MutableStateFlow<String?>(null)

    val persons: StateFlow<List<Person>> = _currentFileId
        .flatMapLatest { fileId ->
            if (fileId != null) repository.getPersonsByFile(fileId)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedPersons: StateFlow<List<Person>> = repository.getDeletedPersons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Completed persons that have been soft-deleted → shown in TrashScreen
    val deletedCompletedPersons: StateFlow<List<Person>> = repository.getDeletedCompletedPersons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Completed persons (fully repaid; auto-purge after 30 days)
    val completedPersons: StateFlow<List<Person>> = _currentFileId
        .flatMapLatest { fileId ->
            if (fileId != null) repository.getCompletedPersonsByFile(fileId)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pending-new-loan placeholders (zero amount, shown distinctly in the main list)
    val pendingNewLoanPersons: StateFlow<List<Person>> = _currentFileId
        .flatMapLatest { fileId ->
            if (fileId != null) repository.getPendingNewLoanPersonsByFile(fileId)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadPersonsForFile(fileId: String) {
        _currentFileId.value = fileId
    }

    fun insertPerson(person: Person) = viewModelScope.launch {
        repository.insertPerson(person)
    }

    fun updatePerson(person: Person) = viewModelScope.launch {
        repository.updatePerson(person)
    }

    fun updateNameAndPlace(id: String, name: String, place: String?) = viewModelScope.launch {
        repository.updateNameAndPlace(id, name, place)
    }

    fun softDeletePerson(id: String) = viewModelScope.launch {
        // Fix 2: Also hard-delete the zero-amount pending-new-loan clone for this person,
        // so no orphaned placeholder card remains after the parent is moved to trash.
        val person = repository.getPersonById(id)
        repository.softDeletePerson(id, System.currentTimeMillis())
        if (person != null) {
            repository.deleteZeroCloneByNameAndFile(person.name, person.fileId)
        }
    }

    /** Soft-deletes a completed person so it appears in TrashScreen for 180 days. */
    fun softDeleteCompletedPerson(id: String) = viewModelScope.launch {
        repository.softDeleteCompletedPerson(id, System.currentTimeMillis())
    }

    fun restorePerson(id: String) = viewModelScope.launch {
        repository.restorePerson(id)
    }

    fun hardDeletePerson(id: String) = viewModelScope.launch {
        repository.hardDeletePerson(id)
    }

    fun purgeExpiredPersons() = viewModelScope.launch {
        val cutoff = System.currentTimeMillis() - (180L * 24 * 60 * 60 * 1000)
        repository.purgeExpiredPersons(cutoff)
    }

    fun purgeExpiredCompletedPersons() = viewModelScope.launch {
        val cutoff = System.currentTimeMillis() - (180L * 24 * 60 * 60 * 1000)
        repository.purgeExpiredCompletedPersons(cutoff)
        repository.purgeExpiredDeletedCompletedPersons(cutoff)
    }

    fun setEditPermission(id: String, granted: Boolean, scope: EditPermissionScope) = viewModelScope.launch {
        repository.setEditPermission(id, granted, scope)
    }

    fun markAllUploadedInFile(fileId: String) = viewModelScope.launch {
        repository.markAllUploadedInFile(fileId, System.currentTimeMillis())
    }

    suspend fun getPersonById(id: String): Person? {
        return repository.getPersonById(id)
    }

    fun updateSortOrder(id: String, sortOrder: Int) = viewModelScope.launch {
        repository.updateSortOrder(id, sortOrder)
    }

    suspend fun findDuplicateByName(fileId: String, name: String): List<Person> =
        repository.findDuplicateByName(fileId, name)

    suspend fun findDuplicateByNameAndPlace(fileId: String, name: String, place: String): List<Person> =
        repository.findDuplicateByNameAndPlace(fileId, name, place)

    fun shiftSortOrdersAfter(fileId: String, afterSortOrder: Int) = viewModelScope.launch {
        repository.shiftSortOrdersAfter(fileId, afterSortOrder)
    }

    suspend fun shiftSortOrdersAfterSync(fileId: String, afterSortOrder: Int) {
        repository.shiftSortOrdersAfter(fileId, afterSortOrder)
    }

    /**
     * Marks the person as completed, creates a zero-placeholder in the main list,
     * and runs a 30-day purge on old completed records.
     * Returns the new placeholder ID so the caller can show a confirmation.
     */
    fun markAsCompleted(person: Person, onDone: (String) -> Unit = {}) = viewModelScope.launch {
        val newId = repository.markAsCompletedAndCreatePlaceholder(person)
        purgeExpiredCompletedPersons()
        onDone(newId)
    }

    /**
     * Marks a person as completed by ID, then inserts a fresh zero-amount clone
     * back into the active list so the person can start a new loan cycle.
     * Called from PersonDetailScreen after a payment brings the balance to zero.
     */
    fun markPersonAsCompleted(personId: String) = viewModelScope.launch {
        val person = repository.getPersonById(personId) ?: return@launch
        // Mark as completed (moves to completed list)
        repository.markAsCompletedAndCreatePlaceholder(person)
        purgeExpiredCompletedPersons()
        // Insert a fresh zero-amount clone so the person stays visible in active list
        val clone = person.copy(
            id          = java.util.UUID.randomUUID().toString(),
            amountGiven = 0.0,
            isCompleted = false,
            dateGiven   = System.currentTimeMillis()
        )
        repository.insertPerson(clone)
    }

    /** Converts a pending-new-loan placeholder into a real active record. */
    fun activatePendingNewLoan(id: String, amount: Double) = viewModelScope.launch {
        repository.activatePendingNewLoan(id, amount)
    }

    // ── Filter state ──────────────────────────────────────────────────────────
    val filterWeeks        = MutableStateFlow("")
    val filterMinAmount    = MutableStateFlow("")
    val filterMaxAmount    = MutableStateFlow("")
    val filterPaymentType  = MutableStateFlow(PaymentTypeFilterState.ALL)
    val filterCurrentPage  = MutableStateFlow(0)
    val filterShowOverallTotal = MutableStateFlow(true)
    val filterSearchQuery  = MutableStateFlow("")

    val filterViewStartDate = MutableStateFlow(0L)
    val filterViewNumWeeks  = MutableStateFlow("")

    // Controls whether the Completed section is expanded in the UI
    val showCompletedSection = MutableStateFlow(false)

    fun clearFilters() {
        filterWeeks.value   = ""
        filterMinAmount.value = ""
        filterMaxAmount.value = ""
        filterPaymentType.value = PaymentTypeFilterState.ALL
        filterCurrentPage.value = 0
        filterSearchQuery.value = ""
        filterViewStartDate.value = 0L
        filterViewNumWeeks.value = ""
    }

    enum class PaymentTypeFilterState {
        ALL, UPI_GIVEN, CASH_GIVEN, UPI_RECEIVED, CASH_RECEIVED
    }
}