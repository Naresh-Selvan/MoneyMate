package com.moneymate.app.notifications.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.moneymate.app.data.local.dao.PaymentDao
import com.moneymate.app.notifications.NotificationHelper
import com.moneymate.app.utils.AppPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

@HiltWorker
class BadLoanAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val paymentDao: PaymentDao,
    private val notificationHelper: NotificationHelper,
    private val preferences: AppPreferences
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val today = Calendar.getInstance().let {
                "${it.get(Calendar.YEAR)}-${it.get(Calendar.MONTH)}-${it.get(Calendar.DAY_OF_MONTH)}"
            }
            val cutoff = System.currentTimeMillis()

            val badLoans = paymentDao.getBadLoansAllFiles(cutoff)
            for (loan in badLoans) {
                val dedupKey = "notified_bad_${loan.personId}_$today"
                if (!preferences.getPrefs().contains(dedupKey)) {
                    notificationHelper.showBadLoanAlert(
                        loan.personName,
                        loan.daysOverdue.toInt(),
                        loan.balance
                    )
                    preferences.getPrefs().edit().putBoolean(dedupKey, true).apply()
                }
            }
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
