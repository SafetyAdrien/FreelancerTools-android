package com.freelancertools.app.ui.invoices

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelancertools.app.data.local.datastore.PreferencesManager
import com.freelancertools.app.data.local.db.entity.Client
import com.freelancertools.app.data.local.db.entity.Invoice
import com.freelancertools.app.data.repository.ClientRepository
import com.freelancertools.app.data.repository.InvoiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

data class InvoiceEditorUiState(
    val invoiceId: String = UUID.randomUUID().toString(),
    val number: Int = 1,
    val issuerName: String = "",
    val clients: List<Client> = emptyList(),
    val selectedClientId: String? = null,
    val items: List<InvoiceLineItem> = listOf(InvoiceLineItem("", 0.0)),
    val createdAt: Long = System.currentTimeMillis(),
) {
    val selectedClientName: String?
        get() = clients.firstOrNull { it.id == selectedClientId }?.name
    val totalHT: Double
        get() = items.sumOf { it.price }
}

@HiltViewModel
class InvoiceEditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val invoiceRepository: InvoiceRepository,
    private val clientRepository: ClientRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val existingInvoiceId: String? = savedStateHandle.get<String>("invoiceId")?.takeIf { it.isNotBlank() }

    private val editorState = MutableStateFlow(InvoiceEditorUiState())

    val uiState: StateFlow<InvoiceEditorUiState> = combine(
        editorState,
        clientRepository.observeAll(),
    ) { state, clients ->
        state.copy(clients = clients)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InvoiceEditorUiState())

    init {
        viewModelScope.launch {
            val issuer = preferencesManager.companyName.first().ifBlank { preferencesManager.userName.first() }

            if (existingInvoiceId != null) {
                val invoice = invoiceRepository.getById(existingInvoiceId)
                if (invoice != null) {
                    editorState.value = InvoiceEditorUiState(
                        invoiceId = invoice.id,
                        number = invoice.number,
                        issuerName = invoice.issuerName,
                        selectedClientId = invoice.clientId,
                        items = invoice.items.toInvoiceLineItems().ifEmpty { listOf(InvoiceLineItem("", 0.0)) },
                        createdAt = invoice.createdAt,
                    )
                    return@launch
                }
            }
            editorState.update {
                it.copy(number = invoiceRepository.nextInvoiceNumber(), issuerName = issuer)
            }
        }
    }

    fun selectClient(clientId: String) = editorState.update { it.copy(selectedClientId = clientId) }

    fun updateItem(index: Int, item: InvoiceLineItem) = editorState.update { state ->
        state.copy(items = state.items.toMutableList().also { it[index] = item })
    }

    fun addItem() = editorState.update { it.copy(items = it.items + InvoiceLineItem("", 0.0)) }

    fun removeItem(index: Int) = editorState.update { state ->
        val items = state.items.toMutableList()
        if (items.size > 1) items.removeAt(index)
        state.copy(items = items)
    }

    suspend fun saveAndGeneratePdf(): File {
        val state = uiState.value
        val file = InvoicePdfGenerator.generate(
            context = context,
            invoiceNumber = state.number,
            issuerName = state.issuerName,
            clientName = state.selectedClientName,
            items = state.items,
            totalHT = state.totalHT,
            createdAt = state.createdAt,
        )
        invoiceRepository.save(
            Invoice(
                id = state.invoiceId,
                number = state.number,
                clientId = state.selectedClientId,
                issuerName = state.issuerName,
                items = state.items.toJson(),
                totalHT = state.totalHT,
                createdAt = state.createdAt,
                pdfPath = file.absolutePath,
            ),
        )
        return file
    }
}
