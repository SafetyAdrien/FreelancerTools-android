package com.freelancertools.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "timer_sessions")
data class TimerSession(
    @PrimaryKey val id: String,
    val mode: String, // "pomodoro" | "free"
    val durationSeconds: Int,
    val hourlyRate: Double,
    val earned: Double,
    val startedAt: Long,
)
