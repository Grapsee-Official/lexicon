//
//  LexoraHeader.swift
//  Lexora
//
//  Translation of `src/components/lexora/header.tsx`.
//
//  Transparent bar so it flows seamlessly into the hero below.
//  Hamburger (left) + brand lockup (mark + "Lexora" + tagline, mr-auto)
//  + theme toggle + bell + avatar (right).
//  All icons thin stroke (1.0–1.2).
//

import SwiftUI

struct LexoraHeader: View {
    @EnvironmentObject private var vm: LexoraViewModel
    @EnvironmentObject private var theme: LexoraThemeState
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        HStack(spacing: 8) {
            // LEFT: hamburger — spec label "Open navigation menu".
            button(action: { vm.openMenu() }, label: LexoraAccessibilityLabels.menu) {
                Image(systemName: "line.3.horizontal")
                    .font(.system(size: 16, weight: .thin))
                    .foregroundStyle(foregroundAt80)
            }

            // Brand lockup — pushes right cluster to the right edge (mr-auto).
            // Combined into ONE VoiceOver element so the user hears
            // "Lexora, Words. Wisdom. Worlds." in one swipe instead of three.
            HStack(spacing: 8) {
                Image("lexora-mark")
                    .resizable()
                    .renderingMode(.original)
                    .frame(width: 36, height: 36)
                    .lexoraDecorative()

                VStack(alignment: .leading, spacing: 2) {
                    Text("Lexora")
                        .font(.lexoraDisplay(18, weight: .regular))
                        .tracking(-0.4)
                        .foregroundStyle(lexoraPalette(scheme).foreground)

                    // Tagline — TITLE CASE (NOT uppercase), tracking 0.08em.
                    Text("Words. Wisdom. Worlds.")
                        .font(.lexoraBody(8))
                        .tracking(0.6)
                        .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                }
            }
            .layoutPriority(1)
            .frame(maxWidth: .infinity, alignment: .leading)
            .lexoraCombinedElement()
            .accessibilityLabel(Text("Lexora. Words. Wisdom. Worlds."))

            // RIGHT: theme toggle + bell + avatar.
            // Spec labels: bell → "Notifications, new items available",
            // avatar → "Open profile". Theme toggle is supplementary
            // (not in P4-05 icon-only list) but kept here for completeness.
            button(
                action: { theme.toggle() },
                label: theme.preferredScheme == .dark
                    ? LexoraAccessibilityLabels.themeToggleLight
                    : LexoraAccessibilityLabels.themeToggleDark
            ) {
                Image(systemName: theme.preferredScheme == .dark ? "sun.max" : "moon")
                    .font(.system(size: 16, weight: .thin))
                    .foregroundStyle(foregroundAt80)
            }

            button(action: {}, label: LexoraAccessibilityLabels.bell) {
                ZStack(alignment: .topTrailing) {
                    Image(systemName: "bell")
                        .font(.system(size: 16, weight: .thin))
                        .foregroundStyle(foregroundAt80)
                    // Notification dot — decorative (the bell label already
                    // says "new items available"), so hide from VoiceOver.
                    Circle()
                        .fill(LexoraColors.accent)
                        .frame(width: 5, height: 5)
                        .offset(x: 4, y: -4)
                        .lexoraDecorative()
                }
            }

            button(action: {}, label: LexoraAccessibilityLabels.avatar) {
                Image("avatar-default")
                    .resizable()
                    .scaledToFill()
                    .frame(width: 36, height: 36)
                    .clipShape(Circle())
                    .overlay(Circle().stroke(lexoraPalette(scheme).border, lineWidth: 1))
            }
        }
        .frame(height: 56)
        .padding(.horizontal, 16)
        .padding(.top, 0)
    }

    // MARK: - Helpers
    private var foregroundAt80: Color {
        lexoraPalette(scheme).foreground.opacity(0.8)
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
                // the `.lexoraMinTouchTarget()` modifier below expands the
                // TAPPABLE frame to ≥ 44×44 without changing the visual
                // icon size. Apple-HIG-compliant — visual size is a design
                // decision, hit area is an accessibility contract.
                .frame(width: 36, height: 36)
                .lexoraMinTouchTarget()
        }
        .accessibilityLabel(Text(label))
        .accessibilityAddTraits(.isButton)
        .buttonStyle(.plain)
    }
}

#Preview {
    LexoraHeader()
        .environmentObject(LexoraViewModel())
        .environmentObject(LexoraThemeState())
        .background(LexoraColors.background)
}
