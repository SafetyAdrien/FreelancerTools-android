package com.freelancertools.app.ui.common.colorpicker

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color

/**
 * Colors pinned from the ModernColorPicker across the app, kept only for the lifetime of the
 * process (not persisted to disk) — matches the "Document Colors" behavior from the spec.
 */
object DocumentColorsStore {
    val colors = mutableStateListOf<Color>()

    fun pin(color: Color) {
        colors.remove(color)
        colors.add(0, color)
        if (colors.size > 16) colors.removeAt(colors.lastIndex)
    }
}
