package com.moneymate.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.moneymate.app.data.local.entity.LoanType
import com.moneymate.app.data.local.entity.Payment
import com.moneymate.app.data.local.entity.PaymentMode
import com.moneymate.app.data.local.entity.Person
import com.moneymate.app.navigation.Screen
import com.moneymate.app.ui.viewmodel.LoanFileViewModel
import com.moneymate.app.ui.viewmodel.PaymentViewModel
import com.moneymate.app.ui.viewmodel.PersonViewModel
import com.moneymate.app.ui.viewmodel.SettingsViewModel
import com.moneymate.app.ui.viewmodel.UploadState
import com.moneymate.app.ui.viewmodel.UploadViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableLazyListState
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

const val PAGE_SIZE = 20

enum class PaymentTypeFilter {
    ALL, UPI_GIVEN, CASH_GIVEN, UPI_RECEIVED, CASH_RECEIVED
}

fun PaymentTypeFilter.displayName(): String = when (this) {
    PaymentTypeFilter.ALL          -> "All"
    PaymentTypeFilter.UPI_GIVEN    -> "UPI Given"
    PaymentTypeFilter.CASH_GIVEN   -> "Cash Given"
    PaymentTypeFilter.UPI_RECEIVED -> "UPI Received"
    PaymentTypeFilter.CASH_RECEIVED-> "Cash Received"
}

/** Returns the most recent past occurrence of [dayOfWeek] (Calendar.FRIDAY / SATURDAY),
 *  at the start of that day (00:00:00). */
fun lastOccurrenceOf(dayOfWeek: Int): Long {
    val cal = Calendar.getInstance()
    while (cal.get(Calendar.DAY_OF_WEEK) != dayOfWeek) cal.add(Calendar.DAY_OF_YEAR, -1)
    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0);     cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

data class DayBreakdown(val label: String, val given: Double, val received: Double, val pending: Double, val weekStart: Long = 0L, val weekEnd: Long = 0L)

