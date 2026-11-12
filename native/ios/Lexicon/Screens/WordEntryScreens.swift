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
        let word = featured[dayIdx % featured.count]

        ScrollView {
            VStack(spacing: 12) {
                ZStack(alignment: .topTrailing) {
                    VStack(alignment: .leading, spacing: 10) {
                        HStack {
                            HStack(spacing: 8) {
                                Button { theme.toggle() } label: {
                                    BotanicalSprig(color: isDark ? LexiconColors.accentDark : LexiconColors.primary)
                                        .frame(width: 24, height: 24)
                                }
                                .buttonStyle(.plain)
                                VStack(alignment: .leading, spacing: 1) {
                                    Text("Word of the Day")
                                        .font(.lexiconDisplay(23))
                                        .foregroundStyle(p.foreground)
                                    Text("A new word, a wider world.")
                                        .font(.lexiconLong(11))
                                        .foregroundStyle(p.mutedForeground)
                                }
                            }
                            Spacer()
                            Button {
                                dayIdx = (dayIdx + 1) % featured.count
                            } label: {
                                Image(systemName: "clock.arrow.circlepath")
                                    .font(.system(size: 15))
                                    .foregroundStyle(p.foreground)
                                    .frame(width: 34, height: 34)
                                    .background(p.card.opacity(0.85))
                                    .clipShape(Circle())
                                    .overlay(Circle().stroke(p.border, lineWidth: 1))
                            }
                            .buttonStyle(.plain)
                        }

                        HStack(spacing: 6) {
                            Image(systemName: "calendar")
                                .font(.system(size: 11))
                                .foregroundStyle(p.highlight)
                            Text("Tue, Sep 16, 2026")
                                .font(.lexiconBody(11))
                                .foregroundStyle(p.foreground.opacity(0.85))
                        }
                        .padding(.horizontal, 11)
                        .padding(.vertical, 5)
                        .background(p.card)
                        .clipShape(Capsule())
                        .overlay(Capsule().stroke(p.border, lineWidth: 1))

                        Button { vm.openWord(word) } label: {
                            VStack(alignment: .leading, spacing: 8) {
                                HStack(spacing: 8) {
                                    Text(word.word)
                                        .font(.lexiconDisplay(29))
                                        .foregroundStyle(p.foreground)
                                    Image(systemName: "speaker.wave.2")
                                        .font(.system(size: 15))
                                        .foregroundStyle(p.mutedForeground)
                                }
                                Text(word.partOfSpeech.displayValue)
                                    .font(.lexiconLong(13).italic())
                                    .foregroundStyle(p.mutedForeground)
                                Text(word.phonetic)
                                    .font(.lexiconBody(12))
                                    .foregroundStyle(p.mutedForeground)

                                HStack(spacing: 8) {
                                    Rectangle().fill(p.border).frame(width: 64, height: 1)
                                    BotanicalSprig(color: p.highlight).frame(width: 13, height: 13)
                                    Rectangle().fill(p.border).frame(width: 64, height: 1)
                                }

                                Text(word.definitions.first ?? "")
                                    .font(.lexiconLong(13))
                                    .foregroundStyle(p.foreground)
                                    .multilineTextAlignment(.leading)
                                    .frame(maxWidth: 215, alignment: .leading)

                                HStack(alignment: .top, spacing: 8) {
                                    Image(systemName: "quote.opening")
                                        .font(.system(size: 13))
                                        .foregroundStyle(p.highlight)
                                    Text("“\(word.examples.first ?? "")”")
                                        .font(.lexiconLong(11.5).italic())
                                        .foregroundStyle(p.foreground.opacity(0.86))
                                        .multilineTextAlignment(.leading)
                                }
                                .padding(11)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .background(p.background.opacity(0.65))
                                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                                .overlay(RoundedRectangle(cornerRadius: 12, style: .continuous).stroke(p.border, lineWidth: 1))
                            }
                            .padding(16)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(p.card)
                            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                            .overlay(RoundedRectangle(cornerRadius: 18, style: .continuous).stroke(p.border, lineWidth: 1))
                        }
                        .buttonStyle(.plain)

                        HStack(spacing: 6) {
                            ForEach(0..<featured.count, id: \.self) { i in
                                Circle()
                                    .fill(i == dayIdx ? p.primary : p.border)
                                    .frame(width: i == dayIdx ? 7 : 5, height: i == dayIdx ? 7 : 5)
                                    .onTapGesture { dayIdx = i }
                            }
                        }
                        .frame(maxWidth: .infinity)
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 8)

                    Image(countrysideImageName("ill-hero-bridge", isDark: isDark))
                        .resizable()
                        .scaledToFit()
                        .frame(width: 182, height: 256, alignment: .topTrailing)
                        .offset(x: 6, y: 34)
                        .allowsHitTesting(false)
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
                                .scaledToFill()
                                .frame(width: 112, height: 84)
                                .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
                            Text(word.etymology)
                                .font(.lexiconLong(11.5))
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

                CountrysideQuoteBanner(
                    quote: "Words are bridges between minds.",
                    attribution: "Lexicon",
                    illustrationBase: "ill-banner-bridge"
                )
                .padding(.horizontal, 16)
            }
            .padding(.bottom, 88)
        }
        .background(p.background)
    }
}

