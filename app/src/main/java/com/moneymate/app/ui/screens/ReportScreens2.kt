package com.moneymate.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.moneymate.app.data.local.entity.Person
import com.moneymate.app.ui.viewmodel.ReportViewModel
import com.moneymate.app.ui.viewmodel.ReportDataState
import java.util.Calendar

// ── Report 11: About to Close ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutToCloseReportScreen(
    navController: NavController,
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val state by reportViewModel.aboutToCloseLoans.collectAsStateWithLifecycle()

    ReportScaffold(title = "About to Close", navController = navController,
        state = state,
        content = { persons ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(persons) { p ->
                    PersonReportCard(person = p, onClick = { navController.navigate("person_detail/${p.id}") })
                }
            }
        }
    )
}

// ── Report 12: Missing Customers ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissingCustomersReportScreen(
    navController: NavController,
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val state by reportViewModel.missingCustomers.collectAsStateWithLifecycle()

    ReportScaffold(title = "Missing Customers", navController = navController,
        filterBar = { ReportFilterBar(
            files = emptyList(), selectedFileId = reportViewModel.selectedFileId.value,
            fromDate = reportViewModel.fromDate.value, toDate = reportViewModel.toDate.value,
            showFileFilter = false,
            onFromDateChanged = { reportViewModel.setFromDate(it) },
            onToDateChanged = { reportViewModel.setToDate(it) }
        )},
        state = state,
        content = { persons ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(persons) { p ->
                    PersonReportCard(person = p, onClick = { navController.navigate("person_detail/${p.id}") })
                }
            }
        }
    )
}

// ── Report 13: Monthly Interest Pending ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyInterestReportScreen(
    navController: NavController,
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val state by reportViewModel.monthlyInterestPending.collectAsStateWithLifecycle()
    val monthNames = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")

    ReportScaffold(title = "Monthly Interest Pending", navController = navController,
        filterBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                var monthExpanded by remember { mutableStateOf(false) }
                var yearExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = monthExpanded, onExpandedChange = { monthExpanded = !monthExpanded }) {
                    OutlinedTextField(value = monthNames[reportViewModel.interestMonth.value], onValueChange = {}, readOnly = true, modifier = Modifier.weight(1f), label = { Text("Month") })
                    ExposedDropdownMenu(expanded = monthExpanded, onDismissRequest = { monthExpanded = false }) {
                        monthNames.forEachIndexed { i, name ->
                            DropdownMenuItem(text = { Text(name) }, onClick = { reportViewModel.setInterestMonth(i); monthExpanded = false })
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = yearExpanded, onExpandedChange = { yearExpanded = !yearExpanded }) {
                    OutlinedTextField(value = reportViewModel.interestYear.value.toString(), onValueChange = {}, readOnly = true, modifier = Modifier.weight(1f), label = { Text("Year") })
                    ExposedDropdownMenu(expanded = yearExpanded, onDismissRequest = { yearExpanded = false }) {
                        val cy = Calendar.getInstance().get(Calendar.YEAR)
                        (cy - 2..cy + 1).forEach { y ->
                            DropdownMenuItem(text = { Text(y.toString()) }, onClick = { reportViewModel.setInterestYear(y); yearExpanded = false })
                        }
                    }
                }
            }
        },
        state = state,
        content = { persons ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(persons) { p ->
                    PersonReportCard(person = p, onClick = { navController.navigate("person_detail/${p.id}") })
                }
            }
        }
    )
}

// ── Report 14: Completed Loans ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedLoansReportScreen(
    navController: NavController,
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val state by reportViewModel.completedLoansReport.collectAsStateWithLifecycle()

    ReportScaffold(title = "Completed Loans", navController = navController,
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
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(entry.name, fontWeight = FontWeight.Bold)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Loan: ${formatCurrency(entry.loanAmount)}", style = MaterialTheme.typography.bodySmall)
                                Text("Collected: ${formatCurrency(entry.totalCollected)}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Completed: ${formatReportDate(entry.completionDate)}", style = MaterialTheme.typography.bodySmall)
                                Text("Duration: ${entry.durationDays}d", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
                item {
                    SummaryFooter(
                        grandTotal = entries.sumOf { it.loanAmount },
                        cashTotal = entries.sumOf { it.totalCollected },
                        onlineTotal = entries.size.toDouble()
                    )
                }
            }
        }
    )
}

