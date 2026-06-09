package com.moneymate.app.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.moneymate.app.data.local.dao.*
import com.moneymate.app.data.export.ReportExportData
import com.moneymate.app.ui.viewmodel.CombinedSummary
import com.moneymate.app.ui.viewmodel.ExportFormat
import com.moneymate.app.ui.viewmodel.ExportState
import com.moneymate.app.ui.viewmodel.ExportViewModel
import com.moneymate.app.ui.viewmodel.ReportViewModel
import com.moneymate.app.ui.viewmodel.ReportDataState
import com.moneymate.app.ui.viewmodel.ShareTarget
import com.moneymate.app.ui.viewmodel.planReportToExportData
import kotlinx.coroutines.launch

// ── Report 1: Plan ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanReportScreen(
    navController: NavController,
    reportViewModel: ReportViewModel = hiltViewModel(),
    exportViewModel: ExportViewModel = hiltViewModel()
) {
    val state by reportViewModel.planReport.collectAsStateWithLifecycle()
    val fileLabel = reportViewModel.selectedFileId.value?.let { "File $it" } ?: "All Files"
    val dateRange = "${formatReportDate(reportViewModel.fromDate.value)} - ${formatReportDate(reportViewModel.toDate.value)}"

    ReportScaffold(
        title = "Plan",
        navController = navController,
        exportViewModel = exportViewModel,
        onExportData = { data: List<PlanEntry> ->
            planReportToExportData(data, fileLabel, dateRange)
        },
        filterBar = { ReportFilterBar(
            files = emptyList(),
            selectedFileId = reportViewModel.selectedFileId.value,
            fromDate = reportViewModel.fromDate.value,
            toDate = reportViewModel.toDate.value,
            showFileFilter = false,
            onFromDateChanged = { reportViewModel.setFromDate(it) },
            onToDateChanged = { reportViewModel.setToDate(it) }
        )},
        state = state,
        content = { entries ->
            var selectedTab by remember { mutableIntStateOf(0) }
            val tabs = listOf("By Date", "By Line", "All")
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { i, t ->
                    Tab(selected = selectedTab == i, onClick = { selectedTab = i }) { Text(t, modifier = Modifier.padding(8.dp)) }
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(entries) { entry ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(entry.personName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Install: ${entry.paidCount}/${entry.totalInstallments}", style = MaterialTheme.typography.bodySmall)
                                Text("Collected: ${formatCurrency(entry.collectedToday)}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Balance: ${formatCurrency(entry.balance)}", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                                Text("Place: ${entry.place ?: "-"}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                item {
                    val totalCollected = entries.sumOf { it.collectedToday }
                    val totalBalance = entries.sumOf { it.balance }
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total Collected: ${formatCurrency(totalCollected)}", fontWeight = FontWeight.Bold)
                            Text("Total Balance: ${formatCurrency(totalBalance)}", fontWeight = FontWeight.Bold, color = Color.Red)
                        }
                    }
                }
            }
        }
    )
}

// ── Report 2: Daily Summary ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySummaryReportScreen(
    navController: NavController,
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val state by reportViewModel.dailySummary.collectAsStateWithLifecycle()

    ReportScaffold(
        title = "Daily Summary",
        navController = navController,
        filterBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                DateField(
                    value = reportViewModel.dailyReportDate.value,
                    onValueChange = { reportViewModel.setDailyReportDate(it) },
                    label = "Date",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        state = state,
        content = { entries ->
            var tab by remember { mutableIntStateOf(0) }
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }) { Text("Collected", modifier = Modifier.padding(8.dp)) }
                Tab(selected = tab == 1, onClick = { tab = 1 }) { Text("Paid", modifier = Modifier.padding(8.dp)) }
            }
            val filtered = entries.filter { e ->
                if (tab == 0) e.paidAmount > 0 else e.paidAmount < 0
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filtered) { entry ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(entry.personName, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                                Text("Place: ${entry.place ?: "-"}", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(formatCurrency(entry.paidAmount), fontWeight = FontWeight.Bold)
                                Text("Mode: ${entry.paymentMode}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                item {
                    val cashTotal = entries.filter { it.paymentMode == "CASH" }.sumOf { it.paidAmount }
                    val onlineTotal = entries.filter { it.paymentMode == "UPI" }.sumOf { it.paidAmount }
                    val grandTotal = entries.sumOf { it.paidAmount }
                    SummaryFooter(cashTotal = cashTotal, onlineTotal = onlineTotal, grandTotal = grandTotal)
                }
            }
        }
    )
}

