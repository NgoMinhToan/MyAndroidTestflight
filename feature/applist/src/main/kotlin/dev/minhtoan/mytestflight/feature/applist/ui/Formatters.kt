package dev.minhtoan.mytestflight.feature.applist.ui

import android.content.Context
import android.text.format.Formatter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun rememberShortFileSize(bytes: Long): String {
    val context = LocalContext.current
    return remember(context, bytes) { formatShortFileSize(context, bytes) }
}

fun formatShortFileSize(context: Context, bytes: Long): String =
    Formatter.formatShortFileSize(context, bytes)

@Composable
fun rememberFormattedPublishedDate(
    publishedAt: String,
    style: FormatStyle = FormatStyle.MEDIUM,
): String? {
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
    return remember(publishedAt, locale, style) {
        formatPublishedDate(publishedAt, locale, style = style)
    }
}

fun formatPublishedDate(
    publishedAt: String,
    locale: Locale,
    zone: ZoneId = ZoneId.systemDefault(),
    style: FormatStyle = FormatStyle.MEDIUM,
): String? {
    val instant = runCatching { Instant.parse(publishedAt) }.getOrNull() ?: return null
    return DateTimeFormatter
        .ofLocalizedDate(style)
        .withLocale(locale)
        .withZone(zone)
        .format(instant)
}
