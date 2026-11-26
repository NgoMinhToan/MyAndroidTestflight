package dev.minhtoan.mytestflight.feature.applist.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.minhtoan.mytestflight.designsystem.text.UiText
import dev.minhtoan.mytestflight.domain.apk.ResolvedBuild
import dev.minhtoan.mytestflight.domain.error.AppError
import dev.minhtoan.mytestflight.domain.model.AppDetailModel
import dev.minhtoan.mytestflight.domain.repository.InstalledAppRepository
import dev.minhtoan.mytestflight.domain.settings.ServerSettingsRepository
import dev.minhtoan.mytestflight.domain.usecase.GetAppDetailUseCase
import dev.minhtoan.mytestflight.domain.usecase.OpenAppUseCase
import dev.minhtoan.mytestflight.feature.applist.R
import dev.minhtoan.mytestflight.feature.applist.install.InstallCoordinator
import dev.minhtoan.mytestflight.feature.applist.install.InstallEvent
import dev.minhtoan.mytestflight.feature.applist.install.InstallState
import dev.minhtoan.mytestflight.feature.applist.ui.toUiText
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
class AppDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getAppDetail: GetAppDetailUseCase,
    private val installCoordinator: InstallCoordinator,
    private val installedRepository: InstalledAppRepository,
    private val openApp: OpenAppUseCase,
    serverSettings: ServerSettingsRepository,
) : ViewModel() {

    private val appId: String = checkNotNull(savedStateHandle.get<String>(ARG_APP_ID))

    private val _uiState = MutableStateFlow(AppDetailUiState(appId = appId))
    val uiState: StateFlow<AppDetailUiState> = _uiState.asStateFlow()

    private val _openErrors = MutableSharedFlow<UiText>(extraBufferCapacity = 4)
    val openErrors: SharedFlow<UiText> = _openErrors.asSharedFlow()

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

    fun install(build: ResolvedBuild) {
        val details = _uiState.value.details ?: return
        installCoordinator.install(details.app, build)
    }

    fun launchApp() {
        val packageName = _uiState.value.details?.app?.packageName ?: return
        if (openApp(packageName)) return
        viewModelScope.launch {
            _openErrors.emit(UiText.Res(R.string.open_app_failed))
        }
    }

    /** Re-reads the installed version, for example after returning from the system installer. */
    fun refreshInstalled() {
        val current = _uiState.value.model ?: return
        viewModelScope.launch {
            val packageName = current.details.app.packageName
            val installed = runCatching {
                installedRepository.installedApps(listOf(packageName))
            }.getOrDefault(emptyMap())[packageName]
            val updated = current.withInstalled(installed)
            _uiState.update { it.copy(model = updated) }
        }
    }

    private fun load() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getAppDetail(appId).fold(
                onSuccess = { model: AppDetailModel ->
                    _uiState.update { it.copy(model = model, isLoading = false) }
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
