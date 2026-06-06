package com.moneymate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.moneymate.app.data.local.entity.LoanFile
import com.moneymate.app.data.local.entity.Payment
import com.moneymate.app.data.local.entity.PaymentMode
import com.moneymate.app.data.local.entity.Person
import com.moneymate.app.ui.viewmodel.PaymentViewModel
import com.moneymate.app.ui.viewmodel.PersonViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/** A single transaction row in the View by Date breakdown list. */
data class ViewTransaction(
    val id: String,
    val personId: String,
    val personName: String,
    val date: Long,
    val type: String,   // "Given" or "Received"
    val amount: Double
)

/**
 * Self-contained View by Date flow:
 * 1. Shows a start-date picker dialog
 * 2. Shows an end-date picker dialog
 * 3. Shows a bottom-sheet result with totals and a flat transaction list
 *
 * All three steps are handled inside this composable via internal state so the
 * caller only needs to toggle [show] to start the flow and receives [onDismiss]
 * when the user closes everything.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewByDateFlow(
    show: Boolean,
    fileId: String,
    persons: List<Person>,
    completedPersons: List<Person>,
    pendingNewLoanPersons: List<Person>,
    filePaymentsAll: List<Payment>,
    personViewModel: PersonViewModel,
    paymentViewModel: PaymentViewModel,
    paidByPerson: Map<String, Double>,
    file: LoanFile?,
    coroutineScope: CoroutineScope,
    onDismiss: () -> Unit
) {
    if (!show) return

    // ── Internal state ──────────────────────────────────────────────────────────
    var step by remember { mutableStateOf("START_DATE") } // "START_DATE" | "END_DATE" | "RESULT"
    var startDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(System.currentTimeMillis()) }

    // Add-payment inside-sheet state
    var addPaymentPerson by remember { mutableStateOf<Person?>(null) }
    var addPaymentAmount by remember { mutableStateOf("") }
    var addPaymentMode by remember { mutableStateOf(PaymentMode.CASH) }
    var addPaymentType by remember { mutableStateOf("RECEIVED") }

    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    // ── Step 1: Start Date Picker ──────────────────────────────────────────────
    if (step == "START_DATE") {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { onDismiss() },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { startDate = it }
                    step = "END_DATE"
                }) { Text("Select Start →") }
            },
            dismissButton = { TextButton(onClick = { onDismiss() }) { Text("Cancel") } }
        ) { DatePicker(state = dpState) }
    }

    // ── Step 2: End Date Picker ────────────────────────────────────────────────
    if (step == "END_DATE") {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { step = "START_DATE"; onDismiss() },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { endDate = it }
                    step = "RESULT"
                }) { Text("View Range") }
            },
            dismissButton = { TextButton(onClick = { onDismiss() }) { Text("Cancel") } }
        ) { DatePicker(state = dpState) }
    }

    // ── Step 3: Result Bottom Sheet ────────────────────────────────────────────
    if (step == "RESULT") {
        val rangeStart = remember(startDate) {
            Calendar.getInstance().also {
                it.timeInMillis = startDate
                it.set(Calendar.HOUR_OF_DAY, 0)
                it.set(Calendar.MINUTE, 0)
                it.set(Calendar.SECOND, 0)
                it.set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        val rangeEnd = remember(endDate) {
            Calendar.getInstance().also {
                it.timeInMillis = endDate
                it.set(Calendar.HOUR_OF_DAY, 23)
                it.set(Calendar.MINUTE, 59)
                it.set(Calendar.SECOND, 59)
                it.set(Calendar.MILLISECOND, 999)
            }.timeInMillis
        }

        // ── Build the lookup: completed-person ID → active row ID ────────────────
        val completedIdToActiveId: Map<String, String> = remember(completedPersons, pendingNewLoanPersons, persons) {
            val result = mutableMapOf<String, String>()
            pendingNewLoanPersons.forEach { placeholder ->
                val prevId = placeholder.previousPersonId
                if (prevId != null) result[prevId] = placeholder.id
            }
            completedPersons.forEach { comp ->
                val linkedId = comp.linkedNewPersonId
                if (linkedId != null && !result.containsKey(comp.id)) result[comp.id] = linkedId
            }
            // Walk previousPersonId chain for multi-cycle
            val allActiveIds = (persons + pendingNewLoanPersons).map { it.id }.toSet()
            fun resolve(id: String, depth: Int = 0): String? {
                if (depth > 20) return null
                if (id in allActiveIds) return id
                val next = result[id] ?: return null
                return resolve(next, depth + 1)
            }
            completedPersons.forEach { comp ->
                if (!result.containsKey(comp.id)) {
                    resolve(comp.id)?.let { result[comp.id] = it }
                }
            }
            result
        }

        // ── Build a person lookup by ID (for the add-payment button) ────────────
        val personById: Map<String, Person> = remember(persons, completedPersons, pendingNewLoanPersons) {
            (persons + completedPersons + pendingNewLoanPersons).associateBy { it.id }
        }

        // ── Build the flat transaction list ─────────────────────────────────────
        val transactions: List<ViewTransaction> = remember(
            persons, completedPersons, pendingNewLoanPersons,
            filePaymentsAll, rangeStart, rangeEnd, completedIdToActiveId
        ) {
            val list = mutableListOf<ViewTransaction>()

            // All persons (active + completed) for Given transactions
            val allPersonsPool = persons + completedPersons + pendingNewLoanPersons.filter { p ->
                p.name.lowercase() !in persons.map { it.name.lowercase() }.toSet()
            }

            // Map personId → name for received transactions
            val personNameById = allPersonsPool.associate { it.id to it.name }

            // Given transactions: loans created within the date range
            allPersonsPool.forEach { p ->
                // Skip zero-amount and pending new loan 
                if (p.amountGiven <= 0.0) return@forEach
                val effectiveDateGiven = if (p.isPendingNewLoan && p.previousPersonId != null) {
                    completedPersons.firstOrNull { it.id == p.previousPersonId }?.dateGiven ?: p.dateGiven
                } else p.dateGiven

                if (effectiveDateGiven in rangeStart..rangeEnd) {
                    val effectiveAmount = if (p.isPendingNewLoan && p.previousPersonId != null) {
                        completedPersons.firstOrNull { it.id == p.previousPersonId }?.amountGiven ?: p.amountGiven
                    } else p.amountGiven

                    list.add(
                        ViewTransaction(
                            id = "given_${p.id}",
                            personId = p.id,
                            personName = p.name,
                            date = effectiveDateGiven,
                            type = "Given",
                            amount = effectiveAmount
                        )
                    )
                }
            }

            // Received transactions: payments received within the date range
            filePaymentsAll
                .filter { it.date in rangeStart..rangeEnd && !it.isDeleted }
                .forEach { payment ->
                    val ownerId = completedIdToActiveId[payment.personId] ?: payment.personId
                    val ownerName = personNameById[ownerId] ?: personNameById[payment.personId] ?: "Unknown"
                    list.add(
                        ViewTransaction(
                            id = "recv_${payment.id}",
                            personId = ownerId,
                            personName = ownerName,
                            date = payment.date,
                            type = "Received",
                            amount = payment.amount
                        )
                    )
                }

            // Sort chronologically by date, then given before received
            list.sortedWith(compareBy<ViewTransaction> { it.date }.thenBy { if (it.type == "Given") 0 else 1 })
        }

        // ── Compute totals ──────────────────────────────────────────────────────
        val totalGiven = remember(transactions) {
            transactions.filter { it.type == "Given" }.sumOf { it.amount }
        }
        val totalReceived = remember(transactions) {
            transactions.filter { it.type == "Received" }.sumOf { it.amount }
        }
        val netAmount = totalGiven - totalReceived

        val nameDateFmt = remember { SimpleDateFormat("dd MMM yy", Locale.getDefault()) }

        // Keep old-style sheet variables for the add-payment dialog
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            sheetState = sheetState
        ) {
            Column(Modifier.fillMaxWidth()) {
                // Header
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "View: ${file?.name ?: "File"}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${dateFmt.format(Date(startDate))} — ${dateFmt.format(Date(endDate))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { onDismiss() }) {
                        Text("Close")
                    }
                }

                HorizontalDivider()

                // ── Totals Card ──────────────────────────────────────────────────
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Given", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                if (totalGiven > 0) "₹${formatAmount(totalGiven)}" else "Nil",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Received", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                if (totalReceived > 0) "₹${formatAmount(totalReceived)}" else "Nil",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Net", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                if (netAmount >= 0) "₹${formatAmount(netAmount)}" else "-₹${formatAmount(-netAmount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (netAmount > 0) MaterialTheme.colorScheme.primary
                                else if (netAmount < 0) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Column headers
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Name", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Date", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(60.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Type", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(52.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Amount", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(70.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                HorizontalDivider()

                // ── Transaction List ─────────────────────────────────────────────
                LazyColumn(
                    Modifier.fillMaxWidth().fillMaxHeight(0.7f),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    itemsIndexed(transactions, key = { _, t -> t.id }) { idx, tx ->
                        val isGiven = tx.type == "Given"
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isGiven) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.10f)
                                    else Color.Transparent
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${idx + 1}", style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.width(24.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(tx.personName, style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            Text(nameDateFmt.format(Date(tx.date)),
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.width(60.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                tx.type,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(48.dp),
                                color = if (isGiven) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.tertiary
                            )
                            Text(
                                "₹${formatAmount(tx.amount)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.width(64.dp),
                                color = if (isGiven) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.tertiary
                            )
                            // Add-payment button for this person
                            IconButton(
                                onClick = {
                                    personById[tx.personId]?.let { person ->
                                        addPaymentPerson = person
                                        addPaymentAmount = ""
                                        addPaymentMode = PaymentMode.CASH
                                        addPaymentType = "RECEIVED"
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add, "Add Payment",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }

        // ── Add Payment Dialog (inside sheet) ─────────────────────────────────
        addPaymentPerson?.let { p ->
            AlertDialog(
                onDismissRequest = { addPaymentPerson = null; addPaymentAmount = "" },
                title = { Text("Add Entry for ${p.name}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Date: ${dateFmt.format(Date(startDate))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = addPaymentType == "GIVEN",
                                onClick = { addPaymentType = "GIVEN" },
                                label = { Text("Given (I gave)") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = if (addPaymentType == "GIVEN") {
                                    { Icon(Icons.Default.ArrowUpward, null, Modifier.size(14.dp)) }
                                } else null
                            )
                            FilterChip(
                                selected = addPaymentType == "RECEIVED",
                                onClick = { addPaymentType = "RECEIVED" },
                                label = { Text("Received") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = if (addPaymentType == "RECEIVED") {
                                    { Icon(Icons.Default.ArrowDownward, null, Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                        HorizontalDivider()
                        if (addPaymentType == "GIVEN") {
                            Text(
                                "This will ADD to ${p.name}'s given amount (current: ₹${p.amountGiven}).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = addPaymentAmount,
                                onValueChange = { addPaymentAmount = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Amount Given*") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                leadingIcon = { Icon(Icons.Default.ArrowUpward, null, tint = MaterialTheme.colorScheme.primary) }
                            )
                        } else {
                            Text(
                                "This will record a payment received from ${p.name} and reflect in their full record.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = addPaymentAmount,
                                onValueChange = { addPaymentAmount = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Amount Received*") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                leadingIcon = { Icon(Icons.Default.ArrowDownward, null, tint = MaterialTheme.colorScheme.tertiary) }
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = addPaymentMode == PaymentMode.CASH,
                                    onClick = { addPaymentMode = PaymentMode.CASH },
                                    label = { Text("Cash") }
                                )
                                FilterChip(
                                    selected = addPaymentMode == PaymentMode.UPI,
                                    onClick = { addPaymentMode = PaymentMode.UPI },
                                    label = { Text("UPI") }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val amt = addPaymentAmount.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            coroutineScope.launch {
                                if (addPaymentType == "GIVEN") {
                                    personViewModel.updatePerson(p.copy(amountGiven = p.amountGiven + amt))
                                } else {
                                    val newPayment = Payment(
                                        personId = p.id,
                                        amount = amt,
                                        mode = addPaymentMode,
                                        date = startDate
                                    )
                                    paymentViewModel.insertPaymentAwait(newPayment)
                                    // Auto-complete if balance reaches zero
                                    val paidTotal = (paidByPerson[p.id] ?: 0.0) + amt
                                    if (paidTotal >= p.amountGiven && p.amountGiven > 0) {
                                        personViewModel.markAsCompleted(p)
                                    }
                                }
                            }
                            addPaymentPerson = null
                            addPaymentAmount = ""
                        }
                    }) {
                        Text(if (addPaymentType == "GIVEN") "Add Given" else "Add Received")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { addPaymentPerson = null; addPaymentAmount = "" }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

/** Remove trailing zero decimals for cleaner display: 1000.0 → "1000", 500.5 → "500.5" */
private fun formatAmount(v: Double): String {
    val s = String.format(Locale.US, "%.2f", v)
    return if (s.endsWith(".00")) s.substring(0, s.length - 3) else s
}
