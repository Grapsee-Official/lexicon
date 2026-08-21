//
//  SearchModal.swift
//  Lexora
//
//  Translation of `src/components/lexora/search-modal.tsx`.
//
//  Full-screen search overlay with:
//    - Animated entry (opacity + move from top edge).
//    - Top bar: close button (xmark), capsule search TextField (bg-card, border),
//      mic + camera icons.
//    - TextField auto-focuses on appear via @FocusState (250ms delay, matching web).
//    - Live search: filters dictionary via LexoraData.searchWords(query, limit: 8).
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
    @EnvironmentObject private var vm: LexoraViewModel
    @Environment(\.colorScheme) private var scheme

    @State private var query: String = ""
    @FocusState private var isFocused: Bool

    private var palette: LexoraPalette { lexoraPalette(scheme) }

    private let suggestions: [String] = [
        "Petrichor", "Saudade", "Komorebi",
        "Mellifluous", "Halcyon", "Numinous",
    ]

    private var trimmedQuery: String {
        query.trimmingCharacters(in: .whitespacesAndNewlines)
    }

    private var results: [WordEntry] {
        LexoraData.searchWords(query, limit: 8)
    }

    var body: some View {
        ZStack {
            if vm.searchOpen {
                overlayContent
                    .transition(.move(edge: .top).combined(with: .opacity))
            }
        }
        .animation(.easeInOut(duration: 0.2), value: vm.searchOpen)
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
            // Close button (xmark)
            Button {
                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                query = ""
                isFocused = false
                vm.closeSearch()
            } label: {
                Image(systemName: "xmark")
                    .font(.system(size: 16, weight: .regular))
                    .foregroundStyle(palette.foreground.opacity(0.7))
                    .frame(width: 36, height: 36)
            }
            .buttonStyle(.plain)
            .accessibilityLabel(Text("Close search"))

            // Capsule search field
            HStack(spacing: 10) {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 14, weight: .regular))
                    .foregroundStyle(palette.foreground.opacity(0.5))

                TextField("Search for a word...", text: $query)
                    .font(.lexoraBody(14))
                    .foregroundStyle(palette.foreground)
                    .focused($isFocused)
                    .submitLabel(.search)
                    .onSubmit {
                        let trimmed = trimmedQuery
                        guard !trimmed.isEmpty else { return }
                        vm.addRecentSearch(trimmed)
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    }

                // Mic icon
                Button {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                } label: {
                    Image(systemName: "mic")
                        .font(.system(size: 14, weight: .regular))
                        .foregroundStyle(palette.foreground.opacity(0.5))
                        .frame(width: 28, height: 28)
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text("Voice search"))

                // Camera icon
                Button {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                } label: {
                    Image(systemName: "camera")
                        .font(.system(size: 14, weight: .regular))
                        .foregroundStyle(palette.foreground.opacity(0.5))
                        .frame(width: 28, height: 28)
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text("Image search"))
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 10)
            .background(palette.card)
            .clipShape(Capsule())
            .overlay(Capsule().stroke(palette.border, lineWidth: 1))
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
                            .font(.lexoraBody(11, weight: .semibold))
                            .tracking(1.4)
                            .foregroundStyle(palette.mutedForeground)
                        Spacer()
                        Button {
                            UIImpactFeedbackGenerator(style: .light).impactOccurred()
                            vm.clearRecentSearches()
                        } label: {
                            HStack(spacing: 4) {
                                Image(systemName: "trash")
                                    .font(.system(size: 11, weight: .regular))
                                Text("Clear")
                                    .font(.lexoraBody(11))
                            }
                            .foregroundStyle(palette.foreground.opacity(0.6))
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel(Text("Clear recent searches"))
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
                                    Text(recent)
                                        .font(.lexoraBody(14))
                                        .foregroundStyle(palette.foreground.opacity(0.8))
                                        .lineLimit(1)
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                    Image(systemName: "arrow.right")
                                        .font(.system(size: 14, weight: .regular))
                                        .foregroundStyle(palette.mutedForeground)
                                }
                                .padding(.vertical, 10)
                                .contentShape(Rectangle())
                            }
                            .buttonStyle(.plain)
                        }
                    }
                }
            }

            // Suggested pills
            VStack(alignment: .leading, spacing: 12) {
                Text("SUGGESTED")
                    .font(.lexoraBody(11, weight: .semibold))
                    .tracking(1.4)
                    .foregroundStyle(palette.mutedForeground)
                FlowLayout(spacing: 8) {
                    ForEach(suggestions, id: \.self) { s in
                        Button {
                            UIImpactFeedbackGenerator(style: .light).impactOccurred()
                            query = s
                            isFocused = true
                        } label: {
                            Text(s)
                                .font(.lexoraBody(12))
                                .foregroundStyle(palette.foreground.opacity(0.8))
                                .padding(.horizontal, 14)
                                .padding(.vertical, 6)
                                .background(palette.card)
                                .clipShape(Capsule())
                                .overlay(Capsule().stroke(palette.border, lineWidth: 1))
                        }
                        .buttonStyle(.plain)
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
            ForEach(results) { word in
                Button {
                    selectWord(word)
                } label: {
                    HStack(alignment: .firstTextBaseline, spacing: 12) {
                        Text(word.word)
                            .font(.lexoraDisplay(16, weight: .semibold))
                            .foregroundStyle(palette.foreground)
                        Text(word.partOfSpeech.displayValue)
                            .font(.lexoraBody(12).italic())
                            .foregroundStyle(palette.mutedForeground)
                        Text(word.definitions.first ?? "")
                            .font(.lexoraLong(13))
                            .foregroundStyle(palette.foreground.opacity(0.7))
                            .lineLimit(1)
                            .truncationMode(.tail)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    .padding(.horizontal, 8)
                    .padding(.vertical, 12)
                    .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
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

            Text("No words found")
                .font(.lexoraDisplay(18, weight: .semibold))
                .foregroundStyle(palette.foreground)

            Text("We couldn\u{2019}t find \u{201C}\(query)\u{201D} in this offline pack. Try another spelling or download more language pairs.")
                .font(.lexoraBody(12))
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
    let vm = LexoraViewModel()
    vm.searchOpen = true
    return SearchModal()
        .environmentObject(vm)
}
