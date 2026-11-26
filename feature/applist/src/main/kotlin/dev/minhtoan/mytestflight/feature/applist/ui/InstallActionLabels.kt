package dev.minhtoan.mytestflight.feature.applist.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.minhtoan.mytestflight.domain.installer.InstallAction
import dev.minhtoan.mytestflight.feature.applist.R

@Composable
fun installActionLabel(action: InstallAction, noBuild: Boolean = false): String = when {
    action == InstallAction.Install -> stringResource(R.string.install)
    action == InstallAction.Update -> stringResource(R.string.update)
    action == InstallAction.Open -> stringResource(R.string.open)
    noBuild -> stringResource(R.string.list_no_build_pill)
    else -> stringResource(R.string.unsupported_build)
}

@Composable
fun installActionDescription(action: InstallAction, appName: String): String = when (action) {
    InstallAction.Install -> stringResource(R.string.install_content_description, appName)
    InstallAction.Update -> stringResource(R.string.update_content_description, appName)
    InstallAction.Open -> stringResource(R.string.open_content_description, appName)
    InstallAction.Unsupported -> stringResource(R.string.unsupported_content_description, appName)
}
