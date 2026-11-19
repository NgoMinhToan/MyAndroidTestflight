package com.mycompany.testflight.feature.applist

import com.mycompany.testflight.domain.error.AppError
import com.mycompany.testflight.domain.installer.ApkInstaller
import com.mycompany.testflight.domain.installer.InstallStatus
import com.mycompany.testflight.domain.model.AppVersion
import com.mycompany.testflight.domain.model.TestApp
import com.mycompany.testflight.domain.repository.AppRepository
import com.mycompany.testflight.domain.usecase.GetAppsUseCase
import com.mycompany.testflight.domain.usecase.InstallAppUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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
        downloadUrl = "http://localhost/api/v1/apps/demo/versions/1.0.0/download",
    )

    private val app = TestApp(
        id = "demo",
        name = "Demo App",
        packageName = "com.demo",
        iconUrl = null,
        latestVersion = version,
    )

    @Test
    fun `successful refresh exposes apps and clears loading`() = runTest {
        val viewModel = createViewModel(
            repository = FakeAppRepository(Result.success(listOf(app))),
            installer = FakeInstaller(emptyFlow()),
        )

        assertEquals(listOf(app), viewModel.uiState.value.apps)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `failed refresh exposes the error message`() = runTest {
        val viewModel = createViewModel(
            repository = FakeAppRepository(Result.failure(AppError.Network("Server unreachable"))),
            installer = FakeInstaller(emptyFlow()),
        )

        assertEquals("Server unreachable", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.apps.isEmpty())
    }

    @Test
    fun `permission required emits the settings event and resets install state`() = runTest {
        val viewModel = createViewModel(
            repository = FakeAppRepository(Result.success(listOf(app))),
            installer = FakeInstaller(flowOf(InstallStatus.PermissionRequired)),
        )

        viewModel.install(app)
        val event = viewModel.events.first()

        assertTrue(event is AppListEvent.OpenInstallPermissionSettings)
        assertNull(viewModel.uiState.value.installingAppId)
        assertNull(viewModel.uiState.value.installStage)
        assertNull(viewModel.uiState.value.downloadProgress)
    }

    @Test
    fun `download progress maps into the ui state`() = runTest {
        val statuses = Channel<InstallStatus>()
        val viewModel = createViewModel(
            repository = FakeAppRepository(Result.success(listOf(app))),
            installer = FakeInstaller(statuses.receiveAsFlow()),
        )

        viewModel.install(app)
        statuses.send(InstallStatus.Downloading(downloadedBytes = 50, totalBytes = 100))

        assertEquals("demo", viewModel.uiState.value.installingAppId)
        assertEquals(InstallStage.Downloading, viewModel.uiState.value.installStage)
        assertEquals(0.5f, viewModel.uiState.value.downloadProgress)

        statuses.close()
    }

    private fun createViewModel(
        repository: AppRepository,
        installer: ApkInstaller,
    ): AppListViewModel = AppListViewModel(
        getApps = GetAppsUseCase(repository),
        installApp = InstallAppUseCase(installer),
    )

    private class FakeAppRepository(
        private val result: Result<List<TestApp>>,
    ) : AppRepository {
        override suspend fun getApps(): Result<List<TestApp>> = result
    }

    private class FakeInstaller(
        private val statuses: Flow<InstallStatus>,
    ) : ApkInstaller {
        override fun install(app: TestApp, version: AppVersion): Flow<InstallStatus> = statuses
    }
}
