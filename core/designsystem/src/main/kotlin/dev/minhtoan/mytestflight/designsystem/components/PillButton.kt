package dev.minhtoan.mytestflight.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.minhtoan.mytestflight.designsystem.tokens.FriendlyType
import dev.minhtoan.mytestflight.designsystem.tokens.PillShape
import dev.minhtoan.mytestflight.designsystem.tokens.TestflightColors

enum class PillStyle { Primary, Navy, Cream, Dim }

/**
 * Fully rounded pill. The visual pill is compact while the touch target is at least 48dp.
 * [useDisabledColors] lets callers keep the normal palette and dim it themselves (busy rows).
 */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PillStyle = PillStyle.Primary,
    enabled: Boolean = true,
    useDisabledColors: Boolean = !enabled,
    textStyle: androidx.compose.ui.text.TextStyle = FriendlyType.PillLabel,
    contentDescription: String? = null,
) {
    val isDim = style == PillStyle.Dim
    val baseContainer = when (style) {
        PillStyle.Primary -> TestflightColors.CoralStrong
        PillStyle.Navy -> TestflightColors.Navy
        PillStyle.Cream -> TestflightColors.Cream
        PillStyle.Dim -> TestflightColors.DisabledFill
    }
    val baseContent = when (style) {
        PillStyle.Primary, PillStyle.Navy -> TestflightColors.White
        PillStyle.Cream -> TestflightColors.Navy
        PillStyle.Dim -> TestflightColors.DisabledText
    }
    val container: Color
    val content: Color
    if (!enabled && useDisabledColors && !isDim) {
        container = TestflightColors.ButtonDisabledFill
        content = TestflightColors.ButtonDisabledText
    } else {
        container = baseContainer
        content = baseContent
    }

    CompositionLocalProvider(LocalContentColor provides content) {
        Box(
            modifier = modifier
                .minimumInteractiveComponentSize()
                .clip(PillShape)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .semantics {
                    if (contentDescription != null) {
                        this.contentDescription = contentDescription
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(container)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = text,
                    style = textStyle,
                    color = content,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
