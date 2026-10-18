//
//  LexiconTypography.swift
//  Lexicon
//
//  Fonts: Fraunces (display), Inter (body), Lora (long-form dictionary text).
//
//  HOW TO ADD THE GOOGLE FONTS:
//    1. Download Fraunces, Inter, Lora .ttf files from Google Fonts (https://fonts.google.com).
//    2. Drag the .ttf files into the Lexicon/Resources/ folder in Xcode
//       (check "Copy items if needed" + "Add to target: Lexicon").
//    3. Open Info.plist and add the key:
//         UIAppFonts (Array) = [
//           "Fraunces-Regular.ttf",
//           "Fraunces-Medium.ttf",
//           "Fraunces-SemiBold.ttf",
//           "Inter-Regular.ttf",
//           "Inter-Medium.ttf",
//           "Inter-SemiBold.ttf",
//           "Lora-Regular.ttf",
//           "Lora-Italic.ttf"
//         ]
//    4. The custom font names below will then resolve; otherwise they fall back
//       gracefully to system serif/sans-serif.
//

import SwiftUI

/// Semantic font roles mirroring the web utility classes:
///   .font-display  → Fraunces (display/headings)
///   .font-body     → Inter    (UI body)
///   .font-long     → Lora     (long-form dictionary text)
enum LexiconFont {
    // Custom PostScript font names — only resolve if the .ttf files are bundled.
    static let display = "Fraunces-Regular"
    static let displayMedium = "Fraunces-Medium"
    static let displaySemiBold = "Fraunces-SemiBold"
    static let body = "Inter-Regular"
    static let bodyMedium = "Inter-Medium"
    static let bodySemiBold = "Inter-SemiBold"
    static let long = "Lora-Regular"
    static let longItalic = "Lora-Italic"

    /// True when a custom font has been bundled into the app.
    /// SwiftUI silently falls back to system if a custom name isn't found —
    /// we use this to switch on serif fallback when fonts aren't installed.
    static var customFontsAvailable: Bool {
        UIFont.familyNames.contains(where: { $0 == "Fraunces" })
            || UIFont.familyNames.contains(where: { $0 == "Inter" })
            || UIFont.familyNames.contains(where: { $0 == "Lora" })
    }
}

extension Font {
    /// Display/heading serif (Fraunces, falls back to system serif).
    static func lexiconDisplay(_ size: CGFloat, weight: Font.Weight = .regular) -> Font {
        // Try the matched PostScript weight, else fall back to system serif.
        switch weight {
        case .semibold: return .custom(LexiconFont.displaySemiBold, size: size, relativeTo: .title)
        case .medium:   return .custom(LexiconFont.displayMedium,   size: size, relativeTo: .title)
        default:        return .custom(LexiconFont.display,         size: size, relativeTo: .title)
        }
    }

    /// UI body sans-serif (Inter, falls back to system body).
    static func lexiconBody(_ size: CGFloat, weight: Font.Weight = .regular) -> Font {
        switch weight {
        case .semibold: return .custom(LexiconFont.bodySemiBold, size: size, relativeTo: .body)
        case .medium:   return .custom(LexiconFont.bodyMedium,   size: size, relativeTo: .body)
        default:        return .custom(LexiconFont.body,         size: size, relativeTo: .body)
        }
    }

    /// Long-form dictionary text serif (Lora, falls back to system serif).
    static func lexiconLong(_ size: CGFloat, weight: Font.Weight = .regular) -> Font {
        switch weight {
        case .semibold: return .custom(LexiconFont.long, size: size, relativeTo: .body)
        default:        return .custom(LexiconFont.long, size: size, relativeTo: .body)
        }
    }
}
