package com.moneymate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    navController: NavHostController,
    loanFileViewModel: LoanFileViewModel = hiltViewModel(),
    personViewModel: PersonViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel
) {
    // ── Run Auto Purge on Screen Load ──────────────────────────────────────────
    LaunchedEffect(Unit) {
        loanFileViewModel.autoPurge()
    }

    // ── Simple Date Formatters ──────────────────────────────────────────────────
    val dtFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    // ── State collections ───────────────────────────────────────────────────────
    val trashedFiles            by loanFileViewModel.trashedFiles.collectAsState()
    val allDeletedPersons       by personViewModel.allDeletedPersons.collectAsState()
    val deletedPayments         by paymentViewModel.deletedPayments.collectAsState()

    // ── Name resolution mappings ───────────────────────────────────────────────
    val allFiles                by loanFileViewModel.allFilesIncludingDeleted.collectAsState()
    val allPersons              by personViewModel.allPersonsIncludingDeleted.collectAsState()

    val fileMap = remember(allFiles) { allFiles.associate { it.id to it.name } }
    val personMap = remember(allPersons) { allPersons.associate { it.id to it.name } }

    // ── Deletion confirmation targets ──────────────────────────────────────────
    var fileToDelete            by remember { mutableStateOf<LoanFile?>(null) }
    var personToDelete          by remember { mutableStateOf<Person?>(null) }
    var paymentToDelete         by remember { mutableStateOf<Payment?>(null) }

    val isEmpty = trashedFiles.isEmpty() && allDeletedPersons.isEmpty() && deletedPayments.isEmpty()

    // Color constants
    val orangeColor = remember { Color(0xFFE65100) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recently Deleted", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isEmpty) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nothing in Recently Deleted.",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Files Section
                if (trashedFiles.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Files")
                    }
                    items(trashedFiles, key = { "file_${it.id}" }) { file ->
                        val daysRemaining = calculateDaysRemaining(file.deletedAt, 180)
                        val daysColor = getDaysColor(daysRemaining, orangeColor)

                        TrashedCard(
                            title = file.name,
                            subtitle = "Deleted on ${formatDate(file.deletedAt, dtFormat)}",
                            daysRemaining = daysRemaining,
                            daysColor = daysColor,
                            icon = Icons.Default.Folder,
                            iconTint = MaterialTheme.colorScheme.primary,
                            onRestore = { loanFileViewModel.restoreFile(file.id) },
                            onDeleteNow = { fileToDelete = file }
                        )
                    }
                }

                // 2. Persons Section
                if (allDeletedPersons.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Persons")
                    }
                    items(allDeletedPersons, key = { "person_${it.id}" }) { person ->
                        val daysRemaining = calculateDaysRemaining(person.deletedAt, 180)
                        val daysColor = getDaysColor(daysRemaining, orangeColor)
                        val fileBelongedTo = fileMap[person.fileId] ?: "Unknown File"

                        TrashedCard(
                            title = person.name,
                            subtitle = "Belonged to: $fileBelongedTo\nDeleted on ${formatDate(person.deletedAt, dtFormat)}",
                            daysRemaining = daysRemaining,
                            daysColor = daysColor,
                            icon = Icons.Default.Person,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            onRestore = { personViewModel.restorePerson(person.id) },
                            onDeleteNow = { personToDelete = person }
                        )
                    }
                }

                // 3. Payments Section
                if (deletedPayments.isNotEmpty()) {
                    item {
                        SectionHeader(title = "Payments")
                    }
                    items(deletedPayments, key = { "payment_${it.id}" }) { payment ->
                        val daysRemaining = calculateDaysRemaining(payment.deletedAt, 30)
                        val daysColor = getDaysColor(daysRemaining, orangeColor)
                        val personBelongedTo = personMap[payment.personId] ?: "Unknown Person"

                        TrashedCard(
                            title = "${personBelongedTo} • ₹${payment.amount}",
                            subtitle = "Payment Date: ${formatDate(payment.date, dtFormat)}\nDeleted on ${formatDate(payment.deletedAt, dtFormat)}",
                            daysRemaining = daysRemaining,
                            daysColor = daysColor,
                            icon = Icons.Default.Receipt,
                            iconTint = MaterialTheme.colorScheme.tertiary,
                            onRestore = { paymentViewModel.restorePayment(payment.id) },
                            onDeleteNow = { paymentToDelete = payment }
                        )
                    }
                }
            }
        }
    }

    // ── Confirmation Dialogs ──────────────────────────────────────────────────

    fileToDelete?.let { file ->
        DeleteConfirmationDialog(
            onDismiss = { fileToDelete = null },
            onConfirm = {
                loanFileViewModel.hardDeleteFile(file.id)
                fileToDelete = null
            }
        )
    }

    personToDelete?.let { person ->
        DeleteConfirmationDialog(
            onDismiss = { personToDelete = null },
            onConfirm = {
                personViewModel.hardDeletePerson(person.id)
                personToDelete = null
            }
        )
    }

    paymentToDelete?.let { payment ->
        DeleteConfirmationDialog(
            onDismiss = { paymentToDelete = null },
            onConfirm = {
                paymentViewModel.hardDeletePayment(payment.id)
                paymentToDelete = null
            }
        )
    }
}

// ── UI Components ─────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun TrashedCard(
    title: String,
    subtitle: String,
    daysRemaining: Int,
    daysColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    onRestore: () -> Unit,
    onDeleteNow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$daysRemaining days left",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = daysColor
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Row {
                IconButton(onClick = onRestore) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Restore",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDeleteNow) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = "Delete Now",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Permanently Delete?") },
        text = { Text("This cannot be undone. Permanently delete this item?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ── Calculation Helpers ───────────────────────────────────────────────────────

private fun calculateDaysRemaining(deletedAt: Long?, retentionDays: Int): Int {
    val deletedTime = deletedAt ?: 0L
    val elapsedMillis = System.currentTimeMillis() - deletedTime
    val elapsedDays = (elapsedMillis / (1000 * 60 * 60 * 24)).toInt()
    return (retentionDays - elapsedDays).coerceAtLeast(0)
}

@Composable
private fun getDaysColor(days: Int, orangeColor: Color): Color {
    return when {
        days <= 3 -> MaterialTheme.colorScheme.error
        days <= 7 -> orangeColor
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

private fun formatDate(timestamp: Long?, formatter: SimpleDateFormat): String {
    if (timestamp == null || timestamp == 0L) return "N/A"
    return formatter.format(Date(timestamp))
}