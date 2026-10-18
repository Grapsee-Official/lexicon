//
//  ArticleCard.swift
//  Lexicon
//
//  Translation of `src/components/lexicon/article-card.tsx`.
//
//  HORIZONTAL layout (HStack). Image left 35% height full. Text right 65%
//  VStack justify-between. Fixed height 120pt. Width 224pt.
//  Title line-clamp-3, read time + bookmark at bottom.
//

import SwiftUI

struct ArticleCard: View {
    let article: Article
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
        } label: {
            HStack(spacing: 0) {
                // LEFT 35% — image fills the full height.
                Image(article.thumbnail)
                    .resizable()
                    .scaledToFill()
                    .frame(width: 78, height: 120)
                    .clipped()
                    .lexiconDecorative()

                // RIGHT 65% — title top, read time + bookmark bottom.
                VStack(spacing: 8) {
                    Text(article.title)
                        .font(.lexiconDisplay(12, weight: .semibold))
                        .foregroundStyle(lexiconPalette(scheme).foreground)
                        .lineLimit(3)
                        .multilineTextAlignment(.leading)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    Spacer(minLength: 0)

                    HStack {
                        Label("\(article.readMinutes) min read", systemImage: "clock")
                            .font(.lexiconBody(9))
                            .foregroundStyle(lexiconPalette(scheme).mutedForeground)
                            .labelStyle(.titleAndIcon)

                        Spacer()

                        Image(systemName: article.bookmarked ? "bookmark.fill" : "bookmark")
                            .font(.system(size: 11, weight: .regular))
                            .foregroundStyle(lexiconPalette(scheme).mutedForeground)
                            .lexiconDecorative()
                    }
                }
                .padding(12)
                .frame(width: 146, height: 120, alignment: .top)
            }
            .frame(width: 224, height: 120)
            .background(lexiconPalette(scheme).card)
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .shadow(color: Color.black.opacity(0.06), radius: 6, x: 0, y: 2)
        }
        .buttonStyle(.plain)
        // P4-05 — combine the card into ONE VoiceOver element so the
        // screen-reader user hears the whole article summary in one
        // swipe: "Article: {title}, {N} min read, button".
        .accessibilityElement(children: .combine)
        .accessibilityLabel(Text("Article. \(article.title). \(article.readMinutes) minute read\(article.bookmarked ? ". Bookmarked" : "")"))
        .accessibilityHint(Text("Open the article"))
        .accessibilityAddTraits(.isButton)
        // Card is 120pt tall × 224pt wide — well above the 44pt minimum.
    }
}

#Preview {
    ArticleCard(article: LexiconData.articles[0])
        .padding()
        .background(LexiconColors.background)
}
