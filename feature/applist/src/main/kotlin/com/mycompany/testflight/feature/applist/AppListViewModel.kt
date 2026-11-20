package com.mycompany.testflight.feature.applist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycompany.testflight.domain.model.TestApp
import com.mycompany.testflight.domain.settings.ServerSettingsRepository
import com.mycompany.testflight.domain.usecase.GetAppsUseCase
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
class AppListViewModel @Inject constructor(
    private val getApps: GetAppsUseCase,
    private val installCoordinator: InstallCoordinator,
    serverSettings: ServerSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppListUiState())
    val uiState: StateFlow<AppListUiState> = _uiState.asStateFlow()

    val installState: StateFlow<InstallState> = installCoordinator.state
    val events: SharedFlow<InstallEvent> = installCoordinator.events

    private var loadJob: Job? = null

    init {
        refresh()
        viewModelScope.launch {
            serverSettings.serverUrl
                .drop(1)
                .distinctUntilChanged()
                .collect { refresh() }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun refresh() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getApps().fold(
                onSuccess = { apps ->
                    _uiState.update { it.copy(apps = apps, isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(error = error.toUiText(), isLoading = false)
                    }
                },
            )
        }
    }

    fun install(app: TestApp) {
        val version = app.latestVersion ?: return
        installCoordinator.install(app, version)
    }
}
