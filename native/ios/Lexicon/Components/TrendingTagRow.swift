//
//  TrendingTagRow.swift
//  Lexicon
//

import SwiftUI

struct TrendingTagRow: View {
    let tags: [TrendingTag]
    var sceneIntegrated: Bool = false
    var referencePageWidth: CGFloat? = nil
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
        let reference = isDark && sceneIntegrated ? referencePageWidth : nil
        let gap = reference.map { min(10, max(5, $0 * 9 / 591)) } ?? (compact ? 6 : 9)
        let gridWidth = reference.map { min(54, max(32, $0 * 50 / 591)) } ?? 34
        let chipHeight = reference.map { min(44, max(26, $0 * 40 / 591)) } ?? 34
        let sizes: [String: CGFloat] = ["Ephemeral": 105, "Serendipity": 106, "Limerence": 102, "Ethereal": 92]
        let fixedReference = reference != nil && dynamicTypeSize <= .large && tags.allSatisfy { sizes[$0.label] != nil }
        let rowWidth = fixedReference ? reference.map { $0 * 492 / 591 } : nil
        let tagFont = reference.map { min(13, max(9.5, $0 * 13 / 591)) } ?? 10.5
        HStack(spacing: gap) {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: gap) {
                    ForEach(tags) { tag in
                        Button {
                            UIImpactFeedbackGenerator(style: .light).impactOccurred()
                            if let entry = LexiconData.getWordById(tag.wordId) { vm.openWord(entry) }
                        } label: {
                            Text(tag.label)
                                .font(.lexiconBody(compact ? tagFont : 12))
                                .foregroundStyle(chipText)
                                .fixedSize(horizontal: true, vertical: false)
                                .padding(.horizontal, compact ? 5 : 12)
                                .padding(.vertical, compact ? 3 : 8.5)
                                .frame(minWidth: fixedReference ? (reference ?? 0) * (sizes[tag.label] ?? 100) / 591 : 0,
                                       minHeight: chipHeight)
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
                }
            }

            Button {
                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                vm.setActiveTab(.dictionary)
            } label: {
                Image(systemName: "square.grid.2x2")
                    .font(.system(size: 12, weight: .regular))
                    .foregroundStyle(isDark && sceneIntegrated ? Color(hex: "D0AD75") : (isDark ? LexiconColors.accentDark : LexiconColors.foreground.opacity(0.68)))
                    .frame(width: gridWidth, height: dynamicTypeSize.isAccessibilitySize ? max(44, chipHeight) : chipHeight)
                    .background(chipBg)
                    .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
                    .overlay(RoundedRectangle(cornerRadius: 10, style: .continuous).stroke(chipBorder, lineWidth: 1))
            }
            .buttonStyle(.plain)
            .accessibilityLabel(Text(LexiconAccessibilityLabels.grid))
            .accessibilityAddTraits(.isButton)
        }
        .frame(width: rowWidth)
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}
