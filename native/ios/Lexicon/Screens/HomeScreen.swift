//
//  HomeScreen.swift
//  Lexicon
//

import SwiftUI

struct HomeScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    // A fixed editorial fixture makes pixel comparisons reproducible.
    // Real readers continue receiving the daily rotating featured word.
    private var displayedHomeWord: WordEntry {
        #if DEBUG
        if ProcessInfo.processInfo.arguments.contains("-lexoraVisualQa") || ProcessInfo.processInfo.arguments.contains("-lexiconSnapshotSyncDir") {
            return LexiconData.wordOfTheDay
        }
        #endif
        return LexiconData.featuredWord()
    }

    var body: some View {
        GeometryReader { window in
          let pageWidth = min(640, window.size.width)
          let pageInset: CGFloat = scheme == .dark ? min(32, max(12, pageWidth * 30 / 591)) : 16
          let referenceGap = min(18, max(9, pageWidth * 20 / 591))
          ScrollView {
            VStack(spacing: scheme == .dark ? referenceGap : 11) {
                VStack(spacing: scheme == .dark ? 10 : 11) {
                    Hero(sharedNightScene: scheme == .dark && !referenceHeroStacks(width: window.size.width, textSize: dynamicTypeSize))
                        .background {
                            if scheme == .dark {
                                GeometryReader { geometry in
                                    Color.clear.preference(key: HomeHeroHeightKey.self, value: geometry.size.height)
                                }
                            }
                        }
                    TrendingTagRow(tags: LexiconData.trendingTags, sceneIntegrated: scheme == .dark,
                                   referencePageWidth: scheme == .dark ? pageWidth : nil)
                        .padding(.horizontal, pageInset)
                }
                .background {
                    if scheme == .dark {
                        GeometryReader { geometry in
                            Color.clear.preference(
                                key: HomeMastheadFrameKey.self,
                                value: geometry.frame(in: .named("homeScene"))
                            )
                        }
                    }
                }

                WordOfTheDayCard(word: displayedHomeWord)
                    .padding(.horizontal, pageInset)

                // Ten original bookplates in the same canonical order as Android.
                VStack(spacing: scheme == .dark ? pageWidth * 12 / 591 : 10) {
                    SectionHeader(
                        title: "Explore by Category",
                        actionLabel: "View all",
                        divider: false,
                        onAction: { vm.setActiveTab(.dictionary) },
                        homeEditorial: true, homeInset: pageInset
                    )
                    let available = min(640, window.size.width)
                    let count = homeShelfColumns(width: available, textSize: dynamicTypeSize)
                    let gap = scheme == .dark ? min(16, max(6, available * 14 / 591)) : 8
                    let inset = pageInset
                    let peek = scheme == .dark && !dynamicTypeSize.isAccessibilitySize ? available * 21 / 591 : 0
                    let bookplateWidth = max(56, (available - 2 * inset - CGFloat(count - 1) * gap - peek) / CGFloat(count))
                    ScrollView(.horizontal, showsIndicators: false) {
                        LazyHStack(alignment: .top, spacing: gap) {
                            ForEach(LexiconData.categories) { category in
                                CategoryCard(category: category,
                                    shelfArtworkHeight: min(96, max(46, bookplateWidth * (scheme == .dark ? 85.0 / 117.0 : 0.82))),
                                    referenceShelfWidth: scheme == .dark ? bookplateWidth : nil)
                                    .frame(width: bookplateWidth)
                            }
                        }
                        .padding(.horizontal, inset)
                    }
                    // Let the tallest visible caption determine shelf height.
                    // No fixed GeometryReader frame to clip large text.
                }
                .padding(.top, scheme == .dark ? pageWidth * 10 / 591 : 0)

                // From the Library (with horizontal hairline divider)
                VStack(spacing: scheme == .dark ? pageWidth * 14 / 591 : 10) {
                    SectionHeader(
                        title: "From the Library",
                        actionLabel: "View all articles",
                        divider: true,
                        onAction: { vm.openLibrary() },
                        homeEditorial: true, homeInset: pageInset
                    )
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: scheme == .dark ? window.size.width * 13 / 591 : 10) {
                            ForEach(LexiconData.articles) { a in
                                ArticleCard(
                                    article: a,
                                    shelfWidth: scheme == .dark && !dynamicTypeSize.isAccessibilitySize
                                        ? (a.id == "a1" ? min(260, max(130, window.size.width * 240 / 591)) : min(200, max(112, window.size.width * 180 / 591))) : nil,
                                    shelfHeight: scheme == .dark && !dynamicTypeSize.isAccessibilitySize
                                        ? min(132, max(68, window.size.width * 122 / 591)) : nil,
                                    onOpen: { vm.openLibraryArticle(a.id) }
                                )
                            }
                        }
                        .padding(.horizontal, pageInset)
                    }
                }
                .padding(.top, scheme == .dark ? pageWidth * 10 / 591 : 0)
            }
            .padding(.top, 1)
            .padding(.bottom, 12)
          }
        }
        .background(scheme == .dark ? Color.clear : LexiconColors.background)
    }
}

