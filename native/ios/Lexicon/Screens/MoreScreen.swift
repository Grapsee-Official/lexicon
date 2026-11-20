//
//  MoreScreen.swift
//  Lexicon
//
//  Translation of `src/components/lexicon/screens/more-screen.tsx`.
//
//  Profile header card + 4 settings groups (Appearance, Dictionary,
//  Privacy & Data, About) + footer credit. Each group is an uppercase muted
//  header above a card of rows. Each row has an icon in a rounded square
//  (primary @ 10% opacity), a label, a value (optional), and a chevron right.
//
//  Theme toggle reads the shared LexiconThemeState (the next-themes
//  equivalent) and calls theme.toggle(). The isDark resolution mirrors the
//  web `(resolvedTheme ?? theme) === "dark"` logic by combining the stored
//  preference with the current UITraitCollection color scheme when "system".
//

import SwiftUI

private enum MoreReadingDestination: String, Identifiable {
    case about, privacy, licenses, journey
    var id: String { rawValue }
}


struct MoreScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @EnvironmentObject private var theme: LexiconThemeState
    @Environment(\.colorScheme) private var scheme
    @State private var infoKey: String? = nil
    @State private var readingPage: MoreReadingDestination? = nil

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                profileHeader

                SettingsGroup(title: "Appearance", rows: [
                    SettingsRowSpec(
                        icon: isDark ? "moon" : "sun.max",
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
                        value: "Device",
                        action: { infoKey = "text" }
                    ),
                    SettingsRowSpec(
                        icon: "bell",
                        label: "Notifications",
                        value: "Not scheduled",
                        action: { infoKey = "notifications" }
                    ),
                ])

                SettingsGroup(title: "Dictionary", rows: [
                    SettingsRowSpec(
                        icon: "globe",
                        label: "Language pairs",
                        value: "\(LexiconData.translationsFor(LexiconData.serendipityEntry).count) samples",
                        action: { vm.navigate(to: .expandedExploration, word: LexiconData.serendipityEntry, expand36: true) }
                    ),
                    SettingsRowSpec(
                        icon: "externaldrive",
                        label: "Storage",
                        value: "On device",
                        action: { infoKey = "storage" }
                    ),
                    SettingsRowSpec(
                        icon: "arrow.down.circle",
                        label: "Download language packs",
                        value: "Unavailable",
                        action: { infoKey = "packs" }
                    ),
                ])

                SettingsGroup(title: "Privacy & Data", rows: [
                    SettingsRowSpec(
                        icon: "doc.text",
                        label: "Clear recent searches",
                        value: vm.recentSearches.count > 0 ? "\(vm.recentSearches.count) items" : "Empty",
                        action: { infoKey = "recent" }
                    ),
                    SettingsRowSpec(
                        icon: "externaldrive",
                        label: "Clear cache",
                        value: "Temporary files",
                        action: { infoKey = "cache" }
                    ),
                ])

                SettingsGroup(title: "Your Library", rows: [
                    SettingsRowSpec(
                        icon: "books.vertical",
                        label: "Reading journey",
                        value: "\(vm.recentSearches.count) recent · \(vm.savedWordIds.count) saved",
                        action: { readingPage = .journey }
                    ),
                ])

                SettingsGroup(title: "About", rows: [
                    SettingsRowSpec(
                        icon: "info.circle",
                        label: "About Lexicon",
                        value: "Native edition",
                        action: { readingPage = .about }
                    ),
                    SettingsRowSpec(
                        icon: "doc.text",
                        label: "Privacy & storage",
                        value: "",
                        action: { readingPage = .privacy }
                    ),
                    SettingsRowSpec(
                        icon: "chevron.left.slash.chevron.right",
                        label: "Open source licences",
                        value: "",
                        action: { readingPage = .licenses }
                    ),
                ])

                footer
            }
            .padding(.horizontal, 16)
            .padding(.top, 12)
            .padding(.bottom, 160) // reveal content above floating bottom nav
        }
        .background(lexiconPalette(scheme).background)
        .alert(infoTitle, isPresented: Binding(
            get: { infoKey != nil },
            set: { if !$0 { infoKey = nil } }
        )) {
            if infoKey == "recent" || infoKey == "cache" {
                Button("Cancel", role: .cancel) { infoKey = nil }
                Button(infoKey == "recent" ? "Clear history" : "Clear cache", role: .destructive) {
                    if infoKey == "recent" {
                        vm.clearRecentSearches()
                    } else {
                        clearTemporaryCache()
                    }
                    infoKey = nil
                }
            } else {
                Button("Done", role: .cancel) { infoKey = nil }
            }
        } message: {
            Text(infoMessage)
        }
        .sheet(item: $readingPage) { page in
            MoreReadingPage(page: page)
        }
    }

    private var infoTitle: String {
        switch infoKey {
        case "text": return "Comfortable reading"
        case "notifications": return "Daily-word reminders"
        case "storage": return "Your offline library"
        case "packs": return "Language packs"
        case "recent": return "Clear search history?"
        case "cache": return "Clear temporary cache?"
        case "about": return "About Lexicon"
        case "privacy": return "Privacy & local storage"
        default: return "Open-source libraries"
        }
    }

    private var infoMessage: String {
        switch infoKey {
        case "text": return "Lexicon follows Dynamic Type. To change reading size, open iOS Settings → Accessibility → Display & Text Size."
        case "notifications": return "Daily notifications are not currently scheduled. Your Word of the Day remains available offline."
        case "storage": return "Saved words, notes, study lists and templates are stored on this device. The bundled dictionary works offline."
        case "packs": return "The English dictionary is bundled. Additional downloadable language packs are not yet supported. The translation view shows only curated available translations."
        case "recent": return "Remove recent searches on this device? Saved words and notes will be kept."
        case "cache": return "Remove temporary files? Saved words, notes and study progress will remain."
        case "about": return "Lexicon · Words. Wisdom. Worlds. A quiet offline-first language companion inspired by the English countryside."
        case "privacy": return "Word collections, notes, settings and study progress are kept in local app storage. Search history and temporary files can be cleared separately."
        default: return "Lexicon uses SwiftUI, SQLite and a shared C++17 dictionary engine. See the license notices of each external dependency before redistribution."
        }
    }

    private func clearTemporaryCache() {
        let manager = FileManager.default
        guard let cache = manager.urls(for: .cachesDirectory, in: .userDomainMask).first,
              let children = try? manager.contentsOfDirectory(at: cache, includingPropertiesForKeys: nil)
        else { return }
        for file in children { try? manager.removeItem(at: file) }
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
            Image(isDark ? "avatar-dark" : "avatar-default")
                .resizable()
                .scaledToFill()
                .frame(width: 64, height: 64)
                .clipShape(Circle())
                .overlay(
                    Circle().stroke(lexiconPalette(scheme).primary.opacity(0.30), lineWidth: 2)
                )
                .accessibilityHidden(true)

            VStack(alignment: .leading, spacing: 4) {
                Text("Curious Soul")
                    .font(.lexiconDisplay(18, weight: .semibold))
                    .foregroundStyle(lexiconPalette(scheme).foreground)
                Text("Lexicon Companion · since 2026")
                    .font(.lexiconBody(12))
                    .foregroundStyle(lexiconPalette(scheme).mutedForeground)
                HStack(spacing: 16) {
                    statLabel(value: vm.savedWordIds.count, label: "saved")
                    statLabel(value: vm.recentSearches.count, label: "recent")
                }
                .padding(.top, 2)
            }
            Spacer(minLength: 0)
        }
        .padding(16)
        .background(lexiconPalette(scheme).card)
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(lexiconPalette(scheme).border, lineWidth: 1)
        )
        .shadow(color: Color.black.opacity(0.04), radius: 4, x: 0, y: 2)
    }

    private func statLabel(value: Int, label: String) -> some View {
        HStack(spacing: 4) {
            Text("\(value)")
                .font(.lexiconBody(11, weight: .semibold))
                .foregroundStyle(lexiconPalette(scheme).foreground)
            Text(label)
                .font(.lexiconBody(11))
                .foregroundStyle(lexiconPalette(scheme).mutedForeground)
        }
    }

    // MARK: - Footer

    private var footer: some View {
        VStack(spacing: 4) {
            HStack(spacing: 6) {
                Image(systemName: "heart.fill")
                    .font(.system(size: 11, weight: .regular))
                    .foregroundStyle(lexiconPalette(scheme).accent)
                Text("Crafted in the spirit of the Cotswolds")
                    .font(.lexiconBody(11))
                    .foregroundStyle(lexiconPalette(scheme).mutedForeground)
            }
            Text("Lexicon · Words. Wisdom. Worlds.")
                .font(.lexiconBody(10))
                .foregroundStyle(lexiconPalette(scheme).mutedForeground.opacity(0.6))
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
                .font(.lexiconBody(11, weight: .semibold))
                .tracking(1.4)
                .foregroundStyle(lexiconPalette(scheme).mutedForeground)
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
            .background(lexiconPalette(scheme).card)
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 16, style: .continuous)
                    .stroke(lexiconPalette(scheme).border, lineWidth: 1)
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
                    .foregroundStyle(lexiconPalette(scheme).primary)
                    .frame(width: 32, height: 32)
                    .background(lexiconPalette(scheme).primary.opacity(0.10))
                    .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))

                Text(label)
                    .font(.lexiconBody(14))
                    .foregroundStyle(lexiconPalette(scheme).foreground)

                Spacer()

                if !value.isEmpty {
                    Text(value)
                        .font(.lexiconBody(12))
                        .foregroundStyle(lexiconPalette(scheme).mutedForeground)
                }

                Image(systemName: "chevron.right")
                    .font(.system(size: 12, weight: .regular))
                    .foregroundStyle(lexiconPalette(scheme).mutedForeground.opacity(0.6))
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
                    .fill(lexiconPalette(scheme).border)
                    .frame(height: 1)
                    .padding(.horizontal, 16)
            }
        }
    }
}

