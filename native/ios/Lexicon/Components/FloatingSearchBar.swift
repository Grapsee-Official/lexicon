//
//  FloatingSearchBar.swift
//  Lexicon
//

import SwiftUI

struct FloatingSearchBar: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme

    var placeholder: String = "Search for a word..."
    var sceneIntegrated: Bool = false

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    private var isDark: Bool { scheme == .dark }
    private var integrated: Bool { isDark && sceneIntegrated }

    private var iconColor: Color {
        integrated ? Color(hex: "D0AD75") : (isDark ? LexiconColors.accentDark : LexiconColors.highlight)
    }

    private var textColor: Color {
        isDark ? LexiconColors.mutedForegroundDark : LexiconColors.highlight.opacity(0.85)
    }

    private var pillBorder: Color {
        isDark && sceneIntegrated ? Color(hex: "B6A486").opacity(0.19)
            : (isDark ? LexiconColors.goldBorderDark : LexiconColors.foreground.opacity(0.08))
    }

    var body: some View {
        Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            vm.openSearch()
        } label: {
            HStack(spacing: 9) {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 15, weight: .regular))
                    .foregroundStyle(iconColor)
                    .lexiconDecorative()

                Text(placeholder)
                    .font(.lexiconBody(integrated && !dynamicTypeSize.isAccessibilitySize ? 11.5 : 13))
                    .foregroundStyle(textColor)
                    .frame(maxWidth: .infinity, alignment: .leading)

                Image(systemName: "camera")
                    .font(.system(size: 15, weight: .regular))
                    .foregroundStyle(iconColor)
                    .lexiconDecorative()

                Image(systemName: "mic")
                    .font(.system(size: 15, weight: .regular))
                    .foregroundStyle(iconColor)
                    .lexiconDecorative()
            }
            .padding(.horizontal, 15)
            .padding(.vertical, isDark && sceneIntegrated ? 11 : 9)
            .background {
                if integrated {
                    LinearGradient(colors: [
                        Color(hex: "10191E").opacity(0.97),
                        Color(hex: "0B161C").opacity(0.97)
                    ], startPoint: .top, endPoint: .bottom)
                } else {
                    lexiconPalette(scheme).searchPill
                }
            }
            .clipShape(Capsule())
            .overlay(Capsule().stroke(pillBorder, lineWidth: 1))
            .shadow(
                color: isDark ? .clear : LexiconColors.foreground.opacity(0.10),
                radius: 7,
                x: 0,
                y: 3
            )
            .contentShape(Capsule())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(Text(LexiconAccessibilityLabels.openSearch))
        .accessibilityHint(Text("Opens the full-screen search overlay"))
        .accessibilityAddTraits(.isButton)
    }
}
