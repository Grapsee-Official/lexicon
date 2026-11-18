//
//  BottomNav.swift
//  Lexicon
//

import SwiftUI

struct BottomNav: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme

    private var isDark: Bool { scheme == .dark }

    private var activeColor: Color {
        isDark ? LexiconColors.accentDark : LexiconColors.primary
    }

    private var inactiveColor: Color {
        isDark ? LexiconColors.accentDark.opacity(0.78) : LexiconColors.foreground.opacity(0.88)
    }

    private var dotColor: Color {
        isDark ? LexiconColors.highlightDark : LexiconColors.primary
    }

    var body: some View {
        VStack {
            Spacer()
            navCapsule
                .padding(.horizontal, 16)
                .padding(.bottom, 2)
        }
    }

    private var navCapsule: some View {
        HStack(spacing: 0) {
            navItem(.home, label: "Home", system: "house")
            navItem(.dictionary, label: "Dictionary", system: "book")

            Button {
                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                vm.openSearch()
            } label: {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 17, weight: .medium))
                    .foregroundStyle(isDark ? LexiconColors.highlightDark : LexiconColors.primaryForeground)
                    .frame(width: 54, height: 54)
                    .background(isDark ? Color(hex: "101620") : LexiconColors.primary)
                    .clipShape(Circle())
                    .overlay(
                        Circle()
                            .stroke(
                                isDark ? LexiconColors.accentDark.opacity(0.70) : lexiconPalette(scheme).card,
                                lineWidth: isDark ? 1.5 : 3
                            )
                    )
            }
            .buttonStyle(.plain)
            .shadow(color: isDark ? .clear : LexiconColors.foreground.opacity(0.16), radius: 6, y: 2)
            .offset(y: -10)
            .accessibilityLabel(Text(LexiconAccessibilityLabels.search))
            .accessibilityAddTraits(.isButton)
            .frame(maxWidth: .infinity)

            navItem(.collections, label: "Collections", system: "bookmark")
            navItem(.more, label: "More", system: "ellipsis")
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 7)
        .background {
            Capsule()
                .fill(lexiconPalette(scheme).card.opacity(0.96))
                .overlay(Capsule().stroke(lexiconPalette(scheme).border, lineWidth: 1))
        }
        .shadow(
            color: isDark ? .clear : LexiconColors.foreground.opacity(0.12),
            radius: 8,
            x: 0,
            y: 3
        )
    }

    @ViewBuilder
    private func navItem(_ tab: LexiconTab, label: String, system: String) -> some View {
        let active = vm.activeTab == tab
        Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            vm.setActiveTab(tab)
        } label: {
            VStack(spacing: 2) {
                Image(systemName: active && system != "ellipsis" ? "\(system).fill" : system)
                    .font(.system(size: 19, weight: active ? .medium : .regular))
                    .foregroundStyle(active ? activeColor : inactiveColor)

                Text(label)
                    .font(.lexiconBody(10.5, weight: active ? .medium : .regular))
                    .foregroundStyle(active ? activeColor : inactiveColor)
                    .lineLimit(1)
                    .minimumScaleFactor(0.75)

                Circle()
                    .fill(active ? dotColor : .clear)
                    .frame(width: 4, height: 4)
                    .lexiconDecorative()
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 2)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Text(label))
        .accessibilityHint(Text("Switch to \(label) tab"))
        .accessibilityAddTraits(active ? [.isButton, .isSelected] : .isButton)
    }
}
