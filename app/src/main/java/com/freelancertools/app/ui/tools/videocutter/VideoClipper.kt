package com.freelancertools.app.ui.tools.videocutter

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.Transformer
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Trims a single clip [startMs, endMs) from [sourceUri] into [outputFile] using Media3 Transformer. */
object VideoClipper {

    suspend fun clip(context: Context, sourceUri: Uri, startMs: Long, endMs: Long, outputFile: File) {
        suspendCancellableCoroutine { continuation ->
            val mediaItem = MediaItem.Builder()
                .setUri(sourceUri)
                .setClippingConfiguration(
                    MediaItem.ClippingConfiguration.Builder()
                        .setStartPositionMs(startMs)
                        .setEndPositionMs(endMs)
                        .build(),
                )
                .build()
            val editedMediaItem = EditedMediaItem.Builder(mediaItem).build()

            val transformer = Transformer.Builder(context)
                .addListener(object : Transformer.Listener {
                    override fun onCompleted(composition: androidx.media3.transformer.Composition, exportResult: ExportResult) {
                        if (continuation.isActive) continuation.resume(Unit)
                    }

                    override fun onError(
                        composition: androidx.media3.transformer.Composition,
                        exportResult: ExportResult,
                        exportException: ExportException,
                    ) {
                        if (continuation.isActive) continuation.resumeWithException(exportException)
                    }
                })
                .build()

            transformer.start(editedMediaItem, outputFile.absolutePath)

            continuation.invokeOnCancellation { transformer.cancel() }
        }
    }
}
