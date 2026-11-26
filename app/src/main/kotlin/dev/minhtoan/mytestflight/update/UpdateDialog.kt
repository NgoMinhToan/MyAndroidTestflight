package dev.minhtoan.mytestflight.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.minhtoan.mytestflight.R
import dev.minhtoan.mytestflight.designsystem.components.PillButton
import dev.minhtoan.mytestflight.designsystem.components.PillStyle
import dev.minhtoan.mytestflight.designsystem.components.ProgressTrack
import dev.minhtoan.mytestflight.designsystem.tokens.FriendlyType
import dev.minhtoan.mytestflight.designsystem.tokens.TestflightColors
import dev.minhtoan.mytestflight.domain.update.AppRelease
import dev.minhtoan.mytestflight.domain.update.ReleaseSection
import dev.minhtoan.mytestflight.domain.update.UpdateController
import dev.minhtoan.mytestflight.domain.update.UpdateState

/** Collects the shared controller state and shows the update dialog while it is visible. */
@Composable
fun UpdateDialogHost(controller: UpdateController) {
    val state by controller.state.collectAsStateWithLifecycle()
    val visible by controller.isDialogVisible.collectAsStateWithLifecycle()

    if (!visible) return
    if (state is UpdateState.Idle || state is UpdateState.UpToDate) return

    UpdateDialog(
        state = state,
        onDismiss = controller::dismissDialog,
        onDownload = controller::startDownload,
        onInstall = controller::launchInstaller,
    )
}

@Composable
private fun UpdateDialog(
    state: UpdateState,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 420.dp),
            shape = RoundedCornerShape(24.dp),
            color = TestflightColors.White,
            contentColor = TestflightColors.Navy,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Title(state)
                Spacer(Modifier.height(10.dp))
                Notes(state.releaseOrNull(), modifier = Modifier.heightIn(max = 300.dp))
                Spacer(Modifier.height(14.dp))
                Actions(
                    state = state,
                    onDismiss = onDismiss,
                    onDownload = onDownload,
                    onInstall = onInstall,
                )
            }
        }
    }
}

@Composable
private fun Title(state: UpdateState) {
    val title = when (state) {
        is UpdateState.Failed -> stringResource(R.string.update_dialog_error_title)
        is UpdateState.Downloaded -> stringResource(R.string.update_dialog_downloaded_title)
        is UpdateState.Downloading -> stringResource(R.string.update_dialog_downloading_title)
        else -> stringResource(R.string.update_dialog_title)
    }
    val version = state.releaseOrNull()?.versionName

    Text(
        text = title,
        style = FriendlyType.DetailVersion,
        color = TestflightColors.Navy,
    )
    if (version != null) {
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.update_dialog_version, version),
            style = FriendlyType.SheetSection,
            color = TestflightColors.CoralStrong,
        )
    }
}

@Composable
private fun Notes(release: AppRelease?, modifier: Modifier = Modifier) {
    val sections = release?.notes?.sections.orEmpty().filter { it.items.isNotEmpty() }
    val commits = release?.notes?.commits.orEmpty()
    val rawBody = release?.notes?.rawBody.orEmpty().let(::stripMarker).trim()

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        when {
            sections.isNotEmpty() -> sections.forEach { section ->
                NotesSection(section)
            }

            commits.isNotEmpty() -> commits.forEach { commit ->
                val suffix = if (commit.author.isBlank()) {
                    " (${commit.shortSha})"
                } else {
                    " (${commit.shortSha}) by @${commit.author}"
                }
                NotesBullet(commit.message + suffix)
            }

            rawBody.isNotEmpty() -> Text(
                text = rawBody,
                style = FriendlyType.Body,
                color = TestflightColors.Body,
            )

            else -> Text(
                text = stringResource(R.string.update_dialog_no_notes),
                style = FriendlyType.Body,
                color = TestflightColors.Muted,
            )
        }
    }
}

@Composable
private fun NotesSection(section: ReleaseSection) {
    if (section.title.isNotBlank()) {
        Text(
            text = section.title,
            style = FriendlyType.SheetSection,
            color = TestflightColors.CoralStrong,
            modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
        )
    }
    section.items.forEach { NotesBullet(it) }
}

@Composable
private fun NotesBullet(text: String) {
    Text(
        text = "• $text",
        style = FriendlyType.Body,
        color = TestflightColors.Body,
        modifier = Modifier.padding(bottom = 3.dp),
    )
}

@Composable
private fun Actions(
    state: UpdateState,
    onDismiss: () -> Unit,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
) {
    when (state) {
        is UpdateState.Downloading -> {
            val percent = state.progress?.let { (it * 100).toInt() } ?: 0
            ProgressTrack(progress = state.progress, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.update_dialog_progress, percent),
                style = FriendlyType.Helper,
                color = TestflightColors.Muted,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            ButtonRow(
                secondary = stringResource(R.string.update_dialog_cancel) to onDismiss,
            )
        }

        is UpdateState.Downloaded -> ButtonRow(
            secondary = stringResource(R.string.update_dialog_later) to onDismiss,
            primary = stringResource(R.string.update_dialog_install) to onInstall,
        )

        is UpdateState.Failed -> {
            val message = stringResource(updateErrorMessageRes(state.error))
            Text(
                text = message,
                style = FriendlyType.Body,
                color = TestflightColors.ErrorText,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(10.dp))
            ButtonRow(
                secondary = stringResource(R.string.update_dialog_close) to onDismiss,
                primary = if (state.release != null) {
                    stringResource(R.string.update_dialog_retry) to onDownload
                } else {
                    null
                },
            )
        }

        is UpdateState.Checking -> Box(Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.update_dialog_checking),
                style = FriendlyType.Body,
                color = TestflightColors.Muted,
            )
        }

        else -> ButtonRow(
            secondary = stringResource(R.string.update_dialog_later) to onDismiss,
            primary = stringResource(R.string.update_dialog_update) to onDownload,
        )
    }
}

@Composable
private fun ButtonRow(
    secondary: Pair<String, () -> Unit>,
    primary: Pair<String, () -> Unit>? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PillButton(
            text = secondary.first,
            onClick = secondary.second,
            style = PillStyle.Cream,
        )
        if (primary != null) {
            PillButton(
                text = primary.first,
                onClick = primary.second,
                style = PillStyle.Primary,
            )
        }
    }
}

private val markerRegex = Regex("<!--\\s*mytestflight-release\\s*\\{[\\s\\S]*?\\}\\s*-->")

private fun stripMarker(body: String): String = body.replace(markerRegex, "")

private fun UpdateState.releaseOrNull(): AppRelease? = when (this) {
    is UpdateState.Available -> release
    is UpdateState.Downloading -> release
    is UpdateState.Downloaded -> release
    is UpdateState.Failed -> release
    else -> null
}
