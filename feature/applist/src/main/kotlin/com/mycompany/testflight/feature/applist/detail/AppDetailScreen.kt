package com.mycompany.testflight.feature.applist.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycompany.testflight.designsystem.MyTestflightTheme
import com.mycompany.testflight.designsystem.components.FriendlySnackbarHost
import com.mycompany.testflight.designsystem.components.HandleBar
import com.mycompany.testflight.designsystem.components.PillButton
import com.mycompany.testflight.designsystem.components.ScreenState
import com.mycompany.testflight.designsystem.components.SectionHeader
import com.mycompany.testflight.designsystem.components.SheetSurface
import com.mycompany.testflight.designsystem.components.headerBleed
import com.mycompany.testflight.designsystem.components.sheetTopFade
import com.mycompany.testflight.designsystem.text.asString
import com.mycompany.testflight.designsystem.tokens.AppAccentPalette
import com.mycompany.testflight.designsystem.tokens.FriendlyDimens
import com.mycompany.testflight.designsystem.tokens.FriendlyType
import com.mycompany.testflight.designsystem.tokens.JakartaSans
import com.mycompany.testflight.designsystem.tokens.PillShape
import com.mycompany.testflight.designsystem.tokens.TestflightColors
import com.mycompany.testflight.domain.model.AppDetails
import com.mycompany.testflight.domain.model.AppVersion
import com.mycompany.testflight.domain.model.TestApp
import com.mycompany.testflight.feature.applist.R
import com.mycompany.testflight.feature.applist.install.InstallStage
import com.mycompany.testflight.feature.applist.install.InstallState
import com.mycompany.testflight.feature.applist.ui.COLLAPSE_MAX_FLING_PROGRESS
import com.mycompany.testflight.feature.applist.ui.rememberFormattedPublishedDate
import com.mycompany.testflight.feature.applist.ui.rememberShortFileSize
import com.mycompany.testflight.feature.applist.ui.settleCollapseTarget
import java.time.format.FormatStyle
import java.util.Locale
import kotlin.math.roundToInt

private const val EXPANDED_NAME_SP = 29f
private const val COLLAPSED_NAME_SP = 16f
private const val EXPANDED_ICON_LETTER_SP = 28f
private const val COLLAPSED_ICON_LETTER_SP = 14f

private val ExpandedIconSize = 76.dp

