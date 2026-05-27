package com.moneymate.app.data.local.dao

import androidx.room.*
import com.moneymate.app.data.local.entity.EditPermissionScope
import com.moneymate.app.data.local.entity.EditRequest
import com.moneymate.app.data.local.entity.RequestStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface EditRequestDao {

    @Query("SELECT * FROM edit_requests ORDER BY requestedAt DESC")
    fun getAllRequests(): Flow<List<EditRequest>>

    @Query("SELECT * FROM edit_requests WHERE status = 'PENDING' ORDER BY requestedAt DESC")
    fun getPendingRequests(): Flow<List<EditRequest>>

    @Query("SELECT * FROM edit_requests WHERE recordId = :recordId AND recordType = :recordType ORDER BY requestedAt DESC LIMIT 1")
    suspend fun getLatestRequestForRecord(recordId: String, recordType: String): EditRequest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: EditRequest)

    @Query("UPDATE edit_requests SET status = :status, resolvedAt = :resolvedAt WHERE id = :id")
    suspend fun updateRequestStatus(id: String, status: RequestStatus, resolvedAt: Long)

    @Query("UPDATE edit_requests SET firestoreRequestId = :firestoreId WHERE id = :id")
    suspend fun setFirestoreId(id: String, firestoreId: String)

    @Query("UPDATE edit_requests SET scope = :scope WHERE id = :id")
    suspend fun setPermissionScope(id: String, scope: EditPermissionScope)

    @Query("DELETE FROM edit_requests WHERE id = :id")
    suspend fun deleteRequest(id: String)
}