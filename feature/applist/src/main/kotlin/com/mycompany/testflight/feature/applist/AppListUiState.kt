package com.mycompany.testflight.feature.applist

import com.mycompany.testflight.designsystem.text.UiText
import com.mycompany.testflight.domain.model.AppCatalogEntry
import com.mycompany.testflight.domain.model.TestApp

data class AppListUiState(
    val isLoading: Boolean = true,
    val apps: List<TestApp> = emptyList(),
    /** Device-resolved build and action for each app id. */
    val resolutions: Map<String, AppCatalogEntry> = emptyMap(),
    val query: String = "",
    val error: UiText? = null,
)
