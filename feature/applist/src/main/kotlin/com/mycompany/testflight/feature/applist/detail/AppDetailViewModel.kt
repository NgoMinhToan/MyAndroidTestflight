package com.mycompany.testflight.feature.applist.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycompany.testflight.designsystem.text.UiText
import com.mycompany.testflight.domain.error.AppError
import com.mycompany.testflight.domain.model.AppVersion
import com.mycompany.testflight.domain.settings.ServerSettingsRepository
import com.mycompany.testflight.domain.usecase.GetAppUseCase
import com.mycompany.testflight.feature.applist.R
import com.mycompany.testflight.feature.applist.install.InstallCoordinator
import com.mycompany.testflight.feature.applist.install.InstallEvent
import com.mycompany.testflight.feature.applist.install.InstallState
import com.mycompany.testflight.feature.applist.ui.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AppDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getApp: GetAppUseCase,
    private val installCoordinator: InstallCoordinator,
    serverSettings: ServerSettingsRepository,
) : ViewModel() {

    private val appId: String = checkNotNull(savedStateHandle.get<String>(ARG_APP_ID))

    private val _uiState = MutableStateFlow(AppDetailUiState())
    val uiState: StateFlow<AppDetailUiState> = _uiState.asStateFlow()

    val installState: StateFlow<InstallState> = installCoordinator.state
    val events: SharedFlow<InstallEvent> = installCoordinator.events

    private var loadJob: Job? = null

    init {
        load()
        viewModelScope.launch {
            serverSettings.serverUrl
                .drop(1)
                .distinctUntilChanged()
                .collect { load() }
        }
    }

    fun retry() = load()

    fun install(version: AppVersion) {
        val details = _uiState.value.details ?: return
        installCoordinator.install(details.app, version)
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getApp(appId).fold(
                onSuccess = { details ->
                    _uiState.update { it.copy(details = details, isLoading = false) }
                },
                onFailure = { error ->
                    val message = if (error is AppError.Server && error.code == 404) {
                        UiText.Res(R.string.error_app_not_found)
                    } else {
                        error.toUiText()
                    }
                    _uiState.update { it.copy(error = message, isLoading = false) }
                },
            )
        }
    }

    companion object {
        const val ARG_APP_ID = "appId"
    }
}
