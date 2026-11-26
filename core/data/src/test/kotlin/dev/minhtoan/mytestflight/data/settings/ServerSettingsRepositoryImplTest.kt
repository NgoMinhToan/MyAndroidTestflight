package dev.minhtoan.mytestflight.data.settings

import dev.minhtoan.mytestflight.domain.settings.ServerUrlError
import dev.minhtoan.mytestflight.domain.settings.ServerUrlValidation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ServerSettingsRepositoryImplTest {

    private val defaultUrl = "http://10.0.2.2:8080"

    @Test
    fun `initializes from a valid stored value`() {
        val store = FakeStore("http://example.com:9000/")
        val repository = createRepository(store)

        assertEquals("http://example.com:9000", repository.serverUrl.value)
    }

    @Test
    fun `falls back to the default when the stored value is invalid`() {
        val store = FakeStore("not a url")
        val repository = createRepository(store)

        assertEquals(defaultUrl, repository.serverUrl.value)
    }

    @Test
    fun `saves a valid url and publishes it`() = runTest {
        val store = FakeStore()
        val repository = createRepository(store)

        val result = repository.updateServerUrl("http://127.0.0.1:8080/")

        assertEquals(ServerUrlValidation.Valid("http://127.0.0.1:8080"), result)
        assertEquals("http://127.0.0.1:8080", store.value)
        assertEquals("http://127.0.0.1:8080", repository.serverUrl.value)
    }

    @Test
    fun `does not persist an invalid url`() = runTest {
        val store = FakeStore()
        val repository = createRepository(store)

        val result = repository.updateServerUrl("http://example.com/path")

        assertEquals(ServerUrlValidation.Invalid(ServerUrlError.PathNotAllowed), result)
        assertNull(store.value)
        assertEquals(defaultUrl, repository.serverUrl.value)
    }

    @Test
    fun `reset clears the stored value and restores the default`() = runTest {
        val store = FakeStore("http://example.com")
        val repository = createRepository(store)

        repository.resetToDefault()

        assertNull(store.value)
        assertEquals(defaultUrl, repository.serverUrl.value)
    }

    private fun createRepository(store: ServerUrlStore) = ServerSettingsRepositoryImpl(
        store = store,
        validator = HttpUrlServerUrlValidator(),
        defaultServerUrl = defaultUrl,
    )

    private class FakeStore(var value: String? = null) : ServerUrlStore {
        override fun read(): String? = value

        override fun write(url: String) {
            value = url
        }

        override fun clear() {
            value = null
        }
    }
}
