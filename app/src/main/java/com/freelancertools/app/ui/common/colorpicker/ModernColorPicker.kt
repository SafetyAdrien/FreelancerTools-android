package com.freelancertools.app.ui.common.colorpicker

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Gradient
import androidx.compose.material.icons.rounded.GridOn
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import kotlin.random.Random

private val GLOBAL_PRESET_COLORS = listOf(
    0xFFE53935, 0xFFFB8C00, 0xFFFDD835, 0xFF6D4C41, 0xFF8BC34A, 0xFF2E7D32, 0xFFD81B60, 0xFF8E24AA,
    0xFF1E88E5, 0xFF00ACC1, 0xFFC5E1A5, 0xFF000000, 0xFF424242, 0xFF9E9E9E, 0xFFFFFFFF, 0xFF4FC3F7,
).map { Color(it) }

/**
 * Reusable "ModernColorPicker" component (screen body or dialog). Hue/saturation/value/alpha are
 * hoisted by the caller and never re-derived from the resulting [Color] mid-gesture, so dragging
 * a slider can't fight with a round-tripped RGB->HSV conversion (same class of bug as the
 * Paramètres text-field cursor jump — the fix is the same: one source of truth, synced outward).
 */
@Composable
fun ModernColorPickerPanel(
    hue: Float,
    sat: Float,
    v: Float,
    alpha: Float,
    onHsvaChange: (hue: Float, sat: Float, v: Float, alpha: Float) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ColorPickerViewModel = hiltViewModel(),
) {
    val customGlobalColors by viewModel.customGlobalColors.collectAsStateWithLifecycle()
    val documentColors = DocumentColorsStore.colors
    val currentColor = Color.hsv(hue, sat, v, alpha)

    var activeTab by rememberSaveable { mutableIntStateOf(0) }
    var pickerCollapsed by rememberSaveable { mutableStateOf(false) }
    var globalCollapsed by rememberSaveable { mutableStateOf(false) }
    var documentCollapsed by rememberSaveable { mutableStateOf(false) }

    Column(modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ToolTabsRow(activeTab, onSelect = { activeTab = it })

        CollapsibleSection("Color Picker", pickerCollapsed, { pickerCollapsed = !pickerCollapsed }) {
            when (activeTab) {
                0 -> {
                    HueSaturationValueSquare(
                        hue = hue,
                        sat = sat,
                        v = v,
                        onSatValueChange = { s, newV -> onHsvaChange(hue, s, newV, alpha) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    HueSlider(hue, onHueChange = { onHsvaChange(it, sat, v, alpha) })
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AlphaSlider(
                            baseColor = Color.hsv(hue, sat, v),
                            alpha = alpha,
                            onAlphaChange = { onHsvaChange(hue, sat, v, it) },
                            modifier = Modifier.weight(1f),
                        )
                        ColorSwatch(currentColor)
                    }
                }
                1 -> GrayscaleTab(v = v, alpha = alpha, currentColor = currentColor, onHsvaChange = onHsvaChange)
                2 -> RgbSlidersTab(hue = hue, sat = sat, v = v, alpha = alpha, currentColor = currentColor, onHsvaChange = onHsvaChange)
                3 -> PalettesTab(alpha = alpha, onHsvaChange = onHsvaChange)
                4 -> ImagePaletteTab(alpha = alpha, onHsvaChange = onHsvaChange)
                else -> PencilTab(alpha = alpha, onHsvaChange = onHsvaChange)
            }
            HexRgbaFields(hue, sat, v, alpha, onHsvaChange)
        }

        CollapsibleSection("Global Colors", globalCollapsed, { globalCollapsed = !globalCollapsed }) {
            ColorGrid(
                colors = GLOBAL_PRESET_COLORS + customGlobalColors,
                onColorTap = { tapped -> rgbToHsv(tapped).let { onHsvaChange(it[0], it[1], it[2], alpha) } },
                onAdd = { viewModel.addGlobalColor(currentColor) },
            )
        }

        CollapsibleSection("Document Colors", documentCollapsed, { documentCollapsed = !documentCollapsed }) {
            ColorGrid(
                colors = documentColors,
                onColorTap = { tapped -> rgbToHsv(tapped).let { onHsvaChange(it[0], it[1], it[2], alpha) } },
                onAdd = { DocumentColorsStore.pin(currentColor) },
                emptyHint = "Aucune couleur épinglée pour l'instant.",
            )
        }
    }
}

/** Shared color picker as a modal dialog — used from Palettes, Blob Maker, etc. */
@Composable
fun ModernColorPickerDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onColorConfirmed: (Color) -> Unit,
) {
    val initialHsv = remember { rgbToHsv(initialColor) }
    var hue by rememberSaveable { mutableFloatStateOf(initialHsv[0]) }
    var sat by rememberSaveable { mutableFloatStateOf(initialHsv[1]) }
    var v by rememberSaveable { mutableFloatStateOf(initialHsv[2]) }
    var alpha by rememberSaveable { mutableFloatStateOf(initialColor.alpha) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 640.dp),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Choisir une couleur", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Fermer")
                    }
                }

                ModernColorPickerPanel(
                    hue = hue,
                    sat = sat,
                    v = v,
                    alpha = alpha,
                    onHsvaChange = { h, s, newV, a -> hue = h; sat = s; v = newV; alpha = a },
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Annuler") }
                    Button(
                        onClick = {
                            val final = Color.hsv(hue, sat, v, alpha)
                            DocumentColorsStore.pin(final)
                            onColorConfirmed(final)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                    ) { Text("Utiliser cette couleur") }
                }
            }
        }
    }
}

