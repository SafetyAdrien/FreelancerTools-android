package com.freelancertools.app.ui.tools.scalecalculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.menuAnchor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.SectionTitle
import com.freelancertools.app.ui.common.ToolScaffold
import kotlin.math.pow
import kotlin.math.roundToInt

private data class Ratio(val label: String, val value: Double)

private val RATIOS = listOf(
    Ratio("Minor Second (1.067)", 1.067),
    Ratio("Major Second (1.125)", 1.125),
    Ratio("Minor Third (1.200)", 1.200),
    Ratio("Major Third (1.250)", 1.250),
    Ratio("Perfect Fourth (1.333)", 1.333),
    Ratio("Augmented Fourth (1.414)", 1.414),
    Ratio("Perfect Fifth (1.500)", 1.500),
    Ratio("Golden Ratio (1.618)", 1.618),
)

private val STEPS = listOf(
    "H6" to -1, "Body" to 0, "H5" to 1, "H4" to 2, "H3" to 3, "H2" to 4, "H1" to 5,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScaleCalculatorScreen(navigation: ScaffoldNavigation) {
    var baseSize by rememberSaveable { mutableStateOf("16") }
    var ratioIndex by rememberSaveable { mutableStateOf(3) }
    var expanded by rememberSaveable { mutableStateOf(false) }

    val base = baseSize.toDoubleOrNull() ?: 16.0
    val ratio = RATIOS[ratioIndex]

    ToolScaffold(title = "Scale Calculator", icon = Icons.Rounded.FormatSize, navigation = navigation) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = baseSize,
                    onValueChange = { baseSize = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Taille de base (px)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = ratio.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Ratio") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        RATIOS.forEachIndexed { index, r ->
                            DropdownMenuItem(
                                text = { Text(r.label) },
                                onClick = { ratioIndex = index; expanded = false },
                            )
                        }
                    }
                }
            }
        }

        SectionTitle("Échelle typographique")
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            STEPS.reversed().forEach { (name, step) ->
                val px = base * ratio.value.pow(step)
                val rem = px / 16.0
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${(px * 100).roundToInt() / 100.0}px · ${(rem * 1000).roundToInt() / 1000.0}rem",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
