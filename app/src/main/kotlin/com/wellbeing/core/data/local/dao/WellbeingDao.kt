package com.wellbeing.core.data.local.dao

import androidx.room.*
import com.wellbeing.core.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WellbeingDao {
    // Usage Stats
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageStat(stat: AppUsageStat)

    @Query("SELECT * FROM app_usage_stats WHERE date = :date")
    fun getUsageStatsForDate(date: Long): Flow<List<AppUsageStat>>

    @Query("SELECT * FROM app_usage_stats WHERE packageName = :packageName AND date = :date")
    suspend fun getUsageStat(packageName: String, date: Long): AppUsageStat?

    @Query("DELETE FROM app_usage_stats WHERE date = :date")
    suspend fun deleteUsageStatsForDate(date: Long)

    @Query("DELETE FROM device_usage_stats WHERE date = :date")
    suspend fun deleteDeviceUsageStatsForDate(date: Long)

    // Usage Events
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsageEvents(events: List<UsageEventEntity>)

    @Query("SELECT * FROM usage_events WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getUsageEvents(startTime: Long, endTime: Long): List<UsageEventEntity>

    @Query("DELETE FROM usage_events WHERE timestamp < :timestamp")
    suspend fun deleteOldEvents(timestamp: Long)

    // App Limits
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppLimit(limit: AppLimit)

    @Query("SELECT * FROM app_limits")
    fun getAllAppLimits(): Flow<List<AppLimit>>

    @Query("SELECT * FROM app_limits WHERE packageName = :packageName")
    suspend fun getAppLimit(packageName: String): AppLimit?

    @Delete
    suspend fun deleteAppLimit(limit: AppLimit)

    // Focus Mode
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusProfile(profile: FocusModeProfile): Long

    @Query("SELECT * FROM focus_mode_profiles")
    fun getFocusProfiles(): Flow<List<FocusModeProfile>>

    @Query("SELECT * FROM focus_mode_profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveFocusProfile(): Flow<FocusModeProfile?>

    @Query("SELECT packageName FROM focus_mode_profile_apps WHERE profileId = :profileId")
    fun getAppsForProfile(profileId: Long): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfileApps(apps: List<FocusModeProfileApp>)

    @Query("DELETE FROM focus_mode_profile_apps WHERE profileId = :profileId")
    suspend fun deleteAppsForProfile(profileId: Long)

    @Query("UPDATE focus_mode_profiles SET isActive = 0")
    suspend fun deactivateAllFocusProfiles()

    @Query("UPDATE focus_mode_profiles SET isActive = 1 WHERE id = :profileId")
    suspend fun activateFocusProfile(profileId: Long)

    // Bedtime
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBedtimeSchedule(schedule: BedtimeSchedule)

    @Query("SELECT * FROM bedtime_schedules WHERE id = 1")
    fun getBedtimeSchedule(): Flow<BedtimeSchedule?>

    // Goals
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenTimeGoal(goal: ScreenTimeGoal)

    @Query("SELECT * FROM screen_time_goals WHERE id = 1")
    fun getScreenTimeGoal(): Flow<ScreenTimeGoal?>

    // Device Usage Stats
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeviceUsageStat(stat: DeviceUsageStat)

    @Query("SELECT * FROM device_usage_stats WHERE date = :date")
    fun getDeviceUsageStat(date: Long): Flow<DeviceUsageStat?>

    // Settings
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: SettingsEntity)

    @Query("SELECT value FROM settings WHERE `key` = :key")
    fun getSetting(key: String): Flow<String?>
}
