package com.moneymate.app.data.local.dao

import androidx.room.*
import com.moneymate.app.data.local.entity.AppUser
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUserDao {

    @Query("SELECT * FROM app_users WHERE isDeleted = 0 AND isActive = 1 ORDER BY displayName ASC")
    fun getAllActiveUsers(): Flow<List<AppUser>>

    @Query("SELECT * FROM app_users WHERE (isDeleted = 1 OR isActive = 0) ORDER BY displayName ASC")
    fun getAllInactiveUsers(): Flow<List<AppUser>>

    @Query("SELECT * FROM app_users WHERE isDeleted = 0 ORDER BY displayName ASC")
    fun getAllUsers(): Flow<List<AppUser>>

    @Query("SELECT * FROM app_users WHERE isDeleted = 0 AND isActive = 1 ORDER BY displayName ASC")
    suspend fun getAllActiveUsersOnce(): List<AppUser>

    @Query("SELECT * FROM app_users WHERE email = :email AND isDeleted = 0 LIMIT 1")
    suspend fun getUserByEmail(email: String): AppUser?

    @Query("SELECT * FROM app_users WHERE id = :id")
    suspend fun getUserById(id: Long): AppUser?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: AppUser): Long

    @Update
    suspend fun update(user: AppUser)

    @Query("UPDATE app_users SET isDeleted = 1, isActive = 0 WHERE id = :id")
    suspend fun softDelete(id: Long)

    @Query("UPDATE app_users SET isActive = 1, isDeleted = 0 WHERE id = :id")
    suspend fun restore(id: Long)

    @Query("UPDATE app_users SET lastLoginAt = :timestamp WHERE id = :id")
    suspend fun updateLastLogin(id: Long, timestamp: Long)

    @Query("SELECT COUNT(*) FROM app_users WHERE isDeleted = 0 AND isActive = 1")
    suspend fun getActiveUserCount(): Int

    @Query("SELECT * FROM app_users WHERE isDeleted = 0 AND isActive = 1 AND (assignedFileIds = '' OR assignedFileIds LIKE '%' || :fileId || '%')")
    fun getUsersForFile(fileId: String): Flow<List<AppUser>>
}
