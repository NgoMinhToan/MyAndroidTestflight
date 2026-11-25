package com.mycompany.testflight.feature.applist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycompany.testflight.designsystem.text.UiText
import com.mycompany.testflight.domain.apk.ResolvedBuild
import com.mycompany.testflight.domain.model.AppCatalogEntry
import com.mycompany.testflight.domain.model.TestApp
import com.mycompany.testflight.domain.repository.InstalledAppRepository
import com.mycompany.testflight.domain.settings.ServerSettingsRepository
import com.mycompany.testflight.domain.usecase.GetCatalogUseCase
import com.mycompany.testflight.domain.usecase.OpenAppUseCase
import com.mycompany.testflight.feature.applist.R
import com.mycompany.testflight.feature.applist.install.InstallCoordinator
import com.mycompany.testflight.feature.applist.install.InstallEvent
import com.mycompany.testflight.feature.applist.install.InstallState
import com.mycompany.testflight.feature.applist.ui.toUiText
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val getCatalog: GetCatalogUseCase,
    private val installCoordinator: InstallCoordinator,
    private val installedRepository: InstalledAppRepository,
    private val openApp: OpenAppUseCase,
    serverSettings: ServerSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppListUiState())
    val uiState: StateFlow<AppListUiState> = _uiState.asStateFlow()

    private val _openErrors = MutableSharedFlow<UiText>(extraBufferCapacity = 4)
    val openErrors: SharedFlow<UiText> = _openErrors.asSharedFlow()

    val installState: StateFlow<InstallState> = installCoordinator.state
    val events: SharedFlow<InstallEvent> = installCoordinator.events

    private var loadJob: Job? = null
    private var entries: List<AppCatalogEntry> = emptyList()

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
            getCatalog().fold(
                onSuccess = { loaded ->
                    entries = loaded
                    publish(loaded)
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(error = error.toUiText(), isLoading = false)
                    }
                },
            )
        }
    }

    /** Re-reads installed packages, for example after returning from the system installer. */
    fun refreshInstalled() {
        val current = entries
        if (current.isEmpty()) return
        viewModelScope.launch {
            val installed = runCatching {
                installedRepository.installedApps(current.map { it.app.packageName })
            }.getOrDefault(emptyMap())
            entries = current.map { it.withInstalled(installed[it.app.packageName]) }
            publish(entries)
        }
    }

    fun install(app: TestApp, build: ResolvedBuild) {
        installCoordinator.install(app, build)
    }

    fun launchApp(packageName: String) {
        if (openApp(packageName)) return
        viewModelScope.launch {
            _openErrors.emit(UiText.Res(R.string.open_app_failed))
        }
    }

    private fun publish(loaded: List<AppCatalogEntry>) {
        _uiState.update {
            it.copy(
                apps = loaded.map { entry -> entry.app },
                resolutions = loaded.associateBy { entry -> entry.app.id },
                isLoading = false,
            )
        }
    }
}
