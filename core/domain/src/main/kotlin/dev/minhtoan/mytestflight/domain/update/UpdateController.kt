package dev.minhtoan.mytestflight.domain.update

import kotlinx.coroutines.flow.StateFlow

/**
 * App-wide self-update state shared by the settings screen, the dialog, and notifications.
 * The implementation lives in the app module because it owns notifications and the foreground
 * download service.
 */
interface UpdateController {
    val state: StateFlow<UpdateState>

    val isDialogVisible: StateFlow<Boolean>

    /** Runs once per cold start; failures stay silent and never open the dialog. */
    fun autoCheckOnStart()

    /** Runs a check from the settings button; always opens the dialog when an update exists. */
    fun checkForUpdates(manual: Boolean)

    fun startDownload()

    /** "Later": hides the dialog and remembers the version so auto-check stays quiet. */
    fun dismissDialog()

    fun showDialog()

    fun launchInstaller()
}
