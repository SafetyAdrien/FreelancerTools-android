package com.freelancertools.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey val id: String,
    val number: Int,
    val clientId: String?,
    val issuerName: String,
    val items: String, // JSON list of {description, price}
    val totalHT: Double,
    val createdAt: Long,
    val pdfPath: String?,
)
