package com.wellbeing.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wellbeing.core.data.local.dao.WellbeingDao
import com.wellbeing.core.data.local.entities.*

@Database(
    entities = [
        AppUsageStat::class,
        UsageEventEntity::class,
        AppLimit::class,
        FocusModeProfile::class,
        FocusModeProfileApp::class,
        BedtimeSchedule::class,
        FocusModeSchedule::class,
        NudgeConfig::class,
        ScreenTimeGoal::class,
        DeviceUsageStat::class,
        SettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class WellbeingDatabase : RoomDatabase() {
    abstract fun wellbeingDao(): WellbeingDao
}
