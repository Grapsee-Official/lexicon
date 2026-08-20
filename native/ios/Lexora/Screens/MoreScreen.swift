//
//  MoreScreen.swift
//  Lexora
//
//  Translation of `src/components/lexora/screens/more-screen.tsx`.
//
//  Profile header card + 4 settings groups (Appearance, Dictionary,
//  Privacy & Data, About) + footer credit. Each group is an uppercase muted
//  header above a card of rows. Each row has an icon in a rounded square
//  (primary @ 10% opacity), a label, a value (optional), and a chevron right.
//
//  Theme toggle reads the shared LexoraThemeState (the next-themes
//  equivalent) and calls theme.toggle(). The isDark resolution mirrors the
//  web `(resolvedTheme ?? theme) === "dark"` logic by combining the stored
//  preference with the current UITraitCollection color scheme when "system".
//

import SwiftUI

struct MoreScreen: View {
    @EnvironmentObject private var vm: LexoraViewModel
    @EnvironmentObject private var theme: LexoraThemeState
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                profileHeader

                SettingsGroup(title: "Appearance", rows: [
                    SettingsRowSpec(
                        icon: isDark ? "moon" : "sun",
                        label: "Theme",
                        value: themeValueText,
                        action: {
                            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                            theme.toggle()
                        }
                    ),
                    SettingsRowSpec(
                        icon: "eye",
                        label: "Text size",
                        value: "Default",
                        action: { UIImpactFeedbackGenerator(style: .light).impactOccurred() }
                    ),
                    SettingsRowSpec(
                        icon: "bell",
                        label: "Notifications",
                        value: "On",
                        action: { UIImpactFeedbackGenerator(style: .light).impactOccurred() }
                    ),
                ])

                SettingsGroup(title: "Dictionary", rows: [
                    SettingsRowSpec(
                        icon: "globe",
                        label: "Language pairs",
                        value: "36 installed",
                        action: { UIImpactFeedbackGenerator(style: .light).impactOccurred() }
                    ),
                    SettingsRowSpec(
                        icon: "externaldrive",
                        label: "Storage",
                        value: "412 MB",
                        action: { UIImpactFeedbackGenerator(style: .light).impactOccurred() }
                    ),
                    SettingsRowSpec(
                        icon: "arrow.down.circle",
                        label: "Download language packs",
                        value: "",
                        action: { UIImpactFeedbackGenerator(style: .light).impactOccurred() }
                    ),
                ])

                SettingsGroup(title: "Privacy & Data", rows: [
                    SettingsRowSpec(
                        icon: "doc.text",
                        label: "Clear recent searches",
                        value: vm.recentSearches.count > 0 ? "\(vm.recentSearches.count) items" : "Empty",
                        action: {
                            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                            vm.clearRecentSearches()
                        }
                    ),
                    SettingsRowSpec(
                        icon: "externaldrive",
                        label: "Clear cache",
                        value: "412 MB",
                        action: { UIImpactFeedbackGenerator(style: .light).impactOccurred() }
                    ),
                ])

                SettingsGroup(title: "About", rows: [
                    SettingsRowSpec(
                        icon: "info.circle",
                        label: "About Lexora",
                        value: "v1.0.0",
                        action: { UIImpactFeedbackGenerator(style: .light).impactOccurred() }
                    ),
                    SettingsRowSpec(
                        icon: "doc.text",
                        label: "Privacy policy",
                        value: "",
                        action: { UIImpactFeedbackGenerator(style: .light).impactOccurred() }
                    ),
                    SettingsRowSpec(
                        icon: "chevron.left.slash.chevron.right",
                        label: "Open source licences",
                        value: "",
                        action: { UIImpactFeedbackGenerator(style: .light).impactOccurred() }
                    ),
                ])

