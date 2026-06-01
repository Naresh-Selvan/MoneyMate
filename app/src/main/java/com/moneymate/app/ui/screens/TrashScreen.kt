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
import com.moneymate.app.data.local.entity.Payment
import com.moneymate.app.data.local.entity.Person
import com.moneymate.app.ui.viewmodel.LoanFileViewModel
import com.moneymate.app.ui.viewmodel.PaymentViewModel
import com.moneymate.app.ui.viewmodel.PersonViewModel
import com.moneymate.app.ui.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TrashScreen(
    navController: NavHostController,
    loanFileViewModel: LoanFileViewModel = hiltViewModel(),
    personViewModel: PersonViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel
) {
    val dtFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    val trashedFiles              by loanFileViewModel.trashedFiles.collectAsState()
    val deletedCompletedPersons   by personViewModel.deletedCompletedPersons.collectAsState()
    val deletedPayments           by paymentViewModel.deletedPayments.collectAsState()
    val autoDeleteDays            by settingsViewModel.autoDeleteDays.collectAsState()

    var fileToDelete              by remember { mutableStateOf<LoanFile?>(null) }
    var personToDelete            by remember { mutableStateOf<Person?>(null) }
    var paymentToDelete           by remember { mutableStateOf<Payment?>(null) }

    var selectedFileIds           by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showMultiFileDeleteDialog  by remember { mutableStateOf(false) }
    var showMultiFileRestoreDialog by remember { mutableStateOf(false) }
    val isSelecting = selectedFileIds.isNotEmpty()

    val isEmpty = trashedFiles.isEmpty() && deletedCompletedPersons.isEmpty() && deletedPayments.isEmpty()

    Scaffold(
        topBar = {
            if (isSelecting) {
                TopAppBar(
                    title = { Text("${selectedFileIds.size} selected", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { selectedFileIds = emptySet() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    },
                    actions = {
                        val allSelected = selectedFileIds.size == trashedFiles.size
                        IconButton(onClick = {
                            selectedFileIds = if (allSelected) emptySet()
                            else trashedFiles.map { it.id }.toSet()
                        }) {
                            Icon(
                                if (allSelected) Icons.Default.Close else Icons.Default.DoneAll,
                                contentDescription = if (allSelected) "Deselect All" else "Select All",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { showMultiFileRestoreDialog = true }) {
                            Icon(Icons.Default.Restore, contentDescription = "Restore Selected",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { showMultiFileDeleteDialog = true }) {
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
                            IconButton(onClick = {
                                selectedFileIds = trashedFiles.map { it.id }.toSet()
                            }) {
                                Icon(Icons.Default.DoneAll, contentDescription = "Select All")
                            }
                            IconButton(onClick = { loanFileViewModel.purgeExpiredFiles() }) {
                                Icon(Icons.Default.DeleteForever, contentDescription = "Empty Trash")
                            }
                        }
                    }
                )
            }
        }
    ) { padding ->
        if (isEmpty) {
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
                    Text("Deleted items appear here for 180 days",
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

                // ── Deleted Files ─────────────────────────────────────────────
                if (trashedFiles.isNotEmpty()) {
                    item {
                        SectionHeader("Deleted Files")
                    }
                    items(trashedFiles, key = { it.id }) { file ->
                        val isSelected = selectedFileIds.contains(file.id)
                        TrashedFileCard(
                            file = file,
                            autoDeleteDays = autoDeleteDays,
                            isSelected = isSelected,
                            isSelecting = isSelecting,
                            onLongClick = { selectedFileIds = selectedFileIds + file.id },
                            onClick = {
                                if (isSelecting) {
                                    selectedFileIds = if (isSelected)
                                        selectedFileIds - file.id
                                    else
                                        selectedFileIds + file.id
                                }
                            },
                            onRestore = { loanFileViewModel.restoreFile(file.id) },
                            onDelete = { fileToDelete = file }
                        )
                    }
                }

                // ── Deleted Completed Persons ─────────────────────────────────
                if (deletedCompletedPersons.isNotEmpty()) {
                    item {
                        SectionHeader(
                            "Deleted Completed Persons",
                            topPadding = if (trashedFiles.isNotEmpty()) 12.dp else 0.dp
                        )
                    }
                    items(deletedCompletedPersons, key = { "cp_${it.id}" }) { person ->
                        DeletedCompletedPersonCard(
                            person = person,
                            onRestore = { personViewModel.restorePerson(person.id) },
                            onDelete = { personToDelete = person }
                        )
                    }
                }

                // ── Fix 5: Deleted Payments ──────────────────────────────────
                if (deletedPayments.isNotEmpty()) {
                    item {
                        SectionHeader(
                            "Deleted Payments",
                            topPadding = if (trashedFiles.isNotEmpty() || deletedCompletedPersons.isNotEmpty()) 12.dp else 0.dp
                        )
                    }
                    items(deletedPayments, key = { "pay_${it.id}" }) { payment ->
                        DeletedPaymentCard(
                            payment = payment,
                            dtFormat = dtFormat,
                            onRestore = { paymentViewModel.restorePayment(payment.id) },
                            onDelete = { paymentToDelete = payment }
                        )
                    }
                }
            }
        }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

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
            dismissButton = { TextButton(onClick = { fileToDelete = null }) { Text("Cancel") } }
        )
    }

    personToDelete?.let { person ->
        AlertDialog(
            onDismissRequest = { personToDelete = null },
            title = { Text("Permanently Delete?") },
            text = { Text("\"${person.name}\" will be permanently deleted and cannot be recovered.") },
            confirmButton = {
                TextButton(onClick = {
                    personViewModel.hardDeletePerson(person.id)
                    personToDelete = null
                }) { Text("Delete Forever", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { personToDelete = null }) { Text("Cancel") } }
        )
    }

    paymentToDelete?.let { payment ->
        AlertDialog(
            onDismissRequest = { paymentToDelete = null },
            title = { Text("Permanently Delete Payment?") },
            text = { Text("₹${payment.amount} will be permanently deleted and cannot be recovered.") },
            confirmButton = {
                TextButton(onClick = {
                    paymentViewModel.hardDeletePayment(payment.id)
                    paymentToDelete = null
                }) { Text("Delete Forever", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { paymentToDelete = null }) { Text("Cancel") } }
        )
    }

    if (showMultiFileDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showMultiFileDeleteDialog = false },
            title = { Text("Permanently Delete ${selectedFileIds.size} files?") },
            text = { Text("These files will be permanently deleted and cannot be recovered.") },
            confirmButton = {
                TextButton(onClick = {
                    selectedFileIds.forEach { id -> loanFileViewModel.hardDeleteFile(id) }
                    selectedFileIds = emptySet()
                    showMultiFileDeleteDialog = false
                }) { Text("Delete Forever", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showMultiFileDeleteDialog = false }) { Text("Cancel") } }
        )
    }

    if (showMultiFileRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showMultiFileRestoreDialog = false },
            title = { Text("Restore ${selectedFileIds.size} files?") },
            text = { Text("These files will be restored to your main list.") },
            confirmButton = {
                TextButton(onClick = {
                    selectedFileIds.forEach { id -> loanFileViewModel.restoreFile(id) }
                    selectedFileIds = emptySet()
                    showMultiFileRestoreDialog = false
                }) { Text("Restore All") }
            },
            dismissButton = { TextButton(onClick = { showMultiFileRestoreDialog = false }) { Text("Cancel") } }
        )
    }
}

// ── Section header ─────────────────────────────────────────────────────────────
@Composable
private fun SectionHeader(title: String, topPadding: androidx.compose.ui.unit.Dp = 0.dp) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = topPadding, bottom = 4.dp)
    )
}

