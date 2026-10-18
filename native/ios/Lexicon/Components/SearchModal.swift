//
//  SearchModal.swift
//  Lexicon
//
//  Translation of `src/components/lexicon/search-modal.tsx`.
//
//  Full-screen search overlay with:
//    - Animated entry (opacity + move from top edge).
//    - Top bar: close button (xmark), capsule search TextField (bg-card, border),
//      mic + camera icons.
//    - TextField auto-focuses on appear via @FocusState (250ms delay, matching web).
//    - Live search: filters dictionary via LexiconData.searchWords(query, limit: 8).
//    - Empty state (no query): recent searches list (clock icon + arrow) with
//      "Clear" button, and suggested pills (Petrichor, Saudade, Komorebi,
//      Mellifluous, Halcyon, Numinous).
//    - Results: list of words (title serif, part of speech italic, definition 1-line).
//    - No results: empty state with magnifyingglass icon + message.
//
//  Self-contained overlay — reads vm.searchOpen and conditionally renders.
//  Place at the top of the root ZStack so it covers all other content:
//
//    ZStack {
//        ContentView()
//        SearchModal()           // reads vm.searchOpen, animates in/out
//            .ignoresSafeArea(.keyboard)
//    }
//
//  Or present via `.fullScreenCover(isPresented: $vm.searchOpen) { SearchModal() }`
//  (the default full-screen-cover transition will be used in that case).
//
//  Tapping a result: vm.addRecentSearch(word.word) → vm.closeSearch() →
//  (200ms delay) → vm.openWord(word). Matches the web selectWord() flow.
//

import SwiftUI

