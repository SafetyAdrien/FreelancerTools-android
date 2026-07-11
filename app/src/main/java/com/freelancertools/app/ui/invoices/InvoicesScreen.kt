package com.freelancertools.app.ui.invoices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freelancertools.app.ui.common.EmptyState
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoicesScreen(
    navigation: ScaffoldNavigation,
    onNewInvoice: () -> Unit,
    viewModel: InvoicesViewModel = hiltViewModel(),
) {
    val invoices by viewModel.invoices.collectAsStateWithLifecycle()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE)
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)

    ToolScaffold(
        title = "Factures",
        icon = Icons.Rounded.ReceiptLong,
        navigation = navigation,
        bottomBar = {
            PrimaryActionButton(label = "Nouvelle Facture", icon = Icons.Rounded.Add, onClick = onNewInvoice)
        },
    ) {
        if (invoices.isEmpty()) {
            EmptyState(
                title = "Aucune facture",
                subtitle = "Créez votre première facture en quelques secondes.",
                icon = Icons.Rounded.ReceiptLong,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                invoices.forEach { row ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = MaterialTheme.shapes.large,
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text("Facture #${row.invoice.number}", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    row.clientName ?: "Sans client",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                Text(
                                    dateFormat.format(Date(row.invoice.createdAt)),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                currencyFormat.format(row.invoice.totalHT),
                                style = MaterialTheme.typography.titleMedium,
                            )
                        }
                    }
                }
            }
        }
    }
}
