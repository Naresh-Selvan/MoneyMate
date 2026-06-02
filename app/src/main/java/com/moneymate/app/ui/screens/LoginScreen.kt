package com.moneymate.app.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.moneymate.app.R
import com.moneymate.app.ui.viewmodel.AuthState
import com.moneymate.app.ui.viewmodel.AuthViewModel
import com.moneymate.app.ui.viewmodel.GoogleSignInResult
import com.moneymate.app.ui.viewmodel.PhoneSignInResult
import com.moneymate.app.ui.viewmodel.MigrationState
import com.moneymate.app.ui.viewmodel.MigrationViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    authState: AuthState,
    migrationViewModel: MigrationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val googleSignInResult by viewModel.googleSignInResult.collectAsState()
    val phoneSignInResult by viewModel.phoneSignInResult.collectAsState()
    val migrationState by migrationViewModel.migrationState.collectAsState()

    // ── Google Sign-In ─────────────────────────────────────────────────────
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                viewModel.handleGoogleCredential(credential)
            } catch (e: ApiException) {
                viewModel.setGoogleSignInFailure("Google Sign-In Error Code: ${e.statusCode}")
            }
        } else {
            viewModel.clearGoogleSignInResult()
        }
    }

    fun launchGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        googleSignInLauncher.launch(client.signInIntent)
    }

    // Trigger local migration on generic authentication success
    LaunchedEffect(googleSignInResult) {
        if (googleSignInResult is GoogleSignInResult.Success) {
            migrationViewModel.runMigrationIfNeeded()
        }
    }

    LaunchedEffect(phoneSignInResult) {
        if (phoneSignInResult is PhoneSignInResult.Success) {
            migrationViewModel.runMigrationIfNeeded()
        }
    }

    // Complete session entry sequence
    LaunchedEffect(migrationState) {
        if (migrationState is MigrationState.Success || migrationState is MigrationState.NotNeeded) {
            if (viewModel.googleSignInResult.value is GoogleSignInResult.Success) {
                viewModel.onGoogleSignInHandled()
            } else if (viewModel.phoneSignInResult.value is PhoneSignInResult.Success) {
                viewModel.onPhoneSignInHandled()
            }
        }
    }

    AnimatedContent(
        targetState = authState,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "login_screen_transition"
    ) { state ->
        when (state) {
            AuthState.GOOGLE_SIGN_IN -> {
                if (migrationState is MigrationState.InProgress ||
                    migrationState is MigrationState.Progress ||
                    migrationState is MigrationState.Checking
                ) {
                    MigrationProgressScreen(migrationState = migrationState)
                } else if (migrationState is MigrationState.Error) {
                    MigrationErrorScreen(
                        error = (migrationState as MigrationState.Error).message,
                        onRetry = { migrationViewModel.retryMigration() }
                    )
                } else {
                    AuthenticationSelectionScreen(
                        isLoading = googleSignInResult is GoogleSignInResult.Loading,
                        error = (googleSignInResult as? GoogleSignInResult.Failure)?.message,
                        onGoogleSignIn = { launchGoogleSignIn() },
                        onPhoneSignInSelected = { viewModel.navigateToPhoneLogin() }
                    )
                }
            }

            AuthState.PHONE_LOGIN -> {
                BackHandler { viewModel.navigateBackToSelector() }
                PhoneInputScreen(
                    isLoading = phoneSignInResult is PhoneSignInResult.Loading,
                    error = (phoneSignInResult as? PhoneSignInResult.Failure)?.message,
                    onSendOtp = { phone -> viewModel.sendOtpCode(phone, context as Activity) },
                    onBack = { viewModel.navigateBackToSelector() }
                )
            }

            AuthState.OTP_VERIFICATION -> {
                BackHandler { viewModel.navigateToPhoneLogin() }
                OtpVerificationScreen(
                    isLoading = phoneSignInResult is PhoneSignInResult.Loading,
                    error = (phoneSignInResult as? PhoneSignInResult.Failure)?.message,
                    onVerifyOtp = { code -> viewModel.verifyOtpCode(code) },
                    onBack = { viewModel.navigateToPhoneLogin() }
                )
            }

            AuthState.ADMIN_LOGIN, AuthState.LOGIN -> {
                PinLoginScreen(viewModel = viewModel)
            }

            else -> { /* LOADING/AUTHENTICATED States handled outside layout bounds */ }
        }
    }
}

// ─── Component 1: Unified Selector Screen ──────────────────────────────

