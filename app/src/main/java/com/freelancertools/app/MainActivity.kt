package com.freelancertools.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.freelancertools.app.ui.navigation.FreelancerToolsApp
import com.freelancertools.app.ui.settings.SettingsViewModel
import com.freelancertools.app.ui.theme.FreelancerToolsTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            val themeMode by settingsViewModel.themeMode.collectAsStateWithLifecycle()

            FreelancerToolsTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    FreelancerToolsApp(startRoute = intent.getStringExtra(EXTRA_SHORTCUT_ROUTE))
                }
            }
        }
    }

    companion object {
        const val EXTRA_SHORTCUT_ROUTE = "shortcut_route"
    }
}
