package com.mycompany.testflight.feature.applist

import androidx.lifecycle.SavedStateHandle
import com.mycompany.testflight.designsystem.text.UiText
import com.mycompany.testflight.domain.error.AppError
import com.mycompany.testflight.domain.installer.ApkInstaller
import com.mycompany.testflight.domain.installer.InstallStatus
import com.mycompany.testflight.domain.model.AppDetails
import com.mycompany.testflight.domain.model.AppVersion
import com.mycompany.testflight.domain.model.TestApp
import com.mycompany.testflight.domain.repository.AppRepository
import com.mycompany.testflight.domain.settings.ServerSettingsRepository
import com.mycompany.testflight.domain.settings.ServerUrlValidation
import com.mycompany.testflight.domain.usecase.GetAppUseCase
import com.mycompany.testflight.domain.usecase.InstallAppUseCase
import com.mycompany.testflight.feature.applist.detail.AppDetailViewModel
import com.mycompany.testflight.feature.applist.install.InstallCoordinator
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
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppDetailViewModelTest {

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

    private val app = TestApp(
        id = "demo",
        name = "Demo App",
        packageName = "com.demo",
        iconUrl = null,
        latestVersion = version,
        description = "Internal build",
        developer = "Platform Team",
        versionCount = 1,
    )

    private val details = AppDetails(app = app, versions = listOf(version))

    @Test
    fun `loads details for the navigation argument`() = runTest {
        val repository = FakeAppRepository(Result.success(details))

        val viewModel = createViewModel(repository)
        runCurrent()

        assertEquals(details, viewModel.uiState.value.details)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(null, viewModel.uiState.value.error)
    }

    @Test
    fun `maps a 404 to a localized not found error`() = runTest {
        val repository = FakeAppRepository(Result.failure(AppError.Server(404)))

        val viewModel = createViewModel(repository)
        runCurrent()

        assertEquals(
            UiText.Res(R.string.error_app_not_found),
            viewModel.uiState.value.error,
        )
    }

    @Test
    fun `install delegates the selected version to the coordinator`() = runTest {
        val installer = FakeInstaller(Channel<InstallStatus>(Channel.UNLIMITED).receiveAsFlow())
        val viewModel = createViewModel(
            repository = FakeAppRepository(Result.success(details)),
            installer = installer,
        )
        runCurrent()

        viewModel.install(version)
        runCurrent()

        assertEquals(1, installer.calls)
        assertEquals("demo", viewModel.installState.value.appId)
        assertEquals("1.0.0", viewModel.installState.value.versionName)
    }

    @Test
    fun `changing the server url reloads the details`() = runTest {
        val repository = FakeAppRepository(Result.success(details))
        val settings = FakeServerSettings("http://127.0.0.1:8080")
        val viewModel = createViewModel(repository, settings)
        runCurrent()
        assertEquals(1, repository.calls)

        settings.serverUrl.value = "https://example.com:9443"
        runCurrent()

        assertEquals(2, repository.calls)
        assertEquals(details, viewModel.uiState.value.details)
    }

    private fun TestScope.createViewModel(
        repository: AppRepository,
        settings: ServerSettingsRepository = FakeServerSettings(),
        installer: ApkInstaller = FakeInstaller(
            Channel<InstallStatus>(Channel.UNLIMITED).receiveAsFlow(),
        ),
    ): AppDetailViewModel {
        val coordinator = InstallCoordinator(InstallAppUseCase(installer), backgroundScope)
        return AppDetailViewModel(
            savedStateHandle = SavedStateHandle(mapOf("appId" to "demo")),
            getApp = GetAppUseCase(repository),
            installCoordinator = coordinator,
            serverSettings = settings,
        )
    }

    private class FakeAppRepository(
        private val result: Result<AppDetails>,
    ) : AppRepository {
        var calls = 0

        override suspend fun getApps(): Result<List<TestApp>> =
            Result.failure(AppError.Unexpected())

        override suspend fun getApp(id: String): Result<AppDetails> {
            calls++
            return result
        }
    }

    private class FakeInstaller(
        private val statuses: Flow<InstallStatus>,
    ) : ApkInstaller {
        var calls = 0

        override fun install(app: TestApp, version: AppVersion): Flow<InstallStatus> {
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
