package com.freelancertools.app.data.repository

import com.freelancertools.app.data.local.db.dao.PromptDao
import com.freelancertools.app.data.local.db.entity.Prompt
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PromptRepository @Inject constructor(
    private val promptDao: PromptDao,
) {
    fun observeAll(): Flow<List<Prompt>> = promptDao.observeAll()
    suspend fun save(prompt: Prompt) = promptDao.upsert(prompt)
    suspend fun delete(prompt: Prompt) = promptDao.delete(prompt)
}
