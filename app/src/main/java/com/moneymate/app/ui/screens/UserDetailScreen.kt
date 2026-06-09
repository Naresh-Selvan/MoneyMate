package com.moneymate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.moneymate.app.data.local.entity.AppUser
import com.moneymate.app.data.local.entity.AuditAction
import com.moneymate.app.data.local.entity.AuditLog
import com.moneymate.app.data.local.entity.UserRole
import com.moneymate.app.ui.viewmodel.SessionViewModel
import com.moneymate.app.ui.viewmodel.UserViewModel
import dagger.hilt.android.EntryPointAccessors
import java.text.SimpleDateFormat
import java.util.*

/**
 * User detail screen showing full profile, access info, activity, and recent audit log entries.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserDetailScreen(
    navController: NavHostController,
    userId: Long,
    userViewModel: UserViewModel = hiltViewModel(),
    sessionManager: SessionViewModel? = null
) {
    val user by userViewModel.allActiveUsers.collectAsState()
    val currentUser = user.find { it.id == userId }
    val sessionUser = sessionManager?.currentUser?.collectAsState()?.value
    val isAdmin = sessionUser?.role == UserRole.ADMIN

    var showChangeRoleDialog by remember { mutableStateOf(false) }
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var recentLogs by remember { mutableStateOf<List<AuditLog>>(emptyList()) }
    var totalPayments by remember { mutableStateOf(0) }
    var totalPersonsAdded by remember { mutableStateOf(0) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    val contextForLogs = LocalContext.current
    // Load audit logs for this user
    LaunchedEffect(userId) {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                contextForLogs.applicationContext,
                com.moneymate.app.di.RepositoryEntryPoint::class.java
            )
            val auditLogDao = entryPoint.auditLogDao()
            auditLogDao.getAllLogs(500).collect { logs ->
                val userLogs = logs.filter { it.userId == userId }
                recentLogs = userLogs.take(20)
                totalPayments = userLogs.count { it.action == AuditAction.ADD_PAYMENT }
                totalPersonsAdded = userLogs.count { it.action == AuditAction.ADD_PERSON }
            }
        } catch (_: Exception) {}
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    if (isAdmin && currentUser != null) {
                        IconButton(onClick = { showChangeRoleDialog = true }) {
                            Icon(Icons.Default.SwapHoriz, "Change Role")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (currentUser == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("User not found", style = MaterialTheme.typography.bodyLarge)
            }
            return@Scaffold
        }

        val roleColor = when (currentUser.role) {
            UserRole.ADMIN -> Color(0xFF7C4DFF)
            UserRole.BOSS -> Color(0xFF2196F3)
            UserRole.USER -> Color(0xFF4CAF50)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile section
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(roleColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                currentUser.displayName.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = roleColor,
                                fontSize = 24.sp
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(currentUser.displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text(currentUser.email, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = roleColor.copy(alpha = 0.15f)
                        ) {
                            Text(currentUser.role.name, color = roleColor,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                        }
                    }
                }
            }

            // Access section
            item {
                Text("Access", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                if (currentUser.role != UserRole.USER) "All Files (full permissions)"
                                else "Assigned Files: ${currentUser.assignedFileIds.ifBlank { "None" }}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Activity section
            item {
                Text("Activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Last Login", style = MaterialTheme.typography.bodySmall)
                            Text(
                                currentUser.lastLoginAt?.let { dateFormat.format(Date(it)) } ?: "Never",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        HorizontalDivider()
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Payments Recorded", style = MaterialTheme.typography.bodySmall)
                            Text("$totalPayments", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                        HorizontalDivider()
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Persons Added", style = MaterialTheme.typography.bodySmall)
                            Text("$totalPersonsAdded", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Recent Actions section
            item {
                Text("Recent Actions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            if (recentLogs.isEmpty()) {
                item {
                    Text("No recent activity", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(recentLogs) { log ->
                    AuditLogCard(log = log, dateFormat = dateFormat)
                }
            }

            // Danger Zone (ADMIN only)
            if (isAdmin && currentUser.id != sessionUser?.id) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Danger Zone", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { showChangeRoleDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) { Text("Change Role") }

                            Button(
                                onClick = { showDeactivateDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) { Text("Deactivate User") }
                        }
                    }
                }
            }
        }
    }

    // Change Role dialog
    if (showChangeRoleDialog && currentUser != null) {
        var newRole by remember { mutableStateOf(currentUser.role) }
        AlertDialog(
            onDismissRequest = { showChangeRoleDialog = false },
            title = { Text("Change Role — ${currentUser.displayName}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(UserRole.USER, UserRole.BOSS, UserRole.ADMIN).forEach { role ->
                        FilterChip(
                            selected = newRole == role,
                            onClick = { newRole = role },
                            label = { Text(role.name) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    userViewModel.changeUserRole(currentUser.id, newRole, sessionUser?.id ?: 0L) {
                        showChangeRoleDialog = false
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { showChangeRoleDialog = false }) { Text("Cancel") } }
        )
    }

    if (showDeactivateDialog && currentUser != null) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            title = { Text("Deactivate ${currentUser.displayName}?") },
            text = { Text("They will lose access immediately.") },
            confirmButton = {
                Button(onClick = {
                    userViewModel.deactivateUser(currentUser.id, sessionUser?.id ?: 0L) {
                        showDeactivateDialog = false
                        navController.popBackStack()
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Deactivate")
                }
            },
            dismissButton = { TextButton(onClick = { showDeactivateDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun AuditLogCard(log: AuditLog, dateFormat: SimpleDateFormat) {
    val actionIcon = when (log.action) {
        AuditAction.ADD_PERSON, AuditAction.EDIT_PERSON, AuditAction.DELETE_PERSON, AuditAction.MOVE_PERSON -> Icons.Default.Person
        AuditAction.ADD_PAYMENT, AuditAction.EDIT_PAYMENT, AuditAction.DELETE_PAYMENT -> Icons.Default.Payments
        AuditAction.ADD_FILE, AuditAction.RENAME_FILE, AuditAction.DELETE_FILE -> Icons.Default.Folder
        AuditAction.LOGIN, AuditAction.LOGOUT -> Icons.Default.Login
        AuditAction.ADD_USER, AuditAction.EDIT_USER, AuditAction.DEACTIVATE_USER, AuditAction.CHANGE_ROLE -> Icons.Default.Group
        AuditAction.EXPORT_REPORT -> Icons.Default.Download
        AuditAction.ADD_EXPENSE, AuditAction.EDIT_EXPENSE, AuditAction.DELETE_EXPENSE -> Icons.Default.Money
        else -> Icons.Default.Info
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(actionIcon, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "${log.action.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }} ${log.targetLabel}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    dateFormat.format(Date(log.timestamp)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
