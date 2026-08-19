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
            // LEFT: hamburger
            button(action: { vm.openMenu() }, label: "Open menu") {
                Image(systemName: "line.3.horizontal")
                    .font(.system(size: 16, weight: .thin))
                    .foregroundStyle(foregroundAt80)
            }

            // Brand lockup — pushes right cluster to the right edge (mr-auto).
            HStack(spacing: 8) {
                Image("lexora-mark")
                    .resizable()
                    .renderingMode(.original)
                    .frame(width: 36, height: 36)

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

            // RIGHT: theme toggle + bell + avatar
            button(action: { theme.toggle() },
                   label: theme.preferredScheme == .dark ? "Switch to light mode" : "Switch to dark mode") {
                Image(systemName: theme.preferredScheme == .dark ? "sun.max" : "moon")
                    .font(.system(size: 16, weight: .thin))
                    .foregroundStyle(foregroundAt80)
            }

            button(action: {}, label: "Notifications") {
                ZStack(alignment: .topTrailing) {
                    Image(systemName: "bell")
                        .font(.system(size: 16, weight: .thin))
                        .foregroundStyle(foregroundAt80)
                    Circle()
                        .fill(LexoraColors.accent)
                        .frame(width: 5, height: 5)
                        .offset(x: 4, y: -4)
                }
            }

            button(action: {}, label: "Profile") {
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
                .frame(width: 36, height: 36)
        }
        .accessibilityLabel(Text(label))
        .buttonStyle(.plain)
    }
}

#Preview {
    LexoraHeader()
        .environmentObject(LexoraViewModel())
        .environmentObject(LexoraThemeState())
        .background(LexoraColors.background)
}
