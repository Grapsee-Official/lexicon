//
//  LexiconTheme.swift
//  Lexicon
//

import SwiftUI

final class LexiconThemeState: ObservableObject {
    @AppStorage("lexicon.theme") var theme: String = "system"

    init() {
        let args = ProcessInfo.processInfo.arguments
        if let idx = args.firstIndex(of: "-lexoraTheme"), idx + 1 < args.count {
            let mode = args[idx + 1].lowercased()
            if mode == "dark" || mode == "light" || mode == "system" {
                theme = mode
            }
        }
    }

    var preferredScheme: ColorScheme? {
        switch theme {
        case "light": return .light
        case "dark":  return .dark
        default:      return nil
        }
    }

    func resolvedScheme(system: ColorScheme) -> ColorScheme {
        switch theme {
        case "dark":  return .dark
        case "light": return .light
        default:      return system
        }
    }

    func toggle() {
        switch theme {
        case "dark":  theme = "light"
        case "light": theme = "dark"
        default:
            let isDark = UITraitCollection.current.userInterfaceStyle == .dark
            theme = isDark ? "light" : "dark"
        }
    }
}

struct LexiconThemeModifier: ViewModifier {
    @EnvironmentObject private var themeState: LexiconThemeState
    @Environment(\.colorScheme) private var systemScheme

    func body(content: Content) -> some View {
        let resolved = themeState.resolvedScheme(system: systemScheme)
        let isDark = (resolved == .dark)

        ZStack {
            (isDark ? LexiconColors.backgroundDark : LexiconColors.background)
                .ignoresSafeArea()
            content
                .environment(\.colorScheme, resolved)
        }
        .preferredColorScheme(themeState.preferredScheme)
    }
}

extension View {
    func lexiconTheme() -> some View {
        modifier(LexiconThemeModifier())
    }

    func lexiconPalette(_ scheme: ColorScheme) -> LexiconPalette {
        LexiconPalette(isDark: scheme == .dark)
    }
}
