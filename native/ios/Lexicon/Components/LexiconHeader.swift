//
//  LexiconHeader.swift
//  Lexicon
//
//  Translation of `src/components/lexicon/header.tsx`.
//
//  Transparent bar so it flows seamlessly into the hero below.
//  Hamburger (left) + brand lockup (mark + "Lexicon" + tagline, mr-auto)
//  + theme toggle + bell + avatar (right).
//  All icons thin stroke (1.0–1.2).
//

import SwiftUI

struct LexiconHeader: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @EnvironmentObject private var theme: LexiconThemeState
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        HStack(spacing: 8) {
            // LEFT: hamburger — spec label "Open navigation menu".
            button(action: { vm.openMenu() }, label: LexiconAccessibilityLabels.menu) {
                Image(systemName: "line.3.horizontal")
                    .font(.system(size: 16, weight: .thin))
                    .foregroundStyle(foregroundAt80)
            }

            // Brand lockup — pushes right cluster to the right edge (mr-auto).
            // Combined into ONE VoiceOver element so the user hears
            // "Lexicon, Words. Wisdom. Worlds." in one swipe instead of three.
            HStack(spacing: 8) {
                Image("lexicon-mark")
                    .resizable()
                    .renderingMode(.original)
                    .frame(width: 36, height: 36)
                    .lexiconDecorative()

                VStack(alignment: .leading, spacing: 2) {
                    Text("Lexicon")
                        .font(.lexiconDisplay(18, weight: .regular))
                        .tracking(-0.4)
                        .foregroundStyle(lexiconPalette(scheme).foreground)

                    // Tagline — TITLE CASE (NOT uppercase), tracking 0.08em.
                    Text("Words. Wisdom. Worlds.")
                        .font(.lexiconBody(8))
                        .tracking(0.6)
                        .foregroundStyle(lexiconPalette(scheme).mutedForeground)
                }
            }
            .layoutPriority(1)
            .frame(maxWidth: .infinity, alignment: .leading)
            .lexiconCombinedElement()
            .accessibilityLabel(Text("Lexicon. Words. Wisdom. Worlds."))

            // RIGHT: theme toggle + bell + avatar.
            // Spec labels: bell → "Notifications, new items available",
            // avatar → "Open profile". Theme toggle is supplementary
            // (not in P4-05 icon-only list) but kept here for completeness.
            button(
                action: { theme.toggle() },
                label: theme.preferredScheme == .dark
                    ? LexiconAccessibilityLabels.themeToggleLight
                    : LexiconAccessibilityLabels.themeToggleDark
            ) {
                Image(systemName: theme.preferredScheme == .dark ? "sun.max" : "moon")
                    .font(.system(size: 16, weight: .thin))
                    .foregroundStyle(foregroundAt80)
            }

            button(action: {}, label: LexiconAccessibilityLabels.bell) {
                ZStack(alignment: .topTrailing) {
                    Image(systemName: "bell")
                        .font(.system(size: 16, weight: .thin))
                        .foregroundStyle(foregroundAt80)
                    // Notification dot — decorative (the bell label already
                    // says "new items available"), so hide from VoiceOver.
                    Circle()
                        .fill(LexiconColors.accent)
                        .frame(width: 5, height: 5)
                        .offset(x: 4, y: -4)
                        .lexiconDecorative()
                }
            }

            button(action: {}, label: LexiconAccessibilityLabels.avatar) {
                Image("avatar-default")
                    .resizable()
                    .scaledToFill()
                    .frame(width: 36, height: 36)
                    .clipShape(Circle())
                    .overlay(Circle().stroke(lexiconPalette(scheme).border, lineWidth: 1))
            }
        }
        .frame(height: 56)
        .padding(.horizontal, 16)
        .padding(.top, 0)
    }

    // MARK: - Helpers
    private var foregroundAt80: Color {
        lexiconPalette(scheme).foreground.opacity(0.8)
    }

    private func button(
        action: @escaping () -> Void,
        label: String,
        @ViewBuilder content: () -> some View
    ) -> some View {
        Button(action: {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            action()
        }) {
            content()
                // Visual size stays at 36×36 (matches the locked web spec);
                // the `.lexiconMinTouchTarget(minSize: 44))` modifier below expands the
                // TAPPABLE frame to ≥ 44×44 without changing the visual
                // icon size. Apple-HIG-compliant — visual size is a design
                // decision, hit area is an accessibility contract.
                .frame(width: 36, height: 36)
                .lexiconMinTouchTarget(minSize: 44))
        }
        .accessibilityLabel(Text(label))
        .accessibilityAddTraits(.isButton)
        .buttonStyle(.plain)
    }
}

#Preview {
    LexiconHeader()
        .environmentObject(LexiconViewModel())
        .environmentObject(LexiconThemeState())
        .background(LexiconColors.background)
}
