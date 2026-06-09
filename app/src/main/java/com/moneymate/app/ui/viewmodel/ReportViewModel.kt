package com.moneymate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymate.app.data.local.dao.*
import com.moneymate.app.data.local.entity.Person
import com.moneymate.app.data.repository.ExpenseRepository
import com.moneymate.app.data.repository.InvestmentRepository
import com.moneymate.app.data.repository.PersonRepository
import com.moneymate.app.data.repository.PaymentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

sealed class ReportDataState<out T> {
    object Loading : ReportDataState<Nothing>()
    data class Success<T>(val data: T) : ReportDataState<T>()
    object Empty : ReportDataState<Nothing>()
    data class Error(val message: String) : ReportDataState<Nothing>()
}

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val paymentRepository: PaymentRepository,
    private val personRepository: PersonRepository,
    private val expenseRepository: ExpenseRepository,
    private val investmentRepository: InvestmentRepository
) : ViewModel() {

    // ── Shared filters ──
    private val _selectedFileId = MutableStateFlow<String?>(null)
    val selectedFileId: StateFlow<String?> = _selectedFileId

    private val _fromDate = MutableStateFlow(Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis)
    val fromDate: StateFlow<Long> = _fromDate

    private val _toDate = MutableStateFlow(Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }.timeInMillis)
    val toDate: StateFlow<Long> = _toDate

    fun setFileId(id: String?) { _selectedFileId.value = id }
    fun setFromDate(ms: Long) { _fromDate.value = ms }
    fun setToDate(ms: Long) { _toDate.value = ms }

    /** Helper: returns an empty flow with proper type inference */
    private fun <T> emptyReportFlow(): Flow<ReportDataState<T>> =
        flowOf(ReportDataState.Empty as ReportDataState<T>)

    // ── Report 1: Plan ──
    @OptIn(ExperimentalCoroutinesApi::class)
    val planReport: StateFlow<ReportDataState<List<PlanEntry>>> = combine(
        _selectedFileId, _fromDate, _toDate
    ) { fid, f, t -> Triple(fid, f, t) }
        .flatMapLatest { (fid, f, t) ->
            if (fid == null) emptyReportFlow()
            else paymentRepository.getPlanReport(fid, f, t).map { list ->
                if (list.isEmpty()) ReportDataState.Empty
                else ReportDataState.Success(list)
            }
        }.catch { e -> emit(ReportDataState.Error(e.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)

    // ── Report 2: Daily Summary ──
    private val _dailyReportDate = MutableStateFlow(System.currentTimeMillis())
    val dailyReportDate: StateFlow<Long> = _dailyReportDate
    fun setDailyReportDate(ms: Long) { _dailyReportDate.value = ms }

    @OptIn(ExperimentalCoroutinesApi::class)
    val dailySummary: StateFlow<ReportDataState<List<DailySummaryEntry>>> = combine(
        _selectedFileId, _dailyReportDate
    ) { fid, date -> Pair(fid, date) }
        .flatMapLatest { (fid, date) ->
            if (fid == null) emptyReportFlow()
            else {
                val start = Calendar.getInstance().apply { timeInMillis = date; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
                val end = start + 86400000
                paymentRepository.getDailySummary(fid, start, end).map { list ->
                    if (list.isEmpty()) ReportDataState.Empty else ReportDataState.Success(list)
                }
            }
        }.catch { e -> emit(ReportDataState.Error(e.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)

    // ── Report 3: Line Summary ──
    @OptIn(ExperimentalCoroutinesApi::class)
    val lineSummary: StateFlow<ReportDataState<List<LineSummaryEntry>>> = combine(
        _selectedFileId, _fromDate, _toDate
    ) { fid, f, t -> Triple(fid, f, t) }
        .flatMapLatest { (fid, f, t) ->
            if (fid == null) emptyReportFlow()
            else paymentRepository.getLineSummary(fid, f, t).map { list ->
                if (list.isEmpty()) ReportDataState.Empty else ReportDataState.Success(list)
            }
        }.catch { e -> emit(ReportDataState.Error(e.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)

    // ── Report 4: Online Collections ──
    @OptIn(ExperimentalCoroutinesApi::class)
    val onlineCollections: StateFlow<ReportDataState<List<OnlineCollectionEntry>>> = combine(
        _selectedFileId, _fromDate, _toDate
    ) { fid, f, t -> Triple(fid, f, t) }
        .flatMapLatest { (fid, f, t) ->
            if (fid == null) emptyReportFlow()
            else paymentRepository.getOnlineCollections(fid, f, t).map { list ->
                if (list.isEmpty()) ReportDataState.Empty else ReportDataState.Success(list)
            }
        }.catch { e -> emit(ReportDataState.Error(e.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)

    // ── Report 5: Site Dashboard ──
    val siteDashboard: StateFlow<ReportDataState<SiteDashboard>> =
        combine(_fromDate, _toDate) { f, t -> Pair(f, t) }
            .flatMapLatest { (f, t) ->
                flow<ReportDataState<SiteDashboard>> {
                    val activeLoans = personRepository.getSiteActiveLoanCount()
                    val outstanding = personRepository.getSiteTotalOutstanding()
                    val collected = paymentRepository.getSiteTotalCollected(f, t)
                    val newLoans = personRepository.getSiteTotalNewLoans(f, t)
                    val expenses = expenseRepository.getSiteTotalExpenses(f, t)
                    emit(ReportDataState.Success(SiteDashboard(activeLoans, outstanding, collected, newLoans, expenses)))
                }
            }.catch { e -> emit(ReportDataState.Error(e.message ?: "Error")) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)

    // ── Report 6: Expense Summary (grouped by category) ──
    @OptIn(ExperimentalCoroutinesApi::class)
    val expenseSummaryReport: StateFlow<ReportDataState<List<CategorySummary>>> = combine(
        _selectedFileId, _fromDate, _toDate
    ) { fid, f, t -> Triple(fid, f, t) }
        .flatMapLatest { (fid, f, t) ->
            if (fid == null) emptyReportFlow()
            else expenseRepository.getCategorySummary(fid, f, t).map { list ->
                if (list.isEmpty()) ReportDataState.Empty else ReportDataState.Success(list)
            }
        }.catch { e -> emit(ReportDataState.Error(e.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)

    // ── Report 7: Investment Summary (grouped by type) ──
    @OptIn(ExperimentalCoroutinesApi::class)
    val investmentSummaryReport: StateFlow<ReportDataState<List<InvestmentCategorySummary>>> = combine(
        _selectedFileId, _fromDate, _toDate
    ) { fid, f, t -> Triple(fid, f, t) }
        .flatMapLatest { (fid, f, t) ->
            if (fid == null) emptyReportFlow()
            else investmentRepository.getTypeSummary(fid, f, t).map { list ->
                if (list.isEmpty()) ReportDataState.Empty else ReportDataState.Success(list)
            }
        }.catch { e -> emit(ReportDataState.Error(e.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)

    // ── Report 9: Book Excess Loss ──
    @OptIn(ExperimentalCoroutinesApi::class)
    val bookExcessLoss: StateFlow<ReportDataState<List<ExcessEntry>>> = _selectedFileId
        .flatMapLatest { fid ->
            if (fid == null) emptyReportFlow()
            else paymentRepository.getBookExcessLoss(fid).map { list ->
                if (list.isEmpty()) ReportDataState.Empty else ReportDataState.Success(list)
            }
        }.catch { e -> emit(ReportDataState.Error(e.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)

    // ── Report 10: Loan Summary ──
    private val _loanSummarySearchByDate = MutableStateFlow(true)
    var loanSummarySearchByDate: Boolean
        get() = _loanSummarySearchByDate.value
        set(v) { _loanSummarySearchByDate.value = v }

    @OptIn(ExperimentalCoroutinesApi::class)
    val loanSummary: StateFlow<ReportDataState<List<LoanSummaryEntry>>> = _selectedFileId
        .flatMapLatest { fid ->
            if (fid == null) emptyReportFlow()
            else paymentRepository.getLoanSummary(fid).map { list ->
                if (list.isEmpty()) ReportDataState.Empty else ReportDataState.Success(list)
            }
        }.catch { e -> emit(ReportDataState.Error(e.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)

    // ── Report 11: About to Close ──
    @OptIn(ExperimentalCoroutinesApi::class)
    val aboutToCloseLoans: StateFlow<ReportDataState<List<Person>>> = _selectedFileId
        .flatMapLatest { fid ->
            if (fid == null) emptyReportFlow()
            else personRepository.getAboutToCloseLoans(fid).map { list ->
                if (list.isEmpty()) ReportDataState.Empty else ReportDataState.Success(list)
            }
        }.catch { e -> emit(ReportDataState.Error(e.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)

    // ── Report 12: Missing Customers ──
    @OptIn(ExperimentalCoroutinesApi::class)
    val missingCustomers: StateFlow<ReportDataState<List<Person>>> = combine(
        _selectedFileId, _fromDate, _toDate
    ) { fid, f, t -> Triple(fid, f, t) }
        .flatMapLatest { (fid, f, t) ->
            if (fid == null) emptyReportFlow()
            else personRepository.getMissingCustomers(fid, f, t).map { list ->
                if (list.isEmpty()) ReportDataState.Empty else ReportDataState.Success(list)
            }
        }.catch { e -> emit(ReportDataState.Error(e.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)

    // ── Report 14: Completed Loans ──
    @OptIn(ExperimentalCoroutinesApi::class)
    val completedLoansReport: StateFlow<ReportDataState<List<CompletedLoanEntry>>> = combine(
        _selectedFileId, _fromDate, _toDate
    ) { fid, f, t -> Triple(fid, f, t) }
        .flatMapLatest { (fid, f, t) ->
            if (fid == null) emptyReportFlow()
            else paymentRepository.getCompletedLoans(fid, f, t).map { list ->
                if (list.isEmpty()) ReportDataState.Empty else ReportDataState.Success(list)
            }
        }.catch { e -> emit(ReportDataState.Error(e.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)

    // ── Report 18: New Customers ──
    @OptIn(ExperimentalCoroutinesApi::class)
    val newCustomersReport: StateFlow<ReportDataState<List<Person>>> = combine(
        _selectedFileId, _fromDate, _toDate
    ) { fid, f, t -> Triple(fid, f, t) }
        .flatMapLatest { (fid, f, t) ->
            if (fid == null) emptyReportFlow()
            else personRepository.getNewCustomers(fid, f, t).map { list ->
                if (list.isEmpty()) ReportDataState.Empty else ReportDataState.Success(list)
            }
        }.catch { e -> emit(ReportDataState.Error(e.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)

    // ── Report 19: Loan Analysis ──
    @OptIn(ExperimentalCoroutinesApi::class)
    val loanAnalysis: StateFlow<ReportDataState<List<LoanAnalysisEntry>>> = combine(
        _selectedFileId, _fromDate, _toDate
    ) { fid, f, t -> Triple(fid, f, t) }
        .flatMapLatest { (fid, f, t) ->
            if (fid == null) emptyReportFlow()
            else paymentRepository.getLoanAnalysis(fid, f, t).map { list ->
                if (list.isEmpty()) ReportDataState.Empty else ReportDataState.Success(list)
            }
        }.catch { e -> emit(ReportDataState.Error(e.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)

    // ── Report 19: Loan Not Taken ──
    @OptIn(ExperimentalCoroutinesApi::class)
    val loanNotTaken: StateFlow<ReportDataState<List<Person>>> = combine(
        _selectedFileId, _fromDate, _toDate
    ) { fid, f, t -> Triple(fid, f, t) }
        .flatMapLatest { (fid, f, t) ->
            if (fid == null) emptyReportFlow()
            else personRepository.getLoanNotTaken(fid, f, t).map { list ->
                if (list.isEmpty()) ReportDataState.Empty else ReportDataState.Success(list)
            }
        }.catch { e -> emit(ReportDataState.Error(e.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)

    // ── Report 20: Ledger ──
    private val _ledgerPersonId = MutableStateFlow<String?>(null)
    fun loadLedger(personId: String) { _ledgerPersonId.value = personId }

    val ledger: StateFlow<ReportDataState<List<LedgerEntry>>> = _ledgerPersonId
        .flatMapLatest { pid ->
            if (pid == null) emptyReportFlow()
            else paymentRepository.getLedgerEntries(pid).map { list ->
                if (list.isEmpty()) ReportDataState.Empty else ReportDataState.Success(list)
            }
        }.catch { e -> emit(ReportDataState.Error(e.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)

    // Helper: non-performing loans threshold
    private val _nonPerformingWeeks = MutableStateFlow(4)
    var nonPerformingWeeks: Int
        get() = _nonPerformingWeeks.value
        set(v) { _nonPerformingWeeks.value = v }

    @OptIn(ExperimentalCoroutinesApi::class)
    val nonPerformingLoans: StateFlow<ReportDataState<List<Person>>> = combine(
        _selectedFileId, _nonPerformingWeeks
    ) { fid, weeks -> Pair(fid, weeks) }
        .flatMapLatest { (fid, weeks) ->
            if (fid == null) emptyReportFlow()
            else {
                val cutoff = System.currentTimeMillis() - (weeks * 7L * 86400000)
                personRepository.getNonPerformingLoans(fid, cutoff).map { list ->
                    if (list.isEmpty()) ReportDataState.Empty else ReportDataState.Success(list)
                }
            }
        }.catch { e -> emit(ReportDataState.Error(e.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)

    // Bad loans threshold
    private val _badLoanDays = MutableStateFlow(100)
    var badLoanDays: Int
        get() = _badLoanDays.value
        set(v) { _badLoanDays.value = v }

    @OptIn(ExperimentalCoroutinesApi::class)
    val badLoansReport: StateFlow<ReportDataState<List<Person>>> = combine(
        _selectedFileId, _badLoanDays
    ) { fid, days -> Pair(fid, days) }
        .flatMapLatest { (fid, days) ->
            if (fid == null) emptyReportFlow()
            else {
                val cutoff = System.currentTimeMillis() - (days * 86400000L)
                personRepository.getBadLoans(fid, cutoff).map { list ->
                    if (list.isEmpty()) ReportDataState.Empty else ReportDataState.Success(list)
                }
            }
        }.catch { e -> emit(ReportDataState.Error(e.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)

    // New bad loans by date
    @OptIn(ExperimentalCoroutinesApi::class)
    val newBadLoansReport: StateFlow<ReportDataState<List<Person>>> = combine(
        _selectedFileId, _fromDate, _toDate
    ) { fid, f, t -> Triple(fid, f, t) }
        .flatMapLatest { (fid, f, t) ->
            if (fid == null) emptyReportFlow()
            else personRepository.getNewBadLoansByDate(fid, f, t).map { list ->
                if (list.isEmpty()) ReportDataState.Empty else ReportDataState.Success(list)
            }
        }.catch { e -> emit(ReportDataState.Error(e.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)

    // Monthly interest pending
    private val _interestMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    private val _interestYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val interestMonth: StateFlow<Int> = _interestMonth
    val interestYear: StateFlow<Int> = _interestYear
    fun setInterestMonth(m: Int) { _interestMonth.value = m }
    fun setInterestYear(y: Int) { _interestYear.value = y }

    @OptIn(ExperimentalCoroutinesApi::class)
    val monthlyInterestPending: StateFlow<ReportDataState<List<Person>>> = combine(
        _selectedFileId, _interestMonth, _interestYear
    ) { fid, m, y -> Triple(fid, m, y) }
        .flatMapLatest { (fid, m, y) ->
            if (fid == null) emptyReportFlow()
            else {
                val cal = Calendar.getInstance()
                cal.set(y, m, 1, 0, 0, 0); cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                val end = cal.timeInMillis
                personRepository.getMonthlyInterestPending(fid, start, end).map { list ->
                    if (list.isEmpty()) ReportDataState.Empty else ReportDataState.Success(list)
                }
            }
        }.catch { e -> emit(ReportDataState.Error(e.message ?: "Error")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)

    // Combined investment/expense (Report 8)
    val combinedReport: StateFlow<ReportDataState<CombinedSummary>> = combine(
        expenseSummaryReport, investmentSummaryReport
    ) { exp, inv ->
        val expData = (exp as? ReportDataState.Success)?.data ?: emptyList()
        val invData = (inv as? ReportDataState.Success)?.data ?: emptyList()
        val totalExp = expData.sumOf { it.grandTotal }
        val totalInv = invData.sumOf { it.grandTotal }
        ReportDataState.Success(CombinedSummary(expData, invData, totalExp, totalInv, totalInv - totalExp))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportDataState.Loading)
}

data class CombinedSummary(
    val expenses: List<CategorySummary>,
    val investments: List<InvestmentCategorySummary>,
    val totalExpenses: Double,
    val totalInvestments: Double,
    val netPosition: Double
)

// ════════════════════════════════════════════════════════════════════════
// Phase 5 — toExportData() conversion functions
// ════════════════════════════════════════════════════════════════════════

fun planReportToExportData(
    entries: List<PlanEntry>,
    fileLabel: String,
    dateRange: String
): com.moneymate.app.data.export.ReportExportData.TableReport {
    val headers = listOf("Name", "Loan Amount", "Installment", "Paid", "Total Inst.", "Collected Today", "Balance", "Place")
    val rows = entries.map { e ->
        listOf(e.personName, formatExportCurrency(e.loanAmount), formatExportCurrency(e.installmentAmount),
            e.paidCount.toString(), e.totalInstallments.toString(),
            formatExportCurrency(e.collectedToday), formatExportCurrency(e.balance), e.place ?: "-")
    }
    val footer = listOf("TOTAL", "", "", "", "",
        formatExportCurrency(entries.sumOf { it.collectedToday }),
        formatExportCurrency(entries.sumOf { it.balance }), "")
    return com.moneymate.app.data.export.ReportExportData.TableReport(
        reportTitle = "Plan Report", fileLabel = fileLabel, dateRange = dateRange,
        headers = headers, rows = rows, footerRow = footer
    )
}

fun dailySummaryToExportData(
    entries: List<DailySummaryEntry>,
    fileLabel: String,
    dateRange: String
): com.moneymate.app.data.export.ReportExportData.TableReport {
    val headers = listOf("Name", "Install Amount", "Paid Amount", "Mode", "Place")
    val rows = entries.map { e ->
        listOf(e.personName, formatExportCurrency(e.installAmount), formatExportCurrency(e.paidAmount),
            e.paymentMode, e.place ?: "-")
    }
    val footer = listOf("TOTAL", "", formatExportCurrency(entries.sumOf { it.paidAmount }), "", "")
    return com.moneymate.app.data.export.ReportExportData.TableReport(
        reportTitle = "Daily Summary", fileLabel = fileLabel, dateRange = dateRange,
        headers = headers, rows = rows, footerRow = footer
    )
}

fun lineSummaryToExportData(
    entries: List<LineSummaryEntry>,
    fileLabel: String,
    dateRange: String
): com.moneymate.app.data.export.ReportExportData.TableReport {
    val headers = listOf("Date", "Collected", "Online", "Cash", "Expense", "Net")
    val rows = entries.map { e ->
        listOf(formatDate(e.date), formatExportCurrency(e.totalCollected),
            formatExportCurrency(e.totalOnline), formatExportCurrency(e.totalCash),
            formatExportCurrency(e.totalExpense), formatExportCurrency(e.netBalance))
    }
    val footer = listOf("TOTAL",
        formatExportCurrency(entries.sumOf { it.totalCollected }),
        formatExportCurrency(entries.sumOf { it.totalOnline }),
        formatExportCurrency(entries.sumOf { it.totalCash }),
        formatExportCurrency(entries.sumOf { it.totalExpense }),
        formatExportCurrency(entries.sumOf { it.netBalance }))
    return com.moneymate.app.data.export.ReportExportData.TableReport(
        reportTitle = "Line Summary", fileLabel = fileLabel, dateRange = dateRange,
        headers = headers, rows = rows, footerRow = footer
    )
}

fun onlineCollectionsToExportData(
    entries: List<OnlineCollectionEntry>,
    fileLabel: String,
    dateRange: String
): com.moneymate.app.data.export.ReportExportData.TableReport {
    val headers = listOf("Name", "Date", "Amount", "Mode")
    val rows = entries.map { e ->
        listOf(e.personName, formatDate(e.date), formatExportCurrency(e.amount), e.paymentMode)
    }
    val footer = listOf("TOTAL", "", formatExportCurrency(entries.sumOf { it.amount }), "")
    return com.moneymate.app.data.export.ReportExportData.TableReport(
        reportTitle = "Online Collections", fileLabel = fileLabel, dateRange = dateRange,
        headers = headers, rows = rows, footerRow = footer
    )
}

fun siteDashboardToExportData(
    dash: SiteDashboard,
    firstFileLabel: String,
    dateRange: String
): com.moneymate.app.data.export.ReportExportData.SiteDashboardData {
    val metrics = listOf(
        DashboardMetric("Active Loans", dash.totalActiveLoans.toString()),
        DashboardMetric("Outstanding Balance", formatExportCurrency(dash.totalOutstanding)),
        DashboardMetric("Collected This Month", formatExportCurrency(dash.totalCollectedThisMonth)),
        DashboardMetric("New Loans This Month", formatExportCurrency(dash.totalNewLoansThisMonth)),
        DashboardMetric("Expenses This Month", formatExportCurrency(dash.totalExpensesThisMonth))
    )
    return com.moneymate.app.data.export.ReportExportData.SiteDashboardData(
        reportTitle = "Site Dashboard", fileLabel = firstFileLabel, dateRange = dateRange,
        metrics = metrics
    )
}

fun categorySummaryToExportData(
    entries: List<CategorySummary>,
    title: String,
    fileLabel: String,
    dateRange: String
): com.moneymate.app.data.export.ReportExportData.TableReport {
    val headers = listOf("Category", "Cash", "Online", "Total")
    val rows = entries.map { e ->
        listOf(e.category, formatExportCurrency(e.cashTotal), formatExportCurrency(e.onlineTotal),
            formatExportCurrency(e.grandTotal))
    }
    val footer = listOf("TOTAL",
        formatExportCurrency(entries.sumOf { it.cashTotal }),
        formatExportCurrency(entries.sumOf { it.onlineTotal }),
        formatExportCurrency(entries.sumOf { it.grandTotal }))
    return com.moneymate.app.data.export.ReportExportData.TableReport(
        reportTitle = title, fileLabel = fileLabel, dateRange = dateRange,
        headers = headers, rows = rows, footerRow = footer
    )
}

fun investmentTypeSummaryToExportData(
    entries: List<InvestmentCategorySummary>,
    title: String,
    fileLabel: String,
    dateRange: String
): com.moneymate.app.data.export.ReportExportData.TableReport {
    val headers = listOf("Type", "Cash", "Online", "Total")
    val rows = entries.map { e ->
        listOf(e.type, formatExportCurrency(e.cashTotal), formatExportCurrency(e.onlineTotal),
            formatExportCurrency(e.grandTotal))
    }
    val footer = listOf("TOTAL",
        formatExportCurrency(entries.sumOf { it.cashTotal }),
        formatExportCurrency(entries.sumOf { it.onlineTotal }),
        formatExportCurrency(entries.sumOf { it.grandTotal }))
    return com.moneymate.app.data.export.ReportExportData.TableReport(
        reportTitle = title, fileLabel = fileLabel, dateRange = dateRange,
        headers = headers, rows = rows, footerRow = footer
    )
}

fun combinedSummaryToExportData(
    combined: CombinedSummary,
    fileLabel: String,
    dateRange: String
): com.moneymate.app.data.export.ReportExportData.GroupedReport {
    val expenseGroup = ReportGroup(
        groupLabel = "Expenses",
        headers = listOf("Category", "Cash", "Online", "Total"),
        rows = combined.expenses.map { e -> listOf(e.category,
            formatExportCurrency(e.cashTotal), formatExportCurrency(e.onlineTotal),
            formatExportCurrency(e.grandTotal)) },
        subtotalRow = listOf("Subtotal", "", "", formatExportCurrency(combined.totalExpenses))
    )
    val investmentGroup = ReportGroup(
        groupLabel = "Investments",
        headers = listOf("Type", "Cash", "Online", "Total"),
        rows = combined.investments.map { i -> listOf(i.type,
            formatExportCurrency(i.cashTotal), formatExportCurrency(i.onlineTotal),
            formatExportCurrency(i.grandTotal)) },
        subtotalRow = listOf("Subtotal", "", "", formatExportCurrency(combined.totalInvestments))
    )
    return com.moneymate.app.data.export.ReportExportData.GroupedReport(
        reportTitle = "Combined Summary", fileLabel = fileLabel, dateRange = dateRange,
        groups = listOf(expenseGroup, investmentGroup)
    )
}

fun excessEntriesToExportData(
    entries: List<ExcessEntry>,
    fileLabel: String,
    dateRange: String
): com.moneymate.app.data.export.ReportExportData.TableReport {
    val headers = listOf("Name", "Loan Amount", "Total Paid", "Excess Amount")
    val rows = entries.map { e ->
        listOf(e.personName, formatExportCurrency(e.loanAmount),
            formatExportCurrency(e.totalPaid), formatExportCurrency(e.excessAmount))
    }
    val footer = listOf("TOTAL", "", "", formatExportCurrency(entries.sumOf { it.excessAmount }))
    return com.moneymate.app.data.export.ReportExportData.TableReport(
        reportTitle = "Book Excess Loss", fileLabel = fileLabel, dateRange = dateRange,
        headers = headers, rows = rows, footerRow = footer
    )
}

fun loanSummaryToExportData(
    entries: List<LoanSummaryEntry>,
    fileLabel: String,
    dateRange: String
): com.moneymate.app.data.export.ReportExportData.TableReport {
    val headers = listOf("Name", "Loan", "Interest", "Install", "Paid", "Total Inst.", "Balance", "Status")
    val rows = entries.map { e ->
        listOf(e.name, formatExportCurrency(e.loanAmount), "${e.interest}%",
            formatExportCurrency(e.installAmount), e.paidCount.toString(), e.totalInstallments.toString(),
            formatExportCurrency(e.balance), e.status)
    }
    return com.moneymate.app.data.export.ReportExportData.TableReport(
        reportTitle = "Loan Summary", fileLabel = fileLabel, dateRange = dateRange,
        headers = headers, rows = rows
    )
}

fun completedLoansToExportData(
    entries: List<CompletedLoanEntry>,
    fileLabel: String,
    dateRange: String
): com.moneymate.app.data.export.ReportExportData.TableReport {
    val headers = listOf("Name", "Loan Amount", "Total Collected", "Completion Date", "Duration (days)")
    val rows = entries.map { e ->
        listOf(e.name, formatExportCurrency(e.loanAmount), formatExportCurrency(e.totalCollected),
            formatDate(e.completionDate), e.durationDays.toString())
    }
    val footer = listOf("TOTAL", formatExportCurrency(entries.sumOf { it.loanAmount }),
        formatExportCurrency(entries.sumOf { it.totalCollected }), "", "")
    return com.moneymate.app.data.export.ReportExportData.TableReport(
        reportTitle = "Completed Loans", fileLabel = fileLabel, dateRange = dateRange,
        headers = headers, rows = rows, footerRow = footer
    )
}

fun loanAnalysisToExportData(
    entries: List<LoanAnalysisEntry>,
    fileLabel: String,
    dateRange: String
): com.moneymate.app.data.export.ReportExportData.TableReport {
    val headers = listOf("Date", "New Loans", "Active", "Completed", "Disbursed", "Collected")
    val rows = entries.map { e ->
        listOf(formatDate(e.date), e.newLoans.toString(), e.activeLoans.toString(),
            e.completedLoans.toString(), formatExportCurrency(e.totalDisbursed),
            formatExportCurrency(e.totalCollected))
    }
    val footer = listOf("TOTAL", entries.sumOf { it.newLoans }.toString(),
        entries.sumOf { it.activeLoans }.toString(), entries.sumOf { it.completedLoans }.toString(),
        formatExportCurrency(entries.sumOf { it.totalDisbursed }),
        formatExportCurrency(entries.sumOf { it.totalCollected }))
    return com.moneymate.app.data.export.ReportExportData.TableReport(
        reportTitle = "Loan Analysis", fileLabel = fileLabel, dateRange = dateRange,
        headers = headers, rows = rows, footerRow = footer
    )
}

fun ledgerToExportData(
    entries: List<LedgerEntry>,
    personName: String,
    dateRange: String
): com.moneymate.app.data.export.ReportExportData.TableReport {
    val headers = listOf("Date", "Description", "Debit", "Credit", "Running Balance")
    var running = 0.0
    val rows = entries.map { e ->
        running += e.amount
        val debit = if (e.type == "PAYMENT" && e.amount > 0) formatExportCurrency(e.amount) else "-"
        val credit = if (e.type == "PAYMENT" && e.amount < 0) formatExportCurrency(-e.amount) else if (e.type != "PAYMENT") formatExportCurrency(e.amount) else "-"
        listOf(formatDate(e.date), "${e.type} (${e.mode})", debit, credit, formatExportCurrency(running))
    }
    val opening = running - entries.sumOf { it.amount }
    val footer = listOf("", "Opening: ${formatExportCurrency(opening)}", "",
        "Total: ${formatExportCurrency(entries.sumOf { it.amount })}", "Closing: ${formatExportCurrency(running)}")
    return com.moneymate.app.data.export.ReportExportData.TableReport(
        reportTitle = "Ledger - $personName", fileLabel = personName, dateRange = dateRange,
        headers = headers, rows = rows, footerRow = footer
    )
}

// ── Helpers ──────────────────────────────────────────────────────────────────

private fun formatExportCurrency(amount: Double): String {
    return String.format("%.2f", amount)
}

private fun formatDate(ms: Long): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(ms))
}

// Type aliases for the export package types used in conversion functions
private typealias ReportGroup = com.moneymate.app.data.export.ReportGroup
private typealias DashboardMetric = com.moneymate.app.data.export.DashboardMetric
