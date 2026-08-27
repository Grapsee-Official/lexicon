//
//  LexiconColors.swift
//  Lexicon
//
//  Exact brand palette from the locked web build (Phase 1).
//  Cotswolds English countryside: aged paper + sage + forest + dusty blue + terracotta + old-wood gold.
//

import SwiftUI

/// Color convenience initializer from a hex string (#RRGGBB or RRGGBB).
/// Used by `LexiconColors` to define the exact brand palette.
extension Color {
    init(hex: String) {
        let cleaned = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: cleaned).scanHexInt64(&int)
        let r, g, b: UInt64
        switch cleaned.count {
        case 6: // RRGGBB
            (r, g, b) = ((int >> 16) & 0xFF, (int >> 8) & 0xFF, int & 0xFF)
        case 8: // RR GG BB AA (rare here)
            (r, g, b) = ((int >> 24) & 0xFF, (int >> 16) & 0xFF, (int >> 8) & 0xFF)
        default:
            (r, g, b) = (0, 0, 0)
        }
        self.init(
            .sRGB,
            red: Double(r) / 255,
            green: Double(g) / 255,
            blue: Double(b) / 255,
            opacity: 1
        )
    }

    /// Hex + alpha (0...1).
    static func hex(_ hex: String, opacity: Double) -> Color {
        Color(hex: hex).opacity(opacity)
    }
}

/// The exact Lexicon brand palette — light + dark variants.
/// Mirrors `src/app/globals.css` token values 1:1.
enum LexiconColors {
    // MARK: - Light palette (locked web build)
    /// Warm cream — main background.  #FAF3E9
    static let background       = Color(hex: "FAF3E9")
    /// Brand green — primary.          #677554
    static let primary          = Color(hex: "677554")
    /// Deep forest — foreground text.  #31462F
    static let foreground       = Color(hex: "31462F")
    /// Watercolour sky — secondary.    #7C9297
    static let secondary        = Color(hex: "7C9297")
    /// Terracotta — accent.            #C98267
    static let accent           = Color(hex: "C98267")
    /// Old wood / gold — highlight.    #A58A61
    static let highlight        = Color(hex: "A58A61")
    /// Card surface (slightly lifted paper).  #FBF4EA  (web uses #FBF7EE; spec says #FBF4EA)
    static let card             = Color(hex: "FBF4EA")
    /// Muted parchment.                #E8DFCC
    static let muted            = Color(hex: "E8DFCC")
    /// Warm grey — muted text.         #6B6557
    static let mutedForeground  = Color(hex: "6B6557")
    /// Cream foreground for primary bg.#F3EBDD
    static let primaryForeground = Color(hex: "F3EBDD")
    /// Soft border (deep @ 10%).       rgba(49,70,47,0.10)
    static let border           = Color(hex: "31462F").opacity(0.10)

    // MARK: - Dark palette (nocturnal academia + antique gold)
    static let backgroundDark       = Color(hex: "1A1F28")
    static let foregroundDark       = Color(hex: "F0D89A")
    static let primaryDark          = Color(hex: "1D2A3A")
    static let accentDark           = Color(hex: "D4A45A")
    static let highlightDark        = Color(hex: "F0D89A")
    static let cardDark             = Color(hex: "232A38")
    static let mutedDark            = Color(hex: "2B3344")
    static let mutedForegroundDark  = Color(hex: "A89F8A")
    static let borderDark           = Color(hex: "F0D89A").opacity(0.15)
}

/// Appearance-aware color resolver — mirrors next-themes `resolvedTheme` behaviour.
/// Returns the dark variant when `colorScheme == .dark`.
struct LexiconPalette {
    let isDark: Bool

    var background: Color      { isDark ? LexiconColors.backgroundDark      : LexiconColors.background }
    var foreground: Color      { isDark ? LexiconColors.foregroundDark      : LexiconColors.foreground }
    var primary: Color         { isDark ? LexiconColors.primaryDark         : LexiconColors.primary }
    var accent: Color          { isDark ? LexiconColors.accentDark          : LexiconColors.accent }
    var highlight: Color       { isDark ? LexiconColors.highlightDark       : LexiconColors.highlight }
    var card: Color            { isDark ? LexiconColors.cardDark            : LexiconColors.card }
    var muted: Color           { isDark ? LexiconColors.mutedDark           : LexiconColors.muted }
    var mutedForeground: Color { isDark ? LexiconColors.mutedForegroundDark : LexiconColors.mutedForeground }
    var border: Color          { isDark ? LexiconColors.borderDark          : LexiconColors.border }
}
