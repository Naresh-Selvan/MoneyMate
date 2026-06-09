package com.moneymate.app.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.moneymate.app.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PENDING_INTENT_FLAGS = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

        fun personIdToNotifId(personId: String, type: String): Int =
            abs(personId.hashCode() + type.hashCode())

        private fun abs(n: Int) = if (n == Int.MIN_VALUE) 0 else kotlin.math.abs(n)
    }

    private fun canNotify(): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) return false
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun deepLinkIntent(route: String): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("nav_route", route)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(context, route.hashCode(), intent, PENDING_INTENT_FLAGS)
    }

    fun showPaymentConfirmation(personName: String, amount: Double, remaining: Double) {
        if (!canNotify()) return
        val notifId = personIdToNotifId(personName, "payment")
        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_PAYMENT_CONFIRMATION)
            .setSmallIcon(android.R.drawable.ic_notification_clear_all)
            .setContentTitle("Payment Confirmed")
            .setContentText("₹%.2f received from %s".format(amount, personName))
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Payment of ₹%.2f received from %s.\nOutstanding balance: ₹%.2f".format(amount, personName, remaining)))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setContentIntent(deepLinkIntent("home"))
            .build()
        NotificationManagerCompat.from(context).notify(notifId, notification)
    }

    fun showAboutToClose(personName: String, remainingInstallments: Int, fileLabel: String) {
        if (!canNotify()) return
        val notifId = personIdToNotifId(personName, "close")
        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_ABOUT_TO_CLOSE)
            .setSmallIcon(android.R.drawable.ic_notification_clear_all)
            .setContentTitle("Loan Closing Soon")
            .setContentText("$personName — $remainingInstallments installments remaining")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "$personName in $fileLabel has only $remainingInstallments installments remaining."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(deepLinkIntent("home"))
            .build()
        NotificationManagerCompat.from(context).notify(notifId, notification)
    }

    fun showBadLoanAlert(personName: String, daysOverdue: Int, balance: Double) {
        if (!canNotify()) return
        val notifId = personIdToNotifId(personName, "bad")
        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_BAD_LOAN)
            .setSmallIcon(android.R.drawable.ic_notification_clear_all)
            .setContentTitle("Bad Loan Alert")
            .setContentText("$personName — $daysOverdue days overdue")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "$personName is $daysOverdue days overdue.\nOutstanding balance: ₹%.2f".format(balance)))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(deepLinkIntent("home"))
            .build()
        NotificationManagerCompat.from(context).notify(notifId, notification)
    }

    fun showDailyCollectionReminder(totalPending: Int) {
        if (!canNotify()) return
        val notifId = 1001
        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_COLLECTION_REMINDER)
            .setSmallIcon(android.R.drawable.ic_notification_clear_all)
            .setContentTitle("Collection Reminder")
            .setContentText("$totalPending persons have not paid today")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "There are $totalPending persons who have not made a payment today.\nTap to view your collection list."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(deepLinkIntent("home"))
            .build()
        NotificationManagerCompat.from(context).notify(notifId, notification)
    }

    fun showPerLoanReminder(personName: String, amount: Double, notes: String?) {
        if (!canNotify()) return
        val notifId = personIdToNotifId(personName, "reminder")
        val body = if (notes.isNullOrBlank())
            "Reminder for $personName — ₹%.2f".format(amount)
        else
            "Reminder for $personName — ₹%.2f\nNote: $notes".format(amount)
        val notification = NotificationCompat.Builder(context, NotificationChannelManager.CHANNEL_PER_LOAN_REMINDER)
            .setSmallIcon(android.R.drawable.ic_notification_clear_all)
            .setContentTitle("Loan Reminder")
            .setContentText("Reminder for $personName")
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(deepLinkIntent("home"))
            .build()
        NotificationManagerCompat.from(context).notify(notifId, notification)
    }
}
