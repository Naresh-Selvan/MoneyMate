package com.moneymate.app.notifications.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.moneymate.app.notifications.NotificationHelper
import com.moneymate.app.utils.AppPreferences
import com.moneymate.app.utils.setReminderState
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class LoanReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val notificationHelper: NotificationHelper,
    private val appPreferences: AppPreferences
) : CoroutineWorker(context, params) {

    companion object {
        const val TAG_PREFIX = "loan_reminder_"
        const val KEY_PERSON_ID = "personId"
        const val KEY_PERSON_NAME = "personName"
        const val KEY_AMOUNT = "amount"
        const val KEY_NOTES = "notes"

        fun createInputData(personId: String, personName: String, amount: Double, notes: String?) = workDataOf(
            KEY_PERSON_ID to personId,
            KEY_PERSON_NAME to personName,
            KEY_AMOUNT to amount,
            KEY_NOTES to (notes ?: "")
        )
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val personId = inputData.getString(KEY_PERSON_ID)
            val personName = inputData.getString(KEY_PERSON_NAME) ?: return@withContext Result.failure()
            val amount = inputData.getDouble(KEY_AMOUNT, 0.0)
            val notes = inputData.getString(KEY_NOTES)
            notificationHelper.showPerLoanReminder(personName, amount, if (notes.isNullOrBlank()) null else notes)
            // Clear the reminder-set flag so the bell icon resets
            if (personId != null) {
                appPreferences.setReminderState(personId, false)
            }
            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}
