//
//  BottomNav.swift
//  Lexicon
//
//  Translation of `src/components/lexicon/bottom-nav.tsx`.
//
//  Floating capsule nav with 5 items: Home, Dictionary, center Search button,
//  Collections, More. Thin SF Symbols icons. Active state with dot indicator.
//

import SwiftUI

struct BottomNav: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        VStack {
            Spacer()
            HStack {
                Spacer()
                navCapsule
                    .padding(.horizontal, 16)
                    .padding(.bottom, 8)
                Spacer()
            }
        }
        .ignoresSafeArea(.container, edges: .bottom)
    }

    private var navCapsule: some View {
        HStack(spacing: 0) {
            navItem(.home, label: "Home", system: "house")
            navItem(.dictionary, label: "Dictionary", system: "book")

            // Center Search button — elevated, brand-coloured.
            // Spec label: "Search for a word" (icon-only button).
            // Visual circle is 40×40; `.lexiconMinTouchTarget()` expands the
            // tappable frame to ≥ 44×44 without changing the visual size.
            Button {
                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                vm.openSearch()
            } label: {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundStyle(lexiconPalette(scheme).primaryForeground)
                    .frame(width: 40, height: 40)
                    .background(lexiconPalette(scheme).primary)
                    .clipShape(Circle())
                    .overlay(
                        Circle()
                            .stroke(lexiconPalette(scheme).background, lineWidth: 3)
                    )
                    .shadow(color: Color.black.opacity(0.15), radius: 8, x: 0, y: 4)
                    .offset(y: -12)
                    .lexiconMinTouchTarget()
            }
            .buttonStyle(.plain)
            .accessibilityLabel(Text(LexiconAccessibilityLabels.search))
            .accessibilityAddTraits(.isButton)
            .frame(maxWidth: .infinity)

            navItem(.collections, label: "Collections", system: "bookmark")
            navItem(.more, label: "More", system: "ellipsis")
        }
        .padding(.horizontal, 4)
        .padding(.vertical, 2)
        .background(navBackground)
        .clipShape(Capsule())
        .overlay(Capsule().stroke(lexiconPalette(scheme).border, lineWidth: 1))
        .shadow(color: Color.black.opacity(0.12), radius: 12, x: 0, y: 6)
    }

    /// Translucent paper surface — approximates `.paper-surface` from the web.
    private var navBackground: some View {
        lexiconPalette(scheme).background.opacity(0.85)
    }

    @ViewBuilder
    private func navItem(_ tab: LexiconTab, label: String, system: String) -> some View {
        let active = vm.activeTab == tab
        Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            vm.setActiveTab(tab)
        } label: {
            VStack(spacing: 2) {
                Image(systemName: active ? "\(system).fill" : system)
                    .font(.system(size: 16, weight: active ? .semibold : .regular))
                    .foregroundStyle(active
                                     ? lexiconPalette(scheme).primary
                                     : lexiconPalette(scheme).mutedForeground)

                Text(label)
                    .font(.lexiconBody(8, weight: active ? .medium : .regular))
                    .foregroundStyle(active
                                     ? lexiconPalette(scheme).primary
                                     : lexiconPalette(scheme).mutedForeground)

                // Active dot indicator — decorative (the `.isSelected`
                // trait below conveys the same information to VoiceOver).
                Circle()
                    .fill(active ? lexiconPalette(scheme).primary : .clear)
                    .frame(width: 3, height: 3)
                    .lexiconDecorative()
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 6)
            // Each tab cell is ~36pt tall (icon + label + dot). Expand
            // to ≥ 44pt hit area without resizing the visual content.
            .lexiconMinTouchTarget()
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        // Combine icon + label + dot into ONE VoiceOver element so the
        // user hears "Home, tab, selected" (or "Dictionary, tab") in a
        // single swipe, not three separate elements.
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Text(label))
        .accessibilityHint(Text("Switch to \(label) tab"))
        .accessibilityAddTraits(active ? [.isButton, .isSelected] : .isButton)
    }
}

#Preview {
    BottomNav()
        .environmentObject(LexiconViewModel())
        .background(LexiconColors.background)
}
