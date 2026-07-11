package com.freelancertools.app.ui.clients

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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

    ToolScaffold(
        title = "Clients",
        icon = Icons.Rounded.People,
        navigation = navigation,
        bottomBar = {
            PrimaryActionButton(
                label = "Nouveau Client",
                icon = Icons.Rounded.PersonAdd,
                onClick = { showAddSheet = true },
            )
        },
    ) {
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::setQuery,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Rechercher un client...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
        )

        if (state.clients.isEmpty()) {
            EmptyState(
                title = "Aucun client",
                subtitle = "Ajoutez votre premier client pour commencer à suivre vos projets.",
                icon = Icons.Rounded.People,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                state.clients.forEach { client ->
                    ClientRow(client = client, onClick = { onOpenClient(client.id) })
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
}

@Composable
private fun ClientRow(client: Client, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
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
