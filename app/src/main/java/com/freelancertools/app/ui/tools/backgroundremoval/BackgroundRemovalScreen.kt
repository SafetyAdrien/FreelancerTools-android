package com.freelancertools.app.ui.tools.backgroundremoval

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.PhotoLibrary
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold
import com.freelancertools.app.ui.tools.common.ImageExporter
import kotlinx.coroutines.launch

@Composable
fun BackgroundRemovalScreen(navigation: ScaffoldNavigation) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var resultBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            resultBitmap = null
            sourceBitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        }
    }

    ToolScaffold(
        title = "Détourage IA",
        icon = Icons.Rounded.AutoAwesome,
        navigation = navigation,
        bottomBar = {
            if (resultBitmap != null) {
                PrimaryActionButton(
                    label = "Exporter PNG",
                    onClick = {
                        resultBitmap?.let {
                            val file = ImageExporter.save(context, it, "detourage-${System.currentTimeMillis()}.png")
                            ImageExporter.share(context, file, title = "Partager l'image détourée")
                        }
                    },
                )
            }
        },
    ) {
        PrimaryActionButton(
            label = "Choisir une image",
            icon = Icons.Rounded.PhotoLibrary,
            onClick = { pickerLauncher.launch("image/*") },
        )

        sourceBitmap?.let { source ->
            PrimaryActionButton(
                label = "Détourer",
                enabled = !isProcessing,
                onClick = {
                    isProcessing = true
                    scope.launch {
                        runCatching { SegmentationProcessor.removeBackground(source) }
                            .onSuccess { resultBitmap = it }
                        isProcessing = false
                    }
                },
            )
        }

        if (isProcessing) {
            CircularProgressIndicator()
        }

        resultBitmap?.let { result ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.large) {
                Column(Modifier.aspectRatio(1f)) {
                    Image(
                        bitmap = checkerboardBitmap(result.width, result.height, result).asImageBitmap(),
                        contentDescription = "Résultat détouré",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        } ?: run {
            if (sourceBitmap == null) {
                Text(
                    "Sélectionnez une photo pour retirer automatiquement l'arrière-plan (segmentation IA locale).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Composites the transparent result over a light/dark checkerboard so transparency is visible in the preview. */
private fun checkerboardBitmap(width: Int, height: Int, foreground: Bitmap): Bitmap {
    val tile = 16
    val checker = Bitmap.createBitmap(tile * 2, tile * 2, Bitmap.Config.ARGB_8888)
    val checkerCanvas = Canvas(checker)
    val light = Paint().apply { color = android.graphics.Color.parseColor("#3A3A3A") }
    val dark = Paint().apply { color = android.graphics.Color.parseColor("#2A2A2A") }
    checkerCanvas.drawRect(0f, 0f, tile.toFloat(), tile.toFloat(), light)
    checkerCanvas.drawRect(tile.toFloat(), 0f, (tile * 2).toFloat(), tile.toFloat(), dark)
    checkerCanvas.drawRect(0f, tile.toFloat(), tile.toFloat(), (tile * 2).toFloat(), dark)
    checkerCanvas.drawRect(tile.toFloat(), tile.toFloat(), (tile * 2).toFloat(), (tile * 2).toFloat(), light)

    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(output)
    val shader = android.graphics.BitmapShader(checker, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT)
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), Paint().apply { this.shader = shader })
    canvas.drawBitmap(foreground, 0f, 0f, null)
    return output
}
