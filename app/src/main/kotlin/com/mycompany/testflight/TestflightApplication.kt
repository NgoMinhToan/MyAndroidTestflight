package com.mycompany.testflight

import android.app.Application
import com.mycompany.testflight.domain.update.UpdateController
import com.mycompany.testflight.update.UpdateNotifications
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
