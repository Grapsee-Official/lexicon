import SwiftUI

/// Original Lexicon Dictionary watercolor layout with real native controls.
struct DictionaryHomeScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @EnvironmentObject private var theme: LexiconThemeState
    @Environment(\.colorScheme) private var scheme
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @State private var draft = ""
    private var isDark: Bool { scheme == .dark }

    var body: some View {
        let p = lexiconPalette(scheme)
        ScrollView {
            VStack(spacing: 13) {
                CountrysideHeroHeader(
                    title: "Dictionary", subtitle: "Look up any word, anytime.",
                    illustrationBase: "ill-hero-cottage",
                    onHistoryClick: { vm.navigate(to: .searchDiscovery) },
                    onLogoToggleTheme: { theme.toggle() }, heroHeight: 148
                )

                CountrysideSearchPill(
                    text: $draft, placeholder: "Search for a word...",
                    onSubmit: {
                        if draft.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty {
                            vm.navigate(to: .searchDiscovery)
                        } else { vm.submitSearch(draft) }
                    },
                    showFilter: true,
                    onFilterClick: { vm.navigate(to: .searchDiscovery) }
                )
                .padding(.horizontal, 16)

                VStack(spacing: 8) {
                    BotanicalSectionTitle(
                        title: "Popular Searches",
                        actionLabel: vm.popularSearches.isEmpty ? "Restore" : "Clear All",
                        isClearAction: !vm.popularSearches.isEmpty,
                        onAction: {
                            if vm.popularSearches.isEmpty { vm.restorePopularSearches() }
                            else { vm.clearPopularSearches() }
                        }
                    )
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            if vm.popularSearches.isEmpty {
                                Text("Restore suggestions or search above.")
                                    .font(.lexiconBody(12)).foregroundStyle(p.mutedForeground)
                            }
                            ForEach(vm.popularSearches, id: \.self) { word in
                                Button { vm.submitSearch(word) } label: {
                                    Text(word).font(.lexiconDisplay(12.5))
                                        .foregroundStyle(p.foreground)
                                        .padding(.horizontal, 13).padding(.vertical, 9)
                                        .background(p.card, in: Capsule())
                                        .overlay(Capsule().stroke(p.border, lineWidth: 1))
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                }

                CountrysideQuoteBanner(
                    quote: "Words are doors to new worlds.",
                    attribution: "Lexora", illustrationBase: "ill-books-daisies"
                )
                .padding(.horizontal, 16)

                VStack(spacing: 8) {
                    BotanicalSectionTitle(
                        title: "Recently Searched", actionLabel: "View all",
                        onAction: { vm.navigate(to: .searchDiscovery) }
                    )
                    VStack(spacing: 0) {
                        if vm.recentSearches.isEmpty {
                            Text("Search for a word to begin your journey.")
                                .font(.lexiconBody(12)).foregroundStyle(p.mutedForeground)
                                .padding(16)
                        }
                        ForEach(Array(vm.recentSearches.prefix(3).enumerated()), id: \.offset) { index, word in
                            if index > 0 { Rectangle().fill(p.border).frame(height: 1) }
                            let entry = LexiconData.getWordById(word)
                            Button { vm.submitSearch(word) } label: {
                                HStack(spacing: 12) {
                                    Image(systemName: "clock.arrow.circlepath")
                                        .foregroundStyle(p.highlight)
                                    VStack(alignment: .leading, spacing: 3) {
                                        Text(word).font(.lexiconDisplay(16))
                                            .foregroundStyle(p.foreground).lineLimit(1)
                                        Text(entry.map { $0.partOfSpeech.displayValue + " · " + $0.phonetic }
                                            ?? "Search again for details")
                                            .font(.lexiconBody(10.5).italic())
                                            .foregroundStyle(p.mutedForeground).lineLimit(1)
                                    }
                                    Spacer()
                                    Image(systemName: "chevron.right").foregroundStyle(p.foreground)
                                }
                                .padding(.horizontal, 14).padding(.vertical, 11)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .background(p.card)
                    .clipShape(RoundedRectangle(cornerRadius: 17, style: .continuous))
                    .overlay(RoundedRectangle(cornerRadius: 17, style: .continuous)
                        .stroke(p.border, lineWidth: 1))
                    .padding(.horizontal, 16)
                }

                VStack(alignment: .leading, spacing: 10) {
                    Label("Quick Access", systemImage: "lightbulb").font(.lexiconDisplay(17))
                        .foregroundStyle(p.foreground)
                    Text("Jump to useful tools and features.")
                        .font(.lexiconBody(11)).foregroundStyle(p.mutedForeground)
                    LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 7), count: dynamicTypeSize.isAccessibilitySize ? 2 : 4), spacing: 7) {
                        quickAccess("Word of the Day", icon: "book", route: .wordOfTheDay, p: p)
                        quickAccess("Favourite Words", icon: "star", route: .collections, p: p)
                        quickAccess("Bookmarks", icon: "bookmark", route: .collections, p: p)
                        quickAccess("History", icon: "clock.arrow.circlepath", route: .searchDiscovery, p: p)
                    }
                }
                .padding(12)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(p.card)
                .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                .overlay(RoundedRectangle(cornerRadius: 18, style: .continuous)
                    .stroke(p.border, lineWidth: 1))
                .padding(.horizontal, 16)

                // Preserve the entire original panorama, with the intrinsic
                // 420:182 aspect rather than cropping it into a 76pt strip.
                // On iPad it becomes a centred, generously sized bookplate.
                Image(countrysideImageName("ill-banner-church", isDark: isDark))
                    .resizable()
                    .aspectRatio(758.0 / 132.0, contentMode: .fill)
                    .clipped()
                    .frame(maxWidth: 640)
                    .background(p.card)
                    .overlay(RoundedRectangle(cornerRadius: 15)
                        .stroke(p.border, lineWidth: 1))
                    .padding(.horizontal, 16)
                    .frame(maxWidth: .infinity)
                    .accessibilityHidden(true)
            }
            .padding(.bottom, 16)
        }
    }

    private func quickAccess(_ title: String, icon: String, route: ScreenRoute,
                             p: LexiconPalette) -> some View {
        Button {
            if title == "Favourite Words" { vm.openCollectionFilter("Favorites") }
            else if title == "Bookmarks" { vm.openCollectionFilter("Bookmarks") }
            else { vm.navigate(to: route) }
        } label: {
            VStack(spacing: 6) {
                Image(systemName: icon).font(.system(size: 21))
                    .foregroundStyle(isDark ? LexiconColors.accentDark : LexiconColors.primary)
                Text(title).font(.lexiconBody(10))
                    .foregroundStyle(p.foreground)
                    .multilineTextAlignment(.center).lineLimit(2)
            }
            .frame(maxWidth: .infinity, minHeight: 62)
            .background(p.background.opacity(0.55))
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(RoundedRectangle(cornerRadius: 14).stroke(p.border, lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}
