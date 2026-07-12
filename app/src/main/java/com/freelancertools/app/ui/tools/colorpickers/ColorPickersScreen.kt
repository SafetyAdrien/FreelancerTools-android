package com.freelancertools.app.ui.tools.colorpickers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold
import com.freelancertools.app.ui.common.colorpicker.ModernColorPickerPanel
import com.freelancertools.app.ui.common.colorpicker.rgbToHsv

/** Default color from the "ModernColorPicker" reference screenshot. */
private val DEFAULT_COLOR = Color(0xFF4D00D4)

@Composable
fun ColorPickersScreen(navigation: ScaffoldNavigation) {
    val initialHsv = remember { rgbToHsv(DEFAULT_COLOR) }
    var hue by rememberSaveable { mutableFloatStateOf(initialHsv[0]) }
    var sat by rememberSaveable { mutableFloatStateOf(initialHsv[1]) }
    var v by rememberSaveable { mutableFloatStateOf(initialHsv[2]) }
    var alpha by rememberSaveable { mutableFloatStateOf(1f) }

    ToolScaffold(
        title = "Color Pickers",
        icon = Icons.Rounded.Colorize,
        navigation = navigation,
        onReset = { hue = initialHsv[0]; sat = initialHsv[1]; v = initialHsv[2]; alpha = 1f },
    ) {
        ModernColorPickerPanel(
            hue = hue,
            sat = sat,
            v = v,
            alpha = alpha,
            onHsvaChange = { h, s, newV, a -> hue = h; sat = s; v = newV; alpha = a },
        )
    }
}
