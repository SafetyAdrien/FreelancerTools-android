package com.freelancertools.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.freelancertools.app.data.local.db.entity.Invoice
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY number DESC")
    fun observeAll(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun getById(id: String): Invoice?

    @Query("SELECT MAX(number) FROM invoices")
    suspend fun getMaxNumber(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(invoice: Invoice)

    @Delete
    suspend fun delete(invoice: Invoice)
}
