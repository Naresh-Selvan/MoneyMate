package com.moneymate.app.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import com.moneymate.app.data.local.entity.Payment
import com.moneymate.app.data.local.entity.PaymentMode
import com.moneymate.app.data.local.entity.Person
import org.burnoutcrew.reorderable.ReorderableLazyListState
import java.text.SimpleDateFormat

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
    onCallNow: () -> Unit = {},
    onQuickPayment: (amount: Double, mode: PaymentMode) -> Unit
) {
    OverduePersonCard(
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
        onView = onView,
        onCallNow = onCallNow
    )
}