#Preview {
    MoreScreen()
        .environmentObject(LexiconViewModel())
        .environmentObject(LexiconThemeState())
}

private struct MoreReadingPage: View {
    let page: MoreReadingDestination
    @Environment(\.dismiss) private var dismiss
    @Environment(\.colorScheme) private var scheme
    @EnvironmentObject private var vm: LexiconViewModel

    private var title: String {
        switch page {
        case .about: return "About Lexicon"
        case .privacy: return "Privacy & storage"
        case .licenses: return "Open-source notices"
        case .journey: return "Reading Journey"
        }
    }

    private var paragraphs: [String] {
        switch page {
        case .about:
            return [
                "Words. Wisdom. Worlds.",
                "Lexicon is a quiet, native reading companion inspired by the old English countryside. Its illustrated dictionary, journals and saved words are parts of one world.",
                "Built for iOS with SwiftUI and a shared C++17 dictionary engine. The starter English library and your saved words are available offline."
            ]
        case .privacy:
            return [
                "Your words are yours.",
                "Saved words, notes, templates, study lists and recent searches are stored in the application's local storage. Search history can be cleared separately without removing saved notes.",
                "Temporary caches can be cleared from More. Removing the app may remove locally stored material. Keep your own copies of important writing."
            ]
        case .licenses:
            return [
                "A little library is built by many hands.",
                "Lexicon uses SwiftUI, SQLite and the shared C++17 dictionary engine. Software libraries retain their respective licences.",
                "Consult the dependency distributions and the Grapsee-Official/lexicon repository for applicable upstream licence texts. This overview is not a replacement for complete legal notices."
            ]
        case .journey:
            return []
        }
    }

