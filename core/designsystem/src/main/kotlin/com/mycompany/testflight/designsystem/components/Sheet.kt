package com.mycompany.testflight.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mycompany.testflight.designsystem.tokens.FriendlyDimens
import com.mycompany.testflight.designsystem.tokens.TestflightColors

val SheetShape = RoundedCornerShape(
    topStart = FriendlyDimens.SheetCorner,
    topEnd = FriendlyDimens.SheetCorner,
)

@Composable
fun SheetSurface(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = SheetShape,
        color = TestflightColors.White,
        contentColor = TestflightColors.Navy,
        shadowElevation = 8.dp,
    ) {
        Column(content = content)
    }
}

@Composable
fun HandleBar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(width = 36.dp, height = 4.dp)
            .clip(RoundedCornerShape(50))
            .background(TestflightColors.Handle),
    )
}

/**
 * Paints [color] a little past the header's bottom edge so the rounded top corners of the
 * sheet reveal the header color instead of the root background, without negative margins.
 * The lambda overload keeps animated color reads in the draw phase.
 */
fun Modifier.headerBleed(
    color: Color,
    extra: Dp = FriendlyDimens.HeaderBleed,
): Modifier = headerBleed(color = { color }, extra = extra)

fun Modifier.headerBleed(
    color: () -> Color,
    extra: Dp = FriendlyDimens.HeaderBleed,
): Modifier = drawBehind {
    drawRect(
        color = color(),
        size = Size(size.width, size.height + extra.toPx()),
    )
}

/**
 * Dissolves scrolling sheet content into the sheet background at its top edge instead of
 * leaving a hard cut under the handle. The white veil fades in with [scrollOffsetPx], which
 * is read in the draw phase so scrolling never triggers recomposition.
 */
fun Modifier.sheetTopFade(
    scrollOffsetPx: () -> Float,
    fadeHeight: Dp = 22.dp,
    revealDistance: Dp = 10.dp,
    color: Color = Color.White,
): Modifier = drawWithCache {
    // Cache the gradient while scrolling: only its opacity changes, which drawRect applies
    // without rebuilding the brush every frame.
    val revealPx = revealDistance.toPx()
    val brush = Brush.verticalGradient(
        colors = listOf(color, color.copy(alpha = 0f)),
        startY = 0f,
        endY = fadeHeight.toPx(),
    )
    onDrawWithContent {
        drawContent()
        val strength = (scrollOffsetPx() / revealPx).coerceIn(0f, 1f)
        if (strength > 0f) {
            drawRect(brush = brush, alpha = strength)
        }
    }
}
