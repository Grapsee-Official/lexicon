//
//  TrendingTagRow.swift
//  Lexicon
//
//  Translation of `src/components/lexicon/trending-tag-row.tsx`.
//
//  Horizontal scroll of pill chips + 5th grid button.
//

import SwiftUI

struct TrendingTagRow: View {
    let tags: [TrendingTag]
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(tags) { tag in
                    Button {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        if let entry = LexiconData.getWordById(tag.wordId) {
                            vm.openWord(entry)
                        }
                    } label: {
                        Text(tag.label)
                            .font(.lexiconBody(11))
                            .foregroundStyle(lexiconPalette(scheme).foreground.opacity(0.7))
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(lexiconPalette(scheme).card)
                            .clipShape(Capsule())
                            .overlay(Capsule().stroke(lexiconPalette(scheme).border, lineWidth: 1))
                            // Pills are ~26pt tall visually — expand the
                            // tappable frame to ≥ 44pt. The pill background
                            // stays small; only the hit area grows.
                            .lexiconMinTouchTarget()
                            .contentShape(Capsule())
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(Text(tag.label))
                    .accessibilityHint(Text("Open the dictionary entry"))
                    .accessibilityAddTraits(.isButton)
                }

                // 5th grid button — spec label "More search options".
                Button {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                } label: {
                    Image(systemName: "square.grid.2x2")
                        .font(.system(size: 12, weight: .regular))
                        .foregroundStyle(lexiconPalette(scheme).foreground.opacity(0.5))
                        .frame(width: 28, height: 28)
                        .background(lexiconPalette(scheme).card)
                        .clipShape(Circle())
                        .overlay(Circle().stroke(lexiconPalette(scheme).border, lineWidth: 1))
                        .lexiconMinTouchTarget()
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text(LexiconAccessibilityLabels.grid))
                .accessibilityAddTraits(.isButton)
            }
            .padding(.horizontal, 1)
        }
    }
}

#Preview {
    TrendingTagRow(tags: LexiconData.trendingTags)
        .environmentObject(LexiconViewModel())
        .padding()
        .background(LexiconColors.background)
}
