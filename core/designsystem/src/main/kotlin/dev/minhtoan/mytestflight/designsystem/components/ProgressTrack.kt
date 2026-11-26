package dev.minhtoan.mytestflight.designsystem.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.progressSemantics
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import dev.minhtoan.mytestflight.designsystem.tokens.TestflightColors

/**
 * 2dp install progress line. At 0 (or when [progress] is null) only the track is visible, so the
 * same composable doubles as the list divider. The animated value is read inside [drawBehind], so
 * progress ticks update the draw phase without recomposing.
 */
@Composable
fun ProgressTrack(
    progress: Float?,
    modifier: Modifier = Modifier,
) {
    val target = (progress ?: 0f).coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = target,
        animationSpec = tween(durationMillis = 220, easing = LinearEasing),
        label = "installProgress",
    )
    val semantics = if (progress != null) {
        Modifier.progressSemantics(target)
    } else {
        Modifier
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .clip(RoundedCornerShape(2.dp))
            .then(semantics)
            .drawBehind {
                drawRect(color = TestflightColors.Track)
                drawRect(
                    color = TestflightColors.CoralStrong,
                    size = Size(width = size.width * animated, height = size.height),
                )
            },
    )
}
