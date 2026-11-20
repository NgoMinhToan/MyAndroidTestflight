package com.mycompany.testflight.designsystem.components

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop

/**
 * A [TextFieldState] kept in sync with a hoisted [value].
 *
 * The last text we sent upward is remembered so that fast typing is never overwritten by a stale
 * parent value; the field is only rewritten when the parent changes it for another reason (reset,
 * normalization, validation).
 */
@Composable
fun rememberSyncedTextFieldState(
    value: String,
    onValueChange: (String) -> Unit,
): TextFieldState {
    val state = rememberTextFieldState(initialText = value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    var lastEmitted by remember { mutableStateOf(value) }

    LaunchedEffect(value) {
        if (value != lastEmitted && state.text.toString() != value) {
            state.setTextAndPlaceCursorAtEnd(value)
            lastEmitted = value
        }
    }
    LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }
            .drop(1)
            .distinctUntilChanged()
            .collect {
                lastEmitted = it
                currentOnValueChange(it)
            }
    }
    return state
}
