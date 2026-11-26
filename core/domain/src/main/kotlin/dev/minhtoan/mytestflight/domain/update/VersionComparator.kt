package dev.minhtoan.mytestflight.domain.update

/**
 * Semantic-version precedence (https://semver.org/#spec-item-11), tolerant of the version names
 * this project produces ("0.1.0", "v1.2.3", "1.2.3-beta.1+build.7").
 *
 * Anything that does not look like a version is compared as text, so a malformed tag never
 * crashes an update check; it just degrades to a plain string comparison.
 */
object VersionComparator {

    fun isNewer(candidate: String, current: String): Boolean = compare(candidate, current) > 0

    fun compare(candidate: String, current: String): Int {
        val a = SemVer.parse(candidate)
        val b = SemVer.parse(current)
        return if (a == null || b == null) {
            candidate.trim().compareTo(current.trim(), ignoreCase = true)
        } else {
            a.compareTo(b)
        }
    }

    private data class SemVer(
        val major: Int,
        val minor: Int,
        val patch: Int,
        val prerelease: List<String>,
    ) : Comparable<SemVer> {

        override fun compareTo(other: SemVer): Int {
            if (major != other.major) return major.compareTo(other.major)
            if (minor != other.minor) return minor.compareTo(other.minor)
            if (patch != other.patch) return patch.compareTo(other.patch)
            return comparePrerelease(prerelease, other.prerelease)
        }

        companion object {
            fun parse(raw: String): SemVer? {
                var value = raw.trim()
                if (value.startsWith("v", ignoreCase = true)) value = value.drop(1)
                value = value.substringBefore('+')
                val core = value.substringBefore('-')
                val prerelease = value.substringAfter('-', missingDelimiterValue = "")
                    .takeIf { value.contains('-') }
                    ?.split('.')
                    ?.filter { it.isNotEmpty() }
                    .orEmpty()

                val parts = core.split('.')
                if (parts.isEmpty() || parts.size > 3) return null
                val numbers = parts.map { it.toIntOrNull() ?: return null }
                return SemVer(
                    major = numbers.getOrElse(0) { 0 },
                    minor = numbers.getOrElse(1) { 0 },
                    patch = numbers.getOrElse(2) { 0 },
                    prerelease = prerelease,
                )
            }

            private fun comparePrerelease(a: List<String>, b: List<String>): Int {
                // A release outranks any prerelease of the same core version.
                if (a.isEmpty() && b.isEmpty()) return 0
                if (a.isEmpty()) return 1
                if (b.isEmpty()) return -1

                val size = minOf(a.size, b.size)
                for (index in 0 until size) {
                    val result = compareIdentifier(a[index], b[index])
                    if (result != 0) return result
                }
                return a.size.compareTo(b.size)
            }

            private fun compareIdentifier(a: String, b: String): Int {
                val aNumber = a.toLongOrNull()
                val bNumber = b.toLongOrNull()
                return when {
                    aNumber != null && bNumber != null -> aNumber.compareTo(bNumber)
                    // Numeric identifiers always have lower precedence than alphanumeric ones.
                    aNumber != null -> -1
                    bNumber != null -> 1
                    else -> a.compareTo(b)
                }
            }
        }
    }
}
