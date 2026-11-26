package dev.minhtoan.mytestflight

import android.app.Application
import dev.minhtoan.mytestflight.domain.update.UpdateController
import dev.minhtoan.mytestflight.update.UpdateNotifications
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class TestflightApplication : Application() {

    @Inject
    lateinit var updateNotifications: UpdateNotifications

    @Inject
    lateinit var updateController: UpdateController

    override fun onCreate() {
        super.onCreate()
        updateNotifications.ensureChannel()
        // One silent check per cold start; network failures never surface a dialog here.
        updateController.autoCheckOnStart()
    }
}
