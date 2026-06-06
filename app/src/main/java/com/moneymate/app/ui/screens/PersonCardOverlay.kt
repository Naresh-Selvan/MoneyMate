package com.moneymate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moneymate.app.data.local.entity.Payment
import com.moneymate.app.data.local.entity.Person
import com.moneymate.app.ui.theme.OverdueHighRed
import com.moneymate.app.ui.theme.OverdueLowOrange
import com.moneymate.app.ui.theme.OverdueMediumOrangeRed
import com.moneymate.app.utils.EmiScheduleEngine

/**
 * Computes overdue days for a person using EmiScheduleEngine.
 * Returns 0 if the person has no amountGiven.
 */
@Composable
fun rememberOverdueDays(person: Person, payments: List<Payment>): Int {
    return remember(person.id, payments) {
        if (person.amountGiven > 0.0) EmiScheduleEngine.getOverdueDays(person, payments) else 0
    }
}

/**
 * Returns the overdue accent color based on overdue days.
 */
fun overdueColorFor(days: Int): Color = when {
    days >= 71 -> OverdueHighRed
    days >= 31 -> OverdueMediumOrangeRed
    days >= 1  -> OverdueLowOrange
    else       -> Color.Transparent
}

/**
 * Adds a left border accent to an existing card's content.
 * Pass this modifier to the Card's modifier chain.
 */
fun Modifier.overdueBorderAccent(overdueDays: Int): Modifier = this.then(
    if (overdueDays > 0) Modifier
        .padding(start = 0.dp) // placeholder — actual border is drawn inside OverdueAccentStrip
    else Modifier
)

/**
 * A small colored strip composable for the left border accent.
 * Place this at the start of the card's Row.
 */
@Composable
fun OverdueAccentStrip(
    overdueDays: Int,
    modifier: Modifier = Modifier
) {
    if (overdueDays > 0) {
        val color = overdueColorFor(overdueDays)
        Box(
            modifier = modifier
                .width(4.dp)
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                .background(color)
        )
    }
}

/**
 * "X days overdue" badge chip to show inside the person card name row.
 */
@Composable
fun OverdueBadgeChip(overdueDays: Int) {
    if (overdueDays > 0) {
        val color = overdueColorFor(overdueDays)
        AssistChip(
            onClick = {},
            label = {
                androidx.compose.material3.Text(
                    "$overdueDays day${if (overdueDays == 1) "" else "s"} overdue",
                    style = MaterialTheme.typography.labelSmall
                )
            },
            colors = AssistChipDefaults.assistChipColors(
                containerColor = color.copy(alpha = 0.15f),
                labelColor = color
            )
        )
    }
}
