package com.moneymate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.moneymate.app.data.local.entity.DefaultPerson
import com.moneymate.app.ui.viewmodel.AuthViewModel
import com.moneymate.app.ui.viewmodel.LoanFileViewModel
import com.moneymate.app.ui.viewmodel.RestoreState
import com.moneymate.app.ui.viewmodel.RestoreViewModel
import com.moneymate.app.ui.viewmodel.SettingsViewModel
import com.moneymate.app.ui.viewmodel.TemplateViewModel
import com.moneymate.app.ui.viewmodel.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    templateViewModel: TemplateViewModel = hiltViewModel(),
    loanFileViewModel: LoanFileViewModel = hiltViewModel(),
    restoreViewModel: RestoreViewModel = hiltViewModel()
) {
    val darkMode by viewModel.darkMode.collectAsState()
    val autoDeleteDays by viewModel.autoDeleteDays.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    var pinLen by remember { mutableStateOf(authViewModel.pinLength) }
    val currentRole by authViewModel.currentRole.collectAsState()

    var showChangeAdmin by remember { mutableStateOf(false) }
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var changePinError by remember { mutableStateOf("") }
    var changePinSuccess by remember { mutableStateOf("") }

    // Template state — which NLR tab is open
    var templateTab by remember { mutableStateOf<String?>(null) }

    // Sync state
    var syncInProgress by remember { mutableStateOf(false) }
    val restoreState by restoreViewModel.restoreState.collectAsState()
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var syncResultMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── Appearance ───────────────────────────────────────────────────
            Text("Appearance", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DarkMode, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Dark Mode", fontWeight = FontWeight.Medium)
                        Text("Switch between light and dark theme", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = darkMode, onCheckedChange = { viewModel.setDarkMode(it) })
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Default Person Templates ─────────────────────────────────────
            Text("Default Person Templates", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(
                "Names auto-inserted when a new NLR file is created. Updated automatically after each upload.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))

            // Sync button — push predefined names into existing NLR files
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Sync, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Sync to Existing Files", fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Add missing predefined names into existing NLR files",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (syncInProgress) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        FilledTonalButton(
                            onClick = {
                                syncInProgress = true
                                syncResultMessage = null
                                loanFileViewModel.syncTemplateToExistingFiles { added ->
                                    syncInProgress = false
                                    syncResultMessage = if (added > 0)
                                        "Synced! $added new name${if (added == 1) "" else "s"} added to existing files."
                                    else
                                        "All files are already up to date."
                                }
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Sync Now")
                        }
                    }
                }
                syncResultMessage?.let { msg ->
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            Spacer(Modifier.height(4.dp))

            templateViewModel.nlrKeys.forEach { nlrKey ->
                NlrTemplateCard(
                    nlrKey = nlrKey,
                    viewModel = templateViewModel,
                    expanded = templateTab == nlrKey,
                    onToggle = { templateTab = if (templateTab == nlrKey) null else nlrKey }
                )
                Spacer(Modifier.height(4.dp))
            }

            Spacer(Modifier.height(8.dp))

            // ── Notifications ────────────────────────────────────────────────
            Text("Notifications", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Push Notifications", fontWeight = FontWeight.Medium)
                        Text("Get notified on payment updates", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = notificationsEnabled, onCheckedChange = { viewModel.setNotificationsEnabled(it) })
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Trash ────────────────────────────────────────────────────────
            Text("Trash", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Auto-delete after", fontWeight = FontWeight.Medium)
                            Text("Items in trash older than this will be permanently deleted", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        listOf(7, 15, 30, 60).forEach { days ->
                            FilterChip(selected = autoDeleteDays == days, onClick = { viewModel.setAutoDeleteDays(days) }, label = { Text("${days}d") })
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // ── Security ─────────────────────────────────────────────────────
            if (currentRole != UserRole.USER) {
                Text("Security", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)

                var biometricEnabled by remember { mutableStateOf(authViewModel.biometricEnabled) }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Fingerprint, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Fingerprint Login", fontWeight = FontWeight.Medium)
                            Text("Use fingerprint to unlock the app", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = {
                                biometricEnabled = it
                                authViewModel.biometricEnabled = it
                            }
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                var showPinLength by remember { mutableStateOf(false) }
                var selectedNewLen by remember { mutableStateOf(pinLen) }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Pin, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text("PIN Length", fontWeight = FontWeight.Medium)
                                Text("Currently: $pinLen digits", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { showPinLength = !showPinLength; selectedNewLen = pinLen }) {
                                Icon(if (showPinLength) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                            }
                        }
                        if (showPinLength) {
                            Text("Switch PIN length for both Admin and Boss PINs.\nNote: You will need to reset both PINs after changing length.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                listOf(4, 6).forEach { len ->
                                    FilterChip(
                                        selected = selectedNewLen == len,
                                        onClick = { selectedNewLen = len },
                                        label = { Text("$len digits", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(4.dp)) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            Button(
                                onClick = { authViewModel.changePinLength(selectedNewLen); pinLen = selectedNewLen; showPinLength = false; changePinError = ""; changePinSuccess = "PIN length updated to $selectedNewLen digits. Please reset your PINs." },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = selectedNewLen != pinLen
                            ) { Text("Apply PIN Length") }
                            if (changePinSuccess.isNotEmpty()) Text(changePinSuccess, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) { Text("Change Admin PIN", fontWeight = FontWeight.Medium) }
                            IconButton(onClick = { showChangeAdmin = !showChangeAdmin; oldPin = ""; newPin = ""; confirmPin = ""; changePinError = ""; changePinSuccess = "" }) {
                                Icon(if (showChangeAdmin) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                            }
                        }
                        if (showChangeAdmin) {
                            OutlinedTextField(value = oldPin, onValueChange = { if (it.length <= pinLen) oldPin = it }, label = { Text("Current Admin PIN") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = newPin, onValueChange = { if (it.length <= pinLen) newPin = it }, label = { Text("New Admin PIN ($pinLen digits)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = confirmPin, onValueChange = { if (it.length <= pinLen) confirmPin = it }, label = { Text("Confirm New PIN") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword), visualTransformation = PasswordVisualTransformation(), singleLine = true, modifier = Modifier.fillMaxWidth())
                            if (changePinError.isNotEmpty()) Text(changePinError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            if (changePinSuccess.isNotEmpty()) Text(changePinSuccess, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                            Button(onClick = {
                                when {
                                    newPin.length < pinLen -> changePinError = "PIN must be $pinLen digits"
                                    newPin != confirmPin -> changePinError = "PINs don't match"
                                    authViewModel.isPalindrome(newPin) -> changePinError = "PIN cannot be a palindrome"
                                    else -> {
                                        if (authViewModel.changeAdminPin(oldPin, newPin)) { changePinSuccess = "Admin PIN changed!"; changePinError = ""; oldPin = ""; newPin = ""; confirmPin = "" }
                                        else changePinError = "Current PIN is incorrect"
                                    }
                                }
                            }, modifier = Modifier.fillMaxWidth()) { Text("Change Admin PIN") }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // ── Restore from Cloud ───────────────────────────────────────────────
            Text("Restore from Cloud", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudDownload, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Restore from Firestore", fontWeight = FontWeight.Medium)
                            Text(
                                "Pull all files, persons and payments back from the cloud into this device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    when (val state = restoreState) {
                        is RestoreState.Idle -> {
                            Button(
                                onClick = { restoreViewModel.checkFirestore() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.CloudDownload, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Check & Restore")
                            }
                        }
                        is RestoreState.Checking, is RestoreState.Restoring -> {
                            val label = if (state is RestoreState.Checking) "Checking Firestore…" else "Restoring data…"
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        is RestoreState.Preview -> {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Found in Firestore:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                    Text("• ${state.fileCount} loan files", style = MaterialTheme.typography.bodySmall)
                                    Text("• ${state.personCount} persons (active + completed + deleted)", style = MaterialTheme.typography.bodySmall)
                                    Text("• ${state.paymentCount} payments", style = MaterialTheme.typography.bodySmall)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "⚠ This will add Firestore data into local DB. If you already have local data, it may create duplicates. Proceed only on a fresh install.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { restoreViewModel.reset() }, modifier = Modifier.weight(1f)) {
                                    Text("Cancel")
                                }
                                Button(
                                    onClick = { showRestoreConfirmDialog = true },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Restore Now")
                                }
                            }
                        }
                        is RestoreState.Success -> {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text(state.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            TextButton(onClick = { restoreViewModel.reset() }, modifier = Modifier.fillMaxWidth()) {
                                Text("Done")
                            }
                        }
                        is RestoreState.Error -> {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                Text(state.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                            TextButton(onClick = { restoreViewModel.reset() }, modifier = Modifier.fillMaxWidth()) {
                                Text("Dismiss")
                            }
                        }
                    }
                }
            }

            // Confirm restore dialog
            if (showRestoreConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showRestoreConfirmDialog = false },
                    title = { Text("Restore from Firestore?") },
                    text = { Text("This will restore all data from Firestore into this device. Only do this on a fresh install to avoid duplicates.") },
                    confirmButton = {
                        Button(onClick = { showRestoreConfirmDialog = false; restoreViewModel.restoreFromFirestore() }) {
                            Text("Yes, Restore")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRestoreConfirmDialog = false }) { Text("Cancel") }
                    }
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── About ────────────────────────────────────────────────────────
            Text("About", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("MoneyMate", fontWeight = FontWeight.Medium)
                        val versionName = LocalContext.current.packageManager
                            .getPackageInfo(LocalContext.current.packageName, 0).versionName ?: "—"
                        Text("Version $versionName", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── NLR Template Card ─────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NlrTemplateCard(
    nlrKey: String,
    viewModel: TemplateViewModel,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    val persons by viewModel.getForNlr(nlrKey).collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }
    var personToDelete by remember { mutableStateOf<DefaultPerson?>(null) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Groups, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(nlrKey, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("${persons.size} default names", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.PersonAdd, null, tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onToggle) {
                    Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null)
                }
            }

            if (expanded) {
                HorizontalDivider()
                if (persons.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("No default names. Tap + to add.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    val listHeight = minOf(persons.size * 56, 350).dp
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(listHeight),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        items(persons, key = { it.id }) { person ->
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(person.name, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
                                    if (!person.place.isNullOrEmpty()) {
                                        Text(person.place, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                IconButton(onClick = { personToDelete = person }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                    TextButton(
                        onClick = { showClearDialog = true },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Clear All", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var addName by remember { mutableStateOf("") }
        var addPlace by remember { mutableStateOf("") }
        var addMobile by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false; addName = ""; addPlace = ""; addMobile = "" },
            title = { Text("Add to $nlrKey Template") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = addName, onValueChange = { addName = it }, label = { Text("Name*") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = addPlace, onValueChange = { addPlace = it }, label = { Text("Place (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = addMobile, onValueChange = { addMobile = it.filter { c -> c.isDigit() || c == '+' } }, label = { Text("Mobile (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (addName.isNotBlank()) {
                        viewModel.addPerson(nlrKey, addName, addPlace.ifEmpty { null }, addMobile.ifEmpty { null }, persons.size)
                        showAddDialog = false; addName = ""; addPlace = ""; addMobile = ""
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("Cancel") } }
        )
    }

    personToDelete?.let { p ->
        AlertDialog(
            onDismissRequest = { personToDelete = null },
            title = { Text("Remove from Template?") },
            text = { Text("\"${p.name}\" will be removed from $nlrKey template. This won't affect existing files.") },
            confirmButton = { TextButton(onClick = { viewModel.deletePerson(p); personToDelete = null }) { Text("Remove", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { personToDelete = null }) { Text("Cancel") } }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear $nlrKey Template?") },
            text = { Text("All ${persons.size} default names will be removed. This won't affect existing files.") },
            confirmButton = { TextButton(onClick = { viewModel.clearAll(nlrKey); showClearDialog = false }) { Text("Clear All", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("Cancel") } }
        )
    }
}