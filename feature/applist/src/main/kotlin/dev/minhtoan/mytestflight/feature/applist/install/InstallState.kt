package dev.minhtoan.mytestflight.feature.applist.install

import dev.minhtoan.mytestflight.designsystem.text.UiText

enum class InstallStage { Downloading, Verifying, Launching }

data class InstallState(
    val appId: String? = null,
    val appName: String? = null,
    val versionName: String? = null,
    val stage: InstallStage? = null,
    val progress: Float? = null,
) {
    val isActive: Boolean get() = appId != null
}

sealed interface InstallEvent {
    data object PermissionRequired : InstallEvent
    data object InstallerOpened : InstallEvent
    data class Failed(val message: UiText) : InstallEvent
}
