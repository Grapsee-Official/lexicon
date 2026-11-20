//
//  WordEntryScreens.swift
//  Lexicon
//
//  Shared Word-Entry Architecture powering:
//    - Page 2: WordOfTheDayScreen
//    - Page 3: WordDetailProfileScreen
//    - Page 9: SearchResultsScreen
//    - Page 10: ExpandedExplorationScreen (with 36-language progressive translations)
//

import SwiftUI

// MARK: - Page 2: Word of the Day
struct WordOfTheDayScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @EnvironmentObject private var theme: LexiconThemeState
    @Environment(\.colorScheme) private var scheme

    @State private var dayIdx: Int = 0
    private var isDark: Bool { scheme == .dark }
    private var featured: [WordEntry] {
        [LexiconData.serendipityEntry, LexiconData.solitudeEntry, LexiconData.wordOfTheDay, LexiconData.dictionary[3]]
    }

    var body: some View {
        let p = lexiconPalette(scheme)
        let word = LexiconData.featuredWord(dayOffset: dayIdx)

        ScrollView {
            VStack(spacing: 12) {
                // Words and landscape belong to the same journal page.
                // No arbitrary height, trapped right-side art, or giant
                // transparent card taller than its own watercolour.
                VStack(alignment: .leading, spacing: 12) {
                    HStack(spacing: 8) {
                        Button { theme.toggle() } label: {
                            BotanicalSprig(color: isDark ? LexiconColors.accentDark : LexiconColors.primary)
                                .frame(width: 26, height: 26)
                                .frame(width: 44, height: 44)
                                .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel("Toggle appearance")
                        VStack(alignment: .leading, spacing: 2) {
                            Text("Word of the Day")
                                .font(.lexiconDisplay(24))
                                .foregroundStyle(p.foreground)
                            Text("A new word, a wider world.")
                                .font(.lexiconLong(12))
                                .foregroundStyle(p.mutedForeground)
                        }
                        Spacer(minLength: 0)
                        Button {
                            dayIdx = (dayIdx + 1) % featured.count
                        } label: {
                            Image(systemName: "arrow.right")
                                .font(.system(size: 19))
                                .foregroundStyle(p.foreground)
                                .frame(width: 44, height: 44)
                                .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel("Next featured word")
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 8)

                    HStack(spacing: 6) {
                        Image(systemName: "calendar")
                            .font(.system(size: 13))
                            .foregroundStyle(p.highlight)
                        Text(Date.now, format: .dateTime.weekday(.abbreviated).month(.abbreviated).day().year())
                            .font(.lexiconBody(12))
                            .foregroundStyle(p.foreground)
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(p.card, in: Capsule())
                    .overlay(Capsule().stroke(p.border, lineWidth: 1))
                    .padding(.horizontal, 18)

                    VStack(alignment: .leading, spacing: 9) {
                        HStack(spacing: 7) {
                            Button { vm.openWord(word) } label: {
                                Text(word.word)
                                    .font(.lexiconDisplay(31))
                                    .foregroundStyle(p.foreground)
                                    .fixedSize(horizontal: false, vertical: true)
                            }
                            .buttonStyle(.plain)
                            Button { vm.pronounce(word.word) } label: {
                                Image(systemName: "speaker.wave.2")
                                    .font(.system(size: 18))
                                    .foregroundStyle(p.mutedForeground)
                                    .frame(width: 44, height: 44)
                            }
                            .buttonStyle(.plain)
                            .accessibilityLabel("Pronounce word")
                            Spacer(minLength: 0)
                        }
                        Text("\(word.partOfSpeech.displayValue) · \(word.phonetic)")
                            .font(.lexiconLong(14).italic())
                            .foregroundStyle(p.mutedForeground)
                        Text(word.definitions.first ?? "")
                            .font(.lexiconLong(15))
                            .foregroundStyle(p.foreground)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .fixedSize(horizontal: false, vertical: true)
                        if let example = word.examples.first, !example.isEmpty {
                            Text("“\(example)”")
                                .font(.lexiconLong(13).italic())
                                .foregroundStyle(p.foreground.opacity(0.85))
                                .fixedSize(horizontal: false, vertical: true)
                        }
                        Button { vm.openWord(word) } label: {
                            Label("Read the full entry", systemImage: "arrow.right")
                                .font(.lexiconBody(12))
                                .foregroundStyle(p.primary)
                        }
                        .buttonStyle(.plain)
                    }
                    .padding(17)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(p.card, in: RoundedRectangle(cornerRadius: 18, style: .continuous))
                    .overlay(RoundedRectangle(cornerRadius: 18, style: .continuous).stroke(p.border, lineWidth: 1))
                    .padding(.horizontal, 16)

                    Image(countrysideImageName("ill-hero-bridge", isDark: isDark))
                        .resizable()
                        .scaledToFit()
                        .frame(maxWidth: .infinity)
                        .accessibilityHidden(true)

                    HStack(spacing: 0) {
                        ForEach(featured.indices, id: \.self) { index in
                            Button { dayIdx = index } label: {
                                Circle()
                                    .fill(index == dayIdx ? p.primary : p.border)
                                    .frame(width: index == dayIdx ? 8 : 5, height: index == dayIdx ? 8 : 5)
                                    .frame(width: 44, height: 44)
                                    .contentShape(Rectangle())
                            }
                            .buttonStyle(.plain)
                            .accessibilityLabel("Featured word \(index + 1)")
                        }
                    }
                    .frame(maxWidth: .infinity)
                }

                // About the Word
                VStack(spacing: 8) {
                    BotanicalSectionTitle(
                        title: "About the Word",
                        actionLabel: "View More",
                        onAction: { vm.openWord(word) }
                    )
                    Button { vm.openWord(word) } label: {
                        HStack(spacing: 10) {
                            Image(countrysideImageName("ill-open-book-meadow", isDark: isDark))
                                .resizable()
                                .scaledToFit()
                                 .frame(width: 112, height: 84)
                                .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
                            Text(word.etymology)
                                .font(.lexiconLong(11))
                                .foregroundStyle(p.foreground.opacity(0.88))
                                .lineLimit(5)
                                .multilineTextAlignment(.leading)
                                .frame(maxWidth: .infinity, alignment: .leading)
                        }
                        .padding(10)
                        .background(p.card)
                        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                        .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).stroke(p.border, lineWidth: 1))
                    }
                    .buttonStyle(.plain)
                    .padding(.horizontal, 16)
                }

                // Explore Further (2x3 Grid)
                VStack(spacing: 8) {
                    BotanicalSectionTitle(title: "Explore Further", actionLabel: nil)
                    let links: [(String, String, Bool)] = [
                        ("Meaning", "book", false),
                        ("Synonyms", "arrow.left.arrow.right", false),
                        ("Antonyms", "arrow.up.arrow.down", false),
                        ("Etymology", "sparkles", false),
                        ("Translations", "globe", true),
                        ("Usage Examples", "doc.text", false),
                    ]
                    LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 8), count: 3), spacing: 8) {
                        ForEach(links, id: \.0) { item in
                            Button {
                                if item.2 { vm.openWordExpanded(word) }
                                else { vm.openWord(word) }
                            } label: {
                                VStack(spacing: 4) {
                                    Image(systemName: item.1)
                                        .font(.system(size: 15))
                                        .foregroundStyle(p.primary)
                                    Text(item.0)
                                        .font(.lexiconBody(10.5))
                                        .foregroundStyle(p.foreground)
                                        .lineLimit(1)
                                        .minimumScaleFactor(0.8)
                                }
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 10)
                                .background(p.card)
                                .clipShape(RoundedRectangle(cornerRadius: 13, style: .continuous))
                                .overlay(RoundedRectangle(cornerRadius: 13, style: .continuous).stroke(p.border, lineWidth: 1))
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 16)
                }

            }
            .padding(.bottom, 160)
        }
        .background(p.background)
    }
}

