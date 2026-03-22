package com.wellbeing.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.WindowManager
import android.view.View
import android.graphics.PixelFormat
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import com.wellbeing.WellbeingApp
import com.wellbeing.core.data.repository.BedtimeRepository
import com.wellbeing.core.data.repository.FocusModeRepository
import com.wellbeing.core.data.repository.UsageRepository
import com.wellbeing.core.data.repository.SettingsRepository
import com.wellbeing.ui.overlay.InterstitialActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class WellbeingAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var usageRepository: UsageRepository
    
    @Inject
    lateinit var focusModeRepository: FocusModeRepository
    
    @Inject
    lateinit var bedtimeRepository: BedtimeRepository
    
    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var grayscaleOverlay: View? = null
    
    private val app: WellbeingApp
        get() = application as WellbeingApp

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceScope.launch {
            try {
                bedtimeRepository.isBedtimeModeActive().collect { active ->
                    if (active) {
                        showGrayscaleOverlay()
                    } else {
                        removeGrayscaleOverlay()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            if (packageName == "com.wellbeing" || packageName == packageName && packageName.contains("wellbeing")) return

            checkAppRestricted(packageName)
        }
    }

    private fun checkAppRestricted(packageName: String) {
        if (!::settingsRepository.isInitialized) return
        
        serviceScope.launch {
            try {
                val isLockModeActive = settingsRepository.isLockModeActive().first()
                if (isLockModeActive) {
                    blockApp(packageName, "Device is locked")
                    return@launch
                }

                val pausedUntil = settingsRepository.getPausedUntilTimestamp().first()
                if (System.currentTimeMillis() < pausedUntil) return@launch

                val isFocusActive = focusModeRepository.isFocusModeActive().first()
                if (isFocusActive) {
                    val activeProfile = focusModeRepository.getActiveProfile().first()
                    val restrictedApps = activeProfile?.let { 
                        focusModeRepository.getAppsForProfile(it.id).first() 
                    } ?: emptyList()
                    
                    if (restrictedApps.contains(packageName)) {
                        blockApp(packageName, "Focus Mode is active")
                        return@launch
                    }
                }

                val limit = usageRepository.getAllAppLimits().first().find { it.packageName == packageName }
                if (limit != null && limit.isActive) {
                    val today = getTodayMidnight()
                    val stats = usageRepository.getUsageStatsForDate(today).first()
                    val appStat = stats.find { it.packageName == packageName }
                    
                    if (appStat != null && appStat.totalForegroundMs >= limit.dailyLimitMs) {
                        blockApp(packageName, "Daily limit reached")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun blockApp(packageName: String, reason: String) {
        val intent = Intent(this, InterstitialActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("EXTRA_PACKAGE_NAME", packageName)
            putExtra("EXTRA_REASON", reason)
        }
        startActivity(intent)
    }

    private fun getTodayMidnight(): Long {
        return java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun showGrayscaleOverlay() {
        if (grayscaleOverlay != null) return
        
        try {
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            grayscaleOverlay = View(this).apply {
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                setLayerType(View.LAYER_TYPE_HARDWARE, Paint().apply {
                    val matrix = ColorMatrix().apply { setSaturation(0f) }
                    colorFilter = ColorMatrixColorFilter(matrix)
                })
            }
            
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) 
                    WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY 
                else 
                    WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )
            
            windowManager.addView(grayscaleOverlay, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun removeGrayscaleOverlay() {
        grayscaleOverlay?.let {
            try {
                val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
                windowManager.removeView(it)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            grayscaleOverlay = null
        }
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