// MARK: - Page 3: Word Detail (Full Word Profile)
struct WordDetailProfileScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme
    private var isDark: Bool { scheme == .dark }

    var body: some View {
        let p = lexiconPalette(scheme)
        let word = vm.detailWord
        let saved = vm.isSaved(word.id)

        ScrollView {
            VStack(spacing: 11) {
                ZStack(alignment: .topTrailing) {
                    Image(countrysideImageName("ill-hero-bridge", isDark: isDark))
                        .resizable()
                        .scaledToFit()
                        .frame(width: 192, height: 228, alignment: .topTrailing)
                        .offset(x: 6, y: 24)
                        .allowsHitTesting(false)

                    VStack(alignment: .leading, spacing: 6) {
                        HStack {
                            Button { vm.navigateBack() } label: {
                                Image(systemName: "arrow.left")
                                    .font(.system(size: 17, weight: .medium))
                                    .foregroundStyle(p.foreground)
                            }
                            .buttonStyle(.plain)
                            Spacer()
                            HStack(spacing: 12) {
                                Button { vm.toggleSave(word.id) } label: {
                                    Image(systemName: saved ? "bookmark.fill" : "bookmark")
                                        .font(.system(size: 17))
                                        .foregroundStyle(saved ? p.highlight : p.foreground)
                                }
                                .buttonStyle(.plain)
                                Image(systemName: "square.and.arrow.up")
                                    .font(.system(size: 16))
                                    .foregroundStyle(p.foreground)
                            }
                        }
                        .padding(.top, 6)

                        HStack(spacing: 4) {
                            BotanicalSprig(color: p.highlight).frame(width: 11, height: 11)
                            Text("Word of the Day")
                                .font(.lexiconBody(10))
                                .foregroundStyle(p.foreground)
                        }
                        .padding(.horizontal, 10)
                        .padding(.vertical, 4)
                        .background(p.card)
                        .clipShape(Capsule())
                        .overlay(Capsule().stroke(p.border, lineWidth: 1))

                        HStack(spacing: 8) {
                            Text(word.word)
                                .font(.lexiconDisplay(30))
                                .foregroundStyle(p.foreground)
                            Image(systemName: "speaker.wave.2")
                                .font(.system(size: 15))
                                .foregroundStyle(p.mutedForeground)
                        }

                        Text(word.phonetic)
                            .font(.lexiconLong(13.5).italic())
                            .foregroundStyle(p.mutedForeground)

                        Text(word.partOfSpeech.displayValue)
                            .font(.lexiconLong(11.5).italic())
                            .foregroundStyle(p.foreground)
                            .padding(.horizontal, 12)
                            .padding(.vertical, 4)
                            .background(p.highlight.opacity(0.20))
                            .clipShape(Capsule())

                        Text(word.definitions.first ?? "")
                            .font(.lexiconLong(13))
                            .foregroundStyle(p.foreground)
                            .frame(maxWidth: 215, alignment: .leading)
                    }
                    .padding(.horizontal, 16)
                }

                // 4 Metadata Cards
                HStack(spacing: 7) {
                    metaTile("book", "Language", word.language.components(separatedBy: " ").first ?? "English", p: p)
                    metaTile("globe", "Origin", word.originPeriod, p: p)
                    metaTile("star", "Frequency", word.frequency, p: p)
                    metaTile("leaf", "Level", word.level, p: p)
                }
                .padding(.horizontal, 16)

                // Meanings Card
                VStack(alignment: .leading, spacing: 9) {
                    HStack(spacing: 6) {
                        BotanicalSprig(color: p.highlight).frame(width: 14, height: 14)
                        Text("Meanings")
                            .font(.lexiconDisplay(16))
                            .foregroundStyle(p.foreground)
                    }
                    ForEach(Array(word.definitions.prefix(2).enumerated()), id: \.offset) { idx, def in
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
                .padding(14)
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

                // Side-by-side Etymology & Usage Examples
                HStack(spacing: 8) {
                    ZStack(alignment: .bottomTrailing) {
                        VStack(alignment: .leading, spacing: 5) {
                            HStack(spacing: 5) {
                                BotanicalSprig(color: p.highlight).frame(width: 13, height: 13)
                                Text("Etymology").font(.lexiconDisplay(14)).foregroundStyle(p.foreground)
                            }
                            Text(word.etymology)
                                .font(.lexiconLong(10))
                                .foregroundStyle(p.foreground.opacity(0.86))
                                .lineLimit(6)
                            Spacer(minLength: 0)
                        }
                        .padding(11)
                        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)

                        Image(countrysideImageName("ill-col-bookmarks", isDark: isDark))
                            .resizable()
                            .scaledToFit()
                            .frame(width: 54, height: 54)
                            .padding(6)
                    }
                    .frame(height: 138)
                    .background(p.card)
                    .clipShape(RoundedRectangle(cornerRadius: 15, style: .continuous))
                    .overlay(RoundedRectangle(cornerRadius: 15, style: .continuous).stroke(p.border, lineWidth: 1))

                    VStack(alignment: .leading, spacing: 6) {
                        HStack(spacing: 5) {
                            BotanicalSprig(color: p.highlight).frame(width: 13, height: 13)
                            Text("Usage Examples").font(.lexiconDisplay(14)).foregroundStyle(p.foreground)
                        }
                        Text("“\(word.examples.first ?? "")”")
                            .font(.lexiconLong(10.5).italic())
                            .foregroundStyle(p.foreground.opacity(0.88))
                            .lineLimit(3)
                        Rectangle().fill(p.border).frame(height: 1)
                        Text("“\(word.examples.last ?? "")”")
                            .font(.lexiconLong(10.5).italic())
                            .foregroundStyle(p.foreground.opacity(0.88))
                            .lineLimit(2)
                        Spacer(minLength: 0)
                    }
                    .padding(11)
                    .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
                    .frame(height: 138)
                    .background(p.card)
                    .clipShape(RoundedRectangle(cornerRadius: 15, style: .continuous))
                    .overlay(RoundedRectangle(cornerRadius: 15, style: .continuous).stroke(p.border, lineWidth: 1))
                }
                .padding(.horizontal, 16)

                // Progressive Expansion to Page 10 (36 Languages)
                Button {
                    vm.openWordExpanded(word)
                } label: {
                    HStack {
                        Image(systemName: "globe")
                            .foregroundStyle(p.primary)
                        Text("Explore Translations Across 36 Languages")
                            .font(.lexiconBody(12, weight: .medium))
                            .foregroundStyle(p.foreground)
                        Spacer()
                        Image(systemName: "arrow.right")
                            .font(.system(size: 12, weight: .medium))
                            .foregroundStyle(p.highlight)
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .background(p.card)
                    .clipShape(Capsule())
                    .overlay(Capsule().stroke(p.border, lineWidth: 1))
                }
                .buttonStyle(.plain)
                .padding(.horizontal, 16)

                if !word.didYouKnow.isEmpty {
                    GeometryReader { geo in
                        let w = geo.size.width
                        ZStack(alignment: .leading) {
                            p.card
                            HStack(spacing: 0) {
                                Spacer(minLength: 0)
                                Image(countrysideImageName("ill-banner-bay", isDark: isDark))
                                    .resizable()
                                    .scaledToFill()
                                    .frame(width: w * 0.48, height: 94)
                                    .clipped()
                            }
                            LinearGradient(
                                stops: [
                                    .init(color: p.card, location: 0.0),
                                    .init(color: p.card.opacity(0.94), location: 0.52),
                                    .init(color: .clear, location: 0.80),
                                ],
                                startPoint: .leading,
                                endPoint: .trailing
                            )
                            VStack(alignment: .leading, spacing: 4) {
                                HStack(spacing: 5) {
                                    Image(systemName: "lightbulb")
                                        .font(.system(size: 12))
                                        .foregroundStyle(p.highlight)
                                    Text("Did You Know?")
                                        .font(.lexiconDisplay(14))
                                        .foregroundStyle(p.foreground)
                                }
                                Text(word.didYouKnow)
                                    .font(.lexiconBody(10))
                                    .foregroundStyle(p.foreground.opacity(0.86))
                                    .lineLimit(3)
                            }
                            .frame(width: w * 0.63, alignment: .leading)
                            .padding(.leading, 13)
                        }
                    }
                    .frame(height: 94)
                    .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                    .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).stroke(p.border, lineWidth: 1))
                    .padding(.horizontal, 16)
                }
            }
            .padding(.bottom, 88)
        }
        .background(p.background)
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
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 6) {
                    ForEach(items, id: \.self) { item in
                        Text(item)
                            .font(.lexiconBody(11))
                            .foregroundStyle(p.foreground)
                            .padding(.horizontal, 11)
                            .padding(.vertical, 4.5)
                            .background(p.background.opacity(0.7))
                            .clipShape(Capsule())
                            .overlay(Capsule().stroke(p.border, lineWidth: 1))
                    }
                }
            }
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(p.card)
        .clipShape(RoundedRectangle(cornerRadius: 15, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 15, style: .continuous).stroke(p.border, lineWidth: 1))
    }
}

