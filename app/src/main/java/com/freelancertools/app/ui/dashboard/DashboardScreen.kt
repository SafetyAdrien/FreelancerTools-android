package com.freelancertools.app.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freelancertools.app.ui.common.QuickActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.SectionTitle
import com.freelancertools.app.ui.common.StatCard
import com.freelancertools.app.ui.common.ToolScaffold
import com.freelancertools.app.ui.navigation.Routes
import com.freelancertools.app.ui.theme.AccentPurple
import com.freelancertools.app.ui.theme.InfoBlue
import com.freelancertools.app.ui.theme.SuccessGreen
import com.freelancertools.app.ui.theme.WarningOrange
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun DashboardScreen(
    navigation: ScaffoldNavigation,
    onNavigate: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val firstName = state.userName.trim().substringBefore(" ").ifBlank { "là" }
    val currencyFormat = remember(Locale.FRANCE) {
        NumberFormat.getCurrencyInstance(Locale.FRANCE)
    }
    val today = remember { LocalDate.now() }
    val dateLabel = remember(today) {
        val month = today.month.getDisplayName(TextStyle.FULL, Locale.FRENCH)
        "${today.dayOfMonth} $month ${today.year}"
    }

    ToolScaffold(title = "Accueil", icon = Icons.Rounded.Home, navigation = navigation, scrollable = true) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Bonjour, $firstName 👋",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "Voici ce qui se passe dans ton studio aujourd'hui.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(20.dp),
            ) {
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    label = "Chiffre d'Affaires",
                    value = currencyFormat.format(state.revenueThisMonth),
                    icon = Icons.Rounded.AttachMoney,
                    accentColor = SuccessGreen,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = "Clients Actifs",
                    value = state.activeClientsCount.toString(),
                    icon = Icons.Rounded.People,
                    accentColor = InfoBlue,
                    modifier = Modifier.weight(1f),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(
                    label = "Projets en cours",
                    value = state.activeProjectsCount.toString(),
                    icon = Icons.Rounded.Folder,
                    accentColor = WarningOrange,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = "Palettes créées",
                    value = state.palettesCount.toString(),
                    icon = Icons.Rounded.Palette,
                    accentColor = AccentPurple,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionTitle("Accès Rapide")
            QuickActionButton(
                label = "Nouvelle Facture",
                icon = Icons.Rounded.ReceiptLong,
                accentColor = InfoBlue,
                onClick = { onNavigate(Routes.invoiceEditor()) },
            )
            QuickActionButton(
                label = "Scanner QR",
                icon = Icons.Rounded.QrCode2,
                accentColor = AccentPurple,
                onClick = { onNavigate(Routes.QR_CODES) },
            )
            QuickActionButton(
                label = "Optimiser Image",
                icon = Icons.Rounded.Image,
                accentColor = WarningOrange,
                onClick = { onNavigate(Routes.IMAGE_OPTIMIZER) },
            )
        }
    }
}
