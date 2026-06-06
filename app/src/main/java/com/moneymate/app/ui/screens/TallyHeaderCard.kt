package com.moneymate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A card-style sticky header that shows a live collection tally:
 *
 *     Expected        Cash          UPI         Pending
 *     ₹10,000       ₹6,000        ₹2,000       ₹2,000
 *
 * @param expectedTotal Sum of perInstallmentAmount for all due persons (₹)
 * @param cashCollected  Sum of CASH payments logged this session (₹)
 * @param upiCollected   Sum of UPI payments logged this session (₹)
 * @param pendingTotal   expectedTotal - (cashCollected + upiCollected) — must be ≥ 0
 */
@Composable
fun TallyHeaderCard(
    expectedTotal: Double,
    cashCollected: Double,
    upiCollected: Double,
    pendingTotal: Double
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // ── Expected (neutral) ──────────────────────────────────────────
            TallyColumn(
                label = "Expected",
                amount = expectedTotal,
                amountColor = MaterialTheme.colorScheme.onSurface,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ── Cash (green tint) ───────────────────────────────────────────
            TallyColumn(
                label = "Cash",
                amount = cashCollected,
                amountColor = MaterialTheme.colorScheme.tertiary,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ── UPI (blue tint) ─────────────────────────────────────────────
            TallyColumn(
                label = "UPI",
                amount = upiCollected,
                amountColor = MaterialTheme.colorScheme.secondary,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // ── Pending (red if > 0, green if cleared) ──────────────────────
            TallyColumn(
                label = "Pending",
                amount = pendingTotal,
                amountColor = if (pendingTotal > 0.0) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary,
                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TallyColumn(
    label: String,
    amount: Double,
    amountColor: androidx.compose.ui.graphics.Color,
    labelColor: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(min = 64.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = labelColor,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = "₹${formatAmount(amount)}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = amountColor,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Formats a monetary amount without trailing zeros.
 * Examples: 10000 → "10,000", 6000.50 → "6,000.5", 2000 → "2,000"
 */
private fun formatAmount(value: Double): String {
    val whole = value.toLong()
    val cents = ((value - whole) * 100.0 + 0.5).toLong()
    return if (cents == 0L) {
        "%,d".format(whole)
    } else {
        "%,d.%02d".format(whole, cents)
    }
}