@Composable
fun AppDetailScreen(
    uiState: AppDetailUiState,
    installState: InstallState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onInstall: (AppVersion) -> Unit,
    modifier: Modifier = Modifier,
) {
    val details = uiState.details
    // Start from the accent already on screen (restored state) and only animate when a load
    // reveals a different one, so returning to a tab does not replay the header color sweep.
    val accent = details?.app?.id?.let(AppAccentPalette::colorFor)
        ?: TestflightColors.Coral
    val headerColor by animateColorAsState(
        targetValue = accent,
        animationSpec = tween(320),
        label = "detailHeader",
    )
    // Saved so switching tabs and coming back restores the header without replaying motion.
    var headerProgress by rememberSaveable { mutableStateOf(0f) }
    var collapseRangePx by remember { mutableFloatStateOf(1f) }
    val headerNestedScroll = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val range = collapseRangePx
                if (range <= 0f) return Offset.Zero
                val up = -available.y
                if (up <= 0f) return Offset.Zero
                val px = headerProgress * range
                val newPx = (px + up).coerceAtMost(range)
                val consumed = newPx - px
                if (consumed <= 0f) return Offset.Zero
                headerProgress = newPx / range
                return Offset(0f, -consumed)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                val range = collapseRangePx
                val down = available.y
                if (range <= 0f || down <= 0f || headerProgress <= 0f) return Offset.Zero
                val px = headerProgress * range
                val newPx = (px - down).coerceAtLeast(0f)
                val consumedDown = px - newPx
                if (consumedDown <= 0f) return Offset.Zero
                headerProgress = newPx / range
                return Offset(0f, consumedDown)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val range = collapseRangePx
                if (range <= 0f) return Velocity.Zero
                val p = headerProgress
                if (p <= 0f || p >= 1f) return Velocity.Zero
                // Positive when the fling would keep collapsing the header.
                val progressVelocity = -available.y / range
                // Let the fling's momentum decide where the header lands and hand that same
                // velocity to the settle spring so it continues the gesture from rest.
                val target = settleCollapseTarget(p, progressVelocity)
                animate(
                    initialValue = p,
                    targetValue = target,
                    initialVelocity = progressVelocity.coerceIn(
                        -COLLAPSE_MAX_FLING_PROGRESS,
                        COLLAPSE_MAX_FLING_PROGRESS,
                    ),
                    animationSpec = spring(
                        dampingRatio = 0.9f,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                ) { value, _ -> headerProgress = value.coerceIn(0f, 1f) }
                headerProgress = target
                return available
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(headerNestedScroll)
            .drawBehind { drawRect(headerColor) },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            DetailHeader(
                details = details,
                headerColor = { headerColor },
                progress = { headerProgress },
                onCollapseRangeChanged = { range ->
                    if (range != collapseRangePx) collapseRangePx = range
                },
                onBack = onBack,
            )
            SheetSurface(
                modifier = Modifier.weight(1f),
            ) {
                HandleBar(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 14.dp, bottom = 4.dp),
                )
                when {
                    details != null -> DetailContent(
                        details = details,
                        installState = installState,
                        onInstall = onInstall,
                    )

                    uiState.isLoading -> Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        ScreenState(
                            kicker = stringResource(R.string.list_loading_kicker),
                            title = stringResource(R.string.detail_loading_title),
                            body = stringResource(R.string.list_loading_body),
                            actions = {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(28.dp),
                                    color = TestflightColors.CoralStrong,
                                    trackColor = TestflightColors.Cream,
                                    strokeWidth = 3.dp,
                                )
                            },
                        )
                    }

                    else -> Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        ScreenState(
                            kicker = stringResource(R.string.list_error_kicker),
                            title = stringResource(R.string.detail_error_title),
                            body = uiState.error?.asString()
                                ?: stringResource(R.string.error_unexpected),
                            actions = {
                                PillButton(
                                    text = stringResource(R.string.retry),
                                    onClick = onRetry,
                                )
                            },
                        )
                    }
                }
            }
        }
        FriendlySnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
        )
    }
}

private fun nameScaleAt(progress: Float): Float =
    1f + (COLLAPSED_NAME_SP / EXPANDED_NAME_SP - 1f) * progress

private fun iconLetterScaleAt(progress: Float): Float =
    1f + (COLLAPSED_ICON_LETTER_SP / EXPANDED_ICON_LETTER_SP - 1f) * progress