// ── Report 3: Line Summary ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineSummaryReportScreen(
    navController: NavController,
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val state by reportViewModel.lineSummary.collectAsStateWithLifecycle()

    ReportScaffold(
        title = "Line Summary",
        navController = navController,
        filterBar = { ReportFilterBar(
            files = emptyList(), selectedFileId = reportViewModel.selectedFileId.value,
            fromDate = reportViewModel.fromDate.value, toDate = reportViewModel.toDate.value,
            showFileFilter = false,
            onFromDateChanged = { reportViewModel.setFromDate(it) },
            onToDateChanged = { reportViewModel.setToDate(it) }
        )},
        state = state,
        content = { entries ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Date", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                        Text("Coll.", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("Online", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("Cash", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("Net", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    }
                }
                items(entries) { entry ->
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(formatReportDate(entry.date), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.5f))
                        Text(formatCurrency(entry.totalCollected), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text(formatCurrency(entry.totalOnline), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text(formatCurrency(entry.totalCash), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                        Text(formatCurrency(entry.netBalance), style = MaterialTheme.typography.bodySmall, color = if (entry.netBalance >= 0) Color(0xFF2E7D32) else Color.Red, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider()
                }
                item {
                    SummaryFooter(
                        cashTotal = entries.sumOf { it.totalCash },
                        onlineTotal = entries.sumOf { it.totalOnline },
                        grandTotal = entries.sumOf { it.totalCollected }
                    )
                }
            }
        }
    )
}

// ── Report 4: Online Collections ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnlineCollectionsReportScreen(
    navController: NavController,
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val state by reportViewModel.onlineCollections.collectAsStateWithLifecycle()

    ReportScaffold(
        title = "Online Collections",
        navController = navController,
        filterBar = { ReportFilterBar(
            files = emptyList(), selectedFileId = reportViewModel.selectedFileId.value,
            fromDate = reportViewModel.fromDate.value, toDate = reportViewModel.toDate.value,
            showFileFilter = false,
            onFromDateChanged = { reportViewModel.setFromDate(it) },
            onToDateChanged = { reportViewModel.setToDate(it) }
        )},
        state = state,
        content = { entries ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(entries) { entry ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(entry.personName, fontWeight = FontWeight.Medium)
                                Text(formatReportDate(entry.date), style = MaterialTheme.typography.bodySmall)
                            }
                            Text(formatCurrency(entry.amount), fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }
                }
                item { SummaryFooter(grandTotal = entries.sumOf { it.amount }) }
            }
        }
    )
}

