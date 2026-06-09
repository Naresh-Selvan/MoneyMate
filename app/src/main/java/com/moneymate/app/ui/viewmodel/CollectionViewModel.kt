package com.moneymate.app.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.moneymate.app.data.local.entity.Payment
import com.moneymate.app.data.local.entity.Person
import com.moneymate.app.data.repository.PersonRepository
import com.moneymate.app.data.repository.PaymentRepository
import com.moneymate.app.utils.FirestorePathProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.UUID
import javax.inject.Inject

data class DashboardSummary(
    val date: Long = System.currentTimeMillis(),
    val totalOutstanding: Double = 0.0,
    val openingBalance: Double = 0.0,
    val activeLoanCount: Int = 0,
    val completedCount: Int = 0,
    val todayCollection: Double = 0.0,
    val todayNewLoans: Double = 0.0,
    val todayExpense: Double = 0.0,
    val todayBill: Double = 0.0
)

data class CollectionPersonState(
    val person: Person,
    val totalPaid: Double = 0.0,
    val pending: Double = 0.0,
    val paidToday: Boolean = false,
    val isBadLoan: Boolean = false,
    val installmentNumber: Int = 0,
    val totalInstallments: Int = 0,
    val perInstallmentAmount: Double = 0.0
)

@HiltViewModel
class CollectionViewModel @Inject constructor(
    private val personRepository: PersonRepository,
    private val paymentRepository: PaymentRepository,
    private val paths: FirestorePathProvider
) : ViewModel() {

    private val _currentFileId = MutableStateFlow<String?>(null)
    private val _filterPendingPayments = MutableStateFlow(false)
    private val _showAllCustomers = MutableStateFlow(false)
    private val _isReordering = MutableStateFlow(false)
    private val _selectedTab = MutableStateFlow(0)
    private val _isHeaderExpanded = MutableStateFlow(true)

    val filterPendingPayments: StateFlow<Boolean> = _filterPendingPayments
    val showAllCustomers: StateFlow<Boolean> = _showAllCustomers
    val isReordering: StateFlow<Boolean> = _isReordering
    val selectedTab: StateFlow<Int> = _selectedTab
    val isHeaderExpanded: StateFlow<Boolean> = _isHeaderExpanded

    val lendingPersons: StateFlow<List<Person>> = _currentFileId
        .flatMapLatest { fileId ->
            if (fileId != null) personRepository.getLendingPersonsByFile(fileId)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val borrowingPersons: StateFlow<List<Person>> = _currentFileId
        .flatMapLatest { fileId ->
            if (fileId != null) personRepository.getBorrowingPersonsByFile(fileId)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedPersons: StateFlow<List<Person>> = _currentFileId
        .flatMapLatest { fileId ->
            if (fileId != null) personRepository.getCompletedPersonsByFile(fileId)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filePayments: StateFlow<List<Payment>> = _currentFileId
        .flatMapLatest { fileId ->
            if (fileId != null) paymentRepository.getPaymentsForFile(fileId)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboard: StateFlow<DashboardSummary> = combine(
        _currentFileId,
        filePayments,
        lendingPersons,
        borrowingPersons,
        completedPersons
    ) { fileId, payments, lending, borrowing, completed ->
        if (fileId == null) return@combine DashboardSummary()

        val allActive = lending + borrowing
        val totalOutstanding = allActive.sumOf {
            val effectiveTotal = if (it.totalRepayment > 0) it.totalRepayment else it.amountGiven
            val paid = payments.filter { p -> p.personId == it.id && !it.isDeleted }.sumOf { p -> p.amount }
            (effectiveTotal - paid).coerceAtLeast(0.0)
        }

        val todayStart = todayStartMs()
        val todayEnd = todayEndMs()
        val todayPayments = payments.filter { it.date in todayStart..todayEnd && !it.isDeleted }
        val todayCollection = todayPayments.sumOf { it.amount }
        val todayLoans = allActive.filter { it.dateGiven in todayStart..todayEnd }.sumOf { it.amountGiven }

        // Opening balance = yesterday's closing balance
        // totalOutstanding = openingBalance + todayLoans - todayCollection
        // So: openingBalance = totalOutstanding + todayCollection - todayLoans
        val openingBalance = totalOutstanding + todayCollection - todayLoans

        DashboardSummary(
            totalOutstanding = totalOutstanding,
            openingBalance = openingBalance,
            activeLoanCount = allActive.size,
            completedCount = completed.size,
            todayCollection = todayCollection,
            todayNewLoans = todayLoans
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    fun loadFile(fileId: String) {
        _currentFileId.value = fileId
    }

    fun setFilterPendingPayments(enabled: Boolean) { _filterPendingPayments.value = enabled }
    fun setShowAllCustomers(enabled: Boolean) { _showAllCustomers.value = enabled }
    fun setReordering(enabled: Boolean) { _isReordering.value = enabled }
    fun setSelectedTab(tab: Int) { _selectedTab.value = tab }
    fun toggleHeaderExpanded() { _isHeaderExpanded.value = !_isHeaderExpanded.value }

    /** Compute the full state for a single person card. */
    fun getCollectionPersonState(person: Person, allPayments: List<Payment>): CollectionPersonState {
        val personPayments = allPayments.filter { it.personId == person.id && !it.isDeleted }
        val totalPaid = personPayments.sumOf { it.amount }
        val effectiveTotal = if (person.totalRepayment > 0) person.totalRepayment else person.amountGiven
        val pending = (effectiveTotal - totalPaid).coerceAtLeast(0.0)

        val todayStart = todayStartMs()
        val todayEnd = todayEndMs()
        val paidToday = personPayments.any { it.date in todayStart..todayEnd }

        val isBadLoan = if (person.amountGiven <= 0.0) false
        else {
            val latestTs = personPayments.maxOfOrNull { it.date }
            if (latestTs == null) false
            else {
                val elapsedDays = (System.currentTimeMillis() - latestTs) / (1000L * 60 * 60 * 24)
                elapsedDays >= person.badLoanDays
            }
        }

        val todayPaymentsCount = personPayments.count { it.date in todayStart..todayEnd }
        val installmentNumber = minOf(todayPaymentsCount + 1, person.numberOfInstallments)

        return CollectionPersonState(
            person = person,
            totalPaid = totalPaid,
            pending = pending,
            paidToday = paidToday,
            isBadLoan = isBadLoan,
            installmentNumber = installmentNumber,
            totalInstallments = person.numberOfInstallments,
            perInstallmentAmount = person.perInstallmentAmount
        )
    }

    fun getPersonStates(persons: List<Person>, allPayments: List<Payment>): List<CollectionPersonState> =
        persons.map { getCollectionPersonState(it, allPayments) }

    /** Return persons who have NOT paid today. */
    fun getUnpaidToday(persons: List<Person>, allPayments: List<Payment>): List<Person> {
        val todayStart = todayStartMs()
        val todayEnd = todayEndMs()
        val paidTodayIds = allPayments
            .filter { it.date in todayStart..todayEnd && !it.isDeleted }
            .map { it.personId }
            .toSet()
        return persons.filter { it.id !in paidTodayIds }
    }

    /** Record a quick payment. */
    fun recordQuickPayment(personId: String, amount: Double, mode: com.moneymate.app.data.local.entity.PaymentMode = com.moneymate.app.data.local.entity.PaymentMode.CASH) {
        viewModelScope.launch {
            paymentRepository.insertPayment(
                Payment(personId = personId, amount = amount, mode = mode, date = System.currentTimeMillis())
            )
        }
    }

    /** Update sort orders after reorder. */
    fun updateSortOrders(fileId: String, reorderedList: List<Person>) {
        viewModelScope.launch {
            reorderedList.forEachIndexed { index, person ->
                personRepository.updateSortOrder(person.id, index)
            }
        }
    }

    /** Move a person to another file with Firestore sync for both files. */
    fun movePersonToFile(person: Person, targetFileId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            // Soft-delete from current file (syncs to Firestore via repository)
            personRepository.softDeletePerson(person.id, System.currentTimeMillis())

            // Insert into target file with new ID
            val newPerson = person.copy(
                id = UUID.randomUUID().toString(),
                fileId = targetFileId,
                sortOrder = 0,
                isDeleted = false,
                deletedAt = null,
                uploadedAt = null
            )
            personRepository.insertPerson(newPerson)

            // Sync new person to Firestore target file using FirestorePathProvider
            try {
                val db = FirebaseFirestore.getInstance()
                val docRef = db.collection(paths.personsCollection(targetFileId)).document(newPerson.id)
                docRef.set(newPerson, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.e("CollectionVM", "Firestore sync failed for moved person to $targetFileId", e)
            }

            onComplete()
        }
    }

    fun isPersonBadLoan(person: Person, payments: List<Payment>): Boolean {
        if (person.amountGiven <= 0.0) return false
        val personPayments = payments.filter { it.personId == person.id && !it.isDeleted }
        val latestTs = personPayments.maxOfOrNull { it.date } ?: return false
        val elapsedDays = (System.currentTimeMillis() - latestTs) / (1000L * 60 * 60 * 24)
        return elapsedDays >= person.badLoanDays
    }

    companion object {
        private fun todayStartMs(): Long {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        private fun todayEndMs(): Long {
            val cal = Calendar.getInstance()
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            return cal.timeInMillis
        }
    }
}
