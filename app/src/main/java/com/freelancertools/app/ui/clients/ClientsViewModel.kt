package com.freelancertools.app.ui.clients

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelancertools.app.data.local.db.entity.Client
import com.freelancertools.app.data.repository.ClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ClientsUiState(
    val clients: List<Client> = emptyList(),
    val query: String = "",
)

@HiltViewModel
class ClientsViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
) : ViewModel() {

    private val query = MutableStateFlow("")

    val uiState: StateFlow<ClientsUiState> = combine(
        clientRepository.observeAll(),
        query,
    ) { clients, q ->
        val filtered = if (q.isBlank()) {
            clients
        } else {
            clients.filter {
                it.name.contains(q, ignoreCase = true) || it.company?.contains(q, ignoreCase = true) == true
            }
        }
        ClientsUiState(filtered, q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClientsUiState())

    fun setQuery(value: String) {
        query.value = value
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
}
