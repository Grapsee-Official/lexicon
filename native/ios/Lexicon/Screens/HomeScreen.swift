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
        ScrollView {
            VStack(spacing: 11) {
                VStack(spacing: scheme == .dark ? 5 : 11) {
                    Hero(sharedNightScene: scheme == .dark)
                    TrendingTagRow(tags: LexiconData.trendingTags, sceneIntegrated: scheme == .dark)
                        .padding(.horizontal, 16)
                }
                .background {
                    if scheme == .dark {
                        HomeNightScene(extendsBehindTags: true)
                    }
                }

                WordOfTheDayCard(word: displayedHomeWord)
                    .padding(.horizontal, 16)

                // Ten original bookplates in the same canonical order as Android.
                VStack(spacing: 10) {
                    SectionHeader(
                        title: "Explore by Category",
                        actionLabel: "View all",
                        divider: false,
                        onAction: { vm.setActiveTab(.dictionary) }
                    )
                    GeometryReader { layout in
                        let available = layout.size.width
                        let largeText = dynamicTypeSize.isAccessibilitySize
                        let bookplateWidth = largeText
                            ? max(150, min(220, available * 0.43))
                            : max(scheme == .dark ? 78 : 94, min(116, (available - 62) / 4))
                        ScrollView(.horizontal, showsIndicators: false) {
                            LazyHStack(alignment: .top, spacing: 10) {
                                ForEach(LexiconData.categories) { category in
                                    CategoryCard(category: category)
                                        .frame(width: scheme == .dark && !largeText
                                               ? max(bookplateWidth, category.title.count > 15 ? 136
                                                     : (category.subtitle.count > 18 ? 108 : 0))
                                               : bookplateWidth)
                                }
                            }
                            .padding(.horizontal, 16)
                        }
                    }
                    .frame(height: dynamicTypeSize.isAccessibilitySize ? 230 : (scheme == .dark ? 114 : 145))
                }

                // From the Library (with horizontal hairline divider)
                VStack(spacing: 10) {
                    SectionHeader(
                        title: "From the Library",
                        actionLabel: "View all articles",
                        divider: true,
                        onAction: { vm.openLibrary() }
                    )
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 10) {
                            ForEach(LexiconData.articles) { a in
                                ArticleCard(
                                    article: a,
                                    onOpen: { vm.openLibraryArticle(a.id) }
                                )
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                }
            }
            .padding(.top, 1)
            .padding(.bottom, 160)
        }
        .background(scheme == .dark ? Color.clear : LexiconColors.background)
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
