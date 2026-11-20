package com.mycompany.testflight.feature.settings

import com.mycompany.testflight.designsystem.text.UiText
import com.mycompany.testflight.domain.settings.ServerSettingsRepository
import com.mycompany.testflight.domain.settings.ServerUrlError
import com.mycompany.testflight.domain.settings.ServerUrlValidation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initializes from the repository`() {
        val repository = FakeServerSettings("http://127.0.0.1:8080")

        val viewModel = SettingsViewModel(repository)

        assertEquals("http://127.0.0.1:8080", viewModel.uiState.value.input)
        assertEquals("http://127.0.0.1:8080", viewModel.uiState.value.savedUrl)
        assertEquals(DEFAULT_URL, viewModel.uiState.value.defaultUrl)
    }

    @Test
    fun `empty input shows a localized error and does not save`() = runTest {
        val repository = FakeServerSettings("http://127.0.0.1:8080")
        val viewModel = SettingsViewModel(repository)

        viewModel.onInputChange("   ")
        viewModel.save()
        runCurrent()

        assertEquals(
            UiText.Res(R.string.settings_url_error_empty),
            viewModel.uiState.value.error,
        )
        assertEquals("http://127.0.0.1:8080", repository.serverUrl.value)
    }

    @Test
    fun `invalid scheme shows a localized error`() = runTest {
        val repository = FakeServerSettings("http://127.0.0.1:8080")
        val viewModel = SettingsViewModel(repository)

        viewModel.onInputChange("ftp://example.com")
        viewModel.save()
        runCurrent()

        assertEquals(
            UiText.Res(R.string.settings_url_error_scheme),
            viewModel.uiState.value.error,
        )
    }

    @Test
    fun `saving a valid url updates the input saved url and repository`() = runTest {
        val repository = FakeServerSettings("http://127.0.0.1:8080")
        val viewModel = SettingsViewModel(repository)
        val savedMessage = async(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.messages.first()
        }

        viewModel.onInputChange("https://example.com:9443")
        viewModel.save()
        runCurrent()

        assertEquals("https://example.com:9443", viewModel.uiState.value.input)
        assertEquals("https://example.com:9443", viewModel.uiState.value.savedUrl)
        assertEquals("https://example.com:9443", repository.serverUrl.value)
        assertEquals(UiText.Res(R.string.settings_saved), savedMessage.await())
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `reset restores the default url`() = runTest {
        val repository = FakeServerSettings("https://example.com:9443")
        val viewModel = SettingsViewModel(repository)
        val resetMessage = async(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.messages.first()
        }

        viewModel.reset()
        runCurrent()

        assertEquals(DEFAULT_URL, viewModel.uiState.value.input)
        assertEquals(DEFAULT_URL, viewModel.uiState.value.savedUrl)
        assertEquals(DEFAULT_URL, repository.serverUrl.value)
        assertEquals(UiText.Res(R.string.settings_reset_done), resetMessage.await())
    }

    @Test
    fun `editing the input clears a previous error`() = runTest {
        val repository = FakeServerSettings("http://127.0.0.1:8080")
        val viewModel = SettingsViewModel(repository)
        viewModel.onInputChange("ftp://example.com")
        viewModel.save()
        runCurrent()

        viewModel.onInputChange("http://example.com")

        assertNull(viewModel.uiState.value.error)
    }

    private class FakeServerSettings(initial: String) : ServerSettingsRepository {
        override val serverUrl = MutableStateFlow(initial)
        override val defaultServerUrl: String = DEFAULT_URL

        override suspend fun updateServerUrl(raw: String): ServerUrlValidation {
            val value = raw.trim()
            return when {
                value.isEmpty() -> ServerUrlValidation.Invalid(ServerUrlError.Empty)
                !value.startsWith("http://") && !value.startsWith("https://") ->
                    ServerUrlValidation.Invalid(ServerUrlError.UnsupportedScheme)

                else -> {
                    serverUrl.value = value
                    ServerUrlValidation.Valid(value)
                }
            }
        }

        override suspend fun resetToDefault() {
            serverUrl.value = defaultServerUrl
        }
    }

    private companion object {
        const val DEFAULT_URL = "http://10.0.2.2:8080"
    }
}
