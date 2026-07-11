package com.freelancertools.app.ui.clients

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelancertools.app.data.local.db.entity.Client
import com.freelancertools.app.data.local.db.entity.Project
import com.freelancertools.app.data.local.db.entity.ProjectStatus
import com.freelancertools.app.data.repository.ClientRepository
import com.freelancertools.app.data.repository.ProjectRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ClientDetailUiState(
    val client: Client? = null,
    val activeProjects: List<Project> = emptyList(),
    val completedProjects: List<Project> = emptyList(),
)

@HiltViewModel
class ClientDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val clientRepository: ClientRepository,
    private val projectRepository: ProjectRepository,
) : ViewModel() {

    private val clientId: String = checkNotNull(savedStateHandle["clientId"])

    val uiState: StateFlow<ClientDetailUiState> = combine(
        clientRepository.observeById(clientId),
        projectRepository.observeForClient(clientId),
    ) { client, projects ->
        ClientDetailUiState(
            client = client,
            activeProjects = projects.filter { it.status != ProjectStatus.DONE },
            completedProjects = projects.filter { it.status == ProjectStatus.DONE },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ClientDetailUiState())

    fun addProject(title: String, tag: String, price: Double, description: String?) {
        if (title.isBlank()) return
        viewModelScope.launch {
            projectRepository.save(
                Project(
                    id = UUID.randomUUID().toString(),
                    clientId = clientId,
                    title = title.trim(),
                    description = description?.trim()?.ifBlank { null },
                    tag = tag.ifBlank { "Général" },
                    price = price,
                    status = ProjectStatus.ACTIVE,
                    completedAt = null,
                ),
            )
        }
    }

    fun markCompleted(project: Project) {
        viewModelScope.launch {
            projectRepository.save(project.copy(status = ProjectStatus.DONE, completedAt = System.currentTimeMillis()))
        }
    }
}
