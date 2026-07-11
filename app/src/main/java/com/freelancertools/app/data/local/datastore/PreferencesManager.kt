package com.freelancertools.app.data.local.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.freelancertools.app.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val USER_NAME = stringPreferencesKey("user_name")
        val COMPANY_NAME = stringPreferencesKey("company_name")
        val LAST_TOOL_ROUTE = stringPreferencesKey("last_tool_route")
        val COLLAPSED_SECTIONS = stringSetPreferencesKey("collapsed_sections")
        val ONBOARDED = booleanPreferencesKey("onboarded")
    }

    val themeMode: Flow<AppThemeMode> = dataStore.data.map { prefs ->
        when (prefs[Keys.THEME_MODE]) {
            "DARK" -> AppThemeMode.DARK
            "LIGHT" -> AppThemeMode.LIGHT
            "SYSTEM" -> AppThemeMode.SYSTEM
            else -> AppThemeMode.DARK
        }
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    val userName: Flow<String> = dataStore.data.map { it[Keys.USER_NAME] ?: "" }
    suspend fun setUserName(name: String) {
        dataStore.edit { it[Keys.USER_NAME] = name }
    }

    val companyName: Flow<String> = dataStore.data.map { it[Keys.COMPANY_NAME] ?: "" }
    suspend fun setCompanyName(name: String) {
        dataStore.edit { it[Keys.COMPANY_NAME] = name }
    }

    val collapsedSections: Flow<Set<String>> = dataStore.data.map {
        it[Keys.COLLAPSED_SECTIONS] ?: emptySet()
    }

    suspend fun setSectionCollapsed(sectionId: String, collapsed: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.COLLAPSED_SECTIONS]?.toMutableSet() ?: mutableSetOf()
            if (collapsed) current.add(sectionId) else current.remove(sectionId)
            prefs[Keys.COLLAPSED_SECTIONS] = current
        }
    }
}
