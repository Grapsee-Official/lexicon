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
    var body: some View { IllustratedDailyWordScreen() }
}

struct WordDetailProfileScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @State private var pageWidth: CGFloat = 390
    private var enlarged: Bool { pageWidth < 310 || dynamicTypeSize >= .xxLarge }
    var body: some View {
        let p = lexiconPalette(scheme)
        let word = vm.detailWord
        let ink = scheme == .dark ? p.foreground : Color(hex: "243B37")
        let rule = scheme == .dark ? p.border : LexiconColors.highlight.opacity(0.34)
        ScrollView {
            VStack(alignment: .leading, spacing: 9) {
                CountrysideHeroHeader(title: "Dictionary", subtitle: "", illustrationBase: "ill-hero-cottage",
                    onHistoryClick: { vm.navigate(to: .searchDiscovery) }, onBack: { vm.navigateBack() })
                VStack(alignment: .leading, spacing: 7) {
                    let artHeight = min(250, max(120, (pageWidth - 36) * 0.45))
                    ZStack(alignment: .trailing) {
                        if !enlarged {
                            Image(countrysideImageName("ill-thumb-meadow", isDark: scheme == .dark, scene: "detail-meadow"))
                                .resizable().scaledToFit()
                                .frame(width: (pageWidth - 36) * 0.56, height: artHeight, alignment: .bottomTrailing)
                                .accessibilityHidden(true)
                            let paper = scheme == .dark ? p.background : Color(hex: "FFF1D9")
                            LinearGradient(stops: [.init(color: paper.opacity(0.45), location: 0),
                                .init(color: paper.opacity(0.8), location: 0.56), .init(color: .clear, location: 1)],
                                startPoint: .leading, endPoint: .trailing)
                                .frame(width: (pageWidth - 36) * 0.62, height: artHeight)
                                .frame(maxWidth: .infinity, alignment: .leading).allowsHitTesting(false)
                        }
                        VStack(alignment: .leading, spacing: 7) {
                            Text(word.word).font(.lexiconDisplay(min(49, max(27, (pageWidth - 36) * 0.096))))
                                .foregroundStyle(ink).fixedSize(horizontal: false, vertical: true)
                            HStack(spacing: 7) {
                                Text(word.phonetic).font(.lexiconLong(15)).foregroundStyle(ink)
                                    .fixedSize(horizontal: false, vertical: true)
                                Button { vm.pronounce(word.word) } label: {
                                    Image(systemName: "speaker.wave.2").foregroundStyle(p.primary)
                                        .frame(width: 44, height: 44).background(p.card, in: Circle())
                                }.buttonStyle(.plain).accessibilityLabel("Pronounce word")
                                Button { vm.toggleSave(word.id) } label: {
                                    Image(systemName: vm.isSaved(word.id) ? "bookmark.fill" : "bookmark")
                                        .foregroundStyle(p.primary).frame(width: 44, height: 44)
                                }.buttonStyle(.plain).accessibilityLabel("Bookmark word")
                            }
                            Text(word.partOfSpeech.displayValue).font(.lexiconLong(13).italic())
                                .foregroundStyle(scheme == .dark ? LexiconColors.highlightDark : .white)
                                .padding(.horizontal, 14).padding(.vertical, 5)
                                .background(scheme == .dark ? p.card : LexiconColors.primary, in: Capsule())
                            Text(word.definitions.first ?? "").font(.lexiconLong(15)).foregroundStyle(ink)
                                .fixedSize(horizontal: false, vertical: true)
                                .frame(maxWidth: enlarged ? .infinity : (pageWidth - 36) * 0.56, alignment: .leading)
                            if enlarged {
                                Image(countrysideImageName("ill-thumb-meadow", isDark: scheme == .dark, scene: "detail-meadow"))
                                    .resizable().scaledToFit().frame(height: artHeight).accessibilityHidden(true)
                            }
                        }.frame(maxWidth: .infinity, alignment: .leading)
                    }.frame(minHeight: artHeight)
                    HStack(spacing: 7) {
                        Rectangle().fill(rule).frame(height: 1)
                        BotanicalSprig(color: p.primary).frame(width: 19, height: 19)
                        Rectangle().fill(rule).frame(height: 1)
                    }
                }.padding(.horizontal, 18)
                let sections = [("Synonyms", word.synonyms.joined(separator: ", ")),
                    ("Antonyms", word.antonyms.joined(separator: ", ")), ("Etymology", word.etymology),
                    ("Meanings", word.definitions.dropFirst().joined(separator: "\n"))]
                ForEach(sections.indices, id: \.self) { i in
                    if !sections[i].1.isEmpty {
                        readingCard(sections[i].0, sections[i].1, p: p, ink: ink, rule: rule)
                    }
                }
                if !word.examples.isEmpty {
                    BotanicalSectionTitle(title: "Usage Examples", actionLabel: nil)
                    ZStack(alignment: .bottomTrailing) {
                        if !enlarged {
                            Image(countrysideImageName("ill-books-daisies", isDark: scheme == .dark, scene: "study-books"))
                                .resizable().scaledToFit().frame(width: (pageWidth - 36) * 0.43, height: 126)
                                .accessibilityHidden(true)
                        }
                        VStack(spacing: 6) {
                            ForEach(Array(word.examples.enumerated()), id: \.offset) { _, example in
                                HStack(alignment: .top, spacing: 6) {
                                    Text("“").font(.lexiconDisplay(22)).foregroundStyle(p.primary)
                                    Text(example).font(.lexiconLong(13)).foregroundStyle(ink)
                                        .fixedSize(horizontal: false, vertical: true)
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                }.padding(11).background(p.card)
                                    .clipShape(RoundedRectangle(cornerRadius: 14))
                                    .overlay(RoundedRectangle(cornerRadius: 14).stroke(rule, lineWidth: 1))
                            }
                        }
                        .frame(maxWidth: enlarged ? .infinity : (pageWidth - 36) * 0.60)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    }.padding(.horizontal, 18)
                }
                Button { vm.openWordExpanded(word) } label: {
                    HStack {
                        Text("Explore translations across 36 languages").font(.lexiconBody(12))
                        Spacer()
                        Image(systemName: "arrow.right")
                    }.foregroundStyle(ink).padding(12)
                        .overlay(RoundedRectangle(cornerRadius: 14).stroke(rule, lineWidth: 1))
                }.buttonStyle(.plain).padding(.horizontal, 18)
                CountrysideQuoteBanner(quote: "Sometimes, the best things in life are found by chance.", illustrationBase: "ill-banner-church")
                    .padding(.horizontal, 18)
            }.padding(.bottom, 16)
        }.background { GeometryReader { geo in
            Color.clear.onAppear { pageWidth = geo.size.width }.onChange(of: geo.size.width) { pageWidth = $0 }
        } }
    }
    private func readingCard(_ title: String?, _ body: String, p: LexiconPalette, ink: Color, rule: Color) -> some View {
        HStack(alignment: .top, spacing: 9) {
            BotanicalSprig(color: p.highlight).frame(width: 19, height: 19).accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 5) {
                if let title { Text(title).font(.lexiconDisplay(17)) }
                Text(body).font(.lexiconLong(13)).fixedSize(horizontal: false, vertical: true)
            }.foregroundStyle(ink).frame(maxWidth: .infinity, alignment: .leading)
        }.padding(13).background(scheme == .dark ? p.card : Color(hex: "FFF2DF").opacity(0.72))
            .clipShape(RoundedRectangle(cornerRadius: 14))
            .overlay(RoundedRectangle(cornerRadius: 14).stroke(rule, lineWidth: 1))
            .padding(.horizontal, 18)
    }
}

