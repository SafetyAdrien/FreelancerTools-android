package com.freelancertools.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.freelancertools.app.data.local.db.entity.Prompt
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<Prompt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(prompt: Prompt)

    @Delete
    suspend fun delete(prompt: Prompt)
}
