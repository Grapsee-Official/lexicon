//
//  ArticleCard.swift
//  Lexicon
//

import SwiftUI

struct ArticleCard: View {
    let article: Article
    @EnvironmentObject private var vm: LexiconViewModel
    var onOpen: () -> Void = {}
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
            onOpen()
        } label: {
            HStack(spacing: 0) {
                if isDark {
                    Image(thumbName)
                        .resizable()
                        .scaledToFill()
                        .frame(width: 60, height: 90)
                        .clipped()
                        .mask {
                            LinearGradient(stops: [
                                .init(color: .black.opacity(0.94), location: 0),
                                .init(color: .black, location: 0.64),
                                .init(color: .clear, location: 1)
                            ], startPoint: .leading, endPoint: .trailing)
                        }
                        .lexiconDecorative()
                } else {
                    Image(thumbName)
                        .resizable()
                        .scaledToFill()
                        .frame(width: 54, height: 80)
                        .clipShape(RoundedRectangle(cornerRadius: 9, style: .continuous))
                        .padding(5)
                        .lexiconDecorative()
                }

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
                                .font(.lexiconBody(9.5))
                                .foregroundStyle(lexiconPalette(scheme).mutedForeground)
                        }

                        Spacer()

                        Image(systemName: vm.bookmarkedArticleIds.contains(article.id) ? "bookmark.fill" : "bookmark")
                            .font(.system(size: 11, weight: .regular))
                            .foregroundStyle(iconColor)
                            .lexiconDecorative()
                    }
                }
                .padding(.leading, 5)
                .padding(.trailing, 10)
                .padding(.vertical, 8)
                .frame(width: isDark ? 114 : 110, height: 90, alignment: .topLeading)
            }
            .frame(width: 174, height: 90)
            .background(isDark ? Color(hex: "0B141C") : lexiconPalette(scheme).card)
            .clipShape(RoundedRectangle(cornerRadius: 13, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 13, style: .continuous)
                    .stroke(isDark ? Color(hex: "B6A486").opacity(0.14) : lexiconPalette(scheme).border, lineWidth: 1)
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
        .accessibilityLabel(Text("Article. \(article.title). \(article.readMinutes) minute read\(vm.bookmarkedArticleIds.contains(article.id) ? ". Bookmarked" : "")"))
        .accessibilityHint(Text("Open the article"))
        .accessibilityAddTraits(.isButton)
    }
}
