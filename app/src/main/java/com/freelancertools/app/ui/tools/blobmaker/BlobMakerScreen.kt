package com.freelancertools.app.ui.tools.blobmaker

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.FormatColorFill
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.SecondaryActionButton
import com.freelancertools.app.ui.common.ToolScaffold
import com.freelancertools.app.ui.common.colorpicker.ModernColorPickerDialog
import kotlin.random.Random

@Composable
fun BlobMakerScreen(navigation: ScaffoldNavigation) {
    var complexity by rememberSaveable { mutableIntStateOf(8) }
    var irregularity by rememberSaveable { mutableIntStateOf(30) }
    var sizeDp by rememberSaveable { mutableFloatStateOf(220f) }
    var colorHex by rememberSaveable { mutableStateOf("#FF3B5C") }
    var showColorPicker by rememberSaveable { mutableStateOf(false) }
    var seed by rememberSaveable { mutableLongStateOf(Random.nextLong()) }
    val context = LocalContext.current
    val clipboard = context.getSystemService(ClipboardManager::class.java)

    val points = remember(complexity, irregularity, seed) {
        BlobGenerator.points(complexity, irregularity, 1f, seed)
    }
    val color = remember(colorHex) {
        runCatching { Color(android.graphics.Color.parseColor(colorHex)) }.getOrDefault(Color(0xFFFF3B5C))
    }

    ToolScaffold(
        title = "Blob Maker",
        icon = Icons.Rounded.FormatColorFill,
        navigation = navigation,
        onReset = {
            complexity = 8
            irregularity = 30
            sizeDp = 220f
            colorHex = "#FF3B5C"
            seed = Random.nextLong()
        },
        bottomBar = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryActionButton(
                    label = "Générer une nouvelle forme",
                    icon = Icons.Rounded.Casino,
                    onClick = { seed = Random.nextLong() },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryActionButton(
                        label = "Copier SVG",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 200 200\">" +
                                "<path d=\"${BlobGenerator.svgPathData(points.map { it * 90f }, 100f, 100f)}\" fill=\"$colorHex\"/></svg>"
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Blob SVG", svg))
                        },
                    )
                    SecondaryActionButton(
                        label = "Sauvegarder PNG",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val file = BlobExporter.exportPng(context, points.map { it * 400f }, 800, colorHex)
                            val uri = BlobExporter.uriFor(context, file)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "image/png"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Partager le blob"))
                        },
                    )
                }
            }
        },
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Couleur", style = MaterialTheme.typography.bodyLarge)
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(color, CircleShape)
                            .clickable { showColorPicker = true },
                    )
                }

                Text("Complexité : $complexity", style = MaterialTheme.typography.titleMedium)
                Slider(value = complexity.toFloat(), onValueChange = { complexity = it.toInt() }, valueRange = 3f..24f, steps = 20)

                Text("Irrégularité : $irregularity%", style = MaterialTheme.typography.titleMedium)
                Slider(value = irregularity.toFloat(), onValueChange = { irregularity = it.toInt() }, valueRange = 0f..80f)

                Text("Taille : ${sizeDp.toInt()} px", style = MaterialTheme.typography.titleMedium)
                Slider(value = sizeDp, onValueChange = { sizeDp = it }, valueRange = 100f..320f)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(24.dp),
                contentAlignment = androidx.compose.ui.Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(sizeDp.dp)) {
                    val radius = size.minDimension / 2f
                    val scaled = points.map { Offset(it.x * radius, it.y * radius) }
                    val mids = BlobGenerator.midpoints(scaled)
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val path = Path().apply {
                        val start = mids.last()
                        moveTo(center.x + start.x, center.y + start.y)
                        for (i in scaled.indices) {
                            val control = scaled[i]
                            val end = mids[i]
                            quadraticTo(center.x + control.x, center.y + control.y, center.x + end.x, center.y + end.y)
                        }
                        close()
                    }
                    drawPath(path, color = color)
                }
            }
        }
    }

    if (showColorPicker) {
        ModernColorPickerDialog(
            initialColor = color,
            onDismiss = { showColorPicker = false },
            onColorConfirmed = { picked ->
                val r = (picked.red * 255).toInt()
                val g = (picked.green * 255).toInt()
                val b = (picked.blue * 255).toInt()
                colorHex = String.format("#%02X%02X%02X", r, g, b)
            },
        )
    }
}
