package com.moneymate.app.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
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
import kotlin.math.roundToInt

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
    var showLogoutDialog by remember { mutableStateOf(false) }

    // ── Fix 1: Drag-to-dustbin state ─────────────────────────────────────────
    var draggingFile       by remember { mutableStateOf<LoanFile?>(null) }
    var dragOffset         by remember { mutableStateOf(Offset.Zero) }
    var showDustbin        by remember { mutableStateOf(false) }
    var dustbinPosition    by remember { mutableStateOf(Offset.Zero) }
    var isOverDustbin      by remember { mutableStateOf(false) }
    var fileToDelete       by remember { mutableStateOf<LoanFile?>(null) }

    val dustbinScale by animateFloatAsState(
        targetValue = if (showDustbin) (if (isOverDustbin) 1.3f else 1f) else 0f,
        animationSpec = tween(200),
        label = "dustbinScale"
    )

    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to ->
            val mutableFiles = files.toMutableList()
            mutableFiles.add(to.index, mutableFiles.removeAt(from.index))
            mutableFiles.forEachIndexed { index, file ->
                viewModel.updateSortOrder(file.id, index)
            }
        }
    )

    // Outer Box needed so the dustbin overlay can float over the Scaffold
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
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
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add File")
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
                        DraggableFileCard(
                            file = file,
                            elevation = elevation,
                            reorderState = reorderState,
                            onClick = {
                                navController.navigate(Screen.FileDetail.createRoute(file.id))
                            },
                            onDragStarted = {
                                draggingFile = file
                                showDustbin = true
                            },
                            onDragMoved = { offset ->
                                dragOffset = offset
                                val cx = dustbinPosition.x + 38f
                                val cy = dustbinPosition.y + 38f
                                val dx = offset.x - cx
                                val dy = offset.y - cy
                                isOverDustbin = dx * dx + dy * dy < 100f * 100f
                            },
                            onDragEnded = {
                                if (isOverDustbin && draggingFile != null) {
                                    fileToDelete = draggingFile
                                }
                                draggingFile = null
                                dragOffset = Offset.Zero
                                showDustbin = false
                                isOverDustbin = false
                            }
                        )
                    }
                }
            }
        }

        // ── Fix 1: Dustbin overlay — visible only during a long-press drag ────
        if (showDustbin) {
            // Dim background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.15f))
            )
            // Dustbin circle
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .onGloballyPositioned { coords ->
                        dustbinPosition = coords.positionInWindow()
                    }
                    .scale(dustbinScale)
                    .background(
                        color = if (isOverDustbin) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .padding(20.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Drop to delete",
                    tint = if (isOverDustbin) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                "Drop here to delete",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 14.dp)
            )
        }

        // ── Fix 1: Ghost card following the finger while dragging ─────────────
        if (draggingFile != null) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(dragOffset.x.roundToInt() - 80, dragOffset.y.roundToInt() - 40) }
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    draggingFile!!.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }

    // ── Logout Confirmation Dialog ────────────────────────────────────────────
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
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    // ── Add File Dialog — 4 predefined NLR + custom ───────────────────────────
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

    // ── Fix 1: Confirmation dialog after drag-drop onto dustbin ──────────────
    fileToDelete?.let { file ->
        AlertDialog(
            onDismissRequest = { fileToDelete = null },
            title = { Text("Delete \"${file.name}\"?") },
            text = {
                Text("This file will be moved to trash. You can restore it within $autoDeleteDays days.")
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
}

// ── Fix 1: DraggableFileCard — long press enters drag mode, no delete button ──
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraggableFileCard(
    file: LoanFile,
    elevation: Dp,
    reorderState: ReorderableLazyListState,
    onClick: () -> Unit,
    onDragStarted: () -> Unit,
    onDragMoved: (Offset) -> Unit,
    onDragEnded: () -> Unit
) {
    var cardPosition by remember { mutableStateOf(Offset.Zero) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            // Normal tap → open file
            .combinedClickable(onClick = onClick)
            // Long press → drag to dustbin.
            // This is on the card body; the drag-handle icon uses detectReorder
            // (a separate gesture) so the two gestures target different touch areas
            // and cannot conflict.
            .onGloballyPositioned { coords ->
                cardPosition = coords.positionInWindow()
            }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        onDragStarted()
                        onDragMoved(Offset(cardPosition.x + offset.x, cardPosition.y + offset.y))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        onDragMoved(Offset(change.position.x + cardPosition.x, change.position.y + cardPosition.y))
                    },
                    onDragEnd    = { onDragEnded() },
                    onDragCancel = { onDragEnded() }
                )
            },
        elevation = CardDefaults.cardElevation(elevation),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag-handle for reorder — uses detectReorder which is a distinct gesture
            // registered on this icon only, not on the card body. No conflict.
            Icon(
                Icons.Default.DragHandle,
                contentDescription = "Drag to reorder",
                modifier = Modifier.size(24.dp).detectReorder(reorderState),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
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
                Text(
                    "Long press & drag to delete",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
            // ── No delete button — deleted via drag-to-dustbin only ──────────
        }
    }
}