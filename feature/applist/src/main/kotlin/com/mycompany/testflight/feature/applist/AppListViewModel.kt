package com.mycompany.testflight.feature.applist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycompany.testflight.domain.installer.InstallStatus
import com.mycompany.testflight.domain.model.TestApp
import com.mycompany.testflight.domain.usecase.GetAppsUseCase
import com.mycompany.testflight.domain.usecase.InstallAppUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val getApps: GetAppsUseCase,
    private val installApp: InstallAppUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppListUiState())
    val uiState: StateFlow<AppListUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<AppListEvent>(Channel.BUFFERED)
    val events: Flow<AppListEvent> = eventChannel.receiveAsFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            getApps().fold(
                onSuccess = { apps ->
                    _uiState.update { it.copy(apps = apps, isLoading = false) }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            errorMessage = error.message ?: "Unable to load apps",
                            isLoading = false,
                        )
                    }
                },
            )
        }
    }

    fun install(app: TestApp) {
        val version = app.latestVersion ?: return
        if (_uiState.value.installingAppId != null) return

        _uiState.update {
            it.copy(
                installingAppId = app.id,
                installStage = InstallStage.Downloading,
                downloadProgress = null,
            )
        }

        viewModelScope.launch {
            installApp(app, version)
                .catch { error ->
                    if (error is CancellationException) throw error
                    eventChannel.send(AppListEvent.Message(error.message ?: "Install failed"))
                    resetInstall(app.id)
                }
                .onCompletion { resetInstall(app.id) }
                .collect(::handleStatus)
        }
    }

    private fun handleStatus(status: InstallStatus) {
        when (status) {
            is InstallStatus.Downloading -> _uiState.update { state ->
                val total = status.totalBytes
                val progress = if (total != null && total > 0) {
                    (status.downloadedBytes.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                } else {
                    null
                }
                state.copy(
                    installStage = InstallStage.Downloading,
                    downloadProgress = progress,
                )
            }

            InstallStatus.Verifying -> _uiState.update {
                it.copy(installStage = InstallStage.Verifying)
            }

            InstallStatus.LaunchingInstaller -> _uiState.update {
                it.copy(installStage = InstallStage.Launching)
            }

            InstallStatus.PermissionRequired -> {
                eventChannel.trySend(AppListEvent.OpenInstallPermissionSettings)
                resetInstall()
            }

            InstallStatus.InstallerLaunched -> {
                eventChannel.trySend(AppListEvent.InstallerOpened)
                resetInstall()
            }

            is InstallStatus.Failed -> {
                eventChannel.trySend(AppListEvent.Message(status.message))
                resetInstall()
            }
        }
    }

    private fun resetInstall(appId: String? = null) {
        _uiState.update { state ->
            if (appId != null && state.installingAppId != appId) {
                state
            } else {
                state.copy(
                    installingAppId = null,
                    installStage = null,
                    downloadProgress = null,
                )
            }
        }
    }
}
