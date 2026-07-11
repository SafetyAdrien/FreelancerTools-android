package com.freelancertools.app.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.SecondaryActionButton
import com.freelancertools.app.ui.common.SectionTitle
import com.freelancertools.app.ui.common.ToolScaffold
import com.freelancertools.app.ui.theme.AppThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    navigation: ScaffoldNavigation,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var backupStatus by remember { mutableStateOf("") }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri: Uri? ->
        if (uri != null) {
            viewModel.exportData { json ->
                context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
                backupStatus = "Export terminé."
            }
        }
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri != null) {
            val content = context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
            if (content != null) {
                viewModel.importData(content) { success ->
                    backupStatus = if (success) "Import réussi." else "Échec de l'import : fichier invalide."
                }
            }
        }
    }

    ToolScaffold(title = "Paramètres", icon = Icons.Rounded.Settings, navigation = navigation) {
        SectionTitle("Apparence")
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Thème", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Choisissez l'apparence de l'application.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp, top = 2.dp),
                )
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    val options = listOf(
                        AppThemeMode.DARK to "Sombre",
                        AppThemeMode.LIGHT to "Clair",
                        AppThemeMode.SYSTEM to "Système",
                    )
                    options.forEachIndexed { index, (mode, label) ->
                        SegmentedButton(
                            selected = state.themeMode == mode,
                            onClick = { viewModel.setThemeMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        ) {
                            Text(label)
                        }
                    }
                }
            }
        }

        SectionTitle("Facturation")
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Utilisées comme émetteur sur vos factures PDF.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = state.userName,
                    onValueChange = viewModel::setUserName,
                    label = { Text("Votre nom") },
                    leadingIcon = { androidx.compose.material3.Icon(Icons.Rounded.Person, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.companyName,
                    onValueChange = viewModel::setCompanyName,
                    label = { Text("Société") },
                    leadingIcon = { androidx.compose.material3.Icon(Icons.Rounded.Business, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        SectionTitle("Sauvegarde")
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Exportez toutes vos données (clients, projets, transactions, factures, palettes, prompts) dans un fichier JSON, ou restaurez-les depuis une sauvegarde.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PrimaryActionButton(
                    label = "Exporter les données",
                    icon = Icons.Rounded.Download,
                    onClick = {
                        val fileName = "freelancer-tools-backup-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.FRANCE).format(Date())}.json"
                        exportLauncher.launch(fileName)
                    },
                )
                SecondaryActionButton(
                    label = "Importer une sauvegarde",
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                )
                if (backupStatus.isNotBlank()) {
                    Text(backupStatus, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        SectionTitle("À propos")
        Row(Modifier.padding(vertical = 4.dp)) {
            Text(
                "Freelancer Tools · v1.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
