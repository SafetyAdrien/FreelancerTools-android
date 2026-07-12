package com.freelancertools.app.ui.common

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop

/**
 * A text field backed by an async source of truth (DataStore/Room) that avoids the classic
 * "cursor jumps back one letter" bug: syncing [sourceValue] straight into a Compose `String`
 * `value` re-seeds the field (and resets the cursor) on every recomposition the source emits,
 * including the echo of the app's own write. Here, local edits keep local state authoritative
 * from the moment the user types; the source only seeds the field before that point, and writes
 * are debounced instead of firing (and racing back) on every keystroke.
 */
@Composable
fun SyncedOutlinedTextField(
    sourceValue: String,
    onValueCommit: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    prefix: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    debounceMillis: Long = 500,
) {
    var fieldValue by remember { mutableStateOf(TextFieldValue(sourceValue, TextRange(sourceValue.length))) }
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(sourceValue) {
        if (!isEditing && sourceValue != fieldValue.text) {
            fieldValue = TextFieldValue(sourceValue, TextRange(sourceValue.length))
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { fieldValue.text }
            .drop(1)
            .debounce(debounceMillis)
            .collectLatest { onValueCommit(it) }
    }

    OutlinedTextField(
        value = fieldValue,
        onValueChange = {
            isEditing = true
            fieldValue = it
        },
        label = { Text(label) },
        leadingIcon = leadingIcon,
        prefix = prefix,
        singleLine = singleLine,
        modifier = modifier,
    )
}
