package com.mycompany.testflight.feature.applist

import com.mycompany.testflight.designsystem.text.UiText
import com.mycompany.testflight.domain.model.TestApp

data class AppListUiState(
    val isLoading: Boolean = true,
    val apps: List<TestApp> = emptyList(),
    val query: String = "",
    val error: UiText? = null,
)
