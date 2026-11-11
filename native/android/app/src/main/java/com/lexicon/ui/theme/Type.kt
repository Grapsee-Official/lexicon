package com.lexicon.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lexicon.R

val DisplayFontFamily = FontFamily(
    Font(R.font.playfair_display, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.playfair_display, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.playfair_display, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.playfair_display, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.playfair_display_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.playfair_display_italic, FontWeight.Medium, FontStyle.Italic),
)

val BodyFontFamily = FontFamily(
    Font(R.font.inter, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.inter, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.inter, FontWeight.SemiBold, FontStyle.Normal),
)

val LongFontFamily = FontFamily(
    Font(R.font.lora, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.lora, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.lora, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.lora_italic, FontWeight.Normal, FontStyle.Italic),
)

val LexiconTypography = Typography(
    displayMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 29.sp,
        lineHeight = 33.sp,
        letterSpacing = (-0.3).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 21.sp,
        letterSpacing = (-0.2).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 26.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 17.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 13.5.sp,
        lineHeight = 19.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp,
        lineHeight = 15.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 9.sp,
        lineHeight = 12.sp,
        letterSpacing = 0.6.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 8.5.sp,
        lineHeight = 11.sp,
    ),
)

object LexiconTextStyles {
    val BrandWordmark = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 24.sp,
        letterSpacing = (-0.2).sp,
    )
    val BrandTagline = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 9.sp,
        lineHeight = 11.sp,
        letterSpacing = 0.2.sp,
    )
    val HeroHeadline = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 29.sp,
        lineHeight = 33.sp,
        letterSpacing = (-0.3).sp,
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
        fontSize = 11.5.sp,
    )
    val WotdLabel = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 8.5.sp,
        letterSpacing = 0.6.sp,
    )
    val WotdWord = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 25.sp,
        letterSpacing = (-0.2).sp,
    )
    val WotdPhonetic = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 10.5.sp,
    )
    val WotdPartOfSpeech = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
    )
    val WotdDefinition = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.5.sp,
        lineHeight = 16.5.sp,
    )
    val WotdExploreMore = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
    )
    val CategoryTitle = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.5.sp,
        lineHeight = 14.sp,
    )
    val CategorySubtitle = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 8.sp,
        lineHeight = 10.sp,
    )
    val ArticleTitle = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Normal,
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
        fontWeight = FontWeight.Normal,
        fontSize = 16.5.sp,
        lineHeight = 20.sp,
        letterSpacing = (-0.2).sp,
    )
    val SectionAction = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
    )
    val NavLabel = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 9.sp,
        lineHeight = 11.sp,
    )
}
