package com.moneymate.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.moneymate.app.data.local.entity.*
import com.moneymate.app.ui.viewmodel.*
import androidx.compose.ui.platform.LocalContext
import com.moneymate.app.utils.isReminderSet
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PersonDetailScreen(
    navController: NavHostController,
    personId: String,
    paymentViewModel: PaymentViewModel = hiltViewModel(),
    personViewModel: PersonViewModel = hiltViewModel(),
    bookAdjustmentViewModel: BookAdjustmentViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dtFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    LaunchedEffect(personId) { paymentViewModel.loadPaymentsForPerson(personId) }

    val payments by paymentViewModel.payments.collectAsState()
    val person by personViewModel.getPersonByIdFlow(personId).collectAsState(initial = null)
    val currentPerson = person

    val isBorrowing = currentPerson?.recordType == LoanType.BORROWING
    val amountGiven = currentPerson?.amountGiven ?: 0.0
    val personTotalRepayment = currentPerson?.totalRepayment ?: 0.0
    val defaultPaymentDate: Long = remember { System.currentTimeMillis() }
    val coroutineScope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var showForceCloseDialog by remember { mutableStateOf(false) }
    var paymentToEdit by remember { mutableStateOf<Payment?>(null) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showMultiDeleteDialog by remember { mutableStateOf(false) }
    var showSingleDeleteConfirm by remember { mutableStateOf<Payment?>(null) }
    var showPaymentActionsSheet by remember { mutableStateOf<Payment?>(null) }
    var showDeletedPayments by remember { mutableStateOf(false) }
    val allDeletedPayments by paymentViewModel.deletedPayments.collectAsState()
    val personDeletedPayments = remember(allDeletedPayments, personId) {
        allDeletedPayments.filter { it.personId == personId }
    }
    val isSelecting = selectedIds.isNotEmpty()

    var sliderActionTarget by remember { mutableStateOf<Pair<String, Payment>?>(null) }
    var newAmount by remember { mutableStateOf("") }
    var newMode by remember { mutableStateOf(PaymentMode.CASH) }
    var newDate by remember(defaultPaymentDate) { mutableLongStateOf(defaultPaymentDate) }
    var showNewDatePicker by remember { mutableStateOf(false) }

    // ── Reminder bell state ───────────────────────────────────────────────────
    var showReminderSheet by remember { mutableStateOf(false) }
    val appPrefs = remember { com.moneymate.app.utils.AppPreferences(context) }

    // Edit payment fields
    var editAmount by remember { mutableStateOf("") }
    var editMode by remember { mutableStateOf(PaymentMode.CASH) }
    var editDate by remember { mutableLongStateOf(defaultPaymentDate) }
    var showEditDatePicker by remember { mutableStateOf(false) }

    val totalPaid = payments.sumOf { it.amount }
    val totalPaidCash = payments.filter { it.mode == PaymentMode.CASH }.sumOf { it.amount }
    val totalPaidUpi = payments.filter { it.mode == PaymentMode.UPI }.sumOf { it.amount }
    val totalRepayment = if (personTotalRepayment > 0.0) personTotalRepayment else amountGiven
    val balance = (totalRepayment - totalPaid).coerceAtLeast(0.0)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                if (isSelecting) {
                    TopAppBar(
                        title = { Text("${selectedIds.size} selected", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { selectedIds = emptySet() }) {
                                Icon(Icons.Default.Close, null)
                            }
                        },
                        actions = {
                            val allSelected = selectedIds.size == payments.size && payments.isNotEmpty()
                            IconButton(onClick = {
                                selectedIds = if (allSelected) emptySet() else payments.map { it.id }.toSet()
                            }) {
                                Icon(
                                    if (allSelected) Icons.Default.Close else Icons.Default.DoneAll,
                                    contentDescription = if (allSelected) "Deselect All" else "Select All",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { showMultiDeleteDialog = true }) {
                                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                } else {
                    TopAppBar(
                        title = { Text(person?.name ?: "Person Detail", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                            }
                        },
                        actions = {
                            if (balance > 0 && person?.isCompleted == false) {
                                IconButton(onClick = { showForceCloseDialog = true }) {
                                    Icon(Icons.Default.Lock, contentDescription = "Force Close", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            person?.let { p ->
                                IconButton(onClick = {
                                    navController.navigate("loan_history/${p.id}/${java.net.URLEncoder.encode(p.name, "UTF-8")}")
                                }) {
                                    Icon(Icons.Default.History, contentDescription = "Loan History", tint = MaterialTheme.colorScheme.primary)
                                }
                                // ── Bell icon for loan reminder ──────────────
                                IconButton(onClick = { showReminderSheet = true }) {
                                    Icon(
                                        if (appPrefs.isReminderSet(p.id)) Icons.Default.Notifications
                                        else Icons.Default.NotificationsNone,
                                        contentDescription = "Set Reminder",
                                        tint = if (appPrefs.isReminderSet(p.id)) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                if (!isSelecting && person?.isCompleted == false) {
                    FloatingActionButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, null)
                    }
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
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
                            LocalSummaryItem(if (isBorrowing) "Borrowed" else "Given", "₹$amountGiven")
                            LocalSummaryItem(if (isBorrowing) "Paid Back" else "Received", "₹$totalPaid")
                            LocalSummaryItem("Pending", "₹$balance")
                        }
                        person?.let { p ->
                            if (p.totalRepayment > p.amountGiven) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Total with interest: ₹${p.totalRepayment} (Principal ₹${p.amountGiven} + Interest ₹${p.interestAmount})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            LocalSummaryItem("Cash", "₹$totalPaidCash")
                            LocalSummaryItem("UPI", "₹$totalPaidUpi")
                        }
                    }
                }

                if (payments.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No payments yet. Tap + to add one!", style = MaterialTheme.typography.bodyLarge)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(payments, key = { it.id }) { payment ->
                            PaymentCardItem(
                                payment = payment,
                                isBorrowing = isBorrowing,
                                dtFormat = dtFormat,
                                isSelected = payment.id in selectedIds,
                                isSelecting = isSelecting,
                                onActionSelect = { actionType -> sliderActionTarget = Pair(actionType, payment) },
                                onToggleSelection = {
                                    selectedIds = if (payment.id in selectedIds) selectedIds - payment.id else selectedIds + payment.id
                                },
                                onCardClick = {
                                    if (!isSelecting) {
                                        showPaymentActionsSheet = payment
                                    } else {
                                        selectedIds = if (payment.id in selectedIds) selectedIds - payment.id else selectedIds + payment.id
                                    }
                                },
                                onEdit = {
                                    editAmount = payment.amount.toBigDecimal().stripTrailingZeros().toPlainString()
                                    editMode = payment.mode
                                    editDate = payment.date
                                    paymentToEdit = payment
                                },
                                onDelete = {
                                    showSingleDeleteConfirm = payment
                                }
                            )
                        }

                        // ── Recently Deleted Payments section ──────────────────
                        if (personDeletedPayments.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(8.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    onClick = { showDeletedPayments = !showDeletedPayments }
                                                ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.DeleteSweep, null,
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                "Recently Deleted Payments (${personDeletedPayments.size})",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Icon(
                                                if (showDeletedPayments) Icons.Default.ExpandLess
                                                else Icons.Default.ExpandMore,
                                                null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        if (showDeletedPayments) {
                                            Spacer(Modifier.height(8.dp))
                                            HorizontalDivider()
                                            Spacer(Modifier.height(8.dp))
                                            Text(
                                                "Deleted payments can be restored within 180 days.",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        personDeletedPayments.forEach { deletedPayment ->
                                            if (showDeletedPayments) {
                                                Spacer(Modifier.height(6.dp))
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(Modifier.weight(1f)) {
                                                            Text(
                                                                "₹${deletedPayment.amount} • ${deletedPayment.mode.name}",
                                                                fontWeight = FontWeight.Medium,
                                                                style = MaterialTheme.typography.bodyMedium
                                                            )
                                                            Text(
                                                                dtFormat.format(Date(deletedPayment.date)),
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                            if (deletedPayment.deletedAt != null) {
                                                                Text(
                                                                    "Deleted: ${dtFormat.format(Date(deletedPayment.deletedAt))}",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                                                )
                                                            }
                                                        }
                                                        FilledTonalButton(
                                                            onClick = { paymentViewModel.restorePayment(deletedPayment.id) },
                                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                                        ) {
                                                            Icon(Icons.Default.Restore, null, modifier = Modifier.size(16.dp))
                                                            Spacer(Modifier.width(4.dp))
                                                            Text("Restore", style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Payment Dialog
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
                        coroutineScope.launch {
                            paymentViewModel.insertPaymentAwait(Payment(personId = personId, amount = amt, mode = newMode, date = newDate))
                            val remainingBalance = balance - amt
                            if (remainingBalance <= 0 && amountGiven > 0) {
                                personViewModel.markPersonAsCompleted(personId)
                            }
                            newAmount = ""; newMode = PaymentMode.CASH; newDate = defaultPaymentDate; showAddDialog = false
                        }
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

    // Force Close Dialog
    if (showForceCloseDialog && currentPerson != null) {
        var collectedAmount by remember { mutableStateOf(balance.toBigDecimal().stripTrailingZeros().toPlainString()) }
        AlertDialog(
            onDismissRequest = { showForceCloseDialog = false },
            title = { Text("Force close loan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Pending balance: ₹$balance")
                    OutlinedTextField(
                        value = collectedAmount,
                        onValueChange = { collectedAmount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Amount collected now") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val collected = collectedAmount.toDoubleOrNull() ?: 0.0
                    coroutineScope.launch {
                        paymentViewModel.insertPaymentAwait(Payment(personId = personId, amount = collected, mode = PaymentMode.CASH, date = System.currentTimeMillis()))
                        val totalPaidWithNew = totalPaid + collected
                        val discrepancy = totalRepayment - totalPaidWithNew
                        if (abs(discrepancy) > 0.01) {
                            bookAdjustmentViewModel.insert(BookAdjustment(
                                personId = personId,
                                fileId = currentPerson!!.fileId,
                                discrepancyAmount = abs(discrepancy),
                                type = if (discrepancy > 0) AdjustmentType.BOOK_LOSS else AdjustmentType.BOOK_PROFIT,
                                reason = "Force close"
                            ))
                        }
                        personViewModel.markPersonAsCompleted(personId)
                        showForceCloseDialog = false
                    }
                }) { Text("Confirm", color = MaterialTheme.colorScheme.error) }                    },
            dismissButton = { TextButton(onClick = { showForceCloseDialog = false }) { Text("Cancel") } }
        )
    }

    // ── Payment Actions Bottom Sheet ────────────────────────────────────────────
    showPaymentActionsSheet?.let { payment ->
        ModalBottomSheet(
            onDismissRequest = { showPaymentActionsSheet = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Payment Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "₹${payment.amount} • ${payment.mode.name} • ${dtFormat.format(Date(payment.date))}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        editAmount = payment.amount.toBigDecimal().stripTrailingZeros().toPlainString()
                        editMode = payment.mode
                        editDate = payment.date
                        paymentToEdit = payment
                        showPaymentActionsSheet = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Edit Payment")
                }

                OutlinedButton(
                    onClick = {
                        showSingleDeleteConfirm = payment
                        showPaymentActionsSheet = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Delete Payment")
                }

                TextButton(
                    onClick = { showPaymentActionsSheet = null },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel")
                }
            }
        }
    }

    // ── Edit Payment Dialog ─────────────────────────────────────────────────────
    if (paymentToEdit != null) {
        AlertDialog(
            onDismissRequest = { paymentToEdit = null },
            title = { Text("Edit Payment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editAmount,
                        onValueChange = { editAmount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Amount") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = editMode == PaymentMode.CASH,
                            onClick = { editMode = PaymentMode.CASH },
                            label = { Text("Cash") }
                        )
                        FilterChip(
                            selected = editMode == PaymentMode.UPI,
                            onClick = { editMode = PaymentMode.UPI },
                            label = { Text("UPI") }
                        )
                    }
                    OutlinedButton(
                        onClick = { showEditDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                        paymentViewModel.updatePayment(
                            paymentToEdit!!.copy(amount = amt, mode = editMode, date = editDate)
                        )
                        paymentToEdit = null
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { paymentToEdit = null }) { Text("Cancel") } }
        )
    }

    // Edit date picker
    if (showEditDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = editDate)
        DatePickerDialog(
            onDismissRequest = { showEditDatePicker = false },
            confirmButton = { TextButton(onClick = { editDate = datePickerState.selectedDateMillis ?: editDate; showEditDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showEditDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    // ── Single Delete Confirmation Dialog ───────────────────────────────────────
    showSingleDeleteConfirm?.let { payment ->
        AlertDialog(
            onDismissRequest = { showSingleDeleteConfirm = null },
            title = { Text("Delete Payment?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Are you sure you want to delete this payment?",
                        style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "₹${payment.amount} • ${payment.mode.name} • ${dtFormat.format(Date(payment.date))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("It can be restored from Recently Deleted within 180 days.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    paymentViewModel.softDeletePayment(payment.id)
                    showSingleDeleteConfirm = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showSingleDeleteConfirm = null }) { Text("Cancel") } }
        )
    }

    // ── Loan Reminder Sheet ─────────────────────────────────────────────────
    if (showReminderSheet && currentPerson != null) {
        SetLoanReminderSheet(
            personId = currentPerson.id,
            personName = currentPerson.name,
            defaultAmount = currentPerson.amountGiven,
            onDismiss = { showReminderSheet = false }
        )
    }

    // ── Multi Delete Confirmation Dialog ────────────────────────────────────────
    if (showMultiDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showMultiDeleteDialog = false },
            title = { Text("Delete ${selectedIds.size} payments?") },
            text = {
                Text("Are you sure you want to delete ${selectedIds.size} selected payments?",
                    style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedIds.forEach { id -> paymentViewModel.softDeletePayment(id) }
                    selectedIds = emptySet()
                    showMultiDeleteDialog = false
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showMultiDeleteDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun LocalSummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PaymentCardItem(
    payment: Payment,
    isBorrowing: Boolean,
    dtFormat: SimpleDateFormat,
    isSelected: Boolean,
    isSelecting: Boolean,
    onActionSelect: (String) -> Unit,
    onToggleSelection: () -> Unit,
    onCardClick: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false // snap back, don't dismiss permanently
            } else if (dismissValue == SwipeToDismissBoxValue.StartToEnd) {
                onEdit()
                false // snap back, don't dismiss permanently
            } else {
                true
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val color by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50) // Green for edit
                    SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.error // Red for delete
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                },
                animationSpec = tween(300),
                label = "swipeBg"
            )
            if (direction != null) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = if (direction == SwipeToDismissBoxValue.StartToEnd)
                        Arrangement.Start else Arrangement.End
                ) {
                    if (direction == SwipeToDismissBoxValue.StartToEnd) {
                        Icon(Icons.Default.Edit, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Edit", color = Color.White, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall)
                    } else {
                        Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Delete", color = Color.White, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        },
        enableDismissFromStartToEnd = !isSelecting,
        enableDismissFromEndToStart = !isSelecting
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onCardClick() },
                    onLongClick = { onActionSelect("DELETE") }
                ),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            )
        ) {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (isSelecting) {
                    Checkbox(checked = isSelected, onCheckedChange = { onToggleSelection() })
                    Spacer(Modifier.width(8.dp))
                }
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "₹${payment.amount}", fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
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
                    // 3-dot menu matching OverduePersonCard pattern
                    var showPaymentMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { showPaymentMenu = true }, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        DropdownMenu(expanded = showPaymentMenu, onDismissRequest = { showPaymentMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                leadingIcon = { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = { showPaymentMenu = false; onEdit() }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                onClick = { showPaymentMenu = false; onDelete() }
                            )
                        }
                    }
                }
            }
        }
    }
}
