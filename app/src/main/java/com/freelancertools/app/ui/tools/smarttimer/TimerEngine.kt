package com.freelancertools.app.ui.tools.smarttimer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

enum class TimerMode(val label: String, val durationSeconds: Int?) {
    POMODORO("Pomodoro (25m)", 25 * 60),
    FREE("Libre", null),
}

data class TimerUiState(
    val isRunning: Boolean = false,
    val mode: TimerMode = TimerMode.POMODORO,
    val elapsedSeconds: Int = 0,
    val hourlyRate: Double = 0.0,
    val taskLabel: String = "",
) {
    val remainingSeconds: Int?
        get() = mode.durationSeconds?.let { (it - elapsedSeconds).coerceAtLeast(0) }
    val earned: Double
        get() = elapsedSeconds / 3600.0 * hourlyRate
}

/**
 * Process-wide timer state shared between [TimerForegroundService] (which owns the ticking and
 * the notification) and the Compose UI (which only observes/commands it). Kept as a plain
 * singleton rather than a bound service to avoid AIDL/Binder boilerplate for a single int counter.
 */
object TimerEngine {
    private val _state = MutableStateFlow(TimerUiState())
    val state: StateFlow<TimerUiState> = _state

    fun configure(mode: TimerMode, hourlyRate: Double, taskLabel: String) {
        _state.update { it.copy(mode = mode, hourlyRate = hourlyRate, taskLabel = taskLabel) }
    }

    fun setRunning(running: Boolean) {
        _state.update { it.copy(isRunning = running) }
    }

    fun tick() {
        _state.update { current ->
            val next = current.elapsedSeconds + 1
            val duration = current.mode.durationSeconds
            if (duration != null && next >= duration) {
                current.copy(elapsedSeconds = duration, isRunning = false)
            } else {
                current.copy(elapsedSeconds = next)
            }
        }
    }

    fun reset() {
        _state.update { it.copy(isRunning = false, elapsedSeconds = 0) }
    }
}
