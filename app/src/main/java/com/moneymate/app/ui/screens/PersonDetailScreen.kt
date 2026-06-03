package com.moneymate.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
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
import com.moneymate.app.data.local.entity.LoanType
import com.moneymate.app.data.local.entity.Payment
import com.moneymate.app.data.local.entity.PaymentMode
import com.moneymate.app.ui.viewmodel.PaymentViewModel
import com.moneymate.app.ui.viewmodel.PersonViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import androidx.compose.foundation.gestures.detectDragGestures

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
    val defaultPaymentDate: Long = remember { System.currentTimeMillis() }

    var showAddDialog      by remember { mutableStateOf(false) }
    var paymentToEdit      by remember { mutableStateOf<Payment?>(null) }

    var selectedIds           by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showMultiDeleteDialog by remember { mutableStateOf(false) }
    val isSelecting = selectedIds.isNotEmpty()

    var sliderActionTarget by remember { mutableStateOf<Pair<String, Payment>?>(null) }

    var newAmount by remember { mutableStateOf("") }
    var newMode   by remember { mutableStateOf(PaymentMode.CASH) }
    var newDate   by remember(defaultPaymentDate) { mutableLongStateOf(defaultPaymentDate) }
    var showNewDatePicker by remember { mutableStateOf(false) }

    val totalPaid     = payments.sumOf { it.amount }
    val totalPaidCash = payments.filter { it.mode == PaymentMode.CASH }.sumOf { it.amount }
    val totalPaidUpi  = payments.filter { it.mode == PaymentMode.UPI  }.sumOf { it.amount }
    val amountGiven   = person?.amountGiven ?: 0.0
    val balance       = amountGiven - totalPaid

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
                        }
                    )
                }
            },
            floatingActionButton = {
                if (!isSelecting) {
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
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            LocalSummaryItem("Cash", "₹$totalPaidCash")
                            LocalSummaryItem("UPI",  "₹$totalPaidUpi")
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
                            val isSelected = payment.id in selectedIds

                            PaymentCardItem(
                                payment = payment,
                                isBorrowing = isBorrowing,
                                dtFormat = dtFormat,
                                isSelected = isSelected,
                                isSelecting = isSelecting,
                                onActionSelect = { actionType ->
                                    sliderActionTarget = Pair(actionType, payment)
                                },
                                onToggleSelection = {
                                    selectedIds = if (isSelected) selectedIds - payment.id else selectedIds + payment.id
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Slide to Confirm Bottom Sheet ─────────────────────────────────────────
    if (sliderActionTarget != null) {
        val target = sliderActionTarget!!
        ModalBottomSheet(
            onDismissRequest = { sliderActionTarget = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (target.first == "DELETE") "Slide to Confirm Delete" else "Slide to Confirm Edit",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Amount: ₹${target.second.amount} (${target.second.mode.name})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(24.dp))

                NativeActionConfirmationSlider(
                    accentColor = if (target.first == "DELETE") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    icon = if (target.first == "DELETE") Icons.Default.Delete else Icons.Default.Edit,
                    // Look around line 170 where sliderActionTarget bottom sheet handles onConfirmed:
                    onConfirmed = {
                        val targetedAction = target.first
                        val targetedPayment = target.second
                        sliderActionTarget = null // Closes bottom sheet overlay

                        if (targetedAction == "DELETE") {
                            paymentViewModel.softDeletePayment(targetedPayment.id)
                        } else {
                            paymentToEdit = targetedPayment // Triggers the Edit AlertDialog layout setup
                        }
                    }
                )
            }
        }
    }

    // ── Dialog Windows ────────────────────────────────────────────────────────
    if (showMultiDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showMultiDeleteDialog = false },
            title = { Text("Delete ${selectedIds.size} payments?") },
            text = { Text("These payments will be moved to Recently Deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    selectedIds.forEach { paymentViewModel.softDeletePayment(it) }
                    selectedIds = emptySet()
                    showMultiDeleteDialog = false
                }) { Text("Delete All", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { showMultiDeleteDialog = false }) { Text("Cancel") } }
        )
    }

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
                        // Auto-complete: if new payment brings remaining balance to zero,
                        // mark as completed and spawn a fresh zero-amount clone.
                        val remainingBalance = balance - amt
                        if (remainingBalance <= 0 && amountGiven > 0) {
                            personViewModel.markPersonAsCompleted(personId)
                        }
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

    paymentToEdit?.let { orig ->
        var editAmount by remember { mutableStateOf(orig.amount.toBigDecimal().stripTrailingZeros().toPlainString()) }
        var editMode   by remember { mutableStateOf(orig.mode) }
        var editDate   by remember { mutableLongStateOf(orig.date) }
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
    var contextualMenuExpanded by remember { mutableStateOf(false) }

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
                if (!isSelecting) {
                    Text(
                        "Long press row to trash entry quickly",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
            }

            if (!isSelecting) {
                Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                    IconButton(onClick = { contextualMenuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = contextualMenuExpanded,
                        onDismissRequest = { contextualMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Entry") },
                            leadingIcon = { Icon(Icons.Default.Edit, null) },
                            onClick = {
                                contextualMenuExpanded = false
                                onActionSelect("EDIT")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Entry", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                contextualMenuExpanded = false
                                onActionSelect("DELETE")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NativeActionConfirmationSlider(
    accentColor: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onConfirmed: () -> Unit
) {
    val trackWidth = 280.dp
    val thumbSize = 56.dp

    val density = LocalDensity.current
    val totalSwipeDistancePx = with(density) { (trackWidth - thumbSize).toPx() }

    var thumbPositionX by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .width(trackWidth)
            .height(thumbSize)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = "Swipe right to execute",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.align(Alignment.Center)
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(thumbPositionX.roundToInt(), 0) }
                .size(thumbSize)
                .padding(4.dp)
                .background(accentColor, CircleShape)
                .pointerInput(totalSwipeDistancePx) {
                    // Changed to detectDragGestures for instantaneous, natural swiping response
                    detectDragGestures(
                        onDragStart = {},
                        onDragEnd = {
                            if (thumbPositionX >= totalSwipeDistancePx * 0.82f) {
                                thumbPositionX = totalSwipeDistancePx
                                onConfirmed()
                            } else {
                                thumbPositionX = 0f
                            }
                        },
                        onDragCancel = { thumbPositionX = 0f },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            thumbPositionX = (thumbPositionX + dragAmount.x).coerceIn(0f, totalSwipeDistancePx)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
fun LocalSummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}