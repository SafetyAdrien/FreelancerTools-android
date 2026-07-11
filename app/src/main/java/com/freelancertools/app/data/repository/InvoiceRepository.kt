package com.freelancertools.app.data.repository

import com.freelancertools.app.data.local.db.dao.InvoiceDao
import com.freelancertools.app.data.local.db.entity.Invoice
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceRepository @Inject constructor(
    private val invoiceDao: InvoiceDao,
) {
    fun observeAll(): Flow<List<Invoice>> = invoiceDao.observeAll()
    suspend fun getById(id: String): Invoice? = invoiceDao.getById(id)
    suspend fun nextInvoiceNumber(): Int = (invoiceDao.getMaxNumber() ?: 0) + 1
    suspend fun save(invoice: Invoice) = invoiceDao.upsert(invoice)
    suspend fun delete(invoice: Invoice) = invoiceDao.delete(invoice)
}
