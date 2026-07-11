package com.freelancertools.app.ui.tools.whois

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold
import kotlinx.coroutines.launch

@Composable
fun WhoisScreen(navigation: ScaffoldNavigation) {
    var domain by rememberSaveable { mutableStateOf("") }
    var result by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val clipboard = LocalContext.current.getSystemService(ClipboardManager::class.java)

    ToolScaffold(
        title = "Whois Lookup",
        icon = Icons.Rounded.Public,
        navigation = navigation,
        bottomBar = {
            PrimaryActionButton(
                label = "Rechercher",
                enabled = domain.isNotBlank() && !isLoading,
                onClick = {
                    isLoading = true
                    result = ""
                    scope.launch {
                        result = runCatching { WhoisClient.lookup(domain) }
                            .getOrElse { "Erreur lors de la requête : ${it.message}" }
                        isLoading = false
                    }
                },
            )
        },
    ) {
        OutlinedTextField(
            value = domain,
            onValueChange = { domain = it },
            label = { Text("Nom de domaine") },
            placeholder = { Text("example.com") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        if (isLoading) {
            CircularProgressIndicator()
        }

        if (result.isNotBlank()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.large) {
                Text(
                    text = result,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                )
            }
            PrimaryActionButton(
                label = "Copier",
                icon = Icons.Rounded.ContentCopy,
                onClick = { clipboard?.setPrimaryClip(ClipData.newPlainText("Whois", result)) },
            )
        }
    }
}
