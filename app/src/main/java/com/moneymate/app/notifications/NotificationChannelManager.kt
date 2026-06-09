package com.moneymate.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationChannelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_COLLECTION_REMINDER = "collection_reminder"
        const val CHANNEL_ABOUT_TO_CLOSE = "about_to_close"
        const val CHANNEL_BAD_LOAN = "bad_loan_alert"
        const val CHANNEL_PAYMENT_CONFIRMATION = "payment_confirmation"
        const val CHANNEL_PER_LOAN_REMINDER = "per_loan_reminder"
    }

    fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channels = listOf(
            NotificationChannel(
                CHANNEL_COLLECTION_REMINDER,
                "Collection Reminder",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily reminder to collect payments"
            },
            NotificationChannel(
                CHANNEL_ABOUT_TO_CLOSE,
                "Loan Closing Soon",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Loans with 3 or fewer installments remaining"
            },
            NotificationChannel(
                CHANNEL_BAD_LOAN,
                "Bad Loan Alert",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Loans overdue past their bad loan threshold"
            },
            NotificationChannel(
                CHANNEL_PAYMENT_CONFIRMATION,
                "Payment Confirmed",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Confirmation after recording a payment"
            },
            NotificationChannel(
                CHANNEL_PER_LOAN_REMINDER,
                "Loan Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Per-loan custom reminder"
            }
        )

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        channels.forEach { manager.createNotificationChannel(it) }
    }
}
