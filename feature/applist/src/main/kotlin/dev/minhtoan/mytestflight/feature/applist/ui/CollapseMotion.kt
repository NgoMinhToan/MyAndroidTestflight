package dev.minhtoan.mytestflight.feature.applist.ui

/** Seconds of fling momentum used to project where a collapsing header should settle. */
internal const val COLLAPSE_FLING_PROJECTION_SECONDS = 0.15f

/** Caps the settle spring's starting velocity, in collapse progress per second. */
internal const val COLLAPSE_MAX_FLING_PROGRESS = 5f

/**
 * Picks the collapse target after a release. [progressVelocity] is positive while the fling
 * keeps collapsing the header, so a short projection lets a committed fling win over the
 * midpoint threshold and makes the settle feel like it carries the gesture's momentum.
 */
internal fun settleCollapseTarget(progress: Float, progressVelocity: Float): Float {
    val projected = (progress + progressVelocity * COLLAPSE_FLING_PROJECTION_SECONDS)
        .coerceIn(0f, 1f)
    return if (projected >= 0.5f) 1f else 0f
}
