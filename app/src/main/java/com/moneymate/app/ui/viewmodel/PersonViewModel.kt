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

    // Completed persons (fully repaid; visible in Completed sheet for 180 days)
    val completedPersons: StateFlow<List<Person>> = _currentFileId
        .flatMapLatest { fileId ->
            if (fileId != null) repository.getCompletedPersonsByFile(fileId)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Loan history ───────────────────────────────────────────────────────
    private val _loanHistoryName = MutableStateFlow<Pair<String, String>?>(null)

    val loanHistory: StateFlow<List<Person>> = _loanHistoryName
        .flatMapLatest { pair ->
            if (pair != null) repository.getLoanHistoryByName(pair.first, pair.second)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadLoanHistory(fileId: String, personName: String) {
        _loanHistoryName.value = Pair(fileId, personName)
    }

    // Pink indicator cards (isPendingNewLoan = true)
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

    /** Reactive Flow variant — for screens that need live person updates (BUG 5 fix). */
    fun getPersonByIdFlow(id: String): kotlinx.coroutines.flow.Flow<Person?> =
        repository.getPersonByIdFlow(id)

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
     * BUG 6 FIX: Decrements sortOrder for all persons whose sortOrder is strictly
     * greater than [currentSortOrder], closing the gap left by a person being moved.
     */
    fun shiftSortOrdersDown(fileId: String, currentSortOrder: Int) = viewModelScope.launch {
        repository.shiftSortOrdersDown(fileId, currentSortOrder)
    }

    /**
     * Marks the person as completed.
     * After this call the DB will contain:
     *   • Original row           → isCompleted = 1  (Completed section)
     *   • White active card      → amountGiven = 0.0, isPendingNewLoan = false  (main list)
     *   • Pink indicator card    → amountGiven = 0.0, isPendingNewLoan = true   (pink row)
     */
    fun markAsCompleted(person: Person, onDone: (String) -> Unit = {}) = viewModelScope.launch {
        val newId = repository.markAsCompletedAndCreatePlaceholder(person)
        purgeExpiredCompletedPersons()
        onDone(newId)
    }

    /**
     * ID-based variant used from PersonDetailScreen when a payment brings the balance to zero.
     */
    fun markPersonAsCompleted(personId: String) = viewModelScope.launch {
        val person = repository.getPersonById(personId) ?: return@launch
        repository.markAsCompletedAndCreatePlaceholder(person)
        purgeExpiredCompletedPersons()
    }

    /** Converts a pending-new-loan pink card into a real active record (legacy path). */
    fun activatePendingNewLoan(id: String, amount: Double) = viewModelScope.launch {
        repository.activatePendingNewLoan(id, amount)
    }

    /**
     * Called when boss taps the white ₹0.0 active card and confirms a new loan amount.
     * Updates amountGiven, sets dateGiven = TODAY, deletes the pink indicator card.
     */
    fun activateZeroActiveCard(person: Person, amount: Double) = viewModelScope.launch {
        repository.activateZeroActiveCard(person, amount)
    }

    /**
     * BUG 2 FIX: Deletes the white ₹0 placeholder for a person after a new loan
     * is created from the completed card tap flow, preventing duplicate active cards.
     */
    fun deleteZeroPlaceholderByNameAndFile(name: String, fileId: String) = viewModelScope.launch {
        repository.deleteZeroCloneByNameAndFile(name, fileId)
    }

    /**
     * Remove any duplicate pending-new-loan pink cards.
     */
    fun removeDuplicatePendingClones() = viewModelScope.launch {
        repository.removeDuplicatePendingClones()
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
        filterWeeks.value     = ""
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