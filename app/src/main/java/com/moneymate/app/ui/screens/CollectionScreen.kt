package com.moneymate.app.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.moneymate.app.data.local.entity.LoanFile
import com.moneymate.app.data.local.entity.Payment
import com.moneymate.app.data.local.entity.PaymentMode
import com.moneymate.app.data.local.entity.Person
import com.moneymate.app.ui.viewmodel.LoanFileViewModel
import com.moneymate.app.ui.viewmodel.PaymentViewModel
import com.moneymate.app.ui.viewmodel.PersonViewModel
import com.moneymate.app.utils.EmiScheduleEngine
import com.moneymate.app.utils.EmiStatus
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CollectionScreen(
    navController: NavHostController,
    personViewModel: PersonViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel(),
    loanFileViewModel: LoanFileViewModel = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy (EEE)", Locale.getDefault()) }

    // ── State ─────────────────────────────────────────────────────────────────
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedFileId by remember { mutableStateOf<String?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var fileDropdownExpanded by remember { mutableStateOf(false) }
    var skippedPersonIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    // Session tally — updated when payments are logged from this screen
    var sessionCashAmount by remember { mutableStateOf(0.0) }
    var sessionUpiAmount by remember { mutableStateOf(0.0) }

    // Pay bottom sheet state
    var paySheetPerson by remember { mutableStateOf<Person?>(null) }
    var payAmount by remember { mutableStateOf("") }
    var payMode by remember { mutableStateOf(PaymentMode.CASH) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // ── Data from ViewModels ──────────────────────────────────────────────────
    val files by loanFileViewModel.allFiles.collectAsState()
    val persons by personViewModel.persons.collectAsState()
    val filePayments by paymentViewModel.filePayments.collectAsState()

    // Load data when file is selected
    LaunchedEffect(selectedFileId) {
        selectedFileId?.let { fileId ->
            personViewModel.loadPersonsForFile(fileId)
            paymentViewModel.loadPaymentsForFile(fileId)
        }
    }

    // Active persons (not completed, not deleted, and amountGiven > 0)
    val activePersons = remember(persons) {
        persons.filter { !it.isCompleted && !it.isDeleted && it.amountGiven > 0.0 }
    }

    // Persons whose EMI is due today or overdue — based on EmiScheduleEngine
    val personsDueToday = remember(activePersons, filePayments) {
        activePersons.filter { person ->
            val personPayments = filePayments.filter { it.personId == person.id }
            val schedule = EmiScheduleEngine.generateSchedule(person, personPayments)
            schedule.any { it.status == EmiStatus.TODAY || it.status == EmiStatus.MISSED }
        }.sortedBy { it.name }
    }

    // Persons not yet skipped
    val visiblePersons = remember(personsDueToday, skippedPersonIds) {
        personsDueToday.filter { it.id !in skippedPersonIds }
    }

    // ── Computed values ───────────────────────────────────────────────────────
    val expectedAmount = remember(visiblePersons) {
        visiblePersons.sumOf { it.perInstallmentAmount }
    }

    // Build a cache: personId -> (overdueDays, isDueToday) to avoid double schedule computation
    data class PersonDueInfo(val overdueDays: Int, val isDueToday: Boolean)

    val dueInfoMap = remember(visiblePersons, filePayments) {
        visiblePersons.associate { person ->
            val personPayments = filePayments.filter { it.personId == person.id }
            val schedule = EmiScheduleEngine.generateSchedule(person, personPayments)
            val overdueDays = EmiScheduleEngine.getOverdueDays(person, personPayments)
            val isDueToday = schedule.any { it.status == EmiStatus.TODAY }
            person.id to PersonDueInfo(overdueDays, isDueToday)
        }
    }

    val totalSkipped = personsDueToday.size - visiblePersons.size

    // ══════════════════════════════════════════════════════════════════════════
    // UI
    // ══════════════════════════════════════════════════════════════════════════
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Collection", fontWeight = FontWeight.Bold)
                        if (selectedFileId != null) {
                            Text(
                                selectedFileName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // ── Step 1: Date Selector ─────────────────────────────────────────
            Text(
                "Collection Date",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(dateFormat.format(Date(selectedDate)))
            }

            Spacer(Modifier.height(16.dp))

            // ── Step 2: File (Line) Selector ─────────────────────────────────
            Text(
                "Select Line",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            ExposedDropdownMenuBox(
                expanded = fileDropdownExpanded,
                onExpandedChange = { fileDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedFileName,
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Choose a loan file…") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = fileDropdownExpanded)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = fileDropdownExpanded,
                    onDismissRequest = { fileDropdownExpanded = false }
                ) {
                    files.filter { !it.isDeleted }.forEach { file ->
                        DropdownMenuItem(
                            text = { Text(file.name) },
                            onClick = {
                                selectedFileId = file.id
                                selectedFileName = file.name
                                skippedPersonIds = emptySet()
                                sessionCashAmount = 0.0
                                sessionUpiAmount = 0.0
                                fileDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Step 3: Collection List ──────────────────────────────────────
            if (selectedFileId == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Payments,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Select a Line above to start collecting",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (personsDueToday.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No EMIs due today or overdue!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (activePersons.isNotEmpty()) {
                            Text(
                                "${activePersons.size} active persons — all caught up",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ── Sticky header: live tally ────────────────────────────
                    stickyHeader {
                        TallyHeaderCard(
                            expectedTotal = expectedAmount,
                            cashCollected = sessionCashAmount,
                            upiCollected = sessionUpiAmount,
                            pendingTotal = (expectedAmount - sessionCashAmount - sessionUpiAmount).coerceAtLeast(0.0)
                        )
                    }

                    // ── Person cards ─────────────────────────────────────────
                    items(visiblePersons, key = { it.id }) { person ->
                        val dueInfo = dueInfoMap[person.id] ?: PersonDueInfo(0, false)
                        val overdueDays = dueInfo.overdueDays
                        val isDueToday = dueInfo.isDueToday

                        CollectionPersonCard(
                            person = person,
                            overdueDays = overdueDays,
                            isDueToday = isDueToday,
                            onQuickPay = {
                                coroutineScope.launch {
                                    paymentViewModel.insertPayment(
                                        Payment(
                                            personId = person.id,
                                            amount = person.perInstallmentAmount,
                                            mode = PaymentMode.CASH,
                                            date = selectedDate
                                        )
                                    )
                                    sessionCashAmount += person.perInstallmentAmount
                                }
                            },
                            onPay = {
                                paySheetPerson = person
                                payAmount = person.perInstallmentAmount.toBigDecimal().stripTrailingZeros().toPlainString()
                                payMode = PaymentMode.CASH
                            },
                            onSkip = {
                                skippedPersonIds = skippedPersonIds + person.id
                            }
                        )
                    }

                    // Bottom spacer so last card isn't hidden behind nav bar
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }

    // ── Date Picker Dialog ────────────────────────────────────────────────────
    if (showDatePicker) {
        val dpState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = dpState)
        }
    }

    // ── Pay Bottom Sheet ──────────────────────────────────────────────────────
    if (paySheetPerson != null) {
        ModalBottomSheet(
            onDismissRequest = { paySheetPerson = null },
            sheetState = sheetState
        ) {
            val person = paySheetPerson!!
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Record Payment for",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        person.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    person.place?.let { place ->
                        Text(
                            place,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Amount field
                OutlinedTextField(
                    value = payAmount,
                    onValueChange = { payAmount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount (₹)") },
                    prefix = { Text("₹ ") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    )
                )

                // Mode toggle
                Text(
                    "Payment Mode",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(PaymentMode.CASH to "CASH", PaymentMode.UPI to "UPI").forEach { (mode, label) ->
                        FilterChip(
                            selected = payMode == mode,
                            onClick = { payMode = mode },
                            label = { Text(label, fontWeight = if (payMode == mode) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.weight(1f),
                            leadingIcon = {
                                if (payMode == mode) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        )
                    }
                }

                // Confirm button
                Button(
                    onClick = {
                        val amount = payAmount.toDoubleOrNull()
                        if (amount != null && amount > 0) {
                            coroutineScope.launch {
                                paymentViewModel.insertPayment(
                                    Payment(
                                        personId = person.id,
                                        amount = amount,
                                        mode = payMode,
                                        date = selectedDate
                                    )
                                )
                                if (payMode == PaymentMode.CASH) {
                                    sessionCashAmount += amount
                                } else {
                                    sessionUpiAmount += amount
                                }
                                paySheetPerson = null
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = payAmount.toDoubleOrNull()?.let { it > 0 } == true
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Confirm Payment", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Person Card
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun CollectionPersonCard(
    person: Person,
    overdueDays: Int,
    isDueToday: Boolean,
    onQuickPay: () -> Unit,
    onPay: () -> Unit,
    onSkip: () -> Unit
) {
    val badgeColor = when {
        isDueToday && overdueDays == 0 -> MaterialTheme.colorScheme.surfaceVariant
        overdueDays in 1..30 -> MaterialTheme.colorScheme.tertiaryContainer
        overdueDays >= 31 -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val badgeTextColor = when {
        isDueToday && overdueDays == 0 -> MaterialTheme.colorScheme.onSurfaceVariant
        overdueDays in 1..30 -> MaterialTheme.colorScheme.onTertiaryContainer
        overdueDays >= 31 -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val badgeLabel = when {
        isDueToday && overdueDays == 0 -> "Due Today"
        overdueDays == 1 -> "1 day overdue"
        else -> "$overdueDays days overdue"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Row 1: Name + place + overdue badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        person.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    person.place?.let { place ->
                        Text(
                            place,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                // Overdue badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = badgeTextColor
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            badgeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeTextColor
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Row 2: Expected EMI amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Payments,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    "Expected: ₹${
                        person.perInstallmentAmount.toBigDecimal()
                            .stripTrailingZeros().toPlainString()
                    }",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.height(12.dp))

            // Row 3: Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quick Pay — records perInstallmentAmount as CASH
                Button(
                    onClick = onQuickPay,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.FlashOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Quick Pay", style = MaterialTheme.typography.labelSmall)
                }

                // Pay — opens bottom sheet
                OutlinedButton(
                    onClick = onPay,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Icon(
                        Icons.Default.Payments,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Pay", style = MaterialTheme.typography.labelSmall)
                }

                // Skip — hides person from list
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Skip", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
