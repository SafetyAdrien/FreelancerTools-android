package com.freelancertools.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelancertools.app.data.local.datastore.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DrawerCatalogState(
    val categories: List<ToolCategory> = toolCategories,
    val hiddenTools: Set<String> = emptySet(),
)

@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    val collapsedSections: StateFlow<Set<String>> = preferencesManager.collapsedSections
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    val catalog: StateFlow<DrawerCatalogState> = combine(
        preferencesManager.categoryOrder,
        preferencesManager.hiddenTools,
    ) { order, hidden ->
        DrawerCatalogState(categories = orderedCategories(order), hiddenTools = hidden)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DrawerCatalogState())

    fun toggleSection(sectionId: String, currentlyCollapsed: Boolean) {
        viewModelScope.launch {
            preferencesManager.setSectionCollapsed(sectionId, !currentlyCollapsed)
        }
    }
}
