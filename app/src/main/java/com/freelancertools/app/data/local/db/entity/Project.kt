package com.freelancertools.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class Project(
    @PrimaryKey val id: String,
    val clientId: String,
    val title: String,
    val description: String?,
    val tag: String,
    val price: Double,
    val status: String, // "active" | "done" | "upcoming"
    val completedAt: Long?,
)

object ProjectStatus {
    const val ACTIVE = "active"
    const val DONE = "done"
    const val UPCOMING = "upcoming"
}
