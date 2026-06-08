package com.moneymate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneymate.app.ui.viewmodel.FileInsightsViewModel
import com.moneymate.app.ui.viewmodel.LoanFileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    fileInsightsVM: FileInsightsViewModel = hiltViewModel(),
    loanFileViewModel: LoanFileViewModel = hiltViewModel()
) {
    val allFiles by loanFileViewModel.allFiles.collectAsState()
    val insightsData by fileInsightsVM.insights.collectAsState()
    val isLoading by fileInsightsVM.isLoading.collectAsState()

    var selectedFileId by remember { mutableStateOf<String?>(null) }
    var fileDropdownExp by remember { mutableStateOf(false) }

    // Load insights when a file is selected
    LaunchedEffect(selectedFileId) {
        selectedFileId?.let { fileInsightsVM.loadInsights(it) }
    }

    val selFile = allFiles.find { it.id == selectedFileId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header ───────────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "File Insights",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Select a file to view today's, weekly, and all-time financial data.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── File selector ────────────────────────────────────────────────
            item {
                ExposedDropdownMenuBox(
                    expanded = fileDropdownExp,
                    onExpandedChange = { fileDropdownExp = it }
                ) {
                    OutlinedTextField(
                        value = selFile?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Choose a file…") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fileDropdownExp) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = fileDropdownExp,
                        onDismissRequest = { fileDropdownExp = false }
                    ) {
                        allFiles.filter { !it.isDeleted }.forEach { file ->
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

            // ── Loading indicator ────────────────────────────────────────────
            if (isLoading) {
                item {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }

            // ── No selection state ───────────────────────────────────────────
            if (selectedFileId == null && !isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Analytics,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                            Text(
                                "Select a file above to view insights",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // ── Insights data ────────────────────────────────────────────────
            if (selectedFileId != null && !isLoading) {
                val d = insightsData

                // Today's Data
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Today",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
                            InsightRow("Given", "₹${d.todayGiven}", MaterialTheme.colorScheme.onPrimaryContainer)
                            InsightRow("Received", "₹${d.todayReceived}", MaterialTheme.colorScheme.onPrimaryContainer)
                            InsightRow(
                                "Net",
                                "₹${d.todayNet}",
                                if (d.todayNet >= 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error,
                                bold = true
                            )
                        }
                    }
                }

                // This Week
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "This Week",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f))
                            InsightRow("Given", "₹${d.weekGiven}", MaterialTheme.colorScheme.onSecondaryContainer)
                            InsightRow("Received", "₹${d.weekReceived}", MaterialTheme.colorScheme.onSecondaryContainer)
                            InsightRow(
                                "Net",
                                "₹${d.weekNet}",
                                if (d.weekNet >= 0) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.error,
                                bold = true
                            )
                        }
                    }
                }

                // All-Time Totals
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "All-Time Totals",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.15f))
                            InsightRow("Total Given", "₹${d.allTimeGiven}", MaterialTheme.colorScheme.onTertiaryContainer)
                            InsightRow("Total Received", "₹${d.allTimeReceived}", MaterialTheme.colorScheme.onTertiaryContainer)
                            HorizontalDivider(color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.15f))
                            InsightRow(
                                "Outstanding",
                                "₹${d.outstanding}",
                                if (d.outstanding > 0) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.tertiary,
                                bold = true
                            )
                            InsightRow("Active Loans", "${d.activeLoanCount}", MaterialTheme.colorScheme.onTertiaryContainer)
                            InsightRow("Completed Loans", "${d.completedLoanCount}", MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                    }
                }

                // Bottom spacing
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun InsightRow(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = color.copy(alpha = 0.7f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
            color = color
        )
    }
}
