
package com.moneymate.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
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

    // ── Multi-select (3-tap to enter) ─────────────────────────────────────────
    var selectedIds           by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showMultiDeleteDialog by remember { mutableStateOf(false) }
    val isSelecting = selectedIds.isNotEmpty()

    // ── Drag-to-delete state ──────────────────────────────────────────────────
    var draggingPayment        by remember { mutableStateOf<Payment?>(null) }
    var dragOffset             by remember { mutableStateOf(Offset.Zero) }
    var showPaymentDustbin     by remember { mutableStateOf(false) }
    var dustbinPos             by remember { mutableStateOf(Offset.Zero) }
    var isOverPaymentDustbin   by remember { mutableStateOf(false) }
    var paymentToConfirmDelete by remember { mutableStateOf<Payment?>(null) }

    val paymentDustbinScale by animateFloatAsState(
        targetValue = if (showPaymentDustbin) (if (isOverPaymentDustbin) 1.3f else 1f) else 0f,
        animationSpec = tween(200),
        label = "paymentDustbinScale"
    )

    var newAmount by remember { mutableStateOf("") }
    var newMode   by remember { mutableStateOf(PaymentMode.CASH) }
    var newDate   by remember(defaultPaymentDate) { mutableStateOf(defaultPaymentDate) }
    var showNewDatePicker by remember { mutableStateOf(false) }

    val totalPaid     = payments.sumOf { it.amount }
    val totalPaidCash = payments.filter { it.mode == PaymentMode.CASH }.sumOf { it.amount }
    val totalPaidUpi  = payments.filter { it.mode == PaymentMode.UPI  }.sumOf { it.amount }
    val amountGiven   = person?.amountGiven ?: 0.0
    val balance       = amountGiven - totalPaid

    // ── Edit button animation state ───────────────────────────────────────────
    var pendingEditPayment by remember { mutableStateOf<Payment?>(null) }
    val editButtonScale by animateFloatAsState(
        targetValue = if (pendingEditPayment != null) 1.6f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "editScale",
        finishedListener = { scale ->
            if (scale >= 1.55f) {
                paymentToEdit = pendingEditPayment
                pendingEditPayment = null
            }
        }
    )

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
                                Icon(Icons.Default.ArrowBack, null)
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

                if (payments.isNotEmpty()) {
                    Text(
                        "Hold & drag to delete  •  Triple-tap to select  •  Long press ✏ to edit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )
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
                            val isEditAnimating = pendingEditPayment?.id == payment.id
                            DraggablePaymentCard(
                                payment = payment,
                                isBorrowing = isBorrowing,
                                dtFormat = dtFormat,
                                isSelected = isSelected,
                                isSelecting = isSelecting,
                                editScale = if (isEditAnimating) editButtonScale else 1f,
                                onTripleTap = {
                                    // 3-tap enters multi-select and selects this card
                                    selectedIds = selectedIds + payment.id
                                },
                                onTap = {
                                    // Normal tap toggles selection only when already in multi-select
                                    if (isSelecting) {
                                        selectedIds = if (isSelected)
                                            selectedIds - payment.id
                                        else
                                            selectedIds + payment.id
                                    }
                                },
                                onDragStarted = {
                                    // Drag only available when not in multi-select mode
                                    if (!isSelecting) {
                                        draggingPayment = payment
                                        showPaymentDustbin = true
                                    }
                                },
                                onDragMoved = { offset ->
                                    if (!isSelecting) {
                                        dragOffset = offset
                                        val cx = dustbinPos.x + 38f
                                        val cy = dustbinPos.y + 38f
                                        val dx = offset.x - cx
                                        val dy = offset.y - cy
                                        isOverPaymentDustbin = dx * dx + dy * dy < 100f * 100f
                                    }
                                },
                                onDragEnded = {
                                    if (!isSelecting) {
                                        if (isOverPaymentDustbin && draggingPayment != null) {
                                            paymentToConfirmDelete = draggingPayment
                                        }
                                        draggingPayment = null
                                        dragOffset = Offset.Zero
                                        showPaymentDustbin = false
                                        isOverPaymentDustbin = false
                                    }
                                },
                                onEditLongPress = {
                                    if (!isSelecting) {
                                        pendingEditPayment = payment
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // ── Dustbin overlay (hidden during multi-select) ───────────────────────
        if (showPaymentDustbin) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.15f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .onGloballyPositioned { coords -> dustbinPos = coords.positionInWindow() }
                    .scale(paymentDustbinScale)
                    .background(
                        if (isOverPaymentDustbin) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .padding(20.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Drop to delete",
                    tint = if (isOverPaymentDustbin) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                "Drop here to delete",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp)
            )
        }

        // ── Drag ghost ────────────────────────────────────────────────────────
        if (draggingPayment != null) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(dragOffset.x.roundToInt() - 80, dragOffset.y.roundToInt() - 40) }
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    "₹${draggingPayment!!.amount}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }

    // ── Confirm single delete ─────────────────────────────────────────────────
    paymentToConfirmDelete?.let { p ->
        AlertDialog(
            onDismissRequest = { paymentToConfirmDelete = null },
            title = { Text("Delete ₹${p.amount} payment?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("This payment will be moved to Recently Deleted.", style = MaterialTheme.typography.bodyMedium)
                    Text("It can be restored within 180 days.", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    paymentViewModel.softDeletePayment(p.id)
                    paymentToConfirmDelete = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { paymentToConfirmDelete = null }) { Text("Cancel") } }
        )
    }

    // ── Confirm multi delete ──────────────────────────────────────────────────
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

    // ── Add Payment dialog ────────────────────────────────────────────────────
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

    // ── Edit Payment dialog ───────────────────────────────────────────────────
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
}

// ── DraggablePaymentCard ───────────────────────────────────────────────────────
// Gestures (mutually exclusive):
//   Single tap      → toggle selection (only when already in multi-select mode)
//   Triple tap      → enter multi-select and select this card
//   Long press+drag → drag to dustbin (only when NOT in multi-select mode)
//   Long press ✏   → scale animation → open edit dialog (consumes event, no drag)
@Composable
fun DraggablePaymentCard(
    payment: Payment,
    isBorrowing: Boolean,
    dtFormat: SimpleDateFormat,
    isSelected: Boolean,
    isSelecting: Boolean,
    editScale: Float,
    onTripleTap: () -> Unit,
    onTap: () -> Unit,
    onDragStarted: () -> Unit,
    onDragMoved: (Offset) -> Unit,
    onDragEnded: () -> Unit,
    onEditLongPress: () -> Unit
) {
    var cardPosition by remember { mutableStateOf(Offset.Zero) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords -> cardPosition = coords.positionInWindow() }
            // Triple-tap detection runs first (declared first = higher priority).
            // detectTapGestures handles onTap and triple tap via onPress counting;
            // we use the requireUnconsumed=false path so it coexists with the drag
            // gesture below without consuming single taps that the drag needs to
            // ignore during its long-press threshold wait.
            .pointerInput(isSelecting) {
                detectTapGestures(
                    onTap = { onTap() }
                )
            }
            // Triple-tap: count rapid successive taps with a 400 ms window.
            .pointerInput(Unit) {
                var tapCount = 0
                var lastTapTime = 0L
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val pressed = event.changes.any { it.pressed }
                        if (!pressed) {
                            val now = System.currentTimeMillis()
                            if (now - lastTapTime < 400L) {
                                tapCount++
                            } else {
                                tapCount = 1
                            }
                            lastTapTime = now
                            if (tapCount >= 3) {
                                tapCount = 0
                                onTripleTap()
                            }
                        }
                    }
                }
            }
            // Long press + drag = delete. Only fires when not in multi-select mode
            // (guarded inside onDragStarted/onDragMoved/onDragEnded callbacks above).
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        onDragStarted()
                        onDragMoved(Offset(cardPosition.x + offset.x, cardPosition.y + offset.y))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        onDragMoved(Offset(change.position.x + cardPosition.x, change.position.y + cardPosition.y))
                    },
                    onDragEnd    = { onDragEnded() },
                    onDragCancel = { onDragEnded() }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            if (isSelecting) {
                Checkbox(checked = isSelected, onCheckedChange = { onTap() })
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
                Text(dtFormat.format(Date(payment.date)), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!isSelecting) {
                    Text(
                        "Hold & drag to delete  •  Triple-tap to select",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    )
                }
            }

            if (!isSelecting) {
                // Long press on edit icon → scale animation → open edit dialog.
                // detectTapGestures(onLongPress) consumes the event internally so
                // it never bubbles up to the card's drag gesture handler.
                IconButton(
                    onClick = { /* intentionally empty — edit is long-press only */ },
                    modifier = Modifier
                        .size(40.dp)
                        .scale(editScale)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { onEditLongPress() }
                            )
                        }
                ) {
                    Icon(Icons.Default.Edit, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
