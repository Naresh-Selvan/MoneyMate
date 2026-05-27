package com.moneymate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.moneymate.app.utils.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferences
) : ViewModel() {

    private val _darkMode = MutableStateFlow(prefs.darkMode)
    val darkMode: StateFlow<Boolean> = _darkMode

    private val _autoDeleteDays = MutableStateFlow(prefs.autoDeleteDays)
    val autoDeleteDays: StateFlow<Int> = _autoDeleteDays

    private val _notificationsEnabled = MutableStateFlow(prefs.notificationsEnabled)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

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
}