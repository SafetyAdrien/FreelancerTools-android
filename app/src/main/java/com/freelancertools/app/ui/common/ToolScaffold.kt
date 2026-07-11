package com.freelancertools.app.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/** How the top bar's leading icon behaves: open the drawer (top-level screens) or pop the back stack (nested screens). */
sealed interface ScaffoldNavigation {
    data class OpenDrawer(val onClick: () -> Unit) : ScaffoldNavigation
    data class Back(val onClick: () -> Unit) : ScaffoldNavigation
}

/**
 * Shared layout for every tool/business screen: top bar with icon + title, scrollable content,
 * optional sticky bottom action bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolScaffold(
    title: String,
    icon: ImageVector,
    navigation: ScaffoldNavigation,
    modifier: Modifier = Modifier,
    scrollable: Boolean = true,
    bottomBar: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(title, style = MaterialTheme.typography.titleLarge)
                    }
                },
                navigationIcon = {
                    when (navigation) {
                        is ScaffoldNavigation.OpenDrawer -> IconButton(onClick = navigation.onClick) {
                            Icon(Icons.Rounded.Menu, contentDescription = "Menu")
                        }
                        is ScaffoldNavigation.Back -> IconButton(onClick = navigation.onClick) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Retour")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.background) {
                Column(Modifier.padding(16.dp)) { bottomBar() }
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        val columnModifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .let { if (scrollable) it.verticalScroll(rememberScrollState()) else it }
            .padding(horizontal = 20.dp, vertical = 12.dp)

        Column(
            modifier = columnModifier,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = content,
        )
    }
}
