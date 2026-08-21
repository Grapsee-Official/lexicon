//
//  WordDetailSheet.swift
//  Lexora
//
//  Translation of `src/components/lexora/word-detail-sheet.tsx`.
//
//  Bottom sheet showing the full dictionary entry for a word:
//    - Header: "LEXORA ENTRY" label (sparkles), word title (Fraunces ~30pt),
//      phonetic (italic) + part of speech (lowercase), 3 action buttons
//      (speaker.wave.2, bookmark fill/outline, share).
//    - Definitions: numbered list, Lora 15pt.
//    - Etymology: card with bg-card surface, border, rounded corners, Lora 13pt.
//    - Language origin: row with globe icon + "Origin:" label.
//    - Synonyms: pill tags (secondary-tinted).
//    - Antonyms: pill tags (red-tinted).
//    - Examples: blockquotes with left border (highlight color), Lora italic 14pt.
//
//  Presented via `.sheet(item: $vm.selectedWord) { word in WordDetailSheet(word: word) }`
//  in ContentView. Dismissal (swipe-down / programmatic) sets vm.selectedWord = nil,
//  which is exactly vm.closeWord().
//
//  Sheet height: 88% of viewport (matches the web `max-h-[88dvh]`).
//

import SwiftUI

struct WordDetailSheet: View {
    let word: WordEntry

    @EnvironmentObject private var vm: LexoraViewModel
    @Environment(\.colorScheme) private var scheme

