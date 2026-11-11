package com.lexicon.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LexiconLightColors = lightColorScheme(
    primary = BrandGreen,
    onPrimary = PrimaryForeground,
    primaryContainer = BrandGreen.copy(alpha = 0.16f),
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
    surfaceTint = Color.Transparent,
    outline = BorderLight,
    outlineVariant = BorderLight,
    scrim = Color.Black,
)

private val LexiconDarkColors = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkDeep,
    primaryContainer = DarkPrimary,
    onPrimaryContainer = DarkHighlight,
    secondary = DarkSecondary,
    onSecondary = DarkForeground,
    tertiary = DarkHighlight,
    onTertiary = DarkDeep,
    background = DarkBackground,
    onBackground = DarkForeground,
    surface = DarkCard,
    onSurface = DarkForeground,
    surfaceVariant = DarkMuted,
    onSurfaceVariant = DarkMutedForeground,
    surfaceTint = Color.Transparent,
    outline = DarkChipBorder,
    outlineVariant = DarkBorder,
    scrim = Color.Black,
)

@Composable
fun LexiconTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) LexiconDarkColors else LexiconLightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val activity = view.context as? Activity ?: return@SideEffect
            val window = activity.window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
            @Suppress("DEPRECATION")
            window.statusBarColor = Color.Transparent.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = Color.Transparent.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = LexiconTypography,
        content = content,
    )
}
