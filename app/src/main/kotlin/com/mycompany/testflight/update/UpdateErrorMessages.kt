package com.mycompany.testflight.update

import com.mycompany.testflight.R
import com.mycompany.testflight.domain.update.UpdateError

internal fun updateErrorMessageRes(error: UpdateError): Int = when (error) {
    UpdateError.Network -> R.string.update_error_network
    UpdateError.Server -> R.string.update_error_server
    UpdateError.NoStableRelease -> R.string.update_error_no_release
    UpdateError.NoCompatibleAsset -> R.string.update_error_no_asset
    UpdateError.ChecksumMismatch -> R.string.update_error_checksum
    UpdateError.Storage -> R.string.update_error_storage
    UpdateError.InstallPermission -> R.string.update_error_permission
    UpdateError.NoInstaller -> R.string.update_error_no_installer
    UpdateError.Unexpected -> R.string.update_error_unexpected
}
