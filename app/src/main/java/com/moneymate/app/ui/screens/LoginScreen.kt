package com.moneymate.app.ui.screens

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
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
import com.moneymate.app.R
import com.moneymate.app.ui.viewmodel.AuthState
import com.moneymate.app.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    authState: AuthState
) {
    val pinLength = viewModel.pinLength
    var pin by remember { mutableStateOf("") }
    val error by viewModel.error.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val lockCountdown by viewModel.lockCountdown.collectAsState()
    val context = LocalContext.current
    val biometricEnabled = viewModel.biometricEnabled

    val biometricManager = BiometricManager.from(context)
    val canUseBiometric = biometricEnabled && biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
        BiometricManager.Authenticators.BIOMETRIC_WEAK
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

    LaunchedEffect(authState) {
        pin = ""
        viewModel.clearError()
    }

    LaunchedEffect(authState, canUseBiometric) {
        if (canUseBiometric && !isLocked) {
            launchBiometric { viewModel.loginAsAdmin(pin = "__biometric__") }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "MoneyMate Logo",
            modifier = Modifier
                .size(120.dp)
                .clip(RoundedCornerShape(24.dp))
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text("MoneyMate", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Loan Tracker", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(56.dp))

        Text("Welcome Back", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Enter your $pinLength-digit PIN", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(32.dp))

        if (isLocked) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
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
            Button(
                onClick = { viewModel.loginAsAdmin(pin) },
                modifier = Modifier.fillMaxWidth(),
                enabled = pin.length == pinLength
            ) { Text("Login") }

            if (canUseBiometric) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = { launchBiometric { viewModel.loginAsAdmin(pin = "__biometric__") } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Use Fingerprint")
                }
            }
        }
    }
}
