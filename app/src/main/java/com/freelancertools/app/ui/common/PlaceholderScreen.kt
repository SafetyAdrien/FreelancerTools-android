package com.freelancertools.app.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/** Temporary screen shown for tools not yet implemented in the current build phase. */
@Composable
fun PlaceholderScreen(title: String, icon: ImageVector, navigation: ScaffoldNavigation) {
    ToolScaffold(title = title, icon = icon, navigation = navigation, scrollable = false) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Bientôt disponible",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Cet outil arrive dans une prochaine phase de construction.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
