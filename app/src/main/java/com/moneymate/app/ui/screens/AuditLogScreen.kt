package com.moneymate.app.ui.screens

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.moneymate.app.data.local.entity.AuditAction
import com.moneymate.app.data.local.entity.AuditLog
import com.moneymate.app.ui.viewmodel.LoanFileViewModel
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Audit log screen with filter bar and CSV export.
 * Visible to ADMIN and BOSS roles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogScreen(
    navController: NavHostController,
    loanFileViewModel: LoanFileViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val files by loanFileViewModel.allFiles.collectAsState()
    val scope = rememberCoroutineScope()

    // Get AuditLogDao via Hilt EntryPoint
    val auditLogDao = remember {
        try {
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                com.moneymate.app.di.RepositoryEntryPoint::class.java
            ).auditLogDao()
        } catch (_: Exception) { null }
    }

    // Filter state
    var allLogs by remember { mutableStateOf<List<AuditLog>>(emptyList()) }
    var selectedUserId by remember { mutableStateOf<Long?>(null) }
    var selectedActionGroup by remember { mutableStateOf<String?>(null) }
    var selectedFileId by remember { mutableStateOf<String?>(null) }
    var expandedLogId by remember { mutableStateOf<Long?>(null) }
    var expandedUserDropdown by remember { mutableStateOf(false) }
    var expandedActionDropdown by remember { mutableStateOf(false) }
    var expandedFileDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }
    val relativeDateFormat = remember { RelativeDateFormat() }

    // Unique users from logs
    val uniqueUsers = remember(allLogs) {
        allLogs.distinctBy { it.userId }.map { it.userId to it.userEmail }
    }

    // Filtered logs
    val filteredLogs = remember(allLogs, selectedUserId, selectedActionGroup, selectedFileId) {
        var filtered = allLogs
        if (selectedUserId != null) filtered = filtered.filter { it.userId == selectedUserId }
        if (selectedActionGroup != null) {
            filtered = filtered.filter { log ->
                when (selectedActionGroup) {
                    "Person" -> log.action in listOf(AuditAction.ADD_PERSON, AuditAction.EDIT_PERSON, AuditAction.DELETE_PERSON, AuditAction.MOVE_PERSON)
                    "Payment" -> log.action in listOf(AuditAction.ADD_PAYMENT, AuditAction.EDIT_PAYMENT, AuditAction.DELETE_PAYMENT)
                    "File" -> log.action in listOf(AuditAction.ADD_FILE, AuditAction.RENAME_FILE, AuditAction.DELETE_FILE)
                    "User" -> log.action in listOf(AuditAction.ADD_USER, AuditAction.EDIT_USER, AuditAction.DEACTIVATE_USER, AuditAction.CHANGE_ROLE)
                    else -> true
                }
            }
        }
        if (selectedFileId != null) filtered = filtered.filter { it.fileId == selectedFileId }
        filtered.sortedByDescending { it.timestamp }
    }

    // Load logs
    LaunchedEffect(Unit) {
        auditLogDao?.getAllLogs(500)?.collect { logs ->
            allLogs = logs
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audit Log", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    // Export CSV
                    IconButton(onClick = {
                        scope.launch {
                            exportAuditLogsAsCsv(context, filteredLogs)
                        }
                    }) {
                        Icon(Icons.Default.Download, "Export CSV")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Filter bar
            Card(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Row 1: User + Action filters
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // User filter
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = uniqueUsers.find { it.first == selectedUserId }?.second ?: "All Users",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("User", style = MaterialTheme.typography.labelSmall) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUserDropdown) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodySmall,
                                singleLine = true
                            )
                            DropdownMenu(expanded = expandedUserDropdown, onDismissRequest = { expandedUserDropdown = false }) {
                                DropdownMenuItem(text = { Text("All Users") }, onClick = { selectedUserId = null; expandedUserDropdown = false })
                                uniqueUsers.forEach { (id, email) ->
                                    DropdownMenuItem(text = { Text(email) }, onClick = { selectedUserId = id; expandedUserDropdown = false })
                                }
                            }
                        }

                        // Action type filter
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = selectedActionGroup ?: "All Actions",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Action", style = MaterialTheme.typography.labelSmall) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedActionDropdown) },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = MaterialTheme.typography.bodySmall,
                                singleLine = true
                            )
                            DropdownMenu(expanded = expandedActionDropdown, onDismissRequest = { expandedActionDropdown = false }) {
                                DropdownMenuItem(text = { Text("All Actions") }, onClick = { selectedActionGroup = null; expandedActionDropdown = false })
                                DropdownMenuItem(text = { Text("Person Actions") }, onClick = { selectedActionGroup = "Person"; expandedActionDropdown = false })
                                DropdownMenuItem(text = { Text("Payment Actions") }, onClick = { selectedActionGroup = "Payment"; expandedActionDropdown = false })
                                DropdownMenuItem(text = { Text("File Actions") }, onClick = { selectedActionGroup = "File"; expandedActionDropdown = false })
                                DropdownMenuItem(text = { Text("User Actions") }, onClick = { selectedActionGroup = "User"; expandedActionDropdown = false })
                            }
                        }
                    }

                    // Row 2: File filter
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = files.find { it.id == selectedFileId }?.name ?: "All Files",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("File", style = MaterialTheme.typography.labelSmall) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFileDropdown) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodySmall,
                            singleLine = true
                        )
                        DropdownMenu(expanded = expandedFileDropdown, onDismissRequest = { expandedFileDropdown = false }) {
                            DropdownMenuItem(text = { Text("All Files") }, onClick = { selectedFileId = null; expandedFileDropdown = false })
                            files.filter { !it.isDeleted }.forEach { file ->
                                DropdownMenuItem(text = { Text(file.name) }, onClick = { selectedFileId = file.id; expandedFileDropdown = false })
                            }
                        }
                    }
                }
            }

            // Log count
            Text(
                "${filteredLogs.size} entries",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )

            // Log entries
            if (filteredLogs.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No audit log entries match filters",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredLogs, key = { it.id }) { log ->
                        val isExpanded = expandedLogId == log.id
                        AuditLogCard(
                            log = log,
                            dateFormat = dateFormat,
                            isExpanded = isExpanded,
                            onClick = { expandedLogId = if (isExpanded) null else log.id }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditLogCard(
    log: AuditLog,
    dateFormat: SimpleDateFormat,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    val actionColor = when (log.action) {
        AuditAction.ADD_PERSON, AuditAction.ADD_PAYMENT, AuditAction.ADD_FILE, AuditAction.ADD_USER,
        AuditAction.LOGIN, AuditAction.EXPORT_REPORT -> MaterialTheme.colorScheme.primary
        AuditAction.EDIT_PERSON, AuditAction.EDIT_PAYMENT, AuditAction.RENAME_FILE, AuditAction.EDIT_USER,
        AuditAction.CHANGE_ROLE -> MaterialTheme.colorScheme.tertiary
        AuditAction.DELETE_PERSON, AuditAction.DELETE_PAYMENT, AuditAction.DELETE_FILE,
        AuditAction.DEACTIVATE_USER, AuditAction.LOGOUT -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val actionIcon = when (log.action) {
        AuditAction.ADD_PERSON, AuditAction.EDIT_PERSON, AuditAction.DELETE_PERSON, AuditAction.MOVE_PERSON -> Icons.Default.Person
        AuditAction.ADD_PAYMENT, AuditAction.EDIT_PAYMENT, AuditAction.DELETE_PAYMENT -> Icons.Default.Payments
        AuditAction.ADD_FILE, AuditAction.RENAME_FILE, AuditAction.DELETE_FILE -> Icons.Default.Folder
        AuditAction.LOGIN, AuditAction.LOGOUT -> Icons.Default.Login
        AuditAction.ADD_USER, AuditAction.EDIT_USER, AuditAction.DEACTIVATE_USER, AuditAction.CHANGE_ROLE -> Icons.Default.Group
        AuditAction.EXPORT_REPORT -> Icons.Default.Download
        AuditAction.ADD_EXPENSE, AuditAction.EDIT_EXPENSE, AuditAction.DELETE_EXPENSE -> Icons.Default.Money
        else -> Icons.Default.Info
    }

    val actionVerb = log.action.name.replace("_", " ").lowercase()
        .replaceFirstChar { it.uppercase() }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 2.dp else 0.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(actionIcon, null, tint = actionColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("${log.userEmail} $actionVerb ${log.targetLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium)
                    Text(
                        getRelativeTime(log.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (log.fileId != null) {
                    Text("File #${log.fileId}", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Expanded details
            if (isExpanded && log.details != null) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                Text("Details:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                val detailPairs = try {
                    val json = org.json.JSONObject(log.details)
                    json.keys().asSequence().map { it to json.getString(it) }.toList()
                } catch (_: Exception) { emptyList() }
                detailPairs.forEach { (key, value) ->
                    Row(Modifier.fillMaxWidth()) {
                        Text("  $key: ", style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold, modifier = Modifier.width(80.dp))
                        Text(value, style = MaterialTheme.typography.labelSmall)
                    }
                }
                Text(
                    dateFormat.format(java.util.Date(log.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private fun getRelativeTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 604_800_000 -> "${diff / 86_400_000}d ago"
        else -> SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(timestamp))
    }
}

private suspend fun exportAuditLogsAsCsv(context: Context, logs: List<AuditLog>) {
    withContext(Dispatchers.IO) {
        val csvContent = buildString {
            appendLine("ID,User Email,Action,Target Type,Target ID,Target Label,Details,Timestamp,File ID")
            logs.forEach { log ->
                val escapedDetails = log.details?.let { "\"${it.replace("\"", "\"\"")}\"" } ?: ""
                appendLine("${log.id},${log.userEmail},${log.action.name},${log.targetType},${log.targetId},${log.targetLabel},$escapedDetails,${log.timestamp},${log.fileId ?: ""}")
            }
        }

        val fileName = "audit_log_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.csv"
        val contentValues = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, fileName)
            put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        try {
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream: OutputStream ->
                    outputStream.write(csvContent.toByteArray(Charsets.UTF_8))
                }
            }
        } catch (_: Exception) {
            // Fallback: try writing to cache dir
            try {
                val file = java.io.File(context.cacheDir, fileName)
                file.writeText(csvContent, Charsets.UTF_8)
            } catch (_: Exception) {}
        }
    }
}

private class RelativeDateFormat {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    fun format(date: Date): String = dateFormat.format(date)
}
