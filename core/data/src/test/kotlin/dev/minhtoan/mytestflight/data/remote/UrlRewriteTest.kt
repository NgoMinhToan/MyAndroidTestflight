package dev.minhtoan.mytestflight.data.remote

import okhttp3.HttpUrl.Companion.toHttpUrl
import org.junit.Assert.assertEquals
import org.junit.Test

class UrlRewriteTest {

    @Test
    fun `rewrites scheme host and port but keeps the path and query`() {
        val original = "http://localhost/api/v1/apps?x=1".toHttpUrl()
        val base = "https://example.com:8443".toHttpUrl()

        val rewritten = rewriteUrl(original, base)

        assertEquals("https://example.com:8443/api/v1/apps?x=1", rewritten.toString())
    }

    @Test
    fun `keeps the original url when the base matches`() {
        val original = "http://10.0.2.2:8080/api/v1/apps".toHttpUrl()
        val base = "http://10.0.2.2:8080".toHttpUrl()

        val rewritten = rewriteUrl(original, base)

        assertEquals(original, rewritten)
    }
}
