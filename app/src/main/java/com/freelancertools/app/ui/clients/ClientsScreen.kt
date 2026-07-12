package com.freelancertools.app.ui.clients

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freelancertools.app.data.local.db.entity.Client
import com.freelancertools.app.ui.common.EmptyState
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientsScreen(
    navigation: ScaffoldNavigation,
    onOpenClient: (String) -> Unit,
    viewModel: ClientsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var showSortSheet by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    ToolScaffold(
        title = "Clients",
        icon = Icons.Rounded.People,
        navigation = navigation,
        bottomBar = {
            if (!state.selectionMode) {
                PrimaryActionButton(
                    label = "Nouveau Client",
                    icon = Icons.Rounded.PersonAdd,
                    onClick = { showAddSheet = true },
                )
            }
        },
    ) {
        if (state.selectionMode) {
            SelectionActionBar(
                selectedCount = state.selectedIds.size,
                totalCount = state.clients.size,
                onSelectAll = viewModel::selectAll,
                onDeselectAll = viewModel::deselectAll,
                onDelete = { confirmDelete = true },
                onClose = viewModel::exitSelectionMode,
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = viewModel::setQuery,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Rechercher un client...") },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                )
                IconButton(onClick = { showSortSheet = true }) {
                    Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = "Trier")
                }
            }
        }

        if (state.clients.isEmpty()) {
            EmptyState(
                title = "Aucun client",
                subtitle = "Ajoutez votre premier client pour commencer à suivre vos projets.",
                icon = Icons.Rounded.People,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.clients.forEach { client ->
                    ClientRow(
                        client = client,
                        selectionMode = state.selectionMode,
                        selected = client.id in state.selectedIds,
                        onClick = {
                            if (state.selectionMode) viewModel.toggleSelection(client.id) else onOpenClient(client.id)
                        },
                        onLongClick = { viewModel.enterSelectionMode(client.id) },
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        AddClientSheet(
            onDismiss = { showAddSheet = false },
            onSave = { name, company, email, phone ->
                viewModel.addClient(name, company, email, phone)
                showAddSheet = false
            },
        )
    }

    if (showSortSheet) {
        SortSheet(
            currentSort = state.sort,
            onDismiss = { showSortSheet = false },
            onSelect = {
                viewModel.setSort(it)
                showSortSheet = false
            },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Supprimer ${state.selectedIds.size} client(s) ?") },
            text = { Text("Leurs projets associés seront également supprimés. Cette action est irréversible.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSelected()
                    confirmDelete = false
                }) { Text("Supprimer", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Annuler") }
            },
        )
    }
}

@Composable
private fun SelectionActionBar(
    selectedCount: Int,
    totalCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit,
    onDelete: () -> Unit,
    onClose: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.Close, contentDescription = "Fermer la sélection")
                }
                Text("$selectedCount sélectionné(s)", style = MaterialTheme.typography.titleMedium)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = if (selectedCount < totalCount) onSelectAll else onDeselectAll) {
                    Icon(Icons.Rounded.DoneAll, contentDescription = "Tout sélectionner / désélectionner")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Supprimer la sélection", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ClientRow(
    client: Client,
    selectionMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        ),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selectionMode) {
                Checkbox(checked = selected, onCheckedChange = { onClick() })
            }
            ClientAvatar(client.name)
            Column(Modifier.padding(start = 12.dp)) {
                Text(client.name, style = MaterialTheme.typography.titleMedium)
                if (!client.company.isNullOrBlank()) {
                    Text(
                        client.company,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
fun ClientAvatar(name: String, size: androidx.compose.ui.unit.Dp = 40.dp) {
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val color = avatarColorFor(name)
    Box(
        modifier = Modifier
            .size(size)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(initial, color = androidx.compose.ui.graphics.Color.White, style = MaterialTheme.typography.titleMedium)
    }
}

private val avatarPalette = listOf(
    androidx.compose.ui.graphics.Color(0xFFFF3B5C),
    androidx.compose.ui.graphics.Color(0xFF3B82F6),
    androidx.compose.ui.graphics.Color(0xFF2ECC71),
    androidx.compose.ui.graphics.Color(0xFFF39C12),
    androidx.compose.ui.graphics.Color(0xFF9B59B6),
)

private fun avatarColorFor(name: String): androidx.compose.ui.graphics.Color {
    val index = (name.hashCode().let { if (it < 0) -it else it }) % avatarPalette.size
    return avatarPalette[index]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortSheet(
    currentSort: ClientSort,
    onDismiss: () -> Unit,
    onSelect: (ClientSort) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(bottom = 20.dp)) {
            Text(
                "Trier par",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )
            ClientSort.entries.forEach { sort ->
                DropdownMenuItem(
                    text = {
                        Text(
                            sort.label,
                            color = if (sort == currentSort) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    },
                    onClick = { onSelect(sort) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddClientSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, company: String?, email: String?, phone: String?) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var company by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Nouveau Client", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nom *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = company,
                onValueChange = { company = it },
                label = { Text("Société") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Téléphone") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            PrimaryActionButton(
                label = "Enregistrer",
                icon = Icons.Rounded.Add,
                enabled = name.isNotBlank(),
                onClick = { onSave(name, company, email, phone) },
            )
        }
    }
}
