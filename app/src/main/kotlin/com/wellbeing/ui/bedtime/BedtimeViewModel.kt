package com.wellbeing.ui.bedtime

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wellbeing.core.data.local.entities.BedtimeSchedule
import com.wellbeing.core.data.repository.BedtimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BedtimeViewModel @Inject constructor(
    private val repository: BedtimeRepository
) : ViewModel() {

    val schedule: StateFlow<BedtimeSchedule?> = repository.getSchedule()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun updateSchedule(schedule: BedtimeSchedule) {
        viewModelScope.launch {
            repository.updateSchedule(schedule)
        }
    }
}
