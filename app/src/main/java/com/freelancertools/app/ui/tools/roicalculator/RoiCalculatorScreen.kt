package com.freelancertools.app.ui.tools.roicalculator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold
import com.freelancertools.app.ui.theme.SuccessGreen
import kotlin.math.roundToInt

@Composable
fun RoiCalculatorScreen(navigation: ScaffoldNavigation) {
    var investment by rememberSaveable { mutableStateOf("") }
    var gain by rememberSaveable { mutableStateOf("") }

    val investmentValue = investment.toDoubleOrNull() ?: 0.0
    val gainValue = gain.toDoubleOrNull() ?: 0.0
    val roi = if (investmentValue > 0) ((gainValue - investmentValue) / investmentValue) * 100 else null

    ToolScaffold(title = "Calculateur ROI", icon = Icons.Rounded.Calculate, navigation = navigation) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                OutlinedTextField(
                    value = investment,
                    onValueChange = { investment = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Investissement (€)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = gain,
                    onValueChange = { gain = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Gain généré (€)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        if (roi != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                ) {
                    Text(
                        "Retour sur investissement",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${(roi * 100).roundToInt() / 100.0}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (roi >= 0) SuccessGreen else MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = "Bénéfice net : ${gainValue - investmentValue} €",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
