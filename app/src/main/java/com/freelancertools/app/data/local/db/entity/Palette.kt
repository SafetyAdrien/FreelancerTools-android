package com.freelancertools.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "palettes")
data class Palette(
    @PrimaryKey val id: String,
    val name: String,
    val colors: String, // JSON list of hex strings
    val createdAt: Long,
)
