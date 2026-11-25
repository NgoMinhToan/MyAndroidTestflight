package com.mycompany.testflight.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mycompany.testflight.designsystem.text.UiText
import com.mycompany.testflight.domain.settings.ServerSettingsRepository
import com.mycompany.testflight.domain.settings.ServerUrlError
import com.mycompany.testflight.domain.settings.ServerUrlValidation
import com.mycompany.testflight.domain.update.AppInfoProvider
import com.mycompany.testflight.domain.update.UpdateController
import com.mycompany.testflight.domain.update.UpdateState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: ServerSettingsRepository,
    private val updateController: UpdateController,
    appInfo: AppInfoProvider,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            input = repository.serverUrl.value,
            savedUrl = repository.serverUrl.value,
            defaultUrl = repository.defaultServerUrl,
            currentVersionName = appInfo.version.versionName,
            currentVersionCode = appInfo.version.versionCode,
            updateStatus = updateController.state.value.toSettingsStatus(),
        ),
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    // One-shot messages (snackbars) are events, not screen state: they must not be replayed
    // after a configuration change or tab restore.
    private val _messages = MutableSharedFlow<UiText>(extraBufferCapacity = 1)
    val messages: SharedFlow<UiText> = _messages.asSharedFlow()

    init {
        viewModelScope.launch {
            updateController.state.collect { state ->
                _uiState.update { it.copy(updateStatus = state.toSettingsStatus()) }
            }
        }
    }

    fun onInputChange(value: String) {
        _uiState.update { it.copy(input = value, error = null) }
    }

    fun checkForUpdates() {
        updateController.checkForUpdates(manual = true)
    }

    fun save() {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            when (val result = repository.updateServerUrl(_uiState.value.input)) {
                is ServerUrlValidation.Valid -> {
                    _uiState.update {
                        it.copy(
                            input = result.normalizedUrl,
                            savedUrl = result.normalizedUrl,
                            isSaving = false,
                        )
                    }
                    _messages.tryEmit(UiText.Res(R.string.settings_saved))
                }

                is ServerUrlValidation.Invalid -> _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = result.reason.toUiText(),
                    )
                }
            }
        }
    }

    fun reset() {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            repository.resetToDefault()
            _uiState.update {
                it.copy(
                    input = repository.serverUrl.value,
                    savedUrl = repository.serverUrl.value,
                    isSaving = false,
                )
            }
            _messages.tryEmit(UiText.Res(R.string.settings_reset_done))
        }
    }
}

private fun UpdateState.toSettingsStatus(): SettingsUpdateStatus = when (this) {
    UpdateState.Idle -> SettingsUpdateStatus.Idle
    UpdateState.Checking -> SettingsUpdateStatus.Checking
    is UpdateState.UpToDate -> SettingsUpdateStatus.UpToDate
    is UpdateState.Available -> SettingsUpdateStatus.Available(release.versionName)
    is UpdateState.Downloading -> SettingsUpdateStatus.Downloading(release.versionName)
    is UpdateState.Downloaded -> SettingsUpdateStatus.Downloaded(
        release?.versionName ?: apk.versionName,
    )
    is UpdateState.Failed -> SettingsUpdateStatus.Failed
}

private fun ServerUrlError.toUiText(): UiText = when (this) {
    ServerUrlError.Empty -> UiText.Res(R.string.settings_url_error_empty)
    ServerUrlError.Malformed -> UiText.Res(R.string.settings_url_error_malformed)
    ServerUrlError.UnsupportedScheme -> UiText.Res(R.string.settings_url_error_scheme)
    ServerUrlError.MissingHost -> UiText.Res(R.string.settings_url_error_host)
    ServerUrlError.PathNotAllowed -> UiText.Res(R.string.settings_url_error_path)
    ServerUrlError.PortOutOfRange -> UiText.Res(R.string.settings_url_error_port)
}
