package com.moneymate.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "edit_requests")
data class EditRequest(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val recordId: String,
    val recordType: RecordType,
    val requestedAt: Long = System.currentTimeMillis(),
    val status: RequestStatus = RequestStatus.PENDING,
    val resolvedAt: Long? = null,
    val scope: EditPermissionScope = EditPermissionScope.NONE,
    val firestoreRequestId: String? = null
)

enum class RecordType {
    PERSON, PAYMENT
}

enum class RequestStatus {
    PENDING,
    APPROVED,
    DENIED
}