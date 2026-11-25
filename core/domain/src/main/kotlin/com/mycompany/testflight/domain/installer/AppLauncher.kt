package com.mycompany.testflight.domain.installer

/** Opens an installed app by package name. Returns false when it has no launchable activity. */
interface AppLauncher {
    fun open(packageName: String): Boolean
}
