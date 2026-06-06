package com.moneymate.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
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
import com.moneymate.app.ui.theme.OverdueHighRed
import com.moneymate.app.ui.theme.OverdueLowOrange
import com.moneymate.app.ui.theme.OverdueMediumOrangeRed
import com.moneymate.app.utils.EmiScheduleEngine
import org.burnoutcrew.reorderable.ReorderableLazyListState
import java.text.SimpleDateFormat
import java.util.*

/**
 * Identical to [PersonCard] in FileDetailScreen.kt, but with overdue-day awareness:
 * - Color-coded left border accent based on overdue days
 * - \"X days overdue\" chip badge shown next to the person name
 * - Badge/strip colors: amber (1–30d), orange-red (31–70d), red (71+)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OverduePersonCard(
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
    onCallNow: () -> Unit = {}
) {
    val isBorrowing = person.recordType == LoanType.BORROWING
    val isFullyPaid  = pending <= 0 && totalPaid > 0

    // ── Overdue computation ────────────────────────────────────────────────
    val overdueDays = remember(person.id, personPayments) {
        if (person.amountGiven > 0.0) EmiScheduleEngine.getOverdueDays(person, personPayments) else 0
    }
    val overdueColor = when {
        overdueDays >= 71 -> OverdueHighRed
        overdueDays >= 31 -> OverdueMediumOrangeRed
        overdueDays >= 1  -> OverdueLowOrange
        else -> Color.Transparent
    }

    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
        elevation = CardDefaults.cardElevation(elevation),
        colors = CardDefaults.cardColors(containerColor = when {
            isSelected  -> MaterialTheme.colorScheme.primaryContainer
            isBorrowing -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            else        -> MaterialTheme.colorScheme.surface
        })
    ) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // ── Left border accent strip ────────────────────────────────────
            if (overdueDays > 0) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(overdueColor)
                )
            }

            // ── Main card content (original PersonCard content) ─────────────
            Row(
                Modifier
                    .weight(1f)
                    .padding(start = if (overdueDays > 0) 8.dp else 12.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                        // ── Overdue badge chip ───────────────────────────────
                        if (overdueDays > 0) {
                            Spacer(Modifier.width(4.dp))
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        "$overdueDays day${if (overdueDays == 1) "" else "s"} overdue",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = overdueColor.copy(alpha = 0.15f),
                                    labelColor = overdueColor
                                )
                            )
                        }
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
                        // BUG 7: Show totalRepayment as the card amount (principal + interest)
                        val displayAmount = if (person.totalRepayment > 0) person.totalRepayment else person.amountGiven
                        Text("₹$displayAmount", fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isBorrowing) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary)
                        if (person.totalRepayment > person.amountGiven) {
                            Text(
                                "Principal: ₹${person.amountGiven} | Interest: ₹${person.interestAmount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
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
}
