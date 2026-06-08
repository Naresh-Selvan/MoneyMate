package com.moneymate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.moneymate.app.ui.viewmodel.PersonViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FileDetailFilterBar(
    isFiltered: Boolean,
    isViewMode: Boolean,
    filterViewStartDate: Long,
    filterViewNumWeeks: String,
    minAmount: String,
    maxAmount: String,
    filterPaymentTypeState: PersonViewModel.PaymentTypeFilterState,
    onClearViewMode: () -> Unit,
    onClearAmountFilter: () -> Unit,
    onClearPaymentTypeFilter: () -> Unit
) {
    if (isFiltered) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isViewMode) {
                val viewFmt = SimpleDateFormat("dd MMM", Locale.getDefault())
                FilterChip(selected = true, onClick = onClearViewMode,
                    label = { Text("View: ${viewFmt.format(Date(filterViewStartDate))} × ${filterViewNumWeeks}wks", style = MaterialTheme.typography.labelSmall) },
                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) })
            }
            if (minAmount.isNotBlank() || maxAmount.isNotBlank()) {
                FilterChip(selected = true,
                    onClick = onClearAmountFilter,
                    label = { Text("₹${minAmount.ifBlank { "0" }}–₹${maxAmount.ifBlank { "∞" }}", style = MaterialTheme.typography.labelSmall) },
                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) })
            }
            if (filterPaymentTypeState != PersonViewModel.PaymentTypeFilterState.ALL) {
                FilterChip(selected = true,
                    onClick = onClearPaymentTypeFilter,
                    label = { Text(filterPaymentTypeState.displayName(), style = MaterialTheme.typography.labelSmall) },
                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) })
            }
        }
    }
}
