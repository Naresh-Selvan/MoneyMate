package com.moneymate.app.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.moneymate.app.data.local.entity.Expense
import com.moneymate.app.data.local.entity.ExpenseCategory
import com.moneymate.app.data.local.entity.Investment
import com.moneymate.app.data.local.entity.InvestmentType
import com.moneymate.app.ui.viewmodel.ExpenseViewModel
import com.moneymate.app.ui.viewmodel.InvestmentViewModel
import com.moneymate.app.ui.viewmodel.LoanFileViewModel
import com.moneymate.app.ui.viewmodel.PersonViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    loanFileViewModel: LoanFileViewModel = hiltViewModel(),
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    investmentViewModel: InvestmentViewModel = hiltViewModel()
) {
    val files by loanFileViewModel.allFiles.collectAsState()
    val selectedFileId by expenseViewModel.selectedFileId.collectAsState()
    val fromDate by expenseViewModel.fromDate.collectAsState()
    val toDate by expenseViewModel.toDate.collectAsState()
    val onlineOnly by expenseViewModel.onlineOnly.collectAsState()
    val searchQuery by expenseViewModel.searchQuery.collectAsState()
    val expenses by expenseViewModel.expenses.collectAsState()
    val expenseSummary by expenseViewModel.summary.collectAsState()
    val categories by expenseViewModel.categories.collectAsState()
    val investments by investmentViewModel.investments.collectAsState()
    val investmentSummary by investmentViewModel.summary.collectAsState()
    val types by investmentViewModel.types.collectAsState()

    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    // Auto-select first file if none selected
    LaunchedEffect(files) {
        if (selectedFileId == null && files.isNotEmpty()) {
            expenseViewModel.setSelectedFile(files.first().id)
            investmentViewModel.setSelectedFile(files.first().id)
        }
    }

    // Sync ViewModel filters
    LaunchedEffect(selectedFileId) {
        selectedFileId?.let {
            investmentViewModel.setSelectedFile(it)
        }
    }

    var tabIndex by remember { mutableStateOf(0) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    var showAddExpenseSheet by remember { mutableStateOf(false) }
    var showAddInvestmentSheet by remember { mutableStateOf(false) }
    var showFileSelector by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expenses & Investments", fontWeight = FontWeight.Bold) },
                actions = {
                    Box {
                        IconButton(onClick = { showFileSelector = true }) {
                            Icon(Icons.Default.SwapHoriz, "Select File")
                        }
                        DropdownMenu(
                            expanded = showFileSelector,
                            onDismissRequest = { showFileSelector = false }
                        ) {
                            files.forEach { file ->
                                DropdownMenuItem(
                                    text = { Text(file.name) },
                                    onClick = {
                                        expenseViewModel.setSelectedFile(file.id)
                                        investmentViewModel.setSelectedFile(file.id)
                                        showFileSelector = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (tabIndex == 0) showAddExpenseSheet = true
                    else showAddInvestmentSheet = true
                }
            ) {
                Icon(Icons.Default.Add, "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // ── Filter bar ──
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(Modifier.padding(12.dp)) {
                    // File selector
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("File: ", style = MaterialTheme.typography.labelMedium)
                        Text(
                            files.find { it.id == selectedFileId }?.name ?: "Select file",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.clickable { showFileSelector = true }
                        )
                    }
                    Spacer(Modifier.height(6.dp))

                    // Date range
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { showFromPicker = true }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.CalendarToday, null, Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("FROM: ${dateFmt.format(Date(fromDate))}", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedButton(onClick = { showToPicker = true }, modifier = Modifier.weight(1f)) {
                            Icon(Icons.Default.CalendarToday, null, Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("TO: ${dateFmt.format(Date(toDate))}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Spacer(Modifier.height(6.dp))

                    // Online only + search
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = onlineOnly, onCheckedChange = { expenseViewModel.setOnlineOnly(it); investmentViewModel.setOnlineOnly(it) })
                            Text("Online Only", style = MaterialTheme.typography.labelSmall)
                        }
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { expenseViewModel.setSearchQuery(it); investmentViewModel.setSearchQuery(it) },
                            placeholder = { Text("Search...", style = MaterialTheme.typography.labelSmall) },
                            singleLine = true,
                            modifier = Modifier.weight(1f).height(40.dp),
                            textStyle = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            // ── Summary card ──
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryItem("Online", "₹${"%.0f".format(expenseSummary.onlineTotal)}")
                    SummaryItem("Cash", "₹${"%.0f".format(expenseSummary.cashTotal)}")
                    SummaryItem("Total", "₹${"%.0f".format(expenseSummary.grandTotal)}")
                }
            }

            // ── Segmented tabs ──
            TabRow(selectedTabIndex = tabIndex) {
                Tab(selected = tabIndex == 0, onClick = { tabIndex = 0 }, text = { Text("Expenses (${expenses.size})") })
                Tab(selected = tabIndex == 1, onClick = { tabIndex = 1 }, text = { Text("Investments (${investments.size})") })
            }

            // ── Tab content ──
            when (tabIndex) {
                0 -> ExpenseListTab(
                    expenses = expenses,
                    categories = categories,
                    dateFormat = dateFmt,
                    onDelete = { expenseViewModel.deleteExpense(it) },
                    onEdit = { /* TODO: edit sheet */ }
                )
                1 -> InvestmentListTab(
                    investments = investments,
                    types = types,
                    dateFormat = dateFmt,
                    onDelete = { investmentViewModel.deleteInvestment(it) },
                    onEdit = { /* TODO: edit sheet */ }
                )
            }
        }
    }

    // Date pickers
    if (showFromPicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = fromDate)
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = { TextButton(onClick = { dpState.selectedDateMillis?.let { expenseViewModel.setFromDate(it); investmentViewModel.setFromDate(it) }; showFromPicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showFromPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = dpState) }
    }
    if (showToPicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = toDate)
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = { TextButton(onClick = { dpState.selectedDateMillis?.let { expenseViewModel.setToDate(it); investmentViewModel.setToDate(it) }; showToPicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showToPicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = dpState) }
    }

    // Add/Edit sheets
    if (showAddExpenseSheet && selectedFileId != null) {
        AddEditExpenseSheet(
            fileId = selectedFileId!!,
            categories = categories,
            onDismiss = { showAddExpenseSheet = false },
            onSave = { expense ->
                expenseViewModel.addExpense(expense)
                showAddExpenseSheet = false
            },
            onAddCategory = { name, callback -> expenseViewModel.addCategory(name, callback) }
        )
    }
    if (showAddInvestmentSheet && selectedFileId != null) {
        AddEditInvestmentSheet(
            fileId = selectedFileId!!,
            types = types,
            onDismiss = { showAddInvestmentSheet = false },
            onSave = { investment ->
                investmentViewModel.addInvestment(investment)
                showAddInvestmentSheet = false
            },
            onAddType = { name, callback -> investmentViewModel.addType(name, callback) }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Expense list
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ExpenseListTab(
    expenses: List<Expense>,
    categories: List<ExpenseCategory>,
    dateFormat: SimpleDateFormat,
    onDelete: (Long) -> Unit,
    onEdit: (Expense) -> Unit
) {
    if (expenses.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No expenses found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(expenses, key = { _, e -> e.id }) { _, expense ->
                ExpenseCard(expense = expense, dateFormat = dateFormat, onDelete = { onDelete(expense.id) }, onEdit = { onEdit(expense) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseCard(
    expense: Expense,
    dateFormat: SimpleDateFormat,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AssistChip(onClick = {}, label = { Text(expense.category, style = MaterialTheme.typography.labelSmall) })
                    if (expense.isOnline) {
                        AssistChip(onClick = {}, label = { Text("Online", style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer))
                    } else {
                        AssistChip(onClick = {}, label = { Text("Cash", style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("₹${"%.0f".format(expense.amount)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error)
                Text(dateFormat.format(Date(expense.date)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!expense.notes.isNullOrBlank()) {
                    Text(expense.notes!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = { showMenu = false; onEdit() })
                    DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDelete() })
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// Investment list
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun InvestmentListTab(
    investments: List<Investment>,
    types: List<InvestmentType>,
    dateFormat: SimpleDateFormat,
    onDelete: (Long) -> Unit,
    onEdit: (Investment) -> Unit
) {
    if (investments.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No investments found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(investments, key = { _, i -> i.id }) { _, investment ->
                InvestmentCard(investment = investment, dateFormat = dateFormat, onDelete = { onDelete(investment.id) }, onEdit = { onEdit(investment) })
            }
        }
    }
}

@Composable
private fun InvestmentCard(
    investment: Investment,
    dateFormat: SimpleDateFormat,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AssistChip(onClick = {}, label = { Text(investment.type, style = MaterialTheme.typography.labelSmall) })
                    if (investment.isOnline) {
                        AssistChip(onClick = {}, label = { Text("Online", style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer))
                    } else {
                        AssistChip(onClick = {}, label = { Text("Cash", style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer))
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("₹${"%.0f".format(investment.amount)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary)
                Text(dateFormat.format(Date(investment.date)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!investment.notes.isNullOrBlank()) {
                    Text(investment.notes!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(18.dp))
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, leadingIcon = { Icon(Icons.Default.Edit, null) },
                        onClick = { showMenu = false; onEdit() })
                    DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { showMenu = false; onDelete() })
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// AddEditExpenseSheet
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditExpenseSheet(
    fileId: String,
    categories: List<ExpenseCategory>,
    onDismiss: () -> Unit,
    onSave: (Expense) -> Unit,
    onAddCategory: (String, (Boolean) -> Unit) -> Unit
) {
    var category by remember { mutableStateOf(categories.firstOrNull()?.name ?: "") }
    var amountText by remember { mutableStateOf("") }
    var isOnline by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showNewCategoryInput by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier.fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 32.dp)
                .let { if (!showNewCategoryInput) it else it }
        ) {
            Text("Add Expense", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            // Category dropdown + inline add
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat.name) }, onClick = { category = cat.name; expanded = false })
                        }
                    }
                }
                if (showNewCategoryInput) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        placeholder = { Text("New name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        if (newCategoryName.isNotBlank()) {
                            onAddCategory(newCategoryName) { success ->
                                if (success) {
                                    category = newCategoryName
                                    showNewCategoryInput = false
                                    newCategoryName = ""
                                }
                            }
                        }
                    }) {
                        Icon(Icons.Default.Check, null)
                    }
                } else {
                    IconButton(onClick = { showNewCategoryInput = true }) {
                        Icon(Icons.Default.Add, "Add category")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Amount
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' }; amountError = false },
                label = { Text("Amount *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Text("₹", modifier = Modifier.padding(start = 8.dp)) },
                isError = amountError,
                supportingText = if (amountError) {{ Text("Amount must be > 0") }} else null
            )

            Spacer(Modifier.height(8.dp))

            // Online toggle
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Online Payment", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = isOnline, onCheckedChange = { isOnline = it })
            }

            Spacer(Modifier.height(8.dp))

            // Date
            val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.CalendarToday, null, Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Date: ${dateFmt.format(Date(date))}")
            }

            Spacer(Modifier.height(8.dp))

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Comments/Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            Spacer(Modifier.height(12.dp))

            // Save
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount <= 0.0 || category.isBlank()) {
                        amountError = amount <= 0.0
                        return@Button
                    }
                    onSave(Expense(
                        fileId = fileId,
                        category = category,
                        amount = amount,
                        isOnline = isOnline,
                        date = date,
                        notes = notes.ifBlank { null }
                    ))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Expense")
            }
        }
    }

    if (showDatePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { dpState.selectedDateMillis?.let { date = it }; showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = dpState) }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// AddEditInvestmentSheet
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditInvestmentSheet(
    fileId: String,
    types: List<InvestmentType>,
    onDismiss: () -> Unit,
    onSave: (Investment) -> Unit,
    onAddType: (String, (Boolean) -> Unit) -> Unit
) {
    var type by remember { mutableStateOf(types.firstOrNull()?.name ?: "") }
    var amountText by remember { mutableStateOf("") }
    var isOnline by remember { mutableStateOf(false) }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }
    var notes by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showNewTypeInput by remember { mutableStateOf(false) }
    var newTypeName by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            Modifier.fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text("Add Investment", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            // Type dropdown + inline add
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }, modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        types.forEach { t ->
                            DropdownMenuItem(text = { Text(t.name) }, onClick = { type = t.name; expanded = false })
                        }
                    }
                }
                if (showNewTypeInput) {
                    OutlinedTextField(
                        value = newTypeName,
                        onValueChange = { newTypeName = it },
                        placeholder = { Text("New name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        if (newTypeName.isNotBlank()) {
                            onAddType(newTypeName) { success ->
                                if (success) { type = newTypeName; showNewTypeInput = false; newTypeName = "" }
                            }
                        }
                    }) { Icon(Icons.Default.Check, null) }
                } else {
                    IconButton(onClick = { showNewTypeInput = true }) { Icon(Icons.Default.Add, "Add type") }
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() || c == '.' }; amountError = false },
                label = { Text("Amount *") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Text("₹", modifier = Modifier.padding(start = 8.dp)) },
                isError = amountError,
                supportingText = if (amountError) {{ Text("Amount must be > 0") }} else null
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Online Payment", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = isOnline, onCheckedChange = { isOnline = it })
            }

            Spacer(Modifier.height(8.dp))

            val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.CalendarToday, null, Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Date: ${dateFmt.format(Date(date))}")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Comments/Notes") },
                modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 3
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount <= 0.0 || type.isBlank()) { amountError = amount <= 0.0; return@Button }
                    onSave(Investment(fileId = fileId, type = type, amount = amount, isOnline = isOnline, date = date, notes = notes.ifBlank { null }))
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save Investment") }
        }
    }

    if (showDatePicker) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = date)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { dpState.selectedDateMillis?.let { date = it }; showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = dpState) }
    }
}
