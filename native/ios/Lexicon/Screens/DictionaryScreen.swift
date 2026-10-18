//
//  DictionaryScreen.swift
//  Lexicon
//
//  Translation of `src/components/lexicon/screens/dictionary-screen.tsx`.
//
//  Alphabetical browse with:
//    • sticky A–Z quick index (horizontal ScrollView of letter circles)
//    • active letter tracked via GeometryReader + PreferenceKey as you scroll
//    • large serif section headers (brand green) with a count + horizontal divider line
//    • grouped card (card surface, border, shadow, rounded corners) of word rows
//    • each row: word title (serif) · part of speech (italic, muted, lowercase)
//                · definition (1 line, Lora) · volume icon on right
//    • tap row → vm.openWord(entry)
//

import SwiftUI

struct DictionaryScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme

    private let sections: [(letter: String, entries: [WordEntry])] = LexiconData.getAlphabetSections()
    @State private var activeLetterIndex: Int = 0

    var body: some View {
        ScrollViewReader { proxy in
            VStack(spacing: 0) {
                alphabetIndex(proxy: proxy)

                ScrollView {
                    LazyVStack(alignment: .leading, spacing: 14) {
                        ForEach(Array(sections.enumerated()), id: \.element.letter) { idx, section in
                            DictionarySection(
                                section: section,
                                onOpen: { entry in vm.openWord(entry) }
                            )
                            .id(section.letter)
                            .background(
                                GeometryReader { geo in
                                    Color.clear.preference(
                                        key: SectionTopPreferenceKey.self,
                                        value: [section.letter: geo.frame(in: .named("dictScroll")).minY]
                                    )
                                }
                            )
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 12)
                    .padding(.bottom, 120) // clear floating bottom nav
                }
                .coordinateSpace(name: "dictScroll")
                .onPreferenceChange(SectionTopPreferenceKey.self) { values in
                    let threshold: CGFloat = 80
                    var bestIdx = activeLetterIndex
                    var bestY: CGFloat = -.infinity
                    for (i, s) in sections.enumerated() {
                        guard let y = values[s.letter] else { continue }
                        // The "active" section is the topmost one whose header has
                        // scrolled up to or past the threshold (y ≤ threshold),
                        // picking the largest such y (closest to the threshold line).
                        if y <= threshold && y > bestY {
                            bestY = y
                            bestIdx = i
                        }
                    }
                    if bestIdx != activeLetterIndex {
                        withAnimation(.easeOut(duration: 0.15)) {
                            activeLetterIndex = bestIdx
                        }
                    }
                }
            }
        }
        .background(LexiconColors.background)
    }

    // MARK: - A–Z quick index

    @ViewBuilder
    private func alphabetIndex(proxy: ScrollViewProxy) -> some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 6) {
                ForEach(Array(sections.enumerated()), id: \.element.letter) { idx, section in
                    let active = idx == activeLetterIndex
                    Button {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        withAnimation(.easeInOut(duration: 0.3)) {
                            proxy.scrollTo(section.letter, anchor: .top)
                        }
                    } label: {
                        Text(section.letter)
                            .font(.lexiconDisplay(11, weight: active ? .semibold : .regular))
                            .foregroundStyle(active
                                             ? lexiconPalette(scheme).primaryForeground
                                             : lexiconPalette(scheme).mutedForeground)
                            .frame(width: 26, height: 26)
                            .background(
                                Circle()
                                    .fill(active ? lexiconPalette(scheme).primary : Color.clear)
                            )
                            .overlay(
                                Circle()
                                    .stroke(active ? Color.clear : lexiconPalette(scheme).border, lineWidth: 1)
                            )
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(Text("Jump to letter \(section.letter)"))
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
        }
        .background(
            lexiconPalette(scheme).card
                .opacity(0.92)
                .overlay(alignment: .bottom) {
                    Rectangle()
                        .fill(lexiconPalette(scheme).border)
                        .frame(height: 1)
                }
                .ignoresSafeArea(edges: .horizontal)
        )
    }
}

// MARK: - Section card

private struct DictionarySection: View {
    let section: (letter: String, entries: [WordEntry])
    let onOpen: (WordEntry) -> Void
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            // Header: large serif letter + divider + count
            HStack(alignment: .firstTextBaseline, spacing: 8) {
                Text(section.letter)
                    .font(.lexiconDisplay(18, weight: .semibold))
                    .foregroundStyle(lexiconPalette(scheme).primary)
                    .fixedSize(horizontal: true, vertical: false)

                Rectangle()
                    .fill(lexiconPalette(scheme).border)
                    .frame(height: 1)

                Text("\(section.entries.count) \(section.entries.count == 1 ? "word" : "words")")
                    .font(.lexiconBody(10))
                    .foregroundStyle(lexiconPalette(scheme).mutedForeground)
                    .fixedSize(horizontal: true, vertical: false)
            }
            .padding(.horizontal, 4)

            // Grouped card with rows
            VStack(spacing: 0) {
                ForEach(Array(section.entries.enumerated()), id: \.element.id) { idx, entry in
                    DictionaryRow(
                        entry: entry,
                        isFirst: idx == 0,
                        onOpen: { onOpen(entry) }
                    )
                }
            }
            .background(lexiconPalette(scheme).card)
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 12, style: .continuous)
                    .stroke(lexiconPalette(scheme).border, lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.04), radius: 4, x: 0, y: 2)
        }
    }
}

// MARK: - Row

private struct DictionaryRow: View {
    let entry: WordEntry
    let isFirst: Bool
    let onOpen: () -> Void
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            onOpen()
        } label: {
            HStack(alignment: .firstTextBaseline, spacing: 10) {
                VStack(alignment: .leading, spacing: 2) {
                    HStack(alignment: .firstTextBaseline, spacing: 6) {
                        Text(entry.word)
                            .font(.lexiconDisplay(14, weight: .semibold))
                            .foregroundStyle(lexiconPalette(scheme).foreground)
                            .lineLimit(1)
                        Text(entry.partOfSpeech.displayValue)
                            .font(.lexiconBody(10).italic())
                            .foregroundStyle(lexiconPalette(scheme).mutedForeground)
                            .lineLimit(1)
                    }
                    Text(entry.definitions.first ?? "")
                        .font(.lexiconLong(12))
                        .foregroundStyle(lexiconPalette(scheme).foreground.opacity(0.7))
                        .lineLimit(1)
                }

                Spacer(minLength: 4)

                Image(systemName: "speaker.wave.2")
                    .font(.system(size: 12, weight: .regular))
                    .foregroundStyle(lexiconPalette(scheme).mutedForeground)
                    .accessibilityHidden(true)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .frame(maxWidth: .infinity, alignment: .leading)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .overlay(alignment: .top) {
            if !isFirst {
                Rectangle()
                    .fill(lexiconPalette(scheme).border)
                    .frame(height: 1)
                    .padding(.horizontal, 12)
            }
        }
    }
}

// MARK: - PreferenceKey for active-letter tracking

private struct SectionTopPreferenceKey: PreferenceKey {
    static var defaultValue: [String: CGFloat] = [:]

    static func reduce(value: inout [String: CGFloat], nextValue: () -> [String: CGFloat]) {
        // Always overwrite with the latest observed values.
        value.merge(nextValue()) { _, new in new }
    }
}

#Preview {
    DictionaryScreen()
        .environmentObject(LexiconViewModel())
}
