package dev.minhtoan.mytestflight.domain.update

/**
 * Small persisted state for self-update: the version the user chose to skip, and the APK that was
 * already downloaded so a notification can still open it after the process is recreated.
 */
interface UpdateLocalStore {
    fun dismissedVersionName(): String?

    fun saveDismissedVersionName(versionName: String)

    fun downloadedApk(): DownloadedApk?

    fun saveDownloadedApk(apk: DownloadedApk)

    fun clearDownloadedApk()
}
