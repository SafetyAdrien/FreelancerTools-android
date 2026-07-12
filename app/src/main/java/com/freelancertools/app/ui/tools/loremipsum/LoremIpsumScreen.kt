package com.freelancertools.app.ui.tools.loremipsum

import android.content.ClipData
import android.content.ClipboardManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.ToolScaffold
import kotlin.random.Random

private enum class LoremType(val label: String) {
    PARAGRAPHS("Paragraphes"),
    SENTENCES("Phrases"),
    WORDS("Mots"),
}

private val WORD_BANK = listOf(
    "lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing", "elit", "sed", "do",
    "eiusmod", "tempor", "incididunt", "ut", "labore", "et", "dolore", "magna", "aliqua", "enim",
    "ad", "minim", "veniam", "quis", "nostrud", "exercitation", "ullamco", "laboris", "nisi",
    "aliquip", "ex", "ea", "commodo", "consequat", "duis", "aute", "irure", "in", "reprehenderit",
    "voluptate", "velit", "esse", "cillum", "eu", "fugiat", "nulla", "pariatur", "excepteur", "sint",
    "occaecat", "cupidatat", "non", "proident", "sunt", "culpa", "qui", "officia", "deserunt", "mollit",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoremIpsumScreen(navigation: ScaffoldNavigation) {
    var type by rememberSaveable { mutableStateOf(LoremType.PARAGRAPHS) }
    var quantity by remember { mutableFloatStateOf(3f) }
    var startWithLorem by rememberSaveable { mutableStateOf(true) }
    var result by rememberSaveable { mutableStateOf("") }
    val clipboard = LocalContext.current.getSystemService(ClipboardManager::class.java)

    ToolScaffold(
        title = "Lorem Ipsum",
        icon = Icons.AutoMirrored.Rounded.Notes,
        navigation = navigation,
        onReset = { type = LoremType.PARAGRAPHS; quantity = 3f; startWithLorem = true; result = "" },
        bottomBar = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryActionButton(
                    label = "Générer",
                    onClick = {
                        result = runCatching { generateLorem(type, quantity.toInt().coerceIn(1, 50), startWithLorem) }
                            .getOrElse { "Erreur lors de la génération, réessayez." }
                    },
                )
                if (result.isNotBlank()) {
                    PrimaryActionButton(
                        label = "Copier",
                        icon = Icons.Rounded.ContentCopy,
                        onClick = {
                            clipboard?.setPrimaryClip(ClipData.newPlainText("Lorem Ipsum", result))
                        },
                    )
                }
            }
        },
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("Type", style = MaterialTheme.typography.titleMedium)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    LoremType.entries.forEachIndexed { index, t ->
                        SegmentedButton(
                            selected = type == t,
                            onClick = { type = t },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = LoremType.entries.size),
                        ) {
                            Text(t.label)
                        }
                    }
                }

                Text("Quantité : ${quantity.toInt()}", style = MaterialTheme.typography.titleMedium)
                Slider(
                    value = quantity,
                    onValueChange = { quantity = it },
                    valueRange = 1f..50f,
                    steps = 48,
                )

                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Checkbox(checked = startWithLorem, onCheckedChange = { startWithLorem = it })
                    Text("Commencer par \"Lorem ipsum...\"", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (result.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.large,
            ) {
                Text(
                    text = result,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}

private val LOREM_OPENER = listOf("lorem", "ipsum", "dolor", "sit", "amet", "consectetur")

private fun generateLorem(type: LoremType, quantity: Int, startWithLorem: Boolean): String {
    fun randomWord() = WORD_BANK[Random.nextInt(WORD_BANK.size)]
    fun capitalize(s: String) = s.replaceFirstChar { it.uppercase() }

    fun words(count: Int, forceOpener: Boolean): List<String> {
        val list = (0 until count).map { randomWord() }.toMutableList()
        if (forceOpener) {
            for (i in LOREM_OPENER.indices) {
                if (i < list.size) list[i] = LOREM_OPENER[i]
            }
        }
        return list
    }

    fun sentence(wordCount: Int = Random.nextInt(6, 14), forceOpener: Boolean = false): String =
        capitalize(words(wordCount, forceOpener).joinToString(" ")) + "."

    fun paragraph(sentenceCount: Int = Random.nextInt(4, 8), forceOpener: Boolean = false): String =
        (0 until sentenceCount).joinToString(" ") { index -> sentence(forceOpener = forceOpener && index == 0) }

    return when (type) {
        LoremType.WORDS -> capitalize(words(quantity, startWithLorem).joinToString(" "))
        LoremType.SENTENCES -> (0 until quantity).joinToString(" ") { index ->
            sentence(forceOpener = startWithLorem && index == 0)
        }
        LoremType.PARAGRAPHS -> (0 until quantity).joinToString("\n\n") { index ->
            paragraph(forceOpener = startWithLorem && index == 0)
        }
    }
}
