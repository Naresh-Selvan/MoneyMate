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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.moneymate.app.data.local.entity.Payment
import com.moneymate.app.data.local.entity.PaymentMode
import com.moneymate.app.data.local.entity.LoanType
import com.moneymate.app.ui.viewmodel.PaymentViewModel
import com.moneymate.app.ui.viewmodel.PersonViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PersonDetailScreen(
    navController: NavHostController,
    personId: String,
    paymentViewModel: PaymentViewModel = hiltViewModel(),
    personViewModel: PersonViewModel   = hiltViewModel()
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dtFormat   = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    LaunchedEffect(personId) { paymentViewModel.loadPaymentsForPerson(personId) }

    val payments by paymentViewModel.payments.collectAsState()
    val person   by produceState<com.moneymate.app.data.local.entity.Person?>(null, personId) {
        value = personViewModel.getPersonById(personId)
    }

    val isBorrowing = person?.recordType == LoanType.BORROWING

    // Default payment date = today
    val defaultPaymentDate: Long = remember { System.currentTimeMillis() }

    var showAddDialog       by remember { mutableStateOf(false) }
    var paymentToDelete     by remember { mutableStateOf<Payment?>(null) }
    var paymentToEdit       by remember { mutableStateOf<Payment?>(null) }
    var selectedIds         by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showMultiDeleteDialog by remember { mutableStateOf(false) }
    val isSelecting = selectedIds.isNotEmpty()

    var newAmount by remember { mutableStateOf("") }
    var newMode   by remember { mutableStateOf(PaymentMode.CASH) }
    var newDate   by remember(defaultPaymentDate) { mutableStateOf(defaultPaymentDate) }
    var showNewDatePicker by remember { mutableStateOf(false) }

    val totalPaid     = payments.sumOf { it.amount }
    val totalPaidCash = payments.filter { it.mode == PaymentMode.CASH }.sumOf { it.amount }
    val totalPaidUpi  = payments.filter { it.mode == PaymentMode.UPI  }.sumOf { it.amount }
    val amountGiven   = person?.amountGiven ?: 0.0
    val balance       = amountGiven - totalPaid

    Scaffold(
        topBar = {
            if (isSelecting) {
                TopAppBar(
                    title = { Text("${selectedIds.size} selected", fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onClick = { selectedIds = emptySet() }) { Icon(Icons.Default.Close, null) } },
                    actions = { IconButton(onClick = { showMultiDeleteDialog = true }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) } }
                )
            } else {
                TopAppBar(
                    title = { Text(person?.name ?: "Person Detail", fontWeight = FontWeight.Bold) },
                    navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }
                )
            }
        },
        floatingActionButton = {
            if (!isSelecting) FloatingActionButton(onClick = { showAddDialog = true }) { Icon(Icons.Default.Add, null) }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {

            // Person summary card
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    person?.let { p ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(p.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.width(8.dp))
                            AssistChip(onClick = {}, label = {
                                Text(if (isBorrowing) "Borrowing" else "Lending", style = MaterialTheme.typography.labelSmall)
                            })
                        }
                        if (!p.place.isNullOrEmpty()) Text(p.place, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (!p.mobileNumber.isNullOrEmpty()) Text("📞 ${p.mobileNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Date: ${dateFormat.format(Date(p.dateGiven))}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(12.dp))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        SummaryItem(if (isBorrowing) "Borrowed" else "Given", "₹$amountGiven")
                        SummaryItem(if (isBorrowing) "Paid Back" else "Received", "₹$totalPaid")
                        SummaryItem("Pending", "₹$balance")
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        SummaryItem("Cash", "₹$totalPaidCash")
                        SummaryItem("UPI",  "₹$totalPaidUpi")
                    }
                }
            }

            if (payments.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No payments yet. Tap + to add one!", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(payments, key = { it.id }) { payment ->
                        val isSelected = payment.id in selectedIds
                        PaymentCard(
                            payment = payment, isSelected = isSelected, isSelecting = isSelecting,
                            isBorrowing = isBorrowing, dtFormat = dtFormat,
                            onClick = {
                                if (isSelecting) selectedIds = if (isSelected) selectedIds - payment.id else selectedIds + payment.id
                            },
                            onLongClick = { selectedIds = selectedIds + payment.id },
                            onDelete = { paymentToDelete = payment },
                            onEdit   = { paymentToEdit   = payment }
                        )
                    }
                }
            }
        }
    }

    // Add Payment dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; newAmount = ""; newMode = PaymentMode.CASH; newDate = defaultPaymentDate },
            title = { Text(if (isBorrowing) "Add Repayment" else "Add Payment Received") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newAmount,
                        onValueChange = { newAmount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Amount") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = newMode == PaymentMode.CASH, onClick = { newMode = PaymentMode.CASH }, label = { Text("Cash") })
                        FilterChip(selected = newMode == PaymentMode.UPI,  onClick = { newMode = PaymentMode.UPI  }, label = { Text("UPI")  })
                    }
                    OutlinedButton(onClick = { showNewDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(dateFormat.format(Date(newDate)))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amt = newAmount.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        paymentViewModel.insertPayment(Payment(personId = personId, amount = amt, mode = newMode, date = newDate))
                        newAmount = ""; newMode = PaymentMode.CASH; newDate = defaultPaymentDate; showAddDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }

    if (showNewDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = newDate)
        DatePickerDialog(
            onDismissRequest = { showNewDatePicker = false },
            confirmButton = { TextButton(onClick = { newDate = state.selectedDateMillis ?: newDate; showNewDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showNewDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }

    // Edit Payment dialog
    paymentToEdit?.let { orig ->
        var editAmount by remember { mutableStateOf(orig.amount.toBigDecimal().stripTrailingZeros().toPlainString()) }
        var editMode   by remember { mutableStateOf(orig.mode) }
        var editDate   by remember { mutableStateOf(orig.date) }
        var showEditDatePicker by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { paymentToEdit = null },
            title = { Text("Edit Payment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = { editAmount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Amount") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = editMode == PaymentMode.CASH, onClick = { editMode = PaymentMode.CASH }, label = { Text("Cash") })
                        FilterChip(selected = editMode == PaymentMode.UPI,  onClick = { editMode = PaymentMode.UPI  }, label = { Text("UPI")  })
                    }
                    OutlinedButton(onClick = { showEditDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(dateFormat.format(Date(editDate)))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amt = editAmount.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        paymentViewModel.updatePayment(orig.copy(amount = amt, mode = editMode, date = editDate))
                        paymentToEdit = null
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { paymentToEdit = null }) { Text("Cancel") } }
        )

        if (showEditDatePicker) {
            val state = rememberDatePickerState(initialSelectedDateMillis = editDate)
            DatePickerDialog(
                onDismissRequest = { showEditDatePicker = false },
                confirmButton = { TextButton(onClick = { editDate = state.selectedDateMillis ?: editDate; showEditDatePicker = false }) { Text("OK") } },
                dismissButton = { TextButton(onClick = { showEditDatePicker = false }) { Text("Cancel") } }
            ) { DatePicker(state = state) }
        }
    }

    paymentToDelete?.let { p ->
        AlertDialog(onDismissRequest = { paymentToDelete = null },
            title = { Text("Delete Payment?") }, text = { Text("₹${p.amount} will be moved to trash.") },
            confirmButton = { TextButton(onClick = { paymentViewModel.softDeletePayment(p.id); paymentToDelete = null }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { paymentToDelete = null }) { Text("Cancel") } })
    }
    if (showMultiDeleteDialog) {
        AlertDialog(onDismissRequest = { showMultiDeleteDialog = false },
            title = { Text("Delete ${selectedIds.size} payments?") }, text = { Text("Moved to trash.") },
            confirmButton = { TextButton(onClick = { selectedIds.forEach { paymentViewModel.softDeletePayment(it) }; selectedIds = emptySet(); showMultiDeleteDialog = false }) { Text("Delete All", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showMultiDeleteDialog = false }) { Text("Cancel") } })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PaymentCard(
    payment: Payment,
    isSelected: Boolean,
    isSelecting: Boolean,
    isBorrowing: Boolean,
    dtFormat: SimpleDateFormat,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isSelecting) { Checkbox(checked = isSelected, onCheckedChange = { onClick() }); Spacer(Modifier.width(8.dp)) }
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₹${payment.amount}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    AssistChip(onClick = {}, label = { Text(payment.mode.name, style = MaterialTheme.typography.labelSmall) })
                    Spacer(Modifier.width(4.dp))
                    AssistChip(onClick = {}, label = {
                        Text(if (isBorrowing) "Repayment" else "Received", style = MaterialTheme.typography.labelSmall)
                    })
                }
                Text(dtFormat.format(Date(payment.date)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!isSelecting) {
                IconButton(onClick = onEdit)   { Icon(Icons.Default.Edit,   null, tint = MaterialTheme.colorScheme.primary) }
                IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)   }
            }
        }
    }
}