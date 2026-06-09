package com.moneymate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.moneymate.app.notifications.workers.LoanReminderWorker
import com.moneymate.app.utils.AppPreferences
import com.moneymate.app.utils.isReminderSet
import com.moneymate.app.utils.setReminderState
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetLoanReminderSheet(
    personId: String,
    personName: String,
    defaultAmount: Double,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val appPrefs = remember { AppPreferences(context) }

    // Default: tomorrow at 09:00 AM
    val defaultDate = remember {
        Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 9); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
    }
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    var selectedDate by remember { mutableStateOf(defaultDate.timeInMillis) }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val isReminderSet = appPrefs.isReminderSet(personId)

    // Time picker
    var showTimePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showTimePicker) {
        val cal = Calendar.getInstance().apply { timeInMillis = selectedDate }
        android.app.TimePickerDialog(
            context,
            { _, hour, minute ->
                val newCal = Calendar.getInstance().apply { timeInMillis = selectedDate }
                newCal.set(Calendar.HOUR_OF_DAY, hour)
                newCal.set(Calendar.MINUTE, minute)
                newCal.set(Calendar.SECOND, 0)
                newCal.set(Calendar.MILLISECOND, 0)
                selectedDate = newCal.timeInMillis
                showTimePicker = false
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            false
        ).show()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Set Reminder for $personName",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Date picker row
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Date: ${dateFormat.format(Date(selectedDate))}")
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
            }

            // Time picker row
            OutlinedButton(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Schedule, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Time: ${timeFormat.format(Date(selectedDate))}")
                Spacer(Modifier.weight(1f))
                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
            }

            // Notes field
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            // Error message
            errorMessage?.let {
                Text(it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Set Reminder button
            Button(
                onClick = {
                    val now = System.currentTimeMillis()
                    val delay = selectedDate - now
                    if (delay <= 0) {
                        errorMessage = "Please select a future time"
                        return@Button
                    }
                    errorMessage = null

                    val inputData = LoanReminderWorker.createInputData(
                        personId = personId,
                        personName = personName,
                        amount = defaultAmount,
                        notes = notes.ifBlank { null }
                    )

                    val request = OneTimeWorkRequestBuilder<LoanReminderWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .addTag("loan_reminder_$personId")
                        .setInputData(inputData)
                        .build()

                    WorkManager.getInstance(context).enqueueUniqueWork(
                        "loan_reminder_$personId",
                        ExistingWorkPolicy.REPLACE,
                        request
                    )

                    appPrefs.setReminderState(personId, true)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Notifications, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Set Reminder")
            }

            // Clear Reminder button (only if reminder is set)
            if (isReminderSet) {
                OutlinedButton(
                    onClick = {
                        WorkManager.getInstance(context).cancelUniqueWork("loan_reminder_$personId")
                        appPrefs.setReminderState(personId, false)
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.NotificationsOff, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Clear Reminder")
                }
            }
        }
    }
}
