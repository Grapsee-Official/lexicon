//
//  HomeScreen.swift
//  Lexicon
//
//  Translation of the home tab composition in `src/app/page.tsx`.
//
//  ScrollView with all home components stacked: Hero → Search → Trending →
//  WOTD → Categories (h-scroll) → Articles (h-scroll). Bottom padding to
//  clear the floating BottomNav.
//

import SwiftUI

struct HomeScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                Hero()

                // FloatingSearchBar (inset like the web `px-4` wrapper).
                FloatingSearchBar()
                    .padding(.horizontal, 16)

                TrendingTagRow(tags: LexiconData.trendingTags)
                    .padding(.horizontal, 16)

                WordOfTheDayCard(word: LexiconData.wordOfTheDay)
                    .padding(.horizontal, 16)

                // ── Explore by Category ──
                VStack(spacing: 8) {
                    SectionHeader(title: "Explore by Category", actionLabel: "View all")
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(LexiconData.categories) { c in
                                CategoryCard(category: c)
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                }

                // ── From the Library ──
                VStack(spacing: 8) {
                    SectionHeader(title: "From the Library", actionLabel: "View all articles")
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            ForEach(LexiconData.articles) { a in
                                ArticleCard(article: a)
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                }
            }
            .padding(.top, 4)
            .padding(.bottom, 120) // clear floating bottom nav
        }
        .background(LexiconColors.background)
    }
}

#Preview {
    HomeScreen()
        .environmentObject(LexiconViewModel())
        .background(LexiconColors.background)
}
