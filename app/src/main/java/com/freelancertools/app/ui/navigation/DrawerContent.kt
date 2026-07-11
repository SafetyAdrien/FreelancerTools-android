package com.freelancertools.app.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DrawerContent(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DrawerViewModel = hiltViewModel(),
) {
    var query by rememberSaveable { mutableStateOf("") }
    val collapsedSections by viewModel.collapsedSections.collectAsStateWithLifecycle()

    ModalDrawerSheet(modifier = modifier) {
        Column(Modifier.fillMaxWidth()) {
            Text(
                text = "Freelancer Tools",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(20.dp, 20.dp, 20.dp, 12.dp),
            )

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Chercher un outil...") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Effacer")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(),
            )

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                contentPadding = PaddingValues(bottom = 8.dp),
            ) {
                if (query.isBlank()) {
                    item { SectionLabel("Général") }
                    items(generalItems, key = { it.id }) { item ->
                        DrawerRow(item, currentRoute == item.route, onNavigate)
                    }

                    item { Spacer(Modifier.height(8.dp)) }
                    item { SectionLabel("Outils") }

                    toolCategories.forEach { category ->
                        val collapsed = category.id in collapsedSections
                        item(key = "header_${category.id}") {
                            CategoryHeader(
                                title = category.title,
                                collapsed = collapsed,
                                onClick = { viewModel.toggleSection(category.id, collapsed) },
                            )
                        }
                        if (!collapsed) {
                            items(category.tools, key = { it.id }) { item ->
                                DrawerRow(item, currentRoute == item.route, onNavigate)
                            }
                        }
                    }
                } else {
                    val matches = allTools.filter { it.title.contains(query, ignoreCase = true) } +
                        generalItems.filter { it.title.contains(query, ignoreCase = true) }
                    if (matches.isEmpty()) {
                        item {
                            Text(
                                text = "Aucun outil trouvé pour “$query”",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(20.dp),
                            )
                        }
                    } else {
                        items(matches, key = { it.id }) { item ->
                            DrawerRow(item, currentRoute == item.route, onNavigate)
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            DrawerRow(settingsItem, currentRoute == settingsItem.route, onNavigate)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 20.dp, top = 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun CategoryHeader(title: String, collapsed: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .then(Modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = if (collapsed) Icons.Rounded.ChevronRight else Icons.Rounded.ExpandMore,
                    contentDescription = if (collapsed) "Développer" else "Réduire",
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DrawerRow(item: ToolItem, selected: Boolean, onNavigate: (String) -> Unit) {
    NavigationDrawerItem(
        label = { Text(item.title) },
        selected = selected,
        onClick = { onNavigate(item.route) },
        icon = { Icon(item.icon, contentDescription = null) },
        shape = RoundedCornerShape(10.dp),
        colors = NavigationDrawerItemDefaults.colors(
            selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unselectedContainerColor = MaterialTheme.colorScheme.background,
        ),
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
    )
}
