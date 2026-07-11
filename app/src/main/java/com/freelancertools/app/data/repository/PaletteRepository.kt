package com.freelancertools.app.data.repository

import com.freelancertools.app.data.local.db.dao.PaletteDao
import com.freelancertools.app.data.local.db.entity.Palette
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaletteRepository @Inject constructor(
    private val paletteDao: PaletteDao,
) {
    fun observeAll(): Flow<List<Palette>> = paletteDao.observeAll()
    fun observeCount(): Flow<Int> = paletteDao.observeCount()
    suspend fun save(palette: Palette) = paletteDao.upsert(palette)
    suspend fun delete(palette: Palette) = paletteDao.delete(palette)
}
