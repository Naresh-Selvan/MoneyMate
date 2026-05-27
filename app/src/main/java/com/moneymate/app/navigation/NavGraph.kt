package com.moneymate.app.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.moneymate.app.ui.screens.FileDetailScreen
import com.moneymate.app.ui.screens.HomeScreen
import com.moneymate.app.ui.screens.PersonDetailScreen
import com.moneymate.app.ui.screens.SettingsScreen
import com.moneymate.app.ui.screens.TrashScreen
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
    object EditRequests : Screen("edit_requests")
    object Settings : Screen("settings")
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel
) {
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                settingsViewModel = settingsViewModel,
                authViewModel = authViewModel  // pass the shared instance
            )
        }
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
        composable(Screen.EditRequests.route) {
            // EditRequestScreen(navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController, viewModel = settingsViewModel, authViewModel = authViewModel)
        }
    }
}