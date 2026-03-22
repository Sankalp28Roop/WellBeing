package com.wellbeing.core.data.repository

import com.wellbeing.core.data.local.dao.WellbeingDao
import com.wellbeing.core.data.local.entities.FocusModeProfile
import com.wellbeing.core.data.local.entities.FocusModeProfileApp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface FocusModeRepository {
    fun getProfiles(): Flow<List<FocusModeProfile>>
    fun getActiveProfile(): Flow<FocusModeProfile?>
    fun getAppsForProfile(profileId: Long): Flow<List<String>>
    suspend fun createProfile(name: String): Long
    suspend fun updateProfileApps(profileId: Long, packageNames: List<String>)
    suspend fun activateProfile(profileId: Long)
    suspend fun deactivateAll()
    fun isFocusModeActive(): Flow<Boolean>
    suspend fun setFocusModeActive(active: Boolean)
    suspend fun pauseFocusMode(durationMs: Long)
}

@Singleton
class FocusModeRepositoryImpl @Inject constructor(
    private val wellbeingDao: WellbeingDao
) : FocusModeRepository {

    override fun getProfiles(): Flow<List<FocusModeProfile>> {
        return wellbeingDao.getFocusProfiles()
    }

    override fun getActiveProfile(): Flow<FocusModeProfile?> {
        return wellbeingDao.getActiveFocusProfile()
    }

    override fun getAppsForProfile(profileId: Long): Flow<List<String>> {
        return wellbeingDao.getAppsForProfile(profileId)
    }

    override suspend fun createProfile(name: String): Long {
        return wellbeingDao.insertFocusProfile(FocusModeProfile(name = name))
    }

    override suspend fun updateProfileApps(profileId: Long, packageNames: List<String>) {
        wellbeingDao.deleteAppsForProfile(profileId)
        val apps = packageNames.map { FocusModeProfileApp(profileId = profileId, packageName = it) }
        wellbeingDao.insertProfileApps(apps)
    }

    override suspend fun activateProfile(profileId: Long) {
        wellbeingDao.deactivateAllFocusProfiles()
        wellbeingDao.activateFocusProfile(profileId)
    }

    override suspend fun deactivateAll() {
        wellbeingDao.deactivateAllFocusProfiles()
    }

    override fun isFocusModeActive(): Flow<Boolean> {
        return getActiveProfile().map { it != null }
    }

    override suspend fun setFocusModeActive(active: Boolean) {
        if (active) {
            val profiles = getProfiles().first()
            if (profiles.isNotEmpty()) {
                activateProfile(profiles.first().id)
            } else {
                val id = createProfile("Default Focus Mode")
                activateProfile(id)
            }
        } else {
            deactivateAll()
        }
    }

    override suspend fun pauseFocusMode(durationMs: Long) {
        // Implementation provided via SettingsRepository for persistence
    }
}
