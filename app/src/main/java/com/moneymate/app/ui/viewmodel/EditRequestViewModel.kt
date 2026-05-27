package com.moneymate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymate.app.data.local.entity.EditPermissionScope
import com.moneymate.app.data.local.entity.EditRequest
import com.moneymate.app.data.local.entity.RequestStatus
import com.moneymate.app.data.repository.EditRequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditRequestViewModel @Inject constructor(
    private val repository: EditRequestRepository
) : ViewModel() {

    val allRequests: StateFlow<List<EditRequest>> = repository.getAllRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingRequests: StateFlow<List<EditRequest>> = repository.getPendingRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertRequest(request: EditRequest) = viewModelScope.launch {
        repository.insertRequest(request)
    }

    fun approveRequest(id: String, scope: EditPermissionScope) = viewModelScope.launch {
        repository.setPermissionScope(id, scope)
        repository.updateRequestStatus(id, RequestStatus.APPROVED, System.currentTimeMillis())
    }

    fun denyRequest(id: String) = viewModelScope.launch {
        repository.updateRequestStatus(id, RequestStatus.DENIED, System.currentTimeMillis())
    }

    fun setFirestoreId(id: String, firestoreId: String) = viewModelScope.launch {
        repository.setFirestoreId(id, firestoreId)
    }

    fun deleteRequest(id: String) = viewModelScope.launch {
        repository.deleteRequest(id)
    }
}