package com.freelancertools.app.ui.tools.fontpairer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.TextFormat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold

private data class FontPair(
    val name: String,
    val titleFamily: FontFamily,
    val bodyFamily: FontFamily,
)

private val PAIRS = listOf(
    FontPair("Classique & Lisible", FontFamily.Serif, FontFamily.SansSerif),
    FontPair("Moderne & Épuré", FontFamily.SansSerif, FontFamily.SansSerif),
    FontPair("Technique & Précis", FontFamily.Monospace, FontFamily.SansSerif),
    FontPair("Élégant & Éditorial", FontFamily.Serif, FontFamily.Serif),
    FontPair("Expressif", FontFamily.Cursive, FontFamily.SansSerif),
)

@Composable
fun FontPairerScreen(navigation: ScaffoldNavigation) {
    ToolScaffold(title = "Font Pairer", icon = Icons.Rounded.TextFormat, navigation = navigation) {
        Text(
            "Ces associations s'appuient sur les familles système (une intégration Google Fonts nécessite un accès réseau).",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            PAIRS.forEach { pair ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            pair.name,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            "Design Studio",
                            fontFamily = pair.titleFamily,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                        Text(
                            "Un texte de démonstration pour évaluer la lisibilité de cette association de polices sur un paragraphe complet.",
                            fontFamily = pair.bodyFamily,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }
        }
    }
}
