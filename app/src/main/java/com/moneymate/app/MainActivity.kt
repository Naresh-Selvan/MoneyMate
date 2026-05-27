package com.moneymate.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.fragment.app.FragmentActivity
import androidx.navigation.compose.rememberNavController
import com.moneymate.app.navigation.NavGraph
import com.moneymate.app.ui.screens.LoginScreen
import com.moneymate.app.ui.screens.UpdateDialog
import com.moneymate.app.ui.theme.MoneyMateTheme
import com.moneymate.app.ui.viewmodel.AuthState
import com.moneymate.app.ui.viewmodel.AuthViewModel
import com.moneymate.app.ui.viewmodel.SettingsViewModel
import com.moneymate.app.ui.viewmodel.UpdateViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private val settingsViewModel: SettingsViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val updateViewModel: UpdateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            authViewModel.checkSessionTimeout()
            // Check for updates on every fresh launch
            val currentVersionCode = packageManager
                .getPackageInfo(packageName, 0).versionCode
            updateViewModel.checkForUpdate(currentVersionCode)
        }

        setContent {
            val darkMode    by settingsViewModel.darkMode.collectAsState()
            val authState   by authViewModel.authState.collectAsState()
            val updateState by updateViewModel.updateState.collectAsState()

            LaunchedEffect(Unit) {
                snapshotFlow { darkMode }
                    .distinctUntilChanged()
                    .drop(1)
                    .collect { recreate() }
            }

            MoneyMateTheme(darkTheme = darkMode, dynamicColor = false) {
                when (authState) {
                    AuthState.LOADING -> {}
                    AuthState.LOGIN, AuthState.ADMIN_LOGIN -> {
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
                // Update dialog floats on top of everything
                UpdateDialog(updateState = updateState, viewModel = updateViewModel)
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
}