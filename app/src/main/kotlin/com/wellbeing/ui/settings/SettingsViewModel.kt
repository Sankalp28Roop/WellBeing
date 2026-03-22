package com.wellbeing.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wellbeing.core.data.local.WellbeingDatabase
import com.wellbeing.data.export.DataExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class SettingsUiState(
    val remindersEnabled: Boolean = true,
    val goalAlertsEnabled: Boolean = true
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: WellbeingDatabase,
    private val dataExporter: DataExporter
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun exportJson(): File? {
        return try {
            kotlinx.coroutines.runBlocking {
                dataExporter.exportToJson()
            }
        } catch (e: Exception) {
            null
        }
    }

    fun exportCsv(): File? {
        return try {
            kotlinx.coroutines.runBlocking {
                dataExporter.exportToCsv()
            }
        } catch (e: Exception) {
            null
        }
    }

    fun shareFile(file: File): android.content.Intent {
        return dataExporter.shareFile(file)
    }

    fun toggleReminders(enabled: Boolean) {
        _uiState.update { it.copy(remindersEnabled = enabled) }
    }

    fun toggleGoalAlerts(enabled: Boolean) {
        _uiState.update { it.copy(goalAlertsEnabled = enabled) }
    }

    fun clearAllData() {
        viewModelScope.launch {
            database.clearAllTables()
        }
    }
}
