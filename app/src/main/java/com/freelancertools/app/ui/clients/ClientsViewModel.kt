package com.freelancertools.app.ui.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelancertools.app.data.local.datastore.PreferencesManager
import com.freelancertools.app.data.local.db.entity.Client
import com.freelancertools.app.data.repository.ClientRepository
import com.freelancertools.app.data.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

enum class ClientSort(val label: String) {
    NAME_ASC("Nom (A→Z)"),
    NAME_DESC("Nom (Z→A)"),
    DATE_ADDED_DESC("Date d'ajout (récent d'abord)"),
    DATE_ADDED_ASC("Date d'ajout (ancien d'abord)"),
}

data class ClientsUiState(
    val clients: List<Client> = emptyList(),
    val query: String = "",
    val sort: ClientSort = ClientSort.NAME_ASC,
    val selectionMode: Boolean = false,
    val selectedIds: Set<String> = emptySet(),
)

@HiltViewModel
class ClientsViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val projectRepository: ProjectRepository,
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val selectionMode = MutableStateFlow(false)
    private val selectedIds = MutableStateFlow<Set<String>>(emptySet())

    val uiState: StateFlow<ClientsUiState> = combine(
        clientRepository.observeAll(),
        query,
        preferencesManager.clientSort,
        selectionMode,
        selectedIds,
    ) { clients, q, sortName, inSelection, selected ->
        val sort = runCatching { ClientSort.valueOf(sortName) }.getOrDefault(ClientSort.NAME_ASC)
        val filtered = if (q.isBlank()) {
            clients
        } else {
            clients.filter {
                it.name.contains(q, ignoreCase = true) || it.company?.contains(q, ignoreCase = true) == true
            }
        }
        val sorted = when (sort) {
            ClientSort.NAME_ASC -> filtered.sortedBy { it.name.lowercase() }
            ClientSort.NAME_DESC -> filtered.sortedByDescending { it.name.lowercase() }
            ClientSort.DATE_ADDED_DESC -> filtered.sortedByDescending { it.createdAt }
            ClientSort.DATE_ADDED_ASC -> filtered.sortedBy { it.createdAt }
        }
        ClientsUiState(sorted, q, sort, inSelection, selected)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClientsUiState())

    fun setQuery(value: String) {
        query.value = value
    }

    fun setSort(sort: ClientSort) {
        viewModelScope.launch { preferencesManager.setClientSort(sort.name) }
    }

    fun addClient(name: String, company: String?, email: String?, phone: String?) {
        if (name.isBlank()) return
        viewModelScope.launch {
            clientRepository.save(
                Client(
                    id = UUID.randomUUID().toString(),
                    name = name.trim(),
                    company = company?.trim()?.ifBlank { null },
                    email = email?.trim()?.ifBlank { null },
                    phone = phone?.trim()?.ifBlank { null },
                    createdAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    fun enterSelectionMode(initialId: String) {
        selectionMode.value = true
        selectedIds.value = setOf(initialId)
    }

    fun exitSelectionMode() {
        selectionMode.value = false
        selectedIds.value = emptySet()
    }

    fun toggleSelection(id: String) {
        val current = selectedIds.value
        val next = if (id in current) current - id else current + id
        selectedIds.value = next
        if (next.isEmpty()) selectionMode.value = false
    }

    fun selectAll() {
        selectedIds.value = uiState.value.clients.map { it.id }.toSet()
    }

    fun deselectAll() {
        selectedIds.value = emptySet()
    }

    fun deleteSelected() {
        val ids = selectedIds.value
        if (ids.isEmpty()) return
        viewModelScope.launch {
            val clients = uiState.value.clients.filter { it.id in ids }
            clients.forEach { client ->
                projectRepository.deleteAllForClient(client.id)
                clientRepository.delete(client)
            }
            exitSelectionMode()
        }
    }
}