    private var palette: LexoraPalette { lexoraPalette(scheme) }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                headerSection
                definitionsSection
                if !word.etymology.isEmpty { etymologySection }
                if !word.language.isEmpty { languageOriginSection }
                if !word.synonyms.isEmpty { synonymsSection }
                if !word.antonyms.isEmpty { antonymsSection }
                if !word.examples.isEmpty { examplesSection }
            }
            .padding(.horizontal, 20)
            .padding(.top, 8)
            .padding(.bottom, 44)
        }
        .background(palette.background.ignoresSafeArea())
        .presentationDetents([.fraction(0.88)])
        .presentationDragIndicator(.visible)
        .presentationBackground(palette.background)
    }

    // MARK: - Header

    private var headerSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(alignment: .top, spacing: 12) {
                // Left: label + title + phonetic/pos
                VStack(alignment: .leading, spacing: 6) {
                    // "LEXORA ENTRY" label with sparkles icon (highlight color)
                    HStack(spacing: 6) {
                        Image(systemName: "sparkles")
                            .font(.system(size: 11, weight: .regular))
                            .foregroundStyle(palette.highlight)
                        Text("LEXORA ENTRY")
                            .font(.lexoraBody(10, weight: .semibold))
                            .tracking(1.6)
                            .foregroundStyle(palette.highlight)
                    }

                    // Word title — large Fraunces serif ~30pt.
                    // Marked as the h1 heading so VoiceOver users land on
                    // the word title first when the sheet opens, and can
                    // use the Headings rotor to return to it.
                    Text(word.word)
                        .font(.lexoraDisplay(30, weight: .semibold))
                        .foregroundStyle(palette.foreground)
                        .lineSpacing(2)
                        .fixedSize(horizontal: false, vertical: true)
                        .lexoraAccessibilityHeading(.h1)

                    // Phonetic (italic muted) · part of speech (lowercase)
                    HStack(spacing: 6) {
                        Text(word.phonetic)
                            .font(.lexoraBody(13).italic())
                            .foregroundStyle(palette.mutedForeground)
                        Text("·")
                            .font(.lexoraBody(13))
                            .foregroundStyle(palette.foreground.opacity(0.4))
                        Text(word.partOfSpeech.displayValue)
                            .font(.lexoraBody(13))
                            .foregroundStyle(palette.foreground.opacity(0.6))
                    }
                }
                .frame(maxWidth: .infinity, alignment: .leading)

                // Right: 3 action buttons. Spec labels:
                //   speaker.wave.2  → "Pronounce word"
                //   bookmark        → "Save word" / "Remove from saved"
                //   share           → "Share word"
                HStack(spacing: 8) {
                    circleButton(
                        icon: "speaker.wave.2",
                        label: LexoraAccessibilityLabels.volume
                    ) {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    }

                    let saved = vm.isSaved(word.id)
                    circleButton(
                        icon: saved ? "bookmark.fill" : "bookmark",
                        label: saved
                            ? LexoraAccessibilityLabels.bookmarkRemove
                            : LexoraAccessibilityLabels.bookmarkSave,
                        saved: saved
                    ) {
                        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                        vm.toggleSave(word.id)
                    }

                    circleButton(
                        icon: "square.and.arrow.up",
                        label: LexoraAccessibilityLabels.share
                    ) {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    }
                }
            }
        }
    }

    /// Circular 40×40 VISUAL action button with card background + border.
    ///
    /// The visual circle is 40×40 (matches the locked web spec), but the
    /// `.lexoraMinTouchTarget()` modifier expands the tappable frame to
    /// ≥ 44×44 so the button meets the P4-05 hard gate. The 4pt expansion
    /// is transparent — it sits in the `HStack(spacing: 8)` padding
    /// between buttons, so the visual layout is unchanged.
    ///
    /// When `saved` is true, uses accent-tinted styling (matches web saved
    /// state). VoiceOver value "Saved" / "Not saved" is announced between
    /// the label and the button trait, so the user hears e.g.
    /// "Save word, Not saved, button" or "Remove from saved, Saved, button".
    private func circleButton(
        icon: String,
        label: String,
        saved: Bool = false,
        action: @escaping () -> Void
    ) -> some View {
        Button(action: action) {
            Image(systemName: icon)
                .font(.system(size: 16, weight: .regular))
                .foregroundStyle(saved ? palette.accent : palette.foreground.opacity(0.8))
                .frame(width: 40, height: 40)
                .background(saved ? palette.accent.opacity(0.15) : palette.card)
                .clipShape(Circle())
                .overlay(
                    Circle().stroke(
                        saved ? palette.accent.opacity(0.4) : palette.border,
                        lineWidth: 1
                    )
                )
                .lexoraMinTouchTarget()
        }
        .buttonStyle(.plain)
        .accessibilityLabel(Text(label))
        .accessibilityAddTraits(.isButton)
        .accessibilityValue(Text(saved ? "Saved" : "Not saved"))
    }

    // MARK: - Definitions (numbered list, Lora 15pt)

    private var definitionsSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            sectionHeader(
                icon: "book",
                title: word.definitions.count > 1 ? "Definitions" : "Definition"
            )
            VStack(alignment: .leading, spacing: 10) {
                ForEach(Array(word.definitions.enumerated()), id: \.offset) { idx, def in
                    HStack(alignment: .top, spacing: 12) {
                        Text("\(idx + 1).")
                            .font(.lexoraBody(12, weight: .medium))
                            .foregroundStyle(palette.highlight)
                            .frame(width: 16, alignment: .leading)
                        Text(def)
                            .font(.lexoraLong(15))
                            .foregroundStyle(palette.foreground.opacity(0.9))
                            .lineSpacing(3)
                            .fixedSize(horizontal: false, vertical: true)
                    }
                }
            }
        }
    }

    // MARK: - Etymology (card with bg-card, border, rounded corners, Lora 13pt)

    private var etymologySection: some View {
        VStack(alignment: .leading, spacing: 8) {
            sectionHeader(icon: "sparkles", title: "Etymology")
            Text(word.etymology)
                .font(.lexoraLong(13))
                .foregroundStyle(palette.foreground.opacity(0.8))
                .lineSpacing(3)
                .fixedSize(horizontal: false, vertical: true)
        }
        .padding(16)
        .background(palette.card)
        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 12, style: .continuous)
                .stroke(palette.border, lineWidth: 1)
        )
    }

    // MARK: - Language origin (globe icon + "Origin:" label)

    private var languageOriginSection: some View {
        HStack(spacing: 10) {
            Image(systemName: "globe")
                .font(.system(size: 14, weight: .regular))
                .foregroundStyle(palette.highlight)
            Text("Origin:")
                .font(.lexoraBody(13))
                .foregroundStyle(palette.mutedForeground)
            Text(word.language)
                .font(.lexoraBody(13))
                .foregroundStyle(palette.foreground.opacity(0.9))
                .lineLimit(2)
            Spacer(minLength: 0)
        }
    }

    // MARK: - Synonyms (pill tags, secondary-tinted)

    private var synonymsSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            sectionHeader(icon: "arrow.2.squarepath", title: "Synonyms")
            FlowLayout(spacing: 8) {
                ForEach(word.synonyms, id: \.self) { syn in
                    pill(text: syn, tint: palette.secondary, foregroundOpacity: 0.8)
                }
            }
        }
    }

    // MARK: - Antonyms (pill tags, red-tinted)

    private var antonymsSection: some View {
        VStack(alignment: .leading, spacing: 10) {
            sectionHeader(icon: "nosign", title: "Antonyms")
            FlowLayout(spacing: 8) {
                ForEach(word.antonyms, id: \.self) { ant in
                    pill(text: ant, tint: Color.red, foregroundOpacity: 0.7)
                }
            }
        }
    }

    // MARK: - Examples (blockquotes with left border, Lora italic 14pt)

    private var examplesSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            sectionHeader(icon: "quote.opening", title: "In Context")
            VStack(alignment: .leading, spacing: 10) {
                ForEach(Array(word.examples.enumerated()), id: \.offset) { _, ex in
                    blockquote(ex)
                }
            }
        }
    }

    /// Blockquote with a 2pt left border (highlight @ 60%) + Lora italic 14pt text.
    private func blockquote(_ text: String) -> some View {
        Text(text)
            .font(.lexoraLong(14).italic())
            .foregroundStyle(palette.foreground.opacity(0.8))
            .lineSpacing(3)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.leading, 12)
            .padding(.trailing, 8)
            .padding(.vertical, 4)
            .overlay(alignment: .leading) {
                Rectangle()
                    .fill(palette.highlight.opacity(0.6))
                    .frame(width: 2)
            }
            .fixedSize(horizontal: false, vertical: true)
    }

    // MARK: - Shared helpers

    /// Section header: thin SF Symbol + uppercase Inter label (muted foreground).
    /// Marked as an h3 heading + `.isHeader` trait so VoiceOver users can
    /// navigate via the Headings rotor (e.g. swipe to "Definitions",
    /// "Etymology", "Synonyms", "Antonyms", "In Context").
    private func sectionHeader(icon: String, title: String) -> some View {
        HStack(spacing: 6) {
            Image(systemName: icon)
                .font(.system(size: 11, weight: .regular))
                .foregroundStyle(palette.foreground.opacity(0.6))
            Text(title.uppercased())
                .font(.lexoraBody(11, weight: .semibold))
                .tracking(1.4)
                .foregroundStyle(palette.foreground.opacity(0.6))
        }
        .lexoraCombinedElement()
        .lexoraAccessibilityHeading(.h3)
    }

    /// Pill tag: capsule shape with tinted background + border.
    private func pill(text: String, tint: Color, foregroundOpacity: Double) -> some View {
        Text(text)
            .font(.lexoraBody(12))
            .foregroundStyle(palette.foreground.opacity(foregroundOpacity))
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(tint.opacity(0.15))
            .clipShape(Capsule())
            .overlay(Capsule().stroke(tint.opacity(0.3), lineWidth: 1))
    }
}

