package dev.minhtoan.mytestflight.update

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import dev.minhtoan.mytestflight.domain.di.ApplicationScope
import dev.minhtoan.mytestflight.domain.update.AppInfoProvider
import dev.minhtoan.mytestflight.domain.update.AppRelease
import dev.minhtoan.mytestflight.domain.update.DownloadedApk
import dev.minhtoan.mytestflight.domain.update.InstallLaunch
import dev.minhtoan.mytestflight.domain.update.UpdateCheck
import dev.minhtoan.mytestflight.domain.update.UpdateController
import dev.minhtoan.mytestflight.domain.update.UpdateDownload
import dev.minhtoan.mytestflight.domain.update.UpdateError
import dev.minhtoan.mytestflight.domain.update.UpdateException
import dev.minhtoan.mytestflight.domain.update.UpdateLocalStore
import dev.minhtoan.mytestflight.domain.update.UpdateRepository
import dev.minhtoan.mytestflight.domain.update.UpdateState
import dev.minhtoan.mytestflight.domain.usecase.CheckForUpdateUseCase
import dev.minhtoan.mytestflight.domain.usecase.DownloadUpdateUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@Singleton
class UpdateControllerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkForUpdate: CheckForUpdateUseCase,
    private val downloadUpdate: DownloadUpdateUseCase,
    private val repository: UpdateRepository,
    private val appInfo: AppInfoProvider,
    private val store: UpdateLocalStore,
    private val notifications: UpdateNotifications,
    private val foreground: AppForegroundTracker,
    @ApplicationScope private val scope: CoroutineScope,
) : UpdateController {

    private val _state = MutableStateFlow(restoreState())
    override val state: StateFlow<UpdateState> = _state.asStateFlow()

    private val _isDialogVisible = MutableStateFlow(false)
    override val isDialogVisible: StateFlow<Boolean> = _isDialogVisible.asStateFlow()

    private var checkJob: Job? = null
    private var downloadJob: Job? = null
    private var autoChecked = false

    /** Bumped on cancel/new download so late progress ticks cannot overwrite a reset state. */
    private var downloadGeneration = 0

    override fun autoCheckOnStart() {
        if (autoChecked) return
        autoChecked = true
        check(manual = false)
    }

    override fun checkForUpdates(manual: Boolean) {
        check(manual)
    }

    override fun startDownload() {
        val release = _state.value.releaseOrNull() ?: return
        if (downloadJob?.isActive == true) return

        _isDialogVisible.value = true
        _state.value = UpdateState.Downloading(release, downloadedBytes = 0, totalBytes = null)
        UpdateDownloadService.start(context, release.versionName)
        val generation = ++downloadGeneration

        downloadJob = scope.launch {
            try {
                downloadUpdate(release).collect { item ->
                    if (generation != downloadGeneration) return@collect
                    when (item) {
                        is UpdateDownload.Progress -> _state.value = UpdateState.Downloading(
                            release = release,
                            downloadedBytes = item.downloadedBytes,
                            totalBytes = item.totalBytes,
                        )

                        UpdateDownload.Verifying -> Unit

                        is UpdateDownload.Completed -> onDownloaded(release, item.apk)
                    }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (error: UpdateException) {
                onDownloadFailed(release, error.error)
            } catch (error: Exception) {
                onDownloadFailed(release, UpdateError.Unexpected)
            } finally {
                UpdateDownloadService.stop(context)
            }
        }
    }

    override fun dismissDialog() {
        when (val current = _state.value) {
            is UpdateState.Available -> {
                // "Later": remember this version so the next cold start stays quiet. The
                // settings button still runs a manual check and shows the dialog again.
                store.saveDismissedVersionName(current.release.versionName)
                _isDialogVisible.value = false
            }

            is UpdateState.Downloading -> {
                downloadGeneration++
                downloadJob?.cancel()
                downloadJob = null
                notifications.cancelDownload()
                _state.value = UpdateState.Available(current.release)
                _isDialogVisible.value = false
            }

            else -> _isDialogVisible.value = false
        }
    }

    override fun showDialog() {
        when (_state.value) {
            is UpdateState.Available,
            is UpdateState.Downloading,
            is UpdateState.Downloaded,
            is UpdateState.Failed,
            -> _isDialogVisible.value = true

            // A notification tap can race the automatic cold-start check; show the dialog now
            // so it is already up when the check finishes.
            UpdateState.Checking -> _isDialogVisible.value = true

            else -> check(manual = true)
        }
    }

    override fun launchInstaller() {
        val downloaded = _state.value as? UpdateState.Downloaded
        val apk = downloaded?.apk ?: store.downloadedApk() ?: return

        scope.launch {
            when (val result = repository.install(apk)) {
                InstallLaunch.Launched -> {
                    notifications.cancelDownloaded()
                    _isDialogVisible.value = false
                }

                InstallLaunch.PermissionRequired -> {
                    openUnknownSourcesSettings()
                    notifications.notifyInstallPermissionNeeded()
                }

                is InstallLaunch.Failed -> {
                    _state.value = UpdateState.Failed(downloaded?.release, result.error)
                    _isDialogVisible.value = true
                    notifications.notifyError(result.error)
                }
            }
        }
    }

    private fun check(manual: Boolean) {
        if (checkJob?.isActive == true || downloadJob?.isActive == true) return

        val previous = _state.value
        _state.value = UpdateState.Checking
        checkJob = scope.launch {
            checkForUpdate().fold(
                onSuccess = { check -> onCheckSuccess(check, manual, previous) },
                onFailure = { error ->
                    val mapped = (error as? UpdateException)?.error ?: UpdateError.Unexpected
                    if (manual) {
                        _state.value = UpdateState.Failed(previous.releaseOrNull(), mapped)
                        _isDialogVisible.value = true
                        notifications.notifyError(mapped)
                    } else {
                        // Automatic checks never interrupt the user.
                        _state.value = restoreState()
                    }
                },
            )
        }
    }

    private fun onCheckSuccess(check: UpdateCheck, manual: Boolean, previous: UpdateState) {
        val release = check.release
        if (release == null) {
            val pending = store.downloadedApk()
            _state.value = when {
                pending == null -> UpdateState.UpToDate(check.current.versionName)
                // Already running the downloaded build: the file is stale.
                pending.versionName == check.current.versionName -> {
                    store.clearDownloadedApk()
                    UpdateState.UpToDate(check.current.versionName)
                }

                else -> UpdateState.Downloaded(previous.releaseOrNull(), pending)
            }
            return
        }

        // A downloaded APK for this exact release stays installable instead of being replaced by
        // an "available" state that would make the user download it again.
        val pending = store.downloadedApk()
        if (pending != null && pending.versionName == release.versionName) {
            _state.value = UpdateState.Downloaded(release, pending)
            if (manual) _isDialogVisible.value = true
            return
        }

        val dismissed = store.dismissedVersionName() == release.versionName
        _state.value = UpdateState.Available(release)
        if (manual || !dismissed) {
            _isDialogVisible.value = true
            notifications.notifyAvailable(release.versionName)
        }
    }

    private fun onDownloaded(release: AppRelease, apk: DownloadedApk) {
        store.saveDownloadedApk(apk)
        _state.value = UpdateState.Downloaded(release, apk)
        notifications.notifyDownloaded(release.versionName)
        if (foreground.isForeground) {
            launchInstaller()
        }
    }

    private fun onDownloadFailed(release: AppRelease, error: UpdateError) {
        _state.value = UpdateState.Failed(release, error)
        _isDialogVisible.value = true
        notifications.notifyError(error)
    }

    private fun openUnknownSourcesSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}"),
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (error: ActivityNotFoundException) {
            // Nothing else to do; the notification still explains what is missing.
        } catch (error: SecurityException) {
            // Some OEM builds restrict this settings page; keep the install state intact.
        }
    }

    private fun restoreState(): UpdateState {
        val apk = store.downloadedApk() ?: return UpdateState.Idle
        if (apk.versionName == appInfo.version.versionName) {
            // The update was installed; the cached file is no longer pending.
            store.clearDownloadedApk()
            return UpdateState.Idle
        }
        return UpdateState.Downloaded(release = null, apk = apk)
    }

    private fun UpdateState.releaseOrNull(): AppRelease? = when (this) {
        is UpdateState.Available -> release
        is UpdateState.Downloading -> release
        is UpdateState.Downloaded -> release
        is UpdateState.Failed -> release
        else -> null
    }
}
