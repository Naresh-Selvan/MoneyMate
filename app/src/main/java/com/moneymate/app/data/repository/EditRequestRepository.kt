package com.moneymate.app.data.repository

import com.moneymate.app.data.local.dao.EditRequestDao
import com.moneymate.app.data.local.entity.EditPermissionScope
import com.moneymate.app.data.local.entity.EditRequest
import com.moneymate.app.data.local.entity.RequestStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EditRequestRepository @Inject constructor(
    private val editRequestDao: EditRequestDao
) {
    fun getAllRequests(): Flow<List<EditRequest>> =
        editRequestDao.getAllRequests()

    fun getPendingRequests(): Flow<List<EditRequest>> =
        editRequestDao.getPendingRequests()

    suspend fun getLatestRequestForRecord(recordId: String, recordType: String): EditRequest? =
        editRequestDao.getLatestRequestForRecord(recordId, recordType)

    suspend fun insertRequest(request: EditRequest) =
        editRequestDao.insertRequest(request)

    suspend fun updateRequestStatus(id: String, status: RequestStatus, resolvedAt: Long) =
        editRequestDao.updateRequestStatus(id, status, resolvedAt)

    suspend fun setFirestoreId(id: String, firestoreId: String) =
        editRequestDao.setFirestoreId(id, firestoreId)

    suspend fun setPermissionScope(id: String, scope: EditPermissionScope) =
        editRequestDao.setPermissionScope(id, scope)

    suspend fun deleteRequest(id: String) =
        editRequestDao.deleteRequest(id)
}