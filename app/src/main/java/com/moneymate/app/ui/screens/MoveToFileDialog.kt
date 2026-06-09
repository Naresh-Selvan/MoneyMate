package com.moneymate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.moneymate.app.data.local.entity.LoanFile
import com.moneymate.app.data.local.entity.Person
import com.moneymate.app.utils.AppPreferences
import java.security.MessageDigest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveToFileDialog(
    person: Person,
    allFiles: List<LoanFile>,
    currentFileId: String,
    appPreferences: AppPreferences,
    onDismiss: () -> Unit,
    onConfirm: (targetFileId: String) -> Unit
) {
    var selectedFileId by remember { mutableStateOf<String?>(null) }
    var password by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }

    val targetFiles = allFiles.filter { it.id != currentFileId && !it.isDeleted }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move ${person.name} to File") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Select the target file and enter admin password to confirm.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // File selection
                Text("Target File", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                if (targetFiles.isEmpty()) {
                    Text(
                        "No other files available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(Modifier.heightIn(max = 200.dp)) {
                        items(targetFiles, key = { it.id }) { file ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedFileId == file.id,
                                    onClick = { selectedFileId = file.id }
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(file.name, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                // Admin password
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it; passwordError = false },
                    label = { Text("Admin Password") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    isError = passwordError,
                    supportingText = if (passwordError) {{ Text("Incorrect password") }} else null,
                    trailingIcon = {
                        IconButton(onClick = { showPassword = !showPassword }) {
                            Icon(
                                if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                null
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val targetId = selectedFileId
                    if (targetId == null) return@TextButton
                    // SHA-256 hash to match AuthViewModel.hashPin()
                    val digest = MessageDigest.getInstance("SHA-256")
                    val hashBytes = digest.digest(password.toByteArray())
                    val hashed = hashBytes.joinToString("") { "%02x".format(it) }
                    if (hashed != appPreferences.adminPinHash) {
                        passwordError = true
                        return@TextButton
                    }
                    onConfirm(targetId)
                },
                enabled = selectedFileId != null && password.isNotBlank()
            ) {
                Text("Move")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
