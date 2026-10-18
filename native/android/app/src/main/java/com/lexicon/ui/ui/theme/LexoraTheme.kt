package com.lexicon.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * LexiconTheme — Material 3 colour scheme with the exact brand palette.
 *
 * Light:  warm cream paper + sage + forest + dusty blue + terracotta + gold
 * Dark:   midnight blue nocturnal-academia + antique gold
 *
 * Status / nav bars are transparent — Compose draws into the insets.
 */

private val LexiconLightColors = lightColorScheme(
    primary = BrandGreen,
    onPrimary = PrimaryForeground,
    primaryContainer = BrandGreen.copy(alpha = 0.18f),
    onPrimaryContainer = DeepForest,
    secondary = DustyBlue,
    onSecondary = PrimaryForeground,
    tertiary = Terracotta,
    onTertiary = PrimaryForeground,
    background = WarmCream,
    onBackground = DeepForest,
    surface = CardSurface,
    onSurface = DeepForest,
    surfaceVariant = MutedParchment,
    onSurfaceVariant = WarmGrey,
    surfaceTint = BrandGreen,
    outline = BorderLight,
    outlineVariant = BorderLight,
    scrim = Color.Black,
)

private val LexiconDarkColors = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkBackground,
    primaryContainer = DarkAccent.copy(alpha = 0.18f),
    onPrimaryContainer = DarkForeground,
    secondary = DarkSecondary,
    onSecondary = DarkForeground,
    tertiary = Terracotta,
    onTertiary = DarkForeground,
    background = DarkBackground,
    onBackground = DarkForeground,
    surface = DarkCard,
    onSurface = DarkForeground,
    surfaceVariant = DarkMuted,
    onSurfaceVariant = DarkMutedForeground,
    surfaceTint = DarkAccent,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
    scrim = Color.Black,
)

@Composable
fun LexiconTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Lexicon uses fixed brand colours — no Material You.
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) androidx.compose.material3.dynamicDarkColorScheme(ctx)
            else androidx.compose.material3.dynamicLightColorScheme(ctx)
        }
        darkTheme -> LexiconDarkColors
        else -> LexiconLightColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // status bar icons: dark on light, light on dark
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightNavigationBars = !darkTheme
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LexiconTypography,
        content = content,
    )
}
