package com.mycompany.testflight.update

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.mycompany.testflight.domain.update.UpdateController
import com.mycompany.testflight.domain.update.UpdateState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Keeps the process alive while the APK downloads and mirrors the controller's progress into the
 * ongoing notification. The download coroutine itself lives in the application scope so the
 * controller and UI can observe it; this service only owns the foreground notification.
 */
@AndroidEntryPoint
class UpdateDownloadService : Service() {

    @Inject
    lateinit var controller: UpdateController

    @Inject
    lateinit var notifications: UpdateNotifications

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var observeJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val versionName = intent?.getStringExtra(EXTRA_VERSION).orEmpty()
        startForeground(notifications.startForegroundNotification(versionName))
        observeProgress(versionName)
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        observeJob?.cancel()
        scope.cancel()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        super.onDestroy()
    }

    private fun startForeground(notification: android.app.Notification) {
        val type = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else {
            0
        }
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
    }

    private fun observeProgress(versionName: String) {
        if (observeJob?.isActive == true) return
        observeJob = scope.launch {
            controller.state.collect { state ->
                when (state) {
                    is UpdateState.Downloading ->
                        notifications.notifyProgress(
                            versionName = state.release.versionName.ifBlank { versionName },
                            progress = state.progress,
                        )

                    else -> stopSelf()
                }
            }
        }
    }

    companion object {
        private const val EXTRA_VERSION = "version"
        private const val NOTIFICATION_ID = 4102

        fun start(context: Context, versionName: String) {
            val intent = Intent(context, UpdateDownloadService::class.java)
                .putExtra(EXTRA_VERSION, versionName)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, UpdateDownloadService::class.java))
        }
    }
}
