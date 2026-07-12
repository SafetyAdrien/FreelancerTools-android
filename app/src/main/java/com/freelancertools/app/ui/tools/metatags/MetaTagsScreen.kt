package com.freelancertools.app.ui.tools.metatags

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Sell
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold

@Composable
fun MetaTagsScreen(navigation: ScaffoldNavigation) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var image by rememberSaveable { mutableStateOf("") }
    var url by rememberSaveable { mutableStateOf("") }
    val clipboard = LocalContext.current.getSystemService(ClipboardManager::class.java)

    val generated = buildMetaTags(title, description, image, url)

    ToolScaffold(
        title = "Générateur Meta Tags",
        icon = Icons.Rounded.Sell,
        navigation = navigation,
        onReset = { title = ""; description = ""; image = ""; url = "" },
        bottomBar = {
            PrimaryActionButton(
                label = "Copier",
                icon = Icons.Rounded.ContentCopy,
                enabled = generated.isNotBlank(),
                onClick = { clipboard?.setPrimaryClip(ClipData.newPlainText("Meta Tags", generated)) },
            )
        },
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = image,
                    onValueChange = { image = it },
                    label = { Text("URL de l'image") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL de la page") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.large,
        ) {
            Text(
                text = generated,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

private fun buildMetaTags(title: String, description: String, image: String, url: String): String {
    fun esc(s: String) = s.replace("\"", "&quot;")
    return buildString {
        appendLine("<title>${esc(title)}</title>")
        appendLine("<meta name=\"description\" content=\"${esc(description)}\" />")
        appendLine()
        appendLine("<meta property=\"og:title\" content=\"${esc(title)}\" />")
        appendLine("<meta property=\"og:description\" content=\"${esc(description)}\" />")
        appendLine("<meta property=\"og:image\" content=\"${esc(image)}\" />")
        appendLine("<meta property=\"og:url\" content=\"${esc(url)}\" />")
        appendLine("<meta property=\"og:type\" content=\"website\" />")
        appendLine()
        appendLine("<meta name=\"twitter:card\" content=\"summary_large_image\" />")
        appendLine("<meta name=\"twitter:title\" content=\"${esc(title)}\" />")
        appendLine("<meta name=\"twitter:description\" content=\"${esc(description)}\" />")
        append("<meta name=\"twitter:image\" content=\"${esc(image)}\" />")
    }
}
