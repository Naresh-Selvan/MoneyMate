package com.moneymate.app.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import com.moneymate.app.ui.viewmodel.UpdateViewModel
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
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
    restoreViewModel: RestoreViewModel = hiltViewModel(),
    updateViewModel: UpdateViewModel = hiltViewModel()
)  {
    val context = LocalContext.current
    val darkMode by viewModel.darkMode.collectAsState()
    val updateState by updateViewModel.updateState.collectAsState()
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

    // Dynamic Account Security State Management Hooks
    var userEmail by remember { mutableStateOf(authViewModel.getCurrentUserEmail()) }
    var userPhone by remember { mutableStateOf(authViewModel.getCurrentUserPhone()) }
    var identityStatusMessage by remember { mutableStateOf<String?>(null) }
    var isIdentityActionLoading by remember { mutableStateOf(false) }

    var showPhoneDialog by remember { mutableStateOf(false) }
    var showOtpDialog by remember { mutableStateOf(false) }
    var phoneNumberInput by remember { mutableStateOf("") }
    var otpVerificationInput by remember { mutableStateOf("") }

    // Core Intent Result Interceptor Contract Mapping for Google Promotion Requests
    val googleAccountLinkLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            isIdentityActionLoading = true
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                authViewModel.linkGoogleAccount(
                    credential = credential,
                    onSuccess = {
                        isIdentityActionLoading = false
                        userEmail = authViewModel.getCurrentUserEmail()
                        identityStatusMessage = "Google account linked successfully!"
                    },
                    onFailure = { err ->
                        isIdentityActionLoading = false
                        identityStatusMessage = "Linking tracking failed: $err"
                    }
                )
            } catch (e: ApiException) {
                isIdentityActionLoading = false
                identityStatusMessage = "Google execution aborted (Code: ${e.statusCode})"
            }
        }
    }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Account Identity Management ──────────────────────────────────
            Text("Identity Accounts", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // Email Section Row
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Primary Mail ID", fontWeight = FontWeight.Medium)
                            Text(userEmail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (userEmail == "Not Linked" || userEmail.isBlank() || !userEmail.contains("@")) {
                            Button(
                                onClick = {
                                    // Replace with your project-specific default web client ID string resource key reference link
                                    val webClientId = context.getString(context.resources.getIdentifier("default_web_client_id", "string", context.packageName))
                                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .requestIdToken(webClientId)
                                        .requestEmail()
                                        .build()
                                    val client = GoogleSignIn.getClient(context, gso)
                                    googleAccountLinkLauncher.launch(client.signInIntent)
                                },
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                            ) { Text("Link", fontSize = 12.sp) }
                        } else {
                            Icon(Icons.Default.CheckCircle, "Verified Identity Profile", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    // Phone Number Section Row
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Linked Phone Number", fontWeight = FontWeight.Medium)
                            Text(userPhone, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = { showPhoneDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (userPhone == "Not Linked") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = if (userPhone == "Not Linked") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Text(if (userPhone == "Not Linked") "Link" else "Edit", fontSize = 12.sp)
                        }
                    }

                    if (isIdentityActionLoading) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                    }

                    identityStatusMessage?.let { msg ->
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (msg.contains("successfully")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

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

            // ── Default Person Templates ─────────────────────────────────────
            Text("Default Person Templates", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text(
                "Names auto-inserted when a new NLR file is created. Updated automatically after each upload.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

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
                        Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(msg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            templateViewModel.nlrKeys.forEach { nlrKey ->
                NlrTemplateCard(
                    nlrKey = nlrKey,
                    viewModel = templateViewModel,
                    expanded = templateTab == nlrKey,
                    onToggle = { templateTab = if (templateTab == nlrKey) null else nlrKey }
                )
            }

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
                            Text("Pull all files, persons and payments back from the cloud into this device.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    when (val state = restoreState) {
                        is RestoreState.Idle -> {
                            Button(onClick = { restoreViewModel.checkFirestore() }, modifier = Modifier.fillMaxWidth()) {
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
                                    Text("⚠ This will add Firestore data into local DB. If you already have local data, it may create duplicates. Proceed only on a fresh install.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                }
                            }
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { restoreViewModel.reset() }, modifier = Modifier.weight(1f)) { Text("Cancel") }
                                Button(onClick = { showRestoreConfirmDialog = true }, modifier = Modifier.weight(1f)) { Text("Restore Now") }
                            }
                        }
                        is RestoreState.Success -> {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text(state.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            TextButton(onClick = { restoreViewModel.reset() }, modifier = Modifier.fillMaxWidth()) { Text("Done") }
                        }
                        is RestoreState.Error -> {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                Text(state.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                            }
                            TextButton(onClick = { restoreViewModel.reset() }, modifier = Modifier.fillMaxWidth()) { Text("Dismiss") }
                        }
                    }
                }
            }

            if (showRestoreConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showRestoreConfirmDialog = false },
                    title = { Text("Restore from Firestore?") },
                    text = { Text("This will restore all data from Firestore into this device. Only do this on a fresh install to avoid duplicates.") },
                    confirmButton = {
                        Button(onClick = { showRestoreConfirmDialog = false; restoreViewModel.restoreFromFirestore() }) { Text("Yes, Restore") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRestoreConfirmDialog = false }) { Text("Cancel") }
                    }
                )
            }

            // ── About ────────────────────────────────────────────────────────
            Text("About", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        val packageInfo = LocalContext.current.packageManager.getPackageInfo(LocalContext.current.packageName, 0)
                        val versionName = packageInfo.versionName ?: "—"
                        @Suppress("DEPRECATION")
                        val versionCode = packageInfo.versionCode
                        Text("MoneyMate", fontWeight = FontWeight.Medium)
                        Text("Version $versionName-$versionCode", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        TextButton(onClick = { updateViewModel.forceCheckForUpdate(versionCode) }, contentPadding = PaddingValues(0.dp)) {
                            Text("Check for updates", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Destructive Session Management Control ───────────────────────
            Button(
                onClick = { authViewModel.logoutUser() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout Account Session", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Spacer(Modifier.height(4.dp))
            UpdateDialog(updateState = updateState, viewModel = updateViewModel)
        }
    }

    // ── Phone Insertion Modal Overlay ──────────────────────────────────────
    if (showPhoneDialog) {
        AlertDialog(
            onDismissRequest = { showPhoneDialog = false; phoneNumberInput = "" },
            title = { Text("Link / Update Phone") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Provide international formatting context definitions (e.g. +91XXXXXXXXXX)", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = phoneNumberInput,
                        onValueChange = { phoneNumberInput = it },
                        label = { Text("Phone Number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPhoneDialog = false
                        isIdentityActionLoading = true
                        authViewModel.startLinkingPhoneNumber(
                            phoneNumber = phoneNumberInput.trim(),
                            activity = context as Activity,
                            onSuccess = {
                                isIdentityActionLoading = false
                                showOtpDialog = true
                            },
                            onFailure = { err ->
                                isIdentityActionLoading = false
                                identityStatusMessage = err
                            }
                        )
                    },
                    enabled = phoneNumberInput.isNotBlank()
                ) { Text("Send Verification Token") }
            },
            dismissButton = { TextButton(onClick = { showPhoneDialog = false }) { Text("Cancel") } }
        )
    }

    // ── OTP Challenge Modal Overlay ─────────────────────────────────────────
    if (showOtpDialog) {
        AlertDialog(
            onDismissRequest = { showOtpDialog = false; otpVerificationInput = "" },
            title = { Text("Enter 6-Digit Code") },
            text = {
                OutlinedTextField(
                    value = otpVerificationInput,
                    onValueChange = { if (it.length <= 6) otpVerificationInput = it },
                    label = { Text("Verification OTP Code") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showOtpDialog = false
                        isIdentityActionLoading = true
                        authViewModel.verifyAndLinkPhoneCode(
                            code = otpVerificationInput.trim(),
                            onSuccess = {
                                isIdentityActionLoading = false
                                userPhone = authViewModel.getCurrentUserPhone()
                                identityStatusMessage = "Phone identity verified and linked successfully!"
                            },
                            onFailure = { err ->
                                isIdentityActionLoading = false
                                identityStatusMessage = err
                            }
                        )
                    },
                    enabled = otpVerificationInput.length == 6
                ) { Text("Verify & Save Connection") }
            },
            dismissButton = { TextButton(onClick = { showOtpDialog = false }) { Text("Cancel") } }
        )
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