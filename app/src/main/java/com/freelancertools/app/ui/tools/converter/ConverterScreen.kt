package com.freelancertools.app.ui.tools.converter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

private enum class ConverterTab(val label: String) {
    LENGTH("Longueur"), WEIGHT("Poids"), CURRENCY("Devises"), COLOR("Couleurs"),
}

private val LENGTH_UNITS = linkedMapOf(
    "mm" to 0.001, "cm" to 0.01, "m" to 1.0, "km" to 1000.0,
    "in" to 0.0254, "ft" to 0.3048, "yd" to 0.9144, "mi" to 1609.344,
)

private val WEIGHT_UNITS = linkedMapOf(
    "mg" to 0.001, "g" to 1.0, "kg" to 1000.0, "t" to 1_000_000.0,
    "oz" to 28.3495, "lb" to 453.592,
)

/** Static, offline approximate rates (base: EUR). Not live — refresh manually if needed. */
private val CURRENCY_RATES = linkedMapOf(
    "EUR" to 1.0, "USD" to 1.08, "GBP" to 0.85, "CHF" to 0.95, "JPY" to 163.0, "CAD" to 1.47,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConverterScreen(navigation: ScaffoldNavigation) {
    var tab by rememberSaveable { mutableStateOf(ConverterTab.LENGTH) }

    ToolScaffold(title = "Convertisseur", icon = Icons.Rounded.SwapHoriz, navigation = navigation) {
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            ConverterTab.entries.forEachIndexed { index, t ->
                SegmentedButton(
                    selected = tab == t,
                    onClick = { tab = t },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = ConverterTab.entries.size),
                ) { Text(t.label) }
            }
        }

        when (tab) {
            ConverterTab.LENGTH -> UnitConverter(LENGTH_UNITS)
            ConverterTab.WEIGHT -> UnitConverter(WEIGHT_UNITS)
            ConverterTab.CURRENCY -> CurrencyConverter()
            ConverterTab.COLOR -> ColorConverter()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitConverter(units: Map<String, Double>) {
    var value by rememberSaveable { mutableStateOf("1") }
    var fromUnit by rememberSaveable { mutableStateOf(units.keys.first()) }
    var toUnit by rememberSaveable { mutableStateOf(units.keys.elementAt(1)) }

    val input = value.toDoubleOrNull() ?: 0.0
    val result = input * (units[fromUnit] ?: 1.0) / (units[toUnit] ?: 1.0)

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Valeur") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                UnitDropdown(units.keys.toList(), fromUnit, { fromUnit = it }, Modifier.weight(1f))
                UnitDropdown(units.keys.toList(), toUnit, { toUnit = it }, Modifier.weight(1f))
            }
            Text(
                text = "${formatNumber(result)} $toUnit",
                style = MaterialTheme.typography.headlineMedium,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyConverter() {
    var value by rememberSaveable { mutableStateOf("1") }
    var fromUnit by rememberSaveable { mutableStateOf("EUR") }
    var toUnit by rememberSaveable { mutableStateOf("USD") }

    val input = value.toDoubleOrNull() ?: 0.0
    val result = input / (CURRENCY_RATES[fromUnit] ?: 1.0) * (CURRENCY_RATES[toUnit] ?: 1.0)

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Montant") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                UnitDropdown(CURRENCY_RATES.keys.toList(), fromUnit, { fromUnit = it }, Modifier.weight(1f))
                UnitDropdown(CURRENCY_RATES.keys.toList(), toUnit, { toUnit = it }, Modifier.weight(1f))
            }
            Text(text = "${formatNumber(result)} $toUnit", style = MaterialTheme.typography.headlineMedium)
            Text(
                "Taux approximatifs et hors-ligne, à titre indicatif.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ColorConverter() {
    var hex by rememberSaveable { mutableStateOf("FF3B5C") }
    val rgb = hexToRgb(hex)

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = MaterialTheme.shapes.large) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = hex,
                onValueChange = { hex = it.filter { c -> c.isLetterOrDigit() }.take(6).uppercase() },
                label = { Text("HEX") },
                prefix = { Text("#") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            if (rgb != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(Color(rgb.first, rgb.second, rgb.third), RoundedCornerShape(12.dp)),
                )
                val (r, g, b) = rgb
                Text("RGB: rgb($r, $g, $b)", style = MaterialTheme.typography.bodyLarge)
                val hsl = rgbToHsl(r, g, b)
                Text(
                    "HSL: hsl(${hsl.first}, ${hsl.second}%, ${hsl.third}%)",
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                Text("Format hexadécimal invalide (ex: FF3B5C)", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitDropdown(options: List<String>, selected: String, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option) }, onClick = { onSelect(option); expanded = false })
            }
        }
    }
}

private fun formatNumber(value: Double): String {
    val rounded = (value * 10000).roundToInt() / 10000.0
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
}

private fun hexToRgb(hex: String): Triple<Int, Int, Int>? {
    if (hex.length != 6) return null
    return try {
        val r = hex.substring(0, 2).toInt(16)
        val g = hex.substring(2, 4).toInt(16)
        val b = hex.substring(4, 6).toInt(16)
        Triple(r, g, b)
    } catch (e: NumberFormatException) {
        null
    }
}

private fun rgbToHsl(r: Int, g: Int, b: Int): Triple<Int, Int, Int> {
    val rf = r / 255.0
    val gf = g / 255.0
    val bf = b / 255.0
    val maxV = max(rf, max(gf, bf))
    val minV = min(rf, min(gf, bf))
    val l = (maxV + minV) / 2
    if (maxV == minV) return Triple(0, 0, (l * 100).roundToInt())
    val d = maxV - minV
    val s = if (l > 0.5) d / (2 - maxV - minV) else d / (maxV + minV)
    val h = when (maxV) {
        rf -> (gf - bf) / d + (if (gf < bf) 6 else 0)
        gf -> (bf - rf) / d + 2
        else -> (rf - gf) / d + 4
    } * 60
    return Triple(h.roundToInt(), (s * 100).roundToInt(), (l * 100).roundToInt())
}
