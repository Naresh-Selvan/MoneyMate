
package com.moneymate.app.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.moneymate.app.data.local.entity.Payment
import com.moneymate.app.data.local.entity.PaymentMode
import com.moneymate.app.data.local.entity.Person
import org.burnoutcrew.reorderable.ReorderableLazyListState
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeablePersonCard(
    person: Person,
    serialNumber: Int,
    totalPaid: Double,
    pending: Double,
    isSelected: Boolean,
    isSelecting: Boolean,
    elevation: androidx.compose.ui.unit.Dp,
    reorderState: ReorderableLazyListState,
    showWeeksColumns: Boolean,
    dateFormat: SimpleDateFormat,
    dayBreakdowns: List<DayBreakdown> = emptyList(),
    personPayments: List<Payment> = emptyList(),
    dateColPager: androidx.compose.foundation.pager.PagerState? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onMarkComplete: () -> Unit = {},
    onView: () -> Unit = {},
    onQuickPayment: (amount: Double, mode: PaymentMode) -> Unit
) {
    var showPaymentSheet by remember { mutableStateOf(false) }

    // Swipe state — only enable swipe when not in selection mode
    val swipeState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd && !isSelecting) {
                showPaymentSheet = true
            }
            false // Never actually dismiss — we just use the swipe as a trigger
        },
        positionalThreshold = { it * 0.35f } // 35% swipe to trigger
    )

    // Reset swipe after sheet is dismissed
    LaunchedEffect(showPaymentSheet) {
        if (!showPaymentSheet) {
            swipeState.reset()
        }
    }

    SwipeToDismissBox(
        state = swipeState,
        enableDismissFromStartToEnd = !isSelecting,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            val progress = swipeState.progress
            val triggered = swipeState.dismissDirection == SwipeToDismissBoxValue.StartToEnd

            val bgColor by animateColorAsState(
                targetValue = if (triggered && progress > 0.1f)
                    MaterialTheme.colorScheme.primaryContainer
                else Color.Transparent,
                animationSpec = tween(150),
                label = "swipe_bg"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .padding(start = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (triggered && progress > 0.1f) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.AddCircle,
                            contentDescription = "Add Payment",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            "Add Payment",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    ) {
        PersonCard(
            person = person,
            serialNumber = serialNumber,
            totalPaid = totalPaid,
            pending = pending,
            isSelected = isSelected,
            isSelecting = isSelecting,
            elevation = elevation,
            reorderState = reorderState,
            showWeeksColumns = showWeeksColumns,
            dateFormat = dateFormat,
            dayBreakdowns = dayBreakdowns,
            personPayments = personPayments,
            dateColPager = dateColPager,
            onClick = onClick,
            onLongClick = onLongClick,
            onDelete = onDelete,
            onEdit = onEdit,
            onMarkComplete = onMarkComplete,
            onView = onView
        )
    }

    // Quick Payment Bottom Sheet
    if (showPaymentSheet) {
        QuickPaymentSheet(
            person = person,
            pending = pending,
            onDismiss = { showPaymentSheet = false },
            onConfirm = { amount, mode ->
                onQuickPayment(amount, mode)
                showPaymentSheet = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickPaymentSheet(
    person: Person,
    pending: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double, PaymentMode) -> Unit
) {
    var amount by remember { mutableStateOf("") }
    var mode   by remember { mutableStateOf(PaymentMode.CASH) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.Payment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        "Add Payment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${person.name}${if (!person.place.isNullOrEmpty()) " • ${person.place}" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Pending balance chip
            if (pending > 0) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Outstanding: ₹$pending",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        // Quick fill button
                        TextButton(
                            onClick = { amount = pending.toBigDecimal().stripTrailingZeros().toPlainString() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text("Fill full", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Amount field
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Amount Received*") },
                prefix = { Text("₹") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            // Mode toggle
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = mode == PaymentMode.CASH,
                    onClick = { mode = PaymentMode.CASH },
                    label = { Text("Cash") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = mode == PaymentMode.UPI,
                    onClick = { mode = PaymentMode.UPI },
                    label = { Text("UPI") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Confirm button
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (amt != null && amt > 0) onConfirm(amt, mode)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.toDoubleOrNull()?.let { it > 0 } ?: false
            ) {
                Text("Confirm Payment")
            }
        }
    }
}
