package com.freelancertools.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelancertools.app.data.backup.BackupManager
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
    val accountUsername: String = "",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val backupManager: BackupManager,
) : ViewModel() {

    val themeMode: StateFlow<AppThemeMode> = preferencesManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppThemeMode.DARK)

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesManager.themeMode,
        preferencesManager.userName,
        preferencesManager.companyName,
        preferencesManager.accountUsername,
    ) { theme, userName, companyName, accountUsername ->
        SettingsUiState(theme, userName, companyName, accountUsername)
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

    fun setAccountUsername(name: String) {
        viewModelScope.launch { preferencesManager.setAccountUsername(name) }
    }

    fun exportData(onResult: (String) -> Unit) {
        viewModelScope.launch { onResult(backupManager.exportJson()) }
    }

    fun importData(content: String, onDone: (success: Boolean) -> Unit) {
        viewModelScope.launch {
            val success = runCatching { backupManager.importJson(content) }.isSuccess
            onDone(success)
        }
    }
}
