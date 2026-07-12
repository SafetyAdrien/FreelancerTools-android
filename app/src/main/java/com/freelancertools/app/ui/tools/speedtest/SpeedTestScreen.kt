package com.freelancertools.app.ui.tools.speedtest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.NetworkCheck
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.StatCard
import com.freelancertools.app.ui.common.ToolScaffold
import com.freelancertools.app.ui.theme.InfoBlue
import com.freelancertools.app.ui.theme.SuccessGreen
import com.freelancertools.app.ui.theme.WarningOrange
import kotlinx.coroutines.launch

@Composable
fun SpeedTestScreen(navigation: ScaffoldNavigation) {
    var isRunning by rememberSaveable { mutableStateOf(false) }
    var progressText by rememberSaveable { mutableStateOf("") }
    var result by remember { mutableStateOf<SpeedTestResult?>(null) }
    val scope = rememberCoroutineScope()

    ToolScaffold(
        title = "Speed Test",
        icon = Icons.Rounded.Speed,
        navigation = navigation,
        onReset = { isRunning = false; progressText = ""; result = null },
        bottomBar = {
            PrimaryActionButton(
                label = if (isRunning) "Test en cours..." else "Démarrer",
                enabled = !isRunning,
                onClick = {
                    isRunning = true
                    result = null
                    scope.launch {
                        result = runCatching { SpeedTestClient.run { progressText = it } }.getOrNull()
                        isRunning = false
                    }
                },
            )
        },
    ) {
        if (isRunning) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                CircularProgressIndicator(modifier = Modifier.padding(24.dp))
                Text(progressText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        result?.let { r ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    label = "Ping",
                    value = "${r.pingMs} ms",
                    icon = Icons.Rounded.NetworkCheck,
                    accentColor = InfoBlue,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    label = "Téléchargement",
                    value = "%.1f Mbps".format(r.downloadMbps),
                    icon = Icons.Rounded.ArrowDownward,
                    accentColor = SuccessGreen,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = "Envoi",
                    value = "%.1f Mbps".format(r.uploadMbps),
                    icon = Icons.Rounded.ArrowUpward,
                    accentColor = WarningOrange,
                    modifier = Modifier.weight(1f),
                )
            }
        } ?: run {
            if (!isRunning) {
                Text(
                    "Mesurez votre débit descendant, montant et le ping via les points de test publics Cloudflare.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
