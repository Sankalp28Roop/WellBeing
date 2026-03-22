package com.wellbeing.ui.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wellbeing.core.data.local.entities.FocusModeProfile
import com.wellbeing.core.data.repository.FocusModeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FocusModeUiState(
    val profiles: List<FocusModeProfile> = emptyList(),
    val activeProfile: FocusModeProfile? = null,
    val selectedApps: List<String> = emptyList(),
    val isLoading: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FocusModeViewModel @Inject constructor(
    private val repository: FocusModeRepository
) : ViewModel() {

    private val _selectedProfileId = MutableStateFlow<Long?>(null)

    val uiState: StateFlow<FocusModeUiState> = combine(
        repository.getProfiles(),
        repository.getActiveProfile(),
        _selectedProfileId.flatMapLatest { id ->
            if (id != null) repository.getAppsForProfile(id) else flowOf(emptyList())
        }
    ) { profiles, active, apps ->
        FocusModeUiState(
            profiles = profiles,
            activeProfile = active,
            selectedApps = apps,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FocusModeUiState(isLoading = true))

    fun selectProfile(profileId: Long) {
        _selectedProfileId.value = profileId
    }

    fun createProfile(name: String) {
        viewModelScope.launch {
            repository.createProfile(name)
        }
    }

    fun toggleFocusMode(profileId: Long, active: Boolean) {
        viewModelScope.launch {
            if (active) repository.activateProfile(profileId)
            else repository.deactivateAll()
        }
    }
}
