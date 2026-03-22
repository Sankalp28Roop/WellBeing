package com.wellbeing.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wellbeing.worker.UsageCollectorWorker
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            UsageCollectorWorker.schedule(context)
            // Also reschedule any active focus mode or bedtime alarms here
        }
    }
}
