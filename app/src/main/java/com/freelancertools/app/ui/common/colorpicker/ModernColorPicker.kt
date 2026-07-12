package com.freelancertools.app.ui.common.colorpicker

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt

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
            if (activeTab == 0) {
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
                HexRgbaFields(hue, sat, v, alpha, onHsvaChange)
            } else {
                Text(
                    "Cet outil arrive bientôt.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            }
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

@Composable
private fun HexRgbaFields(
    hue: Float,
    sat: Float,
    v: Float,
    alpha: Float,
    onHsvaChange: (hue: Float, sat: Float, v: Float, alpha: Float) -> Unit,
) {
    val color = Color.hsv(hue, sat, v, 1f)
    val hex = color.toHexRgb()
    val r = (color.red * 255).roundToInt()
    val g = (color.green * 255).roundToInt()
    val b = (color.blue * 255).roundToInt()
    val a = (alpha * 100).roundToInt()

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
