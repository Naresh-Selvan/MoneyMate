package com.moneymate.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dtFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    LaunchedEffect(personId) { paymentViewModel.loadPaymentsForPerson(personId) }

    val payments by paymentViewModel.payments.collectAsState()
    val person by personViewModel.getPersonByIdFlow(personId).collectAsState(initial = null)

    val isBorrowing = person?.recordType == LoanType.BORROWING
    val defaultPaymentDate: Long = remember { System.currentTimeMillis() }
    val coroutineScope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }
    var showForceCloseDialog by remember { mutableStateOf(false) }
    var paymentToEdit by remember { mutableStateOf<Payment?>(null) }
    var selectedIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showMultiDeleteDialog by remember { mutableStateOf(false) }
    val isSelecting = selectedIds.isNotEmpty()

    var sliderActionTarget by remember { mutableStateOf<Pair<String, Payment>?>(null) }
    var newAmount by remember { mutableStateOf("") }
    var newMode by remember { mutableStateOf(PaymentMode.CASH) }
    var newDate by remember(defaultPaymentDate) { mutableLongStateOf(defaultPaymentDate) }
    var showNewDatePicker by remember { mutableStateOf(false) }

    val totalPaid = payments.sumOf { it.amount }
    val totalPaidCash = payments.filter { it.mode == PaymentMode.CASH }.sumOf { it.amount }
    val totalPaidUpi = payments.filter { it.mode == PaymentMode.UPI }.sumOf { it.amount }
    val amountGiven = person?.amountGiven ?: 0.0
    val personTotalRepayment = person?.totalRepayment ?: 0.0
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
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Force Close Dialog
    if (showForceCloseDialog && person != null) {
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
                                fileId = person!!.fileId,
                                discrepancyAmount = abs(discrepancy),
                                type = if (discrepancy > 0) AdjustmentType.BOOK_LOSS else AdjustmentType.BOOK_PROFIT,
                                reason = "Force close"
                            ))
                        }
                        personViewModel.markPersonAsCompleted(personId)
                        showForceCloseDialog = false
                    }
                }) { Text("Confirm", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showForceCloseDialog = false }) { Text("Cancel") } }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PaymentCardItem(
    payment: Payment,
    isBorrowing: Boolean,
    dtFormat: SimpleDateFormat,
    isSelected: Boolean,
    isSelecting: Boolean,
    onActionSelect: (String) -> Unit,
    onToggleSelection: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { if (isSelecting) onToggleSelection() },
                onLongClick = { onActionSelect("DELETE") }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
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
        }
    }
}
