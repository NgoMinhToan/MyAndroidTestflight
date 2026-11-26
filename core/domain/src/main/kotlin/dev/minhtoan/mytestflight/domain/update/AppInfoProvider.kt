package dev.minhtoan.mytestflight.domain.update

/** Installed app identity, read from the package manager. */
interface AppInfoProvider {
    val version: AppVersionInfo
}
