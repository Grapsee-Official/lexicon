//
//  BottomNav.swift
//  Lexora
//
//  Translation of `src/components/lexora/bottom-nav.tsx`.
//
//  Floating capsule nav with 5 items: Home, Dictionary, center Search button,
//  Collections, More. Thin SF Symbols icons. Active state with dot indicator.
//

import SwiftUI

struct BottomNav: View {
    @EnvironmentObject private var vm: LexoraViewModel
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
            Button {
                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                vm.openSearch()
            } label: {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundStyle(lexoraPalette(scheme).primaryForeground)
                    .frame(width: 40, height: 40)
                    .background(lexoraPalette(scheme).primary)
                    .clipShape(Circle())
                    .overlay(
                        Circle()
                            .stroke(lexoraPalette(scheme).background, lineWidth: 3)
                    )
                    .shadow(color: Color.black.opacity(0.15), radius: 8, x: 0, y: 4)
                    .offset(y: -12)
            }
            .buttonStyle(.plain)
            .accessibilityLabel(Text("Open search"))
            .frame(maxWidth: .infinity)

            navItem(.collections, label: "Collections", system: "bookmark")
            navItem(.more, label: "More", system: "ellipsis")
        }
        .padding(.horizontal, 4)
        .padding(.vertical, 2)
        .background(navBackground)
        .clipShape(Capsule())
        .overlay(Capsule().stroke(lexoraPalette(scheme).border, lineWidth: 1))
        .shadow(color: Color.black.opacity(0.12), radius: 12, x: 0, y: 6)
    }

    /// Translucent paper surface — approximates `.paper-surface` from the web.
    private var navBackground: some View {
        lexoraPalette(scheme).background.opacity(0.85)
    }

    @ViewBuilder
    private func navItem(_ tab: LexoraTab, label: String, system: String) -> some View {
        let active = vm.activeTab == tab
        Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            vm.setActiveTab(tab)
        } label: {
            VStack(spacing: 2) {
                Image(systemName: active ? "\(system).fill" : system)
                    .font(.system(size: 16, weight: active ? .semibold : .regular))
                    .foregroundStyle(active
                                     ? lexoraPalette(scheme).primary
                                     : lexoraPalette(scheme).mutedForeground)

                Text(label)
                    .font(.lexoraBody(8, weight: active ? .medium : .regular))
                    .foregroundStyle(active
                                     ? lexoraPalette(scheme).primary
                                     : lexoraPalette(scheme).mutedForeground)

                // Active dot indicator.
                Circle()
                    .fill(active ? lexoraPalette(scheme).primary : .clear)
                    .frame(width: 3, height: 3)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 6)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    BottomNav()
        .environmentObject(LexoraViewModel())
        .background(LexoraColors.background)
}
