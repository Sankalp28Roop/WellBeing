package com.wellbeing.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.wellbeing.core.data.repository.FocusModeRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FocusModeTileService : TileService() {

    @Inject
    lateinit var focusModeRepository: FocusModeRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onStartListening() {
        super.onStartListening()
        serviceScope.launch {
            val isActive = focusModeRepository.isFocusModeActive().first()
            qsTile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            qsTile.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        serviceScope.launch {
            val isActive = focusModeRepository.isFocusModeActive().first()
            if (isActive) {
                focusModeRepository.setFocusModeActive(false)
            } else {
                focusModeRepository.setFocusModeActive(true)
            }
            onStartListening() // Refresh UI
        }
    }
}
