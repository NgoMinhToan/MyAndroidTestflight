package dev.minhtoan.mytestflight.feature.applist.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.minhtoan.mytestflight.designsystem.components.ProgressTrack
import dev.minhtoan.mytestflight.designsystem.tokens.FriendlyType
import dev.minhtoan.mytestflight.designsystem.tokens.TestflightColors
import dev.minhtoan.mytestflight.domain.apk.ResolvedBuild
import dev.minhtoan.mytestflight.domain.installer.InstallAction
import dev.minhtoan.mytestflight.domain.model.AppVersion
import dev.minhtoan.mytestflight.domain.model.TestApp
import dev.minhtoan.mytestflight.feature.applist.R
import dev.minhtoan.mytestflight.feature.applist.install.InstallState
import dev.minhtoan.mytestflight.feature.applist.ui.installActionDescription
import dev.minhtoan.mytestflight.feature.applist.ui.installActionLabel

internal data class AppRowModel(
    val app: TestApp,
    val version: AppVersion?,
    val build: ResolvedBuild?,
    val action: InstallAction,
    val accent: Color,
    val versionSummary: String,
)

@Composable
internal fun AppRow(
    model: AppRowModel,
    installBusy: Boolean,
    rowInstall: InstallState?,
    onOpenDetail: (String) -> Unit,
    onInstall: (TestApp, ResolvedBuild) -> Unit,
    onLaunchApp: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val app = model.app
    val action = model.action
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) { onOpenDetail(app.id) },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIcon(
                app = app,
                accent = model.accent,
                size = 46.dp,
                cornerRadius = 16.dp,
                letterSize = 17.sp,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.name,
                    style = FriendlyType.RowName,
                    color = TestflightColors.Navy,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = app.packageName,
                    style = FriendlyType.RowPackage,
                    color = TestflightColors.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = model.versionSummary,
                    style = FriendlyType.RowVersion,
                    color = TestflightColors.RowMeta,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(12.dp))
            InstallPill(
                label = installActionLabel(action, noBuild = model.version == null),
                state = rowInstall,
                installBusy = installBusy,
                enabled = action != InstallAction.Unsupported,
                onClick = {
                    when (action) {
                        InstallAction.Install, InstallAction.Update ->
                            model.build?.let { onInstall(app, it) }

                        InstallAction.Open -> onLaunchApp(app.packageName)
                        InstallAction.Unsupported -> Unit
                    }
                },
                idleContainerColor = when (action) {
                    InstallAction.Update -> TestflightColors.Navy
                    InstallAction.Open -> TestflightColors.Cream
                    else -> TestflightColors.CoralStrong
                },
                idleContentColor = when (action) {
                    InstallAction.Open -> TestflightColors.Navy
                    else -> TestflightColors.White
                },
                contentDescription = if (model.version == null &&
                    action == InstallAction.Unsupported
                ) {
                    stringResource(R.string.no_builds)
                } else {
                    installActionDescription(action, app.name)
                },
            )
        }
        // The divider is always the progress track at 0%; it starts after the icon so the
        // separator and the animated progress line share exactly the same bounds.
        ProgressTrack(
            progress = rowInstall?.progress ?: 0f,
            modifier = Modifier.padding(start = 76.dp, end = 18.dp),
        )
    }
}
