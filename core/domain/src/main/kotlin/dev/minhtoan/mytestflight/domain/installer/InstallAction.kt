package dev.minhtoan.mytestflight.domain.installer

import dev.minhtoan.mytestflight.domain.apk.BuildAvailability
import dev.minhtoan.mytestflight.domain.model.AppVersion
import dev.minhtoan.mytestflight.domain.model.InstalledApp

/** What the single primary button should do for an app or a specific build. */
enum class InstallAction { Install, Update, Open, Unsupported }

object InstallActionResolver {

    fun resolve(
        version: AppVersion?,
        availability: BuildAvailability?,
        installed: InstalledApp?,
    ): InstallAction {
        if (installed != null) {
            val hasNewerBuild = version != null &&
                availability is BuildAvailability.Available &&
                version.versionCode > installed.versionCode
            return if (hasNewerBuild) InstallAction.Update else InstallAction.Open
        }
        return if (version != null && availability is BuildAvailability.Available) {
            InstallAction.Install
        } else {
            InstallAction.Unsupported
        }
    }
}
