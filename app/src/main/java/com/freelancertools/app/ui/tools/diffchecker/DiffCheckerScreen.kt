package com.freelancertools.app.ui.tools.diffchecker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CompareArrows
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import com.freelancertools.app.ui.common.PrimaryActionButton
import com.freelancertools.app.ui.common.ScaffoldNavigation
import com.freelancertools.app.ui.common.SectionTitle
import com.freelancertools.app.ui.common.ToolScaffold
import com.freelancertools.app.ui.theme.SuccessGreen

private sealed interface DiffLine {
    data class Same(val text: String) : DiffLine
    data class Added(val text: String) : DiffLine
    data class Removed(val text: String) : DiffLine
}

@Composable
fun DiffCheckerScreen(navigation: ScaffoldNavigation) {
    var before by rememberSaveable { mutableStateOf("") }
    var after by rememberSaveable { mutableStateOf("") }
    var diff by rememberSaveable { mutableStateOf<List<Pair<Int, String>>?>(null) }
    // Pair encodes: type ordinal (0 same, 1 added, 2 removed) to line text.

    ToolScaffold(
        title = "Diff Checker",
        icon = Icons.AutoMirrored.Rounded.CompareArrows,
        navigation = navigation,
        onReset = { before = ""; after = ""; diff = null },
        bottomBar = {
            PrimaryActionButton(
                label = "Comparer",
                onClick = { diff = computeDiff(before, after).map { it.typeOrdinal() to it.text() } },
            )
        },
    ) {
        SectionTitle("Avant")
        OutlinedTextField(
            value = before,
            onValueChange = { before = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
        )
        SectionTitle("Après")
        OutlinedTextField(
            value = after,
            onValueChange = { after = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
        )

        diff?.let { lines ->
            SectionTitle("Résultat")
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = MaterialTheme.shapes.large,
            ) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    lines.forEach { (typeOrdinal, text) ->
                        val bg = when (typeOrdinal) {
                            1 -> SuccessGreen.copy(alpha = 0.18f)
                            2 -> MaterialTheme.colorScheme.error.copy(alpha = 0.18f)
                            else -> androidx.compose.ui.graphics.Color.Transparent
                        }
                        val prefix = when (typeOrdinal) { 1 -> "+ "; 2 -> "- "; else -> "  " }
                        Text(
                            text = prefix + text.ifBlank { " " },
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(bg)
                                .padding(vertical = 2.dp, horizontal = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

private fun DiffLine.typeOrdinal() = when (this) {
    is DiffLine.Same -> 0
    is DiffLine.Added -> 1
    is DiffLine.Removed -> 2
}

private fun DiffLine.text() = when (this) {
    is DiffLine.Same -> text
    is DiffLine.Added -> text
    is DiffLine.Removed -> text
}

/** Classic LCS-based line diff. */
private fun computeDiff(before: String, after: String): List<DiffLine> {
    val a = before.split("\n")
    val b = after.split("\n")
    val n = a.size
    val m = b.size
    val lcs = Array(n + 1) { IntArray(m + 1) }
    for (i in n - 1 downTo 0) {
        for (j in m - 1 downTo 0) {
            lcs[i][j] = if (a[i] == b[j]) lcs[i + 1][j + 1] + 1 else maxOf(lcs[i + 1][j], lcs[i][j + 1])
        }
    }

    val result = mutableListOf<DiffLine>()
    var i = 0
    var j = 0
    while (i < n && j < m) {
        when {
            a[i] == b[j] -> {
                result += DiffLine.Same(a[i])
                i++; j++
            }
            lcs[i + 1][j] >= lcs[i][j + 1] -> {
                result += DiffLine.Removed(a[i])
                i++
            }
            else -> {
                result += DiffLine.Added(b[j])
                j++
            }
        }
    }
    while (i < n) { result += DiffLine.Removed(a[i]); i++ }
    while (j < m) { result += DiffLine.Added(b[j]); j++ }
    return result
}
