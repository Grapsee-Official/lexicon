//
//  LexiconHeader.swift
//  Lexicon
//

import SwiftUI

struct LexiconHeader: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @EnvironmentObject private var theme: LexiconThemeState
    @Environment(\.colorScheme) private var scheme

    private var isDark: Bool { scheme == .dark }

    private var iconTint: Color {
        isDark ? LexiconColors.accentDark.opacity(0.90) : LexiconColors.foreground.opacity(0.72)
    }

    private var wordmarkColor: Color {
        isDark ? LexiconColors.foregroundDark : LexiconColors.primary
    }

    private var taglineColor: Color {
        isDark ? LexiconColors.foregroundDark.opacity(0.76) : LexiconColors.foreground.opacity(0.70)
    }

    private var dotColor: Color {
        isDark ? LexiconColors.highlightDark : LexiconColors.highlight
    }

    var body: some View {
        HStack(spacing: 8) {
            button(action: { vm.setActiveTab(.more) }, label: LexiconAccessibilityLabels.menu) {
                Image(systemName: "line.3.horizontal")
                    .font(.system(size: 18, weight: .light))
                    .foregroundStyle(iconTint)
            }

            HStack(spacing: 8) {
                Button {
                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                    theme.toggle()
                } label: {
                    Image(isDark ? "lexicon-mark-dark" : "lexicon-mark")
                        .resizable()
                        .renderingMode(.original)
                        .scaledToFit()
                        .frame(width: 42, height: 40)
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text(isDark ? LexiconAccessibilityLabels.themeToggleLight : LexiconAccessibilityLabels.themeToggleDark))

                Button {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    vm.setActiveTab(.home)
                } label: {
                    VStack(alignment: .leading, spacing: 1) {
                        Text("Lexora")
                            .font(.lexiconDisplay(21, weight: .regular))
                            .tracking(-0.2)
                            .foregroundStyle(wordmarkColor)

                        Text("Words. Wisdom. Worlds.")
                            .font(.lexiconBody(9))
                            .tracking(0.2)
                            .foregroundStyle(taglineColor)
                    }
                }
                .buttonStyle(.plain)
            }
            .layoutPriority(1)
            .frame(maxWidth: .infinity, alignment: .leading)

            button(action: {}, label: LexiconAccessibilityLabels.bell) {
                ZStack(alignment: .topTrailing) {
                    Image(systemName: "bell")
                        .font(.system(size: 18, weight: .light))
                        .foregroundStyle(iconTint)
                    Circle()
                        .fill(dotColor)
                        .frame(width: 5.5, height: 5.5)
                        .offset(x: 2.5, y: -2.5)
                        .lexiconDecorative()
                }
            }

            button(action: { vm.setActiveTab(.more) }, label: LexiconAccessibilityLabels.avatar) {
                Image(isDark ? "avatar-dark" : "avatar-default")
                    .resizable()
                    .scaledToFill()
                    .frame(width: 36, height: 36)
                    .clipShape(Circle())
                    .overlay(
                        Circle().stroke(
                            isDark ? LexiconColors.accentDark.opacity(0.35) : LexiconColors.foreground.opacity(0.16),
                            lineWidth: 1
                        )
                    )
            }
        }
        .frame(height: 44)
        .padding(.horizontal, 16)
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
                .frame(width: 34, height: 34)
                .lexiconMinTouchTarget()
        }
        .accessibilityLabel(Text(label))
        .accessibilityAddTraits(.isButton)
        .buttonStyle(.plain)
    }
}
