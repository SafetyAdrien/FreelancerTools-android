package com.freelancertools.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey val id: String,
    val label: String,
    val amount: Double,
    val type: String, // "income" | "expense"
    val date: Long,
    val clientId: String?,
)

object TransactionType {
    const val INCOME = "income"
    const val EXPENSE = "expense"
}
