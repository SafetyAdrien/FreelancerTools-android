package com.freelancertools.app.ui.tools.markdownpreview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.SectionTitle
import com.freelancertools.app.ui.common.ToolScaffold

private const val SAMPLE = "# Titre\n\nUn paragraphe avec du **gras** et de l'*italique*.\n\n- Premier point\n- Deuxième point\n\n[Un lien](https://example.com)"

@Composable
fun MarkdownPreviewScreen(navigation: ScaffoldNavigation) {
    var markdown by rememberSaveable { mutableStateOf(SAMPLE) }

    ToolScaffold(
        title = "Markdown Preview",
        icon = Icons.Rounded.Description,
        navigation = navigation,
        onReset = { markdown = SAMPLE },
    ) {
        SectionTitle("Éditeur")
        OutlinedTextField(
            value = markdown,
            onValueChange = { markdown = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
        )

        SectionTitle("Aperçu")
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                markdown.split("\n").forEach { line -> MarkdownLine(line) }
            }
        }
    }
}

@Composable
private fun MarkdownLine(line: String) {
    when {
        line.startsWith("### ") -> Text(inlineStyled(line.removePrefix("### ")), style = MaterialTheme.typography.titleMedium)
        line.startsWith("## ") -> Text(inlineStyled(line.removePrefix("## ")), style = MaterialTheme.typography.titleLarge)
        line.startsWith("# ") -> Text(inlineStyled(line.removePrefix("# ")), style = MaterialTheme.typography.headlineMedium)
        line.startsWith("- ") || line.startsWith("* ") -> Row {
            Text("•  ", style = MaterialTheme.typography.bodyLarge)
            Text(inlineStyled(line.drop(2)), style = MaterialTheme.typography.bodyLarge)
        }
        line.isBlank() -> Text(" ", fontSize = 4.sp)
        else -> Text(inlineStyled(line), style = MaterialTheme.typography.bodyLarge)
    }
}

private val linkRegex = Regex("""\[([^\]]+)]\(([^)]+)\)""")
private val boldRegex = Regex("""\*\*([^*]+)\*\*""")
private val italicRegex = Regex("""(?<!\*)\*([^*]+)\*(?!\*)""")

private fun inlineStyled(raw: String) = buildAnnotatedString {
    // Replace links first with placeholder text (kept simple: rendered underlined, not clickable).
    var text = raw
    val links = mutableListOf<Pair<String, String>>()
    text = linkRegex.replace(text) { match ->
        links += match.groupValues[1] to match.groupValues[2]
        " ${links.size - 1} "
    }

    var index = 0
    while (index < text.length) {
        val remaining = text.substring(index)
        val boldMatch = boldRegex.find(remaining)
        val italicMatch = italicRegex.find(remaining)
        val linkMatch = Regex(""" (\d+) """).find(remaining)

        val candidates = listOfNotNull(
            boldMatch?.let { Triple(it.range.first, "bold", it) },
            italicMatch?.let { Triple(it.range.first, "italic", it) },
            linkMatch?.let { Triple(it.range.first, "link", it) },
        )
        val next = candidates.minByOrNull { it.first }

        if (next == null) {
            append(remaining)
            break
        }
        if (next.first > 0) append(remaining.substring(0, next.first))

        when (next.second) {
            "bold" -> {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(next.third.groupValues[1]) }
            }
            "italic" -> {
                withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(next.third.groupValues[1]) }
            }
            "link" -> {
                val (label, _) = links[next.third.groupValues[1].toInt()]
                withStyle(SpanStyle(color = androidx.compose.ui.graphics.Color(0xFF3B82F6))) { append(label) }
            }
        }
        index += next.third.range.last + 1
    }
}