// ── Report 15: Non Performance Loans ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NonPerformingReportScreen(
    navController: NavController,
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val state by reportViewModel.nonPerformingLoans.collectAsStateWithLifecycle()

    ReportScaffold(title = "Non Performance Loans", navController = navController,
        filterBar = {
            var expanded by remember { mutableStateOf(false) }
            val options = listOf(4, 8, 12)
            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                OutlinedTextField(value = "${reportViewModel.nonPerformingWeeks} weeks", onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth(), label = { Text("Threshold") })
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { w ->
                        DropdownMenuItem(text = { Text("$w weeks") }, onClick = { reportViewModel.nonPerformingWeeks = w; expanded = false })
                    }
                    DropdownMenuItem(text = { Text("Custom...") }, onClick = { expanded = false })
                }
            }
        },
        state = state,
        content = { persons ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(persons) { p ->
                    PersonReportCard(person = p, onClick = { navController.navigate("person_detail/${p.id}") })
                }
            }
        }
    )
}

// ── Report 16: Bad Loans ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadLoansReportScreen(
    navController: NavController,
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val state by reportViewModel.badLoansReport.collectAsStateWithLifecycle()
    val chips = listOf(100, 150, 200)

    ReportScaffold(title = "Bad Loan Summary", navController = navController,
        filterBar = {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                chips.forEach { days ->
                    FilterChip(
                        selected = reportViewModel.badLoanDays == days,
                        onClick = { reportViewModel.badLoanDays = days },
                        label = { Text("$days days") }
                    )
                }
            }
        },
        state = state,
        content = { persons ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(persons) { p ->
                    PersonReportCard(person = p, onClick = { navController.navigate("person_detail/${p.id}") })
                }
            }
        }
    )
}

// ── Report 17: New Bad Loans By Date ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBadLoansReportScreen(
    navController: NavController,
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val state by reportViewModel.newBadLoansReport.collectAsStateWithLifecycle()

    ReportScaffold(title = "New Bad Loans By Date", navController = navController,
        filterBar = { ReportFilterBar(
            files = emptyList(), selectedFileId = reportViewModel.selectedFileId.value,
            fromDate = reportViewModel.fromDate.value, toDate = reportViewModel.toDate.value,
            showFileFilter = false,
            onFromDateChanged = { reportViewModel.setFromDate(it) },
            onToDateChanged = { reportViewModel.setToDate(it) }
        )},
        state = state,
        content = { persons ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(persons) { p ->
                    PersonReportCard(person = p, onClick = { navController.navigate("person_detail/${p.id}") })
                }
            }
        }
    )
}

