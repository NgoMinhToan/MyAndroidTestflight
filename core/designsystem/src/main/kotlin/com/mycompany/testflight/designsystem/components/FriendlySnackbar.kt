package com.mycompany.testflight.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mycompany.testflight.designsystem.tokens.FriendlyType
import com.mycompany.testflight.designsystem.tokens.TestflightColors

/** Navy toast-style snackbar used above the bottom navigation on every screen. */
@Composable
fun FriendlySnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        Snackbar(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            shape = RoundedCornerShape(14.dp),
            containerColor = TestflightColors.Navy,
            contentColor = TestflightColors.White,
        ) {
            Text(
                text = data.visuals.message,
                style = FriendlyType.Snackbar,
                color = TestflightColors.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
