package com.mycompany.testflight.data.installer

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import com.mycompany.testflight.domain.installer.AppLauncher
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntentAppLauncher @Inject constructor(
    @ApplicationContext private val context: Context,
) : AppLauncher {

    override fun open(packageName: String): Boolean {
        if (packageName.isBlank()) return false
        val intent = context.packageManager.getLaunchIntentForPackage(packageName) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (error: ActivityNotFoundException) {
            false
        } catch (error: SecurityException) {
            false
        }
    }
}
