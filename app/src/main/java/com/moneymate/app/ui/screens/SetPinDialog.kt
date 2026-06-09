package com.moneymate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import java.security.MessageDigest

@Composable
fun SetPinDialog(
    currentPinHash: String,
    onDismiss: () -> Unit,
    onSave: (newHash: String) -> Unit
) {
    val hasPin = currentPinHash.isNotBlank()
    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var showRemoveConfirm by remember { mutableStateOf(false) }

    fun hashPin(pin: String): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(pin.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }

    if (showRemoveConfirm) {
        AlertDialog(
            onDismissRequest = { showRemoveConfirm = false },
            title = { Text("Remove PIN?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter your current PIN to remove it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = { currentPin = it; error = "" },
                        label = { Text("Current PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = error.isNotEmpty(),
                        supportingText = if (error.isNotEmpty()) {{ Text(error) }} else null
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (currentPin.length < 4) {
                            error = "PIN must be 4 digits"
                        } else if (hashPin(currentPin) != currentPinHash) {
                            error = "Current PIN is incorrect"
                        } else {
                            onSave("")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Remove PIN") }
            },
            dismissButton = { TextButton(onClick = { showRemoveConfirm = false }) { Text("Cancel") } }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (hasPin) "Change Admin PIN" else "Set Admin PIN") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (hasPin) {
                    Text("Enter your current PIN, then set a new one.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = { currentPin = it; error = "" },
                        label = { Text("Current PIN") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = error.isNotEmpty()
                    )
                } else {
                    Text("Set a 4-digit admin PIN for sensitive operations.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                OutlinedTextField(
                    value = newPin,
                    onValueChange = { if (it.length <= 4) newPin = it; error = "" },
                    label = { Text("New PIN (4 digits)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = error.isNotEmpty()
                )

                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 4) confirmPin = it; error = "" },
                    label = { Text("Confirm PIN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = error.isNotEmpty()
                )

                if (error.isNotEmpty()) {
                    Text(error, color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        newPin.length != 4 -> error = "PIN must be exactly 4 digits"
                        newPin != confirmPin -> error = "PINs don't match"
                        hasPin && hashPin(currentPin) != currentPinHash -> error = "Current PIN is incorrect"
                        newPin.all { it == newPin[0] } -> error = "PIN cannot be all same digits"
                        else -> {
                            onSave(hashPin(newPin))
                        }
                    }
                }
            ) { Text(if (hasPin) "Save Changes" else "Set PIN") }
        },
        dismissButton = {
            Row {
                if (hasPin) {
                    TextButton(onClick = { showRemoveConfirm = true }) {
                        Text("Remove PIN", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
