package com.mycompany.testflight.feature.applist

import com.mycompany.testflight.domain.model.TestApp
import com.mycompany.testflight.feature.applist.search.filterApps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSearchTest {

    private val crm = app(
        name = "Internal CRM",
        packageName = "com.company.crm",
        description = "CRM for the internal sales team",
        developer = "Platform Team",
    )
    private val vietnamese = app(
        name = "Ứng dụng nội bộ",
        packageName = "com.company.internal",
        description = "Bản dựng thử nghiệm",
        developer = "Nhóm nền tảng",
    )
    private val apps = listOf(crm, vietnamese)

    @Test
    fun `blank query returns every app`() {
        assertEquals(apps, filterApps(apps, "   "))
    }

    @Test
    fun `matches name and package ignoring case`() {
        assertEquals(listOf(crm), filterApps(apps, "crm"))
        assertEquals(listOf(crm), filterApps(apps, "COM.COMPANY.CRM"))
    }

    @Test
    fun `matches description and developer`() {
        assertEquals(listOf(crm), filterApps(apps, "sales team"))
        assertEquals(listOf(crm), filterApps(apps, "platform"))
    }

    @Test
    fun `matches vietnamese text without diacritics`() {
        assertEquals(listOf(vietnamese), filterApps(apps, "ung dung noi bo"))
        assertEquals(listOf(vietnamese), filterApps(apps, "nhom nen tang"))
    }

    @Test
    fun `returns nothing when no app matches`() {
        assertTrue(filterApps(apps, "does-not-exist").isEmpty())
    }

    private fun app(
        name: String,
        packageName: String,
        description: String = "",
        developer: String = "",
    ) = TestApp(
        id = packageName,
        name = name,
        packageName = packageName,
        iconUrl = null,
        latestVersion = null,
        description = description,
        developer = developer,
        versionCount = 0,
    )
}
