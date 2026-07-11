package com.freelancertools.app.ui.tools.watermark

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold
import com.freelancertools.app.ui.tools.common.ImageExporter

private enum class WatermarkPosition(val label: String) {
    TOP_LEFT("Haut gauche"), TOP_RIGHT("Haut droite"),
    CENTER("Centre"),
    BOTTOM_LEFT("Bas gauche"), BOTTOM_RIGHT("Bas droite"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatermarkScreen(navigation: ScaffoldNavigation) {
    val context = LocalContext.current
    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var text by rememberSaveable { mutableStateOf("© Mon Studio") }
    var position by rememberSaveable { mutableStateOf(WatermarkPosition.BOTTOM_RIGHT) }
    var opacity by remember { mutableFloatStateOf(70f) }
    var textSize by remember { mutableFloatStateOf(36f) }
    var expanded by rememberSaveable { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            sourceBitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        }
    }

    val preview = remember(sourceBitmap, text, position, opacity, textSize) {
        sourceBitmap?.let { applyWatermark(it, text, position, opacity.toInt(), textSize) }
    }

    ToolScaffold(
        title = "Filigrane",
        icon = Icons.Rounded.Brush,
        navigation = navigation,
        bottomBar = {
            PrimaryActionButton(
                label = "Exporter",
                enabled = preview != null,
                onClick = {
                    preview?.let {
                        val file = ImageExporter.save(context, it, "watermark-${System.currentTimeMillis()}.png")
                        ImageExporter.share(context, file, title = "Partager l'image filigranée")
                    }
                },
            )
        },
    ) {
        PrimaryActionButton(
            label = "Choisir une image",
            icon = Icons.Rounded.PhotoLibrary,
            onClick = { pickerLauncher.launch("image/*") },
        )

        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text("Texte du filigrane") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = position.label,
                onValueChange = {},
                readOnly = true,
                label = { Text("Position") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                WatermarkPosition.entries.forEach { p ->
                    DropdownMenuItem(text = { Text(p.label) }, onClick = { position = p; expanded = false })
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Opacité : ${opacity.toInt()}%", style = MaterialTheme.typography.titleMedium)
                Slider(value = opacity, onValueChange = { opacity = it }, valueRange = 10f..100f)
                Text("Taille du texte : ${textSize.toInt()}", style = MaterialTheme.typography.titleMedium)
                Slider(value = textSize, onValueChange = { textSize = it }, valueRange = 12f..96f)
            }
        }

        preview?.let { bmp ->
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Aperçu",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun applyWatermark(source: Bitmap, text: String, position: WatermarkPosition, opacityPercent: Int, textSizePx: Float): Bitmap {
    val result = source.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(result)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        alpha = (opacityPercent * 255 / 100).coerceIn(0, 255)
        this.textSize = textSizePx
        setShadowLayer(4f, 1f, 1f, Color.BLACK)
    }
    val padding = 24f
    val textWidth = paint.measureText(text)
    val (x, y) = when (position) {
        WatermarkPosition.TOP_LEFT -> padding to padding + textSizePx
        WatermarkPosition.TOP_RIGHT -> (result.width - textWidth - padding) to padding + textSizePx
        WatermarkPosition.CENTER -> (result.width - textWidth) / 2f to result.height / 2f
        WatermarkPosition.BOTTOM_LEFT -> padding to (result.height - padding)
        WatermarkPosition.BOTTOM_RIGHT -> (result.width - textWidth - padding) to (result.height - padding)
    }
    canvas.drawText(text, x, y, paint)
    return result
}
