package com.moneymate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.moneymate.app.data.local.entity.Person
import com.moneymate.app.ui.viewmodel.PaymentViewModel
import com.moneymate.app.ui.viewmodel.PersonViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanHistoryScreen(
    navController: NavHostController,
    fileId: String,
    personName: String,
    personViewModel: PersonViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel()
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    LaunchedEffect(fileId, personName) {
        personViewModel.loadLoanHistory(fileId, personName)
    }

    val loanRecords by personViewModel.loanHistory.collectAsState()
    // Load total paid for all loan records at once (bulk query)
    var totalPaidByPerson by remember { mutableStateOf<Map<String, Double>>(emptyMap()) }
    LaunchedEffect(loanRecords) {
        if (loanRecords.isNotEmpty()) {
            totalPaidByPerson = paymentViewModel.getTotalPaidByPersonIds(
                loanRecords.map { it.id }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Loan History",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                personName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            if (loanRecords.isEmpty()) {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No loan history found.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val reversedRecords = loanRecords.reversed()
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(reversedRecords) { index, record ->
                        val loanNumber = index + 1
                        val totalPaid = totalPaidByPerson[record.id] ?: 0.0
                        val isActive = !record.isCompleted && record.amountGiven > 0.0
                        val effectiveTotal = if (record.totalRepayment > 0) record.totalRepayment else record.amountGiven
                        val balance = (effectiveTotal - totalPaid).coerceAtLeast(0.0)

                        LoanHistoryCard(
                            loanNumber = loanNumber,
                            record = record,
                            totalPaid = totalPaid,
                            balance = balance,
                            isActive = isActive,
                            dateFormat = dateFormat
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun LoanHistoryCard(
    loanNumber: Int,
    record: Person,
    totalPaid: Double,
    balance: Double,
    isActive: Boolean,
    dateFormat: SimpleDateFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header row: Loan number + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Loan $loanNumber",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            if (isActive) "Active" else "Completed",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    leadingIcon = {
                        Icon(
                            if (isActive) Icons.Default.PlayCircle else Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            // Date created
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday, null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Created: ${dateFormat.format(Date(record.dateGiven))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Date completed (if applicable)
            if (record.completedAt != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CheckCircleOutline, null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Completed: ${dateFormat.format(Date(record.completedAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider()

            // Financial details
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Principal
                HistoryDetailRow("Principal", "₹${record.amountGiven}")

                // Interest type and rate
                if (record.interestRate > 0.0 || record.interestAmount > 0.0) {
                    val typeLabel = when (record.interestType) {
                        "PERCENTAGE" -> "Flat Rate"
                        "FIXED_AMOUNT" -> "Fixed Amount"
                        else -> record.interestType
                    }
                    HistoryDetailRow(
                        "Interest ($typeLabel)",
                        if (record.interestType == "FIXED_AMOUNT")
                            "₹${record.interestAmount}"
                        else
                            "${record.interestRate}%  →  ₹${record.interestAmount}"
                    )
                }

                // Total
                HistoryDetailRow(
                    "Total",
                    "₹${if (record.totalRepayment > 0) record.totalRepayment else record.amountGiven}",
                    bold = true
                )

                HorizontalDivider()

                // Received
                HistoryDetailRow(
                    "Received",
                    "₹$totalPaid",
                    valueColor = MaterialTheme.colorScheme.tertiary
                )

                // Balance / Status
                HistoryDetailRow(
                    if (isActive) "Pending" else "Balance",
                    if (balance <= 0.0 && totalPaid > 0) "✓ Settled" else "₹$balance",
                    valueColor = if (balance <= 0.0 && totalPaid > 0)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun HistoryDetailRow(
    label: String,
    value: String,
    bold: Boolean = false,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            color = valueColor
        )
    }
}
