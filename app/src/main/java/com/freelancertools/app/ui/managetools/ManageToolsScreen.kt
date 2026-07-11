package com.freelancertools.app.ui.managetools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold
import com.freelancertools.app.ui.navigation.ToolCategory
import com.freelancertools.app.ui.navigation.ToolItem

@Composable
fun ManageToolsScreen(onBack: () -> Unit, viewModel: ManageToolsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ToolScaffold(
        title = "Gérer les outils",
        icon = Icons.Rounded.Tune,
        navigation = ScaffoldNavigation.Back(onBack),
    ) {
        Text(
            "Réordonnez les catégories du tiroir et masquez les outils que vous n'utilisez pas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.categories.forEachIndexed { index, category ->
                CategoryManagementCard(
                    category = category,
                    hiddenTools = state.hiddenTools,
                    canMoveUp = index > 0,
                    canMoveDown = index < state.categories.lastIndex,
                    onMoveUp = { viewModel.moveCategory(index, -1) },
                    onMoveDown = { viewModel.moveCategory(index, 1) },
                    onToggleTool = viewModel::toggleToolHidden,
                )
            }
        }
    }
}

@Composable
private fun CategoryManagementCard(
    category: ToolCategory,
    hiddenTools: Set<String>,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onToggleTool: (String, Boolean) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(category.title, style = MaterialTheme.typography.titleMedium)
                Row {
                    IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                        Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = "Monter")
                    }
                    IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                        Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Descendre")
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            category.tools.forEach { tool ->
                ToolVisibilityRow(tool = tool, hidden = tool.id in hiddenTools, onToggle = onToggleTool)
            }
        }
    }
}

@Composable
private fun ToolVisibilityRow(tool: ToolItem, hidden: Boolean, onToggle: (String, Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                tool.icon,
                contentDescription = null,
                tint = if (hidden) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp),
            )
            Text(
                tool.title,
                style = MaterialTheme.typography.bodyLarge,
                color = if (hidden) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
        }
        Switch(checked = !hidden, onCheckedChange = { visible -> onToggle(tool.id, !visible) })
    }
}
