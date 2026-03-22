package com.wellbeing.core.data.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun getAppLimitPin(): Flow<String?>
    suspend fun setAppLimitPin(pin: String?)
    fun getFocusModePausedUntil(): Flow<Long>
    suspend fun setFocusModePausedUntil(timestamp: Long)
    fun isLockModeActive(): Flow<Boolean>
    suspend fun setLockModeActive(active: Boolean)
    fun getPausedUntilTimestamp(): Flow<Long>
    suspend fun setPausedUntilTimestamp(timestamp: Long)
}
