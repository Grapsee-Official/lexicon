//
//  ArticleCard.swift
//  Lexicon
//

import SwiftUI

struct ArticleCard: View {
    let article: Article
    @Environment(\.colorScheme) private var scheme

    private var isDark: Bool { scheme == .dark }

    private var thumbName: String {
        isDark ? "\(article.thumbnail)-dark" : article.thumbnail
    }

    private var iconColor: Color {
        isDark ? LexiconColors.accentDark : LexiconColors.highlight
    }

    var body: some View {
        Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
        } label: {
            HStack(spacing: 0) {
                Image(thumbName)
                    .resizable()
                    .scaledToFill()
                    .frame(width: 62, height: 78)
                    .clipShape(RoundedRectangle(cornerRadius: 9, style: .continuous))
                    .padding(5)
                    .lexiconDecorative()

                VStack(alignment: .leading, spacing: 3) {
                    Text(article.title)
                        .font(.lexiconDisplay(11.5, weight: .regular))
                        .foregroundStyle(lexiconPalette(scheme).foreground)
                        .lineLimit(3)
                        .multilineTextAlignment(.leading)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    Spacer(minLength: 0)

                    HStack {
                        HStack(spacing: 3) {
                            Image(systemName: "clock")
                                .font(.system(size: 9.5, weight: .regular))
                                .foregroundStyle(iconColor)
                            Text("\(article.readMinutes) min read")
                                .font(.lexiconBody(9))
                                .foregroundStyle(lexiconPalette(scheme).mutedForeground)
                        }

                        Spacer()

                        Image(systemName: article.bookmarked ? "bookmark.fill" : "bookmark")
                            .font(.system(size: 11, weight: .regular))
                            .foregroundStyle(iconColor)
                            .lexiconDecorative()
                    }
                }
                .padding(.leading, 5)
                .padding(.trailing, 10)
                .padding(.vertical, 8)
                .frame(width: 124, height: 88, alignment: .topLeading)
            }
            .frame(width: 196, height: 88)
            .background(lexiconPalette(scheme).card)
            .clipShape(RoundedRectangle(cornerRadius: 13, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 13, style: .continuous)
                    .stroke(lexiconPalette(scheme).border, lineWidth: 1)
            )
            .shadow(
                color: isDark ? .clear : LexiconColors.foreground.opacity(0.06),
                radius: 4,
                x: 0,
                y: 2
            )
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .combine)
        .accessibilityLabel(Text("Article. \(article.title). \(article.readMinutes) minute read\(article.bookmarked ? ". Bookmarked" : "")"))
        .accessibilityHint(Text("Open the article"))
        .accessibilityAddTraits(.isButton)
    }
}
