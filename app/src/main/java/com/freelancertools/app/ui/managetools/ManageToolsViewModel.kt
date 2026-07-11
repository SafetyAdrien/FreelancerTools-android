package com.freelancertools.app.ui.managetools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelancertools.app.data.local.datastore.PreferencesManager
import com.freelancertools.app.ui.navigation.ToolCategory
import com.freelancertools.app.ui.navigation.orderedCategories
import com.freelancertools.app.ui.navigation.toolCategories
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageToolsUiState(
    val categories: List<ToolCategory> = toolCategories,
    val hiddenTools: Set<String> = emptySet(),
)

@HiltViewModel
class ManageToolsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    val uiState: StateFlow<ManageToolsUiState> = combine(
        preferencesManager.categoryOrder,
        preferencesManager.hiddenTools,
    ) { order, hidden ->
        ManageToolsUiState(categories = orderedCategories(order), hiddenTools = hidden)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ManageToolsUiState())

    fun toggleToolHidden(toolId: String, hidden: Boolean) {
        viewModelScope.launch { preferencesManager.setToolHidden(toolId, hidden) }
    }

    fun moveCategory(index: Int, delta: Int) {
        viewModelScope.launch {
            val current = uiState.value.categories.map { it.id }.toMutableList()
            val target = index + delta
            if (index !in current.indices || target !in current.indices) return@launch
            val item = current.removeAt(index)
            current.add(target, item)
            preferencesManager.setCategoryOrder(current)
        }
    }
}