// MARK: - Page 9: Search Results ("Dictionary Search")
struct SearchResultsScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @EnvironmentObject private var theme: LexiconThemeState
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        let p = lexiconPalette(scheme)
        let matches = vm.searchWords(vm.searchQuery.isEmpty ? "serendipity" : vm.searchQuery, limit: 8)
        let primaryWord = matches.first

        ScrollView {
            VStack(spacing: 12) {
                CountrysideHeroHeader(
                    title: "Dictionary\nSearch",
                    subtitle: "Look up any word and explore its full meaning.",
                    illustrationBase: "ill-hero-cottage",
                    onHistoryClick: { vm.navigate(to: .searchDiscovery) },
                    onLogoToggleTheme: { theme.toggle() },
                    heroHeight: 146
                )

                CountrysideSearchPill(
                    text: $vm.searchQuery,
                    placeholder: "Search for a word...",
                    onSubmit: { vm.submitSearch(vm.searchQuery) }
                )
                .padding(.horizontal, 16)

                if let w = primaryWord {
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
                            Image(systemName: "speaker.wave.2")
                                .font(.system(size: 13))
                                .foregroundStyle(p.mutedForeground)
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

                CountrysideQuoteBanner(
                    quote: "Discover the beauty in every word.",
                    attribution: "Lexicon",
                    illustrationBase: "ill-banner-bridge"
                )
                .padding(.horizontal, 16)
            }
            .padding(.bottom, 88)
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
        let p = lexiconPalette(scheme)
        let word = vm.detailWord
        let translations = LexiconData.translationsFor(word)
        let expanded = showAll36 || vm.detailExpanded36

        ScrollView {
            VStack(spacing: 11) {
                CountrysideHeroHeader(
                    title: "Dictionary",
                    subtitle: "Look up any word, anytime.",
                    illustrationBase: "ill-hero-cottage",
                    onHistoryClick: { showAll36.toggle() },
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

                CountrysideQuoteBanner(
                    quote: "Words are doors to new worlds.",
                    attribution: "Lexicon",
                    illustrationBase: "ill-books-daisies"
                )
                .padding(.horizontal, 16)

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
                                Text("\(word.word) · 36 Languages")
                                    .font(.lexiconDisplay(15))
                                    .foregroundStyle(p.foreground)
                                Text("\(word.phonetic) · Tap to \(expanded ? "collapse" : "expand all 36 translations")")
                                    .font(.lexiconBody(10))
                                    .foregroundStyle(p.mutedForeground)
                            }
                            Spacer()
                            Text(expanded ? "Collapse" : "Expand 36 →")
                                .font(.lexiconBody(11, weight: .medium))
                                .foregroundStyle(p.primary)
                        }
                    }
                    .buttonStyle(.plain)

                    let slice = expanded ? translations : Array(translations.prefix(4))
                    LazyVGrid(columns: [GridItem(.flexible(), spacing: 7), GridItem(.flexible(), spacing: 7)], spacing: 7) {
                        ForEach(slice) { tr in
                            VStack(alignment: .leading, spacing: 2) {
                                Text("\(tr.language) (\(tr.code.uppercased()))")
                                    .font(.lexiconBody(9))
                                    .foregroundStyle(p.mutedForeground)
                                Text(tr.translated)
                                    .font(.lexiconDisplay(12))
                                    .foregroundStyle(p.foreground)
                                    .lineLimit(1)
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
                        ForEach(["serendipity", "nostalgia", "eloquence"], id: \.self) { id in
                            if let entry = LexiconData.getWordById(id) {
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
                    .scaledToFill()
                    .frame(height: 72)
                    .clipShape(RoundedRectangle(cornerRadius: 15, style: .continuous))
                    .overlay(RoundedRectangle(cornerRadius: 15, style: .continuous).stroke(p.border, lineWidth: 1))
                    .padding(.horizontal, 16)
            }
            .padding(.bottom, 88)
        }
        .background(p.background)
    }
}
