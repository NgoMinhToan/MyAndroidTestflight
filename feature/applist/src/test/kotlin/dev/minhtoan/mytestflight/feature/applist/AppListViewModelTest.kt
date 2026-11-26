package dev.minhtoan.mytestflight.feature.applist

import dev.minhtoan.mytestflight.designsystem.text.UiText
import dev.minhtoan.mytestflight.domain.apk.ResolvedBuild
import dev.minhtoan.mytestflight.domain.device.DeviceProfile
import dev.minhtoan.mytestflight.domain.device.DeviceProfileProvider
import dev.minhtoan.mytestflight.domain.error.AppError
import dev.minhtoan.mytestflight.domain.installer.ApkInstaller
import dev.minhtoan.mytestflight.domain.installer.AppLauncher
import dev.minhtoan.mytestflight.domain.installer.InstallAction
import dev.minhtoan.mytestflight.domain.installer.InstallStatus
import dev.minhtoan.mytestflight.domain.model.ApkArtifact
import dev.minhtoan.mytestflight.domain.model.AppDetails
import dev.minhtoan.mytestflight.domain.model.AppVersion
import dev.minhtoan.mytestflight.domain.model.InstalledApp
import dev.minhtoan.mytestflight.domain.model.TestApp
import dev.minhtoan.mytestflight.domain.repository.AppRepository
import dev.minhtoan.mytestflight.domain.repository.InstalledAppRepository
import dev.minhtoan.mytestflight.domain.settings.ServerSettingsRepository
import dev.minhtoan.mytestflight.domain.settings.ServerUrlValidation
import dev.minhtoan.mytestflight.domain.usecase.GetCatalogUseCase
import dev.minhtoan.mytestflight.domain.usecase.InstallAppUseCase
import dev.minhtoan.mytestflight.domain.usecase.OpenAppUseCase
import dev.minhtoan.mytestflight.feature.applist.install.InstallCoordinator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

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
        sizeBytes = version.sizeBytes,
        sha256 = null,
        downloadPath = version.downloadPath,
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
    fun `successful refresh exposes apps and clears loading`() = runTest {
        val repository = FakeAppRepository(Result.success(listOf(app)))

        val viewModel = createViewModel(repository)
        runCurrent()

        assertEquals(listOf(app), viewModel.uiState.value.apps)
        assertEquals(InstallAction.Install, viewModel.uiState.value.resolutions["demo"]?.action)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun `failed refresh exposes a localized error`() = runTest {
        val repository = FakeAppRepository(Result.failure(AppError.Network()))

        val viewModel = createViewModel(repository)
        runCurrent()

        assertEquals(UiText.Res(R.string.error_network), viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.apps.isEmpty())
    }

    @Test
    fun `query updates the ui state`() = runTest {
        val viewModel = createViewModel(FakeAppRepository(Result.success(listOf(app))))

        viewModel.onQueryChange("demo")

        assertEquals("demo", viewModel.uiState.value.query)
    }

    @Test
    fun `changing the server url reloads the catalog`() = runTest {
        val repository = FakeAppRepository(Result.success(listOf(app)))
        val settings = FakeServerSettings("http://127.0.0.1:8080")
        val viewModel = createViewModel(repository, settings)
        runCurrent()
        assertEquals(1, repository.calls)

        settings.serverUrl.value = "https://example.com:9443"
        runCurrent()

        assertEquals(2, repository.calls)
        assertEquals(listOf(app), viewModel.uiState.value.apps)
    }

    @Test
    fun `an installed app behind the server offers an update`() = runTest {
        val installed = mapOf("com.demo" to InstalledApp("com.demo", versionCode = 0))
        val viewModel = createViewModel(
            repository = FakeAppRepository(Result.success(listOf(app))),
            installed = installed,
        )
        runCurrent()

        assertEquals(InstallAction.Update, viewModel.uiState.value.resolutions["demo"]?.action)
    }

    @Test
    fun `an up-to-date installed app offers open`() = runTest {
        val installed = mapOf("com.demo" to InstalledApp("com.demo", versionCode = 1))
        val viewModel = createViewModel(
            repository = FakeAppRepository(Result.success(listOf(app))),
            installed = installed,
        )
        runCurrent()

        assertEquals(InstallAction.Open, viewModel.uiState.value.resolutions["demo"]?.action)
    }

    @Test
    fun `refreshing installed packages updates actions without reloading the catalog`() = runTest {
        val repository = FakeAppRepository(Result.success(listOf(app)))
        val installedRepository = FakeInstalledAppRepository(
            mapOf("com.demo" to InstalledApp("com.demo", versionCode = 1)),
        )
        val viewModel = createViewModel(repository, installedRepository = installedRepository)
        runCurrent()
        assertEquals(1, repository.calls)

        viewModel.refreshInstalled()
        runCurrent()

        assertEquals(1, repository.calls)
        assertEquals(InstallAction.Open, viewModel.uiState.value.resolutions["demo"]?.action)
    }

    @Test
    fun `falls back to the newest older build that supports the device`() = runTest {
        val incompatible = version.copy(
            versionName = "2.0.0",
            versionCode = 20,
            artifacts = listOf(
                ApkArtifact(
                    abi = "x86_64",
                    sizeBytes = 1_024L,
                    sha256 = null,
                    downloadPath = "/api/v1/apps/demo/versions/2.0.0/download?abi=x86_64",
                ),
            ),
        )
        val compatible = version.copy(
            versionName = "1.5.0",
            versionCode = 15,
            artifacts = listOf(
                ApkArtifact(
                    abi = "arm64-v8a",
                    sizeBytes = 2_048L,
                    sha256 = null,
                    downloadPath = "/api/v1/apps/demo/versions/1.5.0/download?abi=arm64-v8a",
                ),
            ),
        )
        val latestUnsupported = app.copy(latestVersion = incompatible)
        val repository = FakeAppRepository(
            apps = Result.success(listOf(latestUnsupported)),
            details = Result.success(
                AppDetails(
                    app = latestUnsupported,
                    versions = listOf(incompatible, compatible),
                ),
            ),
        )

        val viewModel = createViewModel(repository)
        runCurrent()

        val entry = viewModel.uiState.value.resolutions["demo"]
        assertEquals("1.5.0", entry?.version?.versionName)
        assertEquals(InstallAction.Install, entry?.action)
        assertEquals("arm64-v8a", entry?.build?.artifact?.abi)
        assertEquals(1, repository.detailsCalls)
    }

    @Test
    fun `install delegates the resolved build to the coordinator`() = runTest {
        val installer = FakeInstaller(Channel<InstallStatus>(Channel.UNLIMITED).receiveAsFlow())
        val viewModel = createViewModel(
            repository = FakeAppRepository(Result.success(listOf(app))),
            installer = installer,
        )
        runCurrent()

        viewModel.install(app, build)
        runCurrent()

        assertEquals(1, installer.calls)
        assertEquals("demo", viewModel.installState.value.appId)
        assertEquals("1.0.0", viewModel.installState.value.versionName)
    }

    private fun TestScope.createViewModel(
        repository: AppRepository,
        settings: ServerSettingsRepository = FakeServerSettings(),
        installer: ApkInstaller = FakeInstaller(
            Channel<InstallStatus>(Channel.UNLIMITED).receiveAsFlow(),
        ),
        installed: Map<String, InstalledApp> = emptyMap(),
        installedRepository: FakeInstalledAppRepository = FakeInstalledAppRepository(installed),
        launcher: AppLauncher = FakeAppLauncher(),
    ): AppListViewModel {
        val coordinator = InstallCoordinator(InstallAppUseCase(installer), backgroundScope)
        return AppListViewModel(
            getCatalog = GetCatalogUseCase(
                repository = repository,
                installedRepository = installedRepository,
                deviceProfileProvider = FakeDeviceProfileProvider(),
            ),
            installCoordinator = coordinator,
            installedRepository = installedRepository,
            openApp = OpenAppUseCase(launcher),
            serverSettings = settings,
        )
    }

    private class FakeAppRepository(
        private val apps: Result<List<TestApp>>,
        private val details: Result<AppDetails>? = null,
    ) : AppRepository {
        var calls = 0
        var detailsCalls = 0

        override suspend fun getApps(): Result<List<TestApp>> {
            calls++
            return apps
        }

        override suspend fun getApp(id: String): Result<AppDetails> {
            detailsCalls++
            return details ?: Result.failure(AppError.Unexpected())
        }
    }

    private class FakeInstalledAppRepository(
        private val installed: Map<String, InstalledApp>,
    ) : InstalledAppRepository {
        override suspend fun installedApps(
            packageNames: Collection<String>,
        ): Map<String, InstalledApp> = installed.filterKeys { it in packageNames }
    }

    private class FakeDeviceProfileProvider : DeviceProfileProvider {
        override fun current(): DeviceProfile = DeviceProfile(
            supportedAbis = listOf("arm64-v8a", "armeabi-v7a"),
            sdkInt = 34,
        )
    }

    private class FakeAppLauncher : AppLauncher {
        var opened: String? = null

        override fun open(packageName: String): Boolean {
            opened = packageName
            return true
        }
    }

    private class FakeInstaller(
        private val statuses: Flow<InstallStatus>,
    ) : ApkInstaller {
        var calls = 0

        override fun install(app: TestApp, build: ResolvedBuild): Flow<InstallStatus> {
            calls++
            return statuses
        }
    }

    private class FakeServerSettings(
        initial: String = "http://127.0.0.1:8080",
    ) : ServerSettingsRepository {
        override val serverUrl = MutableStateFlow(initial)
        override val defaultServerUrl: String = initial

        override suspend fun updateServerUrl(raw: String): ServerUrlValidation =
            ServerUrlValidation.Valid(raw)

        override suspend fun resetToDefault() = Unit
    }
}
