package com.mycompany.testflight.feature.settings

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mycompany.testflight.designsystem.text.asString

@Composable
fun SettingsRoute() {
    val viewModel: SettingsViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message.asString(context))
        }
    }

    SettingsScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onInputChange = viewModel::onInputChange,
        onSave = viewModel::save,
        onReset = viewModel::reset,
        onCheckForUpdates = viewModel::checkForUpdates,
    )
}
