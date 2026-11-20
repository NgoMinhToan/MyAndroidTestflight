package com.mycompany.testflight.feature.applist.detail

import com.mycompany.testflight.designsystem.text.UiText
import com.mycompany.testflight.domain.model.AppDetails

data class AppDetailUiState(
    val isLoading: Boolean = true,
    val details: AppDetails? = null,
    val error: UiText? = null,
)
