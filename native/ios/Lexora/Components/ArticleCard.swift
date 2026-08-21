//
//  ArticleCard.swift
//  Lexora
//
//  Translation of `src/components/lexora/article-card.tsx`.
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
                    .accessibilityHidden(true)

                // RIGHT 65% — title top, read time + bookmark bottom.
                VStack(spacing: 8) {
                    Text(article.title)
                        .font(.lexoraDisplay(12, weight: .semibold))
                        .foregroundStyle(lexoraPalette(scheme).foreground)
                        .lineLimit(3)
                        .multilineTextAlignment(.leading)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    Spacer(minLength: 0)

                    HStack {
                        Label("\(article.readMinutes) min read", systemImage: "clock")
                            .font(.lexoraBody(9))
                            .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                            .labelStyle(.titleAndIcon)

                        Spacer()

                        Image(systemName: article.bookmarked ? "bookmark.fill" : "bookmark")
                            .font(.system(size: 11, weight: .regular))
                            .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                    }
                }
                .padding(12)
                .frame(width: 146, height: 120, alignment: .top)
            }
            .frame(width: 224, height: 120)
            .background(lexoraPalette(scheme).card)
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .shadow(color: Color.black.opacity(0.06), radius: 6, x: 0, y: 2)
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    ArticleCard(article: LexoraData.articles[0])
        .padding()
        .background(LexoraColors.background)
}