// MARK: - Page 3: Word Detail (Full Word Profile)
struct WordDetailProfileScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @State private var heroWidth: CGFloat = 390
    private var isDark: Bool { scheme == .dark }

    var body: some View {
        let p = lexiconPalette(scheme)
        let word = vm.detailWord
        let saved = vm.isSaved(word.id)

        ScrollView {
            VStack(spacing: dynamicTypeSize.isAccessibilitySize ? 12 : 8) {
                // Exact approved composition: the transparent watercolor
                // lives behind the text as one scene, not beneath the entry.
                VStack(alignment: .leading, spacing: dynamicTypeSize.isAccessibilitySize ? 7 : 2) {
                    HStack(spacing: 0) {
                        Button { vm.navigateBack() } label: {
                            Image(systemName: "arrow.left")
                                .font(.system(size: 19))
                                .foregroundStyle(p.foreground)
                                .frame(width: 44, height: 44)
                                .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel("Back")
                        Spacer()
                        Button { vm.toggleSave(word.id) } label: {
                            Image(systemName: saved ? "bookmark.fill" : "bookmark")
                                .font(.system(size: 19))
                                .foregroundStyle(saved ? p.highlight : p.foreground)
                                .frame(width: 44, height: 44)
                                .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel(saved ? "Remove bookmark" : "Save word")
                        ShareLink(item: "\(word.word) — \(word.definitions.first ?? "")") {
                            Image(systemName: "square.and.arrow.up")
                                .font(.system(size: 19))
                                .foregroundStyle(p.foreground)
                                .frame(width: 44, height: 44)
                                .contentShape(Rectangle())
                        }
                        .accessibilityLabel("Share word")
                    }
                    .padding(.horizontal, 10)

                    HStack(spacing: 6) {
                        BotanicalSprig(color: p.highlight)
                            .frame(width: 13, height: 13)
                        Text("Word of the Day")
                            .font(.lexiconDisplay(14))
                            .foregroundStyle(p.foreground)
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 4)
                    .background(p.highlight.opacity(0.14), in: Capsule())
                    .overlay(Capsule().stroke(p.border, lineWidth: 1))
                    .padding(.leading, 20)

                    HStack(alignment: .center, spacing: 3) {
                        Text(word.word.capitalized)
                            .font(.lexiconDisplay(32))
                            .foregroundStyle(p.foreground)
                            .fixedSize(horizontal: false, vertical: true)
                        Button { vm.pronounce(word.word) } label: {
                            Image(systemName: "speaker.wave.2")
                                .font(.system(size: 18))
                                .foregroundStyle(p.foreground)
                                .frame(width: 44, height: 44)
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel("Pronounce word")
                    }
                    .frame(width: heroWidth * 0.74, alignment: .leading)
                    .padding(.leading, 20)

                    Text(word.phonetic)
                        .font(.lexiconLong(15).italic())
                        .foregroundStyle(p.mutedForeground)
                        .padding(.leading, 20)

                    HStack(spacing: 8) {
                        Text(word.partOfSpeech.displayValue)
                            .font(.lexiconLong(13).italic())
                            .foregroundStyle(p.foreground)
                            .padding(.horizontal, 13)
                            .padding(.vertical, 4)
                            .background(p.highlight.opacity(0.17), in: Capsule())
                            .overlay(Capsule().stroke(p.border, lineWidth: 1))
                        BotanicalSprig(color: p.highlight)
                            .frame(width: 15, height: 15)
                    }
                    .padding(.leading, 20)

                    Text(word.definitions.first ?? "")
                        .font(.lexiconLong(16))
                        .foregroundStyle(p.foreground)
                        .fixedSize(horizontal: false, vertical: true)
                        .frame(width: heroWidth * 0.62, alignment: .leading)
                        .padding(.leading, 20)
                }
                .padding(.bottom, 3)
                .frame(minHeight: max(185, min(300, heroWidth * 0.49)),
                       alignment: .topLeading)
                .background(alignment: .topTrailing) {
                    // Preserve the full transparency of the owner-approved
                    // watercolor while enlarging the scene behind the tiles.
                    Image(countrysideImageName("ill-hero-bridge", isDark: isDark))
                        .resizable()
                        .scaledToFit()
                        .frame(width: heroWidth * 1.20,
                               height: max(230, min(390, heroWidth * 0.78)),
                               alignment: .topTrailing)
                        .offset(x: heroWidth * 0.025, y: -28)
                        .accessibilityHidden(true)
                }
                .padding(.top, dynamicTypeSize.isAccessibilitySize ? 0 : -16)

                // 4 Metadata Cards
                HStack(spacing: 7) {
                    metaTile("book", "Language", word.language.components(separatedBy: " ").first ?? "English", p: p)
                    metaTile("globe", "Origin", word.originPeriod, p: p)
                    metaTile("star", "Frequency", word.frequency, p: p)
                    metaTile("leaf", "Level", word.level, p: p)
                }
                .padding(.horizontal, 16)
                .padding(.top, dynamicTypeSize.isAccessibilitySize ? 0 : -19)

                // Meanings Card
                VStack(alignment: .leading, spacing: 7) {
                    HStack(spacing: 6) {
                        BotanicalSprig(color: p.highlight).frame(width: 14, height: 14)
                        Text("Meanings")
                            .font(.lexiconDisplay(16))
                            .foregroundStyle(p.foreground)
                    }
                    ForEach(Array((word.id == "serendipity" ? Array(word.definitions.dropFirst()) : word.definitions).prefix(2).enumerated()), id: \.offset) { idx, def in
                        HStack(alignment: .top, spacing: 9) {
                            Text("\(idx + 1)")
                                .font(.lexiconBody(10, weight: .medium))
                                .foregroundStyle(p.foreground)
                                .frame(width: 20, height: 20)
                                .background(p.highlight.opacity(0.22))
                                .clipShape(Circle())
                            VStack(alignment: .leading, spacing: 2) {
                                Text(def)
                                    .font(.lexiconLong(12.5))
                                    .foregroundStyle(p.foreground)
                                if idx < word.examples.count {
                                    Text("“\(word.examples[idx])”")
                                        .font(.lexiconBody(10.5))
                                        .foregroundStyle(p.mutedForeground)
                                }
                            }
                        }
                    }
                }
                .padding(12)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(p.card)
                .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).stroke(p.border, lineWidth: 1))
                .padding(.horizontal, 16)

                if !word.synonyms.isEmpty {
                    pillCard(title: "Synonyms", items: word.synonyms, p: p)
                        .padding(.horizontal, 16)
                }
                if !word.antonyms.isEmpty {
                    pillCard(title: "Antonyms", items: word.antonyms, p: p)
                        .padding(.horizontal, 16)
                }

                etymologyUsageCards(word: word, p: p)

                if !word.didYouKnow.isEmpty {
                    // The bookplate height follows its own watercolor ratio;
                    // large type moves copy above it rather than stretching or
                    // cropping the original art.
                    let artName = countrysideImageName("ill-banner-bay", isDark: isDark)
                    let natural = UIImage(named: artName)?.size ?? CGSize(width: 3, height: 1)
                    let artRatio = max(1, natural.width / max(1, natural.height))
                    // The watercolor fills only the right half of this
                    // bookplate. Natural rendered height defines the card,
                    // rather than using the entire banner width as art width.
                    let artWidth = (heroWidth - 32) * 0.49
                    let artHeight = artWidth / artRatio
                    let stacked = dynamicTypeSize.isAccessibilitySize
                        || heroWidth < 345 || word.didYouKnow.count > 135

                    VStack(alignment: .leading, spacing: 0) {
                        if stacked {
                            VStack(alignment: .leading, spacing: 8) {
                                Text("Did You Know?")
                                    .font(.lexiconDisplay(18))
                                    .foregroundStyle(p.foreground)
                                Text(word.didYouKnow)
                                    .font(.lexiconBody(13))
                                    .foregroundStyle(p.foreground.opacity(0.9))
                                    .fixedSize(horizontal: false, vertical: true)
                            }
                            .padding(16)
                        }

                        ZStack(alignment: .leading) {
                            Image(artName)
                                .resizable()
                                .scaledToFit()
                                .frame(width: artWidth, height: artHeight,
                                       alignment: .bottomTrailing)
                                .frame(maxWidth: .infinity, alignment: .trailing)
                                .accessibilityHidden(true)

                            if !stacked {
                                VStack(alignment: .leading, spacing: 5) {
                                    HStack(spacing: 6) {
                                        Image(systemName: "lightbulb")
                                            .foregroundStyle(p.highlight)
                                        Text("Did You Know?")
                                            .font(.lexiconDisplay(15))
                                    }
                                    Text(word.didYouKnow)
                                        .font(.lexiconBody(11))
                                        .fixedSize(horizontal: false, vertical: true)
                                }
                                .foregroundStyle(p.foreground)
                                .frame(width: (heroWidth - 32) * 0.61, alignment: .leading)
                                .padding(.leading, 12)
                            }
                        }
                        // Artwork owns the framed panel's height and aspect.
                        .frame(height: artHeight)
                        .background(p.card, in: RoundedRectangle(cornerRadius: 17, style: .continuous))
                        .clipShape(RoundedRectangle(cornerRadius: 17, style: .continuous))
                        .overlay(
                            RoundedRectangle(cornerRadius: 17, style: .continuous)
                                .stroke(p.border, lineWidth: 1)
                        )
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.horizontal, 16)
                }
            }
            .padding(.bottom, 160)
            .background {
                // Actual content width, not UIScreen: split view and iPad
                // resizing keep the Did You Know art proportional.
                GeometryReader { bounds in
                    Color.clear
                        .onAppear { heroWidth = bounds.size.width }
                        .onChange(of: bounds.size.width) { heroWidth = $0 }
                }
            }
        }
        .background(p.background)
    }


    // Reference composition: two balanced cards on normal phone widths,
    // with the botanical footpiece fading into Etymology's lower-right.
    // Only accessibility-sized type / narrow split view uses a vertical pair.
    private func etymologyUsageCards(word: WordEntry, p: LexiconPalette) -> some View {
        let allExamples = word.examples.filter { !$0.isEmpty }
        let examples = Array((word.id == "serendipity" ? Array(allExamples.dropFirst(2)) : allExamples).prefix(2))
        let stacked = dynamicTypeSize.isAccessibilitySize || heroWidth < 335
        return Group {
            if stacked {
                VStack(spacing: 10) {
                    referenceEtymologyCard(word: word, p: p, stacked: true)
                    if !examples.isEmpty {
                        referenceUsageCard(examples: examples, p: p, stacked: true)
                    }
                }
            } else {
                HStack(alignment: .top, spacing: 8) {
                    referenceEtymologyCard(word: word, p: p, stacked: false)
                    if !examples.isEmpty {
                        referenceUsageCard(examples: examples, p: p, stacked: false)
                    }
                }
            }
        }
        .padding(.horizontal, 16)
    }

    private func referenceEtymologyCard(
        word: WordEntry, p: LexiconPalette, stacked: Bool
    ) -> some View {
        let shape = RoundedRectangle(cornerRadius: 17, style: .continuous)
        return VStack(alignment: .leading, spacing: 7) {
            HStack(spacing: 6) {
                BotanicalSprig(color: p.highlight)
                    .frame(width: 15, height: 15)
                Text("Etymology")
                    .font(.lexiconDisplay(16))
                    .foregroundStyle(p.foreground)
            }
            Text(word.etymology)
                .font(.lexiconLong(11))
                .foregroundStyle(p.foreground.opacity(0.9))
                .fixedSize(horizontal: false, vertical: true)
        }
        .padding(12)
        .frame(maxWidth: .infinity, minHeight: stacked ? 142 : 122,
               alignment: .topLeading)
        .background(p.card, in: shape)
        .overlay(alignment: .bottomTrailing) {
            Image(countrysideImageName("ill-books-daisies", isDark: isDark))
                .resizable()
                .scaledToFit()
                .frame(width: stacked ? 145 : 112, height: 95,
                       alignment: .bottomTrailing)
                .allowsHitTesting(false)
                .accessibilityHidden(true)
        }
        .overlay(shape.stroke(p.border, lineWidth: 1))
    }

    private func referenceUsageCard(
        examples: [String], p: LexiconPalette, stacked: Bool
    ) -> some View {
        let shape = RoundedRectangle(cornerRadius: 17, style: .continuous)
        return VStack(alignment: .leading, spacing: 9) {
            HStack(spacing: 6) {
                BotanicalSprig(color: p.highlight)
                    .frame(width: 15, height: 15)
                Text("Usage Examples")
                    .font(.lexiconDisplay(16))
                    .foregroundStyle(p.foreground)
                    .fixedSize(horizontal: false, vertical: true)
            }
            ForEach(Array(examples.enumerated()), id: \.offset) { index, example in
                if index > 0 {
                    Rectangle()
                        .fill(p.border)
                        .frame(height: 1)
                }
                Text("“\(example)”")
                    .font(.lexiconLong(12).italic())
                    .foregroundStyle(p.foreground.opacity(0.89))
                    .fixedSize(horizontal: false, vertical: true)
            }
        }
        .padding(12)
        .frame(maxWidth: .infinity, minHeight: stacked ? 142 : 122,
               alignment: .topLeading)
        .background(p.card, in: shape)
        .overlay(shape.stroke(p.border, lineWidth: 1))
    }

    private func metaTile(_ icon: String, _ label: String, _ value: String, p: LexiconPalette) -> some View {
        VStack(spacing: 2) {
            Image(systemName: icon)
                .font(.system(size: 13))
                .foregroundStyle(p.primary)
            Text(label)
                .font(.lexiconBody(9))
                .foregroundStyle(p.mutedForeground)
            Text(value)
                .font(.lexiconBody(10, weight: .medium))
                .foregroundStyle(p.foreground)
                .lineLimit(1)
                .minimumScaleFactor(0.8)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 9)
        .background(p.card)
        .clipShape(RoundedRectangle(cornerRadius: 13, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 13, style: .continuous).stroke(p.border, lineWidth: 1))
    }

    private func pillCard(title: String, items: [String], p: LexiconPalette) -> some View {
        VStack(alignment: .leading, spacing: 7) {
            HStack(spacing: 6) {
                BotanicalSprig(color: p.highlight).frame(width: 14, height: 14)
                Text(title).font(.lexiconDisplay(15)).foregroundStyle(p.foreground)
            }
            // The reference shows every synonym/antonym pill at once.
            // Never hide the last label in a horizontal scroll viewport.
            ViewThatFits(in: .horizontal) {
                HStack(spacing: 5) {
                    ForEach(items, id: \.self) { item in
                        referencePill(item, palette: p)
                            .fixedSize(horizontal: true, vertical: false)
                    }
                }
                LazyVGrid(columns: [GridItem(.adaptive(minimum: 105), spacing: 6)], spacing: 6) {
                    ForEach(items, id: \.self) { item in
                        referencePill(item, palette: p)
                    }
                }
            }
        }
        .padding(10)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(p.card)
        .clipShape(RoundedRectangle(cornerRadius: 15, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 15, style: .continuous).stroke(p.border, lineWidth: 1))
    }
    private func referencePill(_ item: String, palette p: LexiconPalette) -> some View {
        Text(item)
            .font(.lexiconBody(11))
            .foregroundStyle(p.foreground)
            .padding(.horizontal, 9)
            .padding(.vertical, 4.5)
            .background(p.background.opacity(0.7), in: Capsule())
            .overlay(Capsule().stroke(p.border, lineWidth: 1))
    }

}

