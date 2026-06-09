package com.moneymate.app.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.moneymate.app.notifications.workers.AboutToCloseWorker
import com.moneymate.app.notifications.workers.BadLoanAlertWorker
import com.moneymate.app.notifications.workers.DailyCollectionReminderWorker
import com.moneymate.app.utils.AppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton responsible for scheduling, re-scheduling, and cancelling
 * all periodic WorkManager workers based on user preferences.
 *
 * Both [MainActivity] (app start) and [SettingsViewModel] (toggle changes)
 * inject this class so that scheduling logic lives in one place.
 */
@Singleton
class WorkerScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appPreferences: AppPreferences
) {

    // ─── Public API ─────────────────────────────────────────────────────────

    /**
     * Schedule all workers whose toggles are currently enabled.
     * Uses [ExistingPeriodicWorkPolicy.KEEP] so existing schedules are preserved.
     * Call this once on app start.
     */
    fun scheduleOnStart() {
        scheduleAll(ExistingPeriodicWorkPolicy.KEEP)
    }

    /**
     * Re-schedule all workers with [ExistingPeriodicWorkPolicy.REPLACE].
     * Call this whenever the user changes notification toggles or the reminder
     * time in [SettingsScreen].
     *
     * Workers whose toggles are **off** will be cancelled because we skip
     * enqueuing them here, and [REPLACE] cancels the existing work for the
     * skipped unique names.
     */
    fun rescheduleForSettingsChange() {
        scheduleAll(ExistingPeriodicWorkPolicy.REPLACE)
    }

    /** Schedule or cancel only the daily collection reminder. */
    fun scheduleDailyReminder() {
        scheduleSingle(
            uniqueName = "daily_collection_reminder",
            enabled = appPreferences.dailyReminderEnabled,
            workerClass = DailyCollectionReminderWorker::class.java
        )
    }

    /** Schedule or cancel only the about-to-close alert. */
    fun scheduleAboutToClose() {
        scheduleSingle(
            uniqueName = "about_to_close_worker",
            enabled = appPreferences.aboutToCloseAlertsEnabled,
            workerClass = AboutToCloseWorker::class.java
        )
    }

    /** Schedule or cancel only the bad loan alert. */
    fun scheduleBadLoanAlert() {
        scheduleSingle(
            uniqueName = "bad_loan_alert_worker",
            enabled = appPreferences.badLoanAlertsEnabled,
            workerClass = BadLoanAlertWorker::class.java
        )
    }

    // ─── Internal ───────────────────────────────────────────────────────────

    private fun scheduleSingle(
        uniqueName: String,
        enabled: Boolean,
        workerClass: Class<out androidx.work.ListenableWorker>
    ) {
        val workManager = WorkManager.getInstance(context)
        if (enabled) {
            val initialDelay = computeInitialDelay(appPreferences.dailyReminderTime)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()
            val request = androidx.work.PeriodicWorkRequest.Builder(workerClass, 24, TimeUnit.HOURS)
                .setInitialDelay(java.time.Duration.ofMillis(initialDelay))
                .setConstraints(constraints)
                .addTag(uniqueName)
                .build()
            workManager.enqueueUniquePeriodicWork(
                uniqueName,
                ExistingPeriodicWorkPolicy.REPLACE,
                request
            )
        } else {
            workManager.cancelUniqueWork(uniqueName)
        }
    }

    private fun scheduleAll(policy: ExistingPeriodicWorkPolicy) {
        val workManager = WorkManager.getInstance(context)
        val initialDelay = computeInitialDelay(appPreferences.dailyReminderTime)

        // All workers run offline — no network needed
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        // Daily Collection Reminder
        if (appPreferences.dailyReminderEnabled) {
            val request = PeriodicWorkRequestBuilder<DailyCollectionReminderWorker>(
                24, TimeUnit.HOURS
            )
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .addTag("daily_collection_reminder")
                .build()

            workManager.enqueueUniquePeriodicWork(
                "daily_collection_reminder",
                policy,
                request
            )
        } else if (policy == ExistingPeriodicWorkPolicy.REPLACE) {
            // Toggle is off — cancel any existing work (REPLACE with nothing cancels)
            workManager.cancelUniqueWork("daily_collection_reminder")
        }

        // About to Close Alerts
        if (appPreferences.aboutToCloseAlertsEnabled) {
            val request = PeriodicWorkRequestBuilder<AboutToCloseWorker>(
                24, TimeUnit.HOURS
            )
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .addTag("about_to_close_worker")
                .build()

            workManager.enqueueUniquePeriodicWork(
                "about_to_close_worker",
                policy,
                request
            )
        } else if (policy == ExistingPeriodicWorkPolicy.REPLACE) {
            workManager.cancelUniqueWork("about_to_close_worker")
        }

        // Bad Loan Alerts
        if (appPreferences.badLoanAlertsEnabled) {
            val request = PeriodicWorkRequestBuilder<BadLoanAlertWorker>(
                24, TimeUnit.HOURS
            )
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .addTag("bad_loan_alert_worker")
                .build()

            workManager.enqueueUniquePeriodicWork(
                "bad_loan_alert_worker",
                policy,
                request
            )
        } else if (policy == ExistingPeriodicWorkPolicy.REPLACE) {
            workManager.cancelUniqueWork("bad_loan_alert_worker")
        }
    }

    private fun computeInitialDelay(dailyReminderTime: String): Long {
        val parts = dailyReminderTime.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.toIntOrNull() ?: 0
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
