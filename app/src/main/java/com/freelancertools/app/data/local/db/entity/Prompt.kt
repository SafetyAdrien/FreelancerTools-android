package com.freelancertools.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompts")
data class Prompt(
    @PrimaryKey val id: String,
    val title: String,
    val tag: String,
    val content: String,
    val updatedAt: Long,
)