// MARK: - Page 9: Search Results ("Dictionary Search")
struct SearchResultsScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @EnvironmentObject private var theme: LexiconThemeState
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        let p = lexiconPalette(scheme)
        let matches = vm.searchWords(vm.searchQuery, limit: 8)
        let primaryWord = matches.first

        ScrollView {
            VStack(spacing: 12) {
                CountrysideHeroHeader(
                    title: "Dictionary\nSearch",
                    subtitle: "Look up any word and explore its full meaning.",
                    illustrationBase: "ill-hero-cottage",
                    onHistoryClick: { vm.navigate(to: .searchDiscovery) },
                    actionKind: "back",
                    actionLabel: "Back to Search",
                    onLogoToggleTheme: { theme.toggle() },
                    heroHeight: 146
                )

                CountrysideSearchPill(
                    text: $vm.searchQuery,
                    placeholder: "Search for a word...",
                    onSubmit: { vm.submitSearch(vm.searchQuery) }
                )
                .padding(.horizontal, 16)

                if vm.searchScopeTab == "Idioms" {
                    let idioms = LexiconData.idiomsAndPhrases.filter { phrase in
                        vm.searchQuery.isEmpty
                            || phrase.phrase.localizedCaseInsensitiveContains(vm.searchQuery)
                            || phrase.meaning.localizedCaseInsensitiveContains(vm.searchQuery)
                            || phrase.example.localizedCaseInsensitiveContains(vm.searchQuery)
                    }
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Idioms & Phrases")
                            .font(.lexiconDisplay(20))
                            .foregroundStyle(p.foreground)
                        if idioms.isEmpty {
                            Text("No matching idioms. Try another phrase.")
                                .font(.lexiconBody(13))
                                .foregroundStyle(p.mutedForeground)
                        }
                        ForEach(idioms, id: \.id) { phrase in
                            VStack(alignment: .leading, spacing: 4) {
                                Text(phrase.phrase).font(.lexiconDisplay(16)).foregroundStyle(p.foreground)
                                Text(phrase.meaning).font(.lexiconBody(13)).foregroundStyle(p.mutedForeground)
                                Text(phrase.example).font(.lexiconLong(13).italic()).foregroundStyle(p.mutedForeground)
                            }
                        }
                    }
                    .padding(16)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(p.card)
                    .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                    .overlay(RoundedRectangle(cornerRadius: 18).stroke(p.border, lineWidth: 1))
                    .padding(.horizontal, 16)
                } else if let w = primaryWord {
                    let fav = vm.isFavorite(w.id)
                    VStack(alignment: .leading, spacing: 10) {
                        HStack {
                            Button { vm.openWord(w) } label: {
                                Text(w.word.lowercased())
                                    .font(.lexiconDisplay(27))
                                    .foregroundStyle(p.foreground)
                            }
                            .buttonStyle(.plain)
                            Spacer()
                            Text(w.partOfSpeech.displayValue)
                                .font(.lexiconLong(11.5).italic())
                                .foregroundStyle(p.foreground)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 4)
                                .background(p.highlight.opacity(0.22))
                                .clipShape(Capsule())
                        }

                        HStack(spacing: 8) {
                            Text(w.phonetic)
                                .font(.lexiconBody(12.5))
                                .foregroundStyle(p.mutedForeground)
                            Button { vm.pronounce(w.word) } label: {
                                Image(systemName: "speaker.wave.2")
                                    .font(.system(size: 16))
                                    .foregroundStyle(p.mutedForeground)
                                    .frame(width: 34, height: 34)
                            }
                            .buttonStyle(.plain)
                            .accessibilityLabel("Pronounce word")
                        }

                        HStack(spacing: 8) {
                            Rectangle().fill(p.border).frame(height: 1)
                            BotanicalSprig(color: p.highlight).frame(width: 14, height: 14)
                            Rectangle().fill(p.border).frame(height: 1)
                        }

                        Text(w.definitions.first ?? "")
                            .font(.lexiconLong(14))
                            .foregroundStyle(p.foreground)

                        if let ex = w.examples.first {
                            HStack(alignment: .top, spacing: 8) {
                                Image(systemName: "quote.opening")
                                    .font(.system(size: 14))
                                    .foregroundStyle(p.primary)
                                Text("“\(ex)”")
                                    .font(.lexiconLong(12.5).italic())
                                    .foregroundStyle(p.foreground)
                            }
                            .padding(12)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(p.highlight.opacity(0.14))
                            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                        }

                        resultRow(title: "Synonyms", value: w.synonyms.joined(separator: ", "), p: p) { vm.openWord(w) }
                        resultRow(title: "Antonyms", value: w.antonyms.isEmpty ? "—" : w.antonyms.joined(separator: ", "), p: p) { vm.openWord(w) }
                        resultRow(title: "Etymology", value: w.etymology, p: p) { vm.openWord(w) }

                        if !w.relatedWords.isEmpty {
                            VStack(alignment: .leading, spacing: 6) {
                                Button { vm.openWordExpanded(w) } label: {
                                    HStack {
                                        BotanicalSprig(color: p.highlight).frame(width: 15, height: 15)
                                        Text("Related Words").font(.lexiconDisplay(14.5)).foregroundStyle(p.foreground)
                                        Spacer()
                                        Image(systemName: "chevron.right").font(.system(size: 12)).foregroundStyle(p.mutedForeground)
                                    }
                                }
                                .buttonStyle(.plain)

                                ScrollView(.horizontal, showsIndicators: false) {
                                    HStack(spacing: 6) {
                                        ForEach(w.relatedWords, id: \.self) { rw in
                                            Text(rw)
                                                .font(.lexiconBody(10.5))
                                                .foregroundStyle(p.foreground)
                                                .padding(.horizontal, 11)
                                                .padding(.vertical, 4.5)
                                                .background(p.highlight.opacity(0.18))
                                                .clipShape(Capsule())
                                        }
                                    }
                                }
                            }
                        }

                        Button {
                            vm.toggleFavorite(w.id)
                        } label: {
                            HStack(spacing: 6) {
                                Image(systemName: fav ? "bookmark.fill" : "bookmark")
                                    .font(.system(size: 13))
                                    .foregroundStyle(fav ? p.highlight : p.foreground)
                                Text(fav ? "Added to Favorites" : "Add to Favorites")
                                    .font(.lexiconBody(12, weight: .medium))
                                    .foregroundStyle(p.foreground)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 10)
                            .background(fav ? p.highlight.opacity(0.22) : p.background.opacity(0.75))
                            .clipShape(Capsule())
                            .overlay(Capsule().stroke(p.border, lineWidth: 1))
                        }
                        .buttonStyle(.plain)
                    }
                    .padding(16)
                    .background(p.card)
                    .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                    .overlay(RoundedRectangle(cornerRadius: 18, style: .continuous).stroke(p.border, lineWidth: 1))
                    .padding(.horizontal, 16)
                } else {
                    VStack(spacing: 6) {
                        Text("No matching entries")
                            .font(.lexiconDisplay(18))
                            .foregroundStyle(p.foreground)
                        Text("Try searching for “serendipity”, “solitude”, or “eloquence”.")
                            .font(.lexiconBody(12))
                            .foregroundStyle(p.mutedForeground)
                    }
                    .padding(22)
                    .frame(maxWidth: .infinity)
                    .background(p.card)
                    .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                    .padding(.horizontal, 16)
                }

                if matches.count > 1 {
                    VStack(alignment: .leading, spacing: 7) {
                        Text("More Matches")
                            .font(.lexiconDisplay(17))
                            .foregroundStyle(p.foreground)
                        ForEach(Array(matches.dropFirst())) { match in
                            Button { vm.openWord(match) } label: {
                                HStack {
                                    VStack(alignment: .leading, spacing: 3) {
                                        Text(match.word)
                                            .font(.lexiconDisplay(15))
                                            .foregroundStyle(p.foreground)
                                        Text(match.definitions.first ?? "No definition available.")
                                            .font(.lexiconBody(10.5))
                                            .foregroundStyle(p.mutedForeground)
                                            .lineLimit(2)
                                    }
                                    Spacer()
                                    Image(systemName: "chevron.right")
                                        .foregroundStyle(p.mutedForeground)
                                }
                                .padding(12)
                                .background(p.card)
                                .clipShape(RoundedRectangle(cornerRadius: 13, style: .continuous))
                                .overlay(RoundedRectangle(cornerRadius: 13).stroke(p.border, lineWidth: 1))
                            }
                            .buttonStyle(.plain)
                        }
                    }
                    .padding(.horizontal, 16)
                }

            }
            .padding(.bottom, 160)
        }
        .background(p.background)
    }

    private func resultRow(title: String, value: String, p: LexiconPalette, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: 8) {
                BotanicalSprig(color: p.highlight).frame(width: 15, height: 15)
                VStack(alignment: .leading, spacing: 2) {
                    Text(title).font(.lexiconDisplay(14.5)).foregroundStyle(p.foreground)
                    Text(value)
                        .font(.lexiconBody(11.5))
                        .foregroundStyle(p.mutedForeground)
                        .lineLimit(2)
                        .multilineTextAlignment(.leading)
                }
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.system(size: 12))
                    .foregroundStyle(p.mutedForeground)
            }
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Page 10: Expanded Word Exploration & Dictionary Lookup Hub
struct ExpandedExplorationScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @EnvironmentObject private var theme: LexiconThemeState
    @Environment(\.colorScheme) private var scheme

    @State private var query: String = ""
    @State private var showAll36: Bool = false
    private var isDark: Bool { scheme == .dark }

    var body: some View {
        if vm.detailExpanded36 {
            translationsDirectory
        } else {
            // The reference depicts a complete word-entry reading page.
            WordDetailProfileScreen()
        }
    }

    private var translationsDirectory: some View {
        let p = lexiconPalette(scheme)
        let word = vm.detailWord
        let translations = LexiconData.translationsFor(word)
        let expanded = showAll36

        return ScrollView {
            VStack(spacing: 11) {
                CountrysideHeroHeader(
                    title: "Dictionary",
                    subtitle: "Look up any word, anytime.",
                    illustrationBase: "ill-hero-cottage",
                    onHistoryClick: { vm.navigate(to: .searchDiscovery) },
                    actionKind: "back",
                    actionLabel: "Back to Search",
                    onLogoToggleTheme: { theme.toggle() },
                    heroHeight: 140
                )

                CountrysideSearchPill(
                    text: $query,
                    placeholder: "Search for a word...",
                    onSubmit: { if !query.isEmpty { vm.submitSearch(query) } },
                    showFilter: true,
                    onFilterClick: { showAll36.toggle() }
                )
                .padding(.horizontal, 16)

                // Popular Searches
                VStack(spacing: 7) {
                    BotanicalSectionTitle(
                        title: "Popular Searches",
                        actionLabel: vm.popularSearches.isEmpty ? "Restore" : "Clear All",
                        isClearAction: !vm.popularSearches.isEmpty,
                        onAction: {
                            if vm.popularSearches.isEmpty { vm.restorePopularSearches() }
                            else { vm.clearPopularSearches() }
                        }
                    )
                    if !vm.popularSearches.isEmpty {
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 7) {
                                ForEach(vm.popularSearches, id: \.self) { term in
                                    Button { vm.submitSearch(term) } label: {
                                        Text(term)
                                            .font(.lexiconBody(11.5))
                                            .foregroundStyle(p.foreground)
                                            .padding(.horizontal, 13)
                                            .padding(.vertical, 6)
                                            .background(p.card)
                                            .clipShape(Capsule())
                                            .overlay(Capsule().stroke(p.border, lineWidth: 1))
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                            .padding(.horizontal, 16)
                        }
                    }
                }


                // Progressively Expandable 36-Language Word Exploration Card
                VStack(alignment: .leading, spacing: 8) {
                    Button {
                        showAll36.toggle()
                        vm.detailExpanded36 = showAll36
                    } label: {
                        HStack {
                            Image(systemName: "globe")
                                .font(.system(size: 16))
                                .foregroundStyle(p.primary)
                            VStack(alignment: .leading, spacing: 1) {
                                Text("\(word.word) · \(translations.count) available languages")
                                    .font(.lexiconDisplay(15))
                                    .foregroundStyle(p.foreground)
                                Text("\(word.phonetic) · \(expanded ? "Hide translations" : "View available translations")")
                                    .font(.lexiconBody(10))
                                    .foregroundStyle(p.mutedForeground)
                            }
                            Spacer()
                            Text(expanded ? "Collapse" : "Expand →")
                                .font(.lexiconBody(11, weight: .medium))
                                .foregroundStyle(p.primary)
                        }
                    }
                    .buttonStyle(.plain)

                    if translations.isEmpty {
                        Text("This word has no offline translations yet.")
                            .font(.lexiconBody(11))
                            .foregroundStyle(p.mutedForeground)
                    }
                    let slice = expanded ? translations : Array(translations.prefix(4))
                    // Adaptive columns preserve the complete word on narrow iPhones.
                    LazyVGrid(columns: [GridItem(.adaptive(minimum: 158), spacing: 7)], spacing: 7) {
                        ForEach(slice) { tr in
                            VStack(alignment: .leading, spacing: 2) {
                                Text("\(tr.language) (\(tr.code.uppercased()))")
                                    .font(.lexiconBody(9))
                                    .foregroundStyle(p.mutedForeground)
                                Text(tr.translated)
                                    .font(.lexiconDisplay(12))
                                    .foregroundStyle(p.foreground)
                                    .fixedSize(horizontal: false, vertical: true)
                            }
                            .padding(.horizontal, 9)
                            .padding(.vertical, 6)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(p.background.opacity(0.65))
                            .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
                            .overlay(RoundedRectangle(cornerRadius: 10, style: .continuous).stroke(p.border, lineWidth: 1))
                        }
                    }
                }
                .padding(13)
                .background(p.card)
                .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).stroke(p.border, lineWidth: 1))
                .padding(.horizontal, 16)

                // Recently Searched
                VStack(spacing: 7) {
                    BotanicalSectionTitle(
                        title: "Recently Searched",
                        actionLabel: "View all",
                        onAction: { vm.navigate(to: .searchDiscovery) }
                    )
                    VStack(spacing: 7) {
                        ForEach(Array(vm.recentSearches.prefix(6)), id: \.self) { query in
                            if let entry = LexiconData.getWordById(query) {
                                Button { vm.openWord(entry) } label: {
                                    HStack(spacing: 12) {
                                        Image(systemName: "clock.arrow.circlepath")
                                            .font(.system(size: 15))
                                            .foregroundStyle(p.mutedForeground)
                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(entry.word.lowercased())
                                                .font(.lexiconDisplay(15))
                                                .foregroundStyle(p.foreground)
                                            Text("\(entry.partOfSpeech.displayValue)  •  \(entry.phonetic)")
                                                .font(.lexiconBody(10.5).italic())
                                                .foregroundStyle(p.mutedForeground)
                                        }
                                        Spacer()
                                        Image(systemName: "chevron.right")
                                            .font(.system(size: 12))
                                            .foregroundStyle(p.mutedForeground)
                                    }
                                    .padding(.horizontal, 14)
                                    .padding(.vertical, 10)
                                    .background(p.card)
                                    .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                                    .overlay(RoundedRectangle(cornerRadius: 14, style: .continuous).stroke(p.border, lineWidth: 1))
                                }
                                .buttonStyle(.plain)
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                }

                // Quick Access 4-tile box
                VStack(alignment: .leading, spacing: 10) {
                    HStack(spacing: 8) {
                        Image(systemName: "lightbulb")
                            .font(.system(size: 15))
                            .foregroundStyle(p.highlight)
                        VStack(alignment: .leading, spacing: 1) {
                            Text("Quick Access").font(.lexiconDisplay(15)).foregroundStyle(p.foreground)
                            Text("Jump to useful tools and features.").font(.lexiconBody(10.5)).foregroundStyle(p.mutedForeground)
                        }
                    }
                    let qa: [(String, String, ScreenRoute)] = [
                        ("Word of\nthe Day", "book", .wordOfTheDay),
                        ("Favourite\nWords", "star", .collections),
                        ("Bookmarks", "bookmark", .collections),
                        ("Study\nLists", "list.bullet.rectangle", .studyLists),
                    ]
                    HStack(spacing: 7) {
                        ForEach(qa, id: \.0) { item in
                            Button { vm.navigate(to: item.2) } label: {
                                VStack(spacing: 4) {
                                    Image(systemName: item.1)
                                        .font(.system(size: 14))
                                        .foregroundStyle(p.primary)
                                    Text(item.0)
                                        .font(.lexiconBody(9.5))
                                        .foregroundStyle(p.foreground)
                                        .multilineTextAlignment(.center)
                                }
                                .frame(maxWidth: .infinity)
                                .frame(height: 66)
                                .background(p.background.opacity(0.65))
                                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                                .overlay(RoundedRectangle(cornerRadius: 12, style: .continuous).stroke(p.border, lineWidth: 1))
                            }
                            .buttonStyle(.plain)
                        }
                    }
                }
                .padding(13)
                .background(p.card)
                .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).stroke(p.border, lineWidth: 1))
                .padding(.horizontal, 16)

                Image(countrysideImageName("ill-banner-church", isDark: isDark))
                    .resizable()
                    .scaledToFit()
                    // No narrow banner frame that shrinks the church and
                    // wildflower watercolor into a flat strip.
                    .frame(maxWidth: .infinity)
                    .accessibilityHidden(true)
                    .padding(.horizontal, 16)
            }
            .padding(.bottom, 160)
        }
        .background(p.background)
        .onAppear { showAll36 = vm.detailExpanded36 }
    }
}
