package com.wellbeing.core.data.repository

import android.content.Context
import com.wellbeing.core.data.local.dao.WellbeingDao
import com.wellbeing.core.data.local.entities.SettingsEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val wellbeingDao: WellbeingDao
) : SettingsRepository {

    private val prefs = context.getSharedPreferences("wellbeing_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LOCK_MODE = "lock_mode_active"
        private const val KEY_PAUSED_UNTIL = "paused_until_timestamp"
    }

    private val _lockModeActive = MutableStateFlow(prefs.getBoolean(KEY_LOCK_MODE, false))

    override fun getAppLimitPin(): Flow<String?> {
        return wellbeingDao.getSetting("app_limit_pin")
    }

    override suspend fun setAppLimitPin(pin: String?) {
        wellbeingDao.insertSetting(SettingsEntity("app_limit_pin", pin))
    }

    override fun getFocusModePausedUntil(): Flow<Long> {
        return wellbeingDao.getSetting("focus_mode_paused_until").map { it?.toLongOrNull() ?: 0L }
    }

    override suspend fun setFocusModePausedUntil(timestamp: Long) {
        wellbeingDao.insertSetting(SettingsEntity("focus_mode_paused_until", timestamp.toString()))
    }

    override fun isLockModeActive(): Flow<Boolean> = _lockModeActive.asStateFlow()

    override suspend fun setLockModeActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_LOCK_MODE, active).apply()
        _lockModeActive.value = active
    }

    override fun getPausedUntilTimestamp(): Flow<Long> {
        return MutableStateFlow(prefs.getLong(KEY_PAUSED_UNTIL, 0L)).asStateFlow()
    }

    override suspend fun setPausedUntilTimestamp(timestamp: Long) {
        prefs.edit().putLong(KEY_PAUSED_UNTIL, timestamp).apply()
    }
}
