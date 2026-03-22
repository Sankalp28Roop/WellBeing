package com.wellbeing

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.wellbeing.worker.UsageCollectorWorker
import com.wellbeing.worker.ReminderWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class WellbeingApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        scheduleWorkers()
    }

    private fun scheduleWorkers() {
        UsageCollectorWorker.schedule(this, immediate = true)
        ReminderWorker.schedule(this)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel1 = NotificationChannel(
                "wellbeing_channel",
                "Wellbeing Notices",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for app limits and reminders"
            }
            
            val channel2 = NotificationChannel(
                "wellbeing_alerts",
                "Wellbeing Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High priority alerts when you exceed your limits"
            }
            
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(listOf(channel1, channel2))
        }
    }
}
