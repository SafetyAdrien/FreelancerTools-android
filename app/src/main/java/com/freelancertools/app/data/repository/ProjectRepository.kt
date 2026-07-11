package com.freelancertools.app.data.repository

import com.freelancertools.app.data.local.db.dao.ProjectDao
import com.freelancertools.app.data.local.db.entity.Project
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProjectRepository @Inject constructor(
    private val projectDao: ProjectDao,
) {
    fun observeForClient(clientId: String): Flow<List<Project>> = projectDao.observeForClient(clientId)
    fun observeAll(): Flow<List<Project>> = projectDao.observeAll()
    fun observeActiveCount(): Flow<Int> = projectDao.observeActiveCount()
    suspend fun save(project: Project) = projectDao.upsert(project)
    suspend fun delete(project: Project) = projectDao.delete(project)
}
