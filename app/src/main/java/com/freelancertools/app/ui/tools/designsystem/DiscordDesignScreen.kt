package com.freelancertools.app.ui.tools.designsystem

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold

/** Discord's brand palette, reproduced verbatim from the Sprint 3 spec's design tokens. */
private object DiscordColors {
    val Blurple = Color(0xFF5865F2)
    val BlurpleHover = Color(0xFF4752C4)
    val SurfaceDarkest = Color(0xFF1E1F22)
    val SurfaceDark = Color(0xFF2B2D31)
    val SurfaceBase = Color(0xFF313338)
    val TextPrimary = Color(0xFFF2F3F5)
    val TextSecondary = Color(0xFFDBDEE1)
    val TextMuted = Color(0xFF949BA4)
    val StatusOnline = Color(0xFF23A55A)
    val StatusIdle = Color(0xFFF0B232)
    val StatusDnd = Color(0xFFF23F43)
    val StatusStreaming = Color(0xFF593695)
    val StatusOffline = Color(0xFF80848E)
}

@Composable
fun DiscordDesignScreen(onBack: () -> Unit) {
    ToolScaffold(
        title = "Discord",
        icon = Icons.Rounded.Layers,
        navigation = ScaffoldNavigation.Back(onBack),
    ) {
        Surface(
            color = DiscordColors.SurfaceDark,
            shape = MaterialTheme.shapes.large,
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                DiscordSectionTitle("Palette")
                DiscordPaletteSection()

                DiscordSectionTitle("Typographie")
                DiscordTypographySection()

                DiscordSectionTitle("Boutons")
                DiscordButtonsSection()

                DiscordSectionTitle("Avatars & statuts")
                DiscordAvatarsSection()

                DiscordSectionTitle("Carte / Embed")
                DiscordEmbedSection()

                DiscordSectionTitle("Mention")
                DiscordMentionSection()
            }
        }
    }
}

@Composable
private fun DiscordSectionTitle(title: String) {
    Text(
        title.uppercase(),
        color = DiscordColors.TextMuted,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
    )
}

@Composable
private fun DiscordPaletteSection() {
    val swatches = listOf(
        "Blurple" to DiscordColors.Blurple,
        "Blurple hover" to DiscordColors.BlurpleHover,
        "Surface 1e1f22" to DiscordColors.SurfaceDarkest,
        "Surface 2b2d31" to DiscordColors.SurfaceDark,
        "Surface 313338" to DiscordColors.SurfaceBase,
        "Online" to DiscordColors.StatusOnline,
        "Idle" to DiscordColors.StatusIdle,
        "Dnd" to DiscordColors.StatusDnd,
        "Streaming" to DiscordColors.StatusStreaming,
        "Offline" to DiscordColors.StatusOffline,
    )
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        swatches.chunked(2).forEach { rowSwatches ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowSwatches.forEach { (label, color) ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color)
                                .border(1.dp, DiscordColors.SurfaceDarkest, RoundedCornerShape(8.dp)),
                        )
                        Text(
                            label,
                            color = DiscordColors.TextSecondary,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
                if (rowSwatches.size == 1) {
                    androidx.compose.foundation.layout.Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DiscordTypographySection() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Titre d'écran", color = DiscordColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
        Text("Titre de section", color = DiscordColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text("Texte de corps, utilisé pour les messages et les descriptions.", color = DiscordColors.TextSecondary, style = MaterialTheme.typography.bodyMedium)
        Text("Texte discret / meta (horodatage, statut)", color = DiscordColors.TextMuted, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun DiscordButtonsSection() {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = DiscordColors.Blurple, contentColor = Color.White),
            shape = RoundedCornerShape(4.dp),
        ) { Text("Primaire") }
        OutlinedButton(
            onClick = {},
            colors = OutlinedButtonDefaults.outlinedButtonColors(contentColor = DiscordColors.TextPrimary),
            border = androidx.compose.foundation.BorderStroke(1.dp, DiscordColors.TextMuted),
            shape = RoundedCornerShape(4.dp),
        ) { Text("Secondaire") }
        Button(
            onClick = {},
            colors = ButtonDefaults.buttonColors(containerColor = DiscordColors.StatusDnd, contentColor = Color.White),
            shape = RoundedCornerShape(4.dp),
        ) { Text("Danger") }
    }
}

@Composable
private fun DiscordAvatarsSection() {
    var pressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val corner by animateDpAsState(
        targetValue = if (pressed || isPressed) 24.dp else 16.dp,
        animationSpec = tween(durationMillis = 350),
        label = "avatarCorner",
    )

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(corner))
                    .background(DiscordColors.Blurple),
                contentAlignment = Alignment.Center,
            ) {
                Text("A", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(DiscordColors.SurfaceDark)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(DiscordColors.StatusOnline),
            )
        }
        OutlinedButton(
            onClick = { pressed = !pressed },
            colors = OutlinedButtonDefaults.outlinedButtonColors(contentColor = DiscordColors.TextPrimary),
            border = androidx.compose.foundation.BorderStroke(1.dp, DiscordColors.TextMuted),
        ) { Text(if (pressed) "Relâcher" else "Appuyer") }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 12.dp)) {
        listOf(
            "En ligne" to DiscordColors.StatusOnline,
            "Absent" to DiscordColors.StatusIdle,
            "Ne pas déranger" to DiscordColors.StatusDnd,
            "En direct" to DiscordColors.StatusStreaming,
            "Hors ligne" to DiscordColors.StatusOffline,
        ).forEach { (label, color) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                Text(label, color = DiscordColors.TextMuted, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

@Composable
private fun DiscordEmbedSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(4.dp))
            .background(DiscordColors.SurfaceDarkest),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(4.dp)
                .background(DiscordColors.Blurple),
        )
        Column(Modifier.padding(12.dp)) {
            Text("Titre de l'embed", color = DiscordColors.TextPrimary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text(
                "Description de l'embed, avec une bordure gauche colorée de 4dp comme sur Discord.",
                color = DiscordColors.TextSecondary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun DiscordMentionSection() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(DiscordColors.Blurple.copy(alpha = 0.3f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text("@utilisateur", color = DiscordColors.Blurple, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
