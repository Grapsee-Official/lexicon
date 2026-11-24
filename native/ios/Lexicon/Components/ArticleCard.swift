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
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    private var isDark: Bool { scheme == .dark }

    private var thumbName: String {
        isDark ? "\(article.thumbnail)-dark" : article.thumbnail
    }

    private var iconColor: Color {
        isDark ? LexiconColors.accentDark : LexiconColors.highlight
    }

    private var cardWidth: CGFloat { isDark ? (dynamicTypeSize.isAccessibilitySize ? 210 : 150) : 174 }
    private var cardHeight: CGFloat { isDark ? (dynamicTypeSize.isAccessibilitySize ? 120 : 78) : 90 }
    private var corner: CGFloat { isDark ? 10 : 13 }

    var body: some View {
        Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            onOpen()
        } label: {
            HStack(spacing: 0) {
                if isDark {
                    Color.clear.frame(width: cardWidth * 0.34)
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
                        .font(.lexiconDisplay(isDark && !dynamicTypeSize.isAccessibilitySize ? 10.5 : 11.5, weight: .regular))
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
                                .font(.lexiconBody(isDark && !dynamicTypeSize.isAccessibilitySize ? 8 : 9.5))
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
                .padding(.trailing, isDark ? 7 : 10)
                .padding(.vertical, 8)
                .frame(width: isDark ? cardWidth * 0.66 : 110, height: cardHeight, alignment: .topLeading)
            }
            .frame(width: cardWidth, height: cardHeight)
            .background(alignment: .leading) {
                if isDark {
                    PreparedArtworkImage(name: thumbName, treatment: .story)
                        .scaledToFill()
                        .frame(width: cardWidth * 0.34, height: cardHeight)
                        .clipped()
                        .mask {
                            LinearGradient(stops: [
                                .init(color: .black.opacity(0.88), location: 0),
                                .init(color: .black, location: 0.14),
                                .init(color: .black, location: 0.72),
                                .init(color: .black.opacity(0.48), location: 0.90),
                                .init(color: .clear, location: 1)
                            ], startPoint: .leading, endPoint: .trailing)
                        }
                        .mask {
                            LinearGradient(stops: [
                                .init(color: .black.opacity(0.72), location: 0),
                                .init(color: .black, location: 0.15),
                                .init(color: .black, location: 0.83),
                                .init(color: .black.opacity(0.65), location: 1)
                            ], startPoint: .top, endPoint: .bottom)
                        }
                        .allowsHitTesting(false)
                        .accessibilityHidden(true)
                }
            }
            .background(isDark ? Color(hex: "0A131B") : lexiconPalette(scheme).card)
            .clipShape(RoundedRectangle(cornerRadius: corner, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: corner, style: .continuous)
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
