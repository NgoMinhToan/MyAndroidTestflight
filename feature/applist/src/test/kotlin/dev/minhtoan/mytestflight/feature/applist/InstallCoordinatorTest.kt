package dev.minhtoan.mytestflight.feature.applist

import dev.minhtoan.mytestflight.designsystem.text.UiText
import dev.minhtoan.mytestflight.domain.apk.ResolvedBuild
import dev.minhtoan.mytestflight.domain.installer.ApkInstaller
import dev.minhtoan.mytestflight.domain.installer.InstallFailure
import dev.minhtoan.mytestflight.domain.installer.InstallStatus
import dev.minhtoan.mytestflight.domain.model.ApkArtifact
import dev.minhtoan.mytestflight.domain.model.AppVersion
import dev.minhtoan.mytestflight.domain.model.TestApp
import dev.minhtoan.mytestflight.domain.usecase.InstallAppUseCase
import dev.minhtoan.mytestflight.feature.applist.install.InstallCoordinator
import dev.minhtoan.mytestflight.feature.applist.install.InstallEvent
import dev.minhtoan.mytestflight.feature.applist.install.InstallStage
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InstallCoordinatorTest {

    private val version = AppVersion(
        versionName = "1.0.0",
        versionCode = 1,
        minSdk = 26,
        releaseNotes = "First build",
        publishedAt = "2026-11-19T08:00:00.000Z",
        sizeBytes = 1_024L,
        sha256 = null,
        downloadPath = "/api/v1/apps/demo/versions/1.0.0/download",
    )

    private val artifact = ApkArtifact(
        abi = "universal",
        sizeBytes = 1_024L,
        sha256 = null,
        downloadPath = "/api/v1/apps/demo/versions/1.0.0/download",
    )

    private val build = ResolvedBuild(version, artifact)

    private val app = TestApp(
        id = "demo",
        name = "Demo App",
        packageName = "com.demo",
        iconUrl = null,
        latestVersion = version,
    )

    @Test
    fun `maps download progress into the state`() = runTest {
        val statuses = Channel<InstallStatus>(Channel.UNLIMITED)
        val coordinator = createCoordinator(statuses)

        coordinator.install(app, build)
        runCurrent()
        statuses.send(InstallStatus.Downloading(downloadedBytes = 50, totalBytes = 100))
        runCurrent()

        assertEquals("demo", coordinator.state.value.appId)
        assertEquals("1.0.0", coordinator.state.value.versionName)
        assertEquals(InstallStage.Downloading, coordinator.state.value.stage)
        assertEquals(0.5f, coordinator.state.value.progress)
    }

    @Test
    fun `emits the permission event and resets when the flow completes`() = runTest {
        val statuses = Channel<InstallStatus>(Channel.UNLIMITED)
        val coordinator = createCoordinator(statuses)
        val event = async(start = CoroutineStart.UNDISPATCHED) { coordinator.events.first() }

        coordinator.install(app, build)
        runCurrent()
        statuses.send(InstallStatus.PermissionRequired)
        statuses.close()
        runCurrent()

        assertEquals(InstallEvent.PermissionRequired, event.await())
        assertFalse(coordinator.state.value.isActive)
        assertNull(coordinator.state.value.stage)
    }

    @Test
    fun `emits the installer opened event`() = runTest {
        val statuses = Channel<InstallStatus>(Channel.UNLIMITED)
        val coordinator = createCoordinator(statuses)
        val event = async(start = CoroutineStart.UNDISPATCHED) { coordinator.events.first() }

        coordinator.install(app, build)
        runCurrent()
        statuses.send(InstallStatus.InstallerLaunched)
        statuses.close()
        runCurrent()

        assertEquals(InstallEvent.InstallerOpened, event.await())
        assertFalse(coordinator.state.value.isActive)
    }

    @Test
    fun `maps a typed failure to localized text`() = runTest {
        val statuses = Channel<InstallStatus>(Channel.UNLIMITED)
        val coordinator = createCoordinator(statuses)
        val event = async(start = CoroutineStart.UNDISPATCHED) { coordinator.events.first() }

        coordinator.install(app, build)
        runCurrent()
        statuses.send(InstallStatus.Failed(InstallFailure.ChecksumMismatch))
        statuses.close()
        runCurrent()

        val failure = event.await() as InstallEvent.Failed
        assertEquals(UiText.Res(R.string.install_error_checksum), failure.message)
        assertFalse(coordinator.state.value.isActive)
    }

    @Test
    fun `ignores a second install while one is active`() = runTest {
        val statuses = Channel<InstallStatus>(Channel.UNLIMITED)
        val installer = FakeInstaller(statuses.receiveAsFlow())
        val coordinator = InstallCoordinator(InstallAppUseCase(installer), backgroundScope)

        coordinator.install(app, build)
        runCurrent()
        coordinator.install(app, build)
        runCurrent()

        assertEquals(1, installer.calls)
        assertTrue(coordinator.state.value.isActive)
    }

    private fun kotlinx.coroutines.test.TestScope.createCoordinator(
        statuses: Channel<InstallStatus>,
    ): InstallCoordinator = InstallCoordinator(
        installApp = InstallAppUseCase(FakeInstaller(statuses.receiveAsFlow())),
        scope = backgroundScope,
    )

    private class FakeInstaller(
        private val statuses: Flow<InstallStatus>,
    ) : ApkInstaller {
        var calls = 0

        override fun install(app: TestApp, build: ResolvedBuild): Flow<InstallStatus> {
            calls++
            return statuses
        }
    }
}
