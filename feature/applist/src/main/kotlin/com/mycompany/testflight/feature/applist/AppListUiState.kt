package com.mycompany.testflight.feature.applist

import com.mycompany.testflight.domain.model.TestApp

data class AppListUiState(
    val isLoading: Boolean = true,
    val apps: List<TestApp> = emptyList(),
    val errorMessage: String? = null,
    val installingAppId: String? = null,
    val installStage: InstallStage? = null,
    val downloadProgress: Float? = null,
)

enum class InstallStage { Downloading, Verifying, Launching }

sealed interface AppListEvent {
    data class Message(val text: String) : AppListEvent
    data object InstallerOpened : AppListEvent
    data object OpenInstallPermissionSettings : AppListEvent
}
