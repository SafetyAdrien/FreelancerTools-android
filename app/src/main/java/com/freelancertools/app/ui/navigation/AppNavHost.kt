package com.freelancertools.app.ui.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
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
import com.freelancertools.app.ui.settings.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun FreelancerToolsApp() {
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
                SettingsScreen(navigation = topLevel)
            }

            registerBusinessRoutes(navController, topLevel, ::push)
            registerToolRoutes(topLevel)
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

private fun NavGraphBuilder.registerToolRoutes(topLevel: ScaffoldNavigation) {
    allTools.forEach { tool ->
        composable(tool.route) {
            PlaceholderScreen(title = tool.title, icon = tool.icon, navigation = topLevel)
        }
    }
}
