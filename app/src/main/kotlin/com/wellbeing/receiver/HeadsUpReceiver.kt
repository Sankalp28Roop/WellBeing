package com.wellbeing.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.DetectedActivity
import com.wellbeing.service.HeadsUpService

class HeadsUpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            result?.transitionEvents?.forEach { event ->
                if (event.activityType == DetectedActivity.WALKING) {
                    val serviceIntent = Intent(context, HeadsUpService::class.java).apply {
                        putExtra("EXTRA_WALKING", event.transitionType == ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                    }
                    context.startService(serviceIntent)
                }
            }
        }
    }
}
