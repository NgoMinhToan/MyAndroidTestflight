package com.mycompany.testflight.feature.applist.detail

import com.mycompany.testflight.designsystem.text.UiText
import com.mycompany.testflight.domain.model.AppDetailModel
import com.mycompany.testflight.domain.model.AppDetails

data class AppDetailUiState(
    /** Known before the network call, so the header accent is right on the first frame. */
    val appId: String = "",
    val isLoading: Boolean = true,
    val model: AppDetailModel? = null,
    val error: UiText? = null,
) {
    val details: AppDetails? get() = model?.details
}