// MARK: - FlowLayout (wrapping pill container — iOS 16+ Layout protocol)

/// Simple flow layout that wraps subviews to the next line when they exceed
/// the available width. Mirrors CSS `flex-wrap: wrap` behaviour.
/// Used for synonym / antonym / suggestion pill tags.
private struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let maxWidth = proposal.width ?? .infinity
        var totalHeight: CGFloat = 0
        var totalWidth: CGFloat = 0
        var lineWidth: CGFloat = 0
        var lineHeight: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if lineWidth + size.width > maxWidth, lineWidth > 0 {
                totalWidth = max(totalWidth, lineWidth)
                totalHeight += lineHeight + spacing
                lineWidth = size.width
                lineHeight = size.height
            } else {
                if lineWidth > 0 { lineWidth += spacing }
                lineWidth += size.width
                lineHeight = max(lineHeight, size.height)
            }
        }
        totalWidth = max(totalWidth, lineWidth)
        totalHeight += lineHeight
        return CGSize(width: totalWidth, height: totalHeight)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let maxWidth = bounds.width
        var x: CGFloat = bounds.minX
        var y: CGFloat = bounds.minY
        var lineHeight: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > bounds.minX + maxWidth, x > bounds.minX {
                x = bounds.minX
                y += lineHeight + spacing
                lineHeight = 0
            }
            subview.place(at: CGPoint(x: x, y: y), anchor: .topLeading, proposal: ProposedViewSize(size))
            x += size.width + spacing
            lineHeight = max(lineHeight, size.height)
        }
    }
}

#Preview {
    WordDetailSheet(word: LexoraData.wordOfTheDay)
        .environmentObject(LexoraViewModel())
}