@Composable
private fun ToolTabsRow(activeTab: Int, onSelect: (Int) -> Unit) {
    val icons = listOf(
        Icons.Rounded.Colorize,
        Icons.Rounded.Gradient,
        Icons.Rounded.Circle,
        Icons.Rounded.Palette,
        Icons.Rounded.Cloud,
        Icons.Rounded.GridOn,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        icons.forEachIndexed { index, icon ->
            val selected = index == activeTab
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { onSelect(index) },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CollapsibleSection(
    title: String,
    collapsed: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Icon(
                if (collapsed) Icons.Rounded.ChevronRight else Icons.Rounded.ExpandMore,
                contentDescription = if (collapsed) "Développer" else "Réduire",
            )
        }
        if (!collapsed) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
        }
    }
}

@Composable
private fun HueSaturationValueSquare(
    hue: Float,
    sat: Float,
    v: Float,
    onSatValueChange: (sat: Float, v: Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hueColor = Color.hsv(hue, 1f, 1f)
    BoxWithConstraints(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.horizontalGradient(listOf(Color.White, hueColor)))
            .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black))),
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()

        fun updateFromOffset(offset: Offset) {
            if (widthPx <= 0f || heightPx <= 0f) return
            val x = offset.x.coerceIn(0f, widthPx)
            val y = offset.y.coerceIn(0f, heightPx)
            onSatValueChange(x / widthPx, 1f - y / heightPx)
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        updateFromOffset(change.position)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { updateFromOffset(it) })
                },
        )

        val density = LocalDensity.current
        val thumbSize = 24.dp
        val thumbX = with(density) { (sat * widthPx).toDp() } - thumbSize / 2
        val thumbY = with(density) { ((1f - v) * heightPx).toDp() } - thumbSize / 2
        Box(
            modifier = Modifier
                .padding(start = thumbX, top = thumbY)
                .size(thumbSize)
                .border(BorderStroke(3.dp, Color.White), CircleShape)
                .border(BorderStroke(1.dp, Color.Black.copy(alpha = 0.3f)), CircleShape),
        )
    }
}

@Composable
private fun HueSlider(hue: Float, onHueChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    val rainbow = remember { (0..6).map { Color.hsv(it * 60f, 1f, 1f) } }
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp)),
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Brush.horizontalGradient(rainbow))
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        if (widthPx > 0f) onHueChange((change.position.x / widthPx * 360f).coerceIn(0f, 360f))
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        if (widthPx > 0f) onHueChange((it.x / widthPx * 360f).coerceIn(0f, 360f))
                    })
                },
        )
        val density = LocalDensity.current
        val thumbX = with(density) { (hue / 360f * widthPx).toDp() } - 3.dp
        Box(
            modifier = Modifier
                .padding(start = thumbX)
                .width(6.dp)
                .fillMaxHeight()
                .background(Color.White, RoundedCornerShape(3.dp))
                .border(BorderStroke(1.dp, Color.Black.copy(alpha = 0.3f)), RoundedCornerShape(3.dp)),
        )
    }
}

