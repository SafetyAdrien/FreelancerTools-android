package com.freelancertools.app.ui.tools.backgroundremoval

import android.graphics.Bitmap
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.Segmentation
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Runs ML Kit selfie segmentation and cuts the background out, leaving a transparent PNG. */
object SegmentationProcessor {

    suspend fun removeBackground(source: Bitmap): Bitmap = withContext(Dispatchers.Default) {
        val options = SelfieSegmenterOptions.Builder()
            .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE)
            .enableRawSizeMask()
            .build()
        val segmenter = Segmentation.getClient(options)

        val input = InputImage.fromBitmap(source, 0)
        val mask = Tasks.await(segmenter.process(input))
        val buffer = mask.buffer
        val maskWidth = mask.width
        val maskHeight = mask.height

        val scaled = if (maskWidth != source.width || maskHeight != source.height) {
            Bitmap.createScaledBitmap(source, maskWidth, maskHeight, true)
        } else {
            source
        }

        val result = Bitmap.createBitmap(maskWidth, maskHeight, Bitmap.Config.ARGB_8888)
        buffer.rewind()
        val pixels = IntArray(maskWidth * maskHeight)
        scaled.getPixels(pixels, 0, maskWidth, 0, 0, maskWidth, maskHeight)
        for (i in 0 until maskWidth * maskHeight) {
            val confidence = buffer.float
            val alpha = (confidence * 255f).toInt().coerceIn(0, 255)
            val rgb = pixels[i] and 0x00FFFFFF
            pixels[i] = (alpha shl 24) or rgb
        }
        result.setPixels(pixels, 0, maskWidth, 0, 0, maskWidth, maskHeight)
        segmenter.close()
        result
    }
}
