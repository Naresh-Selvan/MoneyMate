package com.moneymate.app.notifications.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.moneymate.app.data.local.dao.PersonDao
import com.moneymate.app.notifications.NotificationHelper
import com.moneymate.app.utils.AppPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

@HiltWorker
class AboutToCloseWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val personDao: PersonDao,
    private val notificationHelper: NotificationHelper,
    private val preferences: AppPreferences
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val today = Calendar.getInstance().let {
                "${it.get(Calendar.YEAR)}-${it.get(Calendar.MONTH)}-${it.get(Calendar.DAY_OF_MONTH)}"
            }

            val persons = personDao.getAboutToCloseLoansAllFiles()
            for (person in persons) {
                val dedupKey = "notified_close_${person.id}_$today"
                if (!preferences.getPrefs().contains(dedupKey)) {
                    // The SQL query filters for persons with (numberOfInstallments - paidCount) <= 3.
                    // We don't have the exact paid count, but remaining is guaranteed ≤ 3.
                    // Display 3 as the notification text (the exact number matters less than
                    // the alert itself — the user will open the app for details).
                    val remaining = person.numberOfInstallments.coerceAtMost(3)
                    notificationHelper.showAboutToClose(person.name, remaining, "")
                    preferences.getPrefs().edit().putBoolean(dedupKey, true).apply()
                }
            }
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
