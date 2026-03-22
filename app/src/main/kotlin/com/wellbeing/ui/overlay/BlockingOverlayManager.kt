package com.wellbeing.ui.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import com.wellbeing.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockingOverlayManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var currentView: View? = null

    fun showBlockingOverlay(packageName: String, reason: String) {
        if (currentView != null) return

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        // Inflate a simple layout (we'll need to create this XML or use ComposeView)
        // For simplicity in this step, I'll use a TextView.
        // In a real app, I'd use ComposeView.
        val view = TextView(context).apply {
            text = "Usage limit reached for $packageName\n$reason"
            setBackgroundColor(0xFF000000.toInt())
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
            textSize = 24f
            setOnClickListener { hideBlockingOverlay() }
        }

        windowManager.addView(view, params)
        currentView = view
    }

    fun hideBlockingOverlay() {
        currentView?.let {
            windowManager.removeView(it)
            currentView = null
        }
    }
}
