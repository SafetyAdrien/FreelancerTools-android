package com.freelancertools.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.freelancertools.app.data.local.db.entity.Client
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY name ASC")
    fun observeAll(): Flow<List<Client>>

    @Query("SELECT * FROM clients WHERE id = :id")
    fun observeById(id: String): Flow<Client?>

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getById(id: String): Client?

    @Query("SELECT COUNT(*) FROM clients")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(client: Client)

    @Update
    suspend fun update(client: Client)

    @Delete
    suspend fun delete(client: Client)
}
