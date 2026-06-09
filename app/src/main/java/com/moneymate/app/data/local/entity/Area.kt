package com.moneymate.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a known place/area value within a specific loan file.
 * Used for dropdown selection in AddEditPersonDialog instead of free-text place.
 */
@Entity(
    tableName = "areas",
    indices = [androidx.room.Index("fileId")]
)
data class Area(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fileId: String,
    val name: String,
    val sortOrder: Int = 0,
    val isDeleted: Boolean = false
)
