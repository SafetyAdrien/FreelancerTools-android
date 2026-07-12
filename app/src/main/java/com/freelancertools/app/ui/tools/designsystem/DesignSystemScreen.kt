package com.freelancertools.app.ui.tools.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.SectionTitle
import com.freelancertools.app.ui.common.ToolScaffold

@Composable
fun DesignSystemScreen(navigation: ScaffoldNavigation) {
    var resetKey by rememberSaveable { mutableIntStateOf(0) }

    ToolScaffold(
        title = "Design System",
        icon = Icons.Rounded.Widgets,
        navigation = navigation,
        onReset = { resetKey++ },
    ) {
        Text(
            "Bibliothèque de composants consultables et testables — chaque démo ci-dessous est en direct.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        key(resetKey) {
            SectionTitle("Boutons")
            ButtonsDemo()

            SectionTitle("Champ de texte")
            TextFieldDemo()

            SectionTitle("Switch & Checkbox")
            SwitchAndCheckboxDemo()

            SectionTitle("Slider")
            SliderDemo()

            SectionTitle("Badges")
            BadgesDemo()

            SectionTitle("Carte")
            CardDemo()

            SectionTitle("Modale")
            ModalDemo()
        }

        SectionTitle("Discord Design System")
        Text(
            "Reproduction visuelle des composants caractéristiques de l'UI Discord, avec leurs tokens de couleur.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        DiscordPaletteDemo()
        DiscordMessageBubbleDemo()
        DiscordButtonsDemo()
        DiscordRoleBadgeDemo()
        DiscordAvatarStatusDemo()
        DiscordSidebarDemo()
    }
}

@Composable
private fun ComponentDemoCard(
    title: String,
    codeSnippet: String? = null,
    content: @Composable () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
            if (codeSnippet != null) {
                Text(
                    codeSnippet,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(10.dp),
                )
            }
        }
    }
}

