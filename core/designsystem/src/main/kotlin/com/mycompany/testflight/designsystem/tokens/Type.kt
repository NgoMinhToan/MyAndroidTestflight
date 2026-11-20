package com.mycompany.testflight.designsystem.tokens

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.mycompany.testflight.designsystem.R

val JakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
    Font(R.font.plus_jakarta_sans_extrabold, FontWeight.ExtraBold),
)

val PlexMono = FontFamily(
    Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_mono_medium, FontWeight.Medium),
)

/** Named styles used by the Friendly screens. Colors are supplied at the call site. */
object FriendlyType {
    val Hero = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 30.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.04f).em,
    )
    val DetailTitle = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 29.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.04f).em,
    )
    val DetailVersion = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 19.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.03f).em,
    )
    val StateTitle = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 19.sp,
        lineHeight = 23.sp,
        letterSpacing = (-0.025f).em,
    )
    val SheetSection = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = (-0.01f).em,
    )
    val RowName = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.5.sp,
        lineHeight = 17.sp,
        letterSpacing = (-0.015f).em,
    )
    val RowPackage = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
    )
    val RowVersion = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
    )
    val Kicker = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 10.5.sp,
        lineHeight = 14.sp,
    )
    val SearchText = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    )
    val SettingsTitle = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.02f).em,
    )
    val SectionLabel = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )
    val SettingsRowTitle = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )
    val SettingsRowValue = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )
    val Subtitle = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.5.sp,
        lineHeight = 16.sp,
    )
    val Meta = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        lineHeight = 14.sp,
    )
    val Overline = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 9.5.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.12.em,
    )
    val DetailOverline = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 9.5.sp,
        lineHeight = 13.sp,
        letterSpacing = 0.06.em,
    )
    val MonoMeta = TextStyle(
        fontFamily = PlexMono,
        fontWeight = FontWeight.Medium,
        fontSize = 9.5.sp,
        lineHeight = 14.sp,
    )
    val MonoInput = TextStyle(
        fontFamily = PlexMono,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
    )
    val Body = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 11.5.sp,
        lineHeight = 19.sp,
    )
    val VersionNote = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 17.6.sp,
    )
    val PillLabel = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 10.sp,
        lineHeight = 13.sp,
    )
    val VersionPillLabel = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 9.5.sp,
        lineHeight = 13.sp,
    )
    val Button = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 11.5.sp,
        lineHeight = 15.sp,
    )
    val TabLabel = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 10.sp,
        lineHeight = 13.sp,
    )
    val DetailTag = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 9.sp,
        lineHeight = 12.sp,
    )
    val Badge = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 8.sp,
        lineHeight = 11.sp,
        letterSpacing = 0.08.em,
    )
    val Snackbar = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.Bold,
        fontSize = 10.5.sp,
        lineHeight = 15.sp,
    )
    val FieldLabel = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 11.sp,
        lineHeight = 15.sp,
    )
    val Helper = TextStyle(
        fontFamily = JakartaSans,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 16.sp,
    )
}

/** Material3 typography, all families replaced so no default Roboto leaks through. */
val FriendlyTypography: Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = JakartaSans, fontWeight = FontWeight.ExtraBold,
        fontSize = 57.sp, lineHeight = 60.sp, letterSpacing = (-0.02f).em,
    ),
    displayMedium = TextStyle(
        fontFamily = JakartaSans, fontWeight = FontWeight.ExtraBold,
        fontSize = 45.sp, lineHeight = 48.sp, letterSpacing = (-0.02f).em,
    ),
    displaySmall = TextStyle(
        fontFamily = JakartaSans, fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp, lineHeight = 40.sp, letterSpacing = (-0.02f).em,
    ),
    headlineLarge = FriendlyType.Hero,
    headlineMedium = FriendlyType.DetailTitle,
    headlineSmall = FriendlyType.StateTitle,
    titleLarge = TextStyle(
        fontFamily = JakartaSans, fontWeight = FontWeight.Bold,
        fontSize = 16.sp, lineHeight = 21.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = JakartaSans, fontWeight = FontWeight.ExtraBold,
        fontSize = 13.5.sp, lineHeight = 18.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = JakartaSans, fontWeight = FontWeight.ExtraBold,
        fontSize = 12.sp, lineHeight = 16.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = JakartaSans, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 19.8.sp,
    ),
    bodyMedium = FriendlyType.Body,
    bodySmall = TextStyle(
        fontFamily = JakartaSans, fontWeight = FontWeight.Normal,
        fontSize = 10.sp, lineHeight = 16.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = JakartaSans, fontWeight = FontWeight.ExtraBold,
        fontSize = 11.5.sp, lineHeight = 15.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = JakartaSans, fontWeight = FontWeight.ExtraBold,
        fontSize = 10.sp, lineHeight = 14.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = JakartaSans, fontWeight = FontWeight.ExtraBold,
        fontSize = 9.5.sp, lineHeight = 12.sp,
    ),
)
