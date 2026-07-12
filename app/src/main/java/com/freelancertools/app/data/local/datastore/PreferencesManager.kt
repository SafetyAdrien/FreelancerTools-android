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
        val ACCOUNT_USERNAME = stringPreferencesKey("account_username")
        val LAST_TOOL_ROUTE = stringPreferencesKey("last_tool_route")
        val COLLAPSED_SECTIONS = stringSetPreferencesKey("collapsed_sections")
        val HIDDEN_TOOLS = stringSetPreferencesKey("hidden_tools")
        val CATEGORY_ORDER = stringPreferencesKey("category_order")
        val CLIENT_SORT = stringPreferencesKey("client_sort")
        val GLOBAL_COLORS = stringPreferencesKey("global_colors")
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

    /**
     * Displayed everywhere in the app (dashboard greeting, headers) except on exported invoices,
     * which always use [userName]/[companyName] — a pseudo has no place on an official document.
     */
    val accountUsername: Flow<String> = dataStore.data.map { it[Keys.ACCOUNT_USERNAME] ?: "" }
    suspend fun setAccountUsername(name: String) {
        dataStore.edit { it[Keys.ACCOUNT_USERNAME] = name }
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

    /** IDs of tools hidden from the drawer via Paramètres > Gérer les outils. */
    val hiddenTools: Flow<Set<String>> = dataStore.data.map { it[Keys.HIDDEN_TOOLS] ?: emptySet() }

    suspend fun setToolHidden(toolId: String, hidden: Boolean) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.HIDDEN_TOOLS]?.toMutableSet() ?: mutableSetOf()
            if (hidden) current.add(toolId) else current.remove(toolId)
            prefs[Keys.HIDDEN_TOOLS] = current
        }
    }

    /** User-customized drawer category order (category ids, comma-joined). Empty = default order. */
    val categoryOrder: Flow<List<String>> = dataStore.data.map { prefs ->
        prefs[Keys.CATEGORY_ORDER]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun setCategoryOrder(order: List<String>) {
        dataStore.edit { it[Keys.CATEGORY_ORDER] = order.joinToString(",") }
    }

    /** Persisted Clients list sort order (name of a ClientSort enum entry). */
    val clientSort: Flow<String> = dataStore.data.map { it[Keys.CLIENT_SORT] ?: "NAME_ASC" }

    suspend fun setClientSort(value: String) {
        dataStore.edit { it[Keys.CLIENT_SORT] = value }
    }

    /** User-added custom swatches (hex strings) appended to the ModernColorPicker's "Global Colors" grid. */
    val globalColors: Flow<List<String>> = dataStore.data.map { prefs ->
        prefs[Keys.GLOBAL_COLORS]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun addGlobalColor(hex: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.GLOBAL_COLORS]?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            if (hex !in current) {
                prefs[Keys.GLOBAL_COLORS] = (current + hex).joinToString(",")
            }
        }
    }
}
