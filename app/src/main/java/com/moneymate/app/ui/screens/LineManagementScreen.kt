package com.moneymate.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.moneymate.app.data.local.entity.LoanFile
import com.moneymate.app.data.repository.PersonRepository
import com.moneymate.app.di.RepositoryEntryPoint
import com.moneymate.app.navigation.Screen
import com.moneymate.app.ui.viewmodel.LoanFileViewModel
import com.moneymate.app.ui.viewmodel.SettingsViewModel
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineManagementScreen(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    loanFileViewModel: LoanFileViewModel = hiltViewModel()
) {
    val files by loanFileViewModel.allFiles.collectAsState()
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<LoanFile?>(null) }
    var showRenameDialog by remember { mutableStateOf<LoanFile?>(null) }
    var renameText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    // Get PersonRepository from Hilt for active person counts
    val context = androidx.compose.ui.platform.LocalContext.current
    val personRepository = remember {
        EntryPointAccessors.fromApplication(context.applicationContext, RepositoryEntryPoint::class.java).personRepository()
    }

    // Load active person counts per file
    val personCounts = remember { mutableStateMapOf<String, Int>() }
    LaunchedEffect(files) {
        files.forEach { file ->
            launch {
                val count = personRepository.getActiveLoanCount(file.id)
                personCounts[file.id] = count
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lines", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { navController.navigate(Screen.LineMove.route) }) {
                        Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Line Move")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Line")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (files.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No lines yet. Tap + to create one.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(files, key = { it.id }) { file ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                // Swipe left - show rename/delete options via snapshotFlow
                                false // Don't actually dismiss
                            }
                            false
                        }
                    )
                    var showActions by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DragHandle,
                                contentDescription = "Reorder",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(file.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.People,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "${personCounts[file.id] ?: 0} active",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(file.createdAt)),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            // Action buttons
                            Row {
                                IconButton(onClick = {
                                    renameText = file.name
                                    showRenameDialog = file
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Rename",
                                        tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { showDeleteDialog = file }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Add File Dialog ───────────────────────────────────────────────────
    if (showAddDialog) {
        var newName by remember { mutableStateOf("") }
        var nameError by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create New Line") },
            text = {
                Column {
                    Text("Enter a name for the new loan file.", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it; nameError = false },
                        label = { Text("Line Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameError,
                        supportingText = if (nameError) {{ Text("Name is required") }} else null
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isBlank()) {
                            nameError = true
                        } else {
                            loanFileViewModel.insertFile(LoanFile(name = newName.trim(), sortOrder = files.size))
                            showAddDialog = false
                        }
                    },
                    enabled = newName.isNotBlank()
                ) { Text("Create") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }

    // ── Rename Dialog ─────────────────────────────────────────────────────
    showRenameDialog?.let { file ->
        var nameError by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename Line") },
            text = {
                Column {
                    OutlinedTextField(
                        value = renameText,
                        onValueChange = { renameText = it; nameError = false },
                        label = { Text("New Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = nameError,
                        supportingText = if (nameError) {{ Text("Name is required or already in use") }} else null
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (renameText.isBlank()) {
                            nameError = true
                        } else if (files.any { it.name.equals(renameText.trim(), ignoreCase = true) && it.id != file.id }) {
                            nameError = true
                            // reuse nameError to show uniqueness failure
                        } else {
                            loanFileViewModel.updateFile(file.copy(name = renameText.trim()))
                            showRenameDialog = null
                        }
                    }
                ) { Text("Rename") }
            },
            dismissButton = { TextButton(onClick = { showRenameDialog = null }) { Text("Cancel") } }
        )
    }

    // ── Delete Confirmation Dialog ────────────────────────────────────────
    showDeleteDialog?.let { file ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete \"${file.name}\"?") },
            text = {
                Text("All persons and payments in this file will be moved to trash.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        val deletedFileId = file.id
                        val deletedFileName = file.name
                        loanFileViewModel.softDeleteFile(deletedFileId)
                        showDeleteDialog = null
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "\"${deletedFileName}\" moved to trash",
                                actionLabel = "Undo",
                                duration = SnackbarDuration.Long
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                loanFileViewModel.restoreFile(deletedFileId)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") } }
        )
    }
}
