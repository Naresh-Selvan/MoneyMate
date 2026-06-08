package com.moneymate.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.moneymate.app.ui.screens.*
import com.moneymate.app.ui.viewmodel.AuthViewModel
import com.moneymate.app.ui.viewmodel.SettingsViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object FileDetail : Screen("file_detail/{fileId}") {
        fun createRoute(fileId: String) = "file_detail/$fileId"
    }
    object PersonDetail : Screen("person_detail/{personId}") {
        fun createRoute(personId: String) = "person_detail/$personId"
    }
    object Trash : Screen("trash")
    object Settings : Screen("settings")
    object LoanHistory : Screen("loan_history/{personId}/{personName}") {
        fun createRoute(personId: String, personName: String) =
            "loan_history/$personId/${java.net.URLEncoder.encode(personName, "UTF-8")}"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Bottom navigation items
    val bottomNavItems = listOf(
        BottomNavScreen.Home,
        BottomNavScreen.Expense,
        BottomNavScreen.Customer,
        BottomNavScreen.Reports,
        BottomNavScreen.Settings,
    )

    // Show bottom bar only on the 5 main tab routes
    val showBottomBar = bottomNavItems.any { currentRoute == it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        // Pop up to the home tab to avoid building up a large back stack
                                        popUpTo(BottomNavScreen.Home.route) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavScreen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            // ── Bottom nav tab destinations ──────────────────────────────────
            composable(BottomNavScreen.Home.route) {
                HomeScreen(
                    navController = navController,
                    settingsViewModel = settingsViewModel,
                    authViewModel = authViewModel
                )
            }
            composable(BottomNavScreen.Expense.route) {
                ExpenseScreen()
            }
            composable(BottomNavScreen.Customer.route) {
                CustomerScreen()
            }
            composable(BottomNavScreen.Reports.route) {
                ReportsScreen()
            }
            composable(BottomNavScreen.Settings.route) {
                SettingsScreen(navController, viewModel = settingsViewModel, authViewModel = authViewModel)
            }

            // ── Detail screens (NO bottom nav) ───────────────────────────────
            composable(
                route = Screen.FileDetail.route,
                arguments = listOf(navArgument("fileId") { type = NavType.StringType })
            ) { backStack ->
                val fileId = backStack.arguments?.getString("fileId") ?: return@composable
                FileDetailScreen(navController, fileId, settingsViewModel = settingsViewModel)
            }
            composable(
                route = Screen.PersonDetail.route,
                arguments = listOf(navArgument("personId") { type = NavType.StringType })
            ) { backStack ->
                val personId = backStack.arguments?.getString("personId") ?: return@composable
                PersonDetailScreen(navController, personId)
            }
            composable(Screen.Trash.route) {
                TrashScreen(navController, settingsViewModel = settingsViewModel)
            }
            composable(
                route = Screen.LoanHistory.route,
                arguments = listOf(
                    navArgument("personId") { type = NavType.StringType },
                    navArgument("personName") { type = NavType.StringType }
                )
            ) { backStack ->
                val personId = backStack.arguments?.getString("personId") ?: return@composable
                val personName = java.net.URLDecoder.decode(
                    backStack.arguments?.getString("personName") ?: "",
                    "UTF-8"
                )
                LoanHistoryScreen(
                    navController = navController,
                    personId = personId,
                    personName = personName
                )
            }
        }
    }
}