package com.mycompany.testflight.feature.settings

import com.mycompany.testflight.designsystem.text.UiText

data class SettingsUiState(
    val input: String = "",
    val savedUrl: String = "",
    val defaultUrl: String = "",
    val isSaving: Boolean = false,
    val error: UiText? = null,
)
