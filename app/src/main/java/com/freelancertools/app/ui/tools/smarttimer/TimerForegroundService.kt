package com.freelancertools.app.ui.tools.smarttimer

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.freelancertools.app.FreelancerToolsApp
import com.freelancertools.app.MainActivity
import androidx.glance.appwidget.updateAll
import com.freelancertools.app.widget.SmartTimerWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/** Foreground service that keeps [TimerEngine] ticking every second while the app is backgrounded. */
class TimerForegroundService : Service() {

    private val scope = CoroutineScope(SupervisorJob())
    private var tickerJob: Job? = null
    private var notificationJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> TimerEngine.setRunning(false)
            ACTION_RESET -> TimerEngine.reset()
            else -> TimerEngine.setRunning(true)
        }
        startForeground(NOTIFICATION_ID, buildNotification(TimerEngine.state.value))
        startTickerIfNeeded()
        observeStateForNotification()
        return START_STICKY
    }

    private fun startTickerIfNeeded() {
        if (tickerJob?.isActive == true) return
        tickerJob = scope.launch {
            while (true) {
                delay(1000)
                if (TimerEngine.state.value.isRunning) {
                    TimerEngine.tick()
                }
            }
        }
    }

    private fun observeStateForNotification() {
        if (notificationJob?.isActive == true) return
        var lastRunning: Boolean? = null
        notificationJob = scope.launch {
            TimerEngine.state.collectLatest { state ->
                val manager = getSystemService(android.app.NotificationManager::class.java)
                manager?.notify(NOTIFICATION_ID, buildNotification(state))

                // Refresh the home screen widget on start/pause transitions and every 30s while running,
                // rather than on every tick, to stay within AppWidgetManager's update-rate expectations.
                if (lastRunning != state.isRunning || (state.isRunning && state.elapsedSeconds % 30 == 0)) {
                    SmartTimerWidget().updateAll(this@TimerForegroundService)
                }
                lastRunning = state.isRunning

                if (!state.isRunning && state.elapsedSeconds == 0) {
                    stopSelf()
                }
            }
        }
    }

    private fun buildNotification(state: TimerUiState): Notification {
        val minutes = state.elapsedSeconds / 60
        val seconds = state.elapsedSeconds % 60
        val timeText = "%02d:%02d".format(minutes, seconds)
        val earnedText = NumberFormat.getCurrencyInstance(Locale.FRANCE).format(state.earned)

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        return NotificationCompat.Builder(this, FreelancerToolsApp.TIMER_CHANNEL_ID)
            .setContentTitle(if (state.isRunning) "Smart Timer en cours" else "Smart Timer en pause")
            .setContentText("$timeText · $earnedText")
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setOngoing(state.isRunning)
            .setContentIntent(contentIntent)
            .setOnlyAlertOnce(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        const val NOTIFICATION_ID = 4201
        const val ACTION_START = "com.freelancertools.app.timer.START"
        const val ACTION_PAUSE = "com.freelancertools.app.timer.PAUSE"
        const val ACTION_RESET = "com.freelancertools.app.timer.RESET"
    }
}