@Composable
private fun AlphaSlider(baseColor: Color, alpha: Float, onAlphaChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp)),
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        CheckerboardBackground(Modifier.matchParentSize())
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Brush.horizontalGradient(listOf(baseColor.copy(alpha = 0f), baseColor.copy(alpha = 1f))))
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        if (widthPx > 0f) onAlphaChange((change.position.x / widthPx).coerceIn(0f, 1f))
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        if (widthPx > 0f) onAlphaChange((it.x / widthPx).coerceIn(0f, 1f))
                    })
                },
        )
        val density = LocalDensity.current
        val thumbX = with(density) { (alpha * widthPx).toDp() } - 3.dp
        Box(
            modifier = Modifier
                .padding(start = thumbX)
                .width(6.dp)
                .fillMaxHeight()
                .background(Color.White, RoundedCornerShape(3.dp))
                .border(BorderStroke(1.dp, Color.Black.copy(alpha = 0.3f)), RoundedCornerShape(3.dp)),
        )
    }
}

/** Tab 2: a single black-to-white slider; the result always has sat=0 so R=G=B by construction. */
@Composable
private fun GrayscaleTab(
    v: Float,
    alpha: Float,
    currentColor: Color,
    onHsvaChange: (hue: Float, sat: Float, v: Float, alpha: Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Luminosité", style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GrayscaleSlider(
                value = v,
                onValueChange = { onHsvaChange(0f, 0f, it, alpha) },
                modifier = Modifier.weight(1f),
            )
            ColorSwatch(currentColor)
        }
    }
}

@Composable
private fun GrayscaleSlider(value: Float, onValueChange: (Float) -> Unit, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp)),
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Brush.horizontalGradient(listOf(Color.Black, Color.White)))
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        if (widthPx > 0f) onValueChange((change.position.x / widthPx).coerceIn(0f, 1f))
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        if (widthPx > 0f) onValueChange((it.x / widthPx).coerceIn(0f, 1f))
                    })
                },
        )
        val density = LocalDensity.current
        val thumbX = with(density) { (value * widthPx).toDp() } - 3.dp
        Box(
            modifier = Modifier
                .padding(start = thumbX)
                .width(6.dp)
                .fillMaxHeight()
                .background(Color.Gray, RoundedCornerShape(3.dp))
                .border(BorderStroke(1.dp, Color.Black.copy(alpha = 0.3f)), RoundedCornerShape(3.dp)),
        )
    }
}

/** Tab 3: independent 0-255 R/G/B sliders + a 0-100% alpha slider, all driving the same HSVA state. */
@Composable
private fun RgbSlidersTab(
    hue: Float,
    sat: Float,
    v: Float,
    alpha: Float,
    currentColor: Color,
    onHsvaChange: (hue: Float, sat: Float, v: Float, alpha: Float) -> Unit,
) {
    val color = Color.hsv(hue, sat, v)
    val r = (color.red * 255).roundToInt()
    val g = (color.green * 255).roundToInt()
    val b = (color.blue * 255).roundToInt()

    fun setRgb(newR: Int, newG: Int, newB: Int) {
        val next = Color(newR / 255f, newG / 255f, newB / 255f)
        rgbToHsv(next).let { onHsvaChange(it[0], it[1], it[2], alpha) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ColorSwatch(currentColor)
            Text("R $r · V $g · B $b", style = MaterialTheme.typography.bodyMedium)
        }
        RgbChannelSlider("Rouge", r, Color(0xFFE53935)) { setRgb(it, g, b) }
        RgbChannelSlider("Vert", g, Color(0xFF2E7D32)) { setRgb(r, it, b) }
        RgbChannelSlider("Bleu", b, Color(0xFF1E88E5)) { setRgb(r, g, it) }
        Text("Alpha : ${(alpha * 100).roundToInt()}%", style = MaterialTheme.typography.labelLarge)
        Slider(value = alpha, onValueChange = { onHsvaChange(hue, sat, v, it) }, valueRange = 0f..1f)
    }
}

@Composable
private fun RgbChannelSlider(label: String, value: Int, tint: Color, onValueChange: (Int) -> Unit) {
    Column {
        Text("$label : $value", style = MaterialTheme.typography.labelLarge)
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.roundToInt()) },
            valueRange = 0f..255f,
            colors = SliderDefaults.colors(thumbColor = tint, activeTrackColor = tint),
        )
    }
}

private val THEMED_PALETTES: List<Pair<String, List<Color>>> = listOf(
    "Pastel" to listOf(0xFFFFD1DC, 0xFFFFECB3, 0xFFC8E6C9, 0xFFB3E5FC, 0xFFD1C4E9, 0xFFF8BBD0),
    "Vives" to listOf(0xFFFF1744, 0xFFFF9100, 0xFFFFEA00, 0xFF00E676, 0xFF00B0FF, 0xFFD500F9),
    "Monochrome" to listOf(0xFF000000, 0xFF262626, 0xFF4D4D4D, 0xFF808080, 0xFFB3B3B3, 0xFFE6E6E6, 0xFFFFFFFF),
    "Terre" to listOf(0xFF6D4C41, 0xFF8D6E63, 0xFFA1887F, 0xFFBCAAA4, 0xFFD7CCC8, 0xFF3E2723),
).map { (name, hexList) -> name to hexList.map { Color(it) } }

