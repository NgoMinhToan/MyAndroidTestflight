package dev.minhtoan.mytestflight.designsystem.tokens

import androidx.compose.ui.graphics.Color

/**
 * Deterministic pastel accent per app id, shared by the list and detail screens.
 * Uses floorMod so [Int.MIN_VALUE] cannot produce a negative index.
 */
object AppAccentPalette {
    val colors: List<Color> = listOf(
        Color(0xFFFFD66B),
        Color(0xFF9AD8FF),
        Color(0xFFC8F0CE),
        Color(0xFFF9B9E8),
        Color(0xFFD8CBFF),
        Color(0xFFA8E6DC),
        Color(0xFFDFF2A8),
        Color(0xFFE8E4DE),
    )

    fun colorFor(seed: String): Color =
        colors[Math.floorMod(seed.hashCode(), colors.size)]

    /** Small helper for tests/previews that want a stable index without Color (unused in production). */
    internal fun indexFor(seed: String): Int = Math.floorMod(seed.hashCode(), colors.size)
}
