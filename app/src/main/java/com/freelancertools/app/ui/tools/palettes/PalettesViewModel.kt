package com.freelancertools.app.ui.tools.palettes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freelancertools.app.data.local.db.entity.Palette
import com.freelancertools.app.data.repository.PaletteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject

private val json = Json { ignoreUnknownKeys = true }

@HiltViewModel
class PalettesViewModel @Inject constructor(
    private val paletteRepository: PaletteRepository,
) : ViewModel() {

    val savedPalettes: StateFlow<List<Palette>> = paletteRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun savePalette(name: String, colors: List<String>) {
        viewModelScope.launch {
            paletteRepository.save(
                Palette(
                    id = UUID.randomUUID().toString(),
                    name = name.ifBlank { "Nouvelle Palette" },
                    colors = json.encodeToString(colors),
                    createdAt = System.currentTimeMillis(),
                ),
            )
        }
    }
}

fun Palette.colorList(): List<String> =
    runCatching { json.decodeFromString<List<String>>(colors) }.getOrDefault(emptyList())
