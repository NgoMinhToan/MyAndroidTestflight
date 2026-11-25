package com.mycompany.testflight.feature.applist.install

import com.mycompany.testflight.designsystem.text.UiText
import com.mycompany.testflight.domain.apk.ResolvedBuild
import com.mycompany.testflight.domain.di.ApplicationScope
import com.mycompany.testflight.domain.installer.InstallStatus
import com.mycompany.testflight.domain.model.TestApp
import com.mycompany.testflight.domain.usecase.InstallAppUseCase
import com.mycompany.testflight.feature.applist.R
import com.mycompany.testflight.feature.applist.ui.toUiText
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Singleton
class InstallCoordinator @Inject constructor(
    private val installApp: InstallAppUseCase,
    @ApplicationScope private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(InstallState())
    val state: StateFlow<InstallState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<InstallEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<InstallEvent> = _events.asSharedFlow()

    private var job: Job? = null

    fun install(app: TestApp, build: ResolvedBuild) {
        if (job?.isActive == true) return

        _state.value = InstallState(
            appId = app.id,
            appName = app.name,
            versionName = build.version.versionName,
            stage = InstallStage.Downloading,
            progress = null,
        )

        job = scope.launch {
            installApp(app, build)
                .catch { error ->
                    if (error is CancellationException) throw error
                    _events.tryEmit(
                        InstallEvent.Failed(UiText.Res(R.string.install_error_unexpected)),
                    )
                }
                .onCompletion { resetInstall() }
                .collect(::handleStatus)
        }
    }

    private fun handleStatus(status: InstallStatus) {
        when (status) {
            is InstallStatus.Downloading -> _state.update { state ->
                val total = status.totalBytes
                val progress = if (total != null && total > 0) {
                    (status.downloadedBytes.toFloat() / total.toFloat()).coerceIn(0f, 1f)
                } else {
                    null
                }
                state.copy(stage = InstallStage.Downloading, progress = progress)
            }

            InstallStatus.Verifying -> _state.update { it.copy(stage = InstallStage.Verifying) }

            InstallStatus.LaunchingInstaller -> _state.update {
                it.copy(stage = InstallStage.Launching)
            }

            InstallStatus.PermissionRequired ->
                _events.tryEmit(InstallEvent.PermissionRequired)

            InstallStatus.InstallerLaunched ->
                _events.tryEmit(InstallEvent.InstallerOpened)

            is InstallStatus.Failed ->
                _events.tryEmit(InstallEvent.Failed(status.failure.toUiText()))
        }
    }

    private fun resetInstall() {
        _state.value = InstallState()
        job = null
    }
}