// ── Report 5: Site Dashboard ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteDashboardReportScreen(
    navController: NavController,
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val state by reportViewModel.siteDashboard.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Site Dashboard") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ReportFilterBar(
                files = emptyList(), selectedFileId = reportViewModel.selectedFileId.value,
                fromDate = reportViewModel.fromDate.value, toDate = reportViewModel.toDate.value,
                showFileFilter = false, showDateFilter = true,
                onFromDateChanged = { reportViewModel.setFromDate(it) },
                onToDateChanged = { reportViewModel.setToDate(it) },
                onDownloadClick = { /* Phase 5 */ }
            )
            val currentSiteState = state
            when (currentSiteState) {
                is ReportDataState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is ReportDataState.Empty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No data for selected range.") }
                is ReportDataState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${currentSiteState.message}", color = Color.Red)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { /* Retry */ }) { Text("Retry") }
                    }
                }
                is ReportDataState.Success -> {
                    val dash = currentSiteState.data
                    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = { /* Phase 5 */ }) { Icon(Icons.Default.Download, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Excel") }
                            OutlinedButton(onClick = { /* Phase 5 */ }) { Icon(Icons.Default.Download, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("PDF") }
                            OutlinedButton(onClick = { /* Phase 5 */ }) { Icon(Icons.Default.Compare, null, Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Comparison") }
                        }
                        Spacer(Modifier.height(16.dp))
                        DashboardCard("Active Loans", dash.totalActiveLoans.toString())
                        DashboardCard("Outstanding Balance", formatCurrency(dash.totalOutstanding))
                        DashboardCard("Collected (Month)", formatCurrency(dash.totalCollectedThisMonth))
                        DashboardCard("New Loans (Month)", formatCurrency(dash.totalNewLoansThisMonth))
                        DashboardCard("Expenses (Month)", formatCurrency(dash.totalExpensesThisMonth))
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// ── Report 6: Expense Summary ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseSummaryReportScreen(
    navController: NavController,
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val state by reportViewModel.expenseSummaryReport.collectAsStateWithLifecycle()

    ReportScaffold(
        title = "Expense Summary",
        navController = navController,
        filterBar = { ReportFilterBar(
            files = emptyList(), selectedFileId = reportViewModel.selectedFileId.value,
            fromDate = reportViewModel.fromDate.value, toDate = reportViewModel.toDate.value,
            showFileFilter = false,
            onFromDateChanged = { reportViewModel.setFromDate(it) },
            onToDateChanged = { reportViewModel.setToDate(it) }
        )},
        state = state,
        content = { entries ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Category", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                        Text("Cash", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("Online", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("Total", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    }
                }
                items(entries) { entry ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(entry.category, modifier = Modifier.weight(1.5f))
                        Text(formatCurrency(entry.cashTotal), modifier = Modifier.weight(1f))
                        Text(formatCurrency(entry.onlineTotal), modifier = Modifier.weight(1f))
                        Text(formatCurrency(entry.grandTotal), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider()
                }
                item {
                    SummaryFooter(
                        cashTotal = entries.sumOf { it.cashTotal },
                        onlineTotal = entries.sumOf { it.onlineTotal },
                        grandTotal = entries.sumOf { it.grandTotal }
                    )
                }
            }
        }
    )
}

// ── Report 7: Investment Summary ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentSummaryReportScreen(
    navController: NavController,
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val state by reportViewModel.investmentSummaryReport.collectAsStateWithLifecycle()

    ReportScaffold(
        title = "Investment Summary",
        navController = navController,
        filterBar = { ReportFilterBar(
            files = emptyList(), selectedFileId = reportViewModel.selectedFileId.value,
            fromDate = reportViewModel.fromDate.value, toDate = reportViewModel.toDate.value,
            showFileFilter = false,
            onFromDateChanged = { reportViewModel.setFromDate(it) },
            onToDateChanged = { reportViewModel.setToDate(it) }
        )},
        state = state,
        content = { entries ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Type", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                        Text("Cash", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("Online", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("Total", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    }
                }
                items(entries) { entry ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(entry.type, modifier = Modifier.weight(1.5f))
                        Text(formatCurrency(entry.cashTotal), modifier = Modifier.weight(1f))
                        Text(formatCurrency(entry.onlineTotal), modifier = Modifier.weight(1f))
                        Text(formatCurrency(entry.grandTotal), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider()
                }
                item {
                    SummaryFooter(
                        cashTotal = entries.sumOf { it.cashTotal },
                        onlineTotal = entries.sumOf { it.onlineTotal },
                        grandTotal = entries.sumOf { it.grandTotal }
                    )
                }
            }
        }
    )
}