// ── Report 18: New Customers ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCustomersReportScreen(
    navController: NavController,
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val state by reportViewModel.newCustomersReport.collectAsStateWithLifecycle()

    ReportScaffold(title = "New Customers", navController = navController,
        filterBar = { ReportFilterBar(
            files = emptyList(), selectedFileId = reportViewModel.selectedFileId.value,
            fromDate = reportViewModel.fromDate.value, toDate = reportViewModel.toDate.value,
            showFileFilter = false,
            onFromDateChanged = { reportViewModel.setFromDate(it) },
            onToDateChanged = { reportViewModel.setToDate(it) }
        )},
        state = state,
        content = { persons ->
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(persons) { p ->
                    PersonReportCard(person = p, onClick = { navController.navigate("person_detail/${p.id}") })
                }
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Total customers: ${persons.size}", fontWeight = FontWeight.Bold)
                            Text("Total loans: ${formatCurrency(persons.sumOf { it.amountGiven })}", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    )
}

// ── Report 19: Loan Analysis ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanAnalysisReportScreen(
    navController: NavController,
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    val analysisState by reportViewModel.loanAnalysis.collectAsStateWithLifecycle()
    val notTakenState by reportViewModel.loanNotTaken.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Loan Analysis") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        var tab by remember { mutableIntStateOf(0) }
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }) { Text("Loan Analysis", modifier = Modifier.padding(8.dp)) }
                Tab(selected = tab == 1, onClick = { tab = 1 }) { Text("Loan Not Taken", modifier = Modifier.padding(8.dp)) }
            }
            ReportFilterBar(
                files = emptyList(), selectedFileId = reportViewModel.selectedFileId.value,
                fromDate = reportViewModel.fromDate.value, toDate = reportViewModel.toDate.value,
                showFileFilter = false,
                onFromDateChanged = { reportViewModel.setFromDate(it) },
                onToDateChanged = { reportViewModel.setToDate(it) }
            )
            when (tab) {
                0 -> when (val s = analysisState) {
                    is ReportDataState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is ReportDataState.Empty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No data.") }
                    is ReportDataState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: ${s.message}", color = Color.Red) }
                    is ReportDataState.Success -> {
                        LazyColumn {
                            items(s.data) { entry ->
                                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(formatReportDate(entry.date), fontWeight = FontWeight.Bold)
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Active: ${entry.activeLoans}", style = MaterialTheme.typography.bodySmall)
                                            Text("Completed: ${entry.completedLoans}", style = MaterialTheme.typography.bodySmall)
                                        }
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Disbursed: ${formatCurrency(entry.totalDisbursed)}", style = MaterialTheme.typography.bodySmall)
                                            Text("Collected: ${formatCurrency(entry.totalCollected)}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> when (val s = notTakenState) {
                    is ReportDataState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is ReportDataState.Empty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No data.") }
                    is ReportDataState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: ${s.message}", color = Color.Red) }
                    is ReportDataState.Success -> {
                        LazyColumn {
                            items(s.data) { p ->
                                PersonReportCard(person = p, onClick = { navController.navigate("person_detail/${p.id}") })
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Report 20: Ledger Report ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgerReportScreen(
    navController: NavController,
    personId: String? = null,
    personName: String? = null,
    reportViewModel: ReportViewModel = hiltViewModel()
) {
    LaunchedEffect(personId) { if (personId != null) reportViewModel.loadLedger(personId) }
    val state by reportViewModel.ledger.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ledger${if (personName != null) " - $personName" else ""}") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                is ReportDataState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                is ReportDataState.Empty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No payment entries.") }
                is ReportDataState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Error: ${(state as ReportDataState.Error).message}", color = Color.Red) }
                is ReportDataState.Success -> {
                    val entries = (state as ReportDataState.Success).data
                    var runningBalance = 0.0
                    LazyColumn {
                        item {
                            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Date", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                                Text("Type", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("Amount", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("Mode", fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                                Text("Balance", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            }
                            HorizontalDivider()
                        }
                        items(entries) { entry ->
                            runningBalance += entry.amount
                            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(formatReportDate(entry.date), style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1.2f))
                                Text(entry.type, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                Text(formatCurrency(entry.amount), fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                Text(entry.mode, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(0.8f))
                                Text(formatCurrency(runningBalance), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            }
                            HorizontalDivider()
                        }
                        item {
                            Card(modifier = Modifier.fillMaxWidth().padding(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text("Opening Balance: ${formatCurrency(runningBalance - entries.sumOf { it.amount })}", style = MaterialTheme.typography.bodySmall)
                                    Text("Total Paid: ${formatCurrency(entries.sumOf { it.amount })}", fontWeight = FontWeight.Bold)
                                    Text("Closing Balance: ${formatCurrency(runningBalance)}", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Shared Person Report Card ──
@Composable
fun PersonReportCard(person: Person, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(person.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Loan: ${formatCurrency(person.amountGiven)}", style = MaterialTheme.typography.bodySmall)
                Text("Place: ${person.place ?: "-"}", style = MaterialTheme.typography.bodySmall)
            }
            if (person.mobileNumber != null) {
                Text("Mobile: ${person.mobileNumber}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
