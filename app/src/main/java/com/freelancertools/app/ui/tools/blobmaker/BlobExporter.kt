package com.freelancertools.app.ui.tools.blobmaker

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.ui.geometry.Offset
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object BlobExporter {

    fun exportPng(context: Context, points: List<Offset>, sizePx: Int, colorHex: String): File {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val center = sizePx / 2f
        val mids = BlobGenerator.midpoints(points)

        val path = Path()
        val start = mids.last()
        path.moveTo(center + start.x, center + start.y)
        for (i in points.indices) {
            val control = points[i]
            val end = mids[i]
            path.quadTo(center + control.x, center + control.y, center + end.x, center + end.y)
        }
        path.close()

        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = runCatching { Color.parseColor(colorHex) }.getOrDefault(Color.parseColor("#FF3B5C"))
            style = Paint.Style.FILL
        }
        canvas.drawPath(path, paint)

        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, "blob-${System.currentTimeMillis()}.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return file
    }

    fun uriFor(context: Context, file: File) =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
