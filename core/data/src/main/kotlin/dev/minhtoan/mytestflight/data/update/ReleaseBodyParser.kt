package dev.minhtoan.mytestflight.data.update

import dev.minhtoan.mytestflight.domain.update.ReleaseAsset
import dev.minhtoan.mytestflight.domain.update.ReleaseCommit
import dev.minhtoan.mytestflight.domain.update.ReleaseNotes
import dev.minhtoan.mytestflight.domain.update.ReleaseSection
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Release body as returned by the GitHub API, after merging the hidden machine block (when the
 * workflow wrote one) with the human-readable markdown headings.
 */
data class ParsedReleaseBody(
    val version: String?,
    val versionCode: Int?,
    val tag: String?,
    val publishedAt: String?,
    val notes: ReleaseNotes,
    val assets: Map<String, ReleaseAsset>,
    val commits: List<ReleaseCommit>,
    val hasMarker: Boolean,
)

/**
 * Parses the release body this project's workflow generates:
 *
 * ```markdown
 * ## Version 1.2.3
 * Build 42
 * Released 2026-11-25
 *
 * ## New features
 * - Add dark mode (abc1234) by @octocat
 *
 * <!-- mytestflight-release {"version":"1.2.3",...} -->
 * ```
 *
 * The hidden JSON block is authoritative when present. Without it, the renderer falls back to
 * reading the headings and bullets so hand-written releases still show useful notes.
 */
object ReleaseBodyParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // Android's ICU regex engine rejects a bare "}" after a quantifier, unlike java.util.regex
    // on the JVM, so the closing brace must be escaped.
    private val markerRegex = Regex(
        pattern = "<!--\\s*mytestflight-release\\s*(\\{[\\s\\S]*?\\})\\s*-->",
    )
    private val versionRegex = Regex(
        pattern = "^##\\s*Version\\s+v?([^\\s]+)\\s*$",
        option = RegexOption.IGNORE_CASE,
    )
    private val buildRegex = Regex(
        pattern = "^Build\\s+([0-9]+)\\s*$",
        option = RegexOption.IGNORE_CASE,
    )
    private val releasedRegex = Regex(
        pattern = "^Released\\s+(.+?)\\s*$",
        option = RegexOption.IGNORE_CASE,
    )
    private val headingRegex = Regex(pattern = "^##\\s+(.+?)\\s*$")
    private val bulletRegex = Regex(pattern = "^[-*]\\s+(.+?)\\s*$")

    fun parse(body: String?): ParsedReleaseBody {
        val raw = body.orEmpty()
        val marker = markerRegex.find(raw)?.groupValues?.get(1)
        val block = marker?.let(::decodeBlock)

        val fallback = parseHeadings(raw)
        return ParsedReleaseBody(
            version = block?.version ?: fallback.version,
            versionCode = block?.versionCode ?: fallback.versionCode,
            tag = block?.tag,
            publishedAt = block?.publishedAt ?: fallback.publishedAt,
            notes = ReleaseNotes(
                version = block?.version ?: fallback.version,
                versionCode = block?.versionCode ?: fallback.versionCode,
                publishedAt = block?.publishedAt ?: fallback.publishedAt,
                sections = block?.parsedSections ?: fallback.sections,
                commits = block?.parsedCommits.orEmpty(),
                rawBody = raw,
            ),
            assets = block?.parsedAssets.orEmpty(),
            commits = block?.parsedCommits.orEmpty(),
            hasMarker = block != null,
        )
    }

    private fun decodeBlock(jsonText: String): ReleaseBlock? = try {
        json.decodeFromString<ReleaseBlock>(jsonText)
    } catch (error: Exception) {
        // A malformed block must not hide the human-readable notes.
        null
    }

    private fun parseHeadings(raw: String): HeadingParse {
        var version: String? = null
        var versionCode: Int? = null
        var publishedAt: String? = null
        val sections = mutableListOf<ReleaseSection>()
        var currentId: String? = null
        var currentTitle: String? = null
        var currentItems = mutableListOf<String>()
        var looseItems = mutableListOf<String>()

        fun flush() {
            val id = currentId ?: return
            sections += ReleaseSection(
                id = id,
                title = currentTitle.orEmpty(),
                items = currentItems.toList(),
            )
            currentId = null
            currentTitle = null
            currentItems = mutableListOf()
        }

        raw.lineSequence().forEach { line ->
            versionRegex.matchEntire(line)?.let {
                flush()
                version = it.groupValues[1]
                return@forEach
            }
            buildRegex.matchEntire(line)?.let {
                versionCode = it.groupValues[1].toIntOrNull()
                return@forEach
            }
            releasedRegex.matchEntire(line)?.let {
                publishedAt = it.groupValues[1]
                return@forEach
            }
            headingRegex.matchEntire(line)?.let { match ->
                flush()
                val title = match.groupValues[1].trim()
                currentId = sectionId(title)
                currentTitle = title
                return@forEach
            }
            bulletRegex.matchEntire(line)?.let { match ->
                if (currentId != null) {
                    currentItems += match.groupValues[1]
                } else {
                    looseItems += match.groupValues[1]
                }
                return@forEach
            }
        }
        flush()

        if (looseItems.isNotEmpty()) {
            sections.add(0, ReleaseSection(id = "notes", title = "", items = looseItems.toList()))
        }

        return HeadingParse(
            version = version,
            versionCode = versionCode,
            publishedAt = publishedAt,
            sections = sections.filter { it.items.isNotEmpty() },
        )
    }

    private fun sectionId(title: String): String = when (title.lowercase().trim()) {
        "new features", "features", "feature" -> "features"
        "bug fixes", "fixes", "bug fix" -> "fixes"
        "changes", "other changes" -> "changes"
        "breaking changes", "breaking change", "breaking" -> "breaking"
        else -> "section-" + title.lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
    }

    private data class HeadingParse(
        val version: String?,
        val versionCode: Int?,
        val publishedAt: String?,
        val sections: List<ReleaseSection>,
    )

    @Serializable
    private data class ReleaseBlock(
        val version: String? = null,
        val versionCode: Int? = null,
        val tag: String? = null,
        val publishedAt: String? = null,
        val assets: Map<String, BlockAsset> = emptyMap(),
        val sections: List<BlockSection> = emptyList(),
        val commits: List<BlockCommit> = emptyList(),
    ) {
        val parsedAssets: Map<String, ReleaseAsset>
            get() = assets.mapValues { (abi, asset) ->
                ReleaseAsset(
                    abi = abi,
                    name = asset.name,
                    url = asset.url,
                    sizeBytes = asset.size,
                    sha256 = asset.sha256?.removePrefix("sha256:"),
                )
            }

        val parsedSections: List<ReleaseSection>
            get() = sections.map { section ->
                ReleaseSection(id = section.id, title = section.title, items = section.items)
            }

        val parsedCommits: List<ReleaseCommit>
            get() = commits.map {
                ReleaseCommit(
                    shortSha = it.shortSha,
                    author = it.author,
                    message = it.message,
                    type = it.type,
                )
            }
    }

    @Serializable
    private data class BlockAsset(
        val name: String = "",
        val url: String = "",
        val size: Long = 0,
        val sha256: String? = null,
    )

    @Serializable
    private data class BlockSection(
        val id: String = "",
        val title: String = "",
        val items: List<String> = emptyList(),
    )

    @Serializable
    private data class BlockCommit(
        val shortSha: String = "",
        val author: String = "",
        val message: String = "",
        val type: String? = null,
    )
}
