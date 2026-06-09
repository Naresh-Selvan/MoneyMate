package com.moneymate.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.moneymate.app.data.export.ExportManager
import com.moneymate.app.notifications.NotificationChannelManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class MoneyMateApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var exportManager: ExportManager

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var channelManager: NotificationChannelManager

    override fun onCreate() {
        super.onCreate()
        // Create all notification channels (safe on pre-O)
        channelManager.createChannels()
        // Clean up exported files older than 7 days
        exportManager.cleanOldExports()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
