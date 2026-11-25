package com.mycompany.testflight.update

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.mycompany.testflight.MainActivity
import com.mycompany.testflight.R
import com.mycompany.testflight.domain.update.UpdateError
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Owns the notification channel and every notification shown by the self-update flow. */
@Singleton
class UpdateNotifications @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.update_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = context.getString(R.string.update_channel_description)
            setShowBadge(true)
        }
        manager.createNotificationChannel(channel)
    }

    fun buildDownloadNotification(versionName: String, progress: Float?): Notification {
        val percent = progress?.let { (it * 100).toInt().coerceIn(0, 100) }
        val text = if (percent == null) {
            context.getString(R.string.update_notification_downloading_preparing)
        } else {
            context.getString(R.string.update_notification_downloading_text, versionName, percent)
        }
        return baseBuilder()
            .setContentTitle(context.getString(R.string.update_notification_downloading_title))
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification_update)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setProgress(100, percent ?: 0, percent == null)
            .build()
    }

    fun notifyProgress(versionName: String, progress: Float?) {
        post(DOWNLOAD_ID, buildDownloadNotification(versionName, progress))
    }

    /** Posts the foreground-service notification and pins it to the progress id. */
    fun startForegroundNotification(versionName: String): Notification =
        buildDownloadNotification(versionName, null)

    fun notifyAvailable(versionName: String) {
        val intent = MainActivity.updateIntent(context, MainActivity.EXTRA_SHOW_UPDATE)
        val notification = baseBuilder()
            .setContentTitle(context.getString(R.string.update_notification_available_title))
            .setContentText(
                context.getString(R.string.update_notification_available_text, versionName),
            )
            .setSmallIcon(R.drawable.ic_notification_update)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(pendingIntent(intent, REQUEST_AVAILABLE))
            .build()
        post(AVAILABLE_ID, notification)
    }

    fun notifyDownloaded(versionName: String) {
        cancel(DOWNLOAD_ID)
        val intent = MainActivity.updateIntent(context, MainActivity.EXTRA_INSTALL_UPDATE)
        val notification = baseBuilder()
            .setContentTitle(context.getString(R.string.update_notification_downloaded_title))
            .setContentText(
                context.getString(R.string.update_notification_downloaded_text, versionName),
            )
            .setSmallIcon(R.drawable.ic_notification_update)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent(intent, REQUEST_DOWNLOADED))
            .build()
        post(DOWNLOADED_ID, notification)
    }

    fun notifyError(error: UpdateError) {
        cancel(DOWNLOAD_ID)
        val intent = MainActivity.updateIntent(context, MainActivity.EXTRA_SHOW_UPDATE)
        val notification = baseBuilder()
            .setContentTitle(context.getString(R.string.update_notification_error_title))
            .setContentText(context.getString(updateErrorMessageRes(error)))
            .setSmallIcon(R.drawable.ic_notification_update)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setContentIntent(pendingIntent(intent, REQUEST_AVAILABLE))
            .build()
        post(ERROR_ID, notification)
    }

    fun notifyInstallPermissionNeeded() {
        val intent = MainActivity.updateIntent(context, MainActivity.EXTRA_INSTALL_UPDATE)
        val notification = baseBuilder()
            .setContentTitle(context.getString(R.string.update_notification_downloaded_title))
            .setContentText(context.getString(R.string.update_notification_permission_text))
            .setSmallIcon(R.drawable.ic_notification_update)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setContentIntent(pendingIntent(intent, REQUEST_DOWNLOADED))
            .build()
        post(DOWNLOADED_ID, notification)
    }

    fun cancelAvailable() = cancel(AVAILABLE_ID)

    fun cancelDownloaded() = cancel(DOWNLOADED_ID)

    fun cancelDownload() = cancel(DOWNLOAD_ID)

    private fun baseBuilder(): NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID)

    private fun pendingIntent(intent: Intent, requestCode: Int): PendingIntent =
        PendingIntent.getActivity(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

    private fun post(id: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Notifications are an enhancement; the dialog still shows every state.
            return
        }
        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (error: SecurityException) {
            // Permission was revoked between the check and the post.
        }
    }

    private fun cancel(id: Int) {
        NotificationManagerCompat.from(context).cancel(id)
    }

    private companion object {
        const val CHANNEL_ID = "app_updates"
        const val AVAILABLE_ID = 4101
        const val DOWNLOAD_ID = 4102
        const val DOWNLOADED_ID = 4103
        const val ERROR_ID = 4104
        const val REQUEST_AVAILABLE = 5101
        const val REQUEST_DOWNLOADED = 5102
    }
}
