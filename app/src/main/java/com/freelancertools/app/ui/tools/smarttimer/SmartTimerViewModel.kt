package com.freelancertools.app.ui.tools.smarttimer

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelancertools.app.data.local.db.entity.TimerSession
import com.freelancertools.app.data.repository.TimerSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SmartTimerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val timerSessionRepository: TimerSessionRepository,
) : ViewModel() {

    val timerState: StateFlow<TimerUiState> = TimerEngine.state

    val history: StateFlow<List<TimerSession>> = timerSessionRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun start(mode: TimerMode, hourlyRate: Double, taskLabel: String) {
        TimerEngine.configure(mode, hourlyRate, taskLabel)
        sendServiceAction(TimerForegroundService.ACTION_START)
    }

    fun pause() {
        sendServiceAction(TimerForegroundService.ACTION_PAUSE)
    }

    fun reset() {
        val state = timerState.value
        if (state.elapsedSeconds > 0) {
            viewModelScope.launch {
                timerSessionRepository.save(
                    TimerSession(
                        id = UUID.randomUUID().toString(),
                        mode = if (state.mode == TimerMode.POMODORO) "pomodoro" else "free",
                        durationSeconds = state.elapsedSeconds,
                        hourlyRate = state.hourlyRate,
                        earned = state.earned,
                        startedAt = System.currentTimeMillis() - state.elapsedSeconds * 1000L,
                    ),
                )
            }
        }
        sendServiceAction(TimerForegroundService.ACTION_RESET)
    }

    private fun sendServiceAction(action: String) {
        val intent = Intent(context, TimerForegroundService::class.java).setAction(action)
        ContextCompat.startForegroundService(context, intent)
    }
}