    var body: some View {
        let palette = lexiconPalette(scheme)
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    if page == .journey {
                        // Actual saved and searched words from the same local
                        // model used by Dictionary and Collections.
                        Text("Words gathered along the way")
                            .font(.lexiconLong(16))
                            .foregroundStyle(palette.mutedForeground)
                        Text("Recently explored")
                            .font(.lexiconDisplay(23))
                            .foregroundStyle(palette.foreground)
                        if vm.recentSearches.isEmpty {
                            Text("Your discoveries will appear here after you search for a word.")
                                .font(.lexiconLong(14))
                                .foregroundStyle(palette.mutedForeground)
                        }
                        ForEach(Array(vm.recentSearches.enumerated()), id: \.offset) { _, query in
                            Button {
                                dismiss()
                                if let word = LexiconData.getWordById(query.lowercased()) {
                                    vm.openWord(word)
                                } else {
                                    vm.submitSearch(query)
                                }
                            } label: {
                                HStack {
                                    Image(systemName: "clock.arrow.circlepath").foregroundStyle(palette.primary)
                                    Text(query).font(.lexiconDisplay(18)).foregroundStyle(palette.foreground)
                                    Spacer()
                                    Image(systemName: "chevron.right").foregroundStyle(palette.mutedForeground)
                                }
                                .padding(15)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(palette.card, in: RoundedRectangle(cornerRadius: 14))
                                .overlay(RoundedRectangle(cornerRadius: 14).stroke(palette.border, lineWidth: 1))
                            }
                            .buttonStyle(.plain)
                        }
                        Text("Words kept close")
                            .font(.lexiconDisplay(23))
                            .foregroundStyle(palette.foreground)
                        if vm.savedWordIds.isEmpty {
                            Text("Bookmark a word to gather it in your personal library.")
                                .font(.lexiconLong(14))
                                .foregroundStyle(palette.mutedForeground)
                        }
                        ForEach(Array(vm.savedWordIds.enumerated()), id: \.offset) { _, wordId in
                            if let entry = LexiconData.getWordById(wordId) {
                                Button {
                                    dismiss()
                                    vm.openWord(entry)
                                } label: {
                                    HStack {
                                        Image(systemName: "bookmark.fill").foregroundStyle(palette.primary)
                                        VStack(alignment: .leading, spacing: 3) {
                                            Text(entry.word)
                                                .font(.lexiconDisplay(18))
                                                .foregroundStyle(palette.foreground)
                                            Text(entry.phonetic)
                                                .font(.lexiconBody(11))
                                                .foregroundStyle(palette.mutedForeground)
                                        }
                                        Spacer()
                                        Image(systemName: "chevron.right").foregroundStyle(palette.mutedForeground)
                                    }
                                    .padding(15)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .background(palette.card, in: RoundedRectangle(cornerRadius: 14))
                                    .overlay(RoundedRectangle(cornerRadius: 14).stroke(palette.border, lineWidth: 1))
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    } else {
                        ForEach(Array(paragraphs.enumerated()), id: \.offset) { index, paragraph in
                            Text(paragraph)
                                .font(index == 0 ? .lexiconDisplay(24) : .lexiconLong(16))
                                .foregroundStyle(palette.foreground)
                                .fixedSize(horizontal: false, vertical: true)
                        }
                    }
                }
                .frame(maxWidth: 620, alignment: .leading)
                .padding(24)
                .frame(maxWidth: .infinity, alignment: .center)
            }
            .background(palette.background)
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarLeading) {
                    Button("Back") { dismiss() }
                        .accessibilityLabel("Back to More")
                }
            }
        }
    }
}
