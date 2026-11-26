package dev.minhtoan.mytestflight.domain.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionComparatorTest {

    @Test
    fun `higher patch minor and major win`() {
        assertTrue(VersionComparator.isNewer("1.0.1", "1.0.0"))
        assertTrue(VersionComparator.isNewer("1.1.0", "1.0.9"))
        assertTrue(VersionComparator.isNewer("2.0.0", "1.99.99"))
        assertFalse(VersionComparator.isNewer("1.0.0", "1.0.1"))
    }

    @Test
    fun `equal versions are not newer`() {
        assertFalse(VersionComparator.isNewer("1.2.3", "1.2.3"))
        assertFalse(VersionComparator.isNewer("v1.2.3", "1.2.3"))
        assertFalse(VersionComparator.isNewer("1.2.3+build.9", "1.2.3+build.1"))
    }

    @Test
    fun `release outranks its prereleases`() {
        assertTrue(VersionComparator.isNewer("1.2.3", "1.2.3-beta.1"))
        assertFalse(VersionComparator.isNewer("1.2.3-beta.1", "1.2.3"))
    }

    @Test
    fun `prerelease identifiers follow semver precedence`() {
        assertTrue(VersionComparator.isNewer("1.0.0-beta.2", "1.0.0-beta.1"))
        assertTrue(VersionComparator.isNewer("1.0.0-beta.11", "1.0.0-beta.2"))
        assertTrue(VersionComparator.isNewer("1.0.0-rc.1", "1.0.0-beta.9"))
        assertTrue(VersionComparator.isNewer("1.0.0-alpha.1", "1.0.0-alpha"))
        // Numeric identifiers rank below alphanumeric ones.
        assertTrue(VersionComparator.isNewer("1.0.0-alpha.beta", "1.0.0-alpha.1"))
    }

    @Test
    fun `short and v-prefixed versions are accepted`() {
        assertTrue(VersionComparator.isNewer("v2", "v1.9.9"))
        assertTrue(VersionComparator.isNewer("1.2", "1.1.5"))
        assertEquals(0, VersionComparator.compare("v1.2.0", "1.2.0"))
    }

    @Test
    fun `non version strings fall back to text comparison`() {
        assertEquals(0, VersionComparator.compare("nightly", "NIGHTLY"))
        assertTrue(VersionComparator.isNewer("nightly-2", "nightly-1"))
    }
}
