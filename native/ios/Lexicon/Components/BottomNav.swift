//
//  BottomNav.swift
//  Lexicon
//

import SwiftUI

struct BottomNav: View {
    var docked: Bool = false
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme
    @State private var measuredWidth: CGFloat = 390
    private var sealDiameter: CGFloat { homeNight ? min(66, max(36, measuredWidth * 66 / 591)) : 44 }
    private var capsuleInset: CGFloat { homeNight ? min(22, max(10, measuredWidth * 19 / 591)) : 16 }

    private var isDark: Bool { scheme == .dark }
    private var homeNight: Bool { isDark && vm.route == .home && !vm.libraryOpen }

    private var activeColor: Color {
        homeNight ? Color(hex: "DDBD87") : (isDark ? LexiconColors.accentDark : LexiconColors.primary)
    }

    private var inactiveColor: Color {
        homeNight ? Color(hex: "C6A77A") : (isDark ? LexiconColors.accentDark.opacity(0.78) : LexiconColors.foreground.opacity(0.88))
    }

    private var dotColor: Color {
        isDark ? LexiconColors.highlightDark : LexiconColors.primary
    }

    var body: some View {
        VStack(spacing: 0) {
            if !docked { Spacer(minLength: 0) }
            navCapsule
                .padding(.horizontal, capsuleInset)
                .padding(.top, docked ? (homeNight ? 4 : 10) : 0)
                .padding(.bottom, 4)
        }
        .background {
            GeometryReader { geo in
                Color.clear.onAppear { measuredWidth = geo.size.width }
                    .onChange(of: geo.size.width) { measuredWidth = $0 }
            }
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
                    .font(.system(size: homeNight ? sealDiameter * 0.44 : 23, weight: .medium))
                    .foregroundStyle(isDark ? LexiconColors.highlightDark : LexiconColors.primaryForeground)
                    .frame(width: sealDiameter, height: sealDiameter)
                    .background(homeNight ? Color(hex: "0A131B") : (isDark ? Color(hex: "101620") : LexiconColors.primary))
                    .clipShape(Circle())
                    .overlay(
                        Circle()
                            .stroke(
                                homeNight ? Color(hex: "D0AD75").opacity(0.72) : (isDark ? LexiconColors.accentDark.opacity(0.70) : lexiconPalette(scheme).card),
                                lineWidth: homeNight ? 1 : (isDark ? 1.5 : 3)
                            )
                    )
                    .frame(minWidth: 44, minHeight: 44)
                    .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .shadow(color: isDark ? .clear : LexiconColors.foreground.opacity(0.16), radius: 6, y: 2)
            .offset(y: homeNight ? 0 : -10)
            .accessibilityLabel(Text(LexiconAccessibilityLabels.search))
            .accessibilityAddTraits(.isButton)
            .frame(maxWidth: .infinity)

            navItem(.collections, label: "Collections", system: "bookmark")
            navItem(.more, label: "More", system: "ellipsis")
        }
        .padding(.horizontal, 8)
        .padding(.vertical, homeNight ? 4 : 7)
        .background {
            Capsule()
                .fill(homeNight ? Color(hex: "0C151C").opacity(0.95) : lexiconPalette(scheme).card.opacity(0.96))
                .overlay(Capsule().stroke(homeNight ? Color(hex: "B6A486").opacity(0.12) : lexiconPalette(scheme).border, lineWidth: 1))
        }
        .shadow(
            color: isDark ? .clear : LexiconColors.foreground.opacity(0.12),
            radius: 8,
            x: 0,
            y: 3
        )
    }

    private var visualTab: LexiconTab {
        switch vm.route {
        case .myNotes, .studyLists, .quickTemplates, .searchDiscovery:
            return .dictionary
        default:
            return vm.activeTab
        }
    }

    private func navItem(_ tab: LexiconTab, label: String, system: String) -> some View {
        let active = visualTab == tab
        return Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            vm.setActiveTab(tab)
        } label: {
            VStack(spacing: 2) {
                Image(systemName: active && system != "ellipsis" ? "\(system).fill" : system)
                    .font(.system(size: 19, weight: active ? .medium : .regular))
                    .foregroundStyle(active ? activeColor : inactiveColor)

                Text(label)
                    .font(.lexiconBody(9.5, weight: active ? .medium : .regular))
                    .foregroundStyle(active ? activeColor : inactiveColor)
                    .lineLimit(2)
                    .multilineTextAlignment(.center)

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
