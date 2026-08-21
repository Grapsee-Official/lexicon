package com.lexora.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Lexora brand palette — exact hex codes from the locked web build
 * (see /home/z/my-project/src/app/globals.css).
 *
 * Light:  aged paper + sage + forest + dusty blue + terracotta + old-wood gold
 * Dark:   midnight blue nocturnal-academia with antique gold
 */

// === Light palette ===
val BrandGreen       = Color(0xFF677554) // primary
val DeepForest       = Color(0xFF31462F) // foreground / deep
val WarmCream        = Color(0xFFFAF3E9) // background
val DustyBlue        = Color(0xFF7C9297) // secondary
val Terracotta       = Color(0xFFC98267) // accent
val OldWoodGold      = Color(0xFFA58A61) // highlight
val CardSurface      = Color(0xFFFBF7EE) // card
val WotdRightSurface = Color(0xFFFBF4EA) // WOTD right panel surface (matches web)
val MutedParchment   = Color(0xFFE8DFCC) // muted
val WarmGrey         = Color(0xFF6B6557) // muted foreground
val PrimaryForeground= Color(0xFFF3EBDD)
val BorderLight      = Color(0x1A31462F) // 10% deep over background

// === Dark palette (nocturnal academia) ===
val DarkPrimary        = Color(0xFF1D2A3A)
val DarkDeep           = Color(0xFF0F1622)
val DarkBackground     = Color(0xFF1A1F28)
val DarkForeground     = Color(0xFFF0D89A) // warm cream-gold text
val DarkSecondary      = Color(0xFF334A5E)
val DarkAccent         = Color(0xFFD4A45A) // antique gold
val DarkHighlight      = Color(0xFFF0D89A)
val DarkCard           = Color(0xFF232A38)
val DarkMuted          = Color(0xFF2B3344)
val DarkMutedForeground= Color(0xFFA89F8A)
val DarkBorder         = Color(0x26F0D89A) // 15% gold

// === WOTD constants ===
// The left panel uses BrandGreen with white text — same in both themes per the web spec.
val WotdLeftBg         = BrandGreen
val WotdRightBgLight   = WarmCream
val WotdRightBgDark    = DarkBackground