// ── Report 8: Combined Expense/Investment Summary ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CombinedSummaryReportScreen(
    navController: NavController,
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val state by reportViewModel.combinedReport.collectAsStateWithLifecycle()

    ReportScaffold(
        title = "Combined Summary",
        navController = navController,
        filterBar = { ReportFilterBar(
            files = emptyList(), selectedFileId = reportViewModel.selectedFileId.value,
            fromDate = reportViewModel.fromDate.value, toDate = reportViewModel.toDate.value,
            showFileFilter = false,
            onFromDateChanged = { reportViewModel.setFromDate(it) },
            onToDateChanged = { reportViewModel.setToDate(it) }
        )},
        state = state,
        content = { data ->
            val combined = data as CombinedSummary
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Text("Expenses", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(12.dp))
                }
                items(combined.expenses) { entry ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(entry.category, modifier = Modifier.weight(1f))
                        Text(formatCurrency(entry.grandTotal), fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    HorizontalDivider()
                    Text("Subtotal: ${formatCurrency(combined.totalExpenses)}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp), color = Color.Red)
                }
                item {
                    Text("Investments", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(12.dp))
                }
                items(combined.investments) { entry ->
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(entry.type, modifier = Modifier.weight(1f))
                        Text(formatCurrency(entry.grandTotal), fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    HorizontalDivider()
                    Text("Subtotal: ${formatCurrency(combined.totalInvestments)}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp), color = Color(0xFF2E7D32))
                }
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Text(
                            "Net Position: ${formatCurrency(combined.netPosition)}",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp),
                            color = if (combined.netPosition >= 0) Color(0xFF2E7D32) else Color.Red
                        )
                    }
                }
            }
        }
    )
}

// ── Report 9: Book Excess Loss ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookExcessLossReportScreen(
    navController: NavController,
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val state by reportViewModel.bookExcessLoss.collectAsStateWithLifecycle()

    ReportScaffold(
        title = "Book Excess Loss",
        navController = navController,
        filterBar = { ReportFilterBar(
            files = emptyList(), selectedFileId = reportViewModel.selectedFileId.value,
            fromDate = reportViewModel.fromDate.value, toDate = reportViewModel.toDate.value,
            showFileFilter = false, showDateFilter = false,
            onFromDateChanged = {}, onToDateChanged = {}
        )},
        state = state,
        content = { entries ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(entries) { entry ->
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(entry.personName, fontWeight = FontWeight.Bold)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Loan: ${formatCurrency(entry.loanAmount)}", style = MaterialTheme.typography.bodySmall)
                                Text("Paid: ${formatCurrency(entry.totalPaid)}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text("Excess: ${formatCurrency(entry.excessAmount)}", color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item { SummaryFooter(grandTotal = entries.sumOf { it.excessAmount }) }
            }
        }
    )
}