@Composable
private fun DetailHeader(
    details: AppDetails?,
    headerColor: () -> Color,
    /** Read in layout/draw so the drag never recomposes this header. */
    progress: () -> Float,
    onCollapseRangeChanged: (Float) -> Unit,
    onBack: () -> Unit,
) {
    val app = details?.app
    val developerLabel = app?.developer?.takeIf { it.isNotBlank() }
        ?.let { stringResource(R.string.detail_dev_internal, it) }
        ?: stringResource(R.string.detail_dev_internal_fallback)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .headerBleed(color = headerColor)
            .statusBarsPadding()
            .padding(start = 18.dp, end = 18.dp, top = 14.dp),
    ) {
        if (app == null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BackButton(onBack)
                Text(
                    text = developerLabel.uppercase(Locale.getDefault()),
                    style = FriendlyType.DetailOverline,
                    color = TestflightColors.Navy.copy(alpha = 0.6f),
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(Modifier.height(12.dp))
            return@Column
        }

        val latest = app.latestVersion

        Layout(
            content = {
                BackButton(onBack)
                Text(
                    text = developerLabel.uppercase(Locale.getDefault()),
                    style = FriendlyType.DetailOverline,
                    color = TestflightColors.Navy.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.graphicsLayer { alpha = 1f - progress() },
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            shape = RoundedCornerShape(
                                (24f + (8f - 24f) * progress()).dp,
                            )
                            clip = true
                        }
                        .background(TestflightColors.White.copy(alpha = 0.65f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = app.name.take(1).uppercase(),
                        style = TextStyle(
                            fontFamily = JakartaSans,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = EXPANDED_ICON_LETTER_SP.sp,
                        ),
                        color = TestflightColors.Navy.copy(alpha = 0.85f),
                        modifier = Modifier.graphicsLayer {
                            val scale = iconLetterScaleAt(progress())
                            scaleX = scale
                            scaleY = scale
                        },
                    )
                }
                Text(
                    text = app.name,
                    style = FriendlyType.DetailTitle,
                    color = TestflightColors.Navy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    // Scale one fixed text layout instead of animating fontSize: the glyphs
                    // then move with the rest of the header instead of re-laying out (and
                    // snapping) every frame.
                    modifier = Modifier.graphicsLayer {
                        val scale = nameScaleAt(progress())
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0f, 0f)
                    },
                )
                Text(
                    text = app.packageName,
                    style = TextStyle(
                        fontFamily = JakartaSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                    ),
                    color = TestflightColors.Navy.copy(alpha = 0.62f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.graphicsLayer { alpha = 1f - progress() },
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.graphicsLayer { alpha = 1f - progress() },
                ) {
                    if (latest != null) {
                        HeaderTag(
                            text = stringResource(
                                R.string.detail_latest_tag,
                                latest.versionName,
                            ),
                            solid = true,
                        )
                        HeaderTag(
                            text = rememberShortFileSize(latest.sizeBytes),
                            solid = false,
                        )
                        rememberFormattedPublishedDate(
                            latest.publishedAt,
                            FormatStyle.SHORT,
                        )?.let { date ->
                            HeaderTag(text = date, solid = false)
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds(),
        ) { measurables, constraints ->
            val collapse = progress()
            val loose = constraints.copy(minWidth = 0, minHeight = 0)
            val back = measurables[0].measure(loose)
            val developerMaxWidth = (constraints.maxWidth - back.width - 8.dp.roundToPx())
                .coerceAtLeast(0)
            val developer = measurables[1].measure(
                constraints.copy(
                    minWidth = 0,
                    maxWidth = developerMaxWidth,
                    minHeight = 0,
                ),
            )
            val iconSize = (76f + (28f - 76f) * collapse).dp
            val icon = measurables[2].measure(
                Constraints.fixed(iconSize.roundToPx(), iconSize.roundToPx()),
            )

            val rowHeight = 44.dp.roundToPx()
            val bottomPadding = 12.dp.roundToPx()
            val gapAfterRow = 18.dp.roundToPx()
            val gapIconName = 14.dp.roundToPx()
            val gapNamePackage = 5.dp.roundToPx()
            val gapPackageTags = 12.dp.roundToPx()

            val expandedIconY = rowHeight + gapAfterRow
            val collapsedIconX = back.width + 10.dp.roundToPx()
            val iconX = collapsedIconX * collapse
            val collapsedNameX = iconX + icon.width + 8.dp.roundToPx()
            val nameX = collapsedNameX * collapse
            // The title is measured once at its expanded size and drawn scaled down. Measure
            // it un-scaled wide enough that the scaled glyphs can still fill the collapsed
            // slot and ellipsize at its right edge.
            val nameScale = nameScaleAt(collapse)
            val nameMaxWidth = ((constraints.maxWidth - nameX) / nameScale)
                .roundToInt()
                .coerceAtLeast(1)
            val name = measurables[3].measure(loose.copy(maxWidth = nameMaxWidth))
            val packageText = measurables[4].measure(loose)
            val tags = measurables[5].measure(loose)

            val expandedNameY = expandedIconY + ExpandedIconSize.roundToPx() + gapIconName
            val expandedPackageY = expandedNameY + name.height + gapNamePackage
            val expandedTagsY = expandedPackageY + packageText.height + gapPackageTags
            val expandedHeight = expandedTagsY + tags.height + bottomPadding
            val collapsedHeight = rowHeight + bottomPadding
            val height = (
                expandedHeight + (collapsedHeight - expandedHeight) * collapse
                ).roundToInt()
            // Report on every measure: a restored (non-zero) progress must still get the
            // real range before the next drag.
            onCollapseRangeChanged((expandedHeight - collapsedHeight).toFloat())

            val collapsedIconY = (rowHeight - icon.height) / 2f
            val iconY = expandedIconY + (collapsedIconY - expandedIconY) * collapse
            val collapsedNameY = (rowHeight - name.height * nameScale) / 2f
            val nameY = expandedNameY + (collapsedNameY - expandedNameY) * collapse

            layout(constraints.maxWidth, height) {
                back.place(0, (rowHeight - back.height) / 2)
                developer.place(
                    constraints.maxWidth - developer.width,
                    (rowHeight - developer.height) / 2,
                )
                icon.place(iconX.roundToInt(), iconY.roundToInt())
                name.place(nameX.roundToInt(), nameY.roundToInt())
                packageText.place(0, expandedPackageY)
                tags.place(0, expandedTagsY)
            }
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .background(TestflightColors.White.copy(alpha = 0.6f))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    bounded = true,
                    color = TestflightColors.Navy,
                ),
                role = Role.Button,
                onClick = onBack,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = stringResource(R.string.detail_back_content_description),
            tint = TestflightColors.Navy,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun HeaderTag(text: String, solid: Boolean) {
    Text(
        text = text,
        style = FriendlyType.DetailTag,
        color = if (solid) TestflightColors.White else TestflightColors.Navy.copy(alpha = 0.8f),
        maxLines = 1,
        modifier = Modifier
            .clip(PillShape)
            .background(
                if (solid) TestflightColors.Navy
                else TestflightColors.White.copy(alpha = 0.55f),
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

@Composable
private fun DetailContent(
    details: AppDetails,
    installState: InstallState,
    onInstall: (AppVersion) -> Unit,
) {
    val latestName = details.app.latestVersion?.versionName
        ?: details.versions.firstOrNull()?.versionName
    val scrollState = rememberScrollState()
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            // Content sliding under the handle dissolves into the sheet instead of being
            // sliced by the white gap above it.
            .sheetTopFade(scrollOffsetPx = { scrollState.value.toFloat() }),
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                // Keep a 1dp scroll range even when the content is shorter than the sheet so a
                // drag always reaches the nested-scroll connection and collapses the header.
                .heightIn(min = maxHeight + 1.dp)
                .padding(start = 18.dp, end = 18.dp),
        ) {
        Text(
            text = stringResource(R.string.detail_about_title).uppercase(Locale.getDefault()),
            style = FriendlyType.Overline,
            color = TestflightColors.Muted,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = details.app.description.ifBlank {
                stringResource(R.string.detail_no_description)
            },
            style = FriendlyType.Body,
            color = TestflightColors.Body,
        )
        Spacer(Modifier.height(16.dp))
        SectionHeader(
            title = stringResource(R.string.detail_versions_section),
            trailing = pluralStringResource(
                R.plurals.detail_versions_count,
                details.versions.size,
                details.versions.size,
            ),
        )
        Spacer(Modifier.height(10.dp))
        if (details.versions.isEmpty()) {
            Text(
                text = stringResource(R.string.detail_no_versions),
                style = FriendlyType.Body,
                color = TestflightColors.RowMeta,
            )
        } else {
            details.versions.forEach { version ->
                VersionCard(
                    appName = details.app.name,
                    version = version,
                    isLatest = version.versionName == latestName,
                    rowInstall = installState.takeIf {
                        it.appId == details.app.id && it.versionName == version.versionName
                    },
                    installBusy = installState.isActive,
                    onInstall = onInstall,
                )
            }
        }
        Spacer(Modifier.height(9.dp))
        }
    }
}

private val previewVersion = AppVersion(
    versionName = "1.4.2",
    versionCode = 142,
    minSdk = 26,
    releaseNotes = "Fixes login timeout and makes the opportunity table 40% faster.",
    publishedAt = "2026-11-19T08:00:00.000Z",
    sizeBytes = 7_340_032L,
    sha256 = null,
    downloadPath = "/api/v1/apps/crm/versions/1.4.2/download",
)

private val previewApp = TestApp(
    id = "crm",
    name = "Internal CRM",
    packageName = "com.company.crm",
    iconUrl = null,
    latestVersion = previewVersion,
    description = "Manage customers, deals and the sales team's daily activity.",
    developer = "Platform Team",
    versionCount = 1,
)

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun AppDetailPreview() {
    MyTestflightTheme {
        AppDetailScreen(
            uiState = AppDetailUiState(
                isLoading = false,
                details = AppDetails(app = previewApp, versions = listOf(previewVersion)),
            ),
            installState = InstallState(
                appId = "crm",
                appName = "Internal CRM",
                versionName = "1.4.2",
                stage = InstallStage.Downloading,
                progress = 0.42f,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBack = {},
            onRetry = {},
            onInstall = {},
        )
    }
}
