package com.freelancertools.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelancertools.app.data.local.datastore.PreferencesManager
import com.freelancertools.app.ui.theme.AppThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val userName: String = "",
    val companyName: String = "",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    val themeMode: StateFlow<AppThemeMode> = preferencesManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppThemeMode.DARK)

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesManager.themeMode,
        preferencesManager.userName,
        preferencesManager.companyName,
    ) { theme, userName, companyName ->
        SettingsUiState(theme, userName, companyName)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch { preferencesManager.setThemeMode(mode) }
    }

    fun setUserName(name: String) {
        viewModelScope.launch { preferencesManager.setUserName(name) }
    }

    fun setCompanyName(name: String) {
        viewModelScope.launch { preferencesManager.setCompanyName(name) }
    }
}
