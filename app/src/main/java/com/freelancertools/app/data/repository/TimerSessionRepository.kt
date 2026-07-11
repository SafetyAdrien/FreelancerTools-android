package com.freelancertools.app.data.repository

import com.freelancertools.app.data.local.db.dao.TimerSessionDao
import com.freelancertools.app.data.local.db.entity.TimerSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimerSessionRepository @Inject constructor(
    private val timerSessionDao: TimerSessionDao,
) {
    fun observeAll(): Flow<List<TimerSession>> = timerSessionDao.observeAll()
    suspend fun save(session: TimerSession) = timerSessionDao.insert(session)
}
