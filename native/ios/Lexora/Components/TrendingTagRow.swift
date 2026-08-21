//
//  TrendingTagRow.swift
//  Lexora
//
//  Translation of `src/components/lexora/trending-tag-row.tsx`.
//
//  Horizontal scroll of pill chips + 5th grid button.
//

import SwiftUI

struct TrendingTagRow: View {
    let tags: [TrendingTag]
    @EnvironmentObject private var vm: LexoraViewModel
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach(tags) { tag in
                    Button {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        if let entry = LexoraData.getWordById(tag.wordId) {
                            vm.openWord(entry)
                        }
                    } label: {
                        Text(tag.label)
                            .font(.lexoraBody(11))
                            .foregroundStyle(lexoraPalette(scheme).foreground.opacity(0.7))
                            .padding(.horizontal, 12)
                            .padding(.vertical, 6)
                            .background(lexoraPalette(scheme).card)
                            .clipShape(Capsule())
                            .overlay(Capsule().stroke(lexoraPalette(scheme).border, lineWidth: 1))
                    }
                    .buttonStyle(.plain)
                }

                // 5th grid button
                Button {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                } label: {
                    Image(systemName: "square.grid.2x2")
                        .font(.system(size: 12, weight: .regular))
                        .foregroundStyle(lexoraPalette(scheme).foreground.opacity(0.5))
                        .frame(width: 28, height: 28)
                        .background(lexoraPalette(scheme).card)
                        .clipShape(Circle())
                        .overlay(Circle().stroke(lexoraPalette(scheme).border, lineWidth: 1))
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text("More tags"))
            }
            .padding(.horizontal, 1)
        }
    }
}

#Preview {
    TrendingTagRow(tags: LexoraData.trendingTags)
        .environmentObject(LexoraViewModel())
        .padding()
        .background(LexoraColors.background)
}
