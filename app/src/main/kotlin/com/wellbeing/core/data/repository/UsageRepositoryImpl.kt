package com.wellbeing.core.data.repository

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import com.wellbeing.core.data.local.dao.WellbeingDao
import com.wellbeing.core.data.local.entities.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

interface UsageRepository {
    fun getUsageStatsForDate(date: Long): Flow<List<AppUsageStat>>
    fun getDeviceUsageStat(date: Long): Flow<DeviceUsageStat?>
    suspend fun collectAndPersistEvents(from: Long, to: Long)
    suspend fun getUsageEvents(startTime: Long, endTime: Long): List<UsageEventEntity>
    
    // App Limits
    fun getAllAppLimits(): Flow<List<AppLimit>>
    suspend fun setAppLimit(packageName: String, limitMs: Long)
    suspend fun deleteAppLimit(packageName: String)
    
    // Goals
    fun getScreenTimeGoal(): Flow<ScreenTimeGoal?>
    suspend fun setScreenTimeGoal(goalMs: Long, enabled: Boolean = true)
}

@Singleton
class UsageRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wellbeingDao: WellbeingDao
) : UsageRepository {

    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val prefs = context.getSharedPreferences("wellbeing_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_COLLECTED = "last_collected_timestamp"
    }

    private fun getLastCollectedTimestamp(): Long = prefs.getLong(KEY_LAST_COLLECTED, 0L)

    private fun setLastCollectedTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_COLLECTED, timestamp).apply()
    }

    private val systemPackages = setOf(
        "android",
        "com.android.systemui",
        "com.android.launcher",
        "com.android.launcher3",
        "com.google.android.apps.nexuslauncher",
        "com.samsung.android.lool",
        "com.samsung.android.app.launcher",
        "com.miui.home",
        "com.oppo.launcher",
        "com.huawei.android.launcher",
        "com.oneplus.launcher"
    )

    private fun isSystemPackage(packageName: String): Boolean {
        return systemPackages.any { 
            packageName.startsWith(it) || packageName.contains("launcher") || packageName.contains("systemui")
        }
    }

    override fun getUsageStatsForDate(date: Long): Flow<List<AppUsageStat>> {
        return wellbeingDao.getUsageStatsForDate(date)
    }

    override fun getDeviceUsageStat(date: Long): Flow<DeviceUsageStat?> {
        return wellbeingDao.getDeviceUsageStat(date)
    }

    override suspend fun getUsageEvents(startTime: Long, endTime: Long): List<UsageEventEntity> {
        return wellbeingDao.getUsageEvents(startTime, endTime)
    }

    override suspend fun collectAndPersistEvents(from: Long, to: Long) {
        collectUsageStats(from, to)
        collectEvents(from, to)
        setLastCollectedTimestamp(to)
    }

    private suspend fun collectUsageStats(from: Long, to: Long) {
        val calendar = Calendar.getInstance().apply { timeInMillis = from }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val midnight = calendar.timeInMillis

        wellbeingDao.deleteUsageStatsForDate(midnight)
        wellbeingDao.deleteDeviceUsageStatsForDate(midnight)

        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY, midnight, to
        )

        var totalScreenTime = 0L

        usageStats.forEach { stats ->
            val isOurApp = stats.packageName == context.packageName
            val isSystem = isSystemPackage(stats.packageName)

            if (stats.totalTimeInForeground > 0 && !isOurApp && !isSystem) {
                wellbeingDao.insertUsageStat(
                    AppUsageStat(
                        id = 0,
                        packageName = stats.packageName,
                        date = midnight,
                        totalForegroundMs = stats.totalTimeInForeground,
                        timesOpened = 0,
                        notificationCount = 0
                    )
                )
                totalScreenTime += stats.totalTimeInForeground
            }
        }

        wellbeingDao.insertDeviceUsageStat(
            DeviceUsageStat(
                date = midnight,
                totalScreenTimeMs = totalScreenTime,
                unlockCount = 0,
                totalNotifications = 0
            )
        )
    }

    private suspend fun collectEvents(from: Long, to: Long) {
        val events = mutableListOf<UsageEventEntity>()
        val usageEvents = usageStatsManager.queryEvents(from, to)
        val event = UsageEvents.Event()
        
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            val isOurApp = event.packageName == context.packageName
            val isSystem = isSystemPackage(event.packageName)
            
            if (!isOurApp && !isSystem) {
                events.add(
                    UsageEventEntity(
                        packageName = event.packageName,
                        eventType = event.eventType,
                        timestamp = event.timeStamp,
                        className = event.className
                    )
                )
            }
        }
        
        if (events.isNotEmpty()) {
            wellbeingDao.insertUsageEvents(events)
            updateDeviceEvents(events)
        }
    }

    private suspend fun updateDeviceEvents(events: List<UsageEventEntity>) {
        val calendar = Calendar.getInstance()
        val eventsByDay = events.groupBy { event ->
            calendar.timeInMillis = event.timestamp
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }

        eventsByDay.forEach { (midnight, dayEvents) ->
            val currentDeviceStat = wellbeingDao.getDeviceUsageStat(midnight).firstOrNull() 
                ?: DeviceUsageStat(date = midnight, totalScreenTimeMs = 0, unlockCount = 0, totalNotifications = 0)

            val unlockCount = dayEvents.count { it.eventType == UsageEvents.Event.KEYGUARD_HIDDEN }
            val notificationCount = dayEvents.count { it.eventType == 12 }
            
            wellbeingDao.insertDeviceUsageStat(currentDeviceStat.copy(
                unlockCount = currentDeviceStat.unlockCount + unlockCount,
                totalNotifications = currentDeviceStat.totalNotifications + notificationCount
            ))
        }
    }

    override fun getAllAppLimits(): Flow<List<AppLimit>> {
        return wellbeingDao.getAllAppLimits()
    }

    override suspend fun setAppLimit(packageName: String, limitMs: Long) {
        wellbeingDao.insertAppLimit(AppLimit(packageName, limitMs))
    }

    override suspend fun deleteAppLimit(packageName: String) {
        wellbeingDao.getAppLimit(packageName)?.let {
            wellbeingDao.deleteAppLimit(it)
        }
    }

    override fun getScreenTimeGoal(): Flow<ScreenTimeGoal?> {
        return wellbeingDao.getScreenTimeGoal()
    }

    override suspend fun setScreenTimeGoal(goalMs: Long, enabled: Boolean) {
        wellbeingDao.insertScreenTimeGoal(ScreenTimeGoal(goalMs = goalMs, isEnabled = enabled))
    }
}
