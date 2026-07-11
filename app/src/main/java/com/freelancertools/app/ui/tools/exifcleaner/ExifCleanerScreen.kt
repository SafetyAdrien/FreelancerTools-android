package com.freelancertools.app.ui.tools.exifcleaner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.material.icons.rounded.FilterVintage
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.exifinterface.media.ExifInterface
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.SectionTitle
import com.freelancertools.app.ui.common.ToolScaffold
import com.freelancertools.app.ui.tools.common.ImageExporter
import java.io.File

private data class ExifResult(val name: String, val originalSize: Long, val cleanedSize: Long, val tagsRemoved: Int, val file: File)

private val EXIF_TAGS = listOf(
    ExifInterface.TAG_DATETIME, ExifInterface.TAG_GPS_LATITUDE, ExifInterface.TAG_GPS_LONGITUDE,
    ExifInterface.TAG_MAKE, ExifInterface.TAG_MODEL, ExifInterface.TAG_ORIENTATION,
    ExifInterface.TAG_F_NUMBER, ExifInterface.TAG_FOCAL_LENGTH, ExifInterface.TAG_ISO_SPEED,
    ExifInterface.TAG_WHITE_BALANCE, ExifInterface.TAG_SOFTWARE,
)

@Composable
fun ExifCleanerScreen(navigation: ScaffoldNavigation) {
    val context = LocalContext.current
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val results = remember { mutableStateListOf<ExifResult>() }

    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(20)) { uris ->
        if (uris.isNotEmpty()) { selectedUris = uris; results.clear() }
    }

    ToolScaffold(
        title = "Nettoyeur EXIF",
        icon = Icons.Rounded.FilterVintage,
        navigation = navigation,
        bottomBar = {
            PrimaryActionButton(
                label = "Nettoyer (${selectedUris.size})",
                enabled = selectedUris.isNotEmpty(),
                onClick = {
                    results.clear()
                    selectedUris.forEach { uri -> cleanExif(context, uri)?.let { results.add(it) } }
                },
            )
        },
    ) {
        PrimaryActionButton(
            label = "Choisir des images",
            icon = Icons.Rounded.PhotoLibrary,
            onClick = { pickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        )
        Text(
            "${selectedUris.size} image(s) sélectionnée(s)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (results.isNotEmpty()) {
            SectionTitle("Résultat")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                results.forEach { r ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.medium) {
                        Column(Modifier.padding(12.dp)) {
                            Text(r.name, style = MaterialTheme.typography.bodyMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    "${r.tagsRemoved} métadonnée(s) supprimée(s)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    "${r.originalSize / 1024} Ko → ${r.cleanedSize / 1024} Ko",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            PrimaryActionButton(
                label = "Partager les résultats",
                onClick = { ImageExporter.shareMultiple(context, results.map { it.file }, mimeType = "image/*") },
            )
        }
    }
}

private fun cleanExif(context: Context, uri: Uri): ExifResult? = runCatching {
    val originalSize = context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { it.length } ?: 0L

    var tagsPresent = 0
    context.contentResolver.openInputStream(uri)?.use { stream ->
        val exif = ExifInterface(stream)
        tagsPresent = EXIF_TAGS.count { exif.getAttribute(it) != null }
    }

    val bitmap: Bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        ?: return null
    val fileName = "clean-${System.currentTimeMillis()}.jpg"
    val file = ImageExporter.save(context, bitmap, fileName, Bitmap.CompressFormat.JPEG, 95)

    ExifResult(name = fileName, originalSize = originalSize, cleanedSize = file.length(), tagsRemoved = tagsPresent, file = file)
}.getOrNull()
