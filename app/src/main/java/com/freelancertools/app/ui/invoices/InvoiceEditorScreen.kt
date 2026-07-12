package com.freelancertools.app.ui.invoices

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.IosShare
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceEditorScreen(
    onBack: () -> Unit,
    viewModel: InvoiceEditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE)

    ToolScaffold(
        title = "Éditeur Facture",
        icon = Icons.AutoMirrored.Rounded.ReceiptLong,
        navigation = ScaffoldNavigation.Back(onBack),
        bottomBar = {
            PrimaryActionButton(
                label = "Exporter PDF",
                icon = Icons.Rounded.IosShare,
                onClick = {
                    scope.launch {
                        val file = viewModel.saveAndGeneratePdf()
                        val uri = InvoicePdfGenerator.uriFor(context, file)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(Intent.createChooser(intent, "Partager la facture"))
                    }
                },
            )
        },
    ) {
        Text("CLIENT", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        ClientDropdown(
            clientNames = state.clients.map { it.id to it.name },
            selectedClientId = state.selectedClientId,
            onSelect = viewModel::selectClient,
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "PRESTATIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                IconButton(onClick = viewModel::addItem) {
                    Icon(Icons.Rounded.Add, contentDescription = "Ajouter une prestation")
                }
            }
            state.items.forEachIndexed { index, item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = item.description,
                        onValueChange = { viewModel.updateItem(index, item.copy(description = it)) },
                        placeholder = { Text("Prestation...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = if (item.price == 0.0) "" else item.price.toString(),
                        onValueChange = { raw ->
                            val price = raw.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: 0.0
                            viewModel.updateItem(index, item.copy(price = price))
                        },
                        placeholder = { Text("€0,00") },
                        singleLine = true,
                        modifier = Modifier.weight(0.6f).padding(start = 8.dp),
                    )
                    IconButton(onClick = { viewModel.removeItem(index) }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Retirer")
                    }
                }
            }
        }

        InvoicePreviewCard(
            issuerName = state.issuerName.ifBlank { "Votre société" },
            invoiceNumber = state.number,
            clientName = state.selectedClientName,
            items = state.items,
            totalHT = state.totalHT,
            currencyFormat = currencyFormat,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClientDropdown(
    clientNames: List<Pair<String, String>>,
    selectedClientId: String?,
    onSelect: (String) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedLabel = clientNames.firstOrNull { it.first == selectedClientId }?.second ?: "Sélectionnez un client..."

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            clientNames.forEach { (id, name) ->
                DropdownMenuItem(
                    text = { Text(name) },
                    onClick = {
                        onSelect(id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun InvoicePreviewCard(
    issuerName: String,
    invoiceNumber: Int,
    clientName: String?,
    items: List<InvoiceLineItem>,
    totalHT: Double,
    currencyFormat: NumberFormat,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(issuerName, color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Facture #$invoiceNumber", color = Color(0xFF8A8A8A), style = MaterialTheme.typography.labelSmall)
                }
                Text(
                    "FACTURE",
                    color = Color(0xFFFF3B5C),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }

            Column(Modifier.padding(top = 24.dp)) {
                Text("À l'attention de :", color = Color(0xFF8A8A8A), style = MaterialTheme.typography.labelSmall)
                Text(
                    clientName ?: "Sélectionnez un client...",
                    color = if (clientName != null) Color.Black else Color(0xFFAAAAAA),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            Column(Modifier.padding(top = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Description", color = Color(0xFF8A8A8A), style = MaterialTheme.typography.labelSmall)
                    Text("Prix", color = Color(0xFF8A8A8A), style = MaterialTheme.typography.labelSmall)
                }
                items.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(item.description.ifBlank { "Prestation..." }, color = Color.Black)
                        Text(currencyFormat.format(item.price), color = Color.Black)
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text("Total HT", color = Color(0xFF8A8A8A), style = MaterialTheme.typography.labelSmall)
                Text(
                    currencyFormat.format(totalHT),
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }
    }
}
