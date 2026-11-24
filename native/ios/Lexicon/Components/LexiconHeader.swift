//
//  LexiconHeader.swift
//  Lexicon
//

import SwiftUI

struct LexiconHeader: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @EnvironmentObject private var theme: LexiconThemeState
    @Environment(\.colorScheme) private var scheme

    @State private var showDailyLetter = false
    @State private var measuredWidth: CGFloat = 390
    private var reference: CGFloat? { homeNight ? measuredWidth : nil }
    private var pageInset: CGFloat { reference.map { min(32, max(12, $0 * 30 / 591)) } ?? 16 }
    private var crestWidth: CGFloat { reference.map { min(62, max(31, $0 * 62 / 591)) } ?? (homeEditorial ? 36 : 42) }
    private var crestHeight: CGFloat { reference.map { min(57, max(29, $0 * 57 / 591)) } ?? (homeEditorial ? 36 : 40) }
    private var avatarSize: CGFloat { reference.map { min(49, max(28, $0 * 49 / 591)) } ?? (homeEditorial ? 32 : 36) }
    private var brandSize: CGFloat { reference.map { min(29, max(16, $0 * 29 / 591)) } ?? (homeEditorial ? 19 : 21) }
    private var taglineSize: CGFloat { reference.map { min(11, max(8, $0 * 11 / 591)) } ?? (homeEditorial ? 8 : 9) }
    private var symbolSize: CGFloat { reference.map { min(25, max(15, $0 * 25 / 591)) } ?? 18 }
    private var gap: CGFloat { reference.map { min(9, max(4, $0 * 8 / 591)) } ?? 8 }
    private var isDark: Bool { scheme == .dark }
    private var homeNight: Bool { isDark && homeEditorial }
    private var homeEditorial: Bool { vm.route == .home && !vm.libraryOpen }

    private var iconTint: Color {
        homeNight ? Color(hex: "D0AD75") : (isDark ? LexiconColors.accentDark.opacity(0.90) : LexiconColors.foreground.opacity(0.72))
    }

    private var wordmarkColor: Color {
        isDark ? LexiconColors.foregroundDark : LexiconColors.primary
    }

    private var taglineColor: Color {
        homeNight ? Color(hex: "C9BDA9") : (isDark ? LexiconColors.foregroundDark.opacity(0.76) : LexiconColors.foreground.opacity(0.70))
    }

    private var dotColor: Color {
        isDark ? LexiconColors.highlightDark : LexiconColors.highlight
    }

    var body: some View {
        HStack(spacing: gap) {
            button(action: { vm.setActiveTab(.more) }, label: LexiconAccessibilityLabels.menu) {
                Image(systemName: "line.3.horizontal")
                    .font(.system(size: symbolSize, weight: .light))
                    .foregroundStyle(iconTint)
            }

            HStack(spacing: gap) {
                Button {
                    UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                    theme.toggle()
                } label: {
                    Image(homeNight ? "ref-home-crest" : (isDark ? "lexicon-mark-dark" : "lexicon-mark"))
                        .resizable()
                        .renderingMode(.original)
                        .scaledToFit()
                        .frame(width: crestWidth, height: crestHeight)
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text(isDark ? LexiconAccessibilityLabels.themeToggleLight : LexiconAccessibilityLabels.themeToggleDark))

                Button {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    vm.setActiveTab(.home)
                } label: {
                    VStack(alignment: .leading, spacing: 1) {
                        Text("Lexora")
                            .font(.lexiconDisplay(brandSize, weight: .regular))
                            .tracking(-0.2)
                            .foregroundStyle(wordmarkColor)

                        Text("Words. Wisdom. Worlds.")
                            .font(.lexiconBody(taglineSize))
                            .tracking(0.2)
                            .foregroundStyle(taglineColor)
                    }
                }
                .buttonStyle(.plain)
            }
            .layoutPriority(1)
            .frame(maxWidth: .infinity, alignment: .leading)

            button(action: { showDailyLetter = true }, label: LexiconAccessibilityLabels.bell) {
                ZStack(alignment: .topTrailing) {
                    Image(systemName: "bell")
                        .font(.system(size: symbolSize, weight: .light))
                        .foregroundStyle(iconTint)
                    Circle()
                        .fill(dotColor)
                        .frame(width: 5.5, height: 5.5)
                        .offset(x: 2.5, y: -2.5)
                        .lexiconDecorative()
                }
            }

            button(action: { vm.setActiveTab(.more) }, label: LexiconAccessibilityLabels.avatar) {
                Image(homeNight ? "ref-home-avatar" : (isDark ? "avatar-dark" : "avatar-default"))
                    .resizable()
                    .scaledToFill()
                    .frame(width: avatarSize, height: avatarSize)
                    .clipShape(Circle())
                    .overlay(
                        Circle().stroke(
                            isDark ? LexiconColors.accentDark.opacity(0.35) : LexiconColors.foreground.opacity(0.16),
                            lineWidth: 1
                        )
                    )
            }
        }
        .frame(minHeight: 44)
        .padding(.horizontal, pageInset)
        .background {
            GeometryReader { geo in
                Color.clear.onAppear { measuredWidth = geo.size.width }
                    .onChange(of: geo.size.width) { measuredWidth = $0 }
            }
        }
        .alert("A letter from Lexora", isPresented: $showDailyLetter) {
            Button("Explore the word") {
                vm.navigate(to: .wordOfTheDay, word: LexiconData.featuredWord())
            }
            Button("Close", role: .cancel) { }
        } message: {
            let featured = LexiconData.featuredWord()
            Text("Today’s word: \(featured.word). \(featured.definitions.first ?? "")")
        }
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
