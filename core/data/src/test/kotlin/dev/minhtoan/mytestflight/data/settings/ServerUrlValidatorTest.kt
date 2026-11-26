package dev.minhtoan.mytestflight.data.settings

import dev.minhtoan.mytestflight.domain.settings.ServerUrlError
import dev.minhtoan.mytestflight.domain.settings.ServerUrlValidation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ServerUrlValidatorTest {

    private val validator = HttpUrlServerUrlValidator()

    @Test
    fun `accepts an http origin and strips the trailing slash`() {
        val result = validator.validate(" http://10.0.2.2:8080/ ")

        assertEquals(ServerUrlValidation.Valid("http://10.0.2.2:8080"), result)
    }

    @Test
    fun `accepts https and normalizes the host`() {
        val result = validator.validate("https://EXAMPLE.com")

        assertEquals(ServerUrlValidation.Valid("https://example.com"), result)
    }

    @Test
    fun `accepts an ipv6 origin`() {
        val result = validator.validate("http://[::1]:8080")

        assertEquals(ServerUrlValidation.Valid("http://[::1]:8080"), result)
    }

    @Test
    fun `rejects an empty value`() {
        assertEquals(
            ServerUrlValidation.Invalid(ServerUrlError.Empty),
            validator.validate("   "),
        )
    }

    @Test
    fun `rejects a value without a scheme`() {
        assertEquals(
            ServerUrlValidation.Invalid(ServerUrlError.Malformed),
            validator.validate("10.0.2.2:8080"),
        )
    }

    @Test
    fun `rejects a non http scheme`() {
        assertEquals(
            ServerUrlValidation.Invalid(ServerUrlError.UnsupportedScheme),
            validator.validate("ftp://10.0.2.2:8080"),
        )
    }

    @Test
    fun `rejects a path`() {
        assertEquals(
            ServerUrlValidation.Invalid(ServerUrlError.PathNotAllowed),
            validator.validate("http://10.0.2.2:8080/api"),
        )
    }

    @Test
    fun `rejects a query or fragment`() {
        assertEquals(
            ServerUrlValidation.Invalid(ServerUrlError.PathNotAllowed),
            validator.validate("http://10.0.2.2:8080/?a=1"),
        )
        assertEquals(
            ServerUrlValidation.Invalid(ServerUrlError.PathNotAllowed),
            validator.validate("http://10.0.2.2:8080/#frag"),
        )
    }

    @Test
    fun `rejects embedded credentials`() {
        assertEquals(
            ServerUrlValidation.Invalid(ServerUrlError.Malformed),
            validator.validate("http://user:secret@10.0.2.2:8080"),
        )
    }

    @Test
    fun `rejects an out of range port`() {
        assertEquals(
            ServerUrlValidation.Invalid(ServerUrlError.PortOutOfRange),
            validator.validate("http://10.0.2.2:0"),
        )
        assertEquals(
            ServerUrlValidation.Invalid(ServerUrlError.PortOutOfRange),
            validator.validate("http://10.0.2.2:70000"),
        )
    }

    @Test
    fun `reports invalid results with a reason`() {
        val result = validator.validate("nope")

        assertTrue(result is ServerUrlValidation.Invalid)
    }
}
