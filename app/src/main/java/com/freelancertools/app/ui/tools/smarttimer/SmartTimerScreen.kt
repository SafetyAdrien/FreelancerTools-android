package com.freelancertools.app.ui.tools.smarttimer

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.menuAnchor
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
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.SectionTitle
import com.freelancertools.app.ui.common.ToolScaffold
import com.freelancertools.app.ui.theme.InfoBlue
import com.freelancertools.app.ui.theme.SuccessGreen
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartTimerScreen(navigation: ScaffoldNavigation, viewModel: SmartTimerViewModel = hiltViewModel()) {
    val state by viewModel.timerState.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    var taskLabel by rememberSaveable { mutableStateOf("Travail") }
    var hourlyRate by rememberSaveable { mutableStateOf("40") }
    var modeExpanded by rememberSaveable { mutableStateOf(false) }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE)

    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    ToolScaffold(title = "Smart Timer", icon = Icons.Rounded.Timer, navigation = navigation) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.large) {
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(expanded = modeExpanded, onExpandedChange = { modeExpanded = it }, modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = state.mode.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mode") },
                        enabled = !state.isRunning,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                    )
                    ExposedDropdownMenu(expanded = modeExpanded, onDismissRequest = { modeExpanded = false }) {
                        TimerMode.entries.forEach { m ->
                            DropdownMenuItem(text = { Text(m.label) }, onClick = { TimerEngine.configure(m, hourlyRate.toDoubleOrNull() ?: 0.0, taskLabel); modeExpanded = false })
                        }
                    }
                }
                OutlinedTextField(
                    value = hourlyRate,
                    onValueChange = { hourlyRate = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("€ / h") },
                    enabled = !state.isRunning,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        OutlinedTextField(
            value = taskLabel,
            onValueChange = { taskLabel = it },
            label = { Text("Tâche") },
            enabled = !state.isRunning,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center,
        ) {
            val progress = when {
                state.mode.durationSeconds != null -> state.elapsedSeconds.toFloat() / state.mode.durationSeconds
                else -> 0f
            }
            CircularProgressIndicator(
                progress = { if (state.mode.durationSeconds != null) progress.coerceIn(0f, 1f) else 1f },
                modifier = Modifier.size(220.dp),
                strokeWidth = 10.dp,
                color = InfoBlue,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val displaySeconds = state.remainingSeconds ?: state.elapsedSeconds
                Text(
                    text = "%02d:%02d".format(displaySeconds / 60, displaySeconds % 60),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = if (state.isRunning) "En cours" else "En pause",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Gains de la session", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(currencyFormat.format(state.earned), style = MaterialTheme.typography.headlineMedium, color = SuccessGreen, fontWeight = FontWeight.Bold)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                IconButton(
                    onClick = {
                        if (state.isRunning) {
                            viewModel.pause()
                        } else {
                            if (Build.VERSION.SDK_INT >= 33) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            viewModel.start(state.mode, hourlyRate.toDoubleOrNull() ?: 0.0, taskLabel)
                        }
                    },
                    colors = IconButtonDefaults.iconButtonColors(contentColor = if (state.isRunning) MaterialTheme.colorScheme.error else SuccessGreen),
                ) {
                    Icon(if (state.isRunning) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(36.dp))
                }
                IconButton(onClick = { viewModel.reset() }) {
                    Icon(Icons.Rounded.RestartAlt, contentDescription = "Réinitialiser", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                }
            }
        }

        if (history.isNotEmpty()) {
            SectionTitle("Historique des sessions")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                history.take(10).forEach { session ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.medium) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column {
                                Text(
                                    "%02d:%02d".format(session.durationSeconds / 60, session.durationSeconds % 60),
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    SimpleDateFormat("dd/MM HH:mm", Locale.FRANCE).format(Date(session.startedAt)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(currencyFormat.format(session.earned), style = MaterialTheme.typography.bodyMedium, color = SuccessGreen)
                        }
                    }
                }
            }
        }
    }
}
