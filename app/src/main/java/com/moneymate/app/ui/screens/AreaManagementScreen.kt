package com.moneymate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.moneymate.app.data.local.entity.Area
import com.moneymate.app.di.RepositoryEntryPoint
import com.moneymate.app.ui.viewmodel.LoanFileViewModel
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AreaManagementScreen(
    navController: NavHostController,
    loanFileViewModel: LoanFileViewModel = hiltViewModel()
) {
    val files by loanFileViewModel.allFiles.collectAsState()
    var selectedFileId by remember { mutableStateOf<String?>(null) }
    var fileDropdownExp by remember { mutableStateOf(false) }

    // We'll hold areas state locally since we don't have a dedicated ViewModel yet
    var areas by remember { mutableStateOf<List<Area>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<Area?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Area?>(null) }
    val scope = rememberCoroutineScope()

    // Get AreaRepository from Hilt's singleton component
    val context = androidx.compose.ui.platform.LocalContext.current
    val areaRepository = remember {
        val app = context.applicationContext
        EntryPointAccessors.fromApplication(app, RepositoryEntryPoint::class.java).areaRepository()
    }

    // Load areas when file selected
    LaunchedEffect(selectedFileId) {
        selectedFileId?.let { fid ->
            isLoading = true
            try {
                areaRepository.getAreasByFile(fid).collect { areaList ->
                    areas = areaList
                    isLoading = false
                }
            } catch (e: Exception) {
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Areas", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedFileId != null) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Area")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // File selector
            item {
                Text("Select a file to manage areas",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = fileDropdownExp,
                    onExpandedChange = { fileDropdownExp = it }
                ) {
                    val selFile = files.find { it.id == selectedFileId }
                    OutlinedTextField(
                        value = selFile?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Choose a file…") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fileDropdownExp) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        singleLine = true
                    )
                    ExposedDropdownMenu(
                        expanded = fileDropdownExp,
                        onDismissRequest = { fileDropdownExp = false }
                    ) {
                        files.filter { !it.isDeleted }.forEach { file ->
                            DropdownMenuItem(
                                text = { Text(file.name) },
                                onClick = {
                                    selectedFileId = file.id
                                    fileDropdownExp = false
                                }
                            )
                        }
                    }
                }
            }

            if (selectedFileId == null) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Select a file above to manage its areas.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else if (isLoading) {
                item { LinearProgressIndicator(modifier = Modifier.fillMaxWidth()) }
            } else {
                // Header
                item {
                    Text("${areas.size} area(s)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (areas.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Place, null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(40.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text("No areas yet. Tap + to add one.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                items(areas, key = { it.id }) { area ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DragHandle,
                                contentDescription = "Reorder",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Icon(
                                Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(area.name, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            IconButton(onClick = {
                                showRenameDialog = area
                            }) {
                                Icon(Icons.Default.Edit, "Rename",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp))
                            }
                            IconButton(onClick = {
                                showDeleteDialog = area
                            }) {
                                Icon(Icons.Default.Delete, "Delete",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Add Area Dialog ───────────────────────────────────────────────────
    if (showAddDialog && selectedFileId != null) {
        var newName by remember { mutableStateOf("") }
        var nameError by remember { mutableStateOf(false) }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Area") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it; nameError = false },
                        label = { Text("Area Name") },
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
                            scope.launch {
                                val existing = areaRepository.getAreaByName(selectedFileId!!, newName.trim())
                                if (existing != null) {
                                    nameError = true
                                } else {
                                    areaRepository.insert(
                                        Area(fileId = selectedFileId!!, name = newName.trim(), sortOrder = areas.size)
                                    )
                                    showAddDialog = false
                                }
                            }
                        }
                    },
                    enabled = newName.isNotBlank()
                ) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }

    // ── Rename Area Dialog ────────────────────────────────────────────────
    showRenameDialog?.let { area ->
        var newName by remember { mutableStateOf(area.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename Area") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            scope.launch {
                                areaRepository.update(area.copy(name = newName.trim()))
                            }
                            showRenameDialog = null
                        }
                    }
                ) { Text("Rename") }
            },
            dismissButton = { TextButton(onClick = { showRenameDialog = null }) { Text("Cancel") } }
        )
    }

    // ── Delete Area Dialog ────────────────────────────────────────────────
    showDeleteDialog?.let { area ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete \"${area.name}\"?") },
            text = { Text("This area will be moved to trash.") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            areaRepository.softDelete(area.id)
                        }
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") } }
        )
    }
}
