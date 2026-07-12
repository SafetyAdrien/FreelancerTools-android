package com.freelancertools.app.ui.tools.designsystem

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CropSquare
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.SmartButton
import androidx.compose.material.icons.rounded.TableRows
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.ViewCarousel
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * One sub-page of the Design System catalog. `components` is the inventory of Discord component
 * names this page's demos stand in for (from redesign.ts's redesignProps checklist) — shown as a
 * small reference caption, not code to execute.
 */
data class DesignSystemCategory(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val components: List<String>,
)

val DESIGN_SYSTEM_CATEGORIES = listOf(
    DesignSystemCategory(
        id = "buttons",
        title = "Boutons & Actions",
        subtitle = "Boutons primaires, icônes, FAB, actions contextuelles",
        icon = Icons.Rounded.SmartButton,
        components = listOf(
            "Button", "IconButton", "HeaderButton", "HeaderActionButton", "RowButton",
            "InputButton", "FloatingActionButton", "AlertActionButton", "ModalActionButton",
        ),
    ),
    DesignSystemCategory(
        id = "inputs",
        title = "Champs & Saisie",
        subtitle = "Champs de texte, recherche, slider, contrôles segmentés",
        icon = Icons.Rounded.TextFields,
        components = listOf(
            "Input", "InputContainer", "GhostInput", "TextField", "TextInput", "TextArea",
            "SearchField", "Slider", "SegmentedControl", "SegmentedControlPages",
        ),
    ),
    DesignSystemCategory(
        id = "tables",
        title = "Tableaux & Listes",
        subtitle = "Listes de réglages : lignes, cases, radios, switches",
        icon = Icons.Rounded.TableRows,
        components = listOf(
            "TableRow", "TableRowGroup", "TableRowGroupTitle", "TableRowIcon",
            "TableCheckboxRow", "TableRadioRow", "TableRadioGroup", "TableSwitchRow",
        ),
    ),
    DesignSystemCategory(
        id = "modals",
        title = "Modales & Alertes",
        subtitle = "Modales simples, à étapes, avec pied de page",
        icon = Icons.Rounded.Dashboard,
        components = listOf(
            "Modal", "ModalContent", "ModalFooter", "ModalDisclaimer", "ModalScreen",
            "ModalStepIndicator", "ModalFloatingAction", "AlertModal", "AlertModalContainer",
            "StepModal", "Backdrop",
        ),
    ),
    DesignSystemCategory(
        id = "navigation",
        title = "Navigation",
        subtitle = "Pile d'écrans, en-tête collant, onglets",
        icon = Icons.Rounded.ViewCarousel,
        components = listOf(
            "Navigator", "NavigatorHeader", "NavigatorScreen", "FauxHeader", "StickyHeader", "Tabs",
        ),
    ),
    DesignSystemCategory(
        id = "avatars",
        title = "Avatars & Piles",
        subtitle = "Piles d'avatars superposés, icônes de serveur",
        icon = Icons.Rounded.AccountCircle,
        components = listOf("AvatarPile", "AvatarDuoPile", "GuildIconPile", "Pile", "PileOverflow"),
    ),
    DesignSystemCategory(
        id = "overlays",
        title = "Retours & Overlays",
        subtitle = "Toasts, menu contextuel, infobulles, indicateurs",
        icon = Icons.Rounded.Notifications,
        components = listOf(
            "Toast", "ContextMenu", "ContextMenuContainer", "Tooltip", "Coachmark",
            "SceneLoadingIndicator", "HeaderSubmittingIndicator",
        ),
    ),
    DesignSystemCategory(
        id = "cards",
        title = "Cartes",
        subtitle = "Cartes standard, élevées, contournées",
        icon = Icons.Rounded.CropSquare,
        components = listOf("Card"),
    ),
    DesignSystemCategory(
        id = "discord",
        title = "Discord",
        subtitle = "Palette, typographie et composants du design system Discord",
        icon = Icons.Rounded.Layers,
        components = emptyList(),
    ),
)