// ── Report 10: Loan Summary ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanSummaryReportScreen(
    navController: NavController,
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val state by reportViewModel.loanSummary.collectAsStateWithLifecycle()

    ReportScaffold(
        title = "Loan Summary",
        navController = navController,
        filterBar = {
            Column {
                ReportFilterBar(
                    files = emptyList(), selectedFileId = reportViewModel.selectedFileId.value,
                    fromDate = reportViewModel.fromDate.value, toDate = reportViewModel.toDate.value,
                    showFileFilter = false,
                    onFromDateChanged = { reportViewModel.setFromDate(it) },
                    onToDateChanged = { reportViewModel.setToDate(it) }
                )
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = reportViewModel.loanSummarySearchByDate, onCheckedChange = { reportViewModel.loanSummarySearchByDate = it })
                    Text("Search By Date", style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        state = state,
        content = { entries ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(entries) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
                            .clickable { navController.navigate("person_detail/${entry.personId}") }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(entry.name, fontWeight = FontWeight.Bold)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Loan: ${formatCurrency(entry.loanAmount)}", style = MaterialTheme.typography.bodySmall)
                                Text("Interest: ${entry.interest}%", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Install: ${entry.paidCount}/${entry.totalInstallments} @ ${formatCurrency(entry.installAmount)}", style = MaterialTheme.typography.bodySmall)
                                Text("Balance: ${formatCurrency(entry.balance)}", color = Color.Red, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Start: ${formatReportDate(entry.startDate)}", style = MaterialTheme.typography.bodySmall)
                                Text("Status: ${entry.status}", color = if (entry.status == "Active") Color(0xFF1565C0) else Color(0xFF2E7D32), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    )
}

// ── Shared helpers ──

@Composable
fun SummaryFooter(cashTotal: Double = 0.0, onlineTotal: Double = 0.0, grandTotal: Double = 0.0) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            if (cashTotal != 0.0 || onlineTotal != 0.0) {
                Text("Cash Total: ${formatCurrency(cashTotal)}", style = MaterialTheme.typography.bodySmall)
                Text("Online Total: ${formatCurrency(onlineTotal)}", style = MaterialTheme.typography.bodySmall)
            }
            Text("Grand Total: ${formatCurrency(grandTotal)}", fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ReportScaffold(
    title: String,
    navController: NavController,
    filterBar: @Composable () -> Unit = {},
    state: ReportDataState<T>,
    content: @Composable (T) -> Unit,
    exportViewModel: ExportViewModel? = null,
    onExportData: (suspend (T) -> ReportExportData?)? = null
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Observe export state for snackbar
    val exportState by exportViewModel?.exportState?.collectAsState() ?: remember { mutableStateOf(ExportState.Idle) }

    // Track the format being exported for the share action
    var pendingShareFormat by remember { mutableStateOf<ExportFormat?>(null) }

    LaunchedEffect(exportState) {
        when (val s = exportState) {
            is ExportState.Done -> {
                snackbarHostState.showSnackbar(
                    "${if (s.format == ExportFormat.PDF) "PDF" else "Excel"} ready"
                )
                // If we were waiting to share via WhatsApp after export, do it now
                if (pendingShareFormat != null) {
                    exportViewModel?.shareExportedFile(ShareTarget.WHATSAPP)
                    pendingShareFormat = null
                }
            }
            is ExportState.Error -> {
                snackbarHostState.showSnackbar(message = "Export: ${s.message}")
                pendingShareFormat = null
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // When export is enabled, show export bar above the regular filter bar
            if (exportViewModel != null && onExportData != null) {
                val currentSuccessData = (state as? ReportDataState.Success)?.data
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (exportState is ExportState.Exporting) {
                        LinearProgressIndicator(modifier = Modifier.width(80.dp).padding(end = 8.dp))
                    } else {
                        IconButton(onClick = {
                            currentSuccessData?.let { data ->
                                scope.launch {
                                    val exportData = onExportData(data)
                                    if (exportData != null) {
                                        exportViewModel.setCurrentExportData(exportData)
                                        exportViewModel.exportCurrentReport(ExportFormat.PDF)
                                    }
                                }
                            }
                        }) {
                            Icon(Icons.Default.PictureAsPdf, "Export PDF", tint = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = {
                            currentSuccessData?.let { data ->
                                scope.launch {
                                    val exportData = onExportData(data)
                                    if (exportData != null) {
                                        exportViewModel.setCurrentExportData(exportData)
                                        exportViewModel.exportCurrentReport(ExportFormat.EXCEL)
                                    }
                                }
                            }
                        }) {
                            Icon(Icons.Default.TableChart, "Export Excel", tint = MaterialTheme.colorScheme.primary)
                        }
                        // Share icon: export PDF then show snackbar with share action
                        IconButton(onClick = {
                            currentSuccessData?.let { data ->
                                scope.launch {
                                    val exportData = onExportData(data)
                                    if (exportData != null) {
                                        exportViewModel.setCurrentExportData(exportData)
                                        exportViewModel.exportCurrentReport(ExportFormat.PDF)
                                    }
                                }
                            }
                        }) {
                            Icon(Icons.Default.Share, "Share")
                        }
                    }
                }
            }

            filterBar()
            val currentState = state
            when (currentState) {
                is ReportDataState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is ReportDataState.Empty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No data for selected range.", style = MaterialTheme.typography.bodyLarge) }
                is ReportDataState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Error: ${currentState.message}", color = Color.Red)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { /* Retry */ }) { Text("Retry") }
                    }
                }
                is ReportDataState.Success -> content(currentState.data)
            }
        }
    }

    // Handle snackbar action (share the exported file)
    LaunchedEffect(snackbarHostState.currentSnackbarData) {
        snackbarHostState.currentSnackbarData?.let { data ->
            if (data.visuals.actionLabel == "Share") {
                val state = exportViewModel?.exportState?.value
                if (state is ExportState.Done) {
                    exportViewModel?.shareExportedFile(ShareTarget.GENERIC)
                }
            }
        }
    }
}
