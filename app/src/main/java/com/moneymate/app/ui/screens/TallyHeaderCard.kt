package com.moneymate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun TallyHeaderCard(
    expectedTotal: Double,
    cashCollected: Double,
    upiCollected: Double,
    pendingTotal: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TallyColumn("Expected", "₹${expectedTotal.toInt()}", MaterialTheme.colorScheme.onSurfaceVariant)
            TallyColumn("Cash", "₹${cashCollected.toInt()}", MaterialTheme.colorScheme.primary)
            TallyColumn("UPI", "₹${upiCollected.toInt()}", MaterialTheme.colorScheme.tertiary)
            
            val pendingColor = if (pendingTotal > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            TallyColumn("Pending", "₹${pendingTotal.toInt()}", pendingColor)
        }
    }
}

@Composable
private fun TallyColumn(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}
