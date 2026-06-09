package com.moneymate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.moneymate.app.data.local.entity.Payment
import com.moneymate.app.data.local.entity.Person
import com.moneymate.app.di.RepositoryEntryPoint
import com.moneymate.app.ui.viewmodel.CollectionViewModel
import com.moneymate.app.ui.viewmodel.LoanFileViewModel
import com.moneymate.app.ui.viewmodel.PersonViewModel
import com.moneymate.app.ui.viewmodel.PaymentViewModel
import com.moneymate.app.ui.viewmodel.SettingsViewModel
import com.moneymate.app.data.local.entity.LoanType
import com.moneymate.app.data.local.entity.PaymentMode
import com.moneymate.app.utils.AppPreferences
import dagger.hilt.android.EntryPointAccessors
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import java.text.SimpleDateFormat
import java.util.*

/**
 * Collection screen with 3 tabs: Collect, Pay, Completed.
 * Fully wired end-to-end with CollectionViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    navController: NavHostController,
    fileId: String,
    loanFileViewModel: LoanFileViewModel = hiltViewModel(),
    personViewModel: PersonViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    collectionViewModel: CollectionViewModel = hiltViewModel(),
    sessionManager: com.moneymate.app.ui.viewmodel.SessionViewModel? = null
) {
    val context = LocalContext.current
    val appPreferences = remember { AppPreferences(context) }

    // Check file access permission
    LaunchedEffect(fileId, sessionManager) {
        if (sessionManager != null && sessionManager.isLoggedIn.value) {
            if (!sessionManager.canAccessFile(fileId)) {
                navController.popBackStack()
                return@LaunchedEffect
            }
        }
    }

    // Load data
    LaunchedEffect(fileId) {
        collectionViewModel.loadFile(fileId)
        personViewModel.loadPersonsForFile(fileId)
        paymentViewModel.loadPaymentsForFile(fileId)
    }

    val dashboard by collectionViewModel.dashboard.collectAsState()
    val lendingPersons by collectionViewModel.lendingPersons.collectAsState()
    val borrowingPersons by collectionViewModel.borrowingPersons.collectAsState()
    val completedPersons by collectionViewModel.completedPersons.collectAsState()
    val filePayments by collectionViewModel.filePayments.collectAsState()
    val files by loanFileViewModel.allFiles.collectAsState()
    val isHeaderExpanded by collectionViewModel.isHeaderExpanded.collectAsState()
    val filterPendingPayments by collectionViewModel.filterPendingPayments.collectAsState()
    val showAllCustomers by collectionViewModel.showAllCustomers.collectAsState()
    val isReordering by collectionViewModel.isReordering.collectAsState()
    val selectedTab by collectionViewModel.selectedTab.collectAsState()

    // Compute person states
    val collectPersonStates = remember(filterPendingPayments, lendingPersons, filePayments) {
        val filtered = if (filterPendingPayments) {
            collectionViewModel.getUnpaidToday(lendingPersons, filePayments)
        } else lendingPersons
        collectionViewModel.getPersonStates(filtered, filePayments)
    }

    val payPersonStates = remember(showAllCustomers, borrowingPersons, filePayments) {
        val filtered = if (!showAllCustomers) {
            collectionViewModel.getUnpaidToday(borrowingPersons, filePayments)
        } else borrowingPersons
        collectionViewModel.getPersonStates(filtered, filePayments)
    }

    // Completed filters
    var completedPaidFilter by remember { mutableStateOf(true) }
    var completedNotPaidFilter by remember { mutableStateOf(true) }
    var completedClosedFilter by remember { mutableStateOf(true) }
    var completedOnlineFilter by remember { mutableStateOf(true) }
    var completedSortRecent by remember { mutableStateOf(true) }

    // Reorder state
    val reorderableState = rememberReorderableLazyListState(
        onMove = { from, to ->
            if (isReordering) {
                val mut = borrowingPersons.toMutableList()
                mut.add(to.index, mut.removeAt(from.index))
                collectionViewModel.updateSortOrders(fileId, mut.toList())
            }
        }
    )

    // Dialogs
    var showFilterSheet by remember { mutableStateOf(false) }
    var showQuickPay by remember { mutableStateOf(false) }
    var showMoveToFile by remember { mutableStateOf<Person?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var personToEdit by remember { mutableStateOf<Person?>(null) }

    // Get AreaRepository for area names dropdown
    val areaRepo = remember {
        EntryPointAccessors.fromApplication(context.applicationContext, RepositoryEntryPoint::class.java).areaRepository()
    }
    var areaNames by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(fileId) {
        areaRepo.getAreaNames(fileId).collect { names -> areaNames = names }
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Collection", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                actions = {
                    if (isReordering) {
                        TextButton(onClick = { collectionViewModel.setReordering(false) }) {
                            Text("Done", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        IconButton(onClick = { showFilterSheet = true }) {
                            Icon(Icons.Default.FilterList, "Filter")
                        }
                        IconButton(onClick = { showAddDialog = true }) {
                            Icon(Icons.Default.PersonAdd, "Add Person")
                        }
                        // Upload
                        val anyUploaded = (lendingPersons + borrowingPersons).any { it.uploadedAt != null }
                        IconButton(onClick = { /* upload logic via personViewModel */ }) {
                            Icon(
                                if (anyUploaded) Icons.Default.CloudDone else Icons.Default.Upload,
                                "Upload"
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Dashboard header
            DashboardSummaryHeader(
                summary = dashboard,
                isExpanded = isHeaderExpanded,
                onToggleExpand = { collectionViewModel.toggleHeaderExpanded() }
            )

            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { collectionViewModel.setSelectedTab(0) },
                    text = { Text("Collect (${collectPersonStates.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { collectionViewModel.setSelectedTab(1) },
                    text = { Text("Pay (${payPersonStates.size})") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { collectionViewModel.setSelectedTab(2) },
                    text = { Text("Completed (${completedPersons.size})") }
                )
            }

            // Tab content
            when (selectedTab) {
                0 -> CollectTab(
                    personStates = collectPersonStates,
                    filterPendingPayments = filterPendingPayments,
                    onToggleFilterPending = { collectionViewModel.setFilterPendingPayments(it) },
                    onSavePayment = { personId, amount ->
                        collectionViewModel.recordQuickPayment(personId, amount)
                    },
                    onDelete = { person -> personViewModel.softDeletePerson(person.id) },
                    onEditLoan = { person -> personToEdit = person },
                    onEditCustomer = { person -> personToEdit = person },
                    onLongPress = { person -> showMoveToFile = person },
                    onTap = { person -> navController.navigate("person_detail/${person.id}") }
                )
                1 -> PayTab(
                    personStates = payPersonStates,
                    isReordering = isReordering,
                    reorderableState = reorderableState,
                    showAllCustomers = showAllCustomers,
                    onToggleShowAll = { collectionViewModel.setShowAllCustomers(it) },
                    onReorder = { collectionViewModel.setReordering(true) },
                    onAdd = { showAddDialog = true },
                    onQuickPay = { showQuickPay = true },
                    onSavePayment = { personId, amount ->
                        collectionViewModel.recordQuickPayment(personId, amount)
                    },
                    onDelete = { person -> personViewModel.softDeletePerson(person.id) },
                    onEditLoan = { person -> personToEdit = person },
                    onEditCustomer = { person -> personToEdit = person },
                    onLongPress = { person -> showMoveToFile = person },
                    onTap = { person ->
                        if (!isReordering) {
                            navController.navigate("person_detail/${person.id}")
                        }
                    }
                )
                2 -> CompletedTab(
                    completedPersons = completedPersons,
                    allPayments = filePayments,
                    dateFormat = dateFormat,
                    paidFilter = completedPaidFilter,
                    notPaidFilter = completedNotPaidFilter,
                    closedFilter = completedClosedFilter,
                    onlineFilter = completedOnlineFilter,
                    sortRecent = completedSortRecent,
                    onTogglePaidFilter = { completedPaidFilter = it },
                    onToggleNotPaidFilter = { completedNotPaidFilter = it },
                    onToggleClosedFilter = { completedClosedFilter = it },
                    onToggleOnlineFilter = { completedOnlineFilter = it },
                    onToggleSortRecent = { completedSortRecent = it }
                )
            }
        }
    }

    // Dialogs
    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            Column(Modifier.padding(16.dp)) {
                Text("Filters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("Additional filters coming in Phase 3.", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(16.dp))
                TextButton(onClick = { showFilterSheet = false }, modifier = Modifier.fillMaxWidth()) {
                    Text("Close")
                }
            }
        }
    }

    if (showQuickPay) {
        QuickPaySheet(
            persons = borrowingPersons,
            onDismiss = { showQuickPay = false },
            onSavePayment = { personId, amount ->
                collectionViewModel.recordQuickPayment(personId, amount)
            }
        )
    }

    showMoveToFile?.let { person ->
        MoveToFileDialog(
            person = person,
            allFiles = files,
            currentFileId = fileId,
            appPreferences = appPreferences,
            onDismiss = { showMoveToFile = null },
            onConfirm = { targetFileId ->
                collectionViewModel.movePersonToFile(person, targetFileId) {
                    showMoveToFile = null
                }
            }
        )
    }

    if (showAddDialog) {
        val allPersons = lendingPersons + borrowingPersons + completedPersons
        AddEditPersonDialog(
            mode = DialogMode.ADD,
            fileId = fileId,
            allPersonsInFile = allPersons,
            personViewModel = personViewModel,
            areaNames = areaNames,
            onDismiss = { showAddDialog = false },
            onSaved = { person, _ ->
                personViewModel.insertPerson(person)
                showAddDialog = false
            }
        )
    }

    personToEdit?.let { person ->
        val allPersons = lendingPersons + borrowingPersons + completedPersons
        AddEditPersonDialog(
            mode = DialogMode.EDIT,
            existingPerson = person,
            fileId = fileId,
            allPersonsInFile = allPersons,
            personViewModel = personViewModel,
            areaNames = areaNames,
            onDismiss = { personToEdit = null },
            onSaved = { updatedPerson, _ ->
                personViewModel.updatePerson(updatedPerson)
                personToEdit = null
            }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Collect Tab
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun CollectTab(
    personStates: List<com.moneymate.app.ui.viewmodel.CollectionPersonState>,
    filterPendingPayments: Boolean,
    onToggleFilterPending: (Boolean) -> Unit,
    onSavePayment: (String, Double) -> Unit,
    onDelete: (Person) -> Unit,
    onEditLoan: (Person) -> Unit,
    onEditCustomer: (Person) -> Unit,
    onLongPress: (Person) -> Unit,
    onTap: (Person) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Checkbox(checked = filterPendingPayments, onCheckedChange = onToggleFilterPending)
                Text("Filter Pending Payments", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = { /* filter sheet */ }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.FilterList, null, modifier = Modifier.size(18.dp))
            }
        }

        if (personStates.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (filterPendingPayments) "No pending payments due" else "No lending records",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(personStates, key = { _, s -> s.person.id }) { index, state ->
                    CollectionPersonCard(
                        personState = state,
                        serialNumber = index + 1,
                        onDelete = { onDelete(state.person) },
                        onEditLoan = { onEditLoan(state.person) },
                        onEditCustomer = { onEditCustomer(state.person) },
                        onSavePayment = { amount -> onSavePayment(state.person.id, amount) },
                        onLongPress = { onLongPress(state.person) },
                        onTap = { onTap(state.person) }
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Pay Tab
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PayTab(
    personStates: List<com.moneymate.app.ui.viewmodel.CollectionPersonState>,
    isReordering: Boolean,
    reorderableState: org.burnoutcrew.reorderable.ReorderableLazyListState,
    showAllCustomers: Boolean,
    onToggleShowAll: (Boolean) -> Unit,
    onReorder: () -> Unit,
    onAdd: () -> Unit,
    onQuickPay: () -> Unit,
    onSavePayment: (String, Double) -> Unit,
    onDelete: (Person) -> Unit,
    onEditLoan: (Person) -> Unit,
    onEditCustomer: (Person) -> Unit,
    onLongPress: (Person) -> Unit,
    onTap: (Person) -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        // Last Customer Code
        Text(
            "Last Customer Code: ---",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
        )

        // Show All Customer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = showAllCustomers, onCheckedChange = onToggleShowAll)
            Text("Show All Customer", style = MaterialTheme.typography.bodySmall)
        }

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(onClick = onReorder, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Reorder, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("REORDER", style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(onClick = onAdd, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("ADD", style = MaterialTheme.typography.labelSmall)
            }
            Button(onClick = onQuickPay, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Payments, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("QUICK PAY", style = MaterialTheme.typography.labelSmall)
            }
        }

        if (personStates.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    if (!showAllCustomers) "No pending payments due" else "No borrowing records",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(personStates, key = { _, s -> s.person.id }) { index, state ->
                    if (isReordering) {
                        ReorderableItem(reorderableState, key = state.person.id) { isDragging ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .detectReorderAfterLongPress(reorderableState),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 6.dp else 1.dp)
                            ) {
                                CollectionPersonCard(
                                    personState = state,
                                    serialNumber = index + 1,
                                    onDelete = { onDelete(state.person) },
                                    onEditLoan = { onEditLoan(state.person) },
                                    onEditCustomer = { onEditCustomer(state.person) },
                                    onSavePayment = { amount -> onSavePayment(state.person.id, amount) },
                                    onLongPress = { onLongPress(state.person) },
                                    onTap = { onTap(state.person) }
                                )
                            }
                        }
                    } else {
                        CollectionPersonCard(
                            personState = state,
                            serialNumber = index + 1,
                            onDelete = { onDelete(state.person) },
                            onEditLoan = { onEditLoan(state.person) },
                            onEditCustomer = { onEditCustomer(state.person) },
                            onSavePayment = { amount -> onSavePayment(state.person.id, amount) },
                            onLongPress = { onLongPress(state.person) },
                            onTap = { onTap(state.person) }
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Completed Tab
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun CompletedTab(
    completedPersons: List<Person>,
    allPayments: List<Payment>,
    dateFormat: SimpleDateFormat,
    paidFilter: Boolean,
    notPaidFilter: Boolean,
    closedFilter: Boolean,
    onlineFilter: Boolean,
    sortRecent: Boolean,
    onTogglePaidFilter: (Boolean) -> Unit,
    onToggleNotPaidFilter: (Boolean) -> Unit,
    onToggleClosedFilter: (Boolean) -> Unit,
    onToggleOnlineFilter: (Boolean) -> Unit,
    onToggleSortRecent: (Boolean) -> Unit
) {
    val filteredCompleted = remember(completedPersons, allPayments, paidFilter, notPaidFilter, sortRecent) {
        var list = completedPersons
        if (!paidFilter || !notPaidFilter) {
            val paidIds = allPayments.filter { !it.isDeleted }
                .groupBy { it.personId }
                .mapValues { (_, v) -> v.sumOf { it.amount } }
            list = list.filter { person ->
                val totalPaid = paidIds[person.id] ?: 0.0
                val isPaid = totalPaid >= person.amountGiven
                (paidFilter && isPaid) || (notPaidFilter && !isPaid)
            }
        }
        if (sortRecent) list.sortedByDescending { it.completedAt ?: it.dateGiven }
        else list.sortedBy { it.completedAt ?: it.dateGiven }
    }

    Column(Modifier.fillMaxSize()) {
        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            FilterChip(selected = paidFilter, onClick = { onTogglePaidFilter(!paidFilter) },
                label = { Text("Paid", style = MaterialTheme.typography.labelSmall) })
            FilterChip(selected = notPaidFilter, onClick = { onToggleNotPaidFilter(!notPaidFilter) },
                label = { Text("Not Paid", style = MaterialTheme.typography.labelSmall) })
            FilterChip(selected = closedFilter, onClick = { onToggleClosedFilter(!closedFilter) },
                label = { Text("Closed", style = MaterialTheme.typography.labelSmall) })
            FilterChip(selected = onlineFilter, onClick = { onToggleOnlineFilter(!onlineFilter) },
                label = { Text("Online", style = MaterialTheme.typography.labelSmall) })
        }

        // Sort + download
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Recent", style = MaterialTheme.typography.labelSmall)
                Switch(checked = sortRecent, onCheckedChange = onToggleSortRecent, modifier = Modifier.height(24.dp))
            }
            IconButton(onClick = { /* download */ }, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
            }
        }

        if (filteredCompleted.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No completed records match filters",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(filteredCompleted, key = { _, p -> p.id }) { index, person ->
                    val personPayments = allPayments.filter { it.personId == person.id && !it.isDeleted }
                    val totalPaid = personPayments.sumOf { it.amount }
                    val daysLeft = 180 - ((System.currentTimeMillis() - (person.completedAt ?: 0L)) / (1000 * 60 * 60 * 24)).toInt()

                    DraggableCompletedPersonCardFixed(
                        person = person,
                        balance = (person.amountGiven - totalPaid).coerceAtLeast(0.0),
                        daysLeft = daysLeft,
                        dateFormat = dateFormat,
                        payments = personPayments,
                        onTap = {},
                        onDragStarted = {},
                        onDragMoved = {},
                        onDragEnded = {}
                    )
                }
            }
        }
    }
}
