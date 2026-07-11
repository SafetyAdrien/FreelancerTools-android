package com.freelancertools.app.widget

import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.core.content.ContextCompat
import androidx.glance.appwidget.updateAll
import com.freelancertools.app.R
import com.freelancertools.app.ui.tools.smarttimer.TimerEngine
import com.freelancertools.app.ui.tools.smarttimer.TimerForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/** Quick Settings tile that starts/pauses the Smart Timer without opening the app. */
class SmartTimerTileService : TileService() {

    private val scope = CoroutineScope(SupervisorJob())
    private var observeJob: Job? = null

    override fun onStartListening() {
        super.onStartListening()
        refreshTile(TimerEngine.state.value.isRunning)
        observeJob = TimerEngine.state
            .onEach { refreshTile(it.isRunning) }
            .launchIn(scope)
    }

    override fun onStopListening() {
        super.onStopListening()
        observeJob?.cancel()
    }

    override fun onClick() {
        super.onClick()
        val running = TimerEngine.state.value.isRunning
        val action = if (running) TimerForegroundService.ACTION_PAUSE else TimerForegroundService.ACTION_START
        val intent = Intent(this, TimerForegroundService::class.java).setAction(action)
        ContextCompat.startForegroundService(this, intent)
        scope.launch { SmartTimerWidget().updateAll(this@SmartTimerTileService) }
    }

    private fun refreshTile(isRunning: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (isRunning) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = "Smart Timer"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = if (isRunning) "En cours" else "En pause"
        }
        tile.icon = Icon.createWithResource(this, R.drawable.ic_tile_timer)
        tile.updateTile()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
