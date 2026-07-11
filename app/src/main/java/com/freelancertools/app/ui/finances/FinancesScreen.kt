package com.freelancertools.app.ui.finances

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.MoneyOff
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freelancertools.app.data.local.db.entity.TransactionType
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.SectionTitle
import com.freelancertools.app.ui.common.StatCard
import com.freelancertools.app.ui.common.ToolScaffold
import com.freelancertools.app.ui.theme.InfoBlue
import com.freelancertools.app.ui.theme.SuccessGreen
import java.text.NumberFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancesScreen(
    navigation: ScaffoldNavigation,
    viewModel: FinancesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    val currencyFormat = remember(Locale.FRANCE) { NumberFormat.getCurrencyInstance(Locale.FRANCE) }

    ToolScaffold(
        title = "Finances",
        icon = Icons.Rounded.AccountBalanceWallet,
        navigation = navigation,
        bottomBar = {
            PrimaryActionButton(
                label = "Ajouter Transaction",
                icon = Icons.Rounded.Add,
                onClick = { showAddSheet = true },
            )
        },
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(20.dp)) {
                Text(
                    "Solde",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    currencyFormat.format(state.balance),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SectionTitle("Aperçu mensuel")
            MonthlyBarChart(bars = state.dailyBars)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard(
                label = "Revenu Total",
                value = currencyFormat.format(state.totalIncome),
                icon = Icons.Rounded.AccountBalanceWallet,
                accentColor = SuccessGreen,
                modifier = Modifier.weight(1f),
            )
            StatCard(
                label = "Dépense Totale",
                value = currencyFormat.format(state.totalExpense),
                icon = Icons.Rounded.MoneyOff,
                accentColor = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f),
            )
        }
        StatCard(
            label = "Marge",
            value = "${state.marginPercent.toInt()}%",
            icon = Icons.Rounded.TrendingUp,
            accentColor = InfoBlue,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    if (showAddSheet) {
        AddTransactionSheet(
            onDismiss = { showAddSheet = false },
            onSave = { label, amount, type, date ->
                viewModel.addTransaction(label, amount, type, date)
                showAddSheet = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionSheet(
    onDismiss: () -> Unit,
    onSave: (label: String, amount: Double, type: String, date: Long) -> Unit,
) {
    var label by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(TransactionType.INCOME) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Nouvelle Transaction", style = MaterialTheme.typography.titleLarge)

            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                val options = listOf(TransactionType.INCOME to "Revenu", TransactionType.EXPENSE to "Dépense")
                options.forEachIndexed { index, (value, label2) ->
                    SegmentedButton(
                        selected = type == value,
                        onClick = { type = value },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    ) {
                        Text(label2)
                    }
                }
            }

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Libellé *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Montant (€) *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            PrimaryActionButton(
                label = "Enregistrer",
                icon = Icons.Rounded.Add,
                enabled = label.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0.0,
                onClick = {
                    val zone = ZoneId.systemDefault()
                    val today = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
                    onSave(label, amount.toDoubleOrNull() ?: 0.0, type, today)
                },
            )
        }
    }
}
