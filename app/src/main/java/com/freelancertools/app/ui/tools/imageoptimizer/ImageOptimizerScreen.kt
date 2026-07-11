package com.freelancertools.app.ui.tools.imageoptimizer

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.SectionTitle
import com.freelancertools.app.ui.common.ToolScaffold
import com.freelancertools.app.ui.tools.common.ImageExporter
import java.io.File

private enum class OutputFormat(val label: String, val extension: String) {
    WEBP("WEBP", "webp"),
    JPEG("JPEG", "jpg"),
}

@Suppress("DEPRECATION")
private fun OutputFormat.compressFormat(): Bitmap.CompressFormat = when (this) {
    OutputFormat.WEBP -> if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        Bitmap.CompressFormat.WEBP_LOSSY
    } else {
        Bitmap.CompressFormat.WEBP
    }
    OutputFormat.JPEG -> Bitmap.CompressFormat.JPEG
}

private data class ProcessedImage(val name: String, val originalSize: Long, val optimizedSize: Long, val file: File)

@Composable
fun ImageOptimizerScreen(navigation: ScaffoldNavigation) {
    val context = LocalContext.current
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var quality by remember { mutableFloatStateOf(80f) }
    var format by rememberSaveable { mutableStateOf(OutputFormat.WEBP) }
    val log = remember { mutableStateListOf<ProcessedImage>() }

    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(20)) { uris ->
        if (uris.isNotEmpty()) selectedUris = uris
    }

    ToolScaffold(
        title = "Image Optimizer",
        icon = Icons.Rounded.Image,
        navigation = navigation,
        bottomBar = {
            PrimaryActionButton(
                label = "Optimiser (${selectedUris.size})",
                enabled = selectedUris.isNotEmpty(),
                onClick = {
                    log.clear()
                    selectedUris.forEach { uri ->
                        processImage(context, uri, quality.toInt(), format)?.let { log.add(it) }
                    }
                },
            )
        },
    ) {
        PrimaryActionButton(
            label = "Choisir des images",
            icon = Icons.Rounded.PhotoLibrary,
            onClick = {
                pickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            },
        )
        Text(
            "${selectedUris.size} image(s) sélectionnée(s)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Format de sortie", style = MaterialTheme.typography.titleMedium)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    OutputFormat.entries.forEachIndexed { index, f ->
                        SegmentedButton(
                            selected = format == f,
                            onClick = { format = f },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = OutputFormat.entries.size),
                        ) { Text(f.label) }
                    }
                }
                Text("Qualité : ${quality.toInt()}%", style = MaterialTheme.typography.titleMedium)
                Slider(value = quality, onValueChange = { quality = it }, valueRange = 10f..100f)
            }
        }

        if (log.isNotEmpty()) {
            SectionTitle("Journal de traitement")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                log.forEach { item ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.medium) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(item.name, style = MaterialTheme.typography.bodyMedium)
                            val reduction = if (item.originalSize > 0) {
                                100 - (item.optimizedSize * 100 / item.originalSize)
                            } else 0
                            Text(
                                "${formatSize(item.originalSize)} → ${formatSize(item.optimizedSize)} (-$reduction%)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            PrimaryActionButton(
                label = "Partager les résultats",
                onClick = { ImageExporter.shareMultiple(context, log.map { it.file }, mimeType = "image/*") },
            )
        }
    }
}

private fun processImage(context: android.content.Context, uri: Uri, quality: Int, format: OutputFormat): ProcessedImage? {
    return runCatching {
        val originalSize = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 0L
        val input = context.contentResolver.openInputStream(uri)?.use { android.graphics.BitmapFactory.decodeStream(it) }
            ?: return null
        val fileName = "optimized-${System.currentTimeMillis()}.${format.extension}"
        val file = ImageExporter.save(context, input, fileName, format.compressFormat(), quality)
        ProcessedImage(name = fileName, originalSize = originalSize, optimizedSize = file.length(), file = file)
    }.getOrNull()
}

private fun formatSize(bytes: Long): String = when {
    bytes >= 1_000_000 -> "%.1f Mo".format(bytes / 1_000_000.0)
    bytes >= 1_000 -> "%.0f Ko".format(bytes / 1_000.0)
    else -> "$bytes o"
}
