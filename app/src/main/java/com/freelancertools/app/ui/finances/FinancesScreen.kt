package com.freelancertools.app.ui.finances

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.MoneyOff
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freelancertools.app.data.local.db.entity.Client
import com.freelancertools.app.data.local.db.entity.Transaction
import com.freelancertools.app.data.local.db.entity.TransactionType
import com.freelancertools.app.ui.common.EmptyState
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.SectionTitle
import com.freelancertools.app.ui.common.StatCard
import com.freelancertools.app.ui.common.ToolScaffold
import com.freelancertools.app.ui.theme.InfoBlue
import com.freelancertools.app.ui.theme.SuccessGreen
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancesScreen(
    navigation: ScaffoldNavigation,
    viewModel: FinancesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Transaction?>(null) }
    val currencyFormat = remember(Locale.FRANCE) { NumberFormat.getCurrencyInstance(Locale.FRANCE) }
    val dateFormat = remember(Locale.FRANCE) { SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE) }

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

        SectionTitle("Transactions")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::setQuery,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Rechercher...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
            )
            val activeFilterCount = listOfNotNull(
                state.filters.type.takeIf { it != TypeFilter.ALL },
                state.filters.period.takeIf { it != PeriodFilter.ALL },
                state.filters.clientId,
            ).size
            Box {
                IconButton(onClick = { showFilterSheet = true }) {
                    Icon(Icons.Rounded.FilterList, contentDescription = "Filtres")
                }
                if (activeFilterCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .background(MaterialTheme.colorScheme.primary, CircleShape),
                    )
                }
            }
        }

        if (state.filteredTransactions.isEmpty()) {
            EmptyState(
                title = "Aucune transaction",
                subtitle = "Ajoutez une transaction ou ajustez vos filtres.",
                icon = Icons.Rounded.Receipt,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.filteredTransactions.forEach { row ->
                    TransactionRowItem(
                        row = row,
                        currencyFormat = currencyFormat,
                        dateFormat = dateFormat,
                        onDeleteRequest = { pendingDelete = row.transaction },
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        AddTransactionSheet(
            clients = state.clients,
            onDismiss = { showAddSheet = false },
            onSave = { label, amount, type, date, clientId ->
                viewModel.addTransaction(label, amount, type, date, clientId)
                showAddSheet = false
            },
        )
    }

    if (showFilterSheet) {
        FilterSheet(
            filters = state.filters,
            clients = state.clients,
            onDismiss = { showFilterSheet = false },
            onApply = {
                viewModel.setFilters(it)
                showFilterSheet = false
            },
        )
    }

    pendingDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Supprimer la transaction ?") },
            text = { Text("« ${transaction.label} » sera définitivement supprimée.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTransaction(transaction)
                    pendingDelete = null
                }) { Text("Supprimer", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Annuler") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionRowItem(
    row: TransactionRow,
    currencyFormat: NumberFormat,
    dateFormat: SimpleDateFormat,
    onDeleteRequest: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDeleteRequest()
            }
            false // snap back visually; the caller's confirmation dialog performs the actual delete
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = null, tint = Color.White)
            }
        },
    ) {
        val isIncome = row.transaction.type == TransactionType.INCOME
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.large,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                (if (isIncome) SuccessGreen else MaterialTheme.colorScheme.error).copy(alpha = 0.18f),
                                CircleShape,
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            if (isIncome) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                            contentDescription = null,
                            tint = if (isIncome) SuccessGreen else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    Column(Modifier.padding(start = 12.dp)) {
                        Text(row.transaction.label, style = MaterialTheme.typography.bodyLarge)
                        val subtitle = listOfNotNull(row.clientName, dateFormat.format(Date(row.transaction.date)))
                            .joinToString(" · ")
                        Text(
                            subtitle,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = (if (isIncome) "+" else "-") + currencyFormat.format(row.transaction.amount),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isIncome) SuccessGreen else MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSheet(
    filters: TransactionFilters,
    clients: List<Client>,
    onDismiss: () -> Unit,
    onApply: (TransactionFilters) -> Unit,
) {
    var type by remember { mutableStateOf(filters.type) }
    var period by remember { mutableStateOf(filters.period) }
    var clientId by remember { mutableStateOf(filters.clientId) }
    var customStart by remember { mutableStateOf(filters.customStart) }
    var customEnd by remember { mutableStateOf(filters.customEnd) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Filtres", style = MaterialTheme.typography.titleLarge)

            Text("Type", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                TypeFilter.entries.forEachIndexed { index, t ->
                    SegmentedButton(
                        selected = type == t,
                        onClick = { type = t },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = TypeFilter.entries.size),
                    ) { Text(t.label) }
                }
            }

            Text("Période", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PeriodFilter.entries.forEach { p ->
                    FilterChip(selected = period == p, onClick = { period = p }, label = { Text(p.label) })
                }
            }

            if (period == PeriodFilter.CUSTOM) {
                Text(
                    "Période personnalisée : réglez les dates depuis un futur sélecteur — pour l'instant, la plage couvre les 90 derniers jours.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                val zone = ZoneId.systemDefault()
                customEnd = LocalDate.now(zone).atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
                customStart = LocalDate.now(zone).minusDays(90).atStartOfDay(zone).toInstant().toEpochMilli()
            }

            Text("Client", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            ClientFilterDropdown(clients = clients, selectedClientId = clientId, onSelect = { clientId = it })

            PrimaryActionButton(
                label = "Appliquer",
                onClick = {
                    onApply(TransactionFilters(type, period, clientId, customStart, customEnd))
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientFilterDropdown(clients: List<Client>, selectedClientId: String?, onSelect: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val label = clients.firstOrNull { it.id == selectedClientId }?.name ?: "Tous les clients"

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = label,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("Tous les clients") }, onClick = { onSelect(null); expanded = false })
            clients.forEach { client ->
                DropdownMenuItem(text = { Text(client.name) }, onClick = { onSelect(client.id); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionSheet(
    clients: List<Client>,
    onDismiss: () -> Unit,
    onSave: (label: String, amount: Double, type: String, date: Long, clientId: String?) -> Unit,
) {
    var label by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(TransactionType.INCOME) }
    var clientId by rememberSaveable { mutableStateOf<String?>(null) }

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
            ClientFilterDropdown(clients = clients, selectedClientId = clientId, onSelect = { clientId = it })

            PrimaryActionButton(
                label = "Enregistrer",
                icon = Icons.Rounded.Add,
                enabled = label.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0.0,
                onClick = {
                    val zone = ZoneId.systemDefault()
                    val today = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
                    onSave(label, amount.toDoubleOrNull() ?: 0.0, type, today, clientId)
                },
            )
        }
    }
}
