package dev.minhtoan.mytestflight.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.KeyboardActionHandler
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.minhtoan.mytestflight.designsystem.MyTestflightTheme
import dev.minhtoan.mytestflight.designsystem.components.FriendlySnackbarHost
import dev.minhtoan.mytestflight.designsystem.components.rememberSyncedTextFieldState
import dev.minhtoan.mytestflight.designsystem.text.UiText
import dev.minhtoan.mytestflight.designsystem.text.asString

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    snackbarHostState: SnackbarHostState,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
    onCheckForUpdates: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LargeTopAppBar(
                title = { Text(text = stringResource(R.string.settings_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        },
        snackbarHost = { FriendlySnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item(key = "server_header") {
                SectionLabel(stringResource(R.string.settings_server_section))
            }
            item(key = "server_item") {
                ServerItem(
                    uiState = uiState,
                    onInputChange = onInputChange,
                    onSave = {
                        focusManager.clearFocus()
                        onSave()
                    },
                    onReset = {
                        focusManager.clearFocus()
                        onReset()
                    },
                )
            }
            item(key = "connection_header") {
                SectionLabel(stringResource(R.string.settings_connection_section))
            }
            item(key = "connection_in_use") {
                SettingsRow(
                    icon = Icons.Outlined.Link,
                    title = stringResource(R.string.settings_connection_in_use),
                    supportingText = uiState.savedUrl,
                )
            }
            item(key = "connection_divider") {
                SettingsDivider()
            }
            item(key = "connection_status") {
                SettingsRow(
                    icon = Icons.Outlined.CheckCircle,
                    title = stringResource(R.string.settings_connection_status),
                    supportingText = stringResource(R.string.settings_connection_ready),
                    supportingColor = MaterialTheme.colorScheme.tertiary,
                )
            }
            item(key = "update_header") {
                SectionLabel(stringResource(R.string.settings_update_section))
            }
            item(key = "update_current") {
                SettingsRow(
                    icon = Icons.Outlined.SystemUpdate,
                    title = stringResource(R.string.settings_update_current),
                    supportingText = stringResource(
                        R.string.settings_update_current_format,
                        uiState.currentVersionName,
                        uiState.currentVersionCode,
                    ),
                )
            }
            item(key = "update_divider") {
                SettingsDivider()
            }
            item(key = "update_action") {
                UpdateAction(
                    status = uiState.updateStatus,
                    onCheckForUpdates = onCheckForUpdates,
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            .semantics { heading() },
    )
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun UpdateAction(
    status: SettingsUpdateStatus,
    onCheckForUpdates: () -> Unit,
) {
    val (text, color) = status.message()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onCheckForUpdates,
            enabled = status != SettingsUpdateStatus.Checking,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = stringResource(R.string.settings_update_check))
        }
    }
}

@Composable
private fun SettingsUpdateStatus.message(): Pair<String, Color> = when (this) {
    SettingsUpdateStatus.Idle ->
        stringResource(R.string.settings_update_description) to
            MaterialTheme.colorScheme.onSurfaceVariant

    SettingsUpdateStatus.Checking ->
        stringResource(R.string.settings_update_checking) to
            MaterialTheme.colorScheme.onSurfaceVariant

    SettingsUpdateStatus.UpToDate ->
        stringResource(R.string.settings_update_up_to_date) to
            MaterialTheme.colorScheme.tertiary

    is SettingsUpdateStatus.Available ->
        stringResource(R.string.settings_update_available, versionName) to
            MaterialTheme.colorScheme.primary

    is SettingsUpdateStatus.Downloading ->
        stringResource(R.string.settings_update_downloading) to
            MaterialTheme.colorScheme.onSurfaceVariant

    is SettingsUpdateStatus.Downloaded ->
        stringResource(R.string.settings_update_downloaded) to
            MaterialTheme.colorScheme.tertiary

    SettingsUpdateStatus.Failed ->
        stringResource(R.string.settings_update_error) to
            MaterialTheme.colorScheme.error
}

/** The server URL field and its two actions form a single full-width list item. */
@Composable
private fun ServerItem(
    uiState: SettingsUiState,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
) {
    val state = rememberSyncedTextFieldState(uiState.input, onInputChange)
    val error = uiState.error
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        OutlinedTextField(
            state = state,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.settings_server_url_label)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Dns,
                    contentDescription = null,
                )
            },
            placeholder = { Text(stringResource(R.string.settings_server_url_hint)) },
            supportingText = if (error != null) {
                { Text(text = error.asString()) }
            } else {
                null
            },
            isError = error != null,
            lineLimits = TextFieldLineLimits.SingleLine,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done,
            ),
            onKeyboardAction = KeyboardActionHandler { onSave() },
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = onSave,
                enabled = !uiState.isSaving && uiState.input != uiState.savedUrl,
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.settings_save),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OutlinedIconButton(
                onClick = onReset,
                enabled = !uiState.isSaving,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Restore,
                    contentDescription = stringResource(R.string.settings_reset),
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    supportingText: String,
    supportingColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    ListItem(
        modifier = Modifier.fillMaxWidth(),
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
            )
        },
        supportingContent = {
            Text(
                text = supportingText,
                style = MaterialTheme.typography.bodyMedium,
                color = supportingColor,
            )
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun SettingsPreview() {
    MyTestflightTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                input = "http://10.0.2.2:8080/api",
                savedUrl = "http://10.0.2.2:8080",
                defaultUrl = "http://10.0.2.2:8080",
                error = UiText.Res(R.string.settings_url_error_path),
                currentVersionName = "0.1.3",
                currentVersionCode = 4,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onInputChange = {},
            onSave = {},
            onReset = {},
            onCheckForUpdates = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun SettingsReadyPreview() {
    MyTestflightTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                input = "http://10.0.2.2:8080",
                savedUrl = "http://10.0.2.2:8080",
                defaultUrl = "http://10.0.2.2:8080",
                currentVersionName = "0.1.3",
                currentVersionCode = 4,
                updateStatus = SettingsUpdateStatus.UpToDate,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onInputChange = {},
            onSave = {},
            onReset = {},
            onCheckForUpdates = {},
        )
    }
}
