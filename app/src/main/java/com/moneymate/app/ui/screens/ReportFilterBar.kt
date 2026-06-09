package com.moneymate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.moneymate.app.data.local.entity.LoanFile
import com.moneymate.app.data.export.ReportExportData
import com.moneymate.app.ui.viewmodel.ExportFormat
import com.moneymate.app.ui.viewmodel.ExportState
import com.moneymate.app.ui.viewmodel.ExportViewModel
import com.moneymate.app.ui.viewmodel.ShareTarget
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFilterBar(
    files: List<LoanFile>,
    selectedFileId: String?,
    fromDate: Long,
    toDate: Long,
    showDateFilter: Boolean = true,
    showFileFilter: Boolean = true,
    onFileSelected: (String?) -> Unit = {},
    onFromDateChanged: (Long) -> Unit = {},
    onToDateChanged: (Long) -> Unit = {},
    onDownloadClick: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showFileFilter && files.isNotEmpty()) {
                var expanded by remember { mutableStateOf(false) }
                val selectedName = files.find { it.id == selectedFileId }?.name ?: "All Files"

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = selectedName,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(
                            text = { Text("All Files") },
                            onClick = { onFileSelected(null); expanded = false }
                        )
                        files.forEach { file ->
                            DropdownMenuItem(
                                text = { Text(file.name) },
                                onClick = { onFileSelected(file.id); expanded = false }
                            )
                        }
                    }
                }
            }

            if (onDownloadClick != null) {
                IconButton(onClick = onDownloadClick) {
                    Icon(Icons.Default.Download, contentDescription = "Download")
                }
            }
        }

        if (showDateFilter) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateField(
                    value = fromDate,
                    onValueChange = onFromDateChanged,
                    label = "FROM",
                    modifier = Modifier.weight(1f)
                )
                DateField(
                    value = toDate,
                    onValueChange = onToDateChanged,
                    label = "TO",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    value: Long,
    onValueChange: (Long) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    OutlinedTextField(
        value = dateFormat.format(Date(value)),
        onValueChange = {},
        readOnly = true,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        trailingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = "Pick date") },
        modifier = modifier,
        textStyle = MaterialTheme.typography.bodySmall
    )

    if (showPicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = value)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { onValueChange(it) }
                    showPicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

fun formatReportDate(ms: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(ms))
}

fun formatCurrency(amount: Double): String {
    return "₹%.2f".format(amount)
}
