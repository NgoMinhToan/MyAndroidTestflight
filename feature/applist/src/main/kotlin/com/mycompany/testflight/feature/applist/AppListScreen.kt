package com.mycompany.testflight.feature.applist

import android.text.format.Formatter
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.mycompany.testflight.designsystem.MyTestflightTheme
import com.mycompany.testflight.domain.model.AppVersion
import com.mycompany.testflight.domain.model.TestApp
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    uiState: AppListUiState,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
    onInstall: (TestApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.apps_title)) },
                actions = {
                    IconButton(onClick = onRefresh, enabled = !uiState.isLoading) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.refresh_content_description),
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                uiState.isLoading && uiState.apps.isEmpty() ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                uiState.errorMessage != null && uiState.apps.isEmpty() ->
                    ErrorPanel(
                        message = uiState.errorMessage,
                        onRetry = onRefresh,
                        modifier = Modifier.align(Alignment.Center),
                    )

                uiState.apps.isEmpty() ->
                    EmptyPanel(modifier = Modifier.align(Alignment.Center))

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = uiState.apps,
                        key = { app -> app.id },
                        contentType = { "app" },
                    ) { app ->
                        AppRow(
                            app = app,
                            isActive = uiState.installingAppId == app.id,
                            stage = uiState.installStage,
                            progress = uiState.downloadProgress,
                            installEnabled = uiState.installingAppId == null,
                            onInstall = onInstall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorPanel(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.error_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onRetry) {
            Text(stringResource(R.string.retry))
        }
    }
}

@Composable
private fun EmptyPanel(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.empty_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.empty_body),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AppRow(
    app: TestApp,
    isActive: Boolean,
    stage: InstallStage?,
    progress: Float?,
    installEnabled: Boolean,
    onInstall: (TestApp) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(app)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = versionLine(app.latestVersion),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.width(12.dp))
            InstallButton(
                app = app,
                isActive = isActive,
                stage = stage,
                progress = progress,
                enabled = installEnabled && app.latestVersion != null,
                onClick = { onInstall(app) },
            )
        }
    }
}

@Composable
private fun AppIcon(app: TestApp) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        LetterTile(app.name)
        val iconUrl = app.iconUrl
        if (iconUrl != null) {
            AsyncImage(
                model = iconUrl,
                contentDescription = stringResource(R.string.app_icon_content_description, app.name),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun LetterTile(name: String) {
    Text(
        text = name.take(1).uppercase(),
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onSecondaryContainer,
    )
}

@Composable
private fun versionLine(version: AppVersion?): String {
    if (version == null) return stringResource(R.string.no_builds)
    val size = Formatter.formatShortFileSize(LocalContext.current, version.sizeBytes)
    return "v${version.versionName} · $size"
}

@Composable
private fun InstallButton(
    app: TestApp,
    isActive: Boolean,
    stage: InstallStage?,
    progress: Float?,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val label = when {
        !isActive -> stringResource(R.string.install)

        stage == InstallStage.Downloading && progress != null ->
            stringResource(R.string.installing_percent, (progress * 100).roundToInt())

        stage == InstallStage.Downloading -> stringResource(R.string.installing)
        stage == InstallStage.Verifying -> stringResource(R.string.verifying)
        stage == InstallStage.Launching -> stringResource(R.string.opening_installer)
        else -> stringResource(R.string.install)
    }
    val description = stringResource(R.string.install_content_description, app.name)

    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.semantics { contentDescription = description },
    ) {
        if (isActive) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(text = label, maxLines = 1)
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
    downloadUrl = "http://10.0.2.2:8080/api/v1/apps/crm/versions/1.4.2/download",
)

private val previewApps = listOf(
    TestApp(
        id = "crm",
        name = "Internal CRM",
        packageName = "com.company.crm",
        iconUrl = null,
        latestVersion = previewVersion,
    ),
    TestApp(
        id = "ops",
        name = "Ops Console",
        packageName = "com.company.ops",
        iconUrl = null,
        latestVersion = null,
    ),
)

@Preview(showBackground = true)
@Composable
private fun AppListLoadedPreview() {
    MyTestflightTheme {
        AppListScreen(
            uiState = AppListUiState(isLoading = false, apps = previewApps),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onInstall = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppListEmptyPreview() {
    MyTestflightTheme {
        AppListScreen(
            uiState = AppListUiState(isLoading = false),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onInstall = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppListErrorPreview() {
    MyTestflightTheme {
        AppListScreen(
            uiState = AppListUiState(
                isLoading = false,
                errorMessage = "Can't reach the server",
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onInstall = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AppListInstallingPreview() {
    MyTestflightTheme {
        AppListScreen(
            uiState = AppListUiState(
                isLoading = false,
                apps = previewApps,
                installingAppId = "crm",
                installStage = InstallStage.Downloading,
                downloadProgress = 0.42f,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onRefresh = {},
            onInstall = {},
        )
    }
}
