package dev.minhtoan.mytestflight.feature.applist

import dev.minhtoan.mytestflight.designsystem.text.UiText
import dev.minhtoan.mytestflight.domain.model.AppCatalogEntry
import dev.minhtoan.mytestflight.domain.model.TestApp

data class AppListUiState(
    val isLoading: Boolean = true,
    val apps: List<TestApp> = emptyList(),
    /** Device-resolved build and action for each app id. */
    val resolutions: Map<String, AppCatalogEntry> = emptyMap(),
    val query: String = "",
    val error: UiText? = null,
)
