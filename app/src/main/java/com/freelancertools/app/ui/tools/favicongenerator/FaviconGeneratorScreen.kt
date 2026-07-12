package com.freelancertools.app.ui.tools.favicongenerator

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.ViewSidebar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

private val FAVICON_SIZES = listOf(16, 32, 48, 180, 512)

@Composable
fun FaviconGeneratorScreen(navigation: ScaffoldNavigation) {
    val context = LocalContext.current
    var sourceUri by remember { mutableStateOf<Uri?>(null) }
    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            sourceUri = uri
            sourceBitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        }
    }

    ToolScaffold(
        title = "Favicon Generator",
        icon = Icons.Rounded.ViewSidebar,
        navigation = navigation,
        onReset = { sourceUri = null; sourceBitmap = null },
        bottomBar = {
            PrimaryActionButton(
                label = "Générer et exporter (.zip)",
                enabled = sourceBitmap != null,
                onClick = { sourceBitmap?.let { exportFaviconSet(context, it) } },
            )
        },
    ) {
        PrimaryActionButton(
            label = "Choisir une image source",
            icon = Icons.Rounded.PhotoLibrary,
            onClick = { pickerLauncher.launch("image/*") },
        )

        sourceBitmap?.let { bmp ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Image source",
                        modifier = Modifier.size(120.dp),
                    )
                    Text(
                        "Set généré : ${FAVICON_SIZES.joinToString(", ") { "${it}px" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                }
            }
        }
    }
}

private fun exportFaviconSet(context: Context, source: Bitmap) {
    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val zipFile = File(dir, "favicon-set-${System.currentTimeMillis()}.zip")

    ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
        FAVICON_SIZES.forEach { size ->
            val resized = Bitmap.createScaledBitmap(source, size, size, true)
            zip.putNextEntry(ZipEntry("favicon-${size}x${size}.png"))
            resized.compress(Bitmap.CompressFormat.PNG, 100, zip)
            zip.closeEntry()
        }
    }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", zipFile)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/zip"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Partager le set d'icônes"))
}
