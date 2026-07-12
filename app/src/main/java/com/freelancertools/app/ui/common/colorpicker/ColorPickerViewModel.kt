package com.freelancertools.app.ui.common.colorpicker

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelancertools.app.data.local.datastore.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ColorPickerViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
) : ViewModel() {

    val customGlobalColors: StateFlow<List<Color>> = preferencesManager.globalColors
        .map { hexList -> hexList.mapNotNull { parseHexColorOrNull(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun addGlobalColor(color: Color) {
        viewModelScope.launch { preferencesManager.addGlobalColor(color.toHexRgb()) }
    }
}
