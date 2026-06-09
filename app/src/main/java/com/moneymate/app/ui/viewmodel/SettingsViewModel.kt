package com.moneymate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.moneymate.app.notifications.WorkerScheduler
import com.moneymate.app.utils.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferences,
    val workerScheduler: WorkerScheduler
) : ViewModel() {

    private val _darkMode = MutableStateFlow(prefs.darkMode)
    val darkMode: StateFlow<Boolean> = _darkMode

    private val _autoDeleteDays = MutableStateFlow(prefs.autoDeleteDays)
    val autoDeleteDays: StateFlow<Int> = _autoDeleteDays

    private val _notificationsEnabled = MutableStateFlow(prefs.notificationsEnabled)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    private val _isSlideToCallEnabled = MutableStateFlow(prefs.slideToCallEnabled)
    val isSlideToCallEnabled: StateFlow<Boolean> = _isSlideToCallEnabled

    /** Daily reminder toggle */
    private val _dailyReminderEnabled = MutableStateFlow(prefs.dailyReminderEnabled)
    val dailyReminderEnabled: StateFlow<Boolean> = _dailyReminderEnabled

    /** Daily reminder time (HH:mm) */
    private val _dailyReminderTime = MutableStateFlow(prefs.dailyReminderTime)
    val dailyReminderTime: StateFlow<String> = _dailyReminderTime

    /** About to close toggle */
    private val _aboutToCloseAlertsEnabled = MutableStateFlow(prefs.aboutToCloseAlertsEnabled)
    val aboutToCloseAlertsEnabled: StateFlow<Boolean> = _aboutToCloseAlertsEnabled

    /** Bad loan alerts toggle */
    private val _badLoanAlertsEnabled = MutableStateFlow(prefs.badLoanAlertsEnabled)
    val badLoanAlertsEnabled: StateFlow<Boolean> = _badLoanAlertsEnabled

    /** Payment confirmation toggle */
    private val _paymentConfirmationEnabled = MutableStateFlow(prefs.paymentConfirmationEnabled)
    val paymentConfirmationEnabled: StateFlow<Boolean> = _paymentConfirmationEnabled

    fun setDarkMode(enabled: Boolean) {
        prefs.darkMode = enabled
        _darkMode.value = enabled
    }

    fun setAutoDeleteDays(days: Int) {
        prefs.autoDeleteDays = days
        _autoDeleteDays.value = days
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.notificationsEnabled = enabled
        _notificationsEnabled.value = enabled
    }

    fun setSlideToCallEnabled(enabled: Boolean) {
        prefs.slideToCallEnabled = enabled
        _isSlideToCallEnabled.value = enabled
    }

    // ── Notification settings ─────────────────────────────────────────────────

    fun setDailyReminderEnabled(enabled: Boolean) {
        prefs.dailyReminderEnabled = enabled
        _dailyReminderEnabled.value = enabled
        workerScheduler.scheduleDailyReminder()
    }

    fun setDailyReminderTime(time: String) {
        prefs.dailyReminderTime = time
        _dailyReminderTime.value = time
        workerScheduler.scheduleDailyReminder()
    }

    fun setAboutToCloseAlertsEnabled(enabled: Boolean) {
        prefs.aboutToCloseAlertsEnabled = enabled
        _aboutToCloseAlertsEnabled.value = enabled
        workerScheduler.scheduleAboutToClose()
    }

    fun setBadLoanAlertsEnabled(enabled: Boolean) {
        prefs.badLoanAlertsEnabled = enabled
        _badLoanAlertsEnabled.value = enabled
        workerScheduler.scheduleBadLoanAlert()
    }

    fun setPaymentConfirmationEnabled(enabled: Boolean) {
        prefs.paymentConfirmationEnabled = enabled
        _paymentConfirmationEnabled.value = enabled
    }
}