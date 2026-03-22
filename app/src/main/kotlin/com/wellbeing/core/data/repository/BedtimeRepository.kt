package com.wellbeing.core.data.repository

import android.app.NotificationManager
import android.content.Context
import com.wellbeing.core.data.local.dao.WellbeingDao
import com.wellbeing.core.data.local.entities.BedtimeSchedule
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface BedtimeRepository {
    fun getSchedule(): Flow<BedtimeSchedule?>
    suspend fun updateSchedule(schedule: BedtimeSchedule)
    fun isBedtimeModeActive(): Flow<Boolean>
    suspend fun setBedtimeModeActive(active: Boolean)
}

@Singleton
class BedtimeRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wellbeingDao: WellbeingDao
) : BedtimeRepository {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun getSchedule(): Flow<BedtimeSchedule?> {
        return wellbeingDao.getBedtimeSchedule()
    }

    override suspend fun updateSchedule(schedule: BedtimeSchedule) {
        wellbeingDao.insertBedtimeSchedule(schedule)
        updateDndRule(schedule)
    }

    private fun updateDndRule(schedule: BedtimeSchedule) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            // Check if we have permission to change DND
            if (!notificationManager.isNotificationPolicyAccessGranted) {
                return
            }

            // In a real implementation, we would register a specific AutomaticZenRule
            // For now, we'll use a direct filter update when active
            if (schedule.isActive) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            } else {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
        }
    }

    override fun isBedtimeModeActive(): Flow<Boolean> {
        return getSchedule().map { it?.isActive ?: false }
    }

    override suspend fun setBedtimeModeActive(active: Boolean) {
        val currentSchedule = getSchedule().first()
        if (currentSchedule != null) {
            updateSchedule(currentSchedule.copy(isActive = active))
        } else if (active) {
            // Create a default schedule if none exists and we are activating it
            updateSchedule(
                BedtimeSchedule(
                    startHour = 22,
                    startMinute = 0,
                    endHour = 7,
                    endMinute = 0,
                    daysOfWeek = "1,2,3,4,5,6,7",
                    isActive = true
                )
            )
        }
    }
}
