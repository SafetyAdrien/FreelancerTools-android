package com.freelancertools.app.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.freelancertools.app.ui.clients.ClientDetailScreen
import com.freelancertools.app.ui.clients.ClientsScreen
import com.freelancertools.app.ui.common.PlaceholderScreen
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.dashboard.DashboardScreen
import com.freelancertools.app.ui.finances.FinancesScreen
import com.freelancertools.app.ui.invoices.InvoiceEditorScreen
import com.freelancertools.app.ui.invoices.InvoicesScreen
import com.freelancertools.app.ui.managetools.ManageToolsScreen
import com.freelancertools.app.ui.settings.SettingsScreen
import com.freelancertools.app.ui.tools.backgroundremoval.BackgroundRemovalScreen
import com.freelancertools.app.ui.tools.blobmaker.BlobMakerScreen
import com.freelancertools.app.ui.tools.colorpickers.ColorPickersScreen
import com.freelancertools.app.ui.tools.converter.ConverterScreen
import com.freelancertools.app.ui.tools.designsystem.DesignSystemCategoryScreen
import com.freelancertools.app.ui.tools.designsystem.DesignSystemScreen
import com.freelancertools.app.ui.tools.designsystem.DiscordDesignScreen
import com.freelancertools.app.ui.tools.diffchecker.DiffCheckerScreen
import com.freelancertools.app.ui.tools.embedvisualizer.EmbedVisualizerScreen
import com.freelancertools.app.ui.tools.exifcleaner.ExifCleanerScreen
import com.freelancertools.app.ui.tools.favicongenerator.FaviconGeneratorScreen
import com.freelancertools.app.ui.tools.fontlibrary.FontLibraryScreen
import com.freelancertools.app.ui.tools.fontpairer.FontPairerScreen
import com.freelancertools.app.ui.tools.imageoptimizer.ImageOptimizerScreen
import com.freelancertools.app.ui.tools.loremipsum.LoremIpsumScreen
import com.freelancertools.app.ui.tools.markdownpreview.MarkdownPreviewScreen
import com.freelancertools.app.ui.tools.metatags.MetaTagsScreen
import com.freelancertools.app.ui.tools.palettes.PalettesScreen
import com.freelancertools.app.ui.tools.promptmanager.PromptManagerScreen
import com.freelancertools.app.ui.tools.qrcodes.QrCodesScreen
import com.freelancertools.app.ui.tools.roicalculator.RoiCalculatorScreen
import com.freelancertools.app.ui.tools.scalecalculator.ScaleCalculatorScreen
import com.freelancertools.app.ui.tools.smarttimer.SmartTimerScreen
import com.freelancertools.app.ui.tools.speedtest.SpeedTestScreen
import com.freelancertools.app.ui.tools.videocutter.VideoCutterScreen
import com.freelancertools.app.ui.tools.watermark.WatermarkScreen
import com.freelancertools.app.ui.tools.whois.WhoisScreen
import kotlinx.coroutines.launch

@Composable
fun FreelancerToolsApp(startRoute: String? = null) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    fun openDrawer() = scope.launch { drawerState.open() }
    fun closeDrawer() = scope.launch { drawerState.close() }

    fun navigateTopLevel(route: String) {
        closeDrawer()
        navController.navigate(route) {
            popUpTo(Routes.DASHBOARD) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun push(route: String) {
        navController.navigate(route)
    }

    LaunchedEffect(startRoute) {
        if (!startRoute.isNullOrBlank()) {
            navController.navigate(startRoute) {
                popUpTo(Routes.DASHBOARD) { saveState = true }
                launchSingleTop = true
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(currentRoute = currentRoute, onNavigate = ::navigateTopLevel)
        },
    ) {
        val topLevel = ScaffoldNavigation.OpenDrawer { openDrawer() }

        NavHost(navController = navController, startDestination = Routes.DASHBOARD, modifier = Modifier) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(navigation = topLevel, onNavigate = ::navigateTopLevel)
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(navigation = topLevel, onManageTools = { push(Routes.MANAGE_TOOLS) })
            }
            composable(Routes.MANAGE_TOOLS) {
                ManageToolsScreen(onBack = { navController.popBackStack() })
            }

            registerBusinessRoutes(navController, topLevel, ::push)
            registerToolRoutes(navController, topLevel, ::push)
        }
    }
}

private fun NavGraphBuilder.registerBusinessRoutes(
    navController: NavHostController,
    topLevel: ScaffoldNavigation,
    push: (String) -> Unit,
) {
    composable(Routes.FINANCES) {
        FinancesScreen(navigation = topLevel)
    }
    composable(Routes.CLIENTS) {
        ClientsScreen(
            navigation = topLevel,
            onOpenClient = { clientId -> push(Routes.clientDetail(clientId)) },
        )
    }
    composable(Routes.CLIENT_DETAIL) {
        ClientDetailScreen(onBack = { navController.popBackStack() })
    }
    composable(Routes.INVOICES) {
        InvoicesScreen(
            navigation = topLevel,
            onNewInvoice = { push(Routes.invoiceEditor()) },
        )
    }
    composable(Routes.INVOICE_EDITOR) {
        InvoiceEditorScreen(onBack = { navController.popBackStack() })
    }
}

