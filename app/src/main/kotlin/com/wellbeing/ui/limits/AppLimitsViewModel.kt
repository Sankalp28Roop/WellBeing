package com.wellbeing.ui.limits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wellbeing.core.data.local.entities.AppLimit
import com.wellbeing.core.data.repository.SettingsRepository
import com.wellbeing.core.data.repository.UsageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppLimitsViewModel @Inject constructor(
    private val repository: UsageRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val limits: StateFlow<List<AppLimit>> = repository.getAllAppLimits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val appLimitPin: StateFlow<String?> = settingsRepository.getAppLimitPin()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun setLimit(packageName: String, limitMs: Long) {
        viewModelScope.launch {
            repository.setAppLimit(packageName, limitMs)
        }
    }

    fun removeLimit(packageName: String) {
        viewModelScope.launch {
            repository.deleteAppLimit(packageName)
        }
    }

    fun setPin(pin: String) {
        viewModelScope.launch {
            settingsRepository.setAppLimitPin(pin)
        }
    }
}
