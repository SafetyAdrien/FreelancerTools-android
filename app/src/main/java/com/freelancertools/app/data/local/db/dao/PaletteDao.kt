package com.freelancertools.app.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.freelancertools.app.data.local.db.entity.Palette
import kotlinx.coroutines.flow.Flow

@Dao
interface PaletteDao {
    @Query("SELECT * FROM palettes ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Palette>>

    @Query("SELECT COUNT(*) FROM palettes")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(palette: Palette)

    @Delete
    suspend fun delete(palette: Palette)
}