enum class CallNoNumberMode { NONE, ENTER_NUMBER, SELECT_CONTACT }
enum class ContactPickerTarget { NONE, ADD_DIALOG, EDIT_DIALOG, NO_NUMBER_DIALOG }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SlideToCallSheet(
    phoneNumber: String,
    personName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(Icons.Default.Phone, null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary)
            Text("Call $personName?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(phoneNumber, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

            // Slide button
            val offsetX = remember { androidx.compose.animation.core.Animatable(0f) }
            val trackWidth = 280.dp
            val thumbSize = 56.dp
            val maxSlide = with(androidx.compose.ui.platform.LocalDensity.current) { (trackWidth - thumbSize).toPx() }
            val coroutineScope = rememberCoroutineScope()
            var triggered by remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .width(trackWidth)
                    .height(thumbSize)
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    "Slide to call →",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                        .size(thumbSize)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .pointerInput(Unit) {
                            // Correctly implementing the horizontal drag gesture tracker
                            detectHorizontalDragGestures(
                                onDragStart = {},
                                onDragEnd = {
                                    if (offsetX.value >= maxSlide * 0.85f && !triggered) {
                                        triggered = true
                                        onConfirm()
                                    } else {
                                        coroutineScope.launch { offsetX.animateTo(0f, tween(300)) }
                                    }
                                },
                                onDragCancel = {
                                    coroutineScope.launch { offsetX.animateTo(0f, tween(300)) }
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    coroutineScope.launch {
                                        val newValue = offsetX.value + dragAmount
                                        offsetX.snapTo(newValue.coerceIn(0f, maxSlide))
                                    }
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
            TextButton(onClick = onDismiss) { Text("Cancel") }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FileDetailScreen(
    navController: NavHostController,
    fileId: String,
    personViewModel: PersonViewModel   = hiltViewModel(),
    loanFileViewModel: LoanFileViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel  = hiltViewModel(),
    uploadViewModel: UploadViewModel   = hiltViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val dateFormat     = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    LaunchedEffect(fileId) { personViewModel.loadPersonsForFile(fileId) }
    LaunchedEffect(fileId) { paymentViewModel.loadPaymentsForFile(fileId) }
    LaunchedEffect(fileId) { personViewModel.purgeExpiredCompletedPersons() }

    val persons                by personViewModel.persons.collectAsState()
    val deletedPersons         by personViewModel.deletedPersons.collectAsState()
    val pendingNewLoanPersons  by personViewModel.pendingNewLoanPersons.collectAsState()
    val completedPersons       by personViewModel.completedPersons.collectAsState()
    val files                  by loanFileViewModel.allFiles.collectAsState()
    val autoDeleteDays         by settingsViewModel.autoDeleteDays.collectAsState()
    val file                   = files.find { it.id == fileId }
    val uploadState            by uploadViewModel.uploadState.collectAsState()

    // NLR day-of-week mapping (drives date defaults + weeks filter)
    val targetDayOfWeek: Int? = remember(file?.name) {
        when {
            file?.name?.contains("NLR 1", ignoreCase = true) == true ||
                    file?.name?.contains("NLR 2", ignoreCase = true) == true -> Calendar.FRIDAY
            file?.name?.contains("NLR 3", ignoreCase = true) == true ||
                    file?.name?.contains("NLR 4", ignoreCase = true) == true -> Calendar.SATURDAY
            else -> null
        }
    }

    // Default date = last occurrence of the file's target day (or today for custom files)
    val defaultEntryDate: Long = remember(targetDayOfWeek) {
        targetDayOfWeek?.let { lastOccurrenceOf(it) } ?: System.currentTimeMillis()
    }

    // ── Transient UI state ──────────────────────────────────────────────────
    var showAddDialog          by remember { mutableStateOf(false) }
    var personToDelete         by remember { mutableStateOf<Person?>(null) }
    var personToEdit           by remember { mutableStateOf<Person?>(null) }
    var selectedIds            by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showMultiDeleteDialog  by remember { mutableStateOf(false) }
    var showTrash              by remember { mutableStateOf(false) }
    var selectedTrashIds       by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showMultiTrashDeleteDialog by remember { mutableStateOf(false) }
    var showMultiRestoreDialog by remember { mutableStateOf(false) }
    var showUploadConfirm      by remember { mutableStateOf(false) }
    var personToMarkComplete   by remember { mutableStateOf<Person?>(null) }
    // 3-dot menu
    var showThreeDotMenu       by remember { mutableStateOf(false) }
    // Call Now
    var personToCall           by remember { mutableStateOf<Person?>(null) }
    var showSlideToCall        by remember { mutableStateOf(false) }
    var showNoNumberDialog     by remember { mutableStateOf(false) }
    var noNumberEnterText      by remember { mutableStateOf("") }
    var noNumberMode           by remember { mutableStateOf<CallNoNumberMode>(CallNoNumberMode.NONE) }
    val slideToCallEnabled     by settingsViewModel.isSlideToCallEnabled.collectAsState()
    // View-by-date dialog
    var showViewDatePicker2    by remember { mutableStateOf(false) }
    var viewDate               by remember { mutableStateOf(System.currentTimeMillis()) }
    var showViewSheet          by remember { mutableStateOf(false) }
    var viewPersonFilter       by remember { mutableStateOf<Person?>(null) } // null = all persons
    // Add missed payment inside View sheet
    var viewAddPaymentPerson   by remember { mutableStateOf<Person?>(null) }
    var viewAddPaymentAmount   by remember { mutableStateOf("") }
    var viewAddPaymentMode     by remember { mutableStateOf(PaymentMode.CASH) }
    var viewAddPaymentType     by remember { mutableStateOf("RECEIVED") } // "GIVEN" or "RECEIVED"
    var personToActivate       by remember { mutableStateOf<Person?>(null) }
    var activateAmount         by remember { mutableStateOf("") }
    // Prompt to enter a loan amount when tapping a 0-amount active entry
    var showQuickAmountPrompt  by remember { mutableStateOf(false) }
    var targetedZeroPerson     by remember { mutableStateOf<Person?>(null) }
    var quickAmountInput       by remember { mutableStateOf("") }
    var totalsRevealed         by remember { mutableStateOf(false) }
    var autoHideJob            by remember { mutableStateOf<Job?>(null) }
    val holdProgress           = remember { Animatable(0f) }
    var showPlaceDialog        by remember { mutableStateOf(false) }
    var showMobileDialog       by remember { mutableStateOf(false) }
    var pendingPerson          by remember { mutableStateOf<Person?>(null) }
    var showFilterSheet        by remember { mutableStateOf(false) }
    var showSearch             by remember { mutableStateOf(false) }
    var showCompletedDialog    by remember { mutableStateOf(false) }
    val isSelecting            = selectedIds.isNotEmpty()
    val isSelectingTrash       = selectedTrashIds.isNotEmpty()

    // ── Fix 2/3: Completed person drag-to-delete state ───────────────────────
    var draggingCompletedPerson   by remember { mutableStateOf<Person?>(null) }
    var dragOffset                by remember { mutableStateOf(Offset.Zero) }
    var showDustbin               by remember { mutableStateOf(false) }
    var dustbinPosition           by remember { mutableStateOf(Offset.Zero) }
    var isOverDustbin             by remember { mutableStateOf(false) }
    var completedPersonToConfirmDelete by remember { mutableStateOf<Person?>(null) }
    // Animation for dustbin entrance
    val dustbinScale by animateFloatAsState(
        targetValue = if (showDustbin) (if (isOverDustbin) 1.3f else 1f) else 0f,
        animationSpec = tween(200),
        label = "dustbinScale"
    )

    // ── Add-dialog fields ───────────────────────────────────────────────────
    val context = LocalContext.current

    // Declared before contactPickerLauncher so the lambda can reference them
    var newMobile by remember { mutableStateOf("") }
    var editMobileFromContact by remember { mutableStateOf<String?>(null) }

    // Contact picker for Add/Edit dialog mobile field
    // Contact picker for Add/Edit dialog mobile field
    // Contact picker for Add/Edit dialog mobile field
    // Contact picker for Add/Edit dialog mobile field
    // Contact picker for Add/Edit dialog mobile field
    var contactPickerTarget by remember { mutableStateOf<ContactPickerTarget>(ContactPickerTarget.NONE) }
    val contactPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
        if (uri != null) {
            var number = ""
            try {
                val contactId = uri.lastPathSegment

                // Query via Unified Data URI to resolve the phone number correctly on modern Android versions
                val cursor = context.contentResolver.query(
                    android.provider.ContactsContract.Data.CONTENT_URI,
                    arrayOf(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER),
                    "${android.provider.ContactsContract.Data.CONTACT_ID} = ? AND ${android.provider.ContactsContract.Data.MIMETYPE} = ?",
                    arrayOf(contactId, android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE),
                    null
                )

                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val index = c.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                        if (index >= 0) {
                            number = c.getString(index)?.filter { ch -> ch.isDigit() || ch == '+' } ?: ""
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // ONLY execute assignment if we actually successfully retrieved a number
            if (number.isNotBlank()) {
                when (contactPickerTarget) {
                    ContactPickerTarget.ADD_DIALOG -> {
                        newMobile = number
                    }
                    ContactPickerTarget.EDIT_DIALOG -> {
                        editMobileFromContact = number
                    }
                    ContactPickerTarget.NO_NUMBER_DIALOG -> {
                        if (personToCall != null) {
                            personViewModel.updatePerson(personToCall!!.copy(mobileNumber = number))
                            personToCall = personToCall!!.copy(mobileNumber = number)
                            showNoNumberDialog = false
                            if (slideToCallEnabled) {
                                showSlideToCall = true
                            } else {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
                            }
                        }
                    }
                    ContactPickerTarget.NONE -> {}
                }
            }
            contactPickerTarget = ContactPickerTarget.NONE
        }
    }

    fun launchCall(person: Person) {
        val number = person.mobileNumber
        if (number.isNullOrBlank()) {
            personToCall = person; noNumberEnterText = ""; noNumberMode = CallNoNumberMode.NONE; showNoNumberDialog = true
        } else if (slideToCallEnabled) {
            personToCall = person; showSlideToCall = true
        } else {
            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
        }
    }

    var newName   by remember { mutableStateOf("") }
    var newPlace  by remember { mutableStateOf("") }
    var newAmount by remember { mutableStateOf("") }
    var newMode   by remember { mutableStateOf(PaymentMode.CASH) }
    var newType   by remember { mutableStateOf(LoanType.LENDING) }
    var newDate   by remember(defaultEntryDate) { mutableStateOf(defaultEntryDate) }
    var showNewDatePicker by remember { mutableStateOf(false) }
    var insertAfterName   by remember { mutableStateOf("") }
    var insertAfterSerial by remember { mutableStateOf("") }

    // ── Filter / search — ViewModel-backed ─────────────────────────────────
    val filterWeeks           by personViewModel.filterWeeks.collectAsState()
    val minAmount             by personViewModel.filterMinAmount.collectAsState()
    val maxAmount             by personViewModel.filterMaxAmount.collectAsState()
    val filterPaymentTypeState by personViewModel.filterPaymentType.collectAsState()
    val currentPage           by personViewModel.filterCurrentPage.collectAsState()
    val showOverallTotal      by personViewModel.filterShowOverallTotal.collectAsState()
    val searchQuery           by personViewModel.filterSearchQuery.collectAsState()
    val filterViewStartDate   by personViewModel.filterViewStartDate.collectAsState()
    val filterViewNumWeeks    by personViewModel.filterViewNumWeeks.collectAsState()

    val paymentTypeFilter: PaymentTypeFilter = when (filterPaymentTypeState) {
        PersonViewModel.PaymentTypeFilterState.UPI_GIVEN     -> PaymentTypeFilter.UPI_GIVEN
        PersonViewModel.PaymentTypeFilterState.CASH_GIVEN    -> PaymentTypeFilter.CASH_GIVEN
        PersonViewModel.PaymentTypeFilterState.UPI_RECEIVED  -> PaymentTypeFilter.UPI_RECEIVED
        PersonViewModel.PaymentTypeFilterState.CASH_RECEIVED -> PaymentTypeFilter.CASH_RECEIVED
        else -> PaymentTypeFilter.ALL
    }

    // True when "View" mode is active (start date + num weeks both set)
    val isViewMode = filterViewStartDate > 0L && filterViewNumWeeks.toIntOrNull() != null && filterViewNumWeeks.toIntOrNull()!! > 0

    val filePayments          by paymentViewModel.filePayments.collectAsState()
    val filePaymentsAll       by paymentViewModel.filePaymentsWithCompleted.collectAsState()
    val upiReceivedPersonIds  = remember(filePayments) { filePayments.filter { it.mode == PaymentMode.UPI  }.map { it.personId }.toSet() }
    val cashReceivedPersonIds = remember(filePayments) { filePayments.filter { it.mode == PaymentMode.CASH }.map { it.personId }.toSet() }
    val paymentsByPerson      = remember(filePayments) { filePayments.groupBy { it.personId } }
    val paidByPerson          = remember(paymentsByPerson) { paymentsByPerson.mapValues { (_, v) -> v.sumOf { it.amount } } }

    val isFiltered = filterWeeks.isNotBlank() || minAmount.isNotBlank() ||
            maxAmount.isNotBlank() || paymentTypeFilter != PaymentTypeFilter.ALL || isViewMode

    // Compute date range from N weeks of the target day
    val weekDateRange: Pair<Long, Long>? = remember(filterWeeks, targetDayOfWeek) {
        val n   = filterWeeks.toIntOrNull() ?: return@remember null
        val dow = targetDayOfWeek ?: return@remember null
        if (n <= 0) return@remember null
        val latest = Calendar.getInstance().apply {
            while (get(Calendar.DAY_OF_WEEK) != dow) add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59);     set(Calendar.MILLISECOND, 999)
        }
        val earliest = Calendar.getInstance().apply {
            timeInMillis = latest.timeInMillis
            add(Calendar.WEEK_OF_YEAR, -(n - 1))
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0);     set(Calendar.MILLISECOND, 0)
        }
        Pair(earliest.timeInMillis, latest.timeInMillis)
    }

    val filteredPersons = remember(
        persons, weekDateRange, minAmount, maxAmount,
        paymentTypeFilter, upiReceivedPersonIds, cashReceivedPersonIds, searchQuery, filePayments,
        paidByPerson
    ) {
        // A person is active only if not completed AND balance > 0
        var list = persons.filter { person ->
            val balance = person.amountGiven - (paidByPerson[person.id] ?: 0.0)
            !person.isCompleted && (person.amountGiven == 0.0 || balance > 0.0)
        }
        weekDateRange?.let { (s, e) ->
            val personIdsWithPaymentInRange = filePayments
                .filter { it.date in s..e }
                .map { it.personId }
                .toSet()
            list = list.filter { it.id in personIdsWithPaymentInRange }
        }
        minAmount.toDoubleOrNull()?.let { min -> list = list.filter { it.amountGiven >= min } }
        maxAmount.toDoubleOrNull()?.let { max -> list = list.filter { it.amountGiven <= max } }
        when (paymentTypeFilter) {
            PaymentTypeFilter.UPI_GIVEN     -> list = list.filter { it.mode == PaymentMode.UPI }
            PaymentTypeFilter.CASH_GIVEN    -> list = list.filter { it.mode == PaymentMode.CASH }
            PaymentTypeFilter.UPI_RECEIVED  -> list = list.filter { it.id in upiReceivedPersonIds }
            PaymentTypeFilter.CASH_RECEIVED -> list = list.filter { it.id in cashReceivedPersonIds }
            PaymentTypeFilter.ALL -> {}
        }
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                        it.place?.lowercase()?.contains(q) == true ||
                        it.mobileNumber?.lowercase()?.contains(q) == true
            }
        }
        list
    }

    LaunchedEffect(weekDateRange, minAmount, maxAmount, paymentTypeFilter, searchQuery) {
        personViewModel.filterCurrentPage.value = 0
    }

    val totalPages  = maxOf(1, (filteredPersons.size + PAGE_SIZE - 1) / PAGE_SIZE)
    val pagePersons = filteredPersons.drop(currentPage * PAGE_SIZE).take(PAGE_SIZE)
    val filteredPersonIds = remember(filteredPersons) { filteredPersons.map { it.id }.toSet() }
    val pagePersonIds     = remember(pagePersons) { pagePersons.map { it.id }.toSet() }

    // Totals
    val allPersons   = remember(filteredPersons, completedPersons) { filteredPersons + completedPersons }
    val allGiven     = remember(allPersons) { allPersons.sumOf { it.amountGiven } }
    val allCashGiven = remember(allPersons) { allPersons.filter { it.mode == PaymentMode.CASH }.sumOf { it.amountGiven } }
    val allUpiGiven  = remember(allPersons) { allPersons.filter { it.mode == PaymentMode.UPI  }.sumOf { it.amountGiven } }
    val allReceived  = remember(filePaymentsAll) { filePaymentsAll.sumOf { it.amount } }
    val allRecCash   = remember(filePaymentsAll) { filePaymentsAll.filter { it.mode == PaymentMode.CASH }.sumOf { it.amount } }
    val allRecUpi    = remember(filePaymentsAll) { filePaymentsAll.filter { it.mode == PaymentMode.UPI }.sumOf { it.amount } }
    val allBalance   = allGiven - allReceived

    val pageGiven     = remember(pagePersons) { pagePersons.sumOf { it.amountGiven } }
    val pageCashGiven = remember(pagePersons) { pagePersons.filter { it.mode == PaymentMode.CASH }.sumOf { it.amountGiven } }
    val pageUpiGiven  = remember(pagePersons) { pagePersons.filter { it.mode == PaymentMode.UPI  }.sumOf { it.amountGiven } }
    val pageReceived  = remember(pagePersonIds, filePayments) { filePayments.filter { it.personId in pagePersonIds }.sumOf { it.amount } }
    val pageRecCash   = remember(pagePersonIds, filePayments) { filePayments.filter { it.personId in pagePersonIds && it.mode == PaymentMode.CASH }.sumOf { it.amount } }
    val pageRecUpi    = remember(pagePersonIds, filePayments) { filePayments.filter { it.personId in pagePersonIds && it.mode == PaymentMode.UPI  }.sumOf { it.amount } }
    val pageBalance   = pageGiven - pageReceived

    @Suppress("UNUSED_VARIABLE")
    val totalGiven    = if (showOverallTotal) allGiven    else pageGiven
    @Suppress("UNUSED_VARIABLE")
    val totalCash     = if (showOverallTotal) allCashGiven else pageCashGiven
    @Suppress("UNUSED_VARIABLE")
    val totalUpi      = if (showOverallTotal) allUpiGiven else pageUpiGiven
    @Suppress("UNUSED_VARIABLE")
    val totalReceived = if (showOverallTotal) allReceived else pageReceived
    @Suppress("UNUSED_VARIABLE")
    val totalRecCashD = if (showOverallTotal) allRecCash  else pageRecCash
    @Suppress("UNUSED_VARIABLE")
    val totalRecUpiD  = if (showOverallTotal) allRecUpi   else pageRecUpi
    val balance       = if (showOverallTotal) allBalance  else pageBalance

    // ── Last target-day given & received (NLR files only) ─────────────────
    val allPersonIds = remember(allPersons) { allPersons.map { it.id }.toSet() }
    val lastWeekGiven: Double? = remember(targetDayOfWeek, allPersons) {
        val dow = targetDayOfWeek ?: return@remember null
        val weekStart = lastOccurrenceOf(dow)
        val weekEnd   = weekStart + 7L * 24 * 60 * 60 * 1000 - 1
        allPersons.filter { it.dateGiven in weekStart..weekEnd }.sumOf { it.amountGiven }
    }
    val lastWeekReceived: Double? = remember(targetDayOfWeek, filePaymentsAll, allPersonIds) {
        val dow = targetDayOfWeek ?: return@remember null
        val weekStart = lastOccurrenceOf(dow)
        val weekEnd   = weekStart + 7L * 24 * 60 * 60 * 1000 - 1
        filePaymentsAll.filter { it.personId in allPersonIds && it.date in weekStart..weekEnd }.sumOf { it.amount }
    }
    val lastWeekDayLabel: String? = when (targetDayOfWeek) {
        Calendar.FRIDAY   -> "Last Friday"
        Calendar.SATURDAY -> "Last Saturday"
        else              -> null
    }

    val dayBreakdowns: List<DayBreakdown> = remember(filterWeeks, filterViewStartDate, filterViewNumWeeks, targetDayOfWeek, allPersons, filePaymentsAll) {
        val dayFmt = SimpleDateFormat("EEE dd MMM", Locale.getDefault())

        // ── VIEW MODE: forward from a given start date ───────────────────────
        if (isViewMode) {
            val n = filterViewNumWeeks.toIntOrNull() ?: return@remember emptyList()
            val days = mutableListOf<Pair<Long, Long>>()
            val cal = Calendar.getInstance().apply { timeInMillis = filterViewStartDate }
            // Snap to start of that day
            cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
            repeat(n) { weekIndex ->
                val startMs = cal.timeInMillis
                val endMs = Calendar.getInstance().apply {
                    timeInMillis = startMs
                    set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
                    set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
                }.timeInMillis
                days.add(Pair(startMs, endMs))
                cal.add(Calendar.WEEK_OF_YEAR, 1) // move forward one week
            }
            return@remember days.map { (s, e) ->
                val dayPayments  = filePaymentsAll.filter { it.date in s..e }
                val dayPersonIds = dayPayments.map { it.personId }.toSet()
                val dayPersons   = allPersons.filter { it.id in dayPersonIds }
                val given    = dayPersons.sumOf { it.amountGiven }
                val received = dayPayments.sumOf { it.amount }
                DayBreakdown(dayFmt.format(Date(s)), given, received, given - received, weekStart = s, weekEnd = e)
            }
        }

        // ── EXISTING: look-back N weeks from today ───────────────────────────
        val n   = filterWeeks.toIntOrNull() ?: return@remember emptyList()
        val dow = targetDayOfWeek ?: return@remember emptyList()
        if (n <= 0) return@remember emptyList()
        val days = mutableListOf<Pair<Long, Long>>()
        val cal = Calendar.getInstance().apply {
            while (get(Calendar.DAY_OF_WEEK) != dow) add(Calendar.DAY_OF_YEAR, -1)
        }
        repeat(n) {
            val calCopy = cal.clone() as Calendar
            val start = calCopy.apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            val end = calCopy.apply {
                set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            days.add(Pair(start, end))
            cal.add(Calendar.WEEK_OF_YEAR, -1)
        }
        // Reverse so oldest day is first (e.g. 1 May, then 8 May)
        days.reversed().map { (s, e) ->
            val dayPayments  = filePaymentsAll.filter { it.date in s..e }
            val dayPersonIds = dayPayments.map { it.personId }.toSet()
            val dayPersons   = allPersons.filter { it.id in dayPersonIds }
            val given    = dayPersons.sumOf { it.amountGiven }
            val received = dayPayments.sumOf { it.amount }
            DayBreakdown(dayFmt.format(Date(s)), given, received, given - received, weekStart = s, weekEnd = e)
        }
    }

    @Suppress("UNUSED_VARIABLE")
    val summaryPager  = rememberPagerState(pageCount = { 2 })
    val daySummaryPager = if ((filterWeeks.isNotBlank() || isViewMode) && dayBreakdowns.isNotEmpty())
        rememberPagerState(pageCount = { dayBreakdowns.size + 1 })
    else null
    val reorderState  = rememberReorderableLazyListState(
        onMove = { from, to ->
            if (!isFiltered) {
                val gFrom = currentPage * PAGE_SIZE + from.index
                val gTo   = currentPage * PAGE_SIZE + to.index
                if (gFrom in persons.indices && gTo in persons.indices) {
                    val mut = persons.toMutableList()
                    mut.add(gTo, mut.removeAt(gFrom))
                    mut.forEachIndexed { i, p -> personViewModel.updateSortOrder(p.id, i) }
                }
            }
        }
    )

    BackHandler(enabled = isSelectingTrash) { selectedTrashIds = emptySet() }
    BackHandler(enabled = showTrash)        { showTrash = false; selectedTrashIds = emptySet() }
    BackHandler(enabled = isSelecting)      { selectedIds = emptySet() }
    BackHandler(enabled = showSearch)       { showSearch = false; personViewModel.filterSearchQuery.value = "" }
    BackHandler(enabled = isFiltered)       { personViewModel.clearFilters() }

    // Upload snackbar
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(uploadState) {
        when (val s = uploadState) {
            is UploadState.Success -> { snackbar.showSnackbar(s.message); uploadViewModel.resetState() }
            is UploadState.Error   -> { snackbar.showSnackbar(s.message); uploadViewModel.resetState() }
            else -> {}
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────
    fun clearAddFields() {
        newName = ""; newPlace = ""; newMobile = ""
        newAmount = ""; newMode = PaymentMode.CASH; newType = LoanType.LENDING
        newDate = defaultEntryDate; insertAfterName = ""; insertAfterSerial = ""
    }

    fun resolveAfterSortOrder(): Int? {
        val serial = insertAfterSerial.trim().toIntOrNull()
        if (serial != null) {
            val idx = serial - 1
            return if (idx in persons.indices) persons[idx].sortOrder else null
        }
        val name = insertAfterName.trim()
        if (name.isNotBlank()) return persons.firstOrNull { it.name.equals(name, ignoreCase = true) }?.sortOrder
        return null
    }

    fun doInsertPerson(person: Person, afterSortOrder: Int?) {
        coroutineScope.launch {
            if (afterSortOrder != null) {
                personViewModel.shiftSortOrdersAfterSync(fileId, afterSortOrder)
                personViewModel.insertPerson(person.copy(sortOrder = afterSortOrder + 1))
            } else {
                personViewModel.insertPerson(person.copy(sortOrder = persons.size))
            }
        }
    }

    fun attemptAddPerson() {
        val amount = newAmount.toDoubleOrNull() ?: return
        if (newName.isBlank()) return
        val after = resolveAfterSortOrder()
        coroutineScope.launch {
            val dups = personViewModel.findDuplicateByName(fileId, newName.trim())
            val base = Person(
                fileId = fileId, name = newName.trim(),
                place = newPlace.trim().ifEmpty { null },
                mobileNumber = newMobile.trim().ifEmpty { null },
                amountGiven = amount, mode = newMode,
                dateGiven = newDate, recordType = newType
            )
            if (dups.isEmpty()) {
                doInsertPerson(base, after); clearAddFields(); showAddDialog = false
            } else if (newPlace.isBlank()) {
                pendingPerson = base; showPlaceDialog = true
            } else {
                val samePlace = personViewModel.findDuplicateByNameAndPlace(fileId, newName.trim(), newPlace.trim())
                if (samePlace.isNotEmpty() && newMobile.isBlank()) {
                    pendingPerson = base; showMobileDialog = true
                } else {
                    doInsertPerson(base, after); clearAddFields(); showAddDialog = false
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            topBar = {
                when {
                    showTrash && isSelectingTrash -> TopAppBar(
                        title = { Text("${selectedTrashIds.size} selected", fontWeight = FontWeight.Bold) },
                        navigationIcon = { IconButton(onClick = { selectedTrashIds = emptySet() }) { Icon(Icons.Default.Close, null) } },
                        actions = {
                            val allTrashSelected = selectedTrashIds.size == deletedPersons.size && deletedPersons.isNotEmpty()
                            IconButton(onClick = {
                                selectedTrashIds = if (allTrashSelected) emptySet() else deletedPersons.map { it.id }.toSet()
                            }) {
                                Icon(
                                    if (allTrashSelected) Icons.Default.Close else Icons.Default.DoneAll,
                                    contentDescription = if (allTrashSelected) "Deselect All" else "Select All",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { showMultiRestoreDialog = true }) { Icon(Icons.Default.Restore, null, tint = MaterialTheme.colorScheme.primary) }
                            IconButton(onClick = { showMultiTrashDeleteDialog = true }) { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) }
                        }
                    )
                    showTrash -> TopAppBar(
                        title = { Text("Recently Deleted", fontWeight = FontWeight.Bold) },
                        navigationIcon = { IconButton(onClick = { showTrash = false; selectedTrashIds = emptySet() }) { Icon(Icons.Default.ArrowBack, null) } },
                        actions = {
                            if (deletedPersons.isNotEmpty()) {
                                IconButton(onClick = { selectedTrashIds = deletedPersons.map { it.id }.toSet() }) {
                                    Icon(Icons.Default.DoneAll, contentDescription = "Select All")
                                }
                            }
                        }
                    )
                    isSelecting -> TopAppBar(
                        title = { Text("${selectedIds.size} selected", fontWeight = FontWeight.Bold) },
                        navigationIcon = { IconButton(onClick = { selectedIds = emptySet() }) { Icon(Icons.Default.Close, null) } },
                        actions = {
                            val allSelected = selectedIds.size == filteredPersons.size && filteredPersons.isNotEmpty()
                            IconButton(onClick = {
                                selectedIds = if (allSelected) emptySet() else filteredPersons.map { it.id }.toSet()
                            }) {
                                Icon(
                                    if (allSelected) Icons.Default.Close else Icons.Default.DoneAll,
                                    contentDescription = if (allSelected) "Deselect All" else "Select All",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { showMultiDeleteDialog = true }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                        }
                    )
                    showSearch -> TopAppBar(
                        title = {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { personViewModel.filterSearchQuery.value = it },
                                placeholder = { Text("Search name, place, mobile…") },
                                singleLine = true, modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                )
                            )
                        },
                        navigationIcon = { IconButton(onClick = { showSearch = false; personViewModel.filterSearchQuery.value = "" }) { Icon(Icons.Default.ArrowBack, null) } }
                    )
                    else -> TopAppBar(
                        title = { Text(file?.name ?: "File Detail", fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = {
                                if (isFiltered) personViewModel.clearFilters()
                                else navController.popBackStack()
                            }) { Icon(Icons.Default.ArrowBack, null) }
                        },
                        actions = {
                            if (isFiltered || searchQuery.isNotBlank()) {
                                IconButton(onClick = { personViewModel.clearFilters() }) {
                                    Icon(Icons.Default.FilterAltOff, null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            IconButton(onClick = { showAddDialog = true }) {
                                Icon(Icons.Default.PersonAdd, contentDescription = "Add Person")
                            }
                            val anyUploaded = persons.any { it.uploadedAt != null && !it.isDeleted }
                            IconButton(onClick = { showUploadConfirm = true }) {
                                when {
                                    uploadState is UploadState.Uploading ->
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    anyUploaded ->
                                        Icon(Icons.Default.CloudDone, "Uploaded to Firebase",
                                            tint = MaterialTheme.colorScheme.primary)
                                    else ->
                                        Icon(Icons.Default.Upload, "Upload to Firebase")
                                }
                            }
                            IconButton(onClick = { showSearch = true }) { Icon(Icons.Default.Search, "Search") }
                            Box {
                                IconButton(onClick = { showThreeDotMenu = true }) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                                }
                                DropdownMenu(
                                    expanded = showThreeDotMenu,
                                    onDismissRequest = { showThreeDotMenu = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Filter") },
                                        leadingIcon = { Icon(Icons.Default.FilterList, null) },
                                        onClick = { showThreeDotMenu = false; showFilterSheet = true }
                                    )
                                    if (completedPersons.isNotEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("Completed (${completedPersons.size})") },
                                            leadingIcon = { Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) },
                                            onClick = { showThreeDotMenu = false; showCompletedDialog = true }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = { Text("Recently Deleted") },
                                        leadingIcon = { Icon(Icons.Default.Delete, null) },
                                        onClick = { showThreeDotMenu = false; showTrash = true }
                                    )
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("View by Date") },
                                        leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                                        onClick = {
                                            showThreeDotMenu = false
                                            viewPersonFilter = null
                                            showViewDatePicker2 = true
                                        }
                                    )
                                }
                            }
                        }
                    )
                }
            },
            floatingActionButton = {}
        ) { padding ->
            if (showTrash) {
                TrashContent(
                    deletedPersons = deletedPersons, autoDeleteDays = autoDeleteDays,
                    isSelectingTrash = isSelectingTrash, selectedTrashIds = selectedTrashIds, padding = padding,
                    onToggleSelect = { id -> selectedTrashIds = if (id in selectedTrashIds) selectedTrashIds - id else selectedTrashIds + id },
                    onLongSelect   = { id -> selectedTrashIds = selectedTrashIds + id },
                    onRestore      = { personViewModel.restorePerson(it) },
                    onHardDelete   = { personViewModel.hardDeletePerson(it) }
                )
            } else {
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {

                    if (isFiltered) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (filterWeeks.isNotBlank()) {
                                val dowLabel = when (targetDayOfWeek) { Calendar.FRIDAY -> "Fri"; Calendar.SATURDAY -> "Sat"; else -> "Day" }
                                FilterChip(selected = true, onClick = { personViewModel.filterWeeks.value = "" },
                                    label = { Text("Last $filterWeeks ${dowLabel}s", style = MaterialTheme.typography.labelSmall) },
                                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) })
                            }
                            if (isViewMode) {
                                val viewFmt = SimpleDateFormat("dd MMM", Locale.getDefault())
                                FilterChip(selected = true, onClick = { personViewModel.filterViewStartDate.value = 0L; personViewModel.filterViewNumWeeks.value = "" },
                                    label = { Text("View: ${viewFmt.format(Date(filterViewStartDate))} × ${filterViewNumWeeks}wks", style = MaterialTheme.typography.labelSmall) },
                                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) })
                            }
                            if (minAmount.isNotBlank() || maxAmount.isNotBlank()) {
                                FilterChip(selected = true,
                                    onClick = { personViewModel.filterMinAmount.value = ""; personViewModel.filterMaxAmount.value = "" },
                                    label = { Text("₹${minAmount.ifBlank { "0" }}–₹${maxAmount.ifBlank { "∞" }}", style = MaterialTheme.typography.labelSmall) },
                                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) })
                            }
                            if (paymentTypeFilter != PaymentTypeFilter.ALL) {
                                FilterChip(selected = true,
                                    onClick = { personViewModel.filterPaymentType.value = PersonViewModel.PaymentTypeFilterState.ALL },
                                    label = { Text(paymentTypeFilter.displayName(), style = MaterialTheme.typography.labelSmall) },
                                    trailingIcon = { Icon(Icons.Default.Close, null, Modifier.size(14.dp)) })
                            }
                        }
                    }

                    LaunchedEffect(totalsRevealed) {
                        autoHideJob?.cancel()
                        if (totalsRevealed) {
                            autoHideJob = coroutineScope.launch {
                                delay(2 * 60 * 1000L)
                                totalsRevealed = false
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .pointerInput(totalsRevealed) {
                                detectTapGestures(
                                    onTap = {
                                        if (totalsRevealed) {
                                            totalsRevealed = false
                                            autoHideJob?.cancel()
                                            coroutineScope.launch { holdProgress.snapTo(0f) }
                                        }
                                    },
                                    onPress = {
                                        if (!totalsRevealed) {
                                            val job = coroutineScope.launch {
                                                holdProgress.snapTo(0f)
                                                holdProgress.animateTo(1f, tween(1500))
                                                if (holdProgress.value >= 1f) totalsRevealed = true
                                            }
                                            tryAwaitRelease()
                                            if (!totalsRevealed) {
                                                job.cancel()
                                                coroutineScope.launch { holdProgress.snapTo(0f) }
                                            }
                                        }
                                    }
                                )
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                            Box(
                                modifier = Modifier.fillMaxWidth().pointerInput(Unit) {
                                    awaitPointerEventScope {
                                        while (true) { awaitPointerEvent() }
                                    }
                                }
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                    FilterChip(selected = !showOverallTotal, onClick = { personViewModel.filterShowOverallTotal.value = false },
                                        label = { Text("Page ${currentPage + 1}/$totalPages", style = MaterialTheme.typography.labelSmall) })
                                    Spacer(Modifier.width(8.dp))
                                    FilterChip(selected = showOverallTotal, onClick = { personViewModel.filterShowOverallTotal.value = true },
                                        label = { Text("Overall", style = MaterialTheme.typography.labelSmall) })
                                }
                            }
                            Spacer(Modifier.height(4.dp))

                            if (!totalsRevealed) {
                                val progValue by holdProgress.asState()
                                val isRevealing = progValue > 0f
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        if (isRevealing) "Revealing…" else "Hold to view totals",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isRevealing) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.65f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(progValue)
                                                .fillMaxHeight()
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(MaterialTheme.colorScheme.primary)
                                        )
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "Balance: ₹••••",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            AnimatedVisibility(
                                visible = totalsRevealed,
                                enter = fadeIn(tween(400)),
                                exit = fadeOut(tween(300))
                            ) {
                                Column(Modifier.fillMaxWidth()) {
                                    if ((filterWeeks.isNotBlank() || isViewMode) && dayBreakdowns.isNotEmpty() && daySummaryPager != null) {
                                        Column(Modifier.fillMaxWidth()) {
                                            HorizontalPager(
                                                state = daySummaryPager,
                                                modifier = Modifier.fillMaxWidth()
                                            ) { page ->
                                                if (page < dayBreakdowns.size) {
                                                    val day = dayBreakdowns[page]
                                                    Column(
                                                        Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(
                                                            day.label,
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                        Spacer(Modifier.height(8.dp))
                                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                Text("Given", style = MaterialTheme.typography.labelSmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                Text(
                                                                    if (day.given == 0.0) "Nil" else "₹${day.given}",
                                                                    style = MaterialTheme.typography.titleMedium,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = MaterialTheme.colorScheme.primary
                                                                )
                                                            }
                                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                Text("Returned", style = MaterialTheme.typography.labelSmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                Text(
                                                                    if (day.received == 0.0) "Nil" else "₹${day.received}",
                                                                    style = MaterialTheme.typography.titleMedium,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = MaterialTheme.colorScheme.tertiary
                                                                )
                                                            }
                                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                Text("Pending", style = MaterialTheme.typography.labelSmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                Text(
                                                                    if (day.pending == 0.0) "Nil" else "₹${day.pending}",
                                                                    style = MaterialTheme.typography.titleMedium,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (day.pending > 0) MaterialTheme.colorScheme.error
                                                                    else MaterialTheme.colorScheme.primary
                                                                )
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    val totalG = dayBreakdowns.sumOf { it.given }
                                                    val totalR = dayBreakdowns.sumOf { it.received }
                                                    val totalP = dayBreakdowns.sumOf { it.pending }
                                                    Column(
                                                        Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally
                                                    ) {
                                                        Text(
                                                            if (isViewMode) "All ${dayBreakdowns.size} Weeks" else "All ${dayBreakdowns.size} ${if (targetDayOfWeek == Calendar.FRIDAY) "Fridays" else "Saturdays"}",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                                        )
                                                        Spacer(Modifier.height(8.dp))
                                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                Text("Total Given", style = MaterialTheme.typography.labelSmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                Text("₹$totalG", style = MaterialTheme.typography.titleMedium,
                                                                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                            }
                                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                Text("Total Returned", style = MaterialTheme.typography.labelSmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                Text("₹$totalR", style = MaterialTheme.typography.titleMedium,
                                                                    fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                                            }
                                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                Text("Total Pending", style = MaterialTheme.typography.labelSmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                Text("₹$totalP", style = MaterialTheme.typography.titleMedium,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = if (totalP > 0) MaterialTheme.colorScheme.error
                                                                    else MaterialTheme.colorScheme.primary)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            Spacer(Modifier.height(6.dp))
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                                                repeat(dayBreakdowns.size + 1) { i ->
                                                    Box(Modifier.padding(horizontal = 3.dp).size(if (daySummaryPager.currentPage == i) 8.dp else 5.dp).background(
                                                        if (daySummaryPager.currentPage == i) MaterialTheme.colorScheme.primary
                                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), CircleShape))
                                                }
                                            }
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                if (daySummaryPager.currentPage < dayBreakdowns.size)
                                                    "← swipe → (${daySummaryPager.currentPage + 1}/${dayBreakdowns.size + 1})"
                                                else "← swipe for individual days",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.align(Alignment.CenterHorizontally)
                                            )
                                        }
                                    } else {
                                        Column(
                                            Modifier.fillMaxWidth(),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                "Total Balance",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(Modifier.height(4.dp))
                                            Text(
                                                "₹$balance",
                                                style = MaterialTheme.typography.headlineLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = if (balance > 0) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.error
                                            )

                                            if (lastWeekDayLabel != null && lastWeekGiven != null && lastWeekReceived != null) {
                                                Spacer(Modifier.height(12.dp))
                                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                                Spacer(Modifier.height(8.dp))
                                                Text(
                                                    lastWeekDayLabel,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Spacer(Modifier.height(6.dp))
                                                Row(
                                                    Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceEvenly
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text("Given", style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        Text(
                                                            if (lastWeekGiven == 0.0) "Nil" else "₹$lastWeekGiven",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text("Received", style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                        Text(
                                                            if (lastWeekReceived == 0.0) "Nil" else "₹$lastWeekReceived",
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.tertiary
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (totalsRevealed) {
                                        Text(
                                            "Tap card to hide",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (filteredPersons.isEmpty() && pendingNewLoanPersons.isEmpty() && completedPersons.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(if (persons.isEmpty()) "No persons yet. Tap + to add." else "No results match your filters.",
                                style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        val listPager = rememberPagerState(initialPage = 0, pageCount = { totalPages })
                        LaunchedEffect(listPager.currentPage) { personViewModel.filterCurrentPage.value = listPager.currentPage }
                        LaunchedEffect(currentPage) { if (listPager.currentPage != currentPage) listPager.scrollToPage(currentPage) }

                        val dateColPager = daySummaryPager

                        Column(Modifier.fillMaxSize()) {
                            if (!isSelecting && !showTrash) {
                                OutlinedButton(
                                    onClick = { showAddDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Add New Person")
                                }
                            }

                            if (totalPages > 1) {
                                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { if (currentPage > 0) personViewModel.filterCurrentPage.value = currentPage - 1 }, enabled = currentPage > 0) { Icon(Icons.Default.ChevronLeft, null) }
                                    Text("Page ${currentPage + 1} of $totalPages  •  ${filteredPersons.size} total", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    IconButton(onClick = { if (currentPage < totalPages - 1) personViewModel.filterCurrentPage.value = currentPage + 1 }, enabled = currentPage < totalPages - 1) { Icon(Icons.Default.ChevronRight, null) }
                                }
                            }

                            if (dateColPager != null) {
                                val currentColPage = dateColPager.currentPage
                                val colLabel = if (currentColPage < dayBreakdowns.size)
                                    dayBreakdowns[currentColPage].label
                                else "Total"
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                                ) {
                                    Row(
                                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = { if (currentColPage > 0) coroutineScope.launch { dateColPager.animateScrollToPage(currentColPage - 1) } },
                                            enabled = currentColPage > 0
                                        ) { Icon(Icons.Default.ChevronLeft, null, Modifier.size(18.dp)) }

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                colLabel,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                            Text(
                                                "${currentColPage + 1} / ${dayBreakdowns.size + 1}  •  swipe cards ←→",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                            )
                                        }

                                        IconButton(
                                            onClick = { if (currentColPage < dayBreakdowns.size) coroutineScope.launch { dateColPager.animateScrollToPage(currentColPage + 1) } },
                                            enabled = currentColPage < dayBreakdowns.size
                                        ) { Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp)) }
                                    }
                                }
                            }

                            val mergedPersons = remember(filteredPersons, pendingNewLoanPersons) {
                                (filteredPersons + pendingNewLoanPersons).sortedBy { it.sortOrder }
                            }

                            HorizontalPager(state = listPager, Modifier.fillMaxSize()) { page ->
                                val pageItems   = mergedPersons.drop(page * PAGE_SIZE).take(PAGE_SIZE)
                                val globalStart = page * PAGE_SIZE
                                val pageListState = androidx.compose.foundation.lazy.rememberLazyListState()
                                LazyColumn(
                                    state = pageListState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    itemsIndexed(pageItems, key = { _, p -> p.id }) { idx, person ->
                                        val serial = globalStart + idx + 1

                                        if (person.isPendingNewLoan) {
                                            PendingNewLoanCard(
                                                person = person, dateFormat = dateFormat,
                                                onTap = { personToActivate = person; activateAmount = "" }
                                            )
                                        } else {
                                            val isDragging = reorderState.draggingItemKey == person.id
                                            val elevation  = if (isDragging) 8.dp else 2.dp
                                            val isSelected = person.id in selectedIds
                                            val paid       = paidByPerson[person.id] ?: 0.0
                                            val pending    = person.amountGiven - paid
                                            SwipeablePersonCard(
                                                person = person, serialNumber = serial,
                                                totalPaid = paid, pending = pending,
                                                isSelected = isSelected, isSelecting = isSelecting,
                                                elevation = elevation, reorderState = reorderState,
                                                showWeeksColumns = filterWeeks.isNotBlank() || isViewMode,
                                                dateFormat = dateFormat,
                                                dayBreakdowns = dayBreakdowns,
                                                personPayments = paymentsByPerson[person.id] ?: emptyList(),
                                                dateColPager = dateColPager,
                                                onClick = {
                                                    if (isSelecting) {
                                                        selectedIds = if (isSelected) selectedIds - person.id else selectedIds + person.id
                                                    } else if (person.amountGiven == 0.0) {
                                                        targetedZeroPerson = person
                                                        quickAmountInput = ""
                                                        showQuickAmountPrompt = true
                                                    } else {
                                                        navController.navigate("person_detail/${person.id}")
                                                    }
                                                },
                                                onLongClick = { selectedIds = selectedIds + person.id },
                                                onDelete = { personToDelete = person },
                                                onEdit   = { personToEdit   = person },
                                                onMarkComplete = { personToMarkComplete = person },
                                                onView = { viewPersonFilter = person; showViewDatePicker2 = true },
                                                onCallNow = { launchCall(person) },
                                                onQuickPayment = { amount, mode ->
                                                    coroutineScope.launch {
                                                        paymentViewModel.insertPayment(
                                                            Payment(
                                                                personId = person.id,
                                                                amount   = amount,
                                                                mode     = mode,
                                                                date     = System.currentTimeMillis()
                                                            )
                                                        )
                                                        // Auto-complete: if the new payment brings balance to zero,
                                                        // markAsCompleted handles both marking completed AND creating
                                                        // the fresh 0-amount pending-new-loan placeholder internally.
                                                        val newTotalPaid = (paidByPerson[person.id] ?: 0.0) + amount
                                                        val remainingBalance = person.amountGiven - newTotalPaid
                                                        if (remainingBalance <= 0.0 && person.amountGiven > 0) {
                                                            personViewModel.markAsCompleted(person)
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Fix 2: Dustbin overlay (shown on completed-person long press) ────────
        if (showDustbin) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.15f))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .onGloballyPositioned { coords ->
                        dustbinPosition = coords.positionInWindow()
                    }
                    .scale(dustbinScale)
                    .background(
                        if (isOverDustbin) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .padding(20.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Drop to delete",
                    tint = if (isOverDustbin) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                "Drop here to delete",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp)
            )
        }

        // ── Fix 2: Floating drag ghost ──────────────────────────────────────────
        if (draggingCompletedPerson != null) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(dragOffset.x.roundToInt() - 80, dragOffset.y.roundToInt() - 40) }
                    .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    draggingCompletedPerson!!.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }

    // ── Mark as Completed dialog ──────────────────────────────────────────────
    personToMarkComplete?.let { p ->
        val paid    = (paymentsByPerson[p.id] ?: emptyList()).sumOf { it.amount }
        val balance = p.amountGiven - paid
        AlertDialog(
            onDismissRequest = { personToMarkComplete = null },
            title = { Text("Mark ${p.name} as Completed?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (balance > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            Text("Still pending - are you sure they have fully repaid?",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    Text("This will:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Text("Move ${p.name} to the Completed section (visible for 180 days)", style = MaterialTheme.typography.bodySmall)
                    Text("Create a Rs.0 placeholder in the main list (Pending New Loan)", style = MaterialTheme.typography.bodySmall)
                    Text("Retain all name, place, and mobile details on the placeholder", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { personViewModel.markAsCompleted(p); personToMarkComplete = null }) {
                    Text("Mark Completed", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = { TextButton(onClick = { personToMarkComplete = null }) { Text("Cancel") } }
        )
    }

    // ── Fix 2/3: Completed persons bottom sheet with drag-to-delete ───────────
    if (showCompletedDialog) {
        ModalBottomSheet(
            onDismissRequest = { showCompletedDialog = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            val completedPaymentsMap = remember(completedPersons, filePaymentsAll) {
                completedPersons.associate { comp ->
                    comp.id to filePaymentsAll.filter { it.personId == comp.id }
                }
            }
            Column(Modifier.fillMaxWidth()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Completed (${completedPersons.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showCompletedDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                    Text(
                        "Long press a card to drag and drop onto the bin to delete  •  Auto-purges after 180 days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(Modifier.padding(top = 8.dp))
                }
                // ── Fix 7: Group completed persons by completion date (newest first) ──
                val completedGroupFmt = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

                // Build an ordered list of (dateLabel, List<Person>) pairs.
                // completedPersons is already sorted completedAt DESC from the DAO,
                // so we preserve that order while grouping by calendar day.
                val completedByDate: List<Pair<String, List<Person>>> = remember(completedPersons) {
                    val cal = Calendar.getInstance()
                    // Group preserving existing DESC order — LinkedHashMap keeps insertion order.
                    val map = linkedMapOf<String, MutableList<Person>>()
                    completedPersons.forEach { comp ->
                        cal.timeInMillis = comp.completedAt ?: comp.dateGiven
                        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
                        val label = completedGroupFmt.format(cal.time)
                        map.getOrPut(label) { mutableListOf() }.add(comp)
                    }
                    map.entries.map { (label, persons) -> label to persons }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.85f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    completedByDate.forEach { (dateLabel, personsOnDate) ->
                        // ── Date header ───────────────────────────────────────
                        stickyHeader(key = "header_$dateLabel") {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    text = dateLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                        }
                        // ── Cards under this date ─────────────────────────────
                        itemsIndexed(personsOnDate, key = { _, c -> c.id }) { _, comp ->
                            val compPayments = completedPaymentsMap[comp.id] ?: emptyList()
                            val daysLeft = 180 - ((System.currentTimeMillis() - (comp.completedAt ?: 0L)) / (1000 * 60 * 60 * 24)).toInt()
                            Box(Modifier.padding(horizontal = 16.dp, vertical = 5.dp)) {
                                DraggableCompletedPersonCard(
                                    person = comp,
                                    daysLeft = daysLeft,
                                    dateFormat = dateFormat,
                                    payments = compPayments,
                                    onDragStarted = {
                                        draggingCompletedPerson = comp
                                        showDustbin = true
                                    },
                                    onDragMoved = { offset ->
                                        dragOffset = offset
                                        val dustbinCenterX = dustbinPosition.x + 38f
                                        val dustbinCenterY = dustbinPosition.y + 38f
                                        isOverDustbin = (offset.x - dustbinCenterX).let { dx ->
                                            (offset.y - dustbinCenterY).let { dy ->
                                                dx * dx + dy * dy < 100f * 100f
                                            }
                                        }
                                    },
                                    onDragEnded = {
                                        if (isOverDustbin && draggingCompletedPerson != null) {
                                            completedPersonToConfirmDelete = draggingCompletedPerson
                                        }
                                        draggingCompletedPerson = null
                                        dragOffset = Offset.Zero
                                        showDustbin = false
                                        isOverDustbin = false
                                    }
                                )
                            }
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }

    // ── Fix 2: Confirmation dialog after drag-drop onto dustbin ──────────────
    completedPersonToConfirmDelete?.let { comp ->
        AlertDialog(
            onDismissRequest = { completedPersonToConfirmDelete = null },
            title = { Text("Delete ${comp.name}?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("This will move ${comp.name} to Recently Deleted.", style = MaterialTheme.typography.bodyMedium)
                    Text("They can be restored within 180 days.", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    personViewModel.softDeleteCompletedPerson(comp.id)
                    completedPersonToConfirmDelete = null
                    if (completedPersons.size <= 1) showCompletedDialog = false
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { completedPersonToConfirmDelete = null }) { Text("Cancel") } }
        )
    }

    // ── Zero-amount active entry: prompt for loan amount ──────────────────────
    if (showQuickAmountPrompt && targetedZeroPerson != null) {
        AlertDialog(
            onDismissRequest = { showQuickAmountPrompt = false; targetedZeroPerson = null; quickAmountInput = "" },
            title = { Text("Enter Loan Amount for ${targetedZeroPerson!!.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "This entry has no loan amount yet. Enter the amount to activate it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = quickAmountInput,
                        onValueChange = { quickAmountInput = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Loan Amount*") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val entered = quickAmountInput.toDoubleOrNull()
                        if (entered != null && entered > 0) {
                            personViewModel.updatePerson(targetedZeroPerson!!.copy(amountGiven = entered))
                            showQuickAmountPrompt = false
                            targetedZeroPerson = null
                            quickAmountInput = ""
                        }
                    },
                    enabled = quickAmountInput.toDoubleOrNull()?.let { it > 0 } == true
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showQuickAmountPrompt = false; targetedZeroPerson = null; quickAmountInput = "" }) { Text("Cancel") }
            }
        )
    }

    // ── Activate pending-new-loan dialog ──────────────────────────────────────
    personToActivate?.let { p ->
        AlertDialog(
            onDismissRequest = { personToActivate = null; activateAmount = "" },
            title = { Text("New Loan Amount for ${p.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter the new loan amount to activate this record.",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = activateAmount,
                        onValueChange = { activateAmount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("New Loan Amount*") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amt = activateAmount.toDoubleOrNull()
                    if (amt != null && amt > 0) { personViewModel.activatePendingNewLoan(p.id, amt); personToActivate = null; activateAmount = "" }
                }) { Text("Activate") }
            },
            dismissButton = { TextButton(onClick = { personToActivate = null; activateAmount = "" }) { Text("Cancel") } }
        )
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Filter sheet
    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Payment Type", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(PaymentTypeFilter.ALL to "All", PaymentTypeFilter.UPI_GIVEN to "UPI Given", PaymentTypeFilter.CASH_GIVEN to "Cash Given").forEach { (opt, lbl) ->
                        FilterChip(selected = paymentTypeFilter == opt,
                            onClick = { personViewModel.filterPaymentType.value = when (opt) {
                                PaymentTypeFilter.UPI_GIVEN -> PersonViewModel.PaymentTypeFilterState.UPI_GIVEN
                                PaymentTypeFilter.CASH_GIVEN -> PersonViewModel.PaymentTypeFilterState.CASH_GIVEN
                                PaymentTypeFilter.UPI_RECEIVED -> PersonViewModel.PaymentTypeFilterState.UPI_RECEIVED
                                PaymentTypeFilter.CASH_RECEIVED -> PersonViewModel.PaymentTypeFilterState.CASH_RECEIVED
                                else -> PersonViewModel.PaymentTypeFilterState.ALL
                            } },
                            label = { Text(lbl, style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(PaymentTypeFilter.UPI_RECEIVED to "UPI Received", PaymentTypeFilter.CASH_RECEIVED to "Cash Received").forEach { (opt, lbl) ->
                        FilterChip(selected = paymentTypeFilter == opt,
                            onClick = { personViewModel.filterPaymentType.value = when (opt) {
                                PaymentTypeFilter.UPI_GIVEN -> PersonViewModel.PaymentTypeFilterState.UPI_GIVEN
                                PaymentTypeFilter.CASH_GIVEN -> PersonViewModel.PaymentTypeFilterState.CASH_GIVEN
                                PaymentTypeFilter.UPI_RECEIVED -> PersonViewModel.PaymentTypeFilterState.UPI_RECEIVED
                                PaymentTypeFilter.CASH_RECEIVED -> PersonViewModel.PaymentTypeFilterState.CASH_RECEIVED
                                else -> PersonViewModel.PaymentTypeFilterState.ALL
                            } },
                            label = { Text(lbl, style = MaterialTheme.typography.labelSmall) }, modifier = Modifier.weight(1f))
                    }
                }
                HorizontalDivider()
                Text("View", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Pick a start date and number of weeks. Each column shows that week's date and amount received.",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                var showViewDatePicker by remember { mutableStateOf(false) }
                val viewDateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
                OutlinedButton(
                    onClick = { showViewDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.CalendarToday, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (filterViewStartDate > 0L) "Start: ${viewDateFmt.format(Date(filterViewStartDate))}" else "Pick Start Date")
                }
                if (showViewDatePicker) {
                    val dpState = rememberDatePickerState(
                        initialSelectedDateMillis = if (filterViewStartDate > 0L) filterViewStartDate else System.currentTimeMillis()
                    )
                    DatePickerDialog(
                        onDismissRequest = { showViewDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                dpState.selectedDateMillis?.let { personViewModel.filterViewStartDate.value = it }
                                showViewDatePicker = false
                            }) { Text("OK") }
                        },
                        dismissButton = { TextButton(onClick = { showViewDatePicker = false }) { Text("Cancel") } }
                    ) { DatePicker(state = dpState) }
                }

                OutlinedTextField(
                    value = filterViewNumWeeks,
                    onValueChange = { personViewModel.filterViewNumWeeks.value = it.filter { c -> c.isDigit() } },
                    label = { Text("Number of Weeks") }, placeholder = { Text("e.g. 5") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    trailingIcon = { if (filterViewNumWeeks.isNotBlank()) IconButton(onClick = { personViewModel.filterViewNumWeeks.value = "" }) { Icon(Icons.Default.Close, null) } }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("1", "2", "4", "5", "8", "12").forEach { n ->
                        FilterChip(selected = filterViewNumWeeks == n,
                            onClick = { personViewModel.filterViewNumWeeks.value = if (filterViewNumWeeks == n) "" else n },
                            label = { Text(n, style = MaterialTheme.typography.labelSmall) })
                    }
                }
                if (isViewMode) {
                    TextButton(onClick = { personViewModel.filterViewStartDate.value = 0L; personViewModel.filterViewNumWeeks.value = "" }) {
                        Text("Clear View", color = MaterialTheme.colorScheme.error)
                    }
                }
                HorizontalDivider()
                Text("Amount Range", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(value = minAmount, onValueChange = { personViewModel.filterMinAmount.value = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Min ₹") }, singleLine = true, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    OutlinedTextField(value = maxAmount, onValueChange = { personViewModel.filterMaxAmount.value = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("Max ₹") }, singleLine = true, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                }
                if (minAmount.isNotBlank() || maxAmount.isNotBlank()) {
                    TextButton(onClick = { personViewModel.filterMinAmount.value = ""; personViewModel.filterMaxAmount.value = "" }) {
                        Text("Clear Amount", color = MaterialTheme.colorScheme.error)
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    // Add Person dialog
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; clearAddFields() },
            title = { Text("Add Person") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = newType == LoanType.LENDING,   onClick = { newType = LoanType.LENDING },   label = { Text("Lending (I gave)")   })
                        FilterChip(selected = newType == LoanType.BORROWING, onClick = { newType = LoanType.BORROWING }, label = { Text("Borrowing (I owe)") })
                    }
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Name*") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newPlace, onValueChange = { newPlace = it }, label = { Text("Place (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newMobile, onValueChange = { newMobile = it.filter { c -> c.isDigit() || c == '+' || c == '-' || c == ' ' } },
                        label = { Text("Mobile (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        trailingIcon = {
                            IconButton(onClick = { contactPickerTarget = ContactPickerTarget.ADD_DIALOG; contactPickerLauncher.launch(null) }) {
                                Icon(Icons.Default.Contacts, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        })
                    OutlinedTextField(value = newAmount, onValueChange = { newAmount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(if (newType == LoanType.LENDING) "Amount Given*" else "Amount Borrowed*") },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = newMode == PaymentMode.CASH, onClick = { newMode = PaymentMode.CASH }, label = { Text("Cash") })
                        FilterChip(selected = newMode == PaymentMode.UPI,  onClick = { newMode = PaymentMode.UPI  }, label = { Text("UPI")  })
                    }
                    OutlinedButton(onClick = { showNewDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(dateFormat.format(Date(newDate)))
                    }
                    HorizontalDivider()
                    Text("Insert after (optional)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(value = insertAfterName, onValueChange = { insertAfterName = it; if (it.isNotBlank()) insertAfterSerial = "" },
                            label = { Text("Name") }, singleLine = true, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = insertAfterSerial, onValueChange = { insertAfterSerial = it.filter { c -> c.isDigit() }; if (it.isNotBlank()) insertAfterName = "" },
                            label = { Text("# No.") }, singleLine = true, modifier = Modifier.weight(0.5f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    }
                }
            },
            confirmButton = { TextButton(onClick = { attemptAddPerson() }) { Text("Add") } },
            dismissButton = { TextButton(onClick = { showAddDialog = false; clearAddFields() }) { Text("Cancel") } }
        )
    }

    if (showNewDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = newDate)
        DatePickerDialog(
            onDismissRequest = { showNewDatePicker = false },
            confirmButton = { TextButton(onClick = { newDate = state.selectedDateMillis ?: newDate; showNewDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showNewDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }

    // Edit Person dialog
    personToEdit?.let { orig ->
        var editName   by remember { mutableStateOf(orig.name) }
        var editPlace  by remember { mutableStateOf(orig.place ?: "") }
        var editMobile by remember { mutableStateOf(orig.mobileNumber ?: "") }
        // Sync contact picker result
        LaunchedEffect(editMobileFromContact) {
            editMobileFromContact?.let { editMobile = it; editMobileFromContact = null }
        }
        var editAmount by remember { mutableStateOf(orig.amountGiven.toBigDecimal().stripTrailingZeros().toPlainString()) }
        var editMode   by remember { mutableStateOf(orig.mode) }
        var editType   by remember { mutableStateOf(orig.recordType) }
        var editDate   by remember { mutableStateOf(orig.dateGiven) }
        var editMoveAfterName   by remember { mutableStateOf("") }
        var editMoveAfterSerial by remember { mutableStateOf("") }
        var showEditDatePicker by remember { mutableStateOf(false) }

        fun resolveMoveAfterSortOrder(): Int? {
            val serial = editMoveAfterSerial.trim().toIntOrNull()
            if (serial != null) {
                val idx = serial - 1
                return if (idx in persons.indices) persons[idx].sortOrder else null
            }
            val name = editMoveAfterName.trim()
            if (name.isNotBlank()) return persons.firstOrNull { it.name.equals(name, ignoreCase = true) }?.sortOrder
            return null
        }

        AlertDialog(
            onDismissRequest = { personToEdit = null },
            title = { Text("Edit Person") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = editType == LoanType.LENDING,   onClick = { editType = LoanType.LENDING },   label = { Text("Lending")   })
                        FilterChip(selected = editType == LoanType.BORROWING, onClick = { editType = LoanType.BORROWING }, label = { Text("Borrowing") })
                    }
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Name*") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editPlace, onValueChange = { editPlace = it }, label = { Text("Place") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = editMobile, onValueChange = { editMobile = it.filter { c -> c.isDigit() || c == '+' || c == '-' || c == ' ' } },
                        label = { Text("Mobile") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        trailingIcon = {
                            IconButton(onClick = { contactPickerTarget = ContactPickerTarget.EDIT_DIALOG; contactPickerLauncher.launch(null) }) {
                                Icon(Icons.Default.Contacts, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                        })
                    OutlinedTextField(value = editAmount, onValueChange = { editAmount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(if (editType == LoanType.LENDING) "Amount Given" else "Amount Borrowed") },
                        singleLine = true, modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = editMode == PaymentMode.CASH, onClick = { editMode = PaymentMode.CASH }, label = { Text("Cash") })
                        FilterChip(selected = editMode == PaymentMode.UPI,  onClick = { editMode = PaymentMode.UPI  }, label = { Text("UPI")  })
                    }
                    OutlinedButton(onClick = { showEditDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(dateFormat.format(Date(editDate)))
                    }
                    HorizontalDivider()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SwapVert, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.width(4.dp))
                        Text("Move after (optional)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editMoveAfterName,
                            onValueChange = { editMoveAfterName = it; if (it.isNotBlank()) editMoveAfterSerial = "" },
                            label = { Text("Name") }, singleLine = true, modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = editMoveAfterSerial,
                            onValueChange = { editMoveAfterSerial = it.filter { c -> c.isDigit() }; if (it.isNotBlank()) editMoveAfterName = "" },
                            label = { Text("# No.") }, singleLine = true, modifier = Modifier.weight(0.5f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    if (editMoveAfterName.isNotBlank() || editMoveAfterSerial.isNotBlank()) {
                        val targetLabel = if (editMoveAfterSerial.isNotBlank()) {
                            val idx = (editMoveAfterSerial.toIntOrNull() ?: 0) - 1
                            persons.getOrNull(idx)?.name?.let { "after \"$it\" (#${editMoveAfterSerial})" } ?: "serial not found"
                        } else {
                            persons.firstOrNull { it.name.equals(editMoveAfterName.trim(), ignoreCase = true) }
                                ?.let { p -> "after \"${p.name}\" (#${persons.indexOf(p) + 1})" } ?: "name not found"
                        }
                        Text(
                            "Will place: $targetLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (targetLabel.contains("not found")) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amt = editAmount.toDoubleOrNull()
                    if (editName.isNotBlank() && amt != null) {
                        val updatedPerson = orig.copy(
                            name = editName.trim(),
                            place = editPlace.trim().ifEmpty { null },
                            mobileNumber = editMobile.trim().ifEmpty { null },
                            amountGiven = amt, mode = editMode,
                            dateGiven = editDate, recordType = editType
                        )
                        val moveAfter = resolveMoveAfterSortOrder()
                        if (moveAfter != null) {
                            coroutineScope.launch {
                                personViewModel.shiftSortOrdersAfterSync(fileId, moveAfter)
                                personViewModel.updatePerson(updatedPerson.copy(sortOrder = moveAfter + 1))
                            }
                        } else {
                            personViewModel.updatePerson(updatedPerson)
                        }
                        personToEdit = null
                    }
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { personToEdit = null }) { Text("Cancel") } }
        )

        if (showEditDatePicker) {
            val state = rememberDatePickerState(initialSelectedDateMillis = editDate)
            DatePickerDialog(
                onDismissRequest = { showEditDatePicker = false },
                confirmButton = { TextButton(onClick = { editDate = state.selectedDateMillis ?: editDate; showEditDatePicker = false }) { Text("OK") } },
                dismissButton = { TextButton(onClick = { showEditDatePicker = false }) { Text("Cancel") } }
            ) { DatePicker(state = state) }
        }
    }

    // Upload confirm
    if (showUploadConfirm) {
        val activeCount   = persons.count { !it.isDeleted }
        val uploadedCount = persons.count { it.uploadedAt != null && !it.isDeleted }
        val isNlrFile     = listOf("NLR 1","NLR 2","NLR 3","NLR 4").any { file?.name.equals(it, ignoreCase = true) }
        AlertDialog(
            onDismissRequest = { showUploadConfirm = false },
            title = { Text("Upload to Firebase") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("$activeCount", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Records", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (uploadedCount > 0) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text("$uploadedCount", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)
                                Text("Previously sent", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    val completedCount = completedPersons.size
                    val pendingCount   = pendingNewLoanPersons.size
                    if (completedCount > 0 || pendingCount > 0) {
                        HorizontalDivider()
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                            Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(14.dp))
                                    Text("Loan Completion Notes", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                }
                                if (completedCount > 0) {
                                    Text("$completedCount person(s) completed paying their loan:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                    completedPersons.forEach { cp ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                        ) {
                                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                            Text(
                                                "${cp.name}${if (!cp.place.isNullOrEmpty()) " (${cp.place})" else ""} — ₹${cp.amountGiven} fully repaid",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
                                }
                                if (pendingCount > 0) {
                                    if (completedCount > 0) Spacer(Modifier.height(4.dp))
                                    Text("$pendingCount person(s) completed and awaiting a new loan entry:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                                    pendingNewLoanPersons.forEach { pp ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(start = 8.dp, top = 2.dp)
                                        ) {
                                            Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(12.dp))
                                            Text(
                                                "${pp.name}${if (!pp.place.isNullOrEmpty()) " (${pp.place})" else ""}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onTertiaryContainer
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider()
                    Text("What this does:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                    listOf(
                        Triple(Icons.Default.CloudUpload, "Sends all $activeCount active names, amounts, modes, and dates in \"${file?.name}\" to Firebase Firestore (cloud).", MaterialTheme.colorScheme.onSurface),
                        Triple(Icons.Default.Receipt, "Also sends every payment recorded against each person — amounts returned, UPI / Cash, dates.", MaterialTheme.colorScheme.onSurface),
                        Triple(Icons.Default.AccountBalance, "Calculates each person's balance (given − received) and stores it on the cloud record.", MaterialTheme.colorScheme.onSurface),
                        Triple(Icons.Default.Warning, "Overwrites whatever is already in Firebase for this file — previous cloud data is replaced.", MaterialTheme.colorScheme.error),
                    ).forEach { (icon, text, color) ->
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Icon(icon, null, modifier = Modifier.size(14.dp).padding(top = 2.dp), tint = color)
                            Text(text, style = MaterialTheme.typography.bodySmall, color = color)
                        }
                    }
                    if (isNlrFile) {
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Default.Sync, null, modifier = Modifier.size(14.dp).padding(top = 2.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Because this is an NLR file, the predefined names template for \"${file?.name}\" will be updated to match the current list.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Smartphone, null, modifier = Modifier.size(14.dp).padding(top = 2.dp), tint = MaterialTheme.colorScheme.onSurface)
                        Text("Nothing is deleted from your phone — this is a backup/share action only.", style = MaterialTheme.typography.bodySmall)
                    }

                    HorizontalDivider()
                    Text(
                        "Use Verify after uploading to confirm Firebase received all records.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { file?.let { uploadViewModel.uploadFile(it) }; showUploadConfirm = false }) {
                    Icon(Icons.Default.CloudUpload, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Upload Now")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { showUploadConfirm = false }) { Text("Cancel") }
                    TextButton(onClick = { file?.let { uploadViewModel.verifyUpload(it) }; showUploadConfirm = false }) {
                        Text("Verify ↗", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        )
    }

    // Delete / multi-delete
    personToDelete?.let { p ->
        AlertDialog(onDismissRequest = { personToDelete = null },
            title = { Text("Delete ${p.name}?") }, text = { Text("Moved to trash.") },
            confirmButton = { TextButton(onClick = { personViewModel.softDeletePerson(p.id); personToDelete = null }) { Text("Delete", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { personToDelete = null }) { Text("Cancel") } })
    }
    if (showMultiDeleteDialog) {
        AlertDialog(onDismissRequest = { showMultiDeleteDialog = false },
            title = { Text("Delete ${selectedIds.size} persons?") }, text = { Text("Moved to trash.") },
            confirmButton = { TextButton(onClick = { selectedIds.forEach { personViewModel.softDeletePerson(it) }; selectedIds = emptySet(); showMultiDeleteDialog = false }) { Text("Delete All", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showMultiDeleteDialog = false }) { Text("Cancel") } })
    }
    if (showPlaceDialog) {
        AlertDialog(onDismissRequest = { showPlaceDialog = false },
            title = { Text("Duplicate Name") }, text = { Text("\"${pendingPerson?.name}\" already exists. Add anyway?") },
            confirmButton = { TextButton(onClick = { pendingPerson?.let { doInsertPerson(it, resolveAfterSortOrder()) }; showPlaceDialog = false; clearAddFields(); showAddDialog = false }) { Text("Add Anyway") } },
            dismissButton = { TextButton(onClick = { showPlaceDialog = false }) { Text("Back") } })
    }
    if (showMobileDialog) {
        AlertDialog(onDismissRequest = { showMobileDialog = false },
            title = { Text("Same Name & Place") }, text = { Text("Same name and place already exists. Add anyway?") },
            confirmButton = { TextButton(onClick = { pendingPerson?.let { doInsertPerson(it, resolveAfterSortOrder()) }; showMobileDialog = false; clearAddFields(); showAddDialog = false }) { Text("Add Anyway") } },
            dismissButton = { TextButton(onClick = { showMobileDialog = false }) { Text("Back") } })
    }
    if (showMultiTrashDeleteDialog) {
        AlertDialog(onDismissRequest = { showMultiTrashDeleteDialog = false },
            title = { Text("Permanently delete ${selectedTrashIds.size}?") }, text = { Text("Cannot be undone.") },
            confirmButton = { TextButton(onClick = { selectedTrashIds.forEach { personViewModel.hardDeletePerson(it) }; selectedTrashIds = emptySet(); showMultiTrashDeleteDialog = false }) { Text("Delete Forever", color = MaterialTheme.colorScheme.error) } },
            dismissButton = { TextButton(onClick = { showMultiTrashDeleteDialog = false }) { Text("Cancel") } })
    }
    if (showMultiRestoreDialog) {
        AlertDialog(onDismissRequest = { showMultiRestoreDialog = false },
            title = { Text("Restore ${selectedTrashIds.size} persons?") }, text = { Text("Will be restored to the file.") },
            confirmButton = { TextButton(onClick = { selectedTrashIds.forEach { personViewModel.restorePerson(it) }; selectedTrashIds = emptySet(); showMultiRestoreDialog = false }) { Text("Restore All") } },
            dismissButton = { TextButton(onClick = { showMultiRestoreDialog = false }) { Text("Cancel") } })
    }

    // ── View by Date — date picker ────────────────────────────────────────────
    if (showViewDatePicker2) {
        val dpState = rememberDatePickerState(initialSelectedDateMillis = viewDate)
        DatePickerDialog(
            onDismissRequest = { showViewDatePicker2 = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { viewDate = it }
                    showViewDatePicker2 = false
                    showViewSheet = true
                }) { Text("View") }
            },
            dismissButton = { TextButton(onClick = { showViewDatePicker2 = false }) { Text("Cancel") } }
        ) { DatePicker(state = dpState) }
    }

    // ── View by Date — result sheet ───────────────────────────────────────────
    if (showViewSheet) {
        val viewDateFmt = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
        val dayStart = remember(viewDate) {
            Calendar.getInstance().also {
                it.timeInMillis = viewDate
                it.set(Calendar.HOUR_OF_DAY, 0); it.set(Calendar.MINUTE, 0)
                it.set(Calendar.SECOND, 0); it.set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
        val dayEnd = dayStart + 86_399_999L

        // ── FIX: Build a map from completed-person ID → active/placeholder person ID
        // so we can attribute completed-person payments to their active counterpart row.
        //
        // A completed person has a `linkedNewPersonId` pointing to the pending-new-loan
        // placeholder. We collect those pairs so that when we sum received amounts for an
        // active row we also include payments recorded under the old completed ID.
        //
        // We deliberately do NOT add completed persons as separate rows.
        val completedIdToActiveId: Map<String, String> = remember(completedPersons, pendingNewLoanPersons) {
            // pending-new-loan placeholder has previousPersonId = completed person's id
            val result = mutableMapOf<String, String>()
            pendingNewLoanPersons.forEach { placeholder ->
                val prevId = placeholder.previousPersonId
                if (prevId != null) result[prevId] = placeholder.id
            }
            // Also map via completedPerson.linkedNewPersonId for the reverse direction
            completedPersons.forEach { comp ->
                val linkedId = comp.linkedNewPersonId
                if (linkedId != null && !result.containsKey(comp.id)) result[comp.id] = linkedId
            }
            result
        }

        // Payments on selected date keyed by personId
        val viewPaymentsOnDate = remember(filePaymentsAll, dayStart, dayEnd) {
            filePaymentsAll.filter { it.date in dayStart..dayEnd }
        }

        // For each person ID we might render, how much was received on this date?
        // This merges completed-person payments into their linked active/placeholder row.
        val receivedOnDateById: Map<String, Double> = remember(viewPaymentsOnDate, completedIdToActiveId) {
            val map = mutableMapOf<String, Double>()
            viewPaymentsOnDate.forEach { payment ->
                // If this payment belongs to a completed person, attribute it to the active row
                val rowId = completedIdToActiveId[payment.personId] ?: payment.personId
                map[rowId] = (map[rowId] ?: 0.0) + payment.amount
            }
            map
        }

        // Build a lookup: completedPerson.id → Person, so pending-clone rows can show
        // the original completed person's amountGiven in the Given column.
        // Fix 3 & 4: pending-new-loan clones have amountGiven=0.0 — we must resolve the
        // real loan amount from the completed parent via previousPersonId.
        val completedPersonById: Map<String, Person> = remember(completedPersons) {
            completedPersons.associateBy { it.id }
        }

        // Build the single deduplicated list of persons to show.
        // Fix 4: Deduplicate — if an active (non-pending) entry already exists for a name,
        // exclude the pending-new-loan clone for that name from the row list to prevent
        // duplicate rows. The completed person's payments are already merged via
        // completedIdToActiveId so no received amounts are lost.
        val viewPersons: List<Person> = remember(
            viewPersonFilter, persons, pendingNewLoanPersons, completedIdToActiveId
        ) {
            if (viewPersonFilter != null) {
                // Single-person filter: show the exact person that was tapped.
                // If it's a completed person, find its linked placeholder instead so we
                // don't show a completed row and also don't lose the payments.
                val targetId = completedIdToActiveId[viewPersonFilter!!.id] ?: viewPersonFilter!!.id
                val pool = persons + pendingNewLoanPersons
                pool.filter { it.id == targetId }
            } else {
                // All-persons view: active list + pending-new-loan placeholders only.
                // Completed persons are excluded as rows; their payments are merged above.
                // Fix 4: exclude pending-new-loan clones whose name already has an active
                // non-pending entry in the same file, preventing duplicate rows.
                val activePersons = persons.filter { !it.isDeleted }
                val activeNames = activePersons.map { it.name.lowercase() }.toSet()
                val filteredPending = pendingNewLoanPersons.filter { pending ->
                    pending.name.lowercase() !in activeNames
                }
                (activePersons + filteredPending).sortedBy { it.sortOrder }
            }
        }

        // Fix 3 & 4: For each person in the view, resolve the correct amountGiven.
        // Pending-new-loan clones have amountGiven=0 — look up their previousPersonId
        // to find the completed parent and use the parent's amountGiven instead.
        val resolvedAmountGiven: Map<String, Double> = remember(viewPersons, completedPersonById) {
            viewPersons.associate { p ->
                val amount = if (p.isPendingNewLoan && p.previousPersonId != null) {
                    completedPersonById[p.previousPersonId]?.amountGiven ?: p.amountGiven
                } else {
                    p.amountGiven
                }
                p.id to amount
            }
        }

        val totalGivenOnDate = remember(viewPersons, resolvedAmountGiven, dayStart, dayEnd) {
            viewPersons.filter { p ->
                val gDay = Calendar.getInstance().also {
                    // For pending clones, use the completed parent's dateGiven for the date check
                    val dateToCheck = if (p.isPendingNewLoan && p.previousPersonId != null) {
                        completedPersonById[p.previousPersonId]?.dateGiven ?: p.dateGiven
                    } else p.dateGiven
                    it.timeInMillis = dateToCheck
                    it.set(Calendar.HOUR_OF_DAY, 0); it.set(Calendar.MINUTE, 0)
                    it.set(Calendar.SECOND, 0); it.set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                gDay == dayStart
            }.sumOf { resolvedAmountGiven[it.id] ?: 0.0 }
        }

        val totalReceivedOnDate = remember(receivedOnDateById) {
            receivedOnDateById.values.sum()
        }

        ModalBottomSheet(
            onDismissRequest = { showViewSheet = false; viewPersonFilter = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (viewPersonFilter != null) "View: ${viewPersonFilter!!.name}" else "View: ${file?.name ?: "File"}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            viewDateFmt.format(Date(viewDate)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showViewSheet = false; viewPersonFilter = null }) {
                        Icon(Icons.Default.Close, null)
                    }
                }
                HorizontalDivider()
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Name", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Given", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(70.dp), color = MaterialTheme.colorScheme.primary)
                    Text("Received", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(70.dp), color = MaterialTheme.colorScheme.tertiary)
                    Spacer(Modifier.width(32.dp))
                }
                HorizontalDivider()
                LazyColumn(
                    Modifier.fillMaxWidth().fillMaxHeight(0.75f),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    itemsIndexed(viewPersons, key = { _, p -> p.id }) { idx, person ->
                        // Fix 3: Use resolvedAmountGiven so pending-new-loan clone rows
                        // show the original completed person's loan amount, not 0.0.
                        // For the date check on clones, use the completed parent's dateGiven.
                        val effectiveAmountGiven = resolvedAmountGiven[person.id] ?: person.amountGiven
                        val effectiveDateGiven = if (person.isPendingNewLoan && person.previousPersonId != null) {
                            completedPersonById[person.previousPersonId]?.dateGiven ?: person.dateGiven
                        } else person.dateGiven
                        val givenAmt = if (
                            Calendar.getInstance().also {
                                it.timeInMillis = effectiveDateGiven
                                it.set(Calendar.HOUR_OF_DAY, 0); it.set(Calendar.MINUTE, 0)
                                it.set(Calendar.SECOND, 0); it.set(Calendar.MILLISECOND, 0)
                            }.timeInMillis == dayStart
                        ) effectiveAmountGiven else 0.0

                        // Received for this row = own payments + any merged completed-person payments
                        val receivedAmt = receivedOnDateById[person.id] ?: 0.0
                        val hasActivity = givenAmt > 0 || receivedAmt > 0

                        Row(
                            Modifier
                                .fillMaxWidth()
                                .background(
                                    if (hasActivity) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
                                    else androidx.compose.ui.graphics.Color.Transparent
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${idx + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.width(24.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Column(Modifier.weight(1f)) {
                                Text(
                                    person.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                if (!person.place.isNullOrEmpty())
                                    Text(
                                        person.place,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                            }
                            Text(
                                if (givenAmt > 0) "₹${givenAmt}" else "Nil",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (givenAmt > 0) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (givenAmt > 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(70.dp)
                            )
                            Text(
                                if (receivedAmt > 0) "₹${receivedAmt}" else "Nil",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (receivedAmt > 0) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (receivedAmt > 0) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(70.dp)
                            )
                            // Add-payment button: shown for active persons and pending-new-loan
                            // placeholders, but not for completed persons (none are shown as rows)
                            if (!person.isPendingNewLoan || (receivedOnDateById[person.id] != null)) {
                                IconButton(
                                    onClick = {
                                        viewAddPaymentPerson = person
                                        viewAddPaymentAmount = ""
                                        viewAddPaymentMode = PaymentMode.CASH
                                        viewAddPaymentType = "RECEIVED"
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add, "Add Payment",
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            } else {
                                Spacer(Modifier.width(32.dp))
                            }
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    }
                    item {
                        Spacer(Modifier.height(4.dp))
                        HorizontalDivider(thickness = 1.dp)
                        Row(
                            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(Modifier.width(24.dp))
                            Text(
                                "TOTAL",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                if (totalGivenOnDate > 0) "₹${totalGivenOnDate}" else "Nil",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (totalGivenOnDate > 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(70.dp)
                            )
                            Text(
                                if (totalReceivedOnDate > 0) "₹${totalReceivedOnDate}" else "Nil",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (totalReceivedOnDate > 0) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(70.dp)
                            )
                            Spacer(Modifier.width(32.dp))
                        }
                    }
                }
            }
        }

        // Add payment dialog inside view sheet — unchanged
        viewAddPaymentPerson?.let { p ->
            val viewDateFmtInner = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
            AlertDialog(
                onDismissRequest = { viewAddPaymentPerson = null; viewAddPaymentAmount = "" },
                title = { Text("Add Entry for ${p.name}") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Date: ${viewDateFmtInner.format(Date(viewDate))}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = viewAddPaymentType == "GIVEN",
                                onClick = { viewAddPaymentType = "GIVEN" },
                                label = { Text("Given (I gave)") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = if (viewAddPaymentType == "GIVEN") {
                                    { Icon(Icons.Default.ArrowUpward, null, Modifier.size(14.dp)) }
                                } else null
                            )
                            FilterChip(
                                selected = viewAddPaymentType == "RECEIVED",
                                onClick = { viewAddPaymentType = "RECEIVED" },
                                label = { Text("Received") },
                                modifier = Modifier.weight(1f),
                                leadingIcon = if (viewAddPaymentType == "RECEIVED") {
                                    { Icon(Icons.Default.ArrowDownward, null, Modifier.size(14.dp)) }
                                } else null
                            )
                        }
                        HorizontalDivider()
                        if (viewAddPaymentType == "GIVEN") {
                            Text(
                                "This will ADD to ${p.name}'s given amount (current: ₹${p.amountGiven}).",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = viewAddPaymentAmount,
                                onValueChange = { viewAddPaymentAmount = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Amount Given*") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                leadingIcon = { Icon(Icons.Default.ArrowUpward, null, tint = MaterialTheme.colorScheme.primary) }
                            )
                        } else {
                            Text(
                                "This will record a payment received from ${p.name} and reflect in their full record.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = viewAddPaymentAmount,
                                onValueChange = { viewAddPaymentAmount = it.filter { c -> c.isDigit() || c == '.' } },
                                label = { Text("Amount Received*") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                leadingIcon = { Icon(Icons.Default.ArrowDownward, null, tint = MaterialTheme.colorScheme.tertiary) }
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = viewAddPaymentMode == PaymentMode.CASH,
                                    onClick = { viewAddPaymentMode = PaymentMode.CASH },
                                    label = { Text("Cash") }
                                )
                                FilterChip(
                                    selected = viewAddPaymentMode == PaymentMode.UPI,
                                    onClick = { viewAddPaymentMode = PaymentMode.UPI },
                                    label = { Text("UPI") }
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val amt = viewAddPaymentAmount.toDoubleOrNull()
                        if (amt != null && amt > 0) {
                            coroutineScope.launch {
                                if (viewAddPaymentType == "GIVEN") {
                                    personViewModel.updatePerson(p.copy(amountGiven = p.amountGiven + amt))
                                } else {
                                    paymentViewModel.insertPayment(
                                        Payment(
                                            personId = p.id,
                                            amount = amt,
                                            mode = viewAddPaymentMode,
                                            date = viewDate
                                        )
                                    )
                                    // Auto-complete: if balance reaches zero, mark complete
                                    val paid = (paidByPerson[p.id] ?: 0.0) + amt
                                    if (paid >= p.amountGiven && p.amountGiven > 0) {
                                        personViewModel.markAsCompleted(p)
                                    }
                                }
                            }
                            viewAddPaymentPerson = null
                            viewAddPaymentAmount = ""
                        }
                    }) {
                        Text(if (viewAddPaymentType == "GIVEN") "Add Given" else "Add Received")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewAddPaymentPerson = null; viewAddPaymentAmount = "" }) { Text("Cancel") }
                }
            )
        }
    }

    // ── Slide-to-Call bottom sheet ─────────────────────────────────────────────
    if (showSlideToCall && personToCall != null) {
        val callPerson = personToCall!!
        val callNumber = callPerson.mobileNumber ?: ""
        BackHandler { showSlideToCall = false }
        SlideToCallSheet(
            phoneNumber = callNumber,
            personName  = callPerson.name,
            onConfirm   = {
                showSlideToCall = false
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$callNumber")))
            },
            onDismiss   = { showSlideToCall = false }
        )
    }

    // ── No-Number dialog ───────────────────────────────────────────────────────
    if (showNoNumberDialog && personToCall != null) {
        val callPerson = personToCall!!
        BackHandler { showNoNumberDialog = false; noNumberMode = CallNoNumberMode.NONE; noNumberEnterText = "" }
        AlertDialog(
            onDismissRequest = { showNoNumberDialog = false; noNumberMode = CallNoNumberMode.NONE; noNumberEnterText = "" },
            icon = {
                Icon(Icons.Default.PhoneDisabled, null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.error)
            },
            title = { Text("No Number for ${callPerson.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (noNumberMode) {
                        CallNoNumberMode.NONE -> {
                            Text(
                                "No mobile number is saved for this person. What would you like to do?",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        CallNoNumberMode.ENTER_NUMBER -> {
                            Text(
                                "Enter a number to save and call:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = noNumberEnterText,
                                onValueChange = { noNumberEnterText = it.filter { c -> c.isDigit() || c == '+' || c == '-' || c == ' ' } },
                                label = { Text("Mobile Number") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                leadingIcon = { Icon(Icons.Default.Phone, null, modifier = Modifier.size(18.dp)) }
                            )
                        }
                        CallNoNumberMode.SELECT_CONTACT -> {
                            // Contact picker launched immediately; this state is transient
                            Text(
                                "Opening contact picker…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                when (noNumberMode) {
                    CallNoNumberMode.NONE -> {
                        // No confirm in NONE mode — actions are offered as separate buttons below
                    }
                    CallNoNumberMode.ENTER_NUMBER -> {
                        TextButton(
                            onClick = {
                                val number = noNumberEnterText.trim()
                                if (number.isNotBlank()) {
                                    personViewModel.updatePerson(callPerson.copy(mobileNumber = number))
                                    personToCall = callPerson.copy(mobileNumber = number)
                                    showNoNumberDialog = false
                                    noNumberMode = CallNoNumberMode.NONE
                                    noNumberEnterText = ""
                                    if (slideToCallEnabled) showSlideToCall = true
                                    else context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
                                }
                            },
                            enabled = noNumberEnterText.trim().isNotBlank()
                        ) { Text("Save & Call") }
                    }
                    CallNoNumberMode.SELECT_CONTACT -> {}
                }
            },
            dismissButton = {
                when (noNumberMode) {
                    CallNoNumberMode.NONE -> {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(
                                onClick = { noNumberMode = CallNoNumberMode.ENTER_NUMBER },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Enter Number")
                            }
                            TextButton(
                                onClick = {
                                    noNumberMode = CallNoNumberMode.SELECT_CONTACT
                                    contactPickerTarget = ContactPickerTarget.NO_NUMBER_DIALOG
                                    contactPickerLauncher.launch(null)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Contacts, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Select Contact")
                            }
                            TextButton(
                                onClick = { showNoNumberDialog = false; noNumberMode = CallNoNumberMode.NONE; noNumberEnterText = "" },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("Go Back") }
                        }
                    }
                    CallNoNumberMode.ENTER_NUMBER -> {
                        TextButton(
                            onClick = { noNumberMode = CallNoNumberMode.NONE; noNumberEnterText = "" }
                        ) { Text("Back") }
                    }
                    CallNoNumberMode.SELECT_CONTACT -> {
                        TextButton(
                            onClick = { showNoNumberDialog = false; noNumberMode = CallNoNumberMode.NONE; noNumberEnterText = "" }
                        ) { Text("Cancel") }
                    }
                }
            }
        )
    }
}

// ── Fix 2: DraggableCompletedPersonCard ──────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DraggableCompletedPersonCard(
    person: Person,
    daysLeft: Int,
    dateFormat: SimpleDateFormat,
    payments: List<Payment>,
    onDragStarted: () -> Unit,
    onDragMoved: (Offset) -> Unit,
    onDragEnded: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var cardPosition by remember { mutableStateOf(Offset.Zero) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coords ->
                cardPosition = coords.positionInWindow()
            }
            // detectDragGesturesAfterLongPress MUST come before clickable in the
            // modifier chain. Compose resolves pointer events in declaration order —
            // whichever pointerInput is listed first gets priority. The original bug
            // was combinedClickable appearing first: it consumed the long-press so
            // detectDragGesturesAfterLongPress never saw it and drag never started.
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress(
                    onDragStart = { offset ->
                        onDragStarted()
                        onDragMoved(Offset(cardPosition.x + offset.x, cardPosition.y + offset.y))
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        onDragMoved(Offset(change.position.x + cardPosition.x, change.position.y + cardPosition.y))
                    },
                    onDragEnd = { onDragEnded() },
                    onDragCancel = { onDragEnded() }
                )
            }
            // Plain clickable (no onLongClick) handles expand/collapse.
            // Long press is fully owned by the pointerInput above.
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(person.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (!person.place.isNullOrEmpty())
                        Text(person.place, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!person.mobileNumber.isNullOrEmpty())
                        Text(person.mobileNumber!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("₹${person.amountGiven} fully repaid",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium)
                        Text(if (daysLeft > 0) "$daysLeft days left" else "expires soon",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (daysLeft <= 5) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text(
                    "Hold & drag to delete",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(6.dp))
                if (payments.isEmpty()) {
                    Text("No payments recorded.", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Payments (${payments.size})", style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    payments.forEachIndexed { i, payment ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${i + 1}. ${dateFormat.format(java.util.Date(payment.date))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(payment.mode.name, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${payment.amount}", style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Paid", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text("₹${payments.sumOf { it.amount }}", style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompletedPersonCard(
    person: Person,
    daysLeft: Int,
    dateFormat: SimpleDateFormat,
    payments: List<Payment>,
    onHardDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = { expanded = !expanded }),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(person.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    if (!person.place.isNullOrEmpty())
                        Text(person.place, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (!person.mobileNumber.isNullOrEmpty())
                        Text(person.mobileNumber!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("₹${person.amountGiven} fully repaid",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium)
                        Text(if (daysLeft > 0) "$daysLeft days left" else "expires soon",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (daysLeft <= 5) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onHardDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                }
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(6.dp))
                if (payments.isEmpty()) {
                    Text("No payments recorded.", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    Text("Payments (${payments.size})", style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    payments.forEachIndexed { i, payment ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${i + 1}. ${dateFormat.format(java.util.Date(payment.date))}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(payment.mode.name, style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("₹${payment.amount}", style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.tertiary)
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(4.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Paid", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text("₹${payments.sumOf { it.amount }}", style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

// ── PendingNewLoanCard ───────────────────────────────────────────────────────
// Fix 1: Removed "+ Set Amount" button and red delete icon. The card now shows
// only name + "Pending New Loan" label. Tapping the card opens the amount dialog.
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PendingNewLoanCard(
    person: Person,
    dateFormat: SimpleDateFormat,
    onTap: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onTap),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Schedule, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(person.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                if (!person.place.isNullOrEmpty())
                    Text(person.place, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!person.mobileNumber.isNullOrEmpty())
                    Text(person.mobileNumber!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(2.dp))
                Text("Pending New Loan", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── TrashContent ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrashContent(
    deletedPersons: List<Person>, autoDeleteDays: Int,
    isSelectingTrash: Boolean, selectedTrashIds: Set<String>, padding: PaddingValues,
    onToggleSelect: (String) -> Unit, onLongSelect: (String) -> Unit,
    onRestore: (String) -> Unit, onHardDelete: (String) -> Unit
) {
    if (deletedPersons.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Delete, null, Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(16.dp))
                Text("No recently deleted persons", style = MaterialTheme.typography.titleMedium)
                Text("Deleted persons appear here for $autoDeleteDays days", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    } else {
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(deletedPersons, key = { _, p -> p.id }) { _, person ->
                val isSelected = person.id in selectedTrashIds
                val daysLeft = autoDeleteDays - ((System.currentTimeMillis() - (person.deletedAt ?: 0L)) / (1000 * 60 * 60 * 24)).toInt()
                Card(Modifier.fillMaxWidth().combinedClickable(onClick = { if (isSelectingTrash) onToggleSelect(person.id) }, onLongClick = { onLongSelect(person.id) }),
                    colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (isSelectingTrash) { Checkbox(checked = isSelected, onCheckedChange = { onToggleSelect(person.id) }); Spacer(Modifier.width(8.dp)) }
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(person.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                if (person.isCompleted) {
                                    AssistChip(onClick = {}, label = { Text("Completed", style = MaterialTheme.typography.labelSmall) },
                                        colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer))
                                }
                            }
                            Text("₹${person.amountGiven} • ${person.mode.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if (daysLeft > 0) "$daysLeft days left" else "Expires soon", style = MaterialTheme.typography.bodySmall,
                                color = if (daysLeft <= 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (!isSelectingTrash) {
                            IconButton(onClick = { onRestore(person.id) })    { Icon(Icons.Default.Restore,      null, tint = MaterialTheme.colorScheme.primary) }
                            IconButton(onClick = { onHardDelete(person.id) }) { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error)   }
                        }
                    }
                }
            }
        }
    }
}

// ── SummaryItem ───────────────────────────────────────────────────────────────
@Composable
fun SummaryItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(label,  style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── PersonCard ────────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PersonCard(
    person: Person,
    serialNumber: Int,
    totalPaid: Double,
    pending: Double,
    isSelected: Boolean,
    isSelecting: Boolean,
    elevation: androidx.compose.ui.unit.Dp,
    reorderState: ReorderableLazyListState,
    showWeeksColumns: Boolean,
    dateFormat: SimpleDateFormat,
    dayBreakdowns: List<DayBreakdown> = emptyList(),
    personPayments: List<Payment> = emptyList(),
    dateColPager: PagerState? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onMarkComplete: () -> Unit = {},
    onView: () -> Unit = {},
    onCallNow: () -> Unit = {}
) {
    val isBorrowing = person.recordType == LoanType.BORROWING
    val isFullyPaid  = pending <= 0 && totalPaid > 0

    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onClick = onClick, onLongClick = onLongClick),
        elevation = CardDefaults.cardElevation(elevation),
        colors = CardDefaults.cardColors(containerColor = when {
            isSelected  -> MaterialTheme.colorScheme.primaryContainer
            isBorrowing -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            else        -> MaterialTheme.colorScheme.surface
        })
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("$serialNumber.", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(28.dp))

            if (isSelecting) {
                Checkbox(checked = isSelected, onCheckedChange = { onClick() })
                Spacer(Modifier.width(4.dp))
            } else {
                Spacer(Modifier.width(4.dp))
            }

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(person.name, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.width(6.dp))
                    AssistChip(onClick = {}, label = {
                        Text(person.mode.name, style = MaterialTheme.typography.labelSmall)
                    })
                    if (isFullyPaid) {
                        Spacer(Modifier.width(4.dp))
                        AssistChip(onClick = {}, label = { Text("✓ Paid", style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer))
                    }
                }
                if (!person.place.isNullOrEmpty())
                    Text(person.place, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!person.mobileNumber.isNullOrEmpty())
                    Text("📞 ${person.mobileNumber}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!showWeeksColumns || dateColPager == null) {
                    Text(dateFormat.format(Date(person.dateGiven)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("₹${person.amountGiven}", fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isBorrowing) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary)
                }
            }

            if (showWeeksColumns && dateColPager != null && dayBreakdowns.isNotEmpty()) {
                val colPage = dateColPager.currentPage
                Spacer(Modifier.width(8.dp))

                val totalPaidAllTime = personPayments.sumOf { it.amount }

                if (colPage < dayBreakdowns.size) {
                    val dayBreak = dayBreakdowns[colPage]
                    val weekStart = dayBreak.weekStart
                    val weekEnd   = dayBreak.weekEnd
                    val thisWeekReturn = personPayments
                        .filter { it.date in weekStart..weekEnd }
                        .sumOf { it.amount }

                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(110.dp)) {
                        AmountCell(label = "Given", value = "₹${person.amountGiven}", color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        AmountCell(
                            label = "This Week",
                            value = if (thisWeekReturn == 0.0) "-" else "₹$thisWeekReturn",
                            color = if (thisWeekReturn == 0.0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(Modifier.height(4.dp))
                        AmountCell(
                            label = "Total Paid",
                            value = if (totalPaidAllTime == 0.0) "₹0" else "₹$totalPaidAllTime",
                            color = if (totalPaidAllTime >= person.amountGiven) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            bold = true
                        )
                    }
                } else {
                    val pendingTotal = person.amountGiven - totalPaidAllTime
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(110.dp)) {
                        AmountCell(label = "Given", value = "₹${person.amountGiven}", color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))
                        AmountCell(
                            label = "Total Paid",
                            value = if (totalPaidAllTime == 0.0) "₹0" else "₹$totalPaidAllTime",
                            color = if (totalPaidAllTime == 0.0) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(Modifier.height(4.dp))
                        AmountCell(
                            label = "Pending",
                            value = if (pendingTotal <= 0.0) "✓ Clear" else "₹$pendingTotal",
                            color = if (pendingTotal <= 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            bold = true
                        )
                    }
                }
            }

            if (!isSelecting) {
                Spacer(Modifier.width(4.dp))
                var showPersonMenu by remember { mutableStateOf(false) }
                var editButtonPressed by remember { mutableStateOf(false) }
                val editButtonScale by animateFloatAsState(
                    targetValue = if (editButtonPressed) 1.6f else 1f,
                    animationSpec = tween(durationMillis = 300),
                    label = "editScale",
                    finishedListener = { scale ->
                        if (scale >= 1.55f) {
                            editButtonPressed = false
                            onEdit()
                        }
                    }
                )
                Box {
                    IconButton(onClick = { showPersonMenu = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(expanded = showPersonMenu, onDismissRequest = { showPersonMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Call Now") },
                            leadingIcon = { Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.primary) },
                            onClick = { showPersonMenu = false; onCallNow() }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.scale(editButtonScale))
                            },
                            onClick = { showPersonMenu = false; editButtonPressed = true }
                        )
                        DropdownMenuItem(
                            text = { Text("View by Date") },
                            leadingIcon = { Icon(Icons.Default.CalendarToday, null) },
                            onClick = { showPersonMenu = false; onView() }
                        )
                        if (isFullyPaid) {
                            DropdownMenuItem(
                                text = { Text("Mark Complete") },
                                leadingIcon = { Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = { showPersonMenu = false; onMarkComplete() }
                            )
                        }
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { showPersonMenu = false; onDelete() }
                        )
                    }
                }
            }
        }
    }
}

// ── AmountCell ────────────────────────────────────────────────────────────────
@Composable
fun AmountCell(label: String, value: String, color: androidx.compose.ui.graphics.Color, bold: Boolean = false) {
    Column(horizontalAlignment = Alignment.End) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.SemiBold,
            color = color)
    }
}