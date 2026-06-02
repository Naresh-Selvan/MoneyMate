package com.moneymate.app.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.moneymate.app.ui.viewmodel.AuthViewModel
import com.moneymate.app.ui.viewmodel.RestoreState
import com.moneymate.app.ui.viewmodel.RestoreViewModel
import com.moneymate.app.ui.viewmodel.SettingsViewModel
import com.moneymate.app.ui.viewmodel.UserRole

// Helper function to safely extract Activity from context wrapper hierarchies
fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel,
    authViewModel: AuthViewModel,
    restoreViewModel: RestoreViewModel = hiltViewModel(),
    updateViewModel: UpdateViewModel = hiltViewModel()
) {
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

    val restoreState by restoreViewModel.restoreState.collectAsState()
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    // Dynamic Account Security State Management Hooks
    var userEmail by remember { mutableStateOf(authViewModel.getCurrentUserEmail()) }
    var userPhone by remember { mutableStateOf(authViewModel.getCurrentUserPhone()) }
    var identityStatusMessage by remember { mutableStateOf<String?>(null) }
    var isIdentityActionLoading by remember { mutableStateOf(false) }

    var showPhoneDialog by remember { mutableStateOf(false) }
    var showOtpDialog by remember { mutableStateOf(false) }
    var phoneNumberInput by remember { mutableStateOf("") }
    var otpVerificationInput by remember { mutableStateOf("") }

    // Country Code Selector Dropdown States
    val countryCodes = listOf(
        "🇮🇳 +91",
        "🇺🇸 +1",
        "🇬🇧 +44",
        "🇦🇪 +971",
        "🇸🇬 +65"
    )
    var selectedCountryCode by remember { mutableStateOf(countryCodes[0]) }

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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // ── Account Identity Management ──────────────────────────────────
            item {
                Text("Identity Accounts", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
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
            }

            // ── Appearance ───────────────────────────────────────────────────
            item {
                Text("Appearance", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
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
            }

            // ── Notifications ────────────────────────────────────────────────
            item {
                Text("Notifications", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
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
            }

            // ── Trash ────────────────────────────────────────────────────────
            item {
                Text("Trash", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
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
            }

            // ── Security ─────────────────────────────────────────────────────
            item {
                if (currentRole != UserRole.USER) {
                    Text("Security", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(4.dp))

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

                    Spacer(Modifier.height(8.dp))

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

                    Spacer(Modifier.height(8.dp))

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
            }

            // ── Restore from Cloud ───────────────────────────────────────────────
            item {
                Text("Restore from Cloud", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
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
            }

            // ── About ────────────────────────────────────────────────────────
            item {
                Text("About", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
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
            }

            // ── Destructive Session Management Control ───────────────────────
            item {
                Spacer(Modifier.height(8.dp))
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

    // ── Phone Insertion Modal Overlay with Country Code Dropdown ───────────
    if (showPhoneDialog) {
        var dropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showPhoneDialog = false; phoneNumberInput = "" },
            title = { Text("Link / Update Phone") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Select your area identifier prefix and input your local operational digits below.", style = MaterialTheme.typography.bodySmall)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Dropdown Trigger Container Box Box
                        Box(modifier = Modifier.wrapContentSize(Alignment.TopStart)) {
                            OutlinedButton(
                                onClick = { dropdownExpanded = true },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 14.dp),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(56.dp)
                            ) {
                                Text(selectedCountryCode, fontSize = 14.sp)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowDropDown, null)
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                countryCodes.forEach { code ->
                                    DropdownMenuItem(
                                        text = { Text(code) },
                                        onClick = {
                                            selectedCountryCode = code
                                            dropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Phone Number Input Field
                        OutlinedTextField(
                            value = phoneNumberInput,
                            onValueChange = { input ->
                                // Sanitize input to accept numbers only
                                phoneNumberInput = input.filter { it.isDigit() }
                            },
                            label = { Text("Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val activity = context.findActivity()
                        if (activity != null) {
                            showPhoneDialog = false
                            isIdentityActionLoading = true

                            // Extract just the numerical part of the code (e.g., "+91")
                            val cleanPrefix = selectedCountryCode.substringAfter(" ")
                            val fullInternationalNumber = cleanPrefix + phoneNumberInput.trim()

                            authViewModel.startLinkingPhoneNumber(
                                phoneNumber = fullInternationalNumber,
                                activity = activity,
                                onSuccess = {
                                    isIdentityActionLoading = false
                                    showOtpDialog = true
                                },
                                onFailure = { err ->
                                    isIdentityActionLoading = false
                                    identityStatusMessage = err
                                }
                            )
                        } else {
                            identityStatusMessage = "Failed to locate active structural Activity window."
                        }
                    },
                    enabled = phoneNumberInput.isNotBlank()
                ) { Text("Send Verification Token") }
            },
            dismissButton = { TextButton(onClick = { showPhoneDialog = false }) { Text("Cancel") } }
        )
    }

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