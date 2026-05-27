package com.moneymate.app.ui.screens

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.moneymate.app.data.local.entity.LoanFile
import com.moneymate.app.navigation.Screen
import com.moneymate.app.ui.viewmodel.AuthViewModel
import com.moneymate.app.ui.viewmodel.LoanFileViewModel
import com.moneymate.app.ui.viewmodel.SettingsViewModel
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: LoanFileViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel,
    authViewModel: AuthViewModel  // no default — must be passed from NavGraph
) {
    val files by viewModel.allFiles.collectAsState()
    val autoDeleteDays by settingsViewModel.autoDeleteDays.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newFileName by remember { mutableStateOf("") }
    var fileToDelete by remember { mutableStateOf<LoanFile?>(null) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showMultiDeleteDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val isSelecting = selectedIds.isNotEmpty()

    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            val mutableFiles = files.toMutableList()
            mutableFiles.add(to.index, mutableFiles.removeAt(from.index))
            mutableFiles.forEachIndexed { index, file ->
                viewModel.updateSortOrder(file.id, index)
            }
        }
    )

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
                        IconButton(onClick = { showMultiDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete Selected",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("MoneyMate", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout")
                        }
                        IconButton(onClick = { navController.navigate(Screen.Trash.route) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Trash")
                        }
                        IconButton(onClick = { navController.navigate(Screen.Settings.route) }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!isSelecting) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add File")
                }
            }
        }
    ) { padding ->
        if (files.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No files yet. Tap + to create one!",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                state = reorderState.listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .reorderable(reorderState),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(files, key = { it.id }) { file ->
                    val isDragging = reorderState.draggingItemKey == file.id
                    val elevation by animateDpAsState(if (isDragging) 8.dp else 2.dp)
                    val isSelected = selectedIds.contains(file.id)
                    FileCard(
                        file = file,
                        elevation = elevation,
                        isSelected = isSelected,
                        isSelecting = isSelecting,
                        reorderState = reorderState,
                        onClick = {
                            if (isSelecting) {
                                selectedIds = if (isSelected)
                                    selectedIds - file.id
                                else
                                    selectedIds + file.id
                            } else {
                                navController.navigate(Screen.FileDetail.createRoute(file.id))
                            }
                        },
                        onLongClick = { selectedIds = selectedIds + file.id },
                        onDelete = { fileToDelete = file }
                    )
                }
            }
        }
    }

    // Logout Confirmation Dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    authViewModel.logout()
                }) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add File Dialog — 4 predefined NLR + custom
    if (showAddDialog) {
        var customName by remember { mutableStateOf("") }
        var showCustomField by remember { mutableStateOf(false) }
        val nlrOptions = listOf(
            "NLR 1" to "Friday Morning",
            "NLR 2" to "Friday Evening",
            "NLR 3" to "Saturday Morning",
            "NLR 4" to "Saturday Evening"
        )
        AlertDialog(
            onDismissRequest = { showAddDialog = false; customName = ""; showCustomField = false },
            title = { Text("New Loan File") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Pick a preset or create a custom file.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    nlrOptions.forEach { (name, schedule) ->
                        val alreadyExists = files.any { it.name.equals(name, ignoreCase = true) }
                        OutlinedButton(
                            onClick = {
                                viewModel.insertFile(LoanFile(name = name, sortOrder = files.size))
                                showAddDialog = false
                            },
                            enabled = !alreadyExists,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(name, fontWeight = FontWeight.Bold)
                                Text(
                                    if (alreadyExists) "Already created" else schedule,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (alreadyExists) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                    if (showCustomField) {
                        OutlinedTextField(
                            value = customName,
                            onValueChange = { customName = it },
                            label = { Text("Custom file name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        TextButton(
                            onClick = {
                                if (customName.isNotBlank()) {
                                    viewModel.insertFile(LoanFile(name = customName.trim(), sortOrder = files.size))
                                    customName = ""; showCustomField = false; showAddDialog = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Create Custom File") }
                    } else {
                        OutlinedButton(
                            onClick = { showCustomField = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Custom File…")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddDialog = false; customName = ""; showCustomField = false }) { Text("Cancel") }
            }
        )
    }

    // Single Delete Dialog
    fileToDelete?.let { file ->
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Delete File?") },
            text = {
                Text("\"${file.name}\" will be moved to trash. You can restore it within $autoDeleteDays days.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.softDeleteFile(file.id)
                    fileToDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { fileToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // Multi Delete Dialog
    if (showMultiDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showMultiDeleteDialog = false },
            title = { Text("Delete ${selectedIds.size} files?") },
            text = {
                Text("These files will be moved to trash. You can restore them within $autoDeleteDays days.")
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedIds.forEach { id -> viewModel.softDeleteFile(id) }
                    selectedIds = emptySet()
                    showMultiDeleteDialog = false
                }) { Text("Delete All", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showMultiDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileCard(
    file: LoanFile,
    elevation: Dp,
    isSelected: Boolean,
    isSelecting: Boolean,
    reorderState: ReorderableLazyListState,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(elevation),
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
            } else {
                Icon(
                    Icons.Default.DragHandle,
                    contentDescription = "Drag",
                    modifier = Modifier.size(24.dp).detectReorder(reorderState),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Icon(
                Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    file.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "Created: ${
                        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                            .format(java.util.Date(file.createdAt))
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (!isSelecting) {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}