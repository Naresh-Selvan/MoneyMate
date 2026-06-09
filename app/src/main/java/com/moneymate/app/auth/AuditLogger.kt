package com.moneymate.app.auth

import com.moneymate.app.data.local.dao.AuditLogDao
import com.moneymate.app.data.local.entity.AuditAction
import com.moneymate.app.data.local.entity.AuditLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton helper for recording audit log entries.
 * All logging runs on [Dispatchers.IO] and never blocks the UI.
 */
@Singleton
class AuditLogger @Inject constructor(
    private val auditLogDao: AuditLogDao,
    private val sessionManager: SessionManager
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Record an audit log entry.
     *
     * @param action The auditable action performed.
     * @param targetType The type of target entity (e.g. "Person", "Payment", "LoanFile").
     * @param targetId The string ID of the target entity.
     * @param targetLabel A human-readable label for the target (e.g. person name, file name).
     * @param details Optional map of changed fields for EDIT actions.
     * @param fileId Optional file ID for context.
     */
    fun log(
        action: AuditAction,
        targetType: String,
        targetId: String,
        targetLabel: String,
        details: Map<String, String>? = null,
        fileId: String? = null
    ) {
        val user = sessionManager.currentUser.value ?: return
        scope.launch {
            auditLogDao.insert(
                AuditLog(
                    userId = user.id,
                    userEmail = user.email,
                    action = action,
                    targetType = targetType,
                    targetId = targetId,
                    targetLabel = targetLabel,
                    details = details?.let { JSONObject(it).toString() },
                    fileId = fileId,
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    /**
     * Synchronous variant for use inside existing coroutine scopes.
     */
    suspend fun logSync(
        action: AuditAction,
        targetType: String,
        targetId: String,
        targetLabel: String,
        details: Map<String, String>? = null,
        fileId: String? = null
    ) {
        val user = sessionManager.currentUser.value ?: return
        auditLogDao.insert(
            AuditLog(
                userId = user.id,
                userEmail = user.email,
                action = action,
                targetType = targetType,
                targetId = targetId,
                targetLabel = targetLabel,
                details = details?.let { JSONObject(it).toString() },
                fileId = fileId,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    /**
     * Prune audit logs older than the given timestamp.
     * Called once from [MainActivity.onCreate].
     */
    fun pruneOldLogs() {
        scope.launch {
            val cutoff = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000) // 90 days
            auditLogDao.pruneOlderThan(cutoff)
        }
    }
}
