package com.wellbeing.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_usage_stats")
data class AppUsageStat(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val date: Long,          // epoch ms, midnight
    val totalForegroundMs: Long,
    val timesOpened: Int,
    val notificationCount: Int
)

@Entity(tableName = "usage_events")
data class UsageEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val eventType: Int,
    val timestamp: Long,
    val className: String? = null
)

@Entity(tableName = "app_limits")
data class AppLimit(
    @PrimaryKey val packageName: String,
    val dailyLimitMs: Long,
    val isActive: Boolean = true
)

@Entity(tableName = "focus_mode_profiles")
data class FocusModeProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val isActive: Boolean = false
)

@Entity(tableName = "focus_mode_profile_apps")
data class FocusModeProfileApp(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val packageName: String
)

@Entity(tableName = "bedtime_schedules")
data class BedtimeSchedule(
    @PrimaryKey val id: Int = 1, // Single schedule for now
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: String, // e.g. "1,2,3,4,5"
    val isChargingTriggerEnabled: Boolean = false,
    val isGrayscaleEnabled: Boolean = true,
    val isDarkThemeEnabled: Boolean = true,
    val isActive: Boolean = false
)

@Entity(tableName = "focus_mode_schedules")
data class FocusModeSchedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: String,
    val isActive: Boolean = true
)

@Entity(tableName = "nudge_configs")
data class NudgeConfig(
    @PrimaryKey val packageName: String,
    val thresholdMs: Long,
    val isEnabled: Boolean = true
)

@Entity(tableName = "screen_time_goals")
data class ScreenTimeGoal(
    @PrimaryKey val id: Int = 1,
    val goalMs: Long,
    val isEnabled: Boolean = true
)

@Entity(tableName = "device_usage_stats")
data class DeviceUsageStat(
    @PrimaryKey val date: Long, // epoch ms, midnight
    val totalScreenTimeMs: Long,
    val unlockCount: Int,
    val totalNotifications: Int
)

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val key: String,
    val value: String?
)
