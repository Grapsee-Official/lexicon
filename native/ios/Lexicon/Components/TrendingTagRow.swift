//
//  TrendingTagRow.swift
//  Lexicon
//

import SwiftUI

struct TrendingTagRow: View {
    let tags: [TrendingTag]
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme

    private var isDark: Bool { scheme == .dark }

    private var chipBg: Color {
        isDark ? LexiconColors.cardDark : LexiconColors.card.opacity(0.78)
    }

    private var chipBorder: Color {
        isDark ? LexiconColors.borderDark : LexiconColors.foreground.opacity(0.12)
    }

    private var chipText: Color {
        isDark ? LexiconColors.foregroundDark.opacity(0.88) : LexiconColors.foreground.opacity(0.84)
    }

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 7) {
                ForEach(tags) { tag in
                    Button {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        if let entry = LexiconData.getWordById(tag.wordId) {
                            vm.openWord(entry)
                        }
                    } label: {
                        Text(tag.label)
                            .font(.lexiconBody(11.5))
                            .foregroundStyle(chipText)
                            .padding(.horizontal, 13)
                            .padding(.vertical, 5.5)
                            .background(chipBg)
                            .clipShape(Capsule())
                            .overlay(Capsule().stroke(chipBorder, lineWidth: 1))
                            .contentShape(Capsule())
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(Text(tag.label))
                    .accessibilityHint(Text("Open the dictionary entry"))
                    .accessibilityAddTraits(.isButton)
                }

                Button {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    vm.setActiveTab(.dictionary)
                } label: {
                    Image(systemName: "square.grid.2x2")
                        .font(.system(size: 12, weight: .regular))
                        .foregroundStyle(isDark ? LexiconColors.accentDark : LexiconColors.foreground.opacity(0.68))
                        .frame(width: 32, height: 28)
                        .background(chipBg)
                        .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 10, style: .continuous)
                                .stroke(chipBorder, lineWidth: 1)
                        )
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text(LexiconAccessibilityLabels.grid))
                .accessibilityAddTraits(.isButton)
            }
            .padding(.horizontal, 1)
        }
    }
}
