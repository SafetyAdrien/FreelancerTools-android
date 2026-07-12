package com.freelancertools.app.ui.tools.videocutter

import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold
import com.freelancertools.app.ui.tools.common.ImageExporter
import kotlinx.coroutines.launch
import java.io.File
import kotlin.random.Random

private enum class CutMode(val label: String) { FIXED("Durée fixe"), RANDOM("Durée aléatoire") }

@Composable
fun VideoCutterScreen(navigation: ScaffoldNavigation) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var videoUri by remember { mutableStateOf<Uri?>(null) }
    var presetName by rememberSaveable { mutableStateOf("Clips") }
    var mode by rememberSaveable { mutableStateOf(CutMode.FIXED) }
    var fixedDuration by remember { mutableFloatStateOf(15f) }
    var minDuration by remember { mutableFloatStateOf(8f) }
    var maxDuration by remember { mutableFloatStateOf(20f) }
    var isProcessing by remember { mutableStateOf(false) }
    var progressText by remember { mutableStateOf("") }
    val outputFiles = remember { mutableStateListOf<File>() }

    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) videoUri = uri
    }

    ToolScaffold(
        title = "Découpeur Vidéo",
        icon = Icons.Rounded.ContentCut,
        navigation = navigation,
        onReset = {
            videoUri = null
            presetName = "Clips"
            mode = CutMode.FIXED
            fixedDuration = 15f
            minDuration = 8f
            maxDuration = 20f
            progressText = ""
            outputFiles.clear()
        },
        bottomBar = {
            PrimaryActionButton(
                label = if (isProcessing) "Découpage en cours..." else "Découper",
                icon = Icons.Rounded.ContentCut,
                enabled = videoUri != null && !isProcessing,
                onClick = {
                    val uri = videoUri ?: return@PrimaryActionButton
                    isProcessing = true
                    outputFiles.clear()
                    scope.launch {
                        runCatching {
                            cutVideo(context, uri, presetName, mode, fixedDuration.toInt(), minDuration.toInt(), maxDuration.toInt()) { progress ->
                                progressText = progress
                            }
                        }.onSuccess { outputFiles.addAll(it) }
                        isProcessing = false
                    }
                },
            )
        },
    ) {
        PrimaryActionButton(
            label = "Choisir une vidéo",
            icon = Icons.Rounded.VideoLibrary,
            onClick = { pickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
        )
        videoUri?.let {
            Text("Vidéo sélectionnée", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        OutlinedTextField(
            value = presetName,
            onValueChange = { presetName = it },
            label = { Text("Nom du preset") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.large) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    CutMode.entries.forEachIndexed { index, m ->
                        SegmentedButton(
                            selected = mode == m,
                            onClick = { mode = m },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = CutMode.entries.size),
                        ) { Text(m.label) }
                    }
                }

                if (mode == CutMode.FIXED) {
                    Text("Durée : ${fixedDuration.toInt()}s", style = MaterialTheme.typography.titleMedium)
                    Slider(value = fixedDuration, onValueChange = { fixedDuration = it }, valueRange = 3f..60f)
                } else {
                    Text("Min : ${minDuration.toInt()}s", style = MaterialTheme.typography.titleMedium)
                    Slider(value = minDuration, onValueChange = { minDuration = it.coerceAtMost(maxDuration) }, valueRange = 3f..60f)
                    Text("Max : ${maxDuration.toInt()}s", style = MaterialTheme.typography.titleMedium)
                    Slider(value = maxDuration, onValueChange = { maxDuration = it.coerceAtLeast(minDuration) }, valueRange = 3f..60f)
                }
            }
        }

        if (isProcessing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Text(progressText, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (outputFiles.isNotEmpty()) {
            Text("${outputFiles.size} clip(s) générés dans /DecoupeurVideo/$presetName", style = MaterialTheme.typography.bodyMedium)
            PrimaryActionButton(
                label = "Partager les clips",
                onClick = { ImageExporter.shareMultiple(context, outputFiles, mimeType = "video/mp4", title = "Partager les clips") },
            )
        }
    }
}

private suspend fun cutVideo(
    context: android.content.Context,
    uri: Uri,
    presetName: String,
    mode: CutMode,
    fixedSeconds: Int,
    minSeconds: Int,
    maxSeconds: Int,
    onProgress: (String) -> Unit,
): List<File> {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(context, uri)
    val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
    retriever.release()
    if (durationMs <= 0) return emptyList()

    val outDir = File(context.getExternalFilesDir(null), "DecoupeurVideo/${presetName.ifBlank { "Clips" }}").apply { mkdirs() }
    val segments = mutableListOf<Pair<Long, Long>>()
    var cursorMs = 0L
    var index = 0
    while (cursorMs < durationMs) {
        val segmentSeconds = when (mode) {
            CutMode.FIXED -> fixedSeconds
            CutMode.RANDOM -> Random.nextInt(minSeconds, maxSeconds + 1)
        }
        val end = (cursorMs + segmentSeconds * 1000L).coerceAtMost(durationMs)
        if (end - cursorMs < 1000L) break
        segments += cursorMs to end
        cursorMs = end
        index++
        if (index > 200) break // safety guard against pathological configs
    }

    val files = mutableListOf<File>()
    segments.forEachIndexed { i, (start, end) ->
        onProgress("Clip ${i + 1} / ${segments.size}")
        val file = File(outDir, "clip_${i + 1}.mp4")
        VideoClipper.clip(context, uri, start, end, file)
        files += file
    }
    return files
}
