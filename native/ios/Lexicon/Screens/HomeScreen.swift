//
//  HomeScreen.swift
//  Lexicon
//

import SwiftUI

struct HomeScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        ScrollView {
            VStack(spacing: 8) {
                Hero()

                TrendingTagRow(tags: LexiconData.trendingTags)
                    .padding(.horizontal, 16)

                WordOfTheDayCard(word: LexiconData.wordOfTheDay)
                    .padding(.horizontal, 16)

                // All 4 Category cards in an equal-width 4-column HStack — zero clipping on "Science"!
                VStack(spacing: 6) {
                    SectionHeader(
                        title: "Explore by Category",
                        actionLabel: "View all",
                        divider: false,
                        onAction: { vm.setActiveTab(.dictionary) }
                    )
                    HStack(spacing: 8) {
                        ForEach(LexiconData.categories) { c in
                            CategoryCard(category: c)
                        }
                    }
                    .padding(.horizontal, 16)
                }

                // From the Library (with horizontal hairline divider)
                VStack(spacing: 6) {
                    SectionHeader(
                        title: "From the Library",
                        actionLabel: "View all articles",
                        divider: true
                    )
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 9) {
                            ForEach(LexiconData.articles) { a in
                                ArticleCard(article: a)
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
    }
}
