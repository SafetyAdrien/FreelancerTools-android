package com.freelancertools.app.ui.tools.qrcodes

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold
import com.freelancertools.app.ui.tools.common.ImageExporter

private enum class QrTab(val label: String) { SCAN("Scanner"), GENERATE("Générer") }

@Composable
fun QrCodesScreen(navigation: ScaffoldNavigation) {
    var tab by rememberSaveable { mutableStateOf(QrTab.GENERATE) }

    ToolScaffold(title = "QR Codes", icon = Icons.Rounded.QrCode2, navigation = navigation, scrollable = tab == QrTab.GENERATE) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            QrTab.entries.forEachIndexed { index, t ->
                SegmentedButton(
                    selected = tab == t,
                    onClick = { tab = t },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = QrTab.entries.size),
                ) { Text(t.label) }
            }
        }

        when (tab) {
            QrTab.SCAN -> ScanTab()
            QrTab.GENERATE -> GenerateTab()
        }
    }
}

@Composable
private fun ScanTab() {
    val context = LocalContext.current
    var hasPermission by rememberSaveable {
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var detected by rememberSaveable { mutableStateOf<String?>(null) }
    val clipboard = context.getSystemService(ClipboardManager::class.java)

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasPermission = granted
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (!hasPermission) {
            PrimaryActionButton(
                label = "Autoriser la caméra",
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            )
        } else {
            Card(
                shape = MaterialTheme.shapes.large,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f),
            ) {
                QrScannerView(onDetected = { detected = it }, modifier = Modifier.fillMaxSize())
            }
        }

        detected?.let { value ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Contenu détecté", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value, style = MaterialTheme.typography.bodyLarge)
                    val isLink = value.startsWith("http://") || value.startsWith("https://")
                    if (isLink) {
                        PrimaryActionButton(
                            label = "Ouvrir le lien",
                            icon = Icons.Rounded.OpenInNew,
                            onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(value))) },
                        )
                    }
                    OutlinedButton(
                        onClick = { clipboard?.setPrimaryClip(ClipData.newPlainText("QR", value)) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = null)
                        Text(" Copier", modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun GenerateTab() {
    var content by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val bitmap = remember(content) { QrGenerator.generate(content) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Contenu") },
            placeholder = { Text("https://monsite.com") },
            modifier = Modifier.fillMaxWidth(),
        )

        if (bitmap != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "QR Code",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(24.dp),
                )
            }
            PrimaryActionButton(
                label = "Télécharger PNG",
                onClick = {
                    val file = ImageExporter.save(context, bitmap, "qr-${System.currentTimeMillis()}.png")
                    ImageExporter.share(context, file, title = "Partager le QR code")
                },
            )
        }
    }
}
