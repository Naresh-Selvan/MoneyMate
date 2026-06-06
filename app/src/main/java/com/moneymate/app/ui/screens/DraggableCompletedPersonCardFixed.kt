package com.moneymate.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneymate.app.data.local.entity.Payment
import com.moneymate.app.data.local.entity.Person
import java.text.SimpleDateFormat

/**
 * Fixed version of DraggableCompletedPersonCard.
 *
 * **BUG FIX**: The modifier order is swapped so `.clickable` comes BEFORE
 * `.pointerInput(drag)`. In Compose, the innermost (rightmost in the chain)
 * modifier gets pointer events FIRST. Previously `.clickable` was after
 * (closer to content than) `.pointerInput`, meaning `.clickable`'s internal
 * `detectTapGestures` consumed the initial down event on a long press,
 * preventing `detectDragGesturesAfterLongPress` from starting the drag.
 *
 * With the corrected order:
 *   - Long-press drag: `pointerInput(drag)` (innermost) gets events FIRST,
 *     detects the long press, and starts consuming drag events.
 *   - Simple tap: `pointerInput(drag)` gets the down event, waits for a long
 *     press, but then sees an early up event → cancels gracefully.
 *     `clickable` then handles the tap normally.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DraggableCompletedPersonCardFixed(
    person: Person,
    balance: Double,
    daysLeft: Int,
    dateFormat: SimpleDateFormat,
    payments: List<Payment>,
    onTap: () -> Unit,
    onDragStarted: () -> Unit,
    onDragMoved: (Offset) -> Unit,
    onDragEnded: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var cardPosition by remember { mutableStateOf(Offset.Zero) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                cardPosition = coords.positionInWindow()
            }
            // FIX: .clickable BEFORE .pointerInput(drag) so drag gets events first
            .clickable { expanded = !expanded }
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
                    onDragEnd = { onDragEnded() },
                    onDragCancel = { onDragEnded() }
                )
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(person.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (!person.place.isNullOrEmpty())
                        Text(person.place, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!person.mobileNumber.isNullOrEmpty())
                        Text(person.mobileNumber!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {                            Text("₹${person.amountGiven} given",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium)
                        Text(if (balance <= 0.0) "✓ Balance: ₹0" else "Balance: ₹$balance",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (balance <= 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Medium)
                        Text(if (daysLeft > 0) "$daysLeft days left" else "expires soon",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (daysLeft <= 5) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("Tap to start new loan",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Hold & drag\nto delete",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End)
                    IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(28.dp)) {
                        Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(
                        onClick = onTap,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("New Loan", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(6.dp))
                if (payments.isEmpty()) {
                    Text("No payments recorded.", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Payments (${payments.size})", style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    payments.forEachIndexed { i, payment ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${i + 1}. ${dateFormat.format(java.util.Date(payment.date))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(payment.mode.name, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${payment.amount}", style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Paid", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text("₹${payments.filter { !it.isDeleted }.sumOf { it.amount }}", style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
