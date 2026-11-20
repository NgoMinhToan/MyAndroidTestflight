package com.mycompany.testflight.feature.applist.components

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
import com.mycompany.testflight.designsystem.components.ProgressTrack
import com.mycompany.testflight.designsystem.tokens.FriendlyType
import com.mycompany.testflight.designsystem.tokens.TestflightColors
import com.mycompany.testflight.domain.model.TestApp
import com.mycompany.testflight.feature.applist.R
import com.mycompany.testflight.feature.applist.install.InstallState

internal data class AppRowModel(
    val app: TestApp,
    val accent: Color,
    val versionSummary: String,
)

@Composable
internal fun AppRow(
    model: AppRowModel,
    installBusy: Boolean,
    rowInstall: InstallState?,
    onOpenApp: (String) -> Unit,
    onInstall: (TestApp) -> Unit,
    modifier: Modifier = Modifier,
) {
    val app = model.app
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(role = Role.Button) { onOpenApp(app.id) },
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
                idleLabel = stringResource(R.string.install),
                state = rowInstall,
                installBusy = installBusy,
                hasBuild = app.latestVersion != null,
                onClick = { onInstall(app) },
                unavailableLabel = stringResource(R.string.list_no_build_pill),
                unavailableContentDescription = stringResource(R.string.no_builds),
                contentDescription = stringResource(
                    R.string.install_content_description,
                    app.name,
                ),
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
