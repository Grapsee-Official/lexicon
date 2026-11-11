//
//  LexiconTypography.swift
//  Lexicon
//
//  Bundled Google Fonts matching the Brand & Design System board:
//    - Playfair Display (PlayfairDisplay.ttf / PlayfairDisplay-Italic.ttf)
//    - Inter (Inter.ttf)
//    - Lora (Lora.ttf / Lora-Italic.ttf)
//

import SwiftUI

enum LexiconFont {
    static let displayFamily = "Playfair Display"
    static let bodyFamily = "Inter"
    static let longFamily = "Lora"
}

extension Font {
    static func lexiconDisplay(_ size: CGFloat, weight: Font.Weight = .regular) -> Font {
        .custom(LexiconFont.displayFamily, size: size, relativeTo: .title)
            .weight(weight)
    }

    static func lexiconBody(_ size: CGFloat, weight: Font.Weight = .regular) -> Font {
        .custom(LexiconFont.bodyFamily, size: size, relativeTo: .body)
            .weight(weight)
    }

    static func lexiconLong(_ size: CGFloat, weight: Font.Weight = .regular) -> Font {
        .custom(LexiconFont.longFamily, size: size, relativeTo: .body)
            .weight(weight)
    }
}