@Composable
private fun ButtonsDemo() {
    var tapCount by remember { mutableStateOf(0) }
    ComponentDemoCard(title = "Primaire / Secondaire / Danger / Ghost — taps : $tapCount") {
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
}

@Composable
private fun TextFieldDemo() {
    var value by remember { mutableStateOf("") }
    ComponentDemoCard(title = "OutlinedTextField") {
        OutlinedTextField(
            value = value,
            onValueChange = { value = it },
            label = { Text("Champ de démonstration") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun SwitchAndCheckboxDemo() {
    var switchOn by remember { mutableStateOf(true) }
    var checked by remember { mutableStateOf(false) }
    ComponentDemoCard(title = "Switch & Checkbox") {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = switchOn, onCheckedChange = { switchOn = it })
                Text(if (switchOn) "Activé" else "Désactivé", modifier = Modifier.padding(start = 8.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = checked, onCheckedChange = { checked = it })
                Text("Case à cocher", modifier = Modifier.padding(start = 4.dp))
            }
        }
    }
}

@Composable
private fun SliderDemo() {
    var value by remember { mutableFloatStateOf(50f) }
    ComponentDemoCard(title = "Slider — ${value.toInt()}") {
        Slider(value = value, onValueChange = { value = it }, valueRange = 0f..100f)
    }
}

@Composable
private fun BadgesDemo() {
    ComponentDemoCard(title = "Badges") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AssistChip(onClick = {}, label = { Text("Nouveau") })
            AssistChip(onClick = {}, label = { Text("Beta") })
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text("3", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CardDemo() {
    ComponentDemoCard(title = "Card") {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = MaterialTheme.shapes.medium,
        ) {
            Column(Modifier.padding(14.dp)) {
                Text("Titre de carte", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Contenu de démonstration à l'intérieur d'une carte standard.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ModalDemo() {
    var showDialog by remember { mutableStateOf(false) }
    ComponentDemoCard(title = "Modale") {
        OutlinedButton(onClick = { showDialog = true }) { Text("Ouvrir une modale") }
    }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Exemple de modale") },
            text = { Text("Ceci est une AlertDialog Material 3 standard.") },
            confirmButton = { TextButton(onClick = { showDialog = false }) { Text("OK") } },
        )
    }
}

private data class DiscordToken(val name: String, val hex: String, val color: Color)

private val DISCORD_TOKENS = listOf(
    DiscordToken("Blurple", "#5865F2", Color(0xFF5865F2)),
    DiscordToken("Vert (en ligne)", "#23A55A", Color(0xFF23A55A)),
    DiscordToken("Jaune (absent)", "#F0B232", Color(0xFFF0B232)),
    DiscordToken("Rouge (ne pas déranger)", "#F23F43", Color(0xFFF23F43)),
    DiscordToken("Gris (hors ligne)", "#80848E", Color(0xFF80848E)),
    DiscordToken("Fond (chat)", "#313338", Color(0xFF313338)),
    DiscordToken("Fond (sidebar)", "#1E1F22", Color(0xFF1E1F22)),
)

@Composable
private fun DiscordPaletteDemo() {
    ComponentDemoCard(
        title = "Palette de couleurs",
        codeSnippet = DISCORD_TOKENS.joinToString("\n") { "${it.name}: ${it.hex}" },
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DISCORD_TOKENS.take(5).forEach { token ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(token.color),
                )
            }
        }
    }
}

@Composable
private fun DiscordMessageBubbleDemo() {
    ComponentDemoCard(
        title = "Bulle de message",
        codeSnippet = "background: #313338  ·  username: #F2F3F5  ·  accent: #5865F2",
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF313338))
                .padding(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF5865F2)),
                contentAlignment = Alignment.Center,
            ) {
                Text("S", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Column(Modifier.padding(start = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sacha", color = Color(0xFFF2F3F5), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "  aujourd'hui à 14:32",
                        color = Color(0xFF949BA4),
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Text("Voici à quoi ressemble un message dans Discord.", color = Color(0xFFDBDEE1), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun DiscordButtonsDemo() {
    ComponentDemoCard(
        title = "Boutons Discord",
        codeSnippet = "shape: RoundedCornerShape(3.dp)  ·  primary: #5865F2  ·  danger: #DA373C",
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {},
                shape = RoundedCornerShape(3.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5865F2)),
            ) { Text("Confirmer") }
            Button(
                onClick = {},
                shape = RoundedCornerShape(3.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDA373C)),
            ) { Text("Supprimer") }
        }
    }
}

@Composable
private fun DiscordRoleBadgeDemo() {
    ComponentDemoCard(
        title = "Badge de rôle",
        codeSnippet = "shape: RoundedCornerShape(50)  ·  color: role accent",
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RolePill("Admin", Color(0xFFF23F43))
            RolePill("Modérateur", Color(0xFF23A55A))
            RolePill("Membre", Color(0xFF80848E))
        }
    }
}

@Composable
private fun RolePill(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color(0xFF2B2D31))
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(label, color = Color(0xFFDBDEE1), style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(start = 6.dp))
    }
}

@Composable
private fun DiscordAvatarStatusDemo() {
    ComponentDemoCard(
        title = "Avatar + statut",
        codeSnippet = "online: #23A55A  ·  idle: #F0B232  ·  dnd: #F23F43  ·  offline: #80848E",
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            listOf(
                "En ligne" to Color(0xFF23A55A),
                "Absent" to Color(0xFFF0B232),
                "Ne pas déranger" to Color(0xFFF23F43),
                "Hors ligne" to Color(0xFF80848E),
            ).forEach { (label, statusColor) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(44.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF5865F2)),
                        )
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(Color(0xFF313338))
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(statusColor),
                        )
                    }
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun DiscordSidebarDemo() {
    ComponentDemoCard(
        title = "Barre latérale de serveurs",
        codeSnippet = "background: #1E1F22  ·  icon shape: CircleShape (hover → RoundedCornerShape)",
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF1E1F22))
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val serverColors = listOf(Color(0xFF5865F2), Color(0xFF23A55A), Color(0xFFF0B232), Color(0xFFF23F43))
            serverColors.forEach { c ->
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(c),
                )
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF313338))
                    .clickable {},
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Ajouter un serveur", tint = Color(0xFF23A55A), modifier = Modifier.size(16.dp))
            }
        }
    }
}