/** Tab 4: tap-to-select thematic preset palettes (pastel, vives, monochrome, terre). */
@Composable
private fun PalettesTab(alpha: Float, onHsvaChange: (hue: Float, sat: Float, v: Float, alpha: Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        THEMED_PALETTES.forEach { (name, colors) ->
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(name, style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(c)
                                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)), CircleShape)
                                .clickable { rgbToHsv(c).let { onHsvaChange(it[0], it[1], it[2], alpha) } },
                        )
                    }
                }
            }
        }
    }
}

/** Tab 5: import a photo (Photo Picker) and extract dominant colors via androidx.palette. */
@Composable
private fun ImagePaletteTab(alpha: Float, onHsvaChange: (hue: Float, sat: Float, v: Float, alpha: Float) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var swatches by remember { mutableStateOf<List<Color>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            isLoading = true
            scope.launch {
                swatches = withContext(Dispatchers.IO) { extractDominantColors(context, uri) }
                isLoading = false
            }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedButton(
            onClick = { pickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            enabled = !isLoading,
        ) {
            Text("Importer une image")
        }
        if (isLoading) {
            CircularProgressIndicator()
        } else if (swatches.isEmpty()) {
            Text(
                "Importez une image pour en extraire les couleurs dominantes.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                swatches.forEach { c ->
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(c)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)), CircleShape)
                            .clickable { rgbToHsv(c).let { onHsvaChange(it[0], it[1], it[2], alpha) } },
                    )
                }
            }
        }
    }
}

/** Runs off the main thread — bitmap decode + Palette generation are both CPU/IO-bound. */
private fun extractDominantColors(context: android.content.Context, uri: android.net.Uri): List<Color> = runCatching {
    val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        ?: return@runCatching emptyList()
    val palette = Palette.from(bitmap).generate()
    palette.swatches.sortedByDescending { it.population }.take(8).map { Color(it.rgb) }
}.getOrDefault(emptyList())

private val PENCIL_COLORS: List<Color> = listOf(
    0xFFE57373, 0xFFF06292, 0xFFBA68C8, 0xFF9575CD, 0xFF7986CB, 0xFF64B5F6,
    0xFF4FC3F7, 0xFF4DD0E1, 0xFF4DB6AC, 0xFF81C784, 0xFFAED581, 0xFFDCE775,
    0xFFFFF176, 0xFFFFD54F, 0xFFFFB74D, 0xFFA1887F,
).map { Color(it) }

/** Tab 6: purely decorative "pencil" swatches with a light grain overlay, tap to select. */
@Composable
private fun PencilTab(alpha: Float, onHsvaChange: (hue: Float, sat: Float, v: Float, alpha: Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Crayons", style = MaterialTheme.typography.labelLarge)
        PENCIL_COLORS.chunked(8).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { c ->
                    PencilSwatch(c) { rgbToHsv(c).let { onHsvaChange(it[0], it[1], it[2], alpha) } }
                }
            }
        }
    }
}

@Composable
private fun PencilSwatch(color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .clickable(onClick = onClick),
    ) {
        Canvas(Modifier.matchParentSize()) {
            // Fixed seed derived from the color so the grain is stable across redraws instead of
            // flickering, and cheap enough (a handful of dots) to never warrant caching a Bitmap.
            val rnd = Random(color.toArgb())
            repeat(14) {
                val x = rnd.nextFloat() * size.width
                val y = rnd.nextFloat() * size.height
                drawCircle(Color.Black.copy(alpha = 0.06f), radius = 1.2f, center = Offset(x, y))
            }
        }
    }
}

@Composable
private fun CheckerboardBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val cell = 8.dp.toPx()
        if (cell <= 0f) return@Canvas
        val cols = (size.width / cell).toInt() + 1
        val rows = (size.height / cell).toInt() + 1
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val light = (row + col) % 2 == 0
                drawRect(
                    color = if (light) Color(0xFFE0E0E0) else Color(0xFFBDBDBD),
                    topLeft = Offset(col * cell, row * cell),
                    size = Size(cell, cell),
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp)),
    ) {
        CheckerboardBackground(Modifier.matchParentSize())
        Box(Modifier.matchParentSize().background(color))
    }
}

