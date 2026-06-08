package com.moneymate.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneymate.app.data.local.entity.LoanType
import com.moneymate.app.data.local.entity.Payment
import com.moneymate.app.data.local.entity.Person
import org.burnoutcrew.reorderable.ReorderableLazyListState
import java.text.SimpleDateFormat
import java.util.*

// ── PendingNewLoanCard ───────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PendingNewLoanCard(
    person: Person,
    dateFormat: SimpleDateFormat,
    onTap: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onTap),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(person.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                if (!person.place.isNullOrEmpty())
                    Text(person.place, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!person.mobileNumber.isNullOrEmpty())
                    Text(person.mobileNumber!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(2.dp))
                Text("Pending New Loan", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── TrashContent ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrashContent(
    deletedPersons: List<Person>, autoDeleteDays: Int,
    isSelectingTrash: Boolean, selectedTrashIds: Set<String>, padding: PaddingValues,
    onToggleSelect: (String) -> Unit, onLongSelect: (String) -> Unit,
    onRestore: (String) -> Unit, onHardDelete: (String) -> Unit
) {
    if (deletedPersons.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Delete, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Text("No recently deleted persons", style = MaterialTheme.typography.titleMedium)
                Text("Deleted persons appear here for $autoDeleteDays days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(deletedPersons, key = { _, p -> p.id }) { _, person ->
                val isSelected = person.id in selectedTrashIds
                val daysLeft = autoDeleteDays - ((System.currentTimeMillis() - (person.deletedAt ?: 0L)) / (1000 * 60 * 60 * 24)).toInt()
                Card(Modifier.fillMaxWidth().combinedClickable(onClick = { if (isSelectingTrash) onToggleSelect(person.id) }, onLongClick = { onLongSelect(person.id) }),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (isSelectingTrash) { Checkbox(checked = isSelected, onCheckedChange = { onToggleSelect(person.id) }); Spacer(Modifier.width(8.dp)) }
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(person.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                if (person.isCompleted) {
                                    AssistChip(onClick = {}, label = { Text("Completed", style = MaterialTheme.typography.labelSmall) },
                                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer))
                                }
                            }
                            Text("₹${person.amountGiven} • ${person.mode.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if (daysLeft > 0) "$daysLeft days left" else "Expires soon", style = MaterialTheme.typography.bodySmall,
                                color = if (daysLeft <= 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (!isSelectingTrash) {
                            IconButton(onClick = { onRestore(person.id) })    { Icon(Icons.Default.Restore,      null, tint = MaterialTheme.colorScheme.primary) }
                            IconButton(onClick = { onHardDelete(person.id) }) { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error)   }
                        }
                    }
                }
            }
        }
    }
}

// ── SummaryItem ───────────────────────────────────────────────────────────────

