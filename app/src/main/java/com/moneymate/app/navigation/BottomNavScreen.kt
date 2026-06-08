package com.moneymate.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavScreen(val route: String, val label: String, val icon: ImageVector) {
    object Home : BottomNavScreen("home", "Home", Icons.Default.Home)
    object Expense : BottomNavScreen("expense", "Expense", Icons.Default.Payments)
    object Customer : BottomNavScreen("customer", "Customer", Icons.Default.People)
    object Reports : BottomNavScreen("reports", "Reports", Icons.Default.Analytics)
    object Settings : BottomNavScreen("settings", "Settings", Icons.Default.Settings)
}
