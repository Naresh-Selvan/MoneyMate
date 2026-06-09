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
import com.moneymate.app.ui.viewmodel.LicenseViewModel
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
    object LineManagement : Screen("line_management")
    object AreaManagement : Screen("area_management")
    object LineMove : Screen("line_move")
    object License : Screen("license")
    object LoanHistory : Screen("loan_history/{personId}/{personName}") {
        fun createRoute(personId: String, personName: String) =
            "loan_history/$personId/${java.net.URLEncoder.encode(personName, "UTF-8")}"
    }
    object Collection : Screen("collection/{fileId}") {
        fun createRoute(fileId: String) = "collection/$fileId"
    }
    // ══════════════════════════════════════════════════════════════════
    // Role & Permissions: New Screens
    // ══════════════════════════════════════════════════════════════════
    object UserManagement : Screen("user_management")
    object UserDetail : Screen("user_detail/{userId}") {
        fun createRoute(userId: Long) = "user_detail/$userId"
    }
    object AuditLog : Screen("audit_log")
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()
    val reportViewModel: com.moneymate.app.ui.viewmodel.ReportViewModel = hiltViewModel()
    val sessionViewModel: com.moneymate.app.ui.viewmodel.SessionViewModel = hiltViewModel()
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

    // Permission-based bottom nav visibility
    val showReports = sessionViewModel.hasPermission(com.moneymate.app.data.local.entity.Permission.VIEW_REPORTS)
    val showSettings = sessionViewModel.hasPermission(com.moneymate.app.data.local.entity.Permission.MANAGE_SETTINGS)

    val visibleBottomNavItems = bottomNavItems.filter { item ->
        when (item.route) {
            BottomNavScreen.Reports.route -> showReports
            BottomNavScreen.Settings.route -> showSettings
            else -> true
        }
    }

    // Show bottom bar only on main tab routes + reports grid
    val showBottomBar = visibleBottomNavItems.any { currentRoute == it.route } || currentRoute == "reports"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    visibleBottomNavItems.forEach { item ->
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
                    authViewModel = authViewModel,
                    sessionManager = sessionViewModel
                )
            }
            composable(BottomNavScreen.Expense.route) {
                ExpenseScreen(
                    loanFileViewModel = hiltViewModel(),
                    expenseViewModel = hiltViewModel(),
                    investmentViewModel = hiltViewModel()
                )
            }
            composable(BottomNavScreen.Customer.route) {
                CustomerScreen()
            }
            composable(BottomNavScreen.Reports.route) {
                ReportsScreen(navController = navController, reportViewModel = reportViewModel)
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
                // Phase 2: Replace FileDetailScreen with CollectionScreen
                CollectionScreen(navController, fileId, settingsViewModel = settingsViewModel)
            }
            composable(
                route = Screen.Collection.route,
                arguments = listOf(navArgument("fileId") { type = NavType.StringType })
            ) { backStack ->
                val fileId = backStack.arguments?.getString("fileId") ?: return@composable
                CollectionScreen(navController, fileId, settingsViewModel = settingsViewModel)
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
            // ══════════════════════════════════════════════════════════════════
            // Settings Enhancements: New Screens
            // ══════════════════════════════════════════════════════════════════
            composable(Screen.LineManagement.route) {
                LineManagementScreen(
                    navController = navController,
                    settingsViewModel = settingsViewModel
                )
            }
            composable(Screen.AreaManagement.route) {
                AreaManagementScreen(navController = navController)
            }
            composable(Screen.LineMove.route) {
                LineMoveScreen(navController = navController)
            }
            composable(Screen.License.route) {
                LicenseScreen(
                    navController = navController,
                    authViewModel = authViewModel
                )
            }

            // ══════════════════════════════════════════════════════════════════
            // Role & Permissions: New Screens
            // ══════════════════════════════════════════════════════════════════
            composable(Screen.UserManagement.route) {
                UserManagementScreen(
                    navController = navController,
                    sessionManager = sessionViewModel
                )
            }
            composable(
                route = Screen.UserDetail.route,
                arguments = listOf(navArgument("userId") { type = NavType.LongType })
            ) { backStack ->
                val userId = backStack.arguments?.getLong("userId") ?: return@composable
                UserDetailScreen(
                    navController = navController,
                    userId = userId,
                    sessionManager = sessionViewModel
                )
            }
            composable(Screen.AuditLog.route) {
                AuditLogScreen(navController = navController)
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

            // ══════════════════════════════════════════════════════════════════
            // Phase 4 — Report Screens (no bottom nav)
            // ══════════════════════════════════════════════════════════════════
            composable("report/plan") { PlanReportScreen(navController, reportViewModel) }
            composable("report/daily_summary") { DailySummaryReportScreen(navController, reportViewModel) }
            composable("report/line_summary") { LineSummaryReportScreen(navController, reportViewModel) }
            composable("report/online_collections") { OnlineCollectionsReportScreen(navController, reportViewModel) }
            composable("report/site_dashboard") { SiteDashboardReportScreen(navController, reportViewModel) }
            composable("report/expense_summary") { ExpenseSummaryReportScreen(navController, reportViewModel) }
            composable("report/investment_summary") { InvestmentSummaryReportScreen(navController, reportViewModel) }
            composable("report/combined_summary") { CombinedSummaryReportScreen(navController, reportViewModel) }
            composable("report/book_excess_loss") { BookExcessLossReportScreen(navController, reportViewModel) }
            composable("report/loan_summary") { LoanSummaryReportScreen(navController, reportViewModel) }
            composable("report/about_to_close") { AboutToCloseReportScreen(navController, reportViewModel) }
            composable("report/missing_customers") { MissingCustomersReportScreen(navController, reportViewModel) }
            composable("report/monthly_interest") { MonthlyInterestReportScreen(navController, reportViewModel) }
            composable("report/completed_loans") { CompletedLoansReportScreen(navController, reportViewModel) }
            composable("report/non_performing") { NonPerformingReportScreen(navController, reportViewModel) }
            composable("report/bad_loans") { BadLoansReportScreen(navController, reportViewModel) }
            composable("report/new_bad_loans") { NewBadLoansReportScreen(navController, reportViewModel) }
            composable("report/new_customers") { NewCustomersReportScreen(navController, reportViewModel) }
            composable("report/loan_analysis") { LoanAnalysisReportScreen(navController, reportViewModel) }
            composable(
                route = "report/ledger/{personId}/{personName}",
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
                LedgerReportScreen(navController, personId = personId, personName = personName, reportViewModel = reportViewModel)
            }
        }
    }
}