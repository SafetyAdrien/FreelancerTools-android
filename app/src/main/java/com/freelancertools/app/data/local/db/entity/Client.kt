package com.freelancertools.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "clients")
data class Client(
    @PrimaryKey val id: String,
    val name: String,
    val company: String?,
    val email: String?,
    val phone: String?,
    val createdAt: Long,
)
