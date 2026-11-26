package dev.minhtoan.mytestflight.feature.applist.ui

import dev.minhtoan.mytestflight.designsystem.text.UiText
import dev.minhtoan.mytestflight.domain.error.AppError
import dev.minhtoan.mytestflight.domain.installer.InstallFailure
import dev.minhtoan.mytestflight.feature.applist.R

fun Throwable.toUiText(): UiText =
    (this as? AppError)?.toUiText() ?: UiText.Res(R.string.error_unexpected)

fun AppError.toUiText(): UiText = when (this) {
    is AppError.Network -> UiText.Res(R.string.error_network)
    is AppError.Server -> UiText.Res(R.string.error_server_http_format, listOf(code))
    is AppError.InvalidServerUrl -> UiText.Res(R.string.error_invalid_server_url)
    is AppError.Unexpected -> UiText.Res(R.string.error_unexpected)
}

fun InstallFailure.toUiText(): UiText = when (this) {
    is InstallFailure.Http -> UiText.Res(R.string.install_error_http_format, listOf(code))
    InstallFailure.EmptyBody -> UiText.Res(R.string.install_error_empty_body)
    InstallFailure.ChecksumMismatch -> UiText.Res(R.string.install_error_checksum)
    InstallFailure.NoInstaller -> UiText.Res(R.string.install_error_no_installer)
    InstallFailure.InvalidServerUrl -> UiText.Res(R.string.install_error_invalid_server_url)
    is InstallFailure.Io -> UiText.Res(R.string.install_error_io)
    is InstallFailure.Unexpected -> UiText.Res(R.string.install_error_unexpected)
}
