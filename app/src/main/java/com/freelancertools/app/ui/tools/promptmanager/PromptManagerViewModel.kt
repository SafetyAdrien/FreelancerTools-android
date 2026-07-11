package com.freelancertools.app.ui.tools.promptmanager

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelancertools.app.data.local.db.entity.Prompt
import com.freelancertools.app.data.repository.PromptRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PromptManagerViewModel @Inject constructor(
    private val promptRepository: PromptRepository,
) : ViewModel() {

    val prompts: StateFlow<List<Prompt>> = promptRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(id: String?, title: String, tag: String, content: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            promptRepository.save(
                Prompt(
                    id = id ?: UUID.randomUUID().toString(),
                    title = title.trim(),
                    tag = tag.trim().ifBlank { "Général" },
                    content = content,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
        }
    }

    fun delete(prompt: Prompt) {
        viewModelScope.launch { promptRepository.delete(prompt) }
    }
}
