package com.freelancertools.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.CompareArrows
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.FilterVintage
import androidx.compose.material.icons.rounded.FormatColorFill
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Sell
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.TextFormat
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.ViewSidebar
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.ui.graphics.vector.ImageVector

data class ToolItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val route: String,
)

data class ToolCategory(
    val id: String,
    val title: String,
    val tools: List<ToolItem>,
)

/** Root drawer item, always visible, non-filterable, pinned just under the search field. */
val homeItem = ToolItem("home", "Accueil", Icons.Rounded.Home, Routes.DASHBOARD)

/** Pinned drawer item above the categorized list, opens the "Compte" section in Paramètres. */
val accountItem = ToolItem("account", "Compte", Icons.Rounded.AccountCircle, Routes.SETTINGS)

/** No longer shown as a drawer row — Paramètres is reached via the header icon (see DrawerContent). */
val settingsItem = ToolItem("settings", "Paramètres", Icons.Rounded.Settings, Routes.SETTINGS)

val toolCategories = listOf(
    ToolCategory(
        id = "business",
        title = "Business",
        tools = listOf(
            ToolItem("finances", "Finances", Icons.Rounded.AccountBalanceWallet, Routes.FINANCES),
            ToolItem("clients", "Clients", Icons.Rounded.People, Routes.CLIENTS),
            ToolItem("invoices", "Factures", Icons.Rounded.ReceiptLong, Routes.INVOICES),
            ToolItem("roi", "Calculateur ROI", Icons.Rounded.Calculate, Routes.ROI_CALCULATOR),
        ),
    ),
    ToolCategory(
        id = "images",
        title = "Images",
        tools = listOf(
            ToolItem("image_optimizer", "Image Optimizer", Icons.Rounded.Image, Routes.IMAGE_OPTIMIZER),
            ToolItem("background_removal", "Détourage IA", Icons.Rounded.AutoAwesome, Routes.BACKGROUND_REMOVAL),
            ToolItem("exif_cleaner", "Nettoyeur EXIF", Icons.Rounded.FilterVintage, Routes.EXIF_CLEANER),
            ToolItem("favicon_generator", "Favicon Generator", Icons.Rounded.ViewSidebar, Routes.FAVICON_GENERATOR),
            ToolItem("watermark", "Filigrane", Icons.Rounded.Brush, Routes.WATERMARK),
        ),
    ),
    ToolCategory(
        id = "dev",
        title = "Dev",
        tools = listOf(
            ToolItem("meta_tags", "Générateur Meta Tags", Icons.Rounded.Sell, Routes.META_TAGS),
            ToolItem("markdown_preview", "Markdown Preview", Icons.Rounded.Description, Routes.MARKDOWN_PREVIEW),
            ToolItem("whois", "Whois Lookup", Icons.Rounded.Public, Routes.WHOIS_LOOKUP),
            ToolItem("embed_visualizer", "Embed Visualizer", Icons.Rounded.Code, Routes.EMBED_VISUALIZER),
            ToolItem("speed_test", "Speed Test", Icons.Rounded.Speed, Routes.SPEED_TEST),
            ToolItem("diff_checker", "Diff Checker", Icons.Rounded.CompareArrows, Routes.DIFF_CHECKER),
            ToolItem("converter", "Convertisseur", Icons.Rounded.SwapHoriz, Routes.CONVERTER),
        ),
    ),
    ToolCategory(
        id = "design",
        title = "Design",
        tools = listOf(
            ToolItem("palettes", "Palettes", Icons.Rounded.Palette, Routes.PALETTES),
            ToolItem("font_library", "Polices", Icons.Rounded.TextFields, Routes.FONT_LIBRARY),
            ToolItem("font_pairer", "Font Pairer", Icons.Rounded.TextFormat, Routes.FONT_PAIRER),
            ToolItem("scale_calculator", "Scale Calculator", Icons.Rounded.FormatSize, Routes.SCALE_CALCULATOR),
            ToolItem("blob_maker", "Blob Maker", Icons.Rounded.FormatColorFill, Routes.BLOB_MAKER),
            ToolItem("lorem_ipsum", "Lorem Ipsum", Icons.Rounded.Notes, Routes.LOREM_IPSUM),
            ToolItem("color_pickers", "Color Pickers", Icons.Rounded.Colorize, Routes.COLOR_PICKERS),
            ToolItem("design_system", "Design System", Icons.Rounded.Widgets, Routes.DESIGN_SYSTEM),
        ),
    ),
    ToolCategory(
        id = "utilities",
        title = "Utilitaires",
        tools = listOf(
            ToolItem("qr_codes", "QR Codes", Icons.Rounded.QrCode2, Routes.QR_CODES),
            ToolItem("smart_timer", "Smart Timer", Icons.Rounded.Timer, Routes.SMART_TIMER),
            ToolItem("video_cutter", "Découpeur Vidéo", Icons.Rounded.ContentCut, Routes.VIDEO_CUTTER),
            ToolItem("prompt_manager", "Prompt Manager", Icons.Rounded.Diamond, Routes.PROMPT_MANAGER),
        ),
    ),
)

val allTools: List<ToolItem> = toolCategories.flatMap { it.tools }

/**
 * Applies a user-customized category order (a list of category ids) on top of [toolCategories].
 * Unknown/missing ids from [order] fall back to the default declaration order, and any category
 * id in [order] that no longer exists in the catalog is simply ignored.
 */
fun orderedCategories(order: List<String>): List<ToolCategory> {
    if (order.isEmpty()) return toolCategories
    val byId = toolCategories.associateBy { it.id }
    val ordered = order.mapNotNull { byId[it] }
    val remaining = toolCategories.filter { it.id !in order }
    return ordered + remaining
}
