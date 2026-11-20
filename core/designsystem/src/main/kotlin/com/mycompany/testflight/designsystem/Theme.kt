package com.mycompany.testflight.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.mycompany.testflight.designsystem.tokens.FriendlyShapes
import com.mycompany.testflight.designsystem.tokens.FriendlyTypography
import com.mycompany.testflight.designsystem.tokens.TestflightColors

private val FriendlyLightColors = lightColorScheme(
    primary = TestflightColors.CoralStrong,
    onPrimary = TestflightColors.White,
    primaryContainer = TestflightColors.Coral,
    onPrimaryContainer = TestflightColors.Navy,
    secondary = TestflightColors.Navy,
    onSecondary = TestflightColors.White,
    secondaryContainer = TestflightColors.Cream,
    onSecondaryContainer = TestflightColors.CoralStrong,
    tertiary = TestflightColors.ReadyGreen,
    onTertiary = TestflightColors.White,
    background = TestflightColors.Cream,
    onBackground = TestflightColors.Navy,
    surface = TestflightColors.White,
    onSurface = TestflightColors.Navy,
    surfaceVariant = TestflightColors.Cream,
    onSurfaceVariant = TestflightColors.Muted,
    surfaceTint = TestflightColors.CoralStrong,
    surfaceBright = TestflightColors.White,
    surfaceDim = TestflightColors.Cream,
    surfaceContainer = TestflightColors.White,
    surfaceContainerLow = TestflightColors.White,
    surfaceContainerLowest = TestflightColors.White,
    surfaceContainerHigh = TestflightColors.White,
    surfaceContainerHighest = TestflightColors.White,
    inverseSurface = TestflightColors.Navy,
    inverseOnSurface = TestflightColors.White,
    inversePrimary = TestflightColors.Coral,
    error = TestflightColors.CoralStrong,
    onError = TestflightColors.White,
    errorContainer = TestflightColors.ErrorFill,
    onErrorContainer = TestflightColors.CoralStrong,
    outline = TestflightColors.Line,
    outlineVariant = TestflightColors.Line,
    scrim = TestflightColors.Navy,
)

/**
 * Fixed light theme matching the approved Friendly design. No dynamic color and no
 * dark variant: the product is a light-only internal tool.
 */
@Composable
fun MyTestflightTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FriendlyLightColors,
        typography = FriendlyTypography,
        shapes = FriendlyShapes,
        content = content,
    )
}
