package com.moneymate.app.data.local.dao

import androidx.room.*
import com.moneymate.app.data.local.entity.AuditLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getAllLogs(limit: Int = 500): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE fileId = :fileId ORDER BY timestamp DESC LIMIT :limit")
    fun getLogsForFile(fileId: String, limit: Int = 100): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE userId = :userId ORDER BY timestamp DESC")
    fun getLogsForUser(userId: Long): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE targetType = :targetType AND targetId = :targetId ORDER BY timestamp DESC")
    fun getLogsForTarget(targetType: String, targetId: String): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentLogsForUser(userId: Long, limit: Int = 20): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE action IN (:actions) ORDER BY timestamp DESC LIMIT :limit")
    fun getLogsByActions(actions: List<String>, limit: Int = 500): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE timestamp >= :from AND timestamp <= :to ORDER BY timestamp DESC LIMIT :limit")
    fun getLogsByDateRange(from: Long, to: Long, limit: Int = 500): Flow<List<AuditLog>>

    @Insert
    suspend fun insert(log: AuditLog)

    @Query("DELETE FROM audit_logs WHERE timestamp < :timestamp")
    suspend fun pruneOlderThan(timestamp: Long)

    @Query("SELECT COUNT(*) FROM audit_logs WHERE userId = :userId AND action = 'ADD_PAYMENT'")
    suspend fun countPaymentsByUser(userId: Long): Int

    @Query("SELECT COUNT(*) FROM audit_logs WHERE userId = :userId AND action = 'ADD_PERSON'")
    suspend fun countPersonsAddedByUser(userId: Long): Int
}
