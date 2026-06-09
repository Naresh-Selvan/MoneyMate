package com.moneymate.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.moneymate.app.data.repository.LoanFileRepository
import com.moneymate.app.ui.viewmodel.ReportViewModel

data class ReportCardInfo(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val section: String
)

val allReportCards = listOf(
    // Collection
    ReportCardInfo("plan", "Plan", Icons.Default.Assessment, "Collection"),
    ReportCardInfo("daily_summary", "Daily Summary", Icons.Default.CalendarToday, "Collection"),
    ReportCardInfo("line_summary", "Line Summary", Icons.Default.TableChart, "Collection"),
    ReportCardInfo("online_collections", "Online Collection", Icons.Default.Wifi, "Collection"),
    ReportCardInfo("site_dashboard", "Site Dashboard", Icons.Default.Dashboard, "Collection"),
    // Loan
    ReportCardInfo("loan_summary", "Loan Summary", Icons.Default.CompareArrows, "Loan"),
    ReportCardInfo("about_to_close", "About to Close", Icons.Default.Timer, "Loan"),
    ReportCardInfo("completed_loans", "Completed Loans", Icons.Default.CheckCircle, "Loan"),
    ReportCardInfo("book_excess_loss", "Book Excess Loss", Icons.Default.TrendingUp, "Loan"),
    // Customer
    ReportCardInfo("missing_customers", "Missing Customers", Icons.Default.PersonOff, "Customer"),
    ReportCardInfo("monthly_interest", "Monthly Interest Pending", Icons.Default.Pending, "Customer"),
    ReportCardInfo("new_customers", "New Customers", Icons.Default.PersonAdd, "Customer"),
    ReportCardInfo("loan_not_taken", "Loan Not Taken", Icons.Default.RemoveShoppingCart, "Customer"),
    // Finance
    ReportCardInfo("expense_summary", "Expense Summary", Icons.Default.MoneyOff, "Finance"),
    ReportCardInfo("investment_summary", "Investment Summary", Icons.Default.Storefront, "Finance"),
    ReportCardInfo("combined_summary", "Invest/Expense Combined", Icons.Default.AccountBalance, "Finance"),
    ReportCardInfo("ledger", "Ledger Report", Icons.Default.Receipt, "Finance"),
    // Analysis
    ReportCardInfo("non_performing", "Non Performance Loan", Icons.Default.Warning, "Analysis"),
    ReportCardInfo("bad_loans", "Bad Loan Summary", Icons.Default.ReportProblem, "Analysis"),
    ReportCardInfo("new_bad_loans", "New Bad Loan By Date", Icons.Default.EventNote, "Analysis"),
    ReportCardInfo("loan_analysis", "Loan Analysis", Icons.Default.Analytics, "Analysis")
)

val sectionIcons = mapOf(
    "Collection" to Icons.Default.Folder,
    "Loan" to Icons.Default.AccountBalanceWallet,
    "Customer" to Icons.Default.People,
    "Finance" to Icons.Default.AccountBalance,
    "Analysis" to Icons.Default.TrendingUp
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    navController: NavController? = null,
    reportViewModel: ReportViewModel = hiltViewModel(),
    loanFileRepository: LoanFileRepository? = null
) {
    val sections = allReportCards.groupBy { it.section }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Reports") })
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sections.forEach { (section, cards) ->
                item(span = { GridItemSpan(2) }) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                        Icon(
                            sectionIcons[section] ?: Icons.Default.Folder,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            section,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                items(cards, key = { it.id }) { card ->
                    ReportCard(
                        card = card,
                        onClick = {
                            val route = "report/${card.id}"
                            if (navController != null) {
                                navController.navigate(route)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ReportCard(card: ReportCardInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                card.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                card.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
        }
    }
}