// Same width-led policy as Android; adapt to Dynamic Type rather than
// assuming that every phone is a fixed 390-point design canvas.
private func homeShelfColumns(width: CGFloat, textSize: DynamicTypeSize) -> Int {
    if textSize >= .accessibility3 { return width >= 560 ? 3 : 2 }
    if textSize.isAccessibilitySize { return width >= 560 ? 4 : 3 }
    if width >= 560 { return 5 }
    if width < 290 || (width < 400 && textSize >= .xxxLarge) { return 3 }
    return 4
}

// The header and masthead publish geometry in the page coordinate space.
// ContentView owns the actual painting, outside the clipping scroll view.
struct HomeMastheadFrameKey: PreferenceKey {
    static var defaultValue: CGRect = .zero
    static func reduce(value: inout CGRect, nextValue: () -> CGRect) {
        let next = nextValue()
        if !next.isEmpty { value = next }
    }
}

struct HomeHeroHeightKey: PreferenceKey {
    static var defaultValue: CGFloat = 0
    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) {
        value = max(value, nextValue())
    }
}

struct HomeHeaderHeightKey: PreferenceKey {
    static var defaultValue: CGFloat = 0
    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) {
        value = max(value, nextValue())
    }
}

// The header and scroll feed reveal a single continuous native canvas.
struct HomeNightBackdrop: View {
    var body: some View {
        GeometryReader { geo in
            let width = geo.size.width
            let height = max(1, geo.size.height)
            ZStack {
                LinearGradient(stops: [
                    // Sampled from the reference's unpainted screen areas.
                    .init(color: Color(hex: "050F18"), location: 0),
                    .init(color: Color(hex: "040E17"), location: 0.28),
                    .init(color: Color(hex: "061019"), location: 0.57),
                    .init(color: Color(hex: "08111A"), location: 1)
                ], startPoint: .top, endPoint: .bottom)

                // The same very low-contrast wash as the Compose canvas:
                // continuous night ink beneath the header and illustrated sky.
                RadialGradient(
                    colors: [Color(hex: "192A3C").opacity(0.085), .clear],
                    center: UnitPoint(x: 0.78, y: width * 0.38 / height),
                    startRadius: 0, endRadius: max(1, width * 0.76)
                )
                RadialGradient(
                    colors: [Color(hex: "51412C").opacity(0.025), .clear],
                    center: UnitPoint(x: 0.79, y: width * 0.70 / height),
                    startRadius: 0, endRadius: max(1, width * 0.46)
                )
            }
        }
        .ignoresSafeArea()
        .allowsHitTesting(false)
        .accessibilityHidden(true)
    }
}

