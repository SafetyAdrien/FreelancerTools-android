package com.freelancertools.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.freelancertools.app.data.local.db.entity.Project
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects WHERE clientId = :clientId ORDER BY completedAt DESC")
    fun observeForClient(clientId: String): Flow<List<Project>>

    @Query("SELECT * FROM projects ORDER BY completedAt DESC")
    fun observeAll(): Flow<List<Project>>

    @Query("SELECT COUNT(*) FROM projects WHERE status = 'active'")
    fun observeActiveCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(project: Project)

    @Update
    suspend fun update(project: Project)

    @Delete
    suspend fun delete(project: Project)
}
