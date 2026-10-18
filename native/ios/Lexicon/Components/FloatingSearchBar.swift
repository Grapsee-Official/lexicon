//
//  FloatingSearchBar.swift
//  Lexicon
//
//  Translation of `src/components/lexicon/floating-search-bar.tsx`.
//
//  Capsule shape: search icon + "Search for a word..." placeholder
//  + camera icon + mic icon. All thin strokes.
//

import SwiftUI

struct FloatingSearchBar: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme

    var placeholder: String = "Search for a word..."

    var body: some View {
        Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            vm.openSearch()
        } label: {
            HStack(spacing: 10) {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 14, weight: .regular))
                    .foregroundStyle(lexiconPalette(scheme).foreground.opacity(0.5))
                    .lexiconDecorative()

                Text(placeholder)
                    .font(.lexiconBody(13))
                    .foregroundStyle(lexiconPalette(scheme).mutedForeground.opacity(0.7))
                    .frame(maxWidth: .infinity, alignment: .leading)

                Image(systemName: "camera")
                    .font(.system(size: 14, weight: .regular))
                    .foregroundStyle(lexiconPalette(scheme).foreground.opacity(0.4))
                    .lexiconDecorative()

                Image(systemName: "mic")
                    .font(.system(size: 14, weight: .regular))
                    .foregroundStyle(lexiconPalette(scheme).foreground.opacity(0.4))
                    .lexiconDecorative()
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(lexiconPalette(scheme).card)
            .clipShape(Capsule())
            .overlay(Capsule().stroke(lexiconPalette(scheme).border, lineWidth: 1))
            .shadow(color: Color.black.opacity(0.06), radius: 6, x: 0, y: 2)
            // Capsule is ~33pt tall visually (10+13+10). Expand the hit
            // area to ≥ 44pt without changing the visual capsule size.
            .lexiconMinTouchTarget()
            .contentShape(Capsule())
        }
        .buttonStyle(.plain)
        // FloatingSearchBar is an icon+text button (not icon-only), so
        // its label is "Open search" (the visible placeholder text says
        // "Search for a word..." — VoiceOver reads that as the button's
        // visible text, but the explicit label "Open search" is clearer
        // about the action).
        .accessibilityLabel(Text(LexiconAccessibilityLabels.openSearch))
        .accessibilityHint(Text("Opens the full-screen search overlay"))
        .accessibilityAddTraits(.isButton)
    }
}

#Preview {
    FloatingSearchBar()
        .environmentObject(LexiconViewModel())
        .padding()
        .background(LexiconColors.background)
}
