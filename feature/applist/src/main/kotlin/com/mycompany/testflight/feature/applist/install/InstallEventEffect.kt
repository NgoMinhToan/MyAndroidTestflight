package com.mycompany.testflight.feature.applist.install

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.mycompany.testflight.designsystem.text.asString
import com.mycompany.testflight.feature.applist.R
import kotlinx.coroutines.flow.Flow

@Composable
fun InstallEventEffect(
    events: Flow<InstallEvent>,
    snackbarHostState: SnackbarHostState,
) {
    val context = LocalContext.current

    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                InstallEvent.InstallerOpened ->
                    snackbarHostState.showSnackbar(context.getString(R.string.installer_opened))

                InstallEvent.PermissionRequired -> {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}"),
                    )
                    try {
                        context.startActivity(intent)
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.allow_installs),
                        )
                    } catch (error: ActivityNotFoundException) {
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.settings_unavailable),
                        )
                    }
                }

                is InstallEvent.Failed ->
                    snackbarHostState.showSnackbar(event.message.asString(context))
            }
        }
    }
}
