package com.moneymate.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.moneymate.app.navigation.NavGraph
import com.moneymate.app.notifications.WorkerScheduler
import com.moneymate.app.ui.screens.LoginScreen
import com.moneymate.app.ui.screens.UpdateDialog
import com.moneymate.app.ui.theme.MoneyMateTheme
import com.moneymate.app.ui.viewmodel.AuthState
import com.moneymate.app.ui.viewmodel.AuthViewModel
import com.moneymate.app.ui.viewmodel.SettingsViewModel
import com.moneymate.app.auth.AuditLogger
import com.moneymate.app.ui.viewmodel.UpdateViewModel
import com.moneymate.app.utils.AppPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val updateViewModel: UpdateViewModel by viewModels()

    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var workerScheduler: WorkerScheduler

    @Inject
    lateinit var auditLogger: AuditLogger

    // Permission launcher — registered once in onCreate before any dialog
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or denied — no follow-up, user can re-enable from Settings */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            authViewModel.checkSessionTimeout()
            val currentVersionCode = packageManager
                .getPackageInfo(packageName, 0).versionCode
            updateViewModel.checkForUpdate(currentVersionCode)
            // Prune old audit logs on app start (90 day retention)
            auditLogger.pruneOldLogs()
        }

        // Schedule periodic workers on app start
        workerScheduler.scheduleOnStart()

        setContent {
            val darkMode    by settingsViewModel.darkMode.collectAsState()
            val authState   by authViewModel.authState.collectAsState()
            val updateState by updateViewModel.updateState.collectAsState()

            // ── POST_NOTIFICATIONS first-launch dialog state ──────────────
            var showNotificationDialog by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                snapshotFlow { darkMode }
                    .distinctUntilChanged()
                    .drop(1)
                    .collect { recreate() }
            }

            // Check once on first composition whether to show the permission dialog
            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= 33
                    && !appPreferences.notificationPermissionRequested
                    && ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Mark as requested immediately — we never re-prompt automatically
                    appPreferences.notificationPermissionRequested = true
                    showNotificationDialog = true
                }
            }

            MoneyMateTheme(darkTheme = darkMode, dynamicColor = false) {
                when (authState) {
                    AuthState.LOADING -> {}

                    // UPDATED: Added PHONE_LOGIN and OTP_VERIFICATION states to routing group
                    AuthState.GOOGLE_SIGN_IN,
                    AuthState.PHONE_LOGIN,
                    AuthState.OTP_VERIFICATION,
                    AuthState.LOGIN,
                    AuthState.ADMIN_LOGIN,
                    AuthState.PIN_SETUP -> {
                        LoginScreen(viewModel = authViewModel, authState = authState)
                    }

                    AuthState.AUTHENTICATED -> {
                        val navController = rememberNavController()
                        NavGraph(
                            navController = navController,
                            authViewModel = authViewModel
                        )
                    }
                }
                UpdateDialog(updateState = updateState, viewModel = updateViewModel)
            }

            // ── POST_NOTIFICATIONS permission dialog ─────────────────────
            if (showNotificationDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showNotificationDialog = false
                    },
                    title = { Text("Stay on top of collections") },
                    text = {
                        Text("MoneyMate sends daily reminders and loan alerts to keep your collections on track. Allow notifications?")
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            showNotificationDialog = false
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }) {
                            Text("Allow")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showNotificationDialog = false
                        }) {
                            Text("Not Now")
                        }
                    }
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        authViewModel.onAppForeground()
    }

    override fun onStop() {
        super.onStop()
        authViewModel.onAppBackground()
    }

    /**
     * Re-schedule all periodic workers (called from [SettingsScreen] when
     * notification toggles or reminder time changes).
     */
    fun rescheduleWorkers() {
        workerScheduler.rescheduleForSettingsChange()
    }
}
