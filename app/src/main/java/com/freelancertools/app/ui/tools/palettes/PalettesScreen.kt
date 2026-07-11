package com.freelancertools.app.ui.tools.palettes

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold

private val PRESET_COLORS = listOf(
    0xFFFF3B5C, 0xFF3B82F6, 0xFF2ECC71, 0xFFF39C12, 0xFF9B59B6,
    0xFFE74C3C, 0xFF1ABC9C, 0xFFF1C40F, 0xFF34495E, 0xFF00BCD4,
).map { Color(it) }

@Composable
fun PalettesScreen(navigation: ScaffoldNavigation, viewModel: PalettesViewModel = hiltViewModel()) {
    var name by rememberSaveable { mutableStateOf("Nouvelle Palette") }
    var primaryIndex by rememberSaveable { mutableIntStateOf(0) }
    var secondaryIndex by rememberSaveable { mutableIntStateOf(1) }
    var accentIndex by rememberSaveable { mutableIntStateOf(2) }
    val saved by viewModel.savedPalettes.collectAsStateWithLifecycle()
    val clipboard = LocalContext.current.getSystemService(ClipboardManager::class.java)

    val colors = listOf(PRESET_COLORS[primaryIndex], PRESET_COLORS[secondaryIndex], PRESET_COLORS[accentIndex])

    ToolScaffold(
        title = "Palettes",
        icon = Icons.Rounded.Palette,
        navigation = navigation,
        bottomBar = {
            PrimaryActionButton(
                label = "Enregistrer",
                icon = Icons.Rounded.Save,
                onClick = { viewModel.savePalette(name, colors.map { it.toHex() }) },
            )
        },
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nom") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ColorSlotRow("Primaire", colors[0]) { primaryIndex = (primaryIndex + 1) % PRESET_COLORS.size }
                ColorSlotRow("Secondaire", colors[1]) { secondaryIndex = (secondaryIndex + 1) % PRESET_COLORS.size }
                ColorSlotRow("Accent", colors[2]) { accentIndex = (accentIndex + 1) % PRESET_COLORS.size }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp)),
        ) {
            colors.forEach { c ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(c),
                )
            }
        }

        if (saved.isNotEmpty()) {
            Text("Palettes enregistrées", style = MaterialTheme.typography.titleLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(saved, key = { it.id }) { palette ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.width(180.dp),
                    ) {
                        Column {
                            Row(Modifier.height(70.dp)) {
                                palette.colorList().forEach { hex ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxWidth()
                                            .background(parseHexOrGray(hex)),
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(palette.name, style = MaterialTheme.typography.labelLarge, maxLines = 1)
                                IconButton(onClick = {
                                    val text = palette.colorList().joinToString(", ")
                                    clipboard?.setPrimaryClip(ClipData.newPlainText("Palette", text))
                                }) {
                                    Icon(Icons.Rounded.ContentCopy, contentDescription = "Copier")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorSlotRow(label: String, color: Color, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Box(
            modifier = Modifier
                .size(width = 64.dp, height = 32.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color)
                .clickable(onClick = onClick),
        )
    }
}

private fun Color.toHex(): String {
    val r = (red * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue * 255).toInt()
    return String.format("#%02X%02X%02X", r, g, b)
}

private fun parseHexOrGray(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrDefault(Color.Gray)