@Composable
fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(label,  style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── PersonCard ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PersonCard(
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
    dateColPager: PagerState? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onMarkComplete: () -> Unit = {},
    onView: () -> Unit = {},
    onCallNow: () -> Unit = {}
) {
    val isBorrowing = person.recordType == LoanType.BORROWING
    val isFullyPaid  = pending <= 0 && totalPaid > 0

    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
        elevation = CardDefaults.cardElevation(elevation),
        colors = CardDefaults.cardColors(containerColor = when {
            isSelected  -> MaterialTheme.colorScheme.primaryContainer
            isBorrowing -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            else        -> MaterialTheme.colorScheme.surface
        })
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("$serialNumber.", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp))

            if (isSelecting) {
                Checkbox(checked = isSelected, onCheckedChange = { onClick() })
                Spacer(Modifier.width(4.dp))
            } else {
                Spacer(Modifier.width(4.dp))
            }

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(person.name, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(6.dp))
                    AssistChip(onClick = {}, label = {
                        Text(person.mode.name, style = MaterialTheme.typography.labelSmall)
                    })
                    if (isFullyPaid) {
                        Spacer(Modifier.width(4.dp))
                        AssistChip(onClick = {}, label = { Text("✓ Paid", style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer))
                    }
                }
                if (!person.place.isNullOrEmpty())
                    Text(person.place, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!person.mobileNumber.isNullOrEmpty())
                    Text("📞 ${person.mobileNumber}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!showWeeksColumns || dateColPager == null) {
                    Text(dateFormat.format(Date(person.dateGiven)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("₹${person.amountGiven}", fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isBorrowing) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary)
                    PersonInterestInfo(
                        interestRate         = person.interestRate,
                        totalRepayment       = person.totalRepayment,
                        perInstallmentAmount = person.perInstallmentAmount,
                        numberOfInstallments = person.numberOfInstallments,
                        isDurationBased      = person.isDurationBased,
                        durationDays         = person.durationDays
                    )
                }
            }

            if (showWeeksColumns && dateColPager != null && dayBreakdowns.isNotEmpty()) {
                val colPage = dateColPager.currentPage
                Spacer(Modifier.width(8.dp))

                val totalPaidAllTime = personPayments.sumOf { it.amount }

                if (colPage < dayBreakdowns.size) {
                    val dayBreak = dayBreakdowns[colPage]
                    val weekStart = dayBreak.weekStart
                    val weekEnd   = dayBreak.weekEnd
                    val thisWeekReturn = personPayments
                        .filter { it.date in weekStart..weekEnd }
                        .sumOf { it.amount }

                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(110.dp)) {
                        AmountCell(label = "Given", value = "₹${person.amountGiven}", color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        AmountCell(
                            label = "This Week",
                            value = if (thisWeekReturn == 0.0) "-" else "₹$thisWeekReturn",
                            color = if (thisWeekReturn == 0.0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(Modifier.height(4.dp))
                        AmountCell(
                            label = "Total Paid",
                            value = if (totalPaidAllTime == 0.0) "₹0" else "₹$totalPaidAllTime",
                            color = if (totalPaidAllTime >= person.amountGiven) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            bold = true
                        )
                    }
                } else {
                    val pendingTotal = person.amountGiven - totalPaidAllTime
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(110.dp)) {
                        AmountCell(label = "Given", value = "₹${person.amountGiven}", color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        AmountCell(
                            label = "Total Paid",
                            value = if (totalPaidAllTime == 0.0) "₹0" else "₹$totalPaidAllTime",
                            color = if (totalPaidAllTime == 0.0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(Modifier.height(4.dp))
                        AmountCell(
                            label = "Pending",
                            value = if (pendingTotal <= 0.0) "✓ Clear" else "₹$pendingTotal",
                            color = if (pendingTotal <= 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            bold = true
                        )
                    }
                }
            }

            if (!isSelecting) {
                Spacer(Modifier.width(4.dp))
                var showPersonMenu by remember { mutableStateOf(false) }
                var editButtonPressed by remember { mutableStateOf(false) }
                val editButtonScale by animateFloatAsState(
                    targetValue = if (editButtonPressed) 1.6f else 1f,
                    animationSpec = tween(durationMillis = 300),
                    label = "editScale",
                    finishedListener = { scale ->
                        if (scale >= 1.55f) {
                            editButtonPressed = false
                            onEdit()
                        }
                    }
                )
                Box {
                    IconButton(onClick = { showPersonMenu = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = showPersonMenu, onDismissRequest = { showPersonMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Call Now") },
                            leadingIcon = { Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = { showPersonMenu = false; onCallNow() }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.scale(editButtonScale))
                            },
                            onClick = { showPersonMenu = false; editButtonPressed = true }
                        )
                        DropdownMenuItem(
                            text = { Text("View by Date") },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                            onClick = { showPersonMenu = false; onView() }
                        )
                        if (isFullyPaid) {
                            DropdownMenuItem(
                                text = { Text("Mark Complete") },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = { showPersonMenu = false; onMarkComplete() }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { showPersonMenu = false; onDelete() }
                        )
                    }
                }
            }
        }
    }
}

// ── AmountCell ────────────────────────────────────────────────────────────────

@Composable
fun AmountCell(label: String, value: String, color: Color, bold: Boolean = false) {
    Column(horizontalAlignment = Alignment.End) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
            color = color)
    }
}
