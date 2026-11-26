package dev.minhtoan.mytestflight.feature.applist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import dev.minhtoan.mytestflight.designsystem.MyTestflightTheme
import dev.minhtoan.mytestflight.designsystem.components.FriendlySnackbarHost
import dev.minhtoan.mytestflight.designsystem.components.HandleBar
import dev.minhtoan.mytestflight.designsystem.components.PillButton
import dev.minhtoan.mytestflight.designsystem.components.PillStyle
import dev.minhtoan.mytestflight.designsystem.components.ScreenState
import dev.minhtoan.mytestflight.designsystem.components.SectionHeader
import dev.minhtoan.mytestflight.designsystem.components.SheetSurface
import dev.minhtoan.mytestflight.designsystem.components.headerBleed
import dev.minhtoan.mytestflight.designsystem.components.sheetTopFade
import dev.minhtoan.mytestflight.designsystem.components.rememberSyncedTextFieldState
import dev.minhtoan.mytestflight.designsystem.text.UiText
import dev.minhtoan.mytestflight.designsystem.text.asString
import dev.minhtoan.mytestflight.designsystem.tokens.AppAccentPalette
import dev.minhtoan.mytestflight.designsystem.tokens.FriendlyType
import dev.minhtoan.mytestflight.designsystem.tokens.TestflightColors
import dev.minhtoan.mytestflight.domain.apk.BuildAvailability
import dev.minhtoan.mytestflight.domain.apk.ResolvedBuild
import dev.minhtoan.mytestflight.domain.installer.InstallAction
import dev.minhtoan.mytestflight.domain.model.ApkArtifact
import dev.minhtoan.mytestflight.domain.model.AppCatalogEntry
import dev.minhtoan.mytestflight.domain.model.AppVersion
import dev.minhtoan.mytestflight.domain.model.TestApp
import dev.minhtoan.mytestflight.feature.applist.components.AppRow
import dev.minhtoan.mytestflight.feature.applist.components.AppRowModel
import dev.minhtoan.mytestflight.feature.applist.install.InstallStage
import dev.minhtoan.mytestflight.feature.applist.install.InstallState
import dev.minhtoan.mytestflight.feature.applist.search.filterApps
import dev.minhtoan.mytestflight.feature.applist.ui.COLLAPSE_MAX_FLING_PROGRESS
import dev.minhtoan.mytestflight.feature.applist.ui.formatShortFileSize
import dev.minhtoan.mytestflight.feature.applist.ui.settleCollapseTarget
import kotlin.math.roundToInt