                footer
            }
            .padding(.horizontal, 16)
            .padding(.top, 12)
            .padding(.bottom, 120) // clear floating bottom nav
        }
        .background(LexoraColors.background)
    }

    // MARK: - Theme resolution

    /// Mirror of the web `(resolvedTheme ?? theme) === "dark"` check.
    private var isDark: Bool {
        switch theme.theme {
        case "dark":  return true
        case "light": return false
        default:      return scheme == .dark
        }
    }

    private var themeValueText: String {
        // Web shows "Dark"/"Light" once mounted; SwiftUI has no hydration mismatch
        // so we always show the resolved value.
        isDark ? "Dark" : "Light"
    }

    // MARK: - Profile header

    private var profileHeader: some View {
        HStack(spacing: 16) {
            // Avatar (circular, 64pt, primary ring)
            Image("avatar-default")
                .resizable()
                .scaledToFill()
                .frame(width: 64, height: 64)
                .clipShape(Circle())
                .overlay(
                    Circle().stroke(lexoraPalette(scheme).primary.opacity(0.30), lineWidth: 2)
                )
                .accessibilityHidden(true)

            VStack(alignment: .leading, spacing: 4) {
                Text("Curious Soul")
                    .font(.lexoraDisplay(18, weight: .semibold))
                    .foregroundStyle(lexoraPalette(scheme).foreground)
                Text("Lexora Companion · since 2026")
                    .font(.lexoraBody(12))
                    .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                HStack(spacing: 16) {
                    statLabel(value: vm.savedWordIds.count, label: "saved")
                    statLabel(value: vm.recentSearches.count, label: "recent")
                }
                .padding(.top, 2)
            }
            Spacer(minLength: 0)
        }
        .padding(16)
        .background(lexoraPalette(scheme).card)
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(lexoraPalette(scheme).border, lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.04), radius: 4, x: 0, y: 2)
    }

    private func statLabel(value: Int, label: String) -> some View {
        HStack(spacing: 4) {
            Text("\(value)")
                .font(.lexoraBody(11, weight: .semibold))
                .foregroundStyle(lexoraPalette(scheme).foreground)
            Text(label)
                .font(.lexoraBody(11))
                .foregroundStyle(lexoraPalette(scheme).mutedForeground)
        }
    }

    // MARK: - Footer

    private var footer: some View {
        VStack(spacing: 4) {
            HStack(spacing: 6) {
                Image(systemName: "heart.fill")
                    .font(.system(size: 11, weight: .regular))
                    .foregroundStyle(lexoraPalette(scheme).accent)
                Text("Crafted in the spirit of the Cotswolds")
                    .font(.lexoraBody(11))
                    .foregroundStyle(lexoraPalette(scheme).mutedForeground)
            }
            Text("Lexora · Words. Wisdom. Worlds.")
                .font(.lexoraBody(10))
                .foregroundStyle(lexoraPalette(scheme).mutedForeground.opacity(0.6))
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 24)
        .padding(.bottom, 16)
    }
}

// MARK: - SettingsGroup

private struct SettingsRowSpec: Identifiable {
    let id = UUID()
    let icon: String
    let label: String
    let value: String
    let action: () -> Void
}

private struct SettingsGroup: View {
    let title: String
    let rows: [SettingsRowSpec]
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(title.uppercased())
                .font(.lexoraBody(11, weight: .semibold))
                .tracking(1.4)
                .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                .padding(.horizontal, 4)

            VStack(spacing: 0) {
                ForEach(Array(rows.enumerated()), id: \.element.id) { idx, row in
                    SettingsRow(
                        icon: row.icon,
                        label: row.label,
                        value: row.value,
                        isFirst: idx == 0,
                        onClick: row.action
                    )
                }
            }
            .background(lexoraPalette(scheme).card)
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 16, style: .continuous)
                    .stroke(lexoraPalette(scheme).border, lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.04), radius: 4, x: 0, y: 2)
        }
    }
}

// MARK: - SettingsRow

private struct SettingsRow: View {
    let icon: String
    let label: String
    let value: String
    let isFirst: Bool
    let onClick: () -> Void
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        Button {
            onClick()
        } label: {
            HStack(spacing: 12) {
                // Icon in a rounded square (primary @ 10% opacity)
                Image(systemName: icon)
                    .font(.system(size: 14, weight: .regular))
                    .foregroundStyle(lexoraPalette(scheme).primary)
                    .frame(width: 32, height: 32)
                    .background(lexoraPalette(scheme).primary.opacity(0.10))
                    .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))

                Text(label)
                    .font(.lexoraBody(14))
                    .foregroundStyle(lexoraPalette(scheme).foreground)

                Spacer()

                if !value.isEmpty {
                    Text(value)
                        .font(.lexoraBody(12))
                        .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                }

                Image(systemName: "chevron.right")
                    .font(.system(size: 12, weight: .regular))
                    .foregroundStyle(lexoraPalette(scheme).mutedForeground.opacity(0.6))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .frame(maxWidth: .infinity, alignment: .leading)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .overlay(alignment: .top) {
            if !isFirst {
                Rectangle()
                    .fill(lexoraPalette(scheme).border)
                    .frame(height: 1)
                    .padding(.horizontal, 16)
            }
        }
    }
}

#Preview {
    MoreScreen()
        .environmentObject(LexoraViewModel())
        .environmentObject(LexoraThemeState())
}
