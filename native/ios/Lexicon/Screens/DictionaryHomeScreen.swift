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
                    VStack(spacing: 4) {
                        if vm.recentSearches.isEmpty {
                            Text("Search for a word to begin your journey.")
                                .font(.lexiconBody(12)).foregroundStyle(p.mutedForeground)
                                .padding(16)
                        }
                        ForEach(Array(vm.recentSearches.prefix(3).enumerated()), id: \.offset) { _, word in
                            let entry = LexiconData.getWordById(word)
                            Button { vm.submitSearch(word) } label: {
                                HStack(spacing: 12) {
                                    Image(systemName: "clock.arrow.circlepath")
                                        .foregroundStyle(p.highlight)
                                    VStack(alignment: .leading, spacing: 3) {
                                        Text(word).font(.lexiconDisplay(16))
                                            .foregroundStyle(p.foreground).fixedSize(horizontal: false, vertical: true)
                                        Text(entry.map { $0.partOfSpeech.displayValue + " · " + $0.phonetic }
                                            ?? "Search again for details")
                                            .font(.lexiconBody(10.5).italic())
                                            .foregroundStyle(p.mutedForeground).fixedSize(horizontal: false, vertical: true)
                                    }
                                    Spacer()
                                    Image(systemName: "chevron.right").foregroundStyle(p.foreground)
                                }
                                .padding(.horizontal, 14).padding(.vertical, 8)
                                .frame(minHeight: 44)
                                .background(p.card, in: RoundedRectangle(cornerRadius: 17))
                                .overlay(RoundedRectangle(cornerRadius: 17).stroke(p.border, lineWidth: 1))
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 16)
                }

                VStack(alignment: .leading, spacing: 10) {
                    Label("Quick Access", systemImage: "lightbulb").font(.lexiconDisplay(17))
                        .foregroundStyle(p.foreground)
                    Text("Jump to useful tools and features.")
                        .font(.lexiconBody(11)).foregroundStyle(p.mutedForeground)
                    LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 7), count: dynamicTypeSize >= .xxLarge ? 2 : 4), spacing: 7) {
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
                Image(countrysideImageName("ill-banner-church", isDark: isDark, scene: "dictionary-panorama"))
                    .resizable()
                    .aspectRatio(758.0 / 132.0, contentMode: .fill)
                    .clipped()
                    .frame(maxWidth: 640)
                    .background(p.card)
                    .clipShape(RoundedRectangle(cornerRadius: 15))
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
                    .multilineTextAlignment(.center).fixedSize(horizontal: false, vertical: true)
            }
            .frame(maxWidth: .infinity, minHeight: 62)
            .background(p.background.opacity(0.55))
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(RoundedRectangle(cornerRadius: 14).stroke(p.border, lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}


// Editorial lookup prompts, not an exhaustive subject index.
private let categoryLookupTerms: [String: [String]] = [
    "vocabulary": ["serendipity", "eloquence", "ephemeral", "limerence", "ethereal", "solitude"],
    "geography": ["archipelago", "estuary", "peninsula", "longitude", "latitude", "watershed"],
    "literature": ["metaphor", "allegory", "sonnet", "prologue", "elegy", "alliteration"],
    "science": ["photosynthesis", "osmosis", "gravity", "molecule", "entropy", "ecosystem"],
    "history": ["civilization", "chronicle", "dynasty", "renaissance", "archaeology", "antiquity"],
    "mythology": ["myth", "oracle", "phoenix", "labyrinth", "titan", "legend"],
    "philosophy": ["ethics", "epistemology", "ontology", "stoicism", "virtue", "reason"],
    "art_architecture": ["fresco", "perspective", "arch", "colonnade", "mosaic", "facade"],
    "nature": ["petrichor", "meadow", "hedgerow", "canopy", "flora", "fauna"],
    "culture_traditions": ["heritage", "folklore", "ritual", "custom", "festival", "tradition"],
]

struct CategoryDirectoryScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @Environment(\.colorScheme) private var scheme
    @State private var pageWidth: CGFloat = 390
    private var columns: Int { dynamicTypeSize.isAccessibilitySize || pageWidth < 280 ? 1 : (pageWidth >= 540 && dynamicTypeSize < .xxLarge ? 3 : 2) }
    private var artHeight: CGFloat { min(154, max(76, (pageWidth - 32 - CGFloat(columns - 1) * 10) / CGFloat(columns) * 0.62)) }
    var body: some View {
        ScrollView {
            VStack(spacing: 12) {
                let p = lexiconPalette(scheme)
                VStack(alignment: .leading, spacing: 4) {
                    HStack {
                        Button { vm.navigateBack() } label: {
                            Image(systemName: "chevron.left").frame(width: 44, height: 44)
                        }.buttonStyle(.plain).accessibilityLabel("Back")
                        Spacer()
                        Button { vm.openSearch() } label: {
                            Image(systemName: "magnifyingglass").frame(width: 44, height: 44)
                        }.buttonStyle(.plain).accessibilityLabel("Search dictionary")
                    }.foregroundStyle(p.primary)
                    Text("Explore\nCategories").font(.lexiconDisplay(26))
                        .foregroundStyle(p.foreground).fixedSize(horizontal: false, vertical: true)
                        .padding(.leading, 4)
                }.padding(.horizontal, 16).padding(.vertical, 6)
                LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 10), count: columns), spacing: 12) {
                    ForEach(LexiconData.categories) { category in
                        CategoryCard(category: category, shelfArtworkHeight: artHeight)
                    }
                }.padding(.horizontal, 16)
            }.padding(.bottom, 16)
        }.background { GeometryReader { geo in
            Color.clear.onAppear { pageWidth = geo.size.width }.onChange(of: geo.size.width) { pageWidth = $0 }
        } }
    }
}

