//
//  TrendingTagRow.swift
//  Lexicon
//

import SwiftUI

struct TrendingTagRow: View {
    let tags: [TrendingTag]
    var sceneIntegrated: Bool = false
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    private var isDark: Bool { scheme == .dark }
    private var compact: Bool { isDark && sceneIntegrated && !dynamicTypeSize.isAccessibilitySize }

    private var chipBg: Color {
        isDark && sceneIntegrated ? Color(hex: "0B151D").opacity(0.88)
            : (isDark ? LexiconColors.cardDark : LexiconColors.card.opacity(0.78))
    }

    private var chipBorder: Color {
        isDark && sceneIntegrated ? Color(hex: "B6A486").opacity(0.12)
            : (isDark ? LexiconColors.borderDark : LexiconColors.foreground.opacity(0.12))
    }

    private var chipText: Color {
        isDark && sceneIntegrated ? Color(hex: "D8C7A8")
            : (isDark ? LexiconColors.foregroundDark.opacity(0.88) : LexiconColors.foreground.opacity(0.84))
    }

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: compact ? 6 : 9) {
                ForEach(tags) { tag in
                    Button {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        if let entry = LexiconData.getWordById(tag.wordId) {
                            vm.openWord(entry)
                        }
                    } label: {
                        Text(tag.label)
                            .font(.lexiconBody(compact ? 10.5 : 12))
                            .foregroundStyle(chipText)
                            .padding(.horizontal, compact ? 10 : 12)
                            .padding(.vertical, compact ? 6 : 8.5)
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
                        .foregroundStyle(isDark && sceneIntegrated ? Color(hex: "D0AD75") : (isDark ? LexiconColors.accentDark : LexiconColors.foreground.opacity(0.68)))
                        .frame(width: compact ? 30 : 34, height: compact ? 28 : 34)
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