@Composable
private fun AuthenticationSelectionScreen(
    isLoading: Boolean,
    error: String?,
    onGoogleSignIn: () -> Unit,
    onPhoneSignInSelected: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "MoneyMate Logo",
            modifier = Modifier.size(120.dp).clip(RoundedCornerShape(24.dp))
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text("MoneyMate", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Loan Tracker", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(48.dp))

        Text("Sign in to get started", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(40.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            OutlinedButton(
                onClick = onGoogleSignIn,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_google), contentDescription = null, modifier = Modifier.size(20.dp), tint = androidx.compose.ui.graphics.Color.Unspecified)
                Spacer(Modifier.width(12.dp))
                Text("Continue with Google", fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onPhoneSignInSelected,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Phone, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text("Continue with Phone OTP", fontWeight = FontWeight.Medium)
            }
        }

        if (error != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
    }
}

// ─── Component 2: Phone Number Input View ──────────────────────────────

@Composable
private fun PhoneInputScreen(
    isLoading: Boolean,
    error: String?,
    onSendOtp: (String) -> Unit,
    onBack: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Verify Phone Number", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Enter your phone number with country code (e.g., +919876543210)", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { onSendOtp(phoneNumber.trim()) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = phoneNumber.isNotBlank()
            ) {
                Text("Send OTP Verification")
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onBack) {
                Text("Back to options")
            }
        }
    }
}

// ─── Component 3: OTP Code Receipt Verification View ───────────────────

@Composable
private fun OtpVerificationScreen(
    isLoading: Boolean,
    error: String?,
    onVerifyOtp: (String) -> Unit,
    onBack: () -> Unit
) {
    var otpCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Enter OTP Code", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Type the 6-digit verification token sent via SMS", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = otpCode,
            onValueChange = { if (it.length <= 6) otpCode = it },
            label = { Text("Verification Code") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { onVerifyOtp(otpCode.trim()) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = otpCode.length == 6
            ) {
                Text("Verify & Continue")
            }
            Spacer(modifier = Modifier.height(12.dp))
            TextButton(onClick = onBack) {
                Text("Resend or Change Number")
            }
        }
    }
}

// ─── Component 4: Migration Screens ────────────────────────────────────

@Composable
private fun MigrationProgressScreen(migrationState: MigrationState) {
    val message = when (migrationState) {
        is MigrationState.Progress -> migrationState.message
        is MigrationState.Checking -> "Checking for existing data…"
        is MigrationState.InProgress -> "Migrating your data…"
        else -> "Please wait…"
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(32.dp))
        Text("Securing your data", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("We're moving your existing data to your personal account.\nThis happens only once.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(24.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Text(text = message, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text("Do not close the app", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun MigrationErrorScreen(error: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Migration Failed", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Your original data is safe and has NOT been deleted.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Text(text = error, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onRetry, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Retry Migration")
        }
    }
}

// ─── Component 5: PIN Entry Dashboard view ─────────────────────────────

@Composable
private fun PinLoginScreen(viewModel: AuthViewModel) {
    val pinLength = viewModel.pinLength
    var pin by remember { mutableStateOf("") }
    val error by viewModel.error.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val lockCountdown by viewModel.lockCountdown.collectAsState()
    val context = LocalContext.current
    val biometricEnabled = viewModel.biometricEnabled

    val biometricManager = BiometricManager.from(context)
    val canUseBiometric = biometricEnabled && biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
    ) == BiometricManager.BIOMETRIC_SUCCESS

    fun launchBiometric(onSuccess: () -> Unit) {
        val activity = context as? FragmentActivity ?: return
        val executor = ContextCompat.getMainExecutor(context)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }
        }
        val prompt = BiometricPrompt(activity, executor, callback)
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("MoneyMate")
            .setSubtitle("Use fingerprint to login")
            .setNegativeButtonText("Use PIN")
            .build()
        prompt.authenticate(info)
    }

    LaunchedEffect(Unit) {
        pin = ""
        viewModel.clearError()
    }

    LaunchedEffect(canUseBiometric, isLocked) {
        if (canUseBiometric && !isLocked) {
            launchBiometric { viewModel.loginAsAdmin(pin = "__biometric__") }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(painter = painterResource(id = R.drawable.logo), contentDescription = "MoneyMate Logo", modifier = Modifier.size(120.dp).clip(RoundedCornerShape(24.dp)))
        Spacer(modifier = Modifier.height(20.dp))
        Text("MoneyMate", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Loan Tracker", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(56.dp))

        Text("Welcome Back", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Enter your $pinLength-digit PIN", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))

        if (isLocked) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Too many wrong attempts!", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Text("Try again in ${lockCountdown}s", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        } else {
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= pinLength) pin = it },
                label = { Text("PIN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                isError = error != null,
                modifier = Modifier.fillMaxWidth()
            )
            if (error != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { viewModel.loginAsAdmin(pin) }, modifier = Modifier.fillMaxWidth(), enabled = pin.length == pinLength) { Text("Login") }

            if (canUseBiometric) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(onClick = { launchBiometric { viewModel.loginAsAdmin(pin = "__biometric__") } }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Use Fingerprint")
                }
            }
        }
    }
}