// ── TrashedFileCard (existing, unchanged) ──────────────────────────────────────
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
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
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

// ── DeletedCompletedPersonCard (Fix 3) ────────────────────────────────────────
@Composable
fun DeletedCompletedPersonCard(
    person: Person,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    val deletedAt = person.deletedAt ?: 0L
    val daysLeft = 180 - ((System.currentTimeMillis() - deletedAt) / (1000 * 60 * 60 * 24)).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(person.name, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    AssistChip(
                        onClick = {},
                        label = { Text("Completed", style = MaterialTheme.typography.labelSmall) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    )
                }
                if (!person.place.isNullOrBlank()) {
                    Text(person.place, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("₹${person.amountGiven} • ${person.mode.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    if (daysLeft > 0) "Deleted • $daysLeft days left" else "Expires soon",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (daysLeft <= 3) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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

// ── Fix 5: DeletedPaymentCard ─────────────────────────────────────────────────
@Composable
fun DeletedPaymentCard(
    payment: Payment,
    dtFormat: SimpleDateFormat,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    val deletedAt = payment.deletedAt ?: 0L
    val daysLeft = 180 - ((System.currentTimeMillis() - deletedAt) / (1000 * 60 * 60 * 24)).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Receipt, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "₹${payment.amount}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "${payment.mode.name} • ${dtFormat.format(Date(payment.date))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    if (daysLeft > 0) "Deleted • $daysLeft days left" else "Expires soon",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (daysLeft <= 3) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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