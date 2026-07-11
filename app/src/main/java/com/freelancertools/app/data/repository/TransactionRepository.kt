package com.freelancertools.app.data.repository

import com.freelancertools.app.data.local.db.dao.TransactionDao
import com.freelancertools.app.data.local.db.entity.Transaction
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val transactionDao: TransactionDao,
) {
    fun observeAll(): Flow<List<Transaction>> = transactionDao.observeAll()
    fun observeBetween(start: Long, end: Long): Flow<List<Transaction>> = transactionDao.observeBetween(start, end)
    suspend fun save(transaction: Transaction) = transactionDao.upsert(transaction)
    suspend fun delete(transaction: Transaction) = transactionDao.delete(transaction)
}
