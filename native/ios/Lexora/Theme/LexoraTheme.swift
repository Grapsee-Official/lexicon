//
//  LexiconTheme.swift
//  Lexicon
//
//  Environment-driven theme container + ViewModifier for applying the
//  Lexicon background and preferred color scheme at the root of the app.
//

import SwiftUI

/// Theme state held in the environment — drives the appearance-aware palette
/// and persists the user's choice via @AppStorage.
final class LexiconThemeState: ObservableObject {
    /// "light" | "dark" | "system"
    @AppStorage("lexicon.theme") var theme: String = "system"

    var preferredScheme: ColorScheme? {
        switch theme {
        case "light": return .light
        case "dark":  return .dark
        default:      return nil
        }
    }

    func toggle() {
        // Mirror the web toggle behaviour: dark → light, otherwise → dark.
        // (Web checks resolvedTheme; here we resolve system first.)
        switch theme {
        case "dark":  theme = "light"
        case "light": theme = "dark"
        default:
            // "system" — pick the opposite of whatever is currently resolved.
            let isDark = UITraitCollection.current.userInterfaceStyle == .dark
            theme = isDark ? "light" : "dark"
        }
    }
}

/// ViewModifier that paints the Lexicon background + applies the preferred scheme.
struct LexiconThemeModifier: ViewModifier {
    @EnvironmentObject private var themeState: LexiconThemeState

    func body(content: Content) -> some View {
        ZStack {
            LexiconColors.background
                .ignoresSafeArea()
            content
        }
        .preferredColorScheme(themeState.preferredScheme)
        .environmentObject(themeState)
    }
}

extension View {
    /// Apply at the App root to paint the warm-cream background and bind the theme.
    func lexiconTheme() -> some View {
        modifier(LexiconThemeModifier())
    }

    /// Resolved palette for the current color scheme (use inside any view).
    func lexiconPalette(_ scheme: ColorScheme) -> LexiconPalette {
        LexiconPalette(isDark: scheme == .dark)
    }
}
