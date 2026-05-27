package com.moneymate.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.moneymate.app.ui.viewmodel.UpdateInfo
import com.moneymate.app.ui.viewmodel.UpdateState
import com.moneymate.app.ui.viewmodel.UpdateViewModel
import java.io.File

@Composable
fun UpdateDialog(
    updateState: UpdateState,
    viewModel: UpdateViewModel
) {
    val context = LocalContext.current

    when (val state = updateState) {

        // ── Update available ─────────────────────────────────────────────────
        is UpdateState.Available -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismiss() },
                icon = { Icon(Icons.Default.SystemUpdate, null, tint = MaterialTheme.colorScheme.primary) },
                title = { Text("Update Available", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "Current",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "v${getInstalledVersionName(context)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                Icons.Default.ArrowForward, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.CenterVertically)
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "New",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "v${state.info.latestVersion}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        if (state.info.releaseNotes.isNotBlank()) {
                            HorizontalDivider()
                            Text(
                                "What's new:",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                state.info.releaseNotes.take(300),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { viewModel.downloadAndInstall(state.info) }) {
                        Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Update Now")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismiss() }) { Text("Later") }
                }
            )
        }

        // ── Downloading ──────────────────────────────────────────────────────
        is UpdateState.Downloading -> {
            AlertDialog(
                onDismissRequest = {},
                icon = { Icon(Icons.Default.CloudDownload, null, tint = MaterialTheme.colorScheme.primary) },
                title = { Text("Downloading Update…", fontWeight = FontWeight.Bold) },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "The new APK is being downloaded. You'll be prompted to install once it's ready.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {}
            )
        }

        // ── Ready to install ─────────────────────────────────────────────────
        is UpdateState.ReadyToInstall -> {
            LaunchedEffect(state.filePath) {
                installApk(context, state.filePath)
            }
            AlertDialog(
                onDismissRequest = { viewModel.dismiss() },
                icon = { Icon(Icons.Default.InstallMobile, null, tint = MaterialTheme.colorScheme.primary) },
                title = { Text("Ready to Install", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("The update has been downloaded. Tap Install to apply it.")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                            !context.packageManager.canRequestPackageInstalls()
                        ) {
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                                Row(
                                    Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.Warning, null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp))
                                    Text(
                                        "\"Install unknown apps\" permission needed. Tap Install — Android will ask you to enable it.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { installApk(context, state.filePath) }) {
                        Icon(Icons.Default.InstallMobile, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Install")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.dismiss() }) { Text("Later") }
                }
            )
        }

        // ── Error ────────────────────────────────────────────────────────────
        is UpdateState.Error -> {
            AlertDialog(
                onDismissRequest = { viewModel.dismiss() },
                icon = { Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error) },
                title = { Text("Update Failed") },
                text = { Text(state.message, style = MaterialTheme.typography.bodySmall) },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismiss() }) { Text("OK") }
                }
            )
        }

        else -> {} // Idle, Checking, UpToDate — no dialog
    }
}

private fun getInstalledVersionName(context: android.content.Context): String {
    return try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "?"
    } catch (e: Exception) { "?" }
}

private fun installApk(context: android.content.Context, fileUriString: String) {
    try {
        val file = if (fileUriString.startsWith("file://")) {
            File(Uri.parse(fileUriString).path ?: return)
        } else {
            File(Uri.parse(fileUriString).path ?: return)
        }

        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.update_provider",
            file
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        // Android 8+: check install permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            val settingsIntent = Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            ).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            context.startActivity(settingsIntent)
        } else {
            context.startActivity(intent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}