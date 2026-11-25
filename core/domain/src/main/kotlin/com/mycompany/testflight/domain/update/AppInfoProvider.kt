package com.mycompany.testflight.domain.update

/** Installed app identity, read from the package manager. */
interface AppInfoProvider {
    val version: AppVersionInfo
}
