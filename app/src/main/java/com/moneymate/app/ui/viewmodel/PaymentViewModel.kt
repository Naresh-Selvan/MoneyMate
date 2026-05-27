package com.moneymate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymate.app.data.local.entity.EditPermissionScope
import com.moneymate.app.data.local.entity.Payment
import com.moneymate.app.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val repository: PaymentRepository
) : ViewModel() {

    private val _currentPersonId = MutableStateFlow<String?>(null)

    val payments: StateFlow<List<Payment>> = _currentPersonId
        .flatMapLatest { personId ->
            if (personId != null) repository.getPaymentsForPerson(personId)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deletedPayments: StateFlow<List<Payment>> = repository.getDeletedPayments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadPaymentsForPerson(personId: String) {
        _currentPersonId.value = personId
    }

    fun insertPayment(payment: Payment) = viewModelScope.launch {
        repository.insertPayment(payment)
    }

    fun updatePayment(payment: Payment) = viewModelScope.launch {
        repository.updatePayment(payment)
    }

    fun softDeletePayment(id: String) = viewModelScope.launch {
        repository.softDeletePayment(id, System.currentTimeMillis())
    }

    fun restorePayment(id: String) = viewModelScope.launch {
        repository.restorePayment(id)
    }

    fun hardDeletePayment(id: String) = viewModelScope.launch {
        repository.hardDeletePayment(id)
    }

    fun purgeExpiredPayments() = viewModelScope.launch {
        val cutoff = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        repository.purgeExpiredPayments(cutoff)
    }

    fun setEditPermission(id: String, granted: Boolean, scope: EditPermissionScope) = viewModelScope.launch {
        repository.setEditPermission(id, granted, scope)
    }

    fun markAllUploadedForPerson(personId: String) = viewModelScope.launch {
        repository.markAllUploadedForPerson(personId, System.currentTimeMillis())
    }

    private val _currentFileId = MutableStateFlow<String?>(null)

    val filePayments: StateFlow<List<Payment>> = _currentFileId
        .flatMapLatest { fileId ->
            if (fileId != null) repository.getPaymentsForFile(fileId)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Includes completed persons — used for totals so marking complete doesn't drop received
    val filePaymentsWithCompleted: StateFlow<List<Payment>> = _currentFileId
        .flatMapLatest { fileId ->
            if (fileId != null) repository.getPaymentsForFileIncludingCompleted(fileId)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadPaymentsForFile(fileId: String) {
        _currentFileId.value = fileId
    }
}