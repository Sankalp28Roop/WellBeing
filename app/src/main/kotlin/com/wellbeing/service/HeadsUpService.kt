package com.wellbeing.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import com.wellbeing.R
import com.wellbeing.receiver.HeadsUpReceiver
import dagger.hilt.android.AndroidEntryPoint
import android.view.WindowManager
import android.view.Gravity
import android.graphics.PixelFormat
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.os.Build
import androidx.core.content.ContextCompat

@AndroidEntryPoint
class HeadsUpService : Service() {

    private val client by lazy { ActivityRecognition.getClient(this) }
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val isWalking = intent?.getBooleanExtra("EXTRA_WALKING", false) ?: false
        if (isWalking) {
            showOverlay()
        } else if (intent?.hasExtra("EXTRA_WALKING") == true) {
            removeOverlay()
        }

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)
        startTracking()
        return START_STICKY
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "wellbeing_channel")
            .setContentTitle("Heads Up Active")
            .setContentText("Monitoring for distracted walking")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startTracking() {
        val transitions = listOf(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build(),
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_EXIT)
                .build()
        )
        val request = ActivityTransitionRequest(transitions)
        val receiverIntent = Intent(this, HeadsUpReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, receiverIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        try {
            client.requestActivityTransitionUpdates(request, pendingIntent)
        } catch (e: SecurityException) {
            stopSelf()
        }
    }

    private fun showOverlay() {
        if (overlayView != null) return
        
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        overlayView = LayoutInflater.from(this).inflate(R.layout.layout_heads_up_nudge, null)
        
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
            y = 100 // Adjust as needed
        }
        
        try {
            windowManager?.addView(overlayView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeOverlay() {
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayView = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeOverlay()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
}
