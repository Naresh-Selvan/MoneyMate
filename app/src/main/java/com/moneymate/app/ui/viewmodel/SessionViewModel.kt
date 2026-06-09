package com.moneymate.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneymate.app.auth.SessionManager
import com.moneymate.app.data.local.entity.Permission
import com.moneymate.app.data.local.entity.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Wraps [SessionManager] for convenient injection into Compose screens.
 * Exposes current user, role, login state, and permission checks.
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionManager: SessionManager
) : ViewModel() {

    val currentUser: StateFlow<com.moneymate.app.data.local.entity.AppUser?> =
        sessionManager.currentUser

    val currentRole: StateFlow<UserRole> =
        sessionManager.currentRole.stateIn(viewModelScope, SharingStarted.Eagerly, UserRole.ADMIN)

    val isLoggedIn: StateFlow<Boolean> =
        sessionManager.isLoggedIn.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val isSoloMode: StateFlow<Boolean> =
        sessionManager.isSoloMode.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    fun hasPermission(permission: Permission): Boolean =
        sessionManager.hasPermission(permission)

    fun canAccessFile(fileId: String): Boolean =
        sessionManager.canAccessFile(fileId)

    fun logout() {
        viewModelScope.launch {
            sessionManager.logout()
        }
    }
}
