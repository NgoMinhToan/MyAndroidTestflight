package dev.minhtoan.mytestflight.domain.update

/** One downloadable APK published with a release. [abi] is either an ABI name or "universal". */
data class ReleaseAsset(
    val abi: String,
    val name: String,
    val url: String,
    val sizeBytes: Long,
    val sha256: String?,
)

/** A rendered release-notes section, for example "New features". */
data class ReleaseSection(
    val id: String,
    val title: String,
    val items: List<String>,
)

/** A commit listed in the release notes, produced by the release workflow. */
data class ReleaseCommit(
    val shortSha: String,
    val author: String,
    val message: String,
    val type: String?,
)

data class ReleaseNotes(
    val version: String?,
    val versionCode: Int?,
    val publishedAt: String?,
    val sections: List<ReleaseSection>,
    val commits: List<ReleaseCommit>,
    val rawBody: String,
) {
    val isEmpty: Boolean
        get() = sections.all { it.items.isEmpty() } && commits.isEmpty()
}

/** A stable GitHub release of this app. */
data class AppRelease(
    val versionName: String,
    val versionCode: Int?,
    val tag: String,
    val publishedAt: String?,
    val notes: ReleaseNotes,
    /** Keyed by lower-case ABI, plus "universal" for the fallback APK. */
    val assets: Map<String, ReleaseAsset>,
    val releaseUrl: String?,
)

data class AppVersionInfo(
    val versionName: String,
    val versionCode: Long,
)

/** Result of comparing the installed app with the newest stable release. */
data class UpdateCheck(
    val current: AppVersionInfo,
    val release: AppRelease?,
) {
    val hasUpdate: Boolean get() = release != null
}

/** A downloaded, verified APK ready to hand to the system installer. */
data class DownloadedApk(
    val versionName: String,
    val path: String,
    val sha256: String?,
)

sealed interface UpdateDownload {
    data class Progress(
        val downloadedBytes: Long,
        val totalBytes: Long?,
    ) : UpdateDownload

    data object Verifying : UpdateDownload

    data class Completed(val apk: DownloadedApk) : UpdateDownload
}

enum class UpdateError {
    Network,
    Server,
    NoStableRelease,
    NoCompatibleAsset,
    ChecksumMismatch,
    Storage,
    InstallPermission,
    NoInstaller,
    Unexpected,
}

class UpdateException(
    val error: UpdateError,
    cause: Throwable? = null,
) : Exception(cause)

sealed interface UpdateState {
    data object Idle : UpdateState

    data object Checking : UpdateState

    data class UpToDate(val currentVersionName: String) : UpdateState

    data class Available(val release: AppRelease) : UpdateState

    data class Downloading(
        val release: AppRelease,
        val downloadedBytes: Long,
        val totalBytes: Long?,
    ) : UpdateState {
        val progress: Float?
            get() = totalBytes?.takeIf { it > 0 }?.let { total ->
                (downloadedBytes.toFloat() / total.toFloat()).coerceIn(0f, 1f)
            }
    }

    data class Downloaded(
        /** Null after a process restart, when only the persisted APK path is known. */
        val release: AppRelease?,
        val apk: DownloadedApk,
    ) : UpdateState

    data class Failed(
        val release: AppRelease?,
        val error: UpdateError,
    ) : UpdateState
}
