package dev.minhtoan.mytestflight.feature.applist.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.minhtoan.mytestflight.designsystem.components.ProgressTrack
import dev.minhtoan.mytestflight.designsystem.tokens.FriendlyDimens
import dev.minhtoan.mytestflight.designsystem.tokens.FriendlyType
import dev.minhtoan.mytestflight.designsystem.tokens.PillShape
import dev.minhtoan.mytestflight.designsystem.tokens.TestflightColors
import dev.minhtoan.mytestflight.domain.apk.ResolvedBuild
import dev.minhtoan.mytestflight.domain.model.AppVersion
import dev.minhtoan.mytestflight.feature.applist.R
import dev.minhtoan.mytestflight.feature.applist.components.InstallPill
import dev.minhtoan.mytestflight.feature.applist.install.InstallState
import dev.minhtoan.mytestflight.feature.applist.ui.rememberFormattedPublishedDate
import dev.minhtoan.mytestflight.feature.applist.ui.rememberShortFileSize
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun VersionCard(
    appName: String,
    version: AppVersion,
    build: ResolvedBuild?,
    isLatest: Boolean,
    rowInstall: InstallState?,
    installBusy: Boolean,
    onInstall: (ResolvedBuild) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sizeText = rememberShortFileSize(build?.artifact?.sizeBytes ?: version.sizeBytes)
    val dateText = rememberFormattedPublishedDate(version.publishedAt, FormatStyle.SHORT)
    val minSdk = version.minSdk
    val meta = if (minSdk != null) {
        stringResource(
            R.string.detail_version_meta,
            version.versionCode,
            minSdk,
            dateText ?: "",
        )
    } else {
        stringResource(
            R.string.detail_version_meta_no_sdk,
            version.versionCode,
            dateText ?: "",
        )
    }.trimEnd().trimEnd('·').trimEnd()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 9.dp),
        shape = RoundedCornerShape(FriendlyDimens.CardCorner),
        color = TestflightColors.White,
        contentColor = TestflightColors.Navy,
        border = BorderStroke(FriendlyDimens.Hairline, TestflightColors.Line),
    ) {
        Box {
            Column(modifier = Modifier.padding(start = 14.dp, end = 14.dp, top = 13.dp, bottom = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(R.string.detail_version_short, version.versionName),
                        style = FriendlyType.DetailVersion,
                        color = TestflightColors.Navy,
                    )
                    if (isLatest) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.detail_latest_badge)
                                .uppercase(Locale.getDefault()),
                            style = FriendlyType.Badge,
                            color = TestflightColors.White,
                            modifier = Modifier
                                .clip(PillShape)
                                .background(TestflightColors.CoralStrong)
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = sizeText,
                        style = FriendlyType.Meta,
                        color = TestflightColors.TextSecondary,
                        maxLines = 1,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = meta,
                    style = FriendlyType.MonoMeta,
                    color = TestflightColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(7.dp))
                Text(
                    text = version.releaseNotes?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.detail_no_release_notes),
                    style = FriendlyType.VersionNote,
                    color = TestflightColors.VersionNote,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                ) {
                    Spacer(Modifier.weight(1f))
                    InstallPill(
                        label = if (build != null) {
                            stringResource(R.string.detail_install_this_version)
                        } else {
                            stringResource(R.string.unsupported_build)
                        },
                        state = rowInstall,
                        installBusy = installBusy,
                        enabled = build != null,
                        onClick = { build?.let(onInstall) },
                        idleContainerColor = TestflightColors.Cream,
                        idleContentColor = TestflightColors.Navy,
                        textStyle = FriendlyType.VersionPillLabel,
                        contentDescription = if (build != null) {
                            stringResource(
                                R.string.detail_install_version_content_description,
                                appName,
                                version.versionName,
                            )
                        } else {
                            stringResource(R.string.unsupported_content_description, appName)
                        },
                    )
                }
            }
            if (rowInstall != null && rowInstall.progress != null) {
                ProgressTrack(
                    progress = rowInstall.progress,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 14.dp),
                )
            }
        }
    }
}
