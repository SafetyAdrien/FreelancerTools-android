package com.freelancertools.app.ui.tools.fontlibrary

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold

@Composable
fun FontLibraryScreen(navigation: ScaffoldNavigation) {
    var previewText by rememberSaveable { mutableStateOf("Le studio créatif") }
    var textSize by remember { mutableFloatStateOf(28f) }
    var filter by rememberSaveable { mutableStateOf(FontKind.ALL) }
    var query by rememberSaveable { mutableStateOf("") }
    val clipboard = LocalContext.current.getSystemService(ClipboardManager::class.java)

    ToolScaffold(
        title = "Bibliothèque de Polices",
        icon = Icons.Rounded.TextFields,
        navigation = navigation,
        onReset = { previewText = "Le studio créatif"; textSize = 28f; filter = FontKind.ALL; query = "" },
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Rechercher une police") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = previewText,
            onValueChange = { previewText = it },
            label = { Text("Texte d'aperçu") },
            modifier = Modifier.fillMaxWidth(),
        )

        Text("Taille : ${textSize.toInt()}sp", style = MaterialTheme.typography.titleMedium)
        Slider(value = textSize, onValueChange = { textSize = it }, valueRange = 12f..48f)

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(FontKind.entries) { kind ->
                FilterChip(selected = filter == kind, onClick = { filter = kind }, label = { Text(kind.label) })
            }
        }

        val visibleFonts = FONT_CATALOG
            .filter { filter == FontKind.ALL || it.category == filter }
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }

        if (visibleFonts.isEmpty()) {
            Text(
                "Aucune police trouvée pour “$query”.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                visibleFonts.forEach { font ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.large) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(font.name, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                IconButton(onClick = {
                                    clipboard?.setPrimaryClip(ClipData.newPlainText("Police", font.name))
                                }) {
                                    Icon(Icons.Rounded.ContentCopy, contentDescription = "Copier le nom de la police")
                                }
                            }
                            Text(
                                previewText.ifBlank { "Aperçu" },
                                fontFamily = font.fontFamily,
                                fontSize = textSize.sp,
                            )
                        }
                    }
                }
            }
        }
    }
}