private struct IllustratedDailyWordScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    private var isDark: Bool { scheme == .dark }
    @State private var pageWidth: CGFloat = 390
    private var enlarged: Bool { dynamicTypeSize >= .xxLarge || pageWidth < 300 }
    private var artHeight: CGFloat { min(320, max(150, pageWidth * 440 / 832)) }

    var body: some View {
        let p = lexiconPalette(scheme)
        let word = vm.detailWord
        let saved = vm.isSaved(word.id)

        ScrollView {
            VStack(spacing: 11) {
                ZStack(alignment: .topTrailing) {
                    if !enlarged {
                        Image(countrysideImageName("ill-hero-bridge", isDark: isDark, scene: "daily-bridge"))
                            .resizable().scaledToFit()
                            .frame(width: pageWidth * 0.72, height: artHeight, alignment: .topTrailing)
                            .accessibilityHidden(true).allowsHitTesting(false)
                        let paper = isDark ? p.background : Color(hex: "FFF1D9")
                        LinearGradient(stops: [.init(color: paper, location: 0),
                            .init(color: paper.opacity(0.82), location: 0.52),
                            .init(color: .clear, location: 1)], startPoint: .leading, endPoint: .trailing)
                            .frame(width: pageWidth * 0.65, height: artHeight)
                            .frame(maxWidth: .infinity, alignment: .leading).allowsHitTesting(false)
                    }

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
                                ShareLink(item: word.word + " — " + (word.definitions.first ?? "")) {
                                    Image(systemName: "square.and.arrow.up").font(.system(size: 16)).foregroundStyle(p.foreground)
                                }.accessibilityLabel("Share word")
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
                                .font(.lexiconDisplay(min(44, max(24, pageWidth * 62 / 832))))
                                .fixedSize(horizontal: false, vertical: true)
                                .foregroundStyle(p.foreground)
                            Button { vm.pronounce(word.word) } label: {
                                Image(systemName: "speaker.wave.2")
                                    .font(.system(size: 17))
                                    .foregroundStyle(p.mutedForeground)
                                    .frame(width: 34, height: 34)
                            }
                            .buttonStyle(.plain)
                            .accessibilityLabel("Pronounce word")
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
                            .frame(maxWidth: enlarged ? .infinity : pageWidth * 0.55, alignment: .leading)
                            .fixedSize(horizontal: false, vertical: true)
                        if enlarged {
                            Image(countrysideImageName("ill-hero-bridge", isDark: isDark, scene: "daily-bridge"))
                                .resizable().scaledToFit().frame(height: artHeight).accessibilityHidden(true)
                        }
                    }
                    .padding(.horizontal, 16)
                }
                .frame(minHeight: enlarged ? 0 : artHeight, alignment: .top)

                // 4 Metadata Cards
                LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 7), count: enlarged ? 2 : 4), spacing: 7) {
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
                    ForEach(Array((word.definitions.count > 1 ? Array(word.definitions.dropFirst()) : word.definitions).enumerated()), id: \.offset) { idx, def in
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

                etymologyUsageCards(word: word, p: p)

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
                    VStack(alignment: .leading, spacing: 5) {
                        HStack(spacing: 5) {
                            Image(systemName: "lightbulb").foregroundStyle(p.highlight)
                            Text("Did You Know?").font(.lexiconDisplay(14))
                        }
                        Text(word.didYouKnow).font(.lexiconBody(11))
                            .fixedSize(horizontal: false, vertical: true)
                            .frame(maxWidth: enlarged ? .infinity : pageWidth * 0.54, alignment: .leading)
                    }
                    .foregroundStyle(p.foreground).padding(13)
                    .frame(maxWidth: .infinity, minHeight: 94, alignment: .leading)
                    .background(alignment: .trailing) {
                        if !enlarged {
                            Image(countrysideImageName("ill-banner-bay", isDark: isDark, scene: "daily-bay"))
                                .resizable().scaledToFit().accessibilityHidden(true)
                        }
                    }
                    .background(p.card)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                    .overlay(RoundedRectangle(cornerRadius: 16).stroke(p.border, lineWidth: 1))
                    .padding(.horizontal, 16)
                }
            }
            .padding(.bottom, 16)
        }
        .background { GeometryReader { geo in
            Color.clear.onAppear { pageWidth = geo.size.width }.onChange(of: geo.size.width) { pageWidth = $0 }
        } }
    }


    // A separate view expression keeps the large word-detail body tractable
    // for Swift's type checker. Reserve space for the complete watercolor
    // below the etymology copy rather than cutting the painting or text.
    private func etymologyUsageCards(word: WordEntry, p: LexiconPalette) -> some View {
        let layout = enlarged || pageWidth < 324 ? AnyLayout(VStackLayout(spacing: 8)) : AnyLayout(HStackLayout(alignment: .top, spacing: 8))
        return layout {
            VStack(alignment: .leading, spacing: 6) {
                HStack(spacing: 5) {
                    BotanicalSprig(color: p.highlight).frame(width: 15, height: 15)
                    Text("Etymology").font(.lexiconDisplay(15))
                }
                Text(word.etymology.isEmpty ? "Origin information is not available." : word.etymology)
                    .font(.lexiconLong(12)).fixedSize(horizontal: false, vertical: true)
                Image(countrysideImageName("ill-books-daisies", isDark: isDark, scene: "study-books"))
                    .resizable().scaledToFit().frame(height: 68)
                    .frame(maxWidth: .infinity, alignment: .trailing).accessibilityHidden(true)
            }
            .padding(11).frame(maxWidth: .infinity, alignment: .leading)
            .foregroundStyle(p.foreground).background(p.card)
            .clipShape(RoundedRectangle(cornerRadius: 15))
            .overlay(RoundedRectangle(cornerRadius: 15).stroke(p.border, lineWidth: 1))
            VStack(alignment: .leading, spacing: 10) {
                HStack(spacing: 5) {
                    BotanicalSprig(color: p.highlight).frame(width: 15, height: 15)
                    Text("Usage Examples").font(.lexiconDisplay(15))
                }
                ForEach(Array(word.examples.enumerated()), id: \.offset) { index, example in
                    if index > 0 { Rectangle().fill(p.border).frame(height: 1) }
                    Text("“\(example)”").font(.lexiconLong(12).italic())
                        .fixedSize(horizontal: false, vertical: true)
                }
                if word.examples.isEmpty {
                    Text("No usage examples available.").font(.lexiconBody(11)).foregroundStyle(p.mutedForeground)
                }
            }
            .padding(11).frame(maxWidth: .infinity, alignment: .leading)
            .foregroundStyle(p.foreground).background(p.card)
            .clipShape(RoundedRectangle(cornerRadius: 15))
            .overlay(RoundedRectangle(cornerRadius: 15).stroke(p.border, lineWidth: 1))
        }.padding(.horizontal, 16)
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
                .multilineTextAlignment(.center)
                .fixedSize(horizontal: false, vertical: true)
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
        let matches = vm.searchWords(vm.searchQuery, limit: 8)
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
                                    .fixedSize(horizontal: false, vertical: true)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                            }
                            .buttonStyle(.plain)
                            .layoutPriority(1)
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
                                    .frame(width: 44, height: 44).contentShape(Rectangle())
                            }
                            .buttonStyle(.plain)
                            .accessibilityLabel("Pronounce word")
                        }

                        HStack(spacing: 8) {
                            Rectangle().fill(p.border).frame(height: 1)
                            BotanicalSprig(color: p.highlight).frame(width: 14, height: 14)
                            Rectangle().fill(p.border).frame(height: 1)
                        }

                        Text(w.definitions.first ?? "No definition available yet.")
                            .font(.lexiconLong(15))
                            .fixedSize(horizontal: false, vertical: true)
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

                                ResultWordWrap(spacing: 6) {
                                    ForEach(w.relatedWords, id: \.self) { rw in
                                        Button { vm.submitSearch(rw) } label: {
                                            Text(rw).font(.lexiconBody(10.5))
                                                .foregroundStyle(p.foreground)
                                                .padding(.horizontal, 11).padding(.vertical, 6)
                                                .frame(minHeight: 32)
                                                .background(p.primary.opacity(0.18), in: Capsule())
                                        }.buttonStyle(.plain)
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
                            .frame(minHeight: 44)
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
                        Text(vm.searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Search the dictionary" : "No matching entries")
                            .font(.lexiconDisplay(18))
                            .foregroundStyle(p.foreground)
                        Text(vm.searchQuery.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty ? "Enter a word above to discover its meaning." : "Try another spelling, language or filter.")
                            .font(.lexiconBody(12))
                            .foregroundStyle(p.mutedForeground)
                    }
                    .padding(22)
                    .frame(maxWidth: .infinity)
                    .background(p.card)
                    .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                    .padding(.horizontal, 16)
                }

                if vm.searchScopeTab != "Idioms" && matches.count > 1 {
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

                CountrysideQuoteBanner(
                    quote: "Discover the beauty in every word.",
                    attribution: "Lexora",
                    illustrationBase: "ill-banner-bridge"
                )
                .padding(.horizontal, 16)
            }
            .padding(.bottom, 16)
        }
    }

    private func resultRow(title: String, value: String, p: LexiconPalette, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: 8) {
                BotanicalSprig(color: p.highlight).frame(width: 15, height: 15)
                VStack(alignment: .leading, spacing: 2) {
                    Text(title).font(.lexiconDisplay(14.5)).foregroundStyle(p.foreground)
                    Text(value)
                        .font(.lexiconLong(12))
                        .foregroundStyle(p.mutedForeground)
                        .fixedSize(horizontal: false, vertical: true)
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

// Wrap real related-word controls onto the next line instead of hiding them off-screen.
private struct ResultWordWrap: Layout {
    var spacing: CGFloat = 6

    private func arrange(width: CGFloat, subviews: Subviews) -> (CGSize, [CGPoint]) {
        var x: CGFloat = 0, y: CGFloat = 0, rowHeight: CGFloat = 0
        var positions: [CGPoint] = []
        var used: CGFloat = 0
        for view in subviews {
            let size = view.sizeThatFits(ProposedViewSize(width: width, height: nil))
            if x > 0 && x + size.width > width {
                y += rowHeight + spacing
                x = 0
                rowHeight = 0
            }
            positions.append(CGPoint(x: x, y: y))
            used = max(used, x + size.width)
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
        return (CGSize(width: used, height: y + rowHeight), positions)
    }
    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        arrange(width: max(1, proposal.width ?? 320), subviews: subviews).0
    }
    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        let width = max(1, bounds.width)
        let (_, positions) = arrange(width: width, subviews: subviews)
        for (index, view) in subviews.enumerated() {
            let size = view.sizeThatFits(ProposedViewSize(width: width, height: nil))
            view.place(at: CGPoint(x: bounds.minX + positions[index].x, y: bounds.minY + positions[index].y),
                anchor: .topLeading, proposal: ProposedViewSize(size))
        }
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
                    attribution: "Lexora",
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

                Image(countrysideImageName("ill-banner-church", isDark: isDark, scene: "dictionary-panorama"))
                    .resizable()
                    .scaledToFit()
                    .frame(height: 72)
                    .clipShape(RoundedRectangle(cornerRadius: 15, style: .continuous))
                    .overlay(RoundedRectangle(cornerRadius: 15, style: .continuous).stroke(p.border, lineWidth: 1))
                    .padding(.horizontal, 16)
            }
            .padding(.bottom, 16)
        }
        .background(p.background)
        .onAppear { showAll36 = vm.detailExpanded36 }
    }
}
