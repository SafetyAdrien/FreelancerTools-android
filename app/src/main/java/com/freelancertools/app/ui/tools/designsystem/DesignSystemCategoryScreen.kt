package com.freelancertools.app.ui.tools.designsystem

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freelancertools.app.ui.common.EmptyState
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold
import kotlinx.coroutines.delay

@Composable
fun DesignSystemCategoryScreen(categoryId: String, onBack: () -> Unit) {
    val category = DESIGN_SYSTEM_CATEGORIES.firstOrNull { it.id == categoryId }

    ToolScaffold(
        title = category?.title ?: "Design System",
        icon = category?.icon ?: Icons.Rounded.Info,
        navigation = ScaffoldNavigation.Back(onBack),
    ) {
        if (category == null) {
            EmptyState(title = "Catégorie introuvable", subtitle = "", icon = Icons.Rounded.Info)
            return@ToolScaffold
        }

        if (category.components.isNotEmpty()) {
            Text(
                "Composants Discord de référence : ${category.components.joinToString(", ")}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        when (categoryId) {
            "buttons" -> ButtonsCategoryContent()
            "inputs" -> InputsCategoryContent()
            "tables" -> TablesCategoryContent()
            "modals" -> ModalsCategoryContent()
            "navigation" -> NavigationCategoryContent()
            "avatars" -> AvatarsCategoryContent()
            "overlays" -> OverlaysCategoryContent()
            "cards" -> CardsCategoryContent()
            else -> EmptyState(title = "Bientôt disponible", subtitle = "", icon = Icons.Rounded.Info)
        }
    }
}

@Composable
private fun ComponentDemoCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Boutons & Actions
// ---------------------------------------------------------------------------------------------

@Composable
private fun ButtonsCategoryContent() {
    var tapCount by remember { mutableIntStateOf(0) }
    var liked by remember { mutableStateOf(false) }
    var fabCount by remember { mutableIntStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ComponentDemoCard("Button — taps : $tapCount") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { tapCount++ }) { Text("Primaire") }
                OutlinedButton(onClick = { tapCount++ }) { Text("Secondaire") }
                Button(
                    onClick = { tapCount++ },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                ) { Text("Danger") }
                TextButton(onClick = { tapCount++ }) { Text("Ghost") }
            }
        }

        ComponentDemoCard("IconButton") {
            IconButton(onClick = { liked = !liked }) {
                Icon(
                    if (liked) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                    contentDescription = "Aimer",
                    tint = if (liked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        ComponentDemoCard("FloatingActionButton — actions : $fabCount") {
            FloatingActionButton(onClick = { fabCount++ }) {
                Icon(Icons.Rounded.Add, contentDescription = null)
            }
        }

        ComponentDemoCard("Boutons contextuels — Header / Row / Input / Alert / Modal") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("En-tête d'écran", style = MaterialTheme.typography.titleSmall)
                    TextButton(onClick = { tapCount++ }) { Text("HeaderButton") }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Élément de liste", style = MaterialTheme.typography.bodyMedium)
                    OutlinedButton(onClick = { tapCount++ }) { Text("RowButton") }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        placeholder = { Text("Message…") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { tapCount++ }) {
                        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "InputButton")
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { tapCount++ }) { Text("AlertActionButton") }
                    Button(onClick = { tapCount++ }) { Text("ModalActionButton") }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Champs & Saisie
// ---------------------------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputsCategoryContent() {
    var text by remember { mutableStateOf("") }
    var ghostText by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    var sliderValue by remember { mutableFloatStateOf(0.5f) }
    var segment by remember { mutableIntStateOf(0) }
    val segmentLabels = listOf("Un", "Deux", "Trois")

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ComponentDemoCard("Input / TextField / TextInput") {
            OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Champ standard") }, modifier = Modifier.fillMaxWidth())
        }

        ComponentDemoCard("GhostInput / InputContainer") {
            OutlinedTextField(
                value = ghostText,
                onValueChange = { ghostText = it },
                placeholder = { Text("Sans contour…") },
                colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        ComponentDemoCard("TextArea") {
            OutlinedTextField(value = area, onValueChange = { area = it }, minLines = 3, modifier = Modifier.fillMaxWidth())
        }

        ComponentDemoCard("SearchField") {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                placeholder = { Text("Rechercher…") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        ComponentDemoCard("Slider — ${(sliderValue * 100).toInt()}%") {
            Slider(value = sliderValue, onValueChange = { sliderValue = it })
        }

        ComponentDemoCard("SegmentedControl / SegmentedControlPages") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    segmentLabels.forEachIndexed { index, label ->
                        SegmentedButton(
                            selected = segment == index,
                            onClick = { segment = index },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = segmentLabels.size),
                        ) { Text(label) }
                    }
                }
                Text(
                    "Page affichée : ${segmentLabels[segment]}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Tableaux & Listes
// ---------------------------------------------------------------------------------------------

@Composable
private fun TablesCategoryContent() {
    var checkboxChecked by remember { mutableStateOf(true) }
    var switchChecked by remember { mutableStateOf(false) }
    var selectedRadio by remember { mutableStateOf(0) }

    ComponentDemoCard("TableRowGroup — réglages") {
        Column {
            Text("TableRowGroupTitle", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TableRow("TableRow avec icône", Icons.Rounded.Info)
            TableRow("Autre réglage", Icons.Rounded.Groups)
            TableCheckboxRow("TableCheckboxRow", checkboxChecked) { checkboxChecked = it }
            TableSwitchRow("TableSwitchRow", switchChecked) { switchChecked = it }
            Text(
                "TableRadioGroup",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
            listOf("Option A", "Option B", "Option C").forEachIndexed { index, label ->
                TableRadioRow(label, selectedRadio == index) { selectedRadio = index }
            }
        }
    }
}

@Composable
private fun TableRow(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {}
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun TableCheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun TableSwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun TableRadioRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 6.dp))
    }
}

// ---------------------------------------------------------------------------------------------
// Modales & Alertes
// ---------------------------------------------------------------------------------------------

@Composable
private fun ModalsCategoryContent() {
    var showAlert by remember { mutableStateOf(false) }
    var showStepModal by remember { mutableStateOf(false) }
    var step by remember { mutableIntStateOf(0) }
    val totalSteps = 3

    ComponentDemoCard("Modal / AlertModal / Backdrop / ModalDisclaimer") {
        OutlinedButton(onClick = { showAlert = true }) { Text("Ouvrir une modale") }
    }

    ComponentDemoCard("StepModal / ModalStepIndicator / ModalFloatingAction") {
        OutlinedButton(onClick = { step = 0; showStepModal = true }) { Text("Ouvrir un modal à étapes") }
    }

    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text("Exemple de modale") },
            text = {
                Column {
                    Text("Ceci reproduit Modal / ModalContent / ModalFooter.")
                    Text(
                        "ModalDisclaimer : cette action est réversible.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showAlert = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showAlert = false }) { Text("Annuler") } },
        )
    }

    if (showStepModal) {
        AlertDialog(
            onDismissRequest = { showStepModal = false },
            title = { Text("Étape ${step + 1} / $totalSteps") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        repeat(totalSteps) { index ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (index == step) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant),
                            )
                        }
                    }
                    Text("Contenu de l'étape ${step + 1}.")
                }
            },
            confirmButton = {
                if (step < totalSteps - 1) {
                    TextButton(onClick = { step++ }) { Text("Suivant") }
                } else {
                    Button(onClick = { showStepModal = false }) { Text("Terminer") }
                }
            },
            dismissButton = {
                if (step > 0) {
                    TextButton(onClick = { step-- }) { Text("Précédent") }
                } else {
                    TextButton(onClick = { showStepModal = false }) { Text("Fermer") }
                }
            },
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Navigation
// ---------------------------------------------------------------------------------------------

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NavigationCategoryContent() {
    var screen by remember { mutableStateOf("home") }
    var tab by remember { mutableIntStateOf(0) }
    val tabLabels = listOf("Accueil", "Explorer", "Profil")

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ComponentDemoCard("Navigator / NavigatorHeader / NavigatorScreen") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = MaterialTheme.shapes.medium,
            ) {
                Column(Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        if (screen == "detail") {
                            IconButton(onClick = { screen = "home" }) {
                                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Retour")
                            }
                        }
                        Text(if (screen == "home") "Accueil" else "Détail", style = MaterialTheme.typography.titleSmall)
                    }
                    if (screen == "home") {
                        TextButton(onClick = { screen = "detail" }) {
                            Text("Ouvrir un écran")
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.padding(start = 4.dp))
                        }
                    } else {
                        Text("Écran poussé sur la pile de navigation.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        ComponentDemoCard("StickyHeader") {
            LazyColumn(modifier = Modifier.height(160.dp)) {
                (1..3).forEach { section ->
                    stickyHeader {
                        Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
                            Text(
                                "Section $section",
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                            )
                        }
                    }
                    items((1..4).toList()) { row ->
                        Text("Élément $section.$row", modifier = Modifier.padding(8.dp))
                    }
                }
            }
        }

        ComponentDemoCard("Tabs") {
            Column {
                TabRow(selectedTabIndex = tab) {
                    tabLabels.forEachIndexed { index, label ->
                        Tab(selected = tab == index, onClick = { tab = index }, text = { Text(label) })
                    }
                }
                Text(
                    "Onglet actif : ${tabLabels[tab]}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Avatars & Piles
// ---------------------------------------------------------------------------------------------

private val PILE_COLORS = listOf(Color(0xFFE53935), Color(0xFF1E88E5), Color(0xFF43A047), Color(0xFFFB8C00), Color(0xFF8E24AA))

@Composable
private fun AvatarsCategoryContent() {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ComponentDemoCard("AvatarPile / Pile / PileOverflow") {
            Row {
                PILE_COLORS.take(4).forEachIndexed { index, color ->
                    AvatarCircle(color, "${index + 1}", offsetIndex = index)
                }
                OverflowCircle("+3", offsetIndex = 4)
            }
        }

        ComponentDemoCard("AvatarDuoPile") {
            Row {
                AvatarCircle(PILE_COLORS[0], "A", offsetIndex = 0)
                AvatarCircle(PILE_COLORS[1], "B", offsetIndex = 1)
            }
        }

        ComponentDemoCard("GuildIconPile") {
            Row {
                PILE_COLORS.take(3).forEachIndexed { index, color ->
                    Box(
                        modifier = Modifier
                            .offset(x = (index * -12).dp)
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(('A' + index).toString(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarCircle(color: Color, label: String, offsetIndex: Int) {
    Box(
        modifier = Modifier
            .offset(x = (offsetIndex * -12).dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun OverflowCircle(label: String, offsetIndex: Int) {
    Box(
        modifier = Modifier
            .offset(x = (offsetIndex * -12).dp)
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
    }
}

// ---------------------------------------------------------------------------------------------
// Retours & Overlays
// ---------------------------------------------------------------------------------------------

@Composable
private fun OverlaysCategoryContent() {
    var showToast by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showTooltip by remember { mutableStateOf(false) }
    var showCoachmark by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(showToast) {
        if (showToast) {
            delay(2000)
            showToast = false
        }
    }
    LaunchedEffect(isSubmitting) {
        if (isSubmitting) {
            delay(1500)
            isSubmitting = false
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ComponentDemoCard("Toast") {
            Column {
                OutlinedButton(onClick = { showToast = true }) { Text("Afficher un toast") }
                AnimatedVisibility(visible = showToast) {
                    Surface(color = MaterialTheme.colorScheme.inverseSurface, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            "Action effectuée ✓",
                            color = MaterialTheme.colorScheme.inverseOnSurface,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
            }
        }

        ComponentDemoCard("ContextMenu / ContextMenuContainer") {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Rounded.MoreVert, contentDescription = "Menu contextuel")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text("Renommer") }, onClick = { menuExpanded = false })
                    DropdownMenuItem(text = { Text("Dupliquer") }, onClick = { menuExpanded = false })
                    DropdownMenuItem(text = { Text("Supprimer") }, onClick = { menuExpanded = false })
                }
            }
        }

        ComponentDemoCard("Tooltip / Coachmark") {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box {
                    IconButton(onClick = { showTooltip = !showTooltip }) {
                        Icon(Icons.Rounded.Info, contentDescription = "Tooltip")
                    }
                    if (showTooltip) {
                        Surface(
                            color = MaterialTheme.colorScheme.inverseSurface,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.offset(y = 40.dp),
                        ) {
                            Text(
                                "Ceci est une infobulle",
                                color = MaterialTheme.colorScheme.inverseOnSurface,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
                Box {
                    OutlinedButton(onClick = { showCoachmark = !showCoachmark }) { Text("Nouveauté") }
                    if (showCoachmark) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.offset(y = 44.dp),
                        ) {
                            Text(
                                "Découvrez cette fonctionnalité !",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            )
                        }
                    }
                }
            }
        }

        ComponentDemoCard("SceneLoadingIndicator / HeaderSubmittingIndicator") {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = { isSubmitting = true }, enabled = !isSubmitting) {
                    Text(if (isSubmitting) "Envoi…" else "Déclencher un chargement")
                }
                if (isSubmitting) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Text("Chargement de la scène…", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Cartes
// ---------------------------------------------------------------------------------------------

@Composable
private fun CardsCategoryContent() {
    var pressed by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ComponentDemoCard("Card (outlined container)") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = MaterialTheme.shapes.medium,
                onClick = { pressed = !pressed },
            ) {
                Text(
                    if (pressed) "Cliquée !" else "Card standard — cliquez-moi",
                    modifier = Modifier.padding(16.dp),
                )
            }
        }

        ComponentDemoCard("ElevatedCard") {
            ElevatedCard(shape = MaterialTheme.shapes.medium) {
                Text("ElevatedCard", modifier = Modifier.padding(16.dp))
            }
        }

        ComponentDemoCard("OutlinedCard") {
            OutlinedCard(shape = MaterialTheme.shapes.medium) {
                Text("OutlinedCard", modifier = Modifier.padding(16.dp))
            }
        }
    }
}
