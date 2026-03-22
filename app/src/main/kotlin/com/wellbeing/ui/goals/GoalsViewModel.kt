package com.wellbeing.ui.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wellbeing.core.data.local.entities.ScreenTimeGoal
import com.wellbeing.core.data.repository.UsageRepository
import com.wellbeing.core.domain.usecase.GetDeviceUsageStatUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class GoalsUiState(
    val dailyScreenTimeGoalMs: Long = 2 * 60 * 60 * 1000L,
    val currentScreenTimeMs: Long = 0L,
    val isGoalEnabled: Boolean = false,
    val goalProgress: Float = 0f,
    val isLoading: Boolean = false
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val usageRepository: UsageRepository,
    private val getDeviceUsageStatUseCase: GetDeviceUsageStatUseCase
) : ViewModel() {

    private fun getTodayMidnight(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    val uiState: StateFlow<GoalsUiState> = combine(
        usageRepository.getScreenTimeGoal(),
        getDeviceUsageStatUseCase(getTodayMidnight())
    ) { goal, deviceStat ->
        val goalMs = goal?.goalMs ?: (2 * 60 * 60 * 1000L)
        val currentMs = deviceStat?.totalScreenTimeMs ?: 0L
        val progress = if (goalMs > 0) (currentMs.toFloat() / goalMs).coerceIn(0f, 1f) else 0f
        
        GoalsUiState(
            dailyScreenTimeGoalMs = goalMs,
            currentScreenTimeMs = currentMs,
            isGoalEnabled = goal?.isEnabled ?: false,
            goalProgress = progress,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalsUiState(isLoading = true)
    )

    fun setScreenTimeGoal(goalMs: Long) {
        viewModelScope.launch {
            usageRepository.setScreenTimeGoal(goalMs)
        }
    }

    fun toggleGoal(enabled: Boolean) {
        viewModelScope.launch {
            val current = uiState.value.dailyScreenTimeGoalMs
            usageRepository.setScreenTimeGoal(current, enabled)
        }
    }
}
