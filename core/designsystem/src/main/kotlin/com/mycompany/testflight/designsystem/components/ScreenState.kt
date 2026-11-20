package com.mycompany.testflight.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mycompany.testflight.designsystem.tokens.FriendlyType
import com.mycompany.testflight.designsystem.tokens.TestflightColors
import java.util.Locale

/** Centered loading / error / empty / no-results panel used inside the white sheet. */
@Composable
fun ScreenState(
    kicker: String,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = kicker.uppercase(Locale.getDefault()),
            style = FriendlyType.Overline,
            color = TestflightColors.CoralStrong,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(TestflightColors.Cream)
                .padding(horizontal = 10.dp, vertical = 5.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            style = FriendlyType.StateTitle,
            color = TestflightColors.Navy,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(7.dp))
        Text(
            text = body,
            style = FriendlyType.Body,
            color = TestflightColors.Muted,
            textAlign = TextAlign.Center,
        )
        if (actions != null) {
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions,
            )
        }
    }
}
