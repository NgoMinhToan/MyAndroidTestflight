package dev.minhtoan.mytestflight.feature.applist.detail

import dev.minhtoan.mytestflight.designsystem.text.UiText
import dev.minhtoan.mytestflight.domain.model.AppDetailModel
import dev.minhtoan.mytestflight.domain.model.AppDetails

data class AppDetailUiState(
    /** Known before the network call, so the header accent is right on the first frame. */
    val appId: String = "",
    val isLoading: Boolean = true,
    val model: AppDetailModel? = null,
    val error: UiText? = null,
) {
    val details: AppDetails? get() = model?.details
}