struct CategoryDetailScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme
    var body: some View {
        let p = lexiconPalette(scheme)
        let category = LexiconData.categories.first(where: { $0.id == vm.selectedCategoryId }) ?? LexiconData.categories[0]
        let scene = "home-" + category.id.replacingOccurrences(of: "_", with: "-")
        let name = scheme == .dark ? "ref-" + scene :
            countrysideImageName(category.illustration, isDark: false, scene: scene)
        ScrollView {
            VStack(alignment: .leading, spacing: 11) {
                HStack {
                    Button { vm.navigateBack() } label: {
                        Image(systemName: "chevron.left").foregroundStyle(p.primary)
                            .frame(width: 44, height: 44)
                    }.buttonStyle(.plain).accessibilityLabel("Back")
                    Text(category.title).font(.lexiconDisplay(26)).foregroundStyle(p.foreground)
                        .fixedSize(horizontal: false, vertical: true)
                    Spacer(minLength: 0)
                }
                Text(category.subtitle).font(.lexiconLong(14)).foregroundStyle(p.mutedForeground)
                    .padding(.leading, 10)
                Image(name).resizable().scaledToFit().frame(height: 130)
                    .frame(maxWidth: .infinity).accessibilityHidden(true)
                HStack(spacing: 6) {
                    BotanicalSprig(color: p.highlight).frame(width: 18, height: 18)
                    Text("Words to discover").font(.lexiconDisplay(18)).foregroundStyle(p.foreground)
                }
                ForEach(categoryLookupTerms[category.id] ?? [], id: \.self) { term in
                    Button { vm.lookupCategoryTerm(term) } label: {
                        HStack(spacing: 10) {
                            BotanicalSprig(color: p.highlight).frame(width: 18, height: 18)
                            Text(term).font(.lexiconDisplay(17)).foregroundStyle(p.foreground)
                                .fixedSize(horizontal: false, vertical: true)
                            Spacer()
                            Image(systemName: "chevron.right").foregroundStyle(p.primary)
                        }.padding(.horizontal, 14).padding(.vertical, 10)
                            .frame(minHeight: 52)
                            .background(p.card, in: RoundedRectangle(cornerRadius: 14))
                            .overlay(RoundedRectangle(cornerRadius: 14).stroke(p.border, lineWidth: 1))
                    }.buttonStyle(.plain)
                }
                Text("Suggested terms to look up in the dictionary.").font(.lexiconBody(12))
                    .foregroundStyle(p.mutedForeground).padding(.horizontal, 4)
            }.padding(.horizontal, 16).padding(.bottom, 16)
        }
    }
}
