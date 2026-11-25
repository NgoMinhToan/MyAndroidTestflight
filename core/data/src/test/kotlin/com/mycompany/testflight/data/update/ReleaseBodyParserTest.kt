package com.mycompany.testflight.data.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReleaseBodyParserTest {

    @Test
    fun `parses the hidden marker with sections assets and commits`() {
        val body = """
            ## Version 1.4.0
            Build 44
            Released 2026-11-25

            ## New features
            - Add auto update (abc1234) by @octocat

            ## Bug fixes
            - Fix crash on launch (def5678) by @hubot

            <!-- mytestflight-release
            {
              "version": "1.4.0",
              "versionCode": 44,
              "tag": "v1.4.0",
              "publishedAt": "2026-11-25T10:00:00Z",
              "sections": [
                {"id": "features", "title": "New features", "items": ["Add auto update"]},
                {"id": "fixes", "title": "Bug fixes", "items": ["Fix crash on launch"]}
              ],
              "assets": {
                "arm64-v8a": {"name": "MyTestflight-1.4.0-arm64-v8a.apk", "url": "https://example.com/arm64.apk", "size": 1200, "sha256": "sha256:ABC"},
                "universal": {"name": "MyTestflight-1.4.0-universal.apk", "url": "https://example.com/universal.apk", "size": 2400}
              },
              "commits": [
                {"shortSha": "abc1234", "author": "octocat", "message": "Add auto update", "type": "feat"}
              ]
            }
            -->
        """.trimIndent()

        val parsed = ReleaseBodyParser.parse(body)

        assertTrue(parsed.hasMarker)
        assertEquals("1.4.0", parsed.version)
        assertEquals(44, parsed.versionCode)
        assertEquals("v1.4.0", parsed.tag)
        assertEquals("2026-11-25T10:00:00Z", parsed.publishedAt)
        assertEquals(listOf("features", "fixes"), parsed.notes.sections.map { it.id })
        assertEquals(listOf("Add auto update"), parsed.notes.sections.first().items)
        assertEquals(1, parsed.commits.size)
        assertEquals("abc1234", parsed.commits.first().shortSha)
        assertEquals("octocat", parsed.commits.first().author)
        assertEquals("feat", parsed.commits.first().type)
        assertEquals("ABC", parsed.assets.getValue("arm64-v8a").sha256)
        assertEquals(2400, parsed.assets.getValue("universal").sizeBytes)
    }

    @Test
    fun `falls back to headings when the marker is missing`() {
        val body = """
            ## Version 1.2.3
            Build 23
            Released 2026-11-24

            ## New features
            - Search across apps
            * Faster detail load

            ## Bug fixes
            - Stop double installs

            ## Breaking changes
            - Server URL must include a port
        """.trimIndent()

        val parsed = ReleaseBodyParser.parse(body)

        assertFalse(parsed.hasMarker)
        assertEquals("1.2.3", parsed.version)
        assertEquals(23, parsed.versionCode)
        assertEquals("2026-11-24", parsed.publishedAt)
        val byId = parsed.notes.sections.associateBy { it.id }
        assertEquals(
            listOf("Search across apps", "Faster detail load"),
            byId.getValue("features").items,
        )
        assertEquals(listOf("Stop double installs"), byId.getValue("fixes").items)
        assertEquals(
            listOf("Server URL must include a port"),
            byId.getValue("breaking").items,
        )
        assertTrue(parsed.assets.isEmpty())
        assertTrue(parsed.commits.isEmpty())
    }

    @Test
    fun `handles an empty body`() {
        val parsed = ReleaseBodyParser.parse("")

        assertFalse(parsed.hasMarker)
        assertEquals(null, parsed.version)
        assertTrue(parsed.notes.isEmpty)
        assertEquals("", parsed.notes.rawBody)
    }

    @Test
    fun `handles a null body`() {
        val parsed = ReleaseBodyParser.parse(null)

        assertFalse(parsed.hasMarker)
        assertTrue(parsed.notes.isEmpty)
    }

    @Test
    fun `a marker without sections still supplies the version`() {
        val body = """
            <!-- mytestflight-release {"version": "2.0.0", "versionCode": 99, "tag": "v2.0.0"} -->
        """.trimIndent()

        val parsed = ReleaseBodyParser.parse(body)

        assertTrue(parsed.hasMarker)
        assertEquals("2.0.0", parsed.version)
        assertEquals(99, parsed.versionCode)
        assertTrue(parsed.notes.sections.isEmpty())
    }

    @Test
    fun `malformed marker json falls back to headings`() {
        val body = """
            ## Version 1.9.0

            ## Changes
            - Internal cleanup

            <!-- mytestflight-release {not valid json} -->
        """.trimIndent()

        val parsed = ReleaseBodyParser.parse(body)

        assertFalse(parsed.hasMarker)
        assertEquals("1.9.0", parsed.version)
        assertEquals(listOf("Internal cleanup"), parsed.notes.sections.single().items)
    }

    @Test
    fun `bullets before any heading become a loose notes section`() {
        val parsed = ReleaseBodyParser.parse(
            """
                ## Version 1.0.1

                - A lone note
            """.trimIndent(),
        )

        assertEquals("notes", parsed.notes.sections.single().id)
        assertEquals(listOf("A lone note"), parsed.notes.sections.single().items)
    }
}
