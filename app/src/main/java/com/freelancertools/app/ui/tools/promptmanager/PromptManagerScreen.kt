package com.freelancertools.app.ui.tools.promptmanager

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freelancertools.app.data.local.db.entity.Prompt
import com.freelancertools.app.ui.common.EmptyState
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptManagerScreen(navigation: ScaffoldNavigation, viewModel: PromptManagerViewModel = hiltViewModel()) {
    val prompts by viewModel.prompts.collectAsStateWithLifecycle()
    var editing by rememberSaveable { mutableStateOf<String?>(null) }
    var showSheet by rememberSaveable { mutableStateOf(false) }

    ToolScaffold(
        title = "Prompt Manager",
        icon = Icons.Rounded.Diamond,
        navigation = navigation,
        bottomBar = {
            PrimaryActionButton(
                label = "Nouveau Prompt",
                icon = Icons.Rounded.Add,
                onClick = { editing = null; showSheet = true },
            )
        },
    ) {
        if (prompts.isEmpty()) {
            EmptyState(
                title = "Aucun prompt",
                subtitle = "Enregistrez vos prompts favoris pour les retrouver rapidement.",
                icon = Icons.Rounded.Diamond,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                prompts.forEach { prompt ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = MaterialTheme.shapes.large,
                        onClick = { editing = prompt.id; showSheet = true },
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(Modifier.weight(1f)) {
                                    TagPill(prompt.tag)
                                    Text(
                                        prompt.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(top = 6.dp),
                                    )
                                }
                                IconButton(onClick = { viewModel.delete(prompt) }) {
                                    Icon(Icons.Rounded.Delete, contentDescription = "Supprimer")
                                }
                            }
                            Text(
                                prompt.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                            )
                        }
                    }
                }
            }
        }
    }

    if (showSheet) {
        val current = prompts.firstOrNull { it.id == editing }
        PromptEditSheet(
            initial = current,
            onDismiss = { showSheet = false },
            onSave = { title, tag, content ->
                viewModel.save(editing, title, tag, content)
                showSheet = false
            },
        )
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
private fun PromptEditSheet(
    initial: Prompt?,
    onDismiss: () -> Unit,
    onSave: (title: String, tag: String, content: String) -> Unit,
) {
    var title by rememberSaveable { mutableStateOf(initial?.title ?: "") }
    var tag by rememberSaveable { mutableStateOf(initial?.tag ?: "") }
    var content by rememberSaveable { mutableStateOf(initial?.content ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(if (initial == null) "Nouveau Prompt" else "Modifier le Prompt", style = MaterialTheme.typography.titleLarge)
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
                label = { Text("Tag") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Contenu") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                minLines = 5,
            )
            PrimaryActionButton(
                label = "Enregistrer",
                icon = Icons.Rounded.Add,
                enabled = title.isNotBlank(),
                onClick = { onSave(title, tag, content) },
            )
        }
    }
}