struct SearchModal: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme

    @State private var query: String = ""
    @FocusState private var isFocused: Bool

    // P4-05 §7 — VoiceOver focus management.
    // Bound to the results list via `.lexiconAccessibilityFocus(...)`. When
    // the user submits a query, focus moves from the text field to the
    // results list so the screen-reader user hears the first result
    // without manually swiping. Focus is NOT moved while the user types
    // (that would yank focus away from the keyboard).
    @AccessibilityFocusState private var voFocus: LexiconVoiceOverFocus?

    private var palette: LexiconPalette { lexiconPalette(scheme) }

    private let suggestions: [String] = [
        "Petrichor", "Saudade", "Komorebi",
        "Mellifluous", "Halcyon", "Numinous",
    ]

    private var trimmedQuery: String {
        query.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    private var results: [WordEntry] {
        LexiconData.searchWords(query, limit: 8)
    }

    var body: some View {
        ZStack {
            if vm.searchOpen {
                overlayContent
                    .transition(.move(edge: .top).combined(with: .opacity))
            }
        }
        // P4-05 §6 — respect Reduce Motion. When the user has enabled
        // Reduce Motion in Settings, the modal appears instantly with no
        // slide/fade transition.
        .lexiconMotion(.easeInOut(duration: 0.2), value: vm.searchOpen)
        .onAppear(perform: handleOpenChange)
        .onChange(of: vm.searchOpen) { isOpen in
            handleOpenChange(isOpen: isOpen)
        }
    }

    // MARK: - Open/close handling

    private func handleOpenChange(isOpen: Bool? = nil) {
        let open = isOpen ?? vm.searchOpen
        if open {
            // Match web's 250ms focus delay (transition needs to settle first).
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.25) {
                if vm.searchOpen {
                    isFocused = true
                }
            }
        } else {
            query = ""
            isFocused = false
        }
    }

    // MARK: - Overlay content

    private var overlayContent: some View {
        VStack(spacing: 0) {
            topBar
            contentArea
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(palette.background.ignoresSafeArea())
    }

    // MARK: - Top bar (close + capsule search field + mic + camera)

    private var topBar: some View {
        HStack(spacing: 8) {
            // Close button (xmark) — 36×36 visual, 44×44 hit area.
            Button {
                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                query = ""
                isFocused = false
                voFocus = nil
                vm.closeSearch()
            } label: {
                Image(systemName: "xmark")
                    .font(.system(size: 16, weight: .regular))
                    .foregroundStyle(palette.foreground.opacity(0.7))
                    .frame(width: 36, height: 36)
                    .lexiconMinTouchTarget()
            }
            .buttonStyle(.plain)
            .accessibilityLabel(Text(LexiconAccessibilityLabels.closeSearch))
            .accessibilityAddTraits(.isButton)

            // Capsule search field — combined into ONE VoiceOver element
            // so the user hears "Search for a word, search field" instead
            // of navigating the magnifyingglass icon + text field + mic +
            // camera as 4 separate elements.
            HStack(spacing: 10) {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 14, weight: .regular))
                    .foregroundStyle(palette.foreground.opacity(0.5))
                    .lexiconDecorative()

                TextField("Search for a word...", text: $query)
                    .font(.lexiconBody(14))
                    .foregroundStyle(palette.foreground)
                    .focused($isFocused)
                    .submitLabel(.search)
                    .onSubmit {
                        let trimmed = trimmedQuery
                        guard !trimmed.isEmpty else { return }
                        vm.addRecentSearch(trimmed)
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        // P4-05 §7 — move VoiceOver focus to the results
                        // list when the user submits a query. Brief delay
                        // lets the keyboard dismiss first so the focus
                        // move isn't fighting the keyboard dismissal
                        // animation.
                        DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
                            if !results.isEmpty {
                                voFocus = .searchResults
                            }
                        }
                    }

                // Mic icon — 28×28 visual, 44×44 hit area.
                Button {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                } label: {
                    Image(systemName: "mic")
                        .font(.system(size: 14, weight: .regular))
                        .foregroundStyle(palette.foreground.opacity(0.5))
                        .frame(width: 28, height: 28)
                        .lexiconMinTouchTarget()
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text(LexiconAccessibilityLabels.voiceSearch))
                .accessibilityAddTraits(.isButton)

                // Camera icon — 28×28 visual, 44×44 hit area.
                Button {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                } label: {
                    Image(systemName: "camera")
                        .font(.system(size: 14, weight: .regular))
                        .foregroundStyle(palette.foreground.opacity(0.5))
                        .frame(width: 28, height: 28)
                        .lexiconMinTouchTarget()
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text(LexiconAccessibilityLabels.imageSearch))
                .accessibilityAddTraits(.isButton)
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 10)
            .background(palette.card)
            .clipShape(Capsule())
            .overlay(Capsule().stroke(palette.border, lineWidth: 1))
            .accessibilityElement(children: .ignore)
            .accessibilityLabel(Text(LexiconAccessibilityLabels.search))
            .accessibilityAddTraits(.isSearchField)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(palette.background)
    }

    // MARK: - Content area (empty / results / no-results)

    @ViewBuilder
    private var contentArea: some View {
        ScrollView {
            if trimmedQuery.isEmpty {
                emptyState
                    .padding(.bottom, 40)
            } else if !results.isEmpty {
                resultsList
                    .padding(.bottom, 40)
            } else {
                noResultsState
                    .padding(.bottom, 40)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    // MARK: - Empty state (no query)

    private var emptyState: some View {
        VStack(alignment: .leading, spacing: 24) {
            // Recent searches
            if !vm.recentSearches.isEmpty {
                VStack(alignment: .leading, spacing: 12) {
                    HStack {
                        Text("RECENT")
                            .font(.lexiconBody(11, weight: .semibold))
                            .tracking(1.4)
                            .foregroundStyle(palette.mutedForeground)
                            .lexiconAccessibilityHeading(.h2)
                        Spacer()
                        Button {
                            UIImpactFeedbackGenerator(style: .light).impactOccurred()
                            vm.clearRecentSearches()
                        } label: {
                            HStack(spacing: 4) {
                                Image(systemName: "trash")
                                    .font(.system(size: 11, weight: .regular))
                                Text("Clear")
                                    .font(.lexiconBody(11))
                            }
                            .foregroundStyle(palette.foreground.opacity(0.6))
                            .lexiconMinTouchTarget()
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel(Text(LexiconAccessibilityLabels.clearRecents))
                        .accessibilityAddTraits(.isButton)
                    }

                    VStack(spacing: 0) {
                        ForEach(vm.recentSearches, id: \.self) { recent in
                            Button {
                                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                                query = recent
                                isFocused = true
                            } label: {
                                HStack(spacing: 12) {
                                    Image(systemName: "clock")
                                        .font(.system(size: 14, weight: .regular))
                                        .foregroundStyle(palette.mutedForeground)
                                        .lexiconDecorative()
                                    Text(recent)
                                        .font(.lexiconBody(14))
                                        .foregroundStyle(palette.foreground.opacity(0.8))
                                        .lineLimit(1)
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                    Image(systemName: "arrow.right")
                                        .font(.system(size: 14, weight: .regular))
                                        .foregroundStyle(palette.mutedForeground)
                                        .lexiconDecorative()
                                }
                                .padding(.vertical, 10)
                                .lexiconMinTouchTarget()
                                .contentShape(Rectangle())
                            }
                            .buttonStyle(.plain)
                            .accessibilityLabel(Text(recent))
                            .accessibilityHint(Text("Search again"))
                            .accessibilityAddTraits(.isButton)
                        }
                    }
                }
            }

            // Suggested pills
            VStack(alignment: .leading, spacing: 12) {
                Text("SUGGESTED")
                    .font(.lexiconBody(11, weight: .semibold))
                    .tracking(1.4)
                    .foregroundStyle(palette.mutedForeground)
                    .lexiconAccessibilityHeading(.h2)
                FlowLayout(spacing: 8) {
                    ForEach(suggestions, id: \.self) { s in
                        Button {
                            UIImpactFeedbackGenerator(style: .light).impactOccurred()
                            query = s
                            isFocused = true
                        } label: {
                            Text(s)
                                .font(.lexiconBody(12))
                                .foregroundStyle(palette.foreground.opacity(0.8))
                                .padding(.horizontal, 14)
                                .padding(.vertical, 6)
                                .background(palette.card)
                                .clipShape(Capsule())
                                .overlay(Capsule().stroke(palette.border, lineWidth: 1))
                                // Pills are visually ~26pt tall (6+12pt
                                // text+6). Expand the tappable frame to
                                // 44pt — the pill background stays small,
                                // but the hit area grows.
                                .lexiconMinTouchTarget()
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel(Text(s))
                        .accessibilityHint(Text("Search for this word"))
                        .accessibilityAddTraits(.isButton)
                    }
                }
            }
        }
        .padding(.horizontal, 20)
        .padding(.top, 16)
    }

    // MARK: - Results list

    private var resultsList: some View {
        VStack(spacing: 0) {
            // P4-05 §7 — VoiceOver focus target. When the user submits a
            // query, `voFocus` is set to `.searchResults`, which moves
            // VoiceOver focus to this header so the user hears the first
            // result without manually swiping.
            Text("\(results.count) \(results.count == 1 ? "result" : "results")")
                .font(.lexiconBody(11, weight: .semibold))
                .tracking(1.4)
                .foregroundStyle(palette.mutedForeground)
                .frame(maxWidth: .infinity, alignment: .leading)
                .lexiconAccessibilityHeading(.h2)
                .accessibilityFocused($voFocus, equals: .searchResults)

            ForEach(results) { word in
                Button {
                    selectWord(word)
                } label: {
                    HStack(alignment: .firstTextBaseline, spacing: 12) {
                        Text(word.word)
                            .font(.lexiconDisplay(16, weight: .semibold))
                            .foregroundStyle(palette.foreground)
                        Text(word.partOfSpeech.displayValue)
                            .font(.lexiconBody(12).italic())
                            .foregroundStyle(palette.mutedForeground)
                        Text(word.definitions.first ?? "")
                            .font(.lexiconLong(13))
                            .foregroundStyle(palette.foreground.opacity(0.7))
                            .lineLimit(1)
                            .truncationMode(.tail)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 12)
                    .lexiconMinTouchTarget()
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                // Combined element so VoiceOver reads the whole row in
                // one swipe: "Petrichor, noun, the pleasant earthy scent
                // produced when rain falls on dry soil, button".
                .accessibilityElement(children: .ignore)
                .accessibilityLabel(Text("\(word.word), \(word.partOfSpeech.displayValue), \(word.definitions.first ?? "")"))
                .accessibilityHint(Text("Opens the dictionary entry"))
                .accessibilityAddTraits(.isButton)
            }
        }
        .padding(.horizontal, 20)
        .padding(.top, 8)
    }

    /// Select a word: add to recents, close search, open detail (200ms delay).
    private func selectWord(_ word: WordEntry) {
        UIImpactFeedbackGenerator(style: .medium).impactOccurred()
        vm.addRecentSearch(word.word)
        vm.closeSearch()
        query = ""
        isFocused = false
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
            vm.openWord(word)
        }
    }

    // MARK: - No results state

    private var noResultsState: some View {
        VStack(spacing: 12) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 28, weight: .regular))
                .foregroundStyle(palette.mutedForeground)
                .frame(width: 64, height: 64)
                .background(palette.card)
                .clipShape(Circle())
                .overlay(Circle().stroke(palette.border, lineWidth: 1))
                .lexiconDecorative()

            Text("No words found")
                .font(.lexiconDisplay(18, weight: .semibold))
                .foregroundStyle(palette.foreground)
                .lexiconAccessibilityHeading(.h2)

            Text("We couldn\u{2019}t find \u{201C}\(query)\u{201D} in this offline pack. Try another spelling or download more language pairs.")
                .font(.lexiconBody(12))
                .foregroundStyle(palette.mutedForeground)
                .multilineTextAlignment(.center)
                .lineSpacing(2)
                .frame(maxWidth: 280)
                .fixedSize(horizontal: false, vertical: true)
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 64)
    }
}


// MARK: - FlowLayout (wrapping pill container — iOS 16+ Layout protocol)

/// Simple flow layout that wraps subviews to the next line when they exceed
/// the available width. Mirrors CSS `flex-wrap: wrap` behaviour.
/// Used for the suggested-search pill tags.
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
    let vm = LexiconViewModel()
    vm.searchOpen = true
    return SearchModal()
        .environmentObject(vm)
}
