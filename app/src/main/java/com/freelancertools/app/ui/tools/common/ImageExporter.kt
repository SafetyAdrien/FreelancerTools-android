package com.freelancertools.app.ui.tools.common

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/** Shared helper to write a Bitmap to the app's cache exports dir and share it via a chooser intent. */
object ImageExporter {

    fun save(context: Context, bitmap: Bitmap, fileName: String, format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG, quality: Int = 100): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, fileName)
        FileOutputStream(file).use { bitmap.compress(format, quality, it) }
        return file
    }

    fun uriFor(context: Context, file: File) =
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    fun share(context: Context, file: File, mimeType: String = "image/png", title: String = "Partager l'image") {
        val uri = uriFor(context, file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }

    fun shareMultiple(context: Context, files: List<File>, mimeType: String = "image/png", title: String = "Partager") {
        val uris = ArrayList(files.map { uriFor(context, it) })
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = mimeType
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}