// A true SwiftUI destination, not a sheet that may fail to mount after cold launch.
struct LibraryReadingScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme
    @Environment(\.dynamicTypeSize) private var textSize

    private var selected: Article? {
        guard let id = vm.libraryArticleId else { return nil }
        return LexiconData.articles.first(where: { $0.id == id })
    }

    var body: some View {
        let p = lexiconPalette(scheme)
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                HStack {
                    Button {
                        vm.navigateBack()
                    } label: {
                        Label(selected == nil ? "Home" : "All articles",
                              systemImage: "chevron.left")
                            .font(.lexiconBody(13))
                            .frame(minHeight: 44)
                    }
                    Spacer()
                    Button("Close") { vm.closeLibrary() }
                        .font(.lexiconBody(13))
                        .frame(minHeight: 44)
                }
                .foregroundStyle(p.primary)
                .buttonStyle(.plain)

                if let article = selected {
                    articlePage(article, palette: p)
                } else {
                    indexPage(palette: p)
                }

                CountrysideQuoteBanner(
                    quote: "A collection of words is a collection of worlds.",
                    illustrationBase: "ill-banner-bridge"
                )
            }
            .frame(maxWidth: 720, alignment: .leading)
            .padding(.horizontal, 18)
            .padding(.top, 12)
            .padding(.bottom, 160)
            .frame(maxWidth: .infinity)
        }
        .background(p.background)
        .accessibilityIdentifier(selected == nil ? "lexora-library-index" : "lexora-library-article")
    }

    @ViewBuilder
    private func indexPage(palette p: LexiconPalette) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            BotanicalSprig(color: p.primary).frame(width: 32, height: 32)
            Text("From the Library")
                .font(.lexiconDisplay(30))
                .foregroundStyle(p.foreground)
            Text("Three quiet journeys through words, nature and time.")
                .font(.lexiconBody(15))
                .foregroundStyle(p.mutedForeground)
        }
        .padding(.bottom, 6)

        ForEach(LexiconData.articles) { article in
            Button { vm.openLibraryArticle(article.id) } label: {
                HStack(alignment: .top, spacing: 12) {
                    Image(article.thumbnail + (scheme == .dark ? "-dark" : ""))
                        .resizable()
                        .scaledToFit()
                        .frame(width: 90, height: 105)
                        .accessibilityHidden(true)
                    VStack(alignment: .leading, spacing: 7) {
                        Text(article.title)
                            .font(.lexiconDisplay(18))
                            .foregroundStyle(p.foreground)
                            .fixedSize(horizontal: false, vertical: true)
                        Text(article.excerpt)
                            .font(.lexiconBody(12))
                            .foregroundStyle(p.mutedForeground)
                            .fixedSize(horizontal: false, vertical: true)
                        Label("\(article.readMinutes) min read", systemImage: "clock")
                            .font(.lexiconBody(11))
                            .foregroundStyle(p.highlight)
                    }
                    Spacer(minLength: 0)
                    Image(systemName: "chevron.right")
                        .foregroundStyle(p.primary)
                }
                .padding(12)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(p.card)
                .clipShape(RoundedRectangle(cornerRadius: 16))
                .overlay(RoundedRectangle(cornerRadius: 16).stroke(p.border, lineWidth: 1))
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel("Read \(article.title)")
        }
    }

    @ViewBuilder
    private func articlePage(_ article: Article, palette p: LexiconPalette) -> some View {
        Text("A LETTER FROM THE LIBRARY")
            .font(.lexiconBody(11))
            .tracking(1.5)
            .foregroundStyle(p.highlight)
        Text(article.title)
            .font(.lexiconDisplay(30))
            .foregroundStyle(p.foreground)
            .fixedSize(horizontal: false, vertical: true)
        Label("\(article.readMinutes) min read", systemImage: "clock")
            .font(.lexiconBody(12))
            .foregroundStyle(p.mutedForeground)
        Image(article.thumbnail + (scheme == .dark ? "-dark" : ""))
            .resizable()
            .scaledToFit()
            .frame(maxWidth: .infinity)
            .frame(maxHeight: textSize.isAccessibilitySize ? 250 : 190)
            .accessibilityHidden(true)
        Text(article.excerpt)
            .font(.lexiconDisplay(18))
            .foregroundStyle(p.primary)
            .fixedSize(horizontal: false, vertical: true)
        VStack(alignment: .leading, spacing: 18) {
            ForEach(Array(LexiconData.articleReading(article.id).enumerated()), id: \.offset) { item in
                Text(item.element)
                    .font(.lexiconBody(16))
                    .foregroundStyle(p.foreground)
                    .fixedSize(horizontal: false, vertical: true)
                    .lineSpacing(7)
                    .textSelection(.enabled)
            }
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(p.card)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(p.border, lineWidth: 1))

        Button { vm.toggleArticleBookmark(article.id) } label: {
            Label(vm.bookmarkedArticleIds.contains(article.id) ? "Saved to your library" : "Save this story",
                  systemImage: vm.bookmarkedArticleIds.contains(article.id) ? "bookmark.fill" : "bookmark")
                .font(.lexiconBody(14))
                .foregroundStyle(p.primary)
                .frame(maxWidth: .infinity)
                .frame(minHeight: 50)
                .background(p.card)
                .clipShape(Capsule())
                .overlay(Capsule().stroke(p.border, lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}
