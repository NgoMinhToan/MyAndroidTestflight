package com.mycompany.testflight.feature.applist

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AppListRoute() {
    val viewModel: AppListViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AppListEvent.Message -> snackbarHostState.showSnackbar(event.text)

                AppListEvent.InstallerOpened ->
                    snackbarHostState.showSnackbar(context.getString(R.string.installer_opened))

                AppListEvent.OpenInstallPermissionSettings -> {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                        Uri.parse("package:${context.packageName}"),
                    )
                    try {
                        context.startActivity(intent)
                        snackbarHostState.showSnackbar(context.getString(R.string.allow_installs))
                    } catch (error: ActivityNotFoundException) {
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.settings_unavailable),
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        val message = uiState.errorMessage
        if (message != null && uiState.apps.isNotEmpty()) {
            snackbarHostState.showSnackbar(message)
        }
    }

    AppListScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onRefresh = viewModel::refresh,
        onInstall = viewModel::install,
    )
}
