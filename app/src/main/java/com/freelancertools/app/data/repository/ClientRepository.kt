package com.freelancertools.app.data.repository

import com.freelancertools.app.data.local.db.dao.ClientDao
import com.freelancertools.app.data.local.db.entity.Client
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClientRepository @Inject constructor(
    private val clientDao: ClientDao,
) {
    fun observeAll(): Flow<List<Client>> = clientDao.observeAll()
    fun observeById(id: String): Flow<Client?> = clientDao.observeById(id)
    fun observeActiveCount(): Flow<Int> = clientDao.observeCount()
    suspend fun getById(id: String): Client? = clientDao.getById(id)
    suspend fun save(client: Client) = clientDao.upsert(client)
    suspend fun delete(client: Client) = clientDao.delete(client)
}
