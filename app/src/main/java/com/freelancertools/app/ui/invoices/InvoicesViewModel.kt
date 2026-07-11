package com.freelancertools.app.ui.invoices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelancertools.app.data.local.db.entity.Invoice
import com.freelancertools.app.data.repository.ClientRepository
import com.freelancertools.app.data.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class InvoiceRow(val invoice: Invoice, val clientName: String?)

@HiltViewModel
class InvoicesViewModel @Inject constructor(
    invoiceRepository: InvoiceRepository,
    clientRepository: ClientRepository,
) : ViewModel() {

    val invoices: StateFlow<List<InvoiceRow>> = combine(
        invoiceRepository.observeAll(),
        clientRepository.observeAll(),
    ) { invoices, clients ->
        val byId = clients.associateBy { it.id }
        invoices.map { InvoiceRow(it, byId[it.clientId]?.name) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
