package com.freelancertools.app.ui.common.colorpicker

import androidx.compose.ui.graphics.Color

/** [hue 0..360, saturation 0..1, value 0..1]. */
internal fun rgbToHsv(color: Color): FloatArray {
    val r = color.red
    val g = color.green
    val b = color.blue
    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val delta = max - min
    val v = max
    val s = if (max <= 0f) 0f else delta / max
    val h = when {
        delta <= 0f -> 0f
        max == r -> 60f * (((g - b) / delta).mod(6f))
        max == g -> 60f * (((b - r) / delta) + 2f)
        else -> 60f * (((r - g) / delta) + 4f)
    }
    return floatArrayOf(h, s, v)
}

internal fun Color.toHexRgb(): String {
    val r = (red * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue * 255).toInt().coerceIn(0, 255)
    return String.format("%02X%02X%02X", r, g, b)
}

internal fun parseHexColorOrNull(hex: String): Color? {
    val cleaned = hex.removePrefix("#").trim()
    if (cleaned.length != 6 && cleaned.length != 8) return null
    return runCatching {
        val argb = if (cleaned.length == 6) "FF$cleaned" else cleaned
        Color(android.graphics.Color.parseColor("#$argb"))
    }.getOrNull()
}
