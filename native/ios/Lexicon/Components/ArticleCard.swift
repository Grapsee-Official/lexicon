//
//  ArticleCard.swift
//  Lexicon
//

import SwiftUI

struct ArticleCard: View {
    let article: Article
    @EnvironmentObject private var vm: LexiconViewModel
    var shelfWidth: CGFloat? = nil
    var shelfHeight: CGFloat? = nil
    var onOpen: () -> Void = {}
    @Environment(\.colorScheme) private var scheme
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    private var isDark: Bool { scheme == .dark }

    private var thumbName: String {
        let scene: String? = article.id == "a1" ? "home-library-origin" :
            (article.id == "a2" ? "home-library-lost" : (article.id == "a3" ? "home-library-nature" : nil))
        return countrysideImageName(article.thumbnail, isDark: isDark, scene: scene)
    }

    private var iconColor: Color {
        isDark ? LexiconColors.accentDark : LexiconColors.highlight
    }

    @ScaledMetric(relativeTo: .body) private var accessibleWidth: CGFloat = 210
    @ScaledMetric(relativeTo: .body) private var accessibleHeight: CGFloat = 132
    private var cardWidth: CGFloat { shelfWidth ?? (dynamicTypeSize.isAccessibilitySize ? min(310, accessibleWidth) : (isDark ? 150 : 174)) }
    private var cardHeight: CGFloat { shelfHeight ?? (dynamicTypeSize.isAccessibilitySize ? accessibleHeight : (isDark ? 78 : 90)) }
    private var titleSize: CGFloat { shelfWidth.map { min(14, max(10, $0 / (article.id == "a1" ? 240 : 180) * 14)) } ?? 10.5 }
    private var metaSize: CGFloat { shelfWidth.map { min(10, max(8, $0 / (article.id == "a1" ? 240 : 180) * 10)) } ?? 8 }
    private var imageFraction: CGFloat { isDark && shelfWidth != nil && article.id != "a1" ? 0.41 : 0.35 }
    private var corner: CGFloat { isDark ? 10 : 13 }

    var body: some View {
        Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            onOpen()
        } label: {
            HStack(spacing: 0) {
                if isDark {
                    Color.clear.frame(width: cardWidth * imageFraction)
                } else {
                    Image(thumbName)
                        .resizable()
                        .scaledToFill()
                        .frame(width: max(1, cardWidth * 0.33 - 10), height: max(1, cardHeight - 10))
                        .clipShape(RoundedRectangle(cornerRadius: 9, style: .continuous))
                        .padding(5)
                        .lexiconDecorative()
                }

                VStack(alignment: .leading, spacing: 3) {
                    Text(article.title)
                        .font(.lexiconDisplay(isDark && !dynamicTypeSize.isAccessibilitySize ? titleSize : 11.5, weight: .regular))
                        .foregroundStyle(lexiconPalette(scheme).foreground)
                        .lineLimit(dynamicTypeSize.isAccessibilitySize ? 5 : 3)
                        .multilineTextAlignment(.leading)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    Spacer(minLength: 0)

                    HStack {
                        HStack(spacing: 3) {
                            Image(systemName: "clock")
                                .font(.system(size: 9.5, weight: .regular))
                                .foregroundStyle(iconColor)
                            Text("\(article.readMinutes) min read")
                                .font(.lexiconBody(isDark && !dynamicTypeSize.isAccessibilitySize ? metaSize : 9.5))
                                .lineLimit(1)
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
                .frame(width: cardWidth * (isDark ? (1 - imageFraction) : 0.67), height: cardHeight, alignment: .topLeading)
            }
            .frame(width: cardWidth, height: cardHeight)
            .background(alignment: .leading) {
                if isDark {
                    let vector = article.id == "a1" ? "ref-home-library-origin" :
                        (article.id == "a2" ? "ref-home-library-lost" :
                         (article.id == "a3" ? "ref-home-library-nature" : nil))
                    Group {
                        if let vector = vector {
                            Image(vector).resizable().renderingMode(.original)
                        } else {
                            PreparedArtworkImage(name: thumbName, treatment: .story)
                        }
                    }
                    .scaledToFit()
                    .frame(width: cardWidth * imageFraction - 6, height: cardHeight - 6)
                    .clipShape(RoundedRectangle(cornerRadius: 6))
                    .padding(3)
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
