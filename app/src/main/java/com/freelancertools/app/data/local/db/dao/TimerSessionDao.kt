package com.freelancertools.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.freelancertools.app.data.local.db.entity.TimerSession
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerSessionDao {
    @Query("SELECT * FROM timer_sessions ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<TimerSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: TimerSession)
}
