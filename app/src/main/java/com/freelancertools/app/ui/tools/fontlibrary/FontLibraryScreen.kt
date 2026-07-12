package com.freelancertools.app.ui.tools.fontlibrary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold

private enum class FontKind(val label: String) { ALL("Toutes"), MONO("Monospace"), SERIF("Serif"), SANS("Sans-Serif") }

private data class SystemFont(val name: String, val family: FontFamily, val kind: FontKind)

private val FONTS = listOf(
    SystemFont("Sans-serif (Défaut)", FontFamily.SansSerif, FontKind.SANS),
    SystemFont("Serif", FontFamily.Serif, FontKind.SERIF),
    SystemFont("Monospace", FontFamily.Monospace, FontKind.MONO),
    SystemFont("Cursive", FontFamily.Cursive, FontKind.SANS),
)

@Composable
fun FontLibraryScreen(navigation: ScaffoldNavigation) {
    var previewText by rememberSaveable { mutableStateOf("Le studio créatif") }
    var textSize by remember { mutableFloatStateOf(28f) }
    var filter by rememberSaveable { mutableStateOf(FontKind.ALL) }

    ToolScaffold(
        title = "Bibliothèque de Polices",
        icon = Icons.Rounded.TextFields,
        navigation = navigation,
        onReset = { previewText = "Le studio créatif"; textSize = 28f; filter = FontKind.ALL },
    ) {
        OutlinedTextField(
            value = previewText,
            onValueChange = { previewText = it },
            label = { Text("Texte d'aperçu") },
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Taille : ${textSize.toInt()}sp", style = MaterialTheme.typography.titleMedium)
        Slider(value = textSize, onValueChange = { textSize = it }, valueRange = 14f..48f)

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(FontKind.entries) { kind ->
                FilterChip(selected = filter == kind, onClick = { filter = kind }, label = { Text(kind.label) })
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            FONTS.filter { filter == FontKind.ALL || it.kind == filter }.forEach { font ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.large) {
                    Column(Modifier.padding(16.dp)) {
                        Text(font.name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            previewText.ifBlank { "Aperçu" },
                            fontFamily = font.family,
                            fontSize = textSize.sp,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }
    }
}
