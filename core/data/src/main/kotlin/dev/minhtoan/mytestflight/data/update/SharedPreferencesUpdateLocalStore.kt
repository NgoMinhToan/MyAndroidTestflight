package dev.minhtoan.mytestflight.data.update

import android.content.Context
import dev.minhtoan.mytestflight.domain.update.DownloadedApk
import dev.minhtoan.mytestflight.domain.update.UpdateLocalStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesUpdateLocalStore @Inject constructor(
    @ApplicationContext context: Context,
) : UpdateLocalStore {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun dismissedVersionName(): String? =
        preferences.getString(KEY_DISMISSED_VERSION, null)

    override fun saveDismissedVersionName(versionName: String) {
        preferences.edit().putString(KEY_DISMISSED_VERSION, versionName).apply()
    }

    override fun downloadedApk(): DownloadedApk? {
        val path = preferences.getString(KEY_APK_PATH, null) ?: return null
        val version = preferences.getString(KEY_APK_VERSION, null) ?: return null
        return DownloadedApk(
            versionName = version,
            path = path,
            sha256 = preferences.getString(KEY_APK_SHA256, null),
        )
    }

    override fun saveDownloadedApk(apk: DownloadedApk) {
        preferences.edit()
            .putString(KEY_APK_PATH, apk.path)
            .putString(KEY_APK_VERSION, apk.versionName)
            .putString(KEY_APK_SHA256, apk.sha256)
            .apply()
    }

    override fun clearDownloadedApk() {
        preferences.edit()
            .remove(KEY_APK_PATH)
            .remove(KEY_APK_VERSION)
            .remove(KEY_APK_SHA256)
            .apply()
    }

    private companion object {
        const val PREFS_NAME = "self_update"
        const val KEY_DISMISSED_VERSION = "dismissed_version"
        const val KEY_APK_PATH = "downloaded_apk_path"
        const val KEY_APK_VERSION = "downloaded_apk_version"
        const val KEY_APK_SHA256 = "downloaded_apk_sha256"
    }
}
