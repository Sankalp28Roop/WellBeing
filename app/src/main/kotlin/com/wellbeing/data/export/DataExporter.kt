package com.wellbeing.data.export

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.wellbeing.core.data.local.WellbeingDatabase
import com.wellbeing.core.data.local.dao.WellbeingDao
import com.wellbeing.core.data.local.entities.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: WellbeingDatabase
) {
    private val dao: WellbeingDao get() = database.wellbeingDao()
    
    suspend fun exportToJson(): File = withContext(Dispatchers.IO) {
        val json = JSONObject()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        
        val today = getTodayMidnight()
        val weekAgo = today - (7 * 24 * 60 * 60 * 1000L)
        
        json.put("exportDate", dateFormat.format(Date()))
        json.put("period", "Last 7 days")
        
        val usageStats = dao.getUsageStatsForDate(today).first()
        val deviceStats = dao.getDeviceUsageStat(today).first()
        
        val summary = JSONObject().apply {
            put("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(today)))
            put("totalScreenTimeMs", deviceStats?.totalScreenTimeMs ?: 0)
            put("totalScreenTimeMinutes", ((deviceStats?.totalScreenTimeMs ?: 0) / (1000 * 60)))
            put("unlockCount", deviceStats?.unlockCount ?: 0)
            put("totalNotifications", deviceStats?.totalNotifications ?: 0)
        }
        json.put("summary", summary)
        
        val appStatsArray = JSONArray()
        usageStats.sortedByDescending { it.totalForegroundMs }.forEach { stat ->
            val statJson = JSONObject().apply {
                put("packageName", stat.packageName)
                put("appName", stat.packageName.split(".").last())
                put("screenTimeMs", stat.totalForegroundMs)
                put("screenTimeMinutes", stat.totalForegroundMs / (1000 * 60))
                put("timesOpened", stat.timesOpened)
                put("notifications", stat.notificationCount)
            }
            appStatsArray.put(statJson)
        }
        json.put("appUsage", appStatsArray)
        
        val appLimits = dao.getAllAppLimits().first()
        val limitsArray = JSONArray()
        appLimits.forEach { limit ->
            val limitJson = JSONObject().apply {
                put("packageName", limit.packageName)
                put("appName", limit.packageName.split(".").last())
                put("dailyLimitMs", limit.dailyLimitMs)
                put("dailyLimitMinutes", limit.dailyLimitMs / (1000 * 60))
                put("isActive", limit.isActive)
            }
            limitsArray.put(limitJson)
        }
        json.put("appLimits", limitsArray)
        
        val fileName = "wellbeing_export_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.json"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        file.writeText(json.toString(2))
        file
    }
    
    suspend fun exportToCsv(): File = withContext(Dispatchers.IO) {
        val today = getTodayMidnight()
        val usageStats = dao.getUsageStatsForDate(today).first()
        
        val csv = StringBuilder()
        csv.appendLine("App,Screen Time (min),Times Opened,Notifications")
        
        usageStats.sortedByDescending { it.totalForegroundMs }.forEach { stat ->
            val screenTimeMin = stat.totalForegroundMs / (1000 * 60)
            val appName = stat.packageName.split(".").last()
            csv.appendLine("\"$appName\",$screenTimeMin,${stat.timesOpened},${stat.notificationCount}")
        }
        
        val fileName = "wellbeing_export_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        file.writeText(csv.toString())
        file
    }
    
    fun shareFile(file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = if (file.extension == "json") "application/json" else "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    
    private fun getTodayMidnight(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
