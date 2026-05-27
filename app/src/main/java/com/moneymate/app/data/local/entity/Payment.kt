package com.moneymate.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["personId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("personId")]
)
data class Payment(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val personId: String,
    val amount: Double,
    val mode: PaymentMode = PaymentMode.CASH,
    val date: Long = System.currentTimeMillis(),
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val isRollover: Boolean = false,
    val uploadedAt: Long? = null,
    val editPermissionGranted: Boolean = false,
    val editPermissionScope: EditPermissionScope = EditPermissionScope.NONE
)