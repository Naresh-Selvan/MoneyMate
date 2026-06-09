package com.moneymate.app.utils

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("moneymate_prefs", Context.MODE_PRIVATE)

    // ─── Existing: Settings ────────────────────────────────────────────────────

    var autoDeleteDays: Int
        get() = prefs.getInt("auto_delete_days", 30)
        set(value) = prefs.edit { putInt("auto_delete_days", value) }

    var darkMode: Boolean
        get() = prefs.getBoolean("dark_mode", false)
        set(value) = prefs.edit { putBoolean("dark_mode", value) }

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean("notifications_enabled", true)
        set(value) = prefs.edit { putBoolean("notifications_enabled", value) }

    var slideToCallEnabled: Boolean
        get() = prefs.getBoolean("slide_to_call_enabled", true)
        set(value) = prefs.edit { putBoolean("slide_to_call_enabled", value) }

    // ─── Existing: PIN / Auth ──────────────────────────────────────────────────

    var adminPinHash: String
        get() = prefs.getString("admin_pin_hash", "") ?: ""
        set(value) = prefs.edit { putString("admin_pin_hash", value) }

    var isFirstLaunch: Boolean
        get() = prefs.getBoolean("is_first_launch", true)
        set(value) = prefs.edit { putBoolean("is_first_launch", value) }

    var lastActiveTime: Long
        get() = prefs.getLong("last_active_time", 0L)
        set(value) = prefs.edit { putLong("last_active_time", value) }

    var currentRole: String
        get() = prefs.getString("current_role", "") ?: ""
        set(value) = prefs.edit { putString("current_role", value) }

    var pinLength: Int
        get() = prefs.getInt("pin_length", 4)
        set(value) = prefs.edit { putInt("pin_length", value) }

    var wrongAttempts: Int
        get() = prefs.getInt("wrong_attempts", 0)
        set(value) = prefs.edit { putInt("wrong_attempts", value) }

    var lockUntil: Long
        get() = prefs.getLong("lock_until", 0L)
        set(value) = prefs.edit { putLong("lock_until", value) }

    var isLoggedOut: Boolean
        get() = prefs.getBoolean("is_logged_out", false)
        set(value) = prefs.edit { putBoolean("is_logged_out", value) }

    var appWasClosedLoggedIn: Boolean
        get() = prefs.getBoolean("app_was_closed_logged_in", false)
        set(value) = prefs.edit { putBoolean("app_was_closed_logged_in", value) }

    var biometricEnabled: Boolean
        get() = prefs.getBoolean("biometric_enabled", false)
        set(value) = prefs.edit { putBoolean("biometric_enabled", value) }

    // ─── NEW: Google Sign-In / Firebase ───────────────────────────────────────

    /**
     * True once the user has completed Google Sign-In at least once.
     * On subsequent launches we skip the Google Sign-In screen entirely.
     */
    var isGoogleSignedIn: Boolean
        get() = prefs.getBoolean("google_signed_in", false)
        set(value) = prefs.edit { putBoolean("google_signed_in", value) }

    /**
     * Firebase UID stored after the first successful Google Sign-In.
     * Empty string means not yet signed in.
     */
    var firebaseUid: String
        get() = prefs.getString("firebase_uid", "") ?: ""
        set(value) = prefs.edit { putString("firebase_uid", value) }

    /**
     * Display name from the Google account (optional, UI use only).
     */
    var googleDisplayName: String
        get() = prefs.getString("google_display_name", "") ?: ""
        set(value) = prefs.edit { putString("google_display_name", value) }

    /**
     * Email from the Google account (optional, UI use only).
     */
    var googleEmail: String
        get() = prefs.getString("google_email", "") ?: ""
        set(value) = prefs.edit { putString("google_email", value) }

    /**
     * True once the one-time Firestore data migration has completed successfully.
     * This flag is checked before attempting migration — if true, migration is skipped.
     */
    var isMigrationDone: Boolean
        get() = prefs.getBoolean("migration_done", false)
        set(value) = prefs.edit { putBoolean("migration_done", value) }

    // ─── Phase 6: Notification Settings ───────────────────────────────────────

    /** Whether daily collection reminder is enabled */
    var dailyReminderEnabled: Boolean
        get() = prefs.getBoolean("daily_reminder_enabled", true)
        set(value) = prefs.edit { putBoolean("daily_reminder_enabled", value) }

    /** Daily reminder time stored as "HH:mm" (default 08:00) */
    var dailyReminderTime: String
        get() = prefs.getString("daily_reminder_time", "08:00") ?: "08:00"
        set(value) = prefs.edit { putString("daily_reminder_time", value) }

    /** Whether about-to-close alerts are enabled */
    var aboutToCloseAlertsEnabled: Boolean
        get() = prefs.getBoolean("about_to_close_alerts_enabled", true)
        set(value) = prefs.edit { putBoolean("about_to_close_alerts_enabled", value) }

    /** Whether bad loan alerts are enabled */
    var badLoanAlertsEnabled: Boolean
        get() = prefs.getBoolean("bad_loan_alerts_enabled", true)
        set(value) = prefs.edit { putBoolean("bad_loan_alerts_enabled", value) }

    /** Whether payment confirmation notifications are enabled */
    var paymentConfirmationEnabled: Boolean
        get() = prefs.getBoolean("payment_confirmation_enabled", true)
        set(value) = prefs.edit { putBoolean("payment_confirmation_enabled", value) }

    /** Whether notification permission has been requested at least once */
    var notificationPermissionRequested: Boolean
        get() = prefs.getBoolean("notification_permission_requested", false)
        set(value) = prefs.edit { putBoolean("notification_permission_requested", value) }

    /** Whether the app is currently in the foreground */
    var isAppInForeground: Boolean
        get() = prefs.getBoolean("app_in_foreground", false)
        set(value) = prefs.edit { putBoolean("app_in_foreground", value) }

    // ── Settings Enhancements: License / Activation ────────────────────────

    var deviceId: String
        get() = prefs.getString("device_id", "") ?: ""
        set(value) = prefs.edit { putString("device_id", value) }

    var activationStatus: String
        get() = prefs.getString("activation_status", "") ?: ""
        set(value) = prefs.edit { putString("activation_status", value) }

    var activationPlan: String
        get() = prefs.getString("activation_plan", "") ?: ""
        set(value) = prefs.edit { putString("activation_plan", value) }

    var activationExpiry: Long
        get() = prefs.getLong("activation_expiry", 0L)
        set(value) = prefs.edit { putLong("activation_expiry", value) }

    var activatedEmail: String
        get() = prefs.getString("activated_email", "") ?: ""
        set(value) = prefs.edit { putString("activated_email", value) }

    var lastUpdateCheckTime: Long
        get() = prefs.getLong("last_update_check_time", 0L)
        set(value) = prefs.edit { putLong("last_update_check_time", value) }

    var installTime: Long
        get() = prefs.getLong("install_time", 0L)
        set(value) = prefs.edit { putLong("install_time", value) }

    // ── Role & Permissions: Session ────────────────────────────────────

    var currentUserId: Long
        get() = prefs.getLong("current_user_id", 0L)
        set(value) = prefs.edit { putLong("current_user_id", value) }

    var sessionTimeoutMinutes: Int
        get() = prefs.getInt("session_timeout_minutes", 0)
        set(value) = prefs.edit { putInt("session_timeout_minutes", value) }

    /** Get the underlying SharedPreferences for worker dedup keys */
    fun getPrefs() = prefs

    /** Get the active user count from plan limits */
    fun getActiveUserCount(): Int = prefs.getInt("active_user_count", 0)
    fun setActiveUserCount(count: Int) = prefs.edit { putInt("active_user_count", count) }
}

/** Extension function to check if a loan reminder is scheduled for a person */
fun AppPreferences.isReminderSet(personId: String): Boolean =
    getPrefs().getBoolean("reminder_set_$personId", false)

fun AppPreferences.setReminderState(personId: String, isSet: Boolean) {
    getPrefs().edit { putBoolean("reminder_set_$personId", isSet) }
}
