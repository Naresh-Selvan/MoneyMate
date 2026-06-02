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

    // ─── Existing: Init ───────────────────────────────────────────────────────

    fun initDefaultPinIfNeeded() {
        if (prefs.getString("admin_pin_hash", "").isNullOrEmpty()) {
            fun hash(pin: String): String {
                val digest = java.security.MessageDigest.getInstance("SHA-256")
                return digest.digest(pin.toByteArray()).joinToString("") { "%02x".format(it) }
            }
            prefs.edit {
                putString("admin_pin_hash", hash("1904"))
                putInt("pin_length", 4)
            }
        }
    }
}