package com.wellbeing.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wellbeing.core.data.local.entities.AppUsageStat
import com.wellbeing.core.data.local.entities.DeviceUsageStat
import com.wellbeing.core.data.local.entities.AppLimit
import com.wellbeing.core.domain.usecase.GetUsageStatsUseCase
import com.wellbeing.core.domain.usecase.GetDeviceUsageStatUseCase
import com.wellbeing.core.data.repository.UsageRepository
import com.wellbeing.core.data.repository.FocusModeRepository
import com.wellbeing.core.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

enum class DashboardPivot { SCREEN_TIME, UNLOCKS, NOTIFICATIONS }

data class DashboardUiState(
    val stats: List<AppUsageStat> = emptyList(),
    val weeklyDeviceStats: List<DeviceUsageStat> = emptyList(),
    val weeklyAppStats: List<AppUsageStat> = emptyList(),
    val totalScreenTimeMs: Long = 0L,
    val unlockCount: Int = 0,
    val totalNotifications: Int = 0,
    val selectedPivot: DashboardPivot = DashboardPivot.SCREEN_TIME,
    val isLoading: Boolean = false,
    val selectedApp: AppUsageStat? = null,
    val currentAppLimit: AppLimit? = null,
    val isLockModeActive: Boolean = false,
    val isFocusModeActive: Boolean = false,
    val isPaused: Boolean = false
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getUsageStatsUseCase: GetUsageStatsUseCase,
    private val getDeviceUsageStatUseCase: GetDeviceUsageStatUseCase,
    private val usageRepository: UsageRepository,
    private val focusModeRepository: FocusModeRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _selectedPivot = MutableStateFlow(DashboardPivot.SCREEN_TIME)
    val selectedPivot = _selectedPivot.asStateFlow()

    private val _selectedApp = MutableStateFlow<AppUsageStat?>(null)
    val selectedApp = _selectedApp.asStateFlow()

    private val _isLockModeActive = settingsRepository.isLockModeActive()
    private val _isFocusModeActive = focusModeRepository.isFocusModeActive()
    private val _pausedUntil = settingsRepository.getPausedUntilTimestamp()

    private val _isPaused = _pausedUntil.map { System.currentTimeMillis() < it }

    private fun getMidnight(daysOffset: Int = 0): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, daysOffset)
    }.timeInMillis

    private val todayMidnight = getMidnight(0)

    private val usageStatsFlow = getUsageStatsUseCase(todayMidnight)
    private val deviceStatsFlow = getDeviceUsageStatUseCase(todayMidnight)
    private val appLimitsFlow = usageRepository.getAllAppLimits()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> = combine(
        usageStatsFlow,
        deviceStatsFlow,
        _selectedPivot,
        _selectedApp,
        appLimitsFlow,
        _isLockModeActive,
        _isFocusModeActive,
        _isPaused
    ) { values: Array<*> ->
        @Suppress("UNCHECKED_CAST")
        DashboardUiState(
            stats = when (values[2] as DashboardPivot) {
                DashboardPivot.SCREEN_TIME -> (values[0] as List<AppUsageStat>).sortedByDescending { it.totalForegroundMs }
                DashboardPivot.UNLOCKS -> (values[0] as List<AppUsageStat>).sortedByDescending { it.timesOpened }
                DashboardPivot.NOTIFICATIONS -> (values[0] as List<AppUsageStat>).sortedByDescending { it.notificationCount }
            },
            weeklyDeviceStats = emptyList(),
            weeklyAppStats = emptyList(),
            totalScreenTimeMs = (values[1] as DeviceUsageStat?)?.totalScreenTimeMs ?: (values[0] as List<AppUsageStat>).sumOf { it.totalForegroundMs },
            unlockCount = (values[1] as DeviceUsageStat?)?.unlockCount ?: 0,
            totalNotifications = (values[1] as DeviceUsageStat?)?.totalNotifications ?: 0,
            selectedPivot = values[2] as DashboardPivot,
            isLoading = false,
            selectedApp = values[3] as AppUsageStat?,
            currentAppLimit = (values[3] as AppUsageStat?)?.let { app -> (values[4] as List<AppLimit>).find { it.packageName == app.packageName } },
            isLockModeActive = values[5] as Boolean,
            isFocusModeActive = values[6] as Boolean,
            isPaused = values[7] as Boolean
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState(isLoading = true)
    )

    private fun getWeeklyDeviceStatsFlow(): Flow<List<DeviceUsageStat>> {
        val dayFlows = (-6..0).map { i ->
            getDeviceUsageStatUseCase(getMidnight(i))
                .map { it ?: DeviceUsageStat(getMidnight(i), 0, 0, 0) }
        }
        return combine(dayFlows) { it.toList() }
    }

    private fun getWeeklyAppStatsFlow(packageName: String): Flow<List<AppUsageStat>> {
        val dayFlows = (-6..0).map { i ->
            val day = getMidnight(i)
            getUsageStatsUseCase(day).map { dayStats ->
                dayStats.find { it.packageName == packageName } 
                    ?: AppUsageStat(packageName = packageName, date = day, totalForegroundMs = 0, timesOpened = 0, notificationCount = 0)
            }
        }
        return combine(dayFlows) { it.toList() }
    }

    fun onPivotSelected(pivot: DashboardPivot) {
        _selectedPivot.value = pivot
    }

    fun selectApp(stat: AppUsageStat?) {
        _selectedApp.value = stat
    }

    fun setAppLimit(packageName: String, limitMs: Long) {
        viewModelScope.launch {
            usageRepository.setAppLimit(packageName, limitMs)
        }
    }

    fun togglePause(minutes: Int = 15) {
        viewModelScope.launch {
            val until = System.currentTimeMillis() + (minutes * 60 * 1000L)
            settingsRepository.setPausedUntilTimestamp(until)
        }
    }

    fun unpause() {
        viewModelScope.launch {
            settingsRepository.setPausedUntilTimestamp(0L)
        }
    }

    fun toggleLockMode() {
        viewModelScope.launch {
            val currentState = settingsRepository.isLockModeActive().first()
            settingsRepository.setLockModeActive(!currentState)
        }
    }

    fun toggleFocusMode() {
        viewModelScope.launch {
            val currentState = focusModeRepository.isFocusModeActive().first()
            if (currentState) {
                focusModeRepository.deactivateAll()
            } else {
                focusModeRepository.setFocusModeActive(true)
            }
        }
    }
}
