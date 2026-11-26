package dev.minhtoan.mytestflight.feature.applist.search

import dev.minhtoan.mytestflight.domain.model.TestApp
import java.text.Normalizer
import java.util.Locale

internal val COMBINING_MARKS = Regex("\\p{M}+")

internal fun normalizeForSearch(value: String): String =
    Normalizer.normalize(value, Normalizer.Form.NFD)
        .replace(COMBINING_MARKS, "")
        .lowercase(Locale.ROOT)

fun filterApps(apps: List<TestApp>, query: String): List<TestApp> {
    val normalizedQuery = normalizeForSearch(query.trim())
    if (normalizedQuery.isEmpty()) return apps

    return apps.filter { app ->
        val haystack = normalizeForSearch(
            listOf(app.name, app.packageName, app.description, app.developer)
                .joinToString(separator = " "),
        )
        haystack.contains(normalizedQuery)
    }
}
