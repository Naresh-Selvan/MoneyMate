package com.moneymate.app.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.moneymate.app.ui.viewmodel.AuthViewModel
import com.moneymate.app.ui.viewmodel.LicenseState
import com.moneymate.app.ui.viewmodel.LicenseViewModel
import com.moneymate.app.utils.AppConfig
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicenseScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    licenseViewModel: LicenseViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val licenseState by licenseViewModel.licenseState.collectAsState()
    val deviceId = remember { licenseViewModel.getDeviceId() }
    val activatedEmail = remember { licenseViewModel.getActivatedEmail() }
    val activationStatus = remember { licenseViewModel.getActivationStatus() }
    val activationPlan = remember { licenseViewModel.getActivationPlan() }
    val activationExpiry = remember { licenseViewModel.getActivationExpiry() }

    val isTrialActive = remember { licenseViewModel.isTrialActive() }
    val trialDaysRemaining = remember { licenseViewModel.getTrialDaysRemaining() }

    var selectedPlan by remember { mutableStateOf("starter") }
    var transactionId by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf(activatedEmail) }
    var showCopiedSnack by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("License", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = {
            if (showCopiedSnack) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { showCopiedSnack = false }) { Text("Dismiss") }
                    }
                ) { Text("Copied!") }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header ─────────────────────────────────────────────────────
            Text("MoneyMate", fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center)

            // ── Device Info ────────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Device ID:", fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(8.dp))
                        Text(deviceId, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Device ID", deviceId))
                            showCopiedSnack = true
                        }, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.ContentCopy, "Copy", modifier = Modifier.size(16.dp))
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Username:", fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(8.dp))
                        Text(if (activatedEmail.isNotBlank()) activatedEmail else "naresh@gmail.com",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Plan:", fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(8.dp))
                        Text(activationPlan.replaceFirstChar { it.uppercase() }.ifEmpty { "Starter" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Status:", fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(8.dp))
                        val statusColor = when {
                            activationStatus == "active" -> MaterialTheme.colorScheme.primary
                            isTrialActive -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.error
                        }
                        val statusText = when {
                            activationStatus == "active" -> "Active"
                            isTrialActive -> "Trial"
                            else -> "Expired"
                        }
                        Surface(color = statusColor.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                            Text("  $statusText  ",
                                color = statusColor,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    if (activationExpiry > 0) {
                        val expiryDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(activationExpiry))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Expires:", fontWeight = FontWeight.Medium)
                            Spacer(Modifier.width(8.dp))
                            Text(expiryDate, style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        val daysLeft = maxOf(0, (activationExpiry - System.currentTimeMillis()) / (24 * 60 * 60 * 1000))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Days Left:", fontWeight = FontWeight.Medium)
                            Spacer(Modifier.width(8.dp))
                            Text("$daysLeft days", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else if (isTrialActive) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Days Left:", fontWeight = FontWeight.Medium)
                            Spacer(Modifier.width(8.dp))
                            Text("$trialDaysRemaining days", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // ── Plan Cards ─────────────────────────────────────────────────
            Text("Choose a Plan", fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("Starter", "₹999", "1 user"),
                    Triple("Growth", "₹1999", "5 users"),
                    Triple("Enterprise", "₹3999", "Unlimited")
                ).forEach { (name, price, users) ->
                    val isSelected = selectedPlan == name.lowercase()
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .then(if (isSelected) Modifier else Modifier),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surface
                        ),
                        onClick = { selectedPlan = name.lowercase() }
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(name, fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium)
                            Text(price, fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary)
                            Text(users, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // ── UPI Payment ────────────────────────────────────────────────
            Text("Pay via UPI", fontWeight = FontWeight.Bold)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // QR Code placeholder
                    Surface(
                        modifier = Modifier.size(150.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.QrCode, null,
                                    modifier = Modifier.size(60.dp),
                                    tint = MaterialTheme.colorScheme.primary)
                                Text("QR Code",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("UPI ID: ${AppConfig.UPI_ID}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium)
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("UPI ID", AppConfig.UPI_ID))
                            showCopiedSnack = true
                        }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.ContentCopy, "Copy UPI ID", modifier = Modifier.size(16.dp))
                        }
                    }

                    HorizontalDivider()

                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Your Email") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    OutlinedTextField(
                        value = transactionId,
                        onValueChange = { transactionId = it },
                        label = { Text("Transaction ID") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            licenseViewModel.submitTransaction(
                                deviceId = deviceId,
                                email = emailInput,
                                plan = selectedPlan,
                                transactionId = transactionId
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = transactionId.isNotBlank() && emailInput.isNotBlank()
                    ) { Text("Submit for Verification") }

                    // License state feedback
                    when (val state = licenseState) {
                        is LicenseState.Submitting -> {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Text("Verifying…", style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        is LicenseState.Approved -> {
                            Surface(color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.CheckCircle, null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                    Text("Activated!",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        is LicenseState.Error -> {
                            Surface(color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp)) {
                                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.ErrorOutline, null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer)
                                    Text(state.message,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }

            // ── Support ────────────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Support, null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Support", fontWeight = FontWeight.Medium)
                        Text(AppConfig.SUPPORT_PHONE, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_DIAL,
                            Uri.parse("tel:${AppConfig.SUPPORT_PHONE}"))
                        context.startActivity(intent)
                    }) {
                        Icon(Icons.Default.Phone, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Call")
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
