package com.moneymate.app.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.moneymate.app.data.local.entity.LoanType
import com.moneymate.app.data.local.entity.PaymentMode
import com.moneymate.app.data.local.entity.Person
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Scrollable bottom-sheet dialog for adding or editing a Person.
 * Organised into sections: Basic Info, Contact, Loan Defaults, Profile.
 *
 * @param mode ADD or EDIT
 * @param existingPerson  non-null when editing
 * @param fileId          the current file
 * @param allPersonsInFile  list of active persons in the file (for guarantor picker)
 * @param personViewModel  ViewModel for DB writes + duplicate checks
 * @param isSmsPermissionGranted whether SEND_SMS was already granted
 * @param onDismiss dismiss callback
 * @param onSaved called with the final Person and whether it's a new insert
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPersonDialog(
    mode: DialogMode,
    existingPerson: Person? = null,
    fileId: String,
    allPersonsInFile: List<Person>,
    personViewModel: com.moneymate.app.ui.viewmodel.PersonViewModel,
    isSmsPermissionGranted: Boolean = false,
    areaNames: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSaved: (person: Person, isNew: Boolean) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEdit = mode == DialogMode.EDIT
    val defaultDate = System.currentTimeMillis()

    // ── Form state ─────────────────────────────────────────────────────────
    var name by remember { mutableStateOf(existingPerson?.name ?: "") }
    var customerCode by remember { mutableStateOf(existingPerson?.customerCode ?: "") }
    var subCode by remember { mutableStateOf(existingPerson?.subCode ?: "") }
    var sortOrderText by remember { mutableStateOf(existingPerson?.sortOrder?.toString() ?: "") }
    var place by remember { mutableStateOf(existingPerson?.place ?: "") }
    var mobileNumber by remember { mutableStateOf(existingPerson?.mobileNumber ?: "") }
    var alternateMobile by remember { mutableStateOf(existingPerson?.alternateMobile ?: "") }
    var sendSms by remember { mutableStateOf(existingPerson?.sendSms ?: false) }
    var amountGivenText by remember {
        mutableStateOf(
            if (isEdit) existingPerson?.amountGiven?.toBigDecimal()?.stripTrailingZeros()?.toPlainString() ?: ""
            else ""
        )
    }
    var modeState by remember { mutableStateOf(existingPerson?.mode ?: PaymentMode.CASH) }
    var recordType by remember { mutableStateOf(existingPerson?.recordType ?: LoanType.LENDING) }
    var dateGiven by remember { mutableStateOf(existingPerson?.dateGiven ?: defaultDate) }
    var interestRateText by remember {
        mutableStateOf(
            (existingPerson?.interestRate ?: 0.0).toBigDecimal().stripTrailingZeros().toPlainString()
        )
    }
    var loanTypeText by remember { mutableStateOf(existingPerson?.loanType ?: "MONTHLY") }
    var numberOfInstallmentsText by remember {
        mutableStateOf((existingPerson?.numberOfInstallments ?: 10).toString())
    }
    var badLoanDaysText by remember {
        mutableStateOf((existingPerson?.badLoanDays ?: 90).toString())
    }
    var maxLoanAmountText by remember {
        mutableStateOf(
            existingPerson?.maxLoanAmount?.toBigDecimal()?.stripTrailingZeros()?.toPlainString() ?: ""
        )
    }
    var businessType by remember { mutableStateOf(existingPerson?.businessType ?: "") }
    var address by remember { mutableStateOf(existingPerson?.address ?: "") }
    var photoUri by remember { mutableStateOf(existingPerson?.photoUri ?: "") }
    var guarantorPersonId by remember { mutableStateOf(existingPerson?.guarantorPersonId ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showGuarantorSheet by remember { mutableStateOf(false) }
    var guarantorSearchQuery by remember { mutableStateOf("") }

    // ── Validation errors ──────────────────────────────────────────────────
    var nameError by remember { mutableStateOf(false) }
    var mobileError by remember { mutableStateOf(false) }
    var altMobileError by remember { mutableStateOf(false) }
    var codeDuplicateError by remember { mutableStateOf(false) }

    // ── Photo / Camera ─────────────────────────────────────────────────────
    val photosDir = File(context.filesDir, "photos").also { it.mkdirs() }
    val photoFile = File(photosDir, "person_${UUID.randomUUID()}.jpg")
    val photoUriForCamera = androidx.core.content.FileProvider.getUriForFile(
        context,
        "${context.packageName}.photo_provider",
        photoFile
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            photoUri = photoFile.absolutePath
        }
    }

    // ── SMS permission launcher ────────────────────────────────────────────
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            sendSms = false
            Toast.makeText(context, "SMS permission denied. Toggle disabled.", Toast.LENGTH_SHORT).show()
        }
    }

    fun toggleSendSms() {
        if (sendSms) {
            // Turning off — no permission needed
            sendSms = false
        } else {
            if (context.checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                sendSms = true
            } else {
                smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
            }
        }
    }

    // ── Resolve guarantor name ─────────────────────────────────────────────
    fun guarantorName(): String? {
        if (guarantorPersonId.isBlank()) return null
        return allPersonsInFile.find { it.id == guarantorPersonId }?.name
    }

    // ── Candidate persons for guarantor picker ─────────────────────────────
    val guarantorCandidates = remember(allPersonsInFile, guarantorSearchQuery) {
        allPersonsInFile
            .filter { it.id != existingPerson?.id } // exclude self
            .filter { !it.isCompleted && !it.isDeleted }
            .filter { p ->
                guarantorSearchQuery.isBlank() ||
                        p.name.contains(guarantorSearchQuery, ignoreCase = true) ||
                        p.place?.contains(guarantorSearchQuery, ignoreCase = true) == true
            }
    }

    // ── Init photo display ─────────────────────────────────────────────────
    val photoBitmap = remember(photoUri) {
        if (photoUri.isNotBlank()) {
            try {
                BitmapFactory.decodeFile(photoUri)
            } catch (e: Exception) { null }
        } else null
    }

    val initials = remember(name) {
        name.trim().split("\\s+".toRegex()).take(2).joinToString("") { it.firstOrNull()?.uppercase() ?: "" }
    }

    fun validate(): Boolean {
        nameError = name.isBlank()
        mobileError = mobileNumber.isNotBlank() && mobileNumber.filter { it.isDigit() }.length != 10
        altMobileError = alternateMobile.isNotBlank() && alternateMobile.filter { it.isDigit() }.length != 10

        // customerCode uniqueness check
        codeDuplicateError = false
        if (customerCode.isNotBlank()) {
            val existing = allPersonsInFile.filter {
                it.customerCode?.equals(customerCode, ignoreCase = true) == true &&
                        it.id != existingPerson?.id
            }
            codeDuplicateError = existing.isNotEmpty()
        }

        return !nameError && !mobileError && !altMobileError && !codeDuplicateError
    }

    fun buildPerson(): Person? {
        if (!validate()) return null
        val amount = amountGivenText.toDoubleOrNull() ?: if (isEdit) existingPerson!!.amountGiven else return null

        return (existingPerson?.copy() ?: Person(
            id = UUID.randomUUID().toString(),
            fileId = fileId,
            name = name.trim(),
            amountGiven = amount
        )).copy(
            name = name.trim(),
            customerCode = customerCode.trim().ifEmpty { null },
            subCode = subCode.trim().ifEmpty { null },
            sortOrder = sortOrderText.toIntOrNull() ?: existingPerson?.sortOrder ?: 0,
            place = place.trim().ifEmpty { null },
            mobileNumber = mobileNumber.trim().ifEmpty { null },
            alternateMobile = alternateMobile.trim().ifEmpty { null },
            sendSms = sendSms,
            amountGiven = amount,
            mode = modeState,
            recordType = recordType,
            dateGiven = dateGiven,
            interestRate = interestRateText.toDoubleOrNull() ?: existingPerson?.interestRate ?: 0.0,
            loanType = loanTypeText,
            numberOfInstallments = numberOfInstallmentsText.toIntOrNull() ?: existingPerson?.numberOfInstallments ?: 10,
            badLoanDays = badLoanDaysText.toIntOrNull() ?: 90,
            maxLoanAmount = maxLoanAmountText.toDoubleOrNull(),
            businessType = businessType.trim().ifEmpty { null },
            address = address.trim().ifEmpty { null },
            photoUri = photoUri.ifBlank { null },
            guarantorPersonId = guarantorPersonId.ifBlank { null }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Title ──────────────────────────────────────────────────────
            Text(
                if (isEdit) "Edit ${existingPerson?.name}" else "Add New Person",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            HorizontalDivider()

            // ═══════════════════════════════════════════════════════════════════
            // SECTION 1: Basic Info
            // ═══════════════════════════════════════════════════════════════════
            Text("Basic Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text("Name *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = nameError,
                supportingText = if (nameError) {{ Text("Name is required") }} else null
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = customerCode,
                    onValueChange = { customerCode = it; codeDuplicateError = false },
                    label = { Text("Customer Code") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    isError = codeDuplicateError,
                    supportingText = if (codeDuplicateError) {{ Text("Code already in use") }} else null
                )
                OutlinedTextField(
                    value = subCode,
                    onValueChange = { subCode = it },
                    label = { Text("Sub Code") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = sortOrderText,
                    onValueChange = { sortOrderText = it.filter { c -> c.isDigit() } },
                    label = { Text("Order") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                // Mode: CASH / UPI
                var expandedMode by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedMode,
                    onExpandedChange = { expandedMode = !expandedMode },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = modeState.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mode") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMode) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandedMode, onDismissRequest = { expandedMode = false }) {
                        PaymentMode.entries.forEach {
                            DropdownMenuItem(text = { Text(it.name) }, onClick = { modeState = it; expandedMode = false })
                        }
                    }
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = recordType == LoanType.LENDING,
                    onClick = { recordType = LoanType.LENDING },
                    label = { Text("Lending") }
                )
                FilterChip(
                    selected = recordType == LoanType.BORROWING,
                    onClick = { recordType = LoanType.BORROWING },
                    label = { Text("Borrowing") }
                )
            }

            // ═══════════════════════════════════════════════════════════════════
            // SECTION 2: Contact
            // ═══════════════════════════════════════════════════════════════════
            HorizontalDivider()
            Text("Contact", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it.filter { c -> c.isDigit() }; mobileError = false },
                label = { Text("Mobile Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = mobileError,
                supportingText = if (mobileError) {{ Text("Must be 10 digits") }} else null
            )

            OutlinedTextField(
                value = alternateMobile,
                onValueChange = { alternateMobile = it.filter { c -> c.isDigit() }; altMobileError = false },
                label = { Text("Alternate Mobile") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                isError = altMobileError,
                supportingText = if (altMobileError) {{ Text("Must be 10 digits") }} else null
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Switch(checked = sendSms, onCheckedChange = { toggleSendSms() })
                Column {
                    Text("Send SMS on Payment", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Sends payment confirmation via SMS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ═══════════════════════════════════════════════════════════════════
            // SECTION 3: Loan Defaults
            // ═══════════════════════════════════════════════════════════════════
            HorizontalDivider()
            Text("Loan Defaults", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = amountGivenText,
                onValueChange = { amountGivenText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text(if (isEdit) "Loan Amount" else "Loan Amount *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                leadingIcon = { Text("₹", modifier = Modifier.padding(start = 8.dp)) }
            )

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = interestRateText,
                    onValueChange = { interestRateText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Interest Rate (%)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value = badLoanDaysText,
                    onValueChange = { badLoanDaysText = it.filter { c -> c.isDigit() } },
                    label = { Text("Bad Loan Days") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = maxLoanAmountText,
                    onValueChange = { maxLoanAmountText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Max Loan Amount") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Text("₹", modifier = Modifier.padding(start = 8.dp)) }
                )
                // Loan type dropdown
                var expandedLoanType by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedLoanType,
                    onExpandedChange = { expandedLoanType = !expandedLoanType },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = loanTypeText,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Loan Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLoanType) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandedLoanType, onDismissRequest = { expandedLoanType = false }) {
                        listOf("DAILY", "WEEKLY", "MONTHLY").forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = { loanTypeText = it; expandedLoanType = false })
                        }
                    }
                }
            }

            OutlinedTextField(
                value = numberOfInstallmentsText,
                onValueChange = { numberOfInstallmentsText = it.filter { c -> c.isDigit() } },
                label = { Text("Number of Installments") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // Date picker
            val dateFmt = remember { java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()) }
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Date: ${dateFmt.format(java.util.Date(dateGiven))}")
            }

            // ═══════════════════════════════════════════════════════════════════
            // SECTION 4: Profile
            // ═══════════════════════════════════════════════════════════════════
            HorizontalDivider()
            Text("Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)

            OutlinedTextField(
                value = businessType,
                onValueChange = { businessType = it },
                label = { Text("Business Type") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // ── Area dropdown (from managed areas) ───────────────────────────
            var areaDropdownExp by remember { mutableStateOf(false) }
            val areaList = remember(areaNames) { areaNames + listOf("✏️ Type custom area") }
            ExposedDropdownMenuBox(
                expanded = areaDropdownExp,
                onExpandedChange = { areaDropdownExp = it }
            ) {
                OutlinedTextField(
                    value = place,
                    onValueChange = { place = it },
                    readOnly = false,
                    label = { Text("Place / Area") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = areaDropdownExp) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = areaDropdownExp,
                    onDismissRequest = { areaDropdownExp = false }
                ) {
                    areaList.forEach { area ->
                        DropdownMenuItem(
                            text = { Text(area) },
                            onClick = {
                                if (area == "✏️ Type custom area") {
                                    // Keep current text and let user type freely
                                    areaDropdownExp = false
                                } else {
                                    place = area
                                    areaDropdownExp = false
                                }
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 3
            )

            // ── Guarantor ────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showGuarantorSheet = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PersonSearch, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (guarantorPersonId.isNotBlank()) "Guarantor: ${guarantorName() ?: "Unknown"}"
                        else "Select Guarantor"
                    )
                }
                if (guarantorPersonId.isNotBlank()) {
                    IconButton(onClick = { guarantorPersonId = ""; guarantorSearchQuery = "" }) {
                        Icon(Icons.Default.Close, "Clear guarantor", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // ── Photo ─────────────────────────────────────────────────────────
            Text("Photo", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Avatar preview
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoBitmap != null) {
                        Image(
                            bitmap = photoBitmap.asImageBitmap(),
                            contentDescription = "Photo",
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Text(
                            initials.ifEmpty { "?" },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { cameraLauncher.launch(photoUriForCamera) }
                ) {
                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Capture")
                }

                if (photoUri.isNotBlank()) {
                    FilledTonalButton(
                        onClick = {
                            photoUri = ""
                            // Delete the file
                            try { File(photoUri).delete() } catch (_: Exception) {}
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(Icons.Default.Delete, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Clear")
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Actions ──────────────────────────────────────────────────────
            Button(
                onClick = {
                    val person = buildPerson()
                    if (person != null) {
                        onSaved(person, !isEdit)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isEdit) "Save Changes" else "Add Person")
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel")
            }
        }
    }

    // ── Date picker dialog ─────────────────────────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateGiven)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateGiven = datePickerState.selectedDateMillis ?: dateGiven
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // ── Guarantor picker bottom sheet ──────────────────────────────────────
    if (showGuarantorSheet) {
        ModalBottomSheet(
            onDismissRequest = { showGuarantorSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Select Guarantor", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = guarantorSearchQuery,
                    onValueChange = { guarantorSearchQuery = it },
                    label = { Text("Search by name or place") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.Search, null) }
                )
                Spacer(Modifier.height(8.dp))

                if (guarantorCandidates.isEmpty()) {
                    Text(
                        "No eligible persons found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(Modifier.fillMaxWidth().heightIn(max = 300.dp)) {
                        items(guarantorCandidates, key = { it.id }) { p ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        guarantorPersonId = p.id
                                        showGuarantorSheet = false
                                        guarantorSearchQuery = ""
                                    }
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(p.name, fontWeight = FontWeight.Bold)
                                        if (!p.place.isNullOrEmpty()) {
                                            Text(p.place, style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        if (!p.mobileNumber.isNullOrEmpty()) {
                                            Text(p.mobileNumber!!, style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    if (p.id == guarantorPersonId) {
                                        Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class DialogMode { ADD, EDIT }
