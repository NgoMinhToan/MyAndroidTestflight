package dev.minhtoan.mytestflight.feature.settings

import dev.minhtoan.mytestflight.designsystem.text.UiText

data class SettingsUiState(
    val input: String = "",
    val savedUrl: String = "",
    val defaultUrl: String = "",
    val isSaving: Boolean = false,
    val error: UiText? = null,
    val currentVersionName: String = "",
    val currentVersionCode: Long = 0,
    val updateStatus: SettingsUpdateStatus = SettingsUpdateStatus.Idle,
)

/** Mirrors the app-wide update state without exposing download/install details to this screen. */
sealed interface SettingsUpdateStatus {
    data object Idle : SettingsUpdateStatus
    data object Checking : SettingsUpdateStatus
    data object UpToDate : SettingsUpdateStatus
    data class Available(val versionName: String) : SettingsUpdateStatus
    data class Downloading(val versionName: String) : SettingsUpdateStatus
    data class Downloaded(val versionName: String?) : SettingsUpdateStatus
    data object Failed : SettingsUpdateStatus
}
