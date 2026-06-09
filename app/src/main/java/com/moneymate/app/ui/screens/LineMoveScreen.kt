package com.moneymate.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.moneymate.app.data.local.entity.LoanFile
import com.moneymate.app.data.local.entity.Person
import com.moneymate.app.data.repository.PersonRepository
import com.moneymate.app.di.RepositoryEntryPoint
import com.moneymate.app.ui.viewmodel.AuthViewModel
import com.moneymate.app.ui.viewmodel.LoanFileViewModel
import com.moneymate.app.ui.viewmodel.PersonViewModel
import com.moneymate.app.utils.AppPreferences
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import java.security.MessageDigest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LineMoveScreen(
    navController: NavHostController,
    loanFileViewModel: LoanFileViewModel = hiltViewModel(),
    personViewModel: PersonViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val files by loanFileViewModel.allFiles.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    val appPrefs = remember { AppPreferences(context) }

    // Step tracking
    var currentStep by remember { mutableIntStateOf(1) }
    var pinError by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf("") }
    var pinAttempts by remember { mutableIntStateOf(0) }
    var lockUntil by remember { mutableStateOf(0L) }

    // Step 2 state
    var fromFileId by remember { mutableStateOf<String?>(null) }
    var toFileId by remember { mutableStateOf<String?>(null) }
    var fromDropdownExp by remember { mutableStateOf(false) }
    var toDropdownExp by remember { mutableStateOf(false) }
    var personsInFile by remember { mutableStateOf<List<Person>>(emptyList()) }
    var selectedPersonIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectAll by remember { mutableStateOf(false) }

    // Step 3 state
    var isMoving by remember { mutableStateOf(false) }
    var moveResult by remember { mutableStateOf<String?>(null) }

    // Get PersonRepository from Hilt for actual person moves
    val personRepository = remember {
        EntryPointAccessors.fromApplication(context.applicationContext, RepositoryEntryPoint::class.java).personRepository()
    }

    // Load persons when from file changes
    LaunchedEffect(fromFileId) {
        if (fromFileId != null) {
            personViewModel.loadPersonsForFile(fromFileId!!)
        }
    }
    val persons by personViewModel.persons.collectAsState()

    LaunchedEffect(persons) {
        personsInFile = persons
    }

    fun verifyPin(pin: String): Boolean {
        // Check if locked
        if (System.currentTimeMillis() < lockUntil) {
            pinError = "Too many attempts. Try again later."
            return false
        }

        val storedHash = appPrefs.adminPinHash
        if (storedHash.isBlank()) {
            pinError = "No admin PIN set. Please set one in Security settings."
            return false
        }

        val inputHash = MessageDigest.getInstance("SHA-256")
            .digest(pin.toByteArray())
            .joinToString("") { "%02x".format(it) }

        return if (inputHash == storedHash) {
            pinAttempts = 0
            true
        } else {
            pinAttempts++
            if (pinAttempts >= 3) {
                lockUntil = System.currentTimeMillis() + 30_000 // 30 seconds
                pinError = "Too many wrong attempts. Locked for 30 seconds."
            } else {
                pinError = "Wrong PIN. ${3 - pinAttempts} attempt(s) remaining."
            }
            false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Line Move", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Step indicator
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                StepIndicator(step = 1, label = "Auth", isActive = currentStep == 1, isComplete = currentStep > 1)
                HorizontalDivider(modifier = Modifier.weight(1f))
                StepIndicator(step = 2, label = "Select", isActive = currentStep == 2, isComplete = currentStep > 2)
                HorizontalDivider(modifier = Modifier.weight(1f))
                StepIndicator(step = 3, label = "Move", isActive = currentStep == 3, isComplete = currentStep > 3)
            }

            when (currentStep) {
                1 -> {
                    // Step 1: PIN Authentication
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Enter Admin PIN", fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium)
                            Text("This action requires admin authorization.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)

                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = {
                                    if (it.length <= 4) {
                                        pinInput = it
                                        pinError = ""
                                    }
                                },
                                label = { Text("Admin PIN") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                isError = pinError.isNotEmpty(),
                                supportingText = if (pinError.isNotEmpty()) {{ Text(pinError) }} else null
                            )

                            Button(
                                onClick = {
                                    if (verifyPin(pinInput)) {
                                        pinInput = ""
                                        pinError = ""
                                        currentStep = 2
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = pinInput.length == 4
                            ) { Text("Confirm") }
                        }
                    }
                }

                2 -> {
                    // Step 2: Select files and persons
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Select Files", fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium)

                            // From File
                            ExposedDropdownMenuBox(
                                expanded = fromDropdownExp,
                                onExpandedChange = { fromDropdownExp = it }
                            ) {
                                val fromFile = files.find { it.id == fromFileId }
                                OutlinedTextField(
                                    value = fromFile?.name ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("From File") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromDropdownExp) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    singleLine = true
                                )
                                ExposedDropdownMenu(
                                    expanded = fromDropdownExp,
                                    onDismissRequest = { fromDropdownExp = false }
                                ) {
                                    files.filter { !it.isDeleted && it.id != toFileId }.forEach { file ->
                                        DropdownMenuItem(
                                            text = { Text(file.name) },
                                            onClick = {
                                                fromFileId = file.id
                                                fromDropdownExp = false
                                            }
                                        )
                                    }
                                }
                            }

                            // To File
                            ExposedDropdownMenuBox(
                                expanded = toDropdownExp,
                                onExpandedChange = { toDropdownExp = it }
                            ) {
                                val toFile = files.find { it.id == toFileId }
                                OutlinedTextField(
                                    value = toFile?.name ?: "",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("To File") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toDropdownExp) },
                                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                                    singleLine = true
                                )
                                ExposedDropdownMenu(
                                    expanded = toDropdownExp,
                                    onDismissRequest = { toDropdownExp = false }
                                ) {
                                    files.filter { !it.isDeleted && it.id != fromFileId }.forEach { file ->
                                        DropdownMenuItem(
                                            text = { Text(file.name) },
                                            onClick = {
                                                toFileId = file.id
                                                toDropdownExp = false
                                            }
                                        )
                                    }
                                }
                            }

                            if (fromFileId != null && toFileId != null) {
                                HorizontalDivider()
                                Text("Select Persons to Move", fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge)

                                // Select All checkbox
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = selectAll,
                                        onCheckedChange = { checked ->
                                            selectAll = checked
                                            selectedPersonIds = if (checked) personsInFile.map { it.id }.toSet()
                                            else emptySet()
                                        }
                                    )
                                    Text("Select All", fontWeight = FontWeight.Medium)
                                }

                                // Person list
                                LazyColumn(Modifier.fillMaxWidth().weight(1f)) {
                                    items(personsInFile, key = { it.id }) { person ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Checkbox(
                                                    checked = selectedPersonIds.contains(person.id),
                                                    onCheckedChange = { checked ->
                                                        selectedPersonIds = if (checked) {
                                                            selectedPersonIds + person.id
                                                        } else {
                                                            selectedPersonIds - person.id
                                                        }
                                                        selectAll = selectedPersonIds.size == personsInFile.size
                                                    }
                                                )
                                                Column(Modifier.weight(1f)) {
                                                    Text(person.name, fontWeight = FontWeight.Medium)
                                                    Text(
                                                        "₹${person.amountGiven} | ${person.place ?: "No area"}",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (selectedPersonIds.isNotEmpty()) {
                                            currentStep = 3
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = selectedPersonIds.isNotEmpty()
                                ) { Text("Move Selected (${selectedPersonIds.size})") }
                            }
                        }
                    }
                }

                3 -> {
                    // Step 3: Execute move
                    if (isMoving) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator()
                                Spacer(Modifier.height(16.dp))
                                Text("Moving persons…")
                            }
                        }
                    } else if (moveResult != null) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp))
                                Text(moveResult!!, style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold)
                                val toFile = files.find { it.id == toFileId }
                                Text("Moved to: ${toFile?.name ?: "Unknown"}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Button(onClick = { navController.popBackStack() }) {
                                    Text("Done")
                                }
                            }
                        }
                    } else {
                        // Show move button
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.SwapHoriz, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(48.dp))
                                Text("Ready to move ${selectedPersonIds.size} person(s)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold)
                                Button(
                                    onClick = {
                                        isMoving = true
                                        scope.launch {
                                            val selectedPersons = personsInFile.filter { it.id in selectedPersonIds }
                                            for (p in selectedPersons) {
                                                personRepository.softDeletePerson(p.id, System.currentTimeMillis())
                                                val newPerson = p.copy(
                                                    id = java.util.UUID.randomUUID().toString(),
                                                    fileId = toFileId!!,
                                                    sortOrder = 0,
                                                    isDeleted = false,
                                                    deletedAt = null,
                                                    uploadedAt = null
                                                )
                                                personRepository.insertPerson(newPerson)
                                            }
                                            val targetName = files.find { it.id == toFileId }?.name ?: "target"
                                            moveResult = "${selectedPersonIds.size} persons moved to $targetName"
                                            isMoving = false
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("Execute Move") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepIndicator(step: Int, label: String, isActive: Boolean, isComplete: Boolean) {
    val color = when {
        isComplete -> MaterialTheme.colorScheme.primary
        isActive -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val bgColor = when {
        isComplete -> MaterialTheme.colorScheme.primaryContainer
        isActive -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = bgColor,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (isComplete) {
                    Icon(Icons.Default.Check, null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp))
                } else {
                    Text("$step",
                        color = if (isActive) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium)
                }
            }
        }
        Text(label, style = MaterialTheme.typography.labelSmall,
            color = color)
    }
}