private data class HsvaSnapshot(val hue: Float, val sat: Float, val v: Float, val alpha: Float)

@Composable
private fun HexRgbaFields(
    hue: Float,
    sat: Float,
    v: Float,
    alpha: Float,
    onHsvaChange: (hue: Float, sat: Float, v: Float, alpha: Float) -> Unit,
) {
    // The square/sliders update hue/sat/v/alpha on every drag pixel; re-laying-out 5 text fields
    // that often causes visible jank for no benefit, so the *displayed* hex/RGB/A values lag by a
    // short debounce instead of tracking every intermediate frame of a drag.
    var display by remember { mutableStateOf(HsvaSnapshot(hue, sat, v, alpha)) }
    LaunchedEffect(hue, sat, v, alpha) {
        delay(32)
        display = HsvaSnapshot(hue, sat, v, alpha)
    }

    val color = Color.hsv(display.hue, display.sat, display.v, 1f)
    val hex = color.toHexRgb()
    val r = (color.red * 255).roundToInt()
    val g = (color.green * 255).roundToInt()
    val b = (color.blue * 255).roundToInt()
    val a = (display.alpha * 100).roundToInt()

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ColorChannelField(
            label = "Hex",
            sourceText = hex,
            modifier = Modifier.weight(1.6f),
            onCommit = { input ->
                parseHexColorOrNull(input)?.let { parsed ->
                    rgbToHsv(parsed).let { onHsvaChange(it[0], it[1], it[2], alpha) }
                }
            },
        )
        ColorChannelField(
            label = "R",
            sourceText = r.toString(),
            modifier = Modifier.weight(1f),
            keyboardType = KeyboardType.Number,
            onCommit = { input ->
                val channel = input.toIntOrNull()?.coerceIn(0, 255) ?: return@ColorChannelField
                rgbToHsv(Color(channel / 255f, color.green, color.blue)).let { onHsvaChange(it[0], it[1], it[2], alpha) }
            },
        )
        ColorChannelField(
            label = "G",
            sourceText = g.toString(),
            modifier = Modifier.weight(1f),
            keyboardType = KeyboardType.Number,
            onCommit = { input ->
                val channel = input.toIntOrNull()?.coerceIn(0, 255) ?: return@ColorChannelField
                rgbToHsv(Color(color.red, channel / 255f, color.blue)).let { onHsvaChange(it[0], it[1], it[2], alpha) }
            },
        )
        ColorChannelField(
            label = "B",
            sourceText = b.toString(),
            modifier = Modifier.weight(1f),
            keyboardType = KeyboardType.Number,
            onCommit = { input ->
                val channel = input.toIntOrNull()?.coerceIn(0, 255) ?: return@ColorChannelField
                rgbToHsv(Color(color.red, color.green, channel / 255f)).let { onHsvaChange(it[0], it[1], it[2], alpha) }
            },
        )
        ColorChannelField(
            label = "A",
            sourceText = a.toString(),
            modifier = Modifier.weight(1f),
            keyboardType = KeyboardType.Number,
            onCommit = { input ->
                val channel = input.toIntOrNull()?.coerceIn(0, 100) ?: return@ColorChannelField
                onHsvaChange(hue, sat, v, channel / 100f)
            },
        )
    }
}

/**
 * Small numeric field synced from an external [sourceText] except while the user is actively
 * focused on it — unlike a permanent one-way latch, resync resumes on blur, so dragging the
 * square/sliders still updates this field's text when it isn't the one being edited.
 */
@Composable
private fun ColorChannelField(
    label: String,
    sourceText: String,
    onCommit: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    var text by remember { mutableStateOf(sourceText) }
    var focused by remember { mutableStateOf(false) }

    LaunchedEffect(sourceText, focused) {
        if (!focused) text = sourceText
    }

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                onCommit(it)
            },
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focused = it.isFocused },
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
    }
}

@Composable
private fun ColorGrid(
    colors: List<Color>,
    onColorTap: (Color) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 8,
    emptyHint: String? = null,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (colors.isEmpty() && emptyHint != null) {
            Text(emptyHint, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        val items: List<Color?> = colors + null
        items.chunked(columns).forEach { rowItems ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowItems.forEach { swatch ->
                    if (swatch != null) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(swatch)
                                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)), CircleShape)
                                .clickable { onColorTap(swatch) },
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), CircleShape)
                                .clickable(onClick = onAdd),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Ajouter une couleur", modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}
