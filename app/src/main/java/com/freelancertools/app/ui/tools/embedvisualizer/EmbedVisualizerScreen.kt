package com.freelancertools.app.ui.tools.embedvisualizer

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold

@Composable
fun EmbedVisualizerScreen(navigation: ScaffoldNavigation) {
    var url by rememberSaveable { mutableStateOf("") }
    var loadedUrl by rememberSaveable { mutableStateOf<String?>(null) }

    ToolScaffold(
        title = "Embed Visualizer",
        icon = Icons.Rounded.Code,
        navigation = navigation,
        scrollable = false,
        bottomBar = {
            PrimaryActionButton(
                label = "Prévisualiser",
                enabled = url.isNotBlank(),
                onClick = { loadedUrl = normalizeUrl(url) },
            )
        },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL à intégrer") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            if (loadedUrl != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                ) {
                    EmbedWebView(loadedUrl!!, Modifier.fillMaxSize())
                }
            } else {
                Text(
                    "Entrez une URL pour afficher un aperçu embarqué de la page.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun EmbedWebView(url: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
            }
        },
        update = { webView -> webView.loadUrl(url) },
    )
}

private fun normalizeUrl(input: String): String {
    val trimmed = input.trim()
    return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) trimmed else "https://$trimmed"
}
