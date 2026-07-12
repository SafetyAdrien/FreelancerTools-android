package com.freelancertools.app.ui.clients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freelancertools.app.data.local.db.entity.Project
import com.freelancertools.app.ui.common.EmptyState
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.SectionTitle
import com.freelancertools.app.ui.common.ToolScaffold
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDetailScreen(
    onBack: () -> Unit,
    viewModel: ClientDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddProject by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val client = state.client

    LaunchedEffect(Unit) {
        viewModel.clientDeleted.collect { onBack() }
    }

    ToolScaffold(
        title = client?.name ?: "Client",
        icon = Icons.Rounded.Person,
        navigation = ScaffoldNavigation.Back(onBack),
        actions = {
            if (client != null) {
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Supprimer le client",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        bottomBar = {
            PrimaryActionButton(
                label = "Nouveau Projet",
                icon = Icons.Rounded.Add,
                onClick = { showAddProject = true },
            )
        },
    ) {
        if (client != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ClientAvatar(client.name, size = 56.dp)
                Column(Modifier.padding(start = 14.dp)) {
                    Text(client.name, style = MaterialTheme.typography.titleLarge)
                    if (!client.company.isNullOrBlank()) {
                        Text(
                            client.company,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (!client.email.isNullOrBlank()) {
                        Text(
                            client.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            SectionTitle("Projets en cours")
            if (state.activeProjects.isEmpty()) {
                Text(
                    "Aucun projet en cours.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.activeProjects.forEach { project ->
                        ProjectRow(project, onComplete = { viewModel.markCompleted(project) })
                    }
                }
            }

            SectionTitle("Historique & Terminés")
            if (state.completedProjects.isEmpty()) {
                Text(
                    "Aucun projet terminé pour le moment.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.completedProjects.forEach { project -> ProjectRow(project, onComplete = null) }
                }
            }
        } else {
            EmptyState(title = "Client introuvable", subtitle = "", icon = Icons.Rounded.Person)
        }
    }

    if (showAddProject) {
        AddProjectSheet(
            onDismiss = { showAddProject = false },
            onSave = { title, tag, price, description ->
                viewModel.addProject(title, tag, price, description)
                showAddProject = false
            },
        )
    }

    if (confirmDelete && client != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Supprimer ${client.name} ?") },
            text = { Text("Tous ses projets associés seront également supprimés. Cette action est irréversible.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteClient()
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
private fun ProjectRow(project: Project, onComplete: (() -> Unit)?) {
    val currencyFormat = remember(project.id) { NumberFormat.getCurrencyInstance(Locale.FRANCE) }
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                TagPill(project.tag)
                Text(
                    project.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 6.dp),
                )
                if (!project.description.isNullOrBlank()) {
                    Text(
                        project.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                project.completedAt?.let {
                    Text(
                        SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(Date(it)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(currencyFormat.format(project.price), style = MaterialTheme.typography.titleMedium)
                if (onComplete != null) {
                    IconButton(onClick = onComplete) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = "Marquer terminé")
                    }
                }
            }
        }
    }
}

@Composable
private fun TagPill(tag: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
        shape = RoundedCornerShape(8.dp),
    ) {
        Text(
            tag,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddProjectSheet(
    onDismiss: () -> Unit,
    onSave: (title: String, tag: String, price: Double, description: String?) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf("") }
    var tag by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Nouveau Projet", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Titre *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = tag,
                onValueChange = { tag = it },
                label = { Text("Tag (ex: Brand Design)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = price,
                onValueChange = { price = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Prix (€)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
            )
            PrimaryActionButton(
                label = "Enregistrer",
                icon = Icons.Rounded.Add,
                enabled = title.isNotBlank(),
                onClick = { onSave(title, tag, price.toDoubleOrNull() ?: 0.0, description) },
            )
        }
    }
}