private enum class ListStateKey { Loading, Error, Empty, NoResults, Content }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    uiState: AppListUiState,
    installState: InstallState,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
    onQueryChange: (String) -> Unit,
    onInstall: (TestApp, ResolvedBuild) -> Unit,
    onLaunchApp: (String) -> Unit,
    onOpenApp: (String) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val visibleEntries = remember(uiState.apps, uiState.resolutions, uiState.query) {
        filterApps(uiState.apps, uiState.query)
            .mapNotNull { app -> uiState.resolutions[app.id] }
    }
    val rowModels = remember(visibleEntries, context) {
        visibleEntries.map { entry ->
            val version = entry.version
            AppRowModel(
                app = entry.app,
                version = version,
                build = entry.build,
                action = entry.action,
                accent = AppAccentPalette.colorFor(entry.app.id),
                versionSummary = if (version == null) {
                    context.getString(R.string.no_builds)
                } else {
                    context.getString(
                        R.string.list_version_summary,
                        version.versionName,
                        formatShortFileSize(
                            context,
                            entry.build?.artifact?.sizeBytes ?: version.sizeBytes,
                        ),
                    )
                },
            )
        }
    }
    val readyCount = remember(uiState.resolutions) {
        uiState.resolutions.values.count { it.build != null }
    }
    val stateKey = when {
        uiState.isLoading && uiState.apps.isEmpty() -> ListStateKey.Loading
        uiState.error != null && uiState.apps.isEmpty() -> ListStateKey.Error
        uiState.apps.isEmpty() -> ListStateKey.Empty
        visibleEntries.isEmpty() -> ListStateKey.NoResults
        else -> ListStateKey.Content
    }
    val installBusy = installState.isActive
    var headerOffsetPx by remember { mutableFloatStateOf(0f) }
    var headerFullHeightPx by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(stateKey) {
        if (stateKey != ListStateKey.Content) {
            headerOffsetPx = 0f
        }
    }
    val headerNestedScroll = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val max = headerFullHeightPx
                if (max <= 0f) return Offset.Zero
                val up = -available.y
                if (up <= 0f) return Offset.Zero
                val current = headerOffsetPx
                val new = (current + up).coerceAtMost(max)
                val consumed = new - current
                if (consumed <= 0f) return Offset.Zero
                headerOffsetPx = new
                return Offset(0f, -consumed)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                val down = available.y
                if (down <= 0f) return Offset.Zero
                val current = headerOffsetPx
                if (current <= 0f) return Offset.Zero
                val new = (current - down).coerceAtLeast(0f)
                val consumedDown = current - new
                if (consumedDown <= 0f) return Offset.Zero
                headerOffsetPx = new
                return Offset(0f, consumedDown)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val max = headerFullHeightPx
                if (max <= 0f) return Velocity.Zero
                val fraction = (headerOffsetPx / max).coerceIn(0f, 1f)
                if (fraction <= 0f || fraction >= 1f) return Velocity.Zero
                // Positive when the fling would keep collapsing the hero.
                val progressVelocity = -available.y / max
                val target = settleCollapseTarget(fraction, progressVelocity)
                animate(
                    initialValue = fraction,
                    targetValue = target,
                    initialVelocity = progressVelocity.coerceIn(
                        -COLLAPSE_MAX_FLING_PROGRESS,
                        COLLAPSE_MAX_FLING_PROGRESS,
                    ),
                    animationSpec = spring(
                        dampingRatio = 0.9f,
                        stiffness = Spring.StiffnessMediumLow,
                    ),
                ) { value, _ -> headerOffsetPx = value.coerceIn(0f, 1f) * max }
                headerOffsetPx = target * max
                return available
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TestflightColors.Coral),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            ListHero(
                apps = uiState.apps,
                readyCount = readyCount,
                query = uiState.query,
                collapseOffsetPx = { headerOffsetPx },
                onCollapsibleHeight = { headerFullHeightPx = it.toFloat() },
                onQueryChange = onQueryChange,
            )
            SheetSurface(
                modifier = Modifier.weight(1f),
            ) {
                HandleBar(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 14.dp, bottom = 4.dp),
                )
                AnimatedContent(
                    targetState = stateKey,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    transitionSpec = {
                        fadeIn(tween(160)) togetherWith fadeOut(tween(120))
                    },
                    contentAlignment = Alignment.TopCenter,
                    label = "listState",
                ) { key ->
                    when (key) {
                        ListStateKey.Loading -> Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            ScreenState(
                                kicker = stringResource(R.string.list_loading_kicker),
                                title = stringResource(R.string.list_loading_title),
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

                        ListStateKey.Error -> Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            ScreenState(
                                kicker = stringResource(R.string.list_error_kicker),
                                title = stringResource(R.string.error_title),
                                body = uiState.error?.asString()
                                    ?: stringResource(R.string.error_unexpected),
                                actions = {
                                    PillButton(
                                        text = stringResource(R.string.retry),
                                        onClick = onRefresh,
                                    )
                                    PillButton(
                                        text = stringResource(R.string.list_open_settings),
                                        onClick = onOpenSettings,
                                        style = PillStyle.Cream,
                                    )
                                },
                            )
                        }

                        ListStateKey.Empty -> Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            ScreenState(
                                kicker = stringResource(R.string.list_empty_kicker),
                                title = stringResource(R.string.empty_title),
                                body = stringResource(R.string.empty_body),
                                actions = {
                                    PillButton(
                                        text = stringResource(R.string.retry),
                                        onClick = onRefresh,
                                        style = PillStyle.Cream,
                                    )
                                },
                            )
                        }

                        ListStateKey.NoResults -> Box(
                            Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            ScreenState(
                                kicker = stringResource(R.string.list_no_results_kicker),
                                title = stringResource(R.string.search_no_results_title),
                                body = stringResource(
                                    R.string.search_no_results_body,
                                    uiState.query,
                                ),
                            )
                        }

                        ListStateKey.Content -> ContentList(
                            query = uiState.query,
                            itemCount = visibleEntries.size,
                            rowModels = rowModels,
                            installBusy = installBusy,
                            installState = installState,
                            isLoading = uiState.isLoading,
                            nestedScrollConnection = headerNestedScroll,
                            onRefresh = onRefresh,
                            onOpenApp = onOpenApp,
                            onInstall = onInstall,
                            onLaunchApp = onLaunchApp,
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

@Composable
private fun ListHero(
    apps: List<TestApp>,
    readyCount: Int,
    query: String,
    /** Read in layout/draw so collapsing the hero never recomposes it. */
    collapseOffsetPx: () -> Float,
    onCollapsibleHeight: (Int) -> Unit,
    onQueryChange: (String) -> Unit,
) {
    var fullHeightPx by remember { mutableFloatStateOf(0f) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .headerBleed(TestflightColors.Coral)
            .statusBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clipToBounds()
                .collapseVertically(collapseOffsetPx),
        ) {
            Column(
                modifier = Modifier
                    .onSizeChanged {
                        fullHeightPx = it.height.toFloat()
                        onCollapsibleHeight(it.height)
                    }
                    .graphicsLayer {
                        alpha = if (fullHeightPx > 0f) {
                            1f - (collapseOffsetPx() / fullHeightPx).coerceIn(0f, 1f)
                        } else {
                            1f
                        }
                    }
                    .padding(start = 20.dp, end = 20.dp, top = 18.dp, bottom = 14.dp),
            ) {
                Text(
                    text = stringResource(R.string.list_kicker),
                    style = FriendlyType.Kicker,
                    color = TestflightColors.Navy.copy(alpha = 0.82f),
                )
                Text(
                    text = stringResource(R.string.apps_title),
                    style = FriendlyType.Hero,
                    color = TestflightColors.Navy,
                )
                if (apps.isNotEmpty()) {
                    Spacer(Modifier.height(7.dp))
                    Text(
                        text = pluralStringResource(
                            R.plurals.list_ready_count,
                            readyCount,
                            readyCount,
                        ),
                        style = FriendlyType.Subtitle,
                        color = TestflightColors.Navy.copy(alpha = 0.9f),
                    )
                }
            }
        }
        SearchField(
            query = query,
            onQueryChange = onQueryChange,
            modifier = Modifier.padding(horizontal = 20.dp),
        )
        Spacer(Modifier.height(14.dp))
    }
}

private fun Modifier.collapseVertically(offsetPx: () -> Float): Modifier = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)
    val collapse = offsetPx().roundToInt().coerceIn(0, placeable.height)
    layout(placeable.width, placeable.height - collapse) {
        placeable.place(0, -collapse)
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberSyncedTextFieldState(query, onQueryChange)
    TextField(
        state = state,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        textStyle = FriendlyType.SearchText.copy(color = TestflightColors.Navy),
        placeholder = {
            Text(
                text = stringResource(R.string.search_hint),
                style = FriendlyType.SearchText,
                color = TestflightColors.Navy.copy(alpha = 0.55f),
                maxLines = 1,
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
            )
        },
        trailingIcon = if (state.text.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(
                            R.string.search_clear_content_description,
                        ),
                    )
                }
            }
        } else {
            null
        },
        lineLimits = TextFieldLineLimits.SingleLine,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        shape = RoundedCornerShape(28.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = TestflightColors.White,
            unfocusedContainerColor = TestflightColors.White.copy(alpha = 0.72f),
            disabledContainerColor = TestflightColors.White.copy(alpha = 0.5f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = TestflightColors.Navy,
            focusedTextColor = TestflightColors.Navy,
            unfocusedTextColor = TestflightColors.Navy,
            focusedPlaceholderColor = TestflightColors.Navy.copy(alpha = 0.55f),
            unfocusedPlaceholderColor = TestflightColors.Navy.copy(alpha = 0.55f),
            focusedLeadingIconColor = TestflightColors.Navy.copy(alpha = 0.8f),
            unfocusedLeadingIconColor = TestflightColors.Navy.copy(alpha = 0.8f),
            focusedTrailingIconColor = TestflightColors.Navy,
            unfocusedTrailingIconColor = TestflightColors.Navy,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentList(
    query: String,
    itemCount: Int,
    rowModels: List<AppRowModel>,
    installBusy: Boolean,
    installState: InstallState,
    isLoading: Boolean,
    nestedScrollConnection: NestedScrollConnection,
    onRefresh: () -> Unit,
    onOpenApp: (String) -> Unit,
    onInstall: (TestApp, ResolvedBuild) -> Unit,
    onLaunchApp: (String) -> Unit,
) {
    val listState = rememberLazyListState()
    PullToRefreshBox(
        isRefreshing = isLoading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .sheetTopFade(
                    scrollOffsetPx = {
                        if (listState.firstVisibleItemIndex > 0) {
                            Float.MAX_VALUE
                        } else {
                            listState.firstVisibleItemScrollOffset.toFloat()
                        }
                    },
                ),
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            item(key = "section", contentType = "section") {
                SectionHeader(
                    title = if (query.isBlank()) {
                        stringResource(R.string.list_all_apps)
                    } else {
                        stringResource(R.string.list_results_for, query)
                    },
                    trailing = pluralStringResource(
                        R.plurals.list_item_count,
                        itemCount,
                        itemCount,
                    ),
                    modifier = Modifier.padding(
                        start = 18.dp,
                        end = 18.dp,
                        top = 2.dp,
                        bottom = 6.dp,
                    ),
                )
            }
            items(
                items = rowModels,
                key = { model -> model.app.id },
                contentType = { "app" },
            ) { model ->
                AppRow(
                    model = model,
                    installBusy = installBusy,
                    rowInstall = installState.takeIf { it.appId == model.app.id },
                    onOpenDetail = onOpenApp,
                    onInstall = onInstall,
                    onLaunchApp = onLaunchApp,
                )
            }
        }
    }
}

private val previewVersion = AppVersion(
    versionName = "1.4.2",
    versionCode = 142,
    minSdk = 26,
    releaseNotes = "Fixes login timeout",
    publishedAt = "2026-11-19T08:00:00.000Z",
    sizeBytes = 7_340_032L,
    sha256 = null,
    downloadPath = "/api/v1/apps/crm/versions/1.4.2/download",
)

private val previewApps = listOf(
    TestApp(
        id = "crm",
        name = "Internal CRM",
        packageName = "com.company.crm",
        iconUrl = null,
        latestVersion = previewVersion,
        description = "CRM for the internal sales team",
        developer = "Platform Team",
        versionCount = 3,
    ),
    TestApp(
        id = "ops",
        name = "Ops Console",
        packageName = "com.company.ops",
        iconUrl = null,
        latestVersion = null,
    ),
)

private val previewArtifact = ApkArtifact(
    abi = "arm64-v8a",
    sizeBytes = previewVersion.sizeBytes,
    sha256 = null,
    downloadPath = "${previewVersion.downloadPath}?abi=arm64-v8a",
)

private val previewEntries = listOf(
    AppCatalogEntry(
        app = previewApps[0],
        version = previewVersion,
        availability = BuildAvailability.Available(previewArtifact),
        installed = null,
        action = InstallAction.Install,
    ),
    AppCatalogEntry(
        app = previewApps[1],
        version = null,
        availability = null,
        installed = null,
        action = InstallAction.Unsupported,
    ),
).associateBy { it.app.id }

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun AppListLoadedPreview() {
    MyTestflightTheme {
        AppListScreen(
            uiState = AppListUiState(
                isLoading = false,
                apps = previewApps,
                resolutions = previewEntries,
            ),
            installState = InstallState(),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onQueryChange = {},
            onInstall = { _, _ -> },
            onLaunchApp = {},
            onOpenApp = {},
            onOpenSettings = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun AppListEmptyPreview() {
    MyTestflightTheme {
        AppListScreen(
            uiState = AppListUiState(isLoading = false),
            installState = InstallState(),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onQueryChange = {},
            onInstall = { _, _ -> },
            onLaunchApp = {},
            onOpenApp = {},
            onOpenSettings = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun AppListErrorPreview() {
    MyTestflightTheme {
        AppListScreen(
            uiState = AppListUiState(
                isLoading = false,
                error = UiText.Res(R.string.error_network),
            ),
            installState = InstallState(),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onQueryChange = {},
            onInstall = { _, _ -> },
            onLaunchApp = {},
            onOpenApp = {},
            onOpenSettings = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun AppListInstallingPreview() {
    MyTestflightTheme {
        AppListScreen(
            uiState = AppListUiState(
                isLoading = false,
                apps = previewApps,
                resolutions = previewEntries,
            ),
            installState = InstallState(
                appId = "crm",
                appName = "Internal CRM",
                versionName = "1.4.2",
                stage = InstallStage.Downloading,
                progress = 0.42f,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onQueryChange = {},
            onInstall = { _, _ -> },
            onLaunchApp = {},
            onOpenApp = {},
            onOpenSettings = {},
        )
    }
}
