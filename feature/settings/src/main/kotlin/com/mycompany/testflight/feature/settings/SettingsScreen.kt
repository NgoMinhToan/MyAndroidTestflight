package com.mycompany.testflight.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mycompany.testflight.designsystem.MyTestflightTheme
import com.mycompany.testflight.designsystem.components.FriendlySnackbarHost
import com.mycompany.testflight.designsystem.components.rememberSyncedTextFieldState
import com.mycompany.testflight.designsystem.text.UiText
import com.mycompany.testflight.designsystem.text.asString
import com.mycompany.testflight.designsystem.tokens.FriendlyType
import com.mycompany.testflight.designsystem.tokens.TestflightColors

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
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TestflightColors.SettingsBackground),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .imePadding(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 24.dp,
                bottom = 24.dp,
            ),
        ) {
            item(key = "title") {
                Text(
                    text = stringResource(R.string.settings_title),
                    style = FriendlyType.SettingsTitle,
                    color = TestflightColors.Navy,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stringResource(R.string.settings_description),
                    style = FriendlyType.Body,
                    color = TestflightColors.Muted,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
            }
            item(key = "server_header") {
                SectionLabel(stringResource(R.string.settings_server_section))
            }
            item(key = "server_group") {
                SettingsGroup {
                    ServerForm(
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
                Spacer(Modifier.height(20.dp))
            }
            item(key = "connection_header") {
                SectionLabel(stringResource(R.string.settings_connection_section))
            }
            item(key = "connection_group") {
                SettingsGroup {
                    ConnectionRows(savedUrl = uiState.savedUrl)
                }
                Spacer(Modifier.height(20.dp))
            }
            item(key = "update_header") {
                SectionLabel(stringResource(R.string.settings_update_section))
            }
            item(key = "update_group") {
                SettingsGroup {
                    UpdateSection(
                        uiState = uiState,
                        onCheckForUpdates = onCheckForUpdates,
                    )
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
private fun SettingsGroup(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = TestflightColors.SettingsGroup,
        contentColor = TestflightColors.Navy,
    ) {
        Column(content = content)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = FriendlyType.SectionLabel,
        color = TestflightColors.CoralStrong,
        modifier = Modifier.padding(top = 8.dp, bottom = 6.dp),
    )
}

@Composable
private fun ServerForm(
    uiState: SettingsUiState,
    onInputChange: (String) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
) {
    val state = rememberSyncedTextFieldState(uiState.input, onInputChange)
    Column(modifier = Modifier.padding(16.dp)) {
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
            isError = uiState.error != null,
            textStyle = FriendlyType.MonoInput.copy(color = TestflightColors.Navy),
            lineLimits = TextFieldLineLimits.SingleLine,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done,
            ),
            onKeyboardAction = KeyboardActionHandler { onSave() },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TestflightColors.CoralStrong,
                unfocusedBorderColor = TestflightColors.FieldBorder,
                errorBorderColor = TestflightColors.CoralStrong,
                focusedLabelColor = TestflightColors.CoralStrong,
                unfocusedLabelColor = TestflightColors.Muted,
                errorLabelColor = TestflightColors.CoralStrong,
                cursorColor = TestflightColors.CoralStrong,
                focusedTextColor = TestflightColors.Navy,
                unfocusedTextColor = TestflightColors.Navy,
                focusedLeadingIconColor = TestflightColors.Muted,
                unfocusedLeadingIconColor = TestflightColors.Muted,
            ),
        )
        Spacer(Modifier.height(6.dp))
        // Rendered outside the field so the helper text shares the card's text edge with
        // "Mặc định" and the actions below it instead of the field's icon-inset supporting slot.
        Text(
            text = uiState.error?.asString()
                ?: stringResource(R.string.settings_server_url_helper),
            style = FriendlyType.Helper,
            color = if (uiState.error != null) {
                TestflightColors.ErrorText
            } else {
                TestflightColors.TextSecondary
            },
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(
                R.string.settings_default_url_format,
                uiState.defaultUrl,
            ),
            style = FriendlyType.Helper,
            color = TestflightColors.TextSecondary,
        )
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Button(
                onClick = onSave,
                enabled = !uiState.isSaving && uiState.input != uiState.savedUrl,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TestflightColors.CoralStrong,
                    contentColor = TestflightColors.White,
                    disabledContainerColor = TestflightColors.ButtonDisabledFill,
                    disabledContentColor = TestflightColors.ButtonDisabledText,
                ),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                Text(
                    text = stringResource(R.string.settings_save),
                    style = FriendlyType.Button,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            OutlinedIconButton(
                onClick = onReset,
                enabled = !uiState.isSaving,
                modifier = Modifier.size(48.dp),
                border = BorderStroke(1.dp, TestflightColors.FieldBorder),
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    contentColor = TestflightColors.Navy,
                    disabledContentColor = TestflightColors.ButtonDisabledText,
                ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Restore,
                    contentDescription = stringResource(R.string.settings_reset),
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

@Composable
private fun ConnectionRows(savedUrl: String) {
    SettingsRow(
        icon = Icons.Outlined.Link,
        title = stringResource(R.string.settings_connection_in_use),
        value = savedUrl.removePrefix("https://").removePrefix("http://"),
    )
    HorizontalDivider(
        modifier = Modifier.padding(start = 54.dp),
        thickness = 1.dp,
        color = TestflightColors.Line,
    )
    SettingsRow(
        icon = Icons.Outlined.CheckCircle,
        title = stringResource(R.string.settings_connection_status),
        value = stringResource(R.string.settings_connection_ready),
        valueColor = TestflightColors.ReadyGreen,
    )
}

@Composable
private fun UpdateSection(
    uiState: SettingsUiState,
    onCheckForUpdates: () -> Unit,
) {
    SettingsRow(
        icon = Icons.Outlined.SystemUpdate,
        title = stringResource(R.string.settings_update_current),
        value = stringResource(
            R.string.settings_update_current_format,
            uiState.currentVersionName,
            uiState.currentVersionCode,
        ),
    )
    HorizontalDivider(
        modifier = Modifier.padding(start = 54.dp),
        thickness = 1.dp,
        color = TestflightColors.Line,
    )
    Column(modifier = Modifier.padding(16.dp)) {
        val (statusText, statusColor) = uiState.updateStatus.statusText()
        Text(
            text = statusText,
            style = FriendlyType.Helper,
            color = statusColor,
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onCheckForUpdates,
            enabled = uiState.updateStatus != SettingsUpdateStatus.Checking,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = TestflightColors.CoralStrong,
                contentColor = TestflightColors.White,
                disabledContainerColor = TestflightColors.ButtonDisabledFill,
                disabledContentColor = TestflightColors.ButtonDisabledText,
            ),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_update_check),
                style = FriendlyType.Button,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SettingsUpdateStatus.statusText(): Pair<String, Color> = when (this) {
    SettingsUpdateStatus.Idle -> stringResource(R.string.settings_update_description) to
        TestflightColors.TextSecondary

    SettingsUpdateStatus.Checking -> stringResource(R.string.settings_update_checking) to
        TestflightColors.Muted

    SettingsUpdateStatus.UpToDate -> stringResource(R.string.settings_update_up_to_date) to
        TestflightColors.ReadyGreen

    is SettingsUpdateStatus.Available ->
        stringResource(R.string.settings_update_available, versionName) to
            TestflightColors.CoralStrong

    is SettingsUpdateStatus.Downloading -> stringResource(R.string.settings_update_downloading) to
        TestflightColors.Muted

    is SettingsUpdateStatus.Downloaded -> stringResource(R.string.settings_update_downloaded) to
        TestflightColors.ReadyGreen

    SettingsUpdateStatus.Failed -> stringResource(R.string.settings_update_error) to
        TestflightColors.ErrorText
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    value: String,
    valueColor: Color = TestflightColors.Navy,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TestflightColors.Muted,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = title,
            style = FriendlyType.SettingsRowTitle,
            color = TestflightColors.Navy,
            modifier = Modifier.weight(1f),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = value,
            style = FriendlyType.SettingsRowValue,
            color = valueColor,
            textAlign = TextAlign.End,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 200.dp),
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
                currentVersionName = "0.1.0",
                currentVersionCode = 1,
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
                currentVersionName = "0.1.0",
                currentVersionCode = 1,
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
