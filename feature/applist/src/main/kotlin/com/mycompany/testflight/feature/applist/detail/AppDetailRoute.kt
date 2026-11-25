package com.mycompany.testflight.feature.applist.detail

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mycompany.testflight.designsystem.text.asString
import com.mycompany.testflight.feature.applist.install.InstallEventEffect

@Composable
fun AppDetailRoute(onBack: () -> Unit) {
    val viewModel: AppDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val installState by viewModel.installState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    InstallEventEffect(events = viewModel.events, snackbarHostState = snackbarHostState)

    // Keyed on the error alone: a details emission while the same error is alive should not
    // replay the snackbar.
    LaunchedEffect(uiState.error) {
        val error = uiState.error ?: return@LaunchedEffect
        if (uiState.details != null) {
            snackbarHostState.showSnackbar(error.asString(context))
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.openErrors.collect { message ->
            snackbarHostState.showSnackbar(message.asString(context))
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshInstalled()
    }

    AppDetailScreen(
        uiState = uiState,
        installState = installState,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onInstall = viewModel::install,
        onLaunchApp = viewModel::launchApp,
    )
}
