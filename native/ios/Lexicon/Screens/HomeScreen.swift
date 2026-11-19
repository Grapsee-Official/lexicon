//
//  HomeScreen.swift
//  Lexicon
//

import SwiftUI

struct HomeScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @State private var libraryOpen = false
    @State private var articleToRead: Article? = nil

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
                Hero()

                TrendingTagRow(tags: LexiconData.trendingTags)
                    .padding(.horizontal, 16)

                WordOfTheDayCard(word: displayedHomeWord)
                    .padding(.horizontal, 16)

                // All 4 Category cards in an equal-width 4-column HStack — zero clipping on "Science"!
                VStack(spacing: 10) {
                    SectionHeader(
                        title: "Explore by Category",
                        actionLabel: "View all",
                        divider: false,
                        onAction: { vm.setActiveTab(.dictionary) }
                    )
                    // Four illustrated bookplates on a standard phone; on
                    // narrow devices retain their readable size and scroll.
                    GeometryReader { layout in
                        let available = layout.size.width
                        let bookplateWidth = max(78, min(108, (available - 32 - 30) / 4))
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 10) {
                                ForEach(LexiconData.categories) { c in
                                    CategoryCard(category: c)
                                        .frame(width: bookplateWidth)
                                }
                            }
                            .padding(.horizontal, 16)
                        }
                    }
                    .frame(height: dynamicTypeSize.isAccessibilitySize ? 144 : 105)
                }

                // From the Library (with horizontal hairline divider)
                VStack(spacing: 10) {
                    SectionHeader(
                        title: "From the Library",
                        actionLabel: "View all articles",
                        divider: true,
                        onAction: { articleToRead = nil; libraryOpen = true }
                    )
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 10) {
                            ForEach(LexiconData.articles) { a in
                                ArticleCard(
                                    article: a,
                                    onOpen: { articleToRead = a; libraryOpen = true }
                                )
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                }
            }
            .padding(.top, 1)
            .padding(.bottom, 76)
        }
        .background(lexiconPalette(scheme).background)
        .onAppear {
            // Only native debug screenshot sessions request an initial sheet.
            #if DEBUG
            let args = ProcessInfo.processInfo.arguments
            if args.contains("-lexoraVisualQa"),
               let index = args.firstIndex(of: "-lexiconScreen"),
               index + 1 < args.count {
                let screen = args[index + 1]
                if screen == "library" || screen == "library_article" {
                    articleToRead = screen == "library_article"
                        ? LexiconData.articles.first : nil
                    libraryOpen = true
                }
            }
            #endif
        }
        .sheet(isPresented: $libraryOpen) {
            NavigationStack {
                ScrollView {
                    VStack(alignment: .leading, spacing: 15) {
                        if let article = articleToRead {
                            Image(article.thumbnail + (scheme == .dark ? "-dark" : ""))
                                .resizable()
                                .scaledToFit()
                                .frame(maxWidth: .infinity)
                                .frame(height: 175)
                                .clipped()
                                .accessibilityHidden(true)

                            Text(article.excerpt)
                                .font(.lexiconBody(15))
                                .foregroundStyle(lexiconPalette(scheme).mutedForeground)

                            ForEach(Array(LexiconData.articleReading(article.id).enumerated()), id: \.offset) { item in
                                Text(item.element)
                                    .font(.lexiconBody(15))
                                    .foregroundStyle(lexiconPalette(scheme).foreground)
                                    .fixedSize(horizontal: false, vertical: true)
                                    .lineSpacing(6)
                            }
                        } else {
                            Text("Three little journeys through language and time.")
                                .font(.lexiconBody(14))
                                .foregroundStyle(lexiconPalette(scheme).mutedForeground)

                            ForEach(LexiconData.articles) { article in
                                Button { articleToRead = article } label: {
                                    HStack(spacing: 13) {
                                        Image(article.thumbnail + (scheme == .dark ? "-dark" : ""))
                                            .resizable()
                                            .scaledToFill()
                                            .frame(width: 80, height: 80)
                                            .clipped()
                                            .clipShape(RoundedRectangle(cornerRadius: 12))
                                            .accessibilityHidden(true)
                                        VStack(alignment: .leading, spacing: 5) {
                                            Text(article.title)
                                                .font(.lexiconDisplay(17))
                                                .foregroundStyle(lexiconPalette(scheme).foreground)
                                            Text(article.excerpt)
                                                .font(.lexiconBody(12))
                                                .foregroundStyle(lexiconPalette(scheme).mutedForeground)
                                        }
                                        Spacer(minLength: 0)
                                        Image(systemName: "chevron.right")
                                            .foregroundStyle(lexiconPalette(scheme).highlight)
                                    }
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(20)
                }
                .background(lexiconPalette(scheme).background)
                .navigationTitle(articleToRead?.title ?? "From the Library")
                .navigationBarTitleDisplayMode(.inline)
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        if articleToRead != nil {
                            Button("All articles") { articleToRead = nil }
                        }
                    }
                    ToolbarItem(placement: .confirmationAction) {
                        Button("Done") { libraryOpen = false }
                    }
                    ToolbarItem(placement: .bottomBar) {
                        if let article = articleToRead {
                            Button { vm.toggleArticleBookmark(article.id) } label: {
                                Label(
                                    vm.bookmarkedArticleIds.contains(article.id) ? "Saved" : "Save story",
                                    systemImage: vm.bookmarkedArticleIds.contains(article.id)
                                        ? "bookmark.fill" : "bookmark"
                                )
                            }
                        }
                    }
                }
            }
            .presentationDragIndicator(.visible)
        }
    }
}
