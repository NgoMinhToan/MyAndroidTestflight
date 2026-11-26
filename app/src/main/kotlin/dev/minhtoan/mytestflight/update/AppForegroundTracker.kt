package dev.minhtoan.mytestflight.update

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks whether an app activity is visible. Used to decide between opening the installer
 * immediately (foreground) and waiting for the user to tap the "downloaded" notification.
 */
@Singleton
class AppForegroundTracker @Inject constructor() {

    @Volatile
    var isForeground: Boolean = false
        private set

    fun onActivityStarted() {
        isForeground = true
    }

    fun onActivityStopped() {
        isForeground = false
    }
}
