package com.moneymate.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.moneymate.app.auth.SessionManager
import com.moneymate.app.data.local.entity.Permission

/**
 * Permission guard composable — wraps UI elements that should only be shown
 * to users with a specific [permission].
 *
 * If the user has permission, [content] is rendered. Otherwise, if [fallback]
 * is provided it is rendered; if null, nothing is rendered.
 */
@Composable
fun PermissionGuard(
    permission: Permission,
    sessionManager: SessionManager,
    fallback: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState()
    // In solo mode or when logged in, check permissions
    if (!isLoggedIn || sessionManager.hasPermission(permission)) {
        content()
    } else {
        fallback?.invoke()
    }
}
