package com.freelancertools.app.ui.tools.qrcodes

import android.graphics.Bitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

object QrGenerator {
    fun generate(content: String, sizePx: Int = 512): Bitmap? {
        if (content.isBlank()) return null
        return runCatching {
            val hints = mapOf(EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M, EncodeHintType.MARGIN to 1)
            val matrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)
            val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
            for (x in 0 until sizePx) {
                for (y in 0 until sizePx) {
                    bitmap[x, y] = if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                }
            }
            bitmap
        }.getOrNull()
    }
}
