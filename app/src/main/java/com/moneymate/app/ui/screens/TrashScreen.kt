package com.moneymate.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.moneymate.app.data.local.entity.LoanFile
import com.moneymate.app.ui.viewmodel.LoanFileViewModel
import com.moneymate.app.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TrashScreen(
    navController: NavHostController,
    loanFileViewModel: LoanFileViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel
) {
    val trashedFiles by loanFileViewModel.trashedFiles.collectAsState()
    val autoDeleteDays by settingsViewModel.autoDeleteDays.collectAsState()
    var fileToDelete by remember { mutableStateOf<LoanFile?>(null) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showMultiDeleteDialog by remember { mutableStateOf(false) }
    var showMultiRestoreDialog by remember { mutableStateOf(false) }
    val isSelecting = selectedIds.isNotEmpty()

    Scaffold(
        topBar = {
            if (isSelecting) {
                TopAppBar(
                    title = { Text("${selectedIds.size} selected", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { selectedIds = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    },
                    actions = {
                        // Select All / Deselect All toggle
                        val allSelected = selectedIds.size == trashedFiles.size
                        IconButton(onClick = {
                            selectedIds = if (allSelected) emptySet()
                            else trashedFiles.map { it.id }.toSet()
                        }) {
                            Icon(
                                if (allSelected) Icons.Default.Close else Icons.Default.DoneAll,
                                contentDescription = if (allSelected) "Deselect All" else "Select All",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { showMultiRestoreDialog = true }) {
                            Icon(Icons.Default.Restore, contentDescription = "Restore Selected",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { showMultiDeleteDialog = true }) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Delete Selected",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("Trash", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (trashedFiles.isNotEmpty()) {
                            // Select All shortcut when not in multi-select mode
                            IconButton(onClick = {
                                selectedIds = trashedFiles.map { it.id }.toSet()
                            }) {
                                Icon(Icons.Default.DoneAll, contentDescription = "Select All")
                            }
                            IconButton(onClick = {
                                val cutoff = System.currentTimeMillis() - (autoDeleteDays.toLong() * 24 * 60 * 60 * 1000)
                                loanFileViewModel.purgeExpiredFiles()
                            }) {
                                Icon(Icons.Default.DeleteForever, contentDescription = "Empty Trash")
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (trashedFiles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Delete, contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Trash is empty", style = MaterialTheme.typography.titleMedium)
                    Text("Deleted files appear here for $autoDeleteDays days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(trashedFiles, key = { it.id }) { file ->
                    val isSelected = selectedIds.contains(file.id)
                    TrashedFileCard(
                        file = file,
                        autoDeleteDays = autoDeleteDays,
                        isSelected = isSelected,
                        isSelecting = isSelecting,
                        onLongClick = { selectedIds = selectedIds + file.id },
                        onClick = {
                            if (isSelecting) {
                                selectedIds = if (isSelected)
                                    selectedIds - file.id
                                else
                                    selectedIds + file.id
                            }
                        },
                        onRestore = { loanFileViewModel.restoreFile(file.id) },
                        onDelete = { fileToDelete = file }
                    )
                }
            }
        }
    }

    fileToDelete?.let { file ->
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Permanently Delete?") },
            text = { Text("\"${file.name}\" will be permanently deleted and cannot be recovered.") },
            confirmButton = {
                TextButton(onClick = {
                    loanFileViewModel.hardDeleteFile(file.id)
                    fileToDelete = null
                }) { Text("Delete Forever", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (showMultiDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showMultiDeleteDialog = false },
            title = { Text("Permanently Delete ${selectedIds.size} files?") },
            text = { Text("These files will be permanently deleted and cannot be recovered.") },
            confirmButton = {
                TextButton(onClick = {
                    selectedIds.forEach { id -> loanFileViewModel.hardDeleteFile(id) }
                    selectedIds = emptySet()
                    showMultiDeleteDialog = false
                }) { Text("Delete Forever", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showMultiDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showMultiRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showMultiRestoreDialog = false },
            title = { Text("Restore ${selectedIds.size} files?") },
            text = { Text("These files will be restored to your main list.") },
            confirmButton = {
                TextButton(onClick = {
                    selectedIds.forEach { id -> loanFileViewModel.restoreFile(id) }
                    selectedIds = emptySet()
                    showMultiRestoreDialog = false
                }) { Text("Restore All") }
            },
            dismissButton = {
                TextButton(onClick = { showMultiRestoreDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrashedFileCard(
    file: LoanFile,
    autoDeleteDays: Int,
    isSelected: Boolean,
    isSelecting: Boolean,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    val deletedAt = file.deletedAt ?: 0L
    val daysLeft = autoDeleteDays - ((System.currentTimeMillis() - deletedAt) / (1000 * 60 * 60 * 24)).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelecting) {
                Checkbox(checked = isSelected, onCheckedChange = { onClick() })
                Spacer(modifier = Modifier.width(8.dp))
            }
            Icon(Icons.Default.Folder, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(file.name, fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium)
                Text(
                    if (daysLeft > 0) "Deleted • $daysLeft days left" else "Expires soon",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (daysLeft <= 3) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!isSelecting) {
                IconButton(onClick = onRestore) {
                    Icon(Icons.Default.Restore, contentDescription = "Restore",
                        tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteForever, contentDescription = "Delete Forever",
                        tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}