package com.lexora.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Typography — translates the web's three families:
 *   - Fraunces (display / headings)  → serif fallback (or ship Google Fonts TTFs)
 *   - Inter (body)                   → sans-serif fallback
 *   - Lora (long-form dictionary)    → serif fallback
 *
 * To keep this project buildable offline with no font fetch, we use the
 * system FontFamily defaults. The visual result is still a serif headline
 * + sans-serif body + serif long-form, which matches the web hierarchy.
 *
 * To upgrade to the real Google Fonts:
 *   1. Download Fraunces, Inter, Lora TTFs from https://fonts.google.com.
 *   2. Drop them into `app/src/main/res/font/` (lowercase, e.g. fraunces_regular.ttf).
 *   3. Replace [DisplayFontFamily], [BodyFontFamily], [LongFontFamily] below
 *      with FontFamily(Font(R.font.fraunces_regular), …) constructors.
 *   No other code changes required.
 */
val DisplayFontFamily = FontFamily.Serif   // Fraunces replacement
val BodyFontFamily    = FontFamily.SansSerif // Inter replacement
val LongFontFamily    = FontFamily.Serif   // Lora replacement

val LexoraTypography = Typography(
    // Display / headline (Fraunces)
    displayMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.5).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.3).sp,
    ),

    // Title (Fraunces) — used for card titles, WOTD word
    titleLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),

    // Body (Inter)
    bodyLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    ),

    // Label (Inter, semibold) — used for buttons / chips / nav labels
    labelLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.4.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 8.5.sp,
        lineHeight = 10.sp,
        letterSpacing = 0.8.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 8.sp,
        lineHeight = 10.sp,
    ),
)

/**
 * Convenience TextStyle constants for the small bespoke text styles used
 * across Lexora components (matches the web's `text-[Npx]` literals).
 */
object LexoraTextStyles {
    val BrandWordmark = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        letterSpacing = (-0.4).sp,
    )
    val BrandTagline = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 8.sp,
        letterSpacing = 0.8.sp,
    )
    val HeroHeadline = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.5).sp,
    )
    val HeroParagraph = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 16.sp,
    )
    val SearchPlaceholder = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
    )
    val TagChip = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
    )
    val WotdLabel = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 8.5.sp,
        letterSpacing = 0.8.sp,
    )
    val WotdWord = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        lineHeight = 20.sp,
    )
    val WotdPhonetic = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
    )
    val WotdPartOfSpeech = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
    )
    val WotdDefinition = TextStyle(
        fontFamily = LongFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    )
    val WotdExploreMore = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
    )
    val CategoryTitle = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 13.sp,
    )
    val CategorySubtitle = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 8.sp,
        lineHeight = 10.sp,
    )
    val ArticleTitle = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    )
    val ArticleMeta = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 9.sp,
        lineHeight = 11.sp,
    )
    val SectionTitle = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 18.sp,
        letterSpacing = (-0.3).sp,
    )
    val SectionAction = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
    )
    val NavLabel = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 8.sp,
        lineHeight = 10.sp,
    )
}
