package com.wellbeing.service

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.wellbeing.core.data.repository.BedtimeRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BedtimeTileService : TileService() {

    @Inject
    lateinit var bedtimeRepository: BedtimeRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onStartListening() {
        super.onStartListening()
        serviceScope.launch {
            val isActive = bedtimeRepository.isBedtimeModeActive().first()
            qsTile.state = if (isActive) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            qsTile.updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        serviceScope.launch {
            val isActive = bedtimeRepository.isBedtimeModeActive().first()
            if (isActive) {
                bedtimeRepository.setBedtimeModeActive(false)
            } else {
                bedtimeRepository.setBedtimeModeActive(true)
            }
            onStartListening() // Refresh UI
        }
    }
}