private fun NavGraphBuilder.registerToolRoutes(
    navController: NavHostController,
    topLevel: ScaffoldNavigation,
    push: (String) -> Unit,
) {
    composable(Routes.ROI_CALCULATOR) { RoiCalculatorScreen(topLevel) }
    composable(Routes.META_TAGS) { MetaTagsScreen(topLevel) }
    composable(Routes.MARKDOWN_PREVIEW) { MarkdownPreviewScreen(topLevel) }
    composable(Routes.EMBED_VISUALIZER) { EmbedVisualizerScreen(topLevel) }
    composable(Routes.DIFF_CHECKER) { DiffCheckerScreen(topLevel) }
    composable(Routes.CONVERTER) { ConverterScreen(topLevel) }
    composable(Routes.PALETTES) { PalettesScreen(topLevel) }
    composable(Routes.FONT_PAIRER) { FontPairerScreen(topLevel) }
    composable(Routes.SCALE_CALCULATOR) { ScaleCalculatorScreen(topLevel) }
    composable(Routes.BLOB_MAKER) { BlobMakerScreen(topLevel) }
    composable(Routes.LOREM_IPSUM) { LoremIpsumScreen(topLevel) }
    composable(Routes.PROMPT_MANAGER) { PromptManagerScreen(topLevel) }
    composable(Routes.COLOR_PICKERS) { ColorPickersScreen(topLevel) }
    composable(Routes.DESIGN_SYSTEM) {
        DesignSystemScreen(
            navigation = topLevel,
            onOpenCategory = { categoryId -> push(Routes.designSystemCategory(categoryId)) },
        )
    }
    composable(Routes.DESIGN_SYSTEM_CATEGORY) { backStackEntry ->
        val categoryId = backStackEntry.arguments?.getString("categoryId").orEmpty()
        val onBack: () -> Unit = { navController.popBackStack() }
        if (categoryId == "discord") {
            DiscordDesignScreen(onBack = onBack)
        } else {
            DesignSystemCategoryScreen(categoryId = categoryId, onBack = onBack)
        }
    }

    composable(Routes.QR_CODES) { QrCodesScreen(topLevel) }
    composable(Routes.IMAGE_OPTIMIZER) { ImageOptimizerScreen(topLevel) }
    composable(Routes.EXIF_CLEANER) { ExifCleanerScreen(topLevel) }
    composable(Routes.FAVICON_GENERATOR) { FaviconGeneratorScreen(topLevel) }
    composable(Routes.WATERMARK) { WatermarkScreen(topLevel) }
    composable(Routes.FONT_LIBRARY) { FontLibraryScreen(topLevel) }

    composable(Routes.BACKGROUND_REMOVAL) { BackgroundRemovalScreen(topLevel) }
    composable(Routes.VIDEO_CUTTER) { VideoCutterScreen(topLevel) }
    composable(Routes.SMART_TIMER) { SmartTimerScreen(topLevel) }

    composable(Routes.WHOIS_LOOKUP) { WhoisScreen(topLevel) }
    composable(Routes.SPEED_TEST) { SpeedTestScreen(topLevel) }

    val implementedRoutes = setOf(
        // Business routes: registered separately by registerBusinessRoutes(), but their
        // ToolItem entries live in toolCategories (allTools) since the Sprint 2 drawer reorg —
        // without listing them here, this catch-all re-registers a second composable() for the
        // same route *after* the real one, silently shadowing FinancesScreen/ClientsScreen/
        // InvoicesScreen with an empty PlaceholderScreen ("Bientôt disponible").
        Routes.FINANCES, Routes.CLIENTS, Routes.INVOICES,
        Routes.ROI_CALCULATOR, Routes.META_TAGS, Routes.MARKDOWN_PREVIEW, Routes.EMBED_VISUALIZER,
        Routes.DIFF_CHECKER, Routes.CONVERTER, Routes.PALETTES, Routes.FONT_PAIRER,
        Routes.SCALE_CALCULATOR, Routes.BLOB_MAKER, Routes.LOREM_IPSUM, Routes.PROMPT_MANAGER,
        Routes.COLOR_PICKERS, Routes.DESIGN_SYSTEM,
        Routes.QR_CODES, Routes.IMAGE_OPTIMIZER, Routes.EXIF_CLEANER, Routes.FAVICON_GENERATOR,
        Routes.WATERMARK, Routes.FONT_LIBRARY, Routes.BACKGROUND_REMOVAL, Routes.VIDEO_CUTTER,
        Routes.SMART_TIMER, Routes.WHOIS_LOOKUP, Routes.SPEED_TEST,
    )
    allTools.filter { it.route !in implementedRoutes }.forEach { tool ->
        composable(tool.route) {
            PlaceholderScreen(title = tool.title, icon = tool.icon, navigation = topLevel)
        }
    }
}
