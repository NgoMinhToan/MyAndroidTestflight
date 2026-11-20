package com.mycompany.testflight.feature.applist

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mycompany.testflight.designsystem.text.asString
import com.mycompany.testflight.feature.applist.install.InstallEventEffect

@Composable
fun AppListRoute(
    onOpenApp: (String) -> Unit,
    onOpenSettings: () -> Unit,
) {
    val viewModel: AppListViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val installState by viewModel.installState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    InstallEventEffect(events = viewModel.events, snackbarHostState = snackbarHostState)

    LaunchedEffect(uiState.error) {
        val error = uiState.error
        if (error != null && uiState.apps.isNotEmpty()) {
            snackbarHostState.showSnackbar(error.asString(context))
        }
    }

    AppListScreen(
        uiState = uiState,
        installState = installState,
        snackbarHostState = snackbarHostState,
        onRefresh = viewModel::refresh,
        onQueryChange = viewModel::onQueryChange,
        onInstall = viewModel::install,
        onOpenApp = onOpenApp,
        onOpenSettings = onOpenSettings,
    )
}
