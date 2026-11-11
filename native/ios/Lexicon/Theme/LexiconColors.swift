//
//  LexiconColors.swift
//  Lexicon
//
//  Exact brand tokens from the Light & Dark Mode Palette boards
//  and full-screen Light/Dark UI design PNGs.
//

import SwiftUI

extension Color {
    init(hex: String) {
        let cleaned = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: cleaned).scanHexInt64(&int)
        let r, g, b: UInt64
        switch cleaned.count {
        case 6:
            (r, g, b) = ((int >> 16) & 0xFF, (int >> 8) & 0xFF, int & 0xFF)
        case 8:
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

    static func hex(_ hex: String, opacity: Double) -> Color {
        Color(hex: hex).opacity(opacity)
    }
}

enum LexiconColors {
    // MARK: - Light Palette ("Cotswolds Scholarly")
    static let background        = Color(hex: "F3EBDD")
    static let primary           = Color(hex: "657D5A")
    static let foreground        = Color(hex: "31462F")
    static let secondary         = Color(hex: "7C9297")
    static let accent            = Color(hex: "C98267")
    static let highlight         = Color(hex: "A58A61")
    static let card              = Color(hex: "FAF4E8")
    static let searchPill        = Color(hex: "FCF8F0")
    static let wotdRightLight    = Color(hex: "FAF3E6")
    static let wotdGoldLight     = Color(hex: "E2CCA0")
    static let muted             = Color(hex: "E7DEC9")
    static let mutedForeground   = Color(hex: "666052")
    static let primaryForeground = Color(hex: "F9F4EA")
    static let border            = Color(hex: "31462F").opacity(0.12)

    // MARK: - Dark Palette ("Nocturnal Academia")
    static let backgroundDark      = Color(hex: "0E131B")
    static let midnightPaperDark   = Color(hex: "1A1F28")
    static let foregroundDark      = Color(hex: "EDE4D2")
    static let primaryDark         = Color(hex: "1D2A3A")
    static let secondaryDark       = Color(hex: "334A5E")
    static let accentDark          = Color(hex: "D4A45A")
    static let highlightDark       = Color(hex: "F0D89A")
    static let sageDark            = Color(hex: "7D9372")
    static let cardDark            = Color(hex: "141B25")
    static let wotdLeftDark        = Color(hex: "121822")
    static let wotdRightDark       = Color(hex: "141B25")
    static let mutedDark           = Color(hex: "1E2736")
    static let mutedForegroundDark = Color(hex: "9EA6B2")
    static let borderDark          = Color(hex: "9EA6B2").opacity(0.16)
    static let goldBorderDark      = Color(hex: "D4A45A").opacity(0.24)
}

struct LexiconPalette {
    let isDark: Bool

    var background: Color      { isDark ? LexiconColors.backgroundDark      : LexiconColors.background }
    var foreground: Color      { isDark ? LexiconColors.foregroundDark      : LexiconColors.foreground }
    var primary: Color         { isDark ? LexiconColors.accentDark          : LexiconColors.primary }
    var surfacePrimary: Color  { isDark ? LexiconColors.primaryDark         : LexiconColors.primary }
    var secondary: Color       { isDark ? LexiconColors.secondaryDark       : LexiconColors.secondary }
    var accent: Color          { isDark ? LexiconColors.accentDark          : LexiconColors.accent }
    var highlight: Color       { isDark ? LexiconColors.accentDark          : LexiconColors.highlight }
    var glowHighlight: Color   { isDark ? LexiconColors.highlightDark       : LexiconColors.highlight }
    var card: Color            { isDark ? LexiconColors.cardDark            : LexiconColors.card }
    var searchPill: Color      { isDark ? LexiconColors.cardDark.opacity(0.94) : LexiconColors.searchPill }
    var muted: Color           { isDark ? LexiconColors.mutedDark           : LexiconColors.muted }
    var mutedForeground: Color { isDark ? LexiconColors.mutedForegroundDark : LexiconColors.mutedForeground }
    var border: Color          { isDark ? LexiconColors.borderDark          : LexiconColors.border }
}
