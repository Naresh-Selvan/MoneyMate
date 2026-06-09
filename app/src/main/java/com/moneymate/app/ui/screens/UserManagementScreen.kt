package com.moneymate.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.moneymate.app.data.local.entity.AppUser
import com.moneymate.app.data.local.entity.UserRole
import com.moneymate.app.ui.viewmodel.LoanFileViewModel
import com.moneymate.app.ui.viewmodel.SessionViewModel
import com.moneymate.app.ui.viewmodel.UserOperationResult
import com.moneymate.app.ui.viewmodel.UserViewModel

/**
 * User management screen — ADMIN only. Lists all active and inactive users
 * with add/edit/deactivate capabilities.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    navController: NavHostController,
    userViewModel: UserViewModel = hiltViewModel(),
    loanFileViewModel: LoanFileViewModel = hiltViewModel(),
    sessionManager: SessionViewModel? = null
) {
    val activeUsers by userViewModel.allActiveUsers.collectAsState()
    val inactiveUsers by userViewModel.allInactiveUsers.collectAsState()
    val files by loanFileViewModel.allFiles.collectAsState()
    val currentUserId = sessionManager?.currentUser?.value?.id ?: 0L

    var showAddSheet by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<AppUser?>(null) }
    var showDeactivateDialog by remember { mutableStateOf<AppUser?>(null) }
    var showInactiveSection by remember { mutableStateOf(false) }
    var showPlanLimitDialog by remember { mutableStateOf(false) }

    // Plan limit check
    val maxUsers = remember { userViewModel.getMaxUsersForPlan() }
    val atPlanLimit = activeUsers.size >= maxUsers

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Team Management", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    if (atPlanLimit) {
                        Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(end = 8.dp))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (atPlanLimit) {
                    // Show upgrade dialog
                } else {
                    showAddSheet = true
                }
            }) {
                Icon(Icons.Default.PersonAdd, null)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Plan limit banner
            if (atPlanLimit && maxUsers < Int.MAX_VALUE) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Plan limit reached ($maxUsers users). Upgrade to add more.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }

            // Active users header
            item {
                Text("Active Users (${activeUsers.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold)
            }

            if (activeUsers.isEmpty()) {
                item {
                    Text("No team members yet. Tap + to add one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            items(activeUsers, key = { it.id }) { user ->
                UserCard(
                    user = user,
                    onClick = {
                        navController.navigate("user_detail/${user.id}")
                    },
                    onEdit = { editingUser = user },
                    onDeactivate = { showDeactivateDialog = user }
                )
            }

            // Inactive users section
            if (inactiveUsers.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = { showInactiveSection = !showInactiveSection }) {
                        Icon(
                            if (showInactiveSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Inactive Users (${inactiveUsers.size})")
                    }
                }
                if (showInactiveSection) {
                    items(inactiveUsers, key = { it.id }) { user ->
                        UserCard(
                            user = user,
                            onClick = {},
                            onEdit = null,
                            onDeactivate = null,
                            isInactive = true
                        )
                    }
                }
            }
        }
    }

    // Add/Edit bottom sheet
    if (showAddSheet || editingUser != null) {
        AddEditUserSheet(
            user = editingUser,
            files = files,
            onDismiss = { showAddSheet = false; editingUser = null },
            onSave = { email, displayName, role, assignedFileIds, pinHash ->
                if (editingUser != null) {
                    userViewModel.updateUser(
                        userId = editingUser!!.id,
                        email = email,
                        displayName = displayName,
                        role = role,
                        assignedFileIds = assignedFileIds,
                        pinHash = pinHash
                    ) { result ->
                        if (result is UserOperationResult.Success) {
                            showAddSheet = false
                            editingUser = null
                        }
                    }
                } else {
                    userViewModel.addUser(
                        email = email,
                        displayName = displayName,
                        role = role,
                        assignedFileIds = assignedFileIds,
                        pinHash = pinHash,
                        createdByUserId = currentUserId
                    ) { result ->
                        when (result) {
                            is UserOperationResult.Success -> showAddSheet = false
                            is UserOperationResult.PlanLimitReached -> { /* show upgrade dialog */ }
                            is UserOperationResult.Error -> { /* show error */ }
                            else -> {}
                        }
                    }
                }
            }
        )
    }

    // Deactivate confirmation
    showDeactivateDialog?.let { user ->
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = null },
            title = { Text("Deactivate ${user.displayName}?") },
            text = { Text("They will lose access immediately. This action can be reversed by an admin.") },
            confirmButton = {
                Button(
                    onClick = {
                        userViewModel.deactivateUser(user.id, currentUserId) {
                            showDeactivateDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Deactivate") }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateDialog = null }) { Text("Cancel") }
            }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// User Card
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun UserCard(
    user: AppUser,
    onClick: () -> Unit,
    onEdit: (() -> Unit)?,
    onDeactivate: (() -> Unit)?,
    isInactive: Boolean = false
) {
    val roleColor = when (user.role) {
        UserRole.ADMIN -> Color(0xFF7C4DFF) // Purple
        UserRole.BOSS -> Color(0xFF2196F3)   // Blue
        UserRole.USER -> Color(0xFF4CAF50)   // Green
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isInactive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with initials
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(roleColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    user.displayName.take(2).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = roleColor,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(user.displayName, fontWeight = FontWeight.Medium)
                Text(user.email, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Role chip
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = roleColor.copy(alpha = 0.15f)
                    ) {
                        Text(user.role.name, color = roleColor,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                    // Status dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (user.isActive) Color(0xFF4CAF50) else Color(0xFFFF5722))
                    )
                }
            }
            if (!isInactive && onEdit != null) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// AddEditUserSheet — ModalBottomSheet
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditUserSheet(
    user: AppUser?,
    files: List<com.moneymate.app.data.local.entity.LoanFile>,
    onDismiss: () -> Unit,
    onSave: (email: String, displayName: String, role: UserRole, assignedFileIds: String, pinHash: String?) -> Unit
) {
    val isEdit = user != null

    var displayName by remember { mutableStateOf(user?.displayName ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var selectedRole by remember { mutableStateOf(user?.role ?: UserRole.USER) }
    var assignedFileIds by remember {
        mutableStateOf(user?.assignedFileIds ?: "")
    }
    var enablePin by remember { mutableStateOf(user?.pinHash != null) }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var pinError by remember { mutableStateOf(false) }

    val roleOptions = listOf(UserRole.USER, UserRole.BOSS, UserRole.ADMIN)

    val isRoleFileDisabled = selectedRole == UserRole.ADMIN || selectedRole == UserRole.BOSS

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                if (isEdit) "Edit User" else "Add Team Member",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Display Name
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it; nameError = false },
                label = { Text("Display Name *") },
                isError = nameError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; emailError = false },
                label = { Text("Email *") },
                isError = emailError,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            // Role selector
            Text("Role", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                roleOptions.forEach { role ->
                    FilterChip(
                        selected = selectedRole == role,
                        onClick = { selectedRole = role },
                        label = { Text(role.name, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (role) {
                                UserRole.ADMIN -> Color(0xFF7C4DFF).copy(alpha = 0.2f)
                                UserRole.BOSS -> Color(0xFF2196F3).copy(alpha = 0.2f)
                                UserRole.USER -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                            }
                        )
                    )
                }
            }

            // Assigned files (multi-select, only for USER role)
            Text(
                if (isRoleFileDisabled) "Access: All Files (full permissions)"
                else "Assigned Files (only these files)",
                style = MaterialTheme.typography.labelLarge
            )
            if (!isRoleFileDisabled) {
                val selectedSet = remember(assignedFileIds) {
                    mutableStateOf(assignedFileIds.split(",").filter { it.isNotBlank() }.toSet())
                }

                files.filter { !it.isDeleted }.forEach { file ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = selectedSet.value.contains(file.id),
                            onCheckedChange = { checked ->
                                val mutable = selectedSet.value.toMutableSet()
                                if (checked) mutable.add(file.id) else mutable.remove(file.id)
                                selectedSet.value = mutable
                                assignedFileIds = mutable.joinToString(",")
                            }
                        )
                        Text(file.name, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // PIN setup toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Set PIN", style = MaterialTheme.typography.labelLarge)
                Switch(checked = enablePin, onCheckedChange = { enablePin = it })
            }
            if (enablePin) {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                        pin = it; pinError = false
                    }},
                    label = { Text("PIN (4 digits)") },
                    isError = pinError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                        confirmPin = it; pinError = false
                    }},
                    label = { Text("Confirm PIN") },
                    isError = pinError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    // Validate
                    var hasError = false
                    if (displayName.isBlank()) { nameError = true; hasError = true }
                    if (email.isBlank() || !email.contains("@")) { emailError = true; hasError = true }
                    if (enablePin && (pin.length != 4 || pin != confirmPin)) { pinError = true; hasError = true }
                    if (hasError) return@Button

                    val finalPinHash = if (enablePin && pin.length == 4) {
                        val digest = java.security.MessageDigest.getInstance("SHA-256")
                        digest.digest(pin.toByteArray()).joinToString("") { "%02x".format(it) }
                    } else null

                    onSave(email.trim(), displayName.trim(), selectedRole, assignedFileIds, finalPinHash)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = displayName.isNotBlank() && email.isNotBlank() && email.contains("@")
            ) {
                Text(if (isEdit) "Save Changes" else "Add User")
            }
        }
    }
}
