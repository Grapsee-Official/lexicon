package com.lexicon.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Exact brand tokens from the locked Light & Dark Mode Palette reference boards
 * and full-screen Light/Dark UI design PNGs.
 *
 * Light Mode ("Cotswolds Scholarly"):
 *   - Primary:     #657D5A (Botanical, scholarly, timeless)
 *   - Deep:        #31462F (Old books, foliage, depth)
 *   - Background:  #F3EBDD (Aged paper, warmth)
 *   - Secondary:   #7C9297 (Watercolour sky, quietness)
 *   - Accent:      #C98267 (Terracotta, warmth, humanity)
 *   - Highlight:   #A58A61 (Old wood, parchment, countryside)
 *
 * Dark Mode ("Nocturnal Academia"):
 *   - Primary:     #1D2A3A (Deep night, scholarly, timeless)
 *   - Deep:        #0F1622 (Ink shadows, depth, mystery)
 *   - Background:  #0E131B / #1A1F28 (Midnight paper, soft warmth)
 *   - Secondary:   #334A5E (Night sky, calm, quietness)
 *   - Accent:      #D4A45A (Warm lantern light, humanity)
 *   - Highlight:   #F0D89A (Firefly glow, old brass, subtle highlight)
 */

// === Light palette ===
val BrandGreen        = Color(0xFF657D5A) // Primary (#657D5A)
val DeepForest        = Color(0xFF31462F) // Deep (#31462F)
val WarmCream         = Color(0xFFF3EBDD) // Background (#F3EBDD)
val DustyBlue         = Color(0xFF7C9297) // Secondary (#7C9297)
val Terracotta        = Color(0xFFC98267) // Accent (#C98267)
val OldWoodGold       = Color(0xFFA58A61) // Small Highlight (#A58A61)
val CardSurface       = Color(0xFFFAF4E8) // Elevated aged parchment card
val SearchPillLight   = Color(0xFFFCF8F0) // Crisp warm paper search pill
val WotdRightSurface  = Color(0xFFFAF3E6) // WOTD right panel warm parchment
val WotdGoldLight     = Color(0xFFE2CCA0) // Warm parchment gold badge/phonetic on BrandGreen
val MutedParchment    = Color(0xFFE7DEC9)
val WarmGrey          = Color(0xFF666052)
val PrimaryForeground = Color(0xFFF9F4EA)
val BorderLight       = Color(0x2231462F) // 13% DeepForest hairline

// === Dark palette (Dreamy Nocturnal Academia) ===
val DarkPrimary         = Color(0xFF1D2A3A) // Primary (#1D2A3A)
val DarkDeep            = Color(0xFF0F1622) // Deep (#0F1622)
val DarkBackground      = Color(0xFF0E131B) // Midnight canvas matching Dark PNG
val DarkMidnightPaper   = Color(0xFF1A1F28) // Palette board midnight paper (#1A1F28)
val DarkForeground      = Color(0xFFEDE4D2) // Crisp warm ivory-parchment primary ink
val DarkSecondary       = Color(0xFF334A5E) // Secondary (#334A5E)
val DarkAccent          = Color(0xFFD4A45A) // Warm lantern gold (#D4A45A)
val DarkHighlight       = Color(0xFFF0D89A) // Firefly glow / old brass (#F0D89A)
val DarkSage            = Color(0xFF7D9372) // Botanical sage for "Words" in Dark Hero
val DarkCard            = Color(0xFF141B25) // Deep nocturnal card surface
val DarkSearchPill      = Color(0xFF141B25) // Dark search capsule fill
val DarkWotdLeft        = Color(0xFF121822) // Left WOTD nocturnal panel
val DarkWotdRight       = Color(0xFF141B25) // Right WOTD nocturnal panel
val DarkMuted           = Color(0xFF1E2736)
val DarkMutedForeground = Color(0xFF9EA6B2) // Soft moonlit slate-silver body copy
val DarkBorder          = Color(0x2ED4A45A) // Delicate 18% antique gold hairline
val DarkChipBorder      = Color(0x289EA6B2) // Subtle slate-gold hairline on dark pills/cards
