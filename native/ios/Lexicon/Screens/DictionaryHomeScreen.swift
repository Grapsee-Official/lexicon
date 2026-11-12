//
//  DictionaryHomeScreen.swift
//  Lexicon
//

import SwiftUI

struct DictionaryHomeScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @EnvironmentObject private var theme: LexiconThemeState
    @Environment(\.colorScheme) private var scheme

    @State private var draftQuery: String = ""
    private var isDark: Bool { scheme == .dark }
    private let solitude = LexiconData.solitudeEntry
    private let popularIds = ["serendipity", "limerence", "ephemeral", "mellifluous", "quixotic"]

    var body: some View {
        let p = lexiconPalette(scheme)

        ZStack {
            ScrollView {
                VStack(spacing: 11) {
                    CountrysideHeroHeader(
                        title: "Dictionary",
                        subtitle: "Every word has a world.",
                        illustrationBase: "ill-hero-books",
                        onHistoryClick: { vm.navigate(to: .expandedExploration) },
                        onLogoToggleTheme: { theme.toggle() },
                        heroHeight: 132
                    )

                    CountrysideSearchPill(
                        text: $draftQuery,
                        placeholder: "Search for a word...",
                        onSubmit: {
                            if !draftQuery.trimmingCharacters(in: .whitespaces).isEmpty {
                                vm.submitSearch(draftQuery)
                            } else {
                                vm.navigate(to: .searchDiscovery)
                            }
                        },
                        showCameraMic: true
                    )
                    .padding(.horizontal, 16)

                    solitudeWotdCard(p: p)
                        .padding(.horizontal, 16)

                    // Explore Words (6 botanical miniature cards)
                    VStack(spacing: 7) {
                        BotanicalSectionTitle(
                            title: "Explore Words",
                            sprigAfterTitle: true,
                            actionLabel: "View all",
                            onAction: { vm.navigate(to: .expandedExploration) }
                        )
                        ScrollView(.horizontal, showsIndicators: false) {
                            HStack(spacing: 7) {
                                ForEach(LexiconData.exploreTopics) { topic in
                                    Button {
                                        switch topic.id {
                                        case "languages":
                                            vm.openWordExpanded(LexiconData.serendipityEntry)
                                        case "phrases", "idioms":
                                            vm.searchScopeTab = "Idioms"
                                            vm.navigate(to: .searchDiscovery)
                                        case "examples":
                                            vm.navigate(to: .wordOfTheDay)
                                        default:
                                            vm.navigate(to: .expandedExploration)
                                        }
                                    } label: {
                                        VStack(spacing: 3) {
                                            Image(countrysideImageName(topic.illustration, isDark: isDark))
                                                .resizable()
                                                .scaledToFit()
                                                .frame(width: 38, height: 38)
                                            Text(topic.title)
                                                .font(.lexiconDisplay(10, weight: .medium))
                                                .foregroundStyle(p.foreground)
                                                .lineLimit(1)
                                            Text(topic.subtitle)
                                                .font(.lexiconBody(7))
                                                .foregroundStyle(p.mutedForeground)
                                                .lineLimit(1)
                                                .minimumScaleFactor(0.8)
                                        }
                                        .padding(.horizontal, 5)
                                        .padding(.vertical, 7)
                                        .frame(width: 72)
                                        .background(p.card)
                                        .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 12, style: .continuous)
                                                .stroke(p.border, lineWidth: 1)
                                        )
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                            .padding(.horizontal, 16)
                        }
                    }

                    // World of Words — Rudyard Kipling teacup card
                    VStack(spacing: 7) {
                        BotanicalSectionTitle(title: "World of Words", sprigAfterTitle: true, actionLabel: nil)
                        Button {
                            vm.navigate(to: .myNotes)
                        } label: {
                            GeometryReader { geo in
                                let w = geo.size.width
                                ZStack(alignment: .leading) {
                                    p.card
                                    HStack(spacing: 0) {
                                        Spacer(minLength: 0)
                                        Image(countrysideImageName("ill-teacup-books", isDark: isDark))
                                            .resizable()
                                            .scaledToFill()
                                            .frame(width: w * 0.52, height: 110)
                                            .clipped()
                                    }
                                    VStack(alignment: .leading, spacing: 5) {
                                        Text("“Words are the most powerful drug used by mankind.”")
                                            .font(.lexiconDisplay(12.5))
                                            .foregroundStyle(p.foreground)
                                            .lineLimit(3)
                                            .multilineTextAlignment(.leading)
                                        Text("– Rudyard Kipling")
                                            .font(.lexiconBody(9.5))
                                            .foregroundStyle(p.mutedForeground)
                                        HStack(spacing: 4) {
                                            Text("Explore quotes")
                                            Image(systemName: "arrow.right")
                                                .font(.system(size: 9.5, weight: .medium))
                                        }
                                        .font(.lexiconBody(10, weight: .medium))
                                        .foregroundStyle(p.primary)
                                    }
                                    .frame(width: w * 0.55, alignment: .leading)
                                    .padding(.leading, 14)
                                }
                            }
                            .frame(height: 110)
                            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                            .overlay(
                                RoundedRectangle(cornerRadius: 16, style: .continuous)
                                    .stroke(p.border, lineWidth: 1)
                            )
                        }
                        .buttonStyle(.plain)
                        .padding(.horizontal, 16)
                    }

                    // Dual columns: Recently Viewed & Popular Words
                    HStack(alignment: .top, spacing: 8) {
                        VStack(alignment: .leading, spacing: 6) {
                            HStack {
                                Text("Recently Viewed")
                                    .font(.lexiconDisplay(13))
                                    .foregroundStyle(p.foreground)
                                Spacer()
                                Button("View all →") { vm.navigate(to: .expandedExploration) }
                                    .font(.lexiconBody(8.5))
                                    .foregroundStyle(p.mutedForeground)
                                    .buttonStyle(.plain)
                            }
                            VStack(spacing: 0) {
                                ForEach(Array(vm.recentlyViewedIds.prefix(5).enumerated()), id: \.offset) { idx, id in
                                    if let w = LexiconData.getWordById(id) {
                                        if idx > 0 { Rectangle().fill(p.border).frame(height: 1) }
                                        Button { vm.openWord(w) } label: {
                                            HStack(spacing: 5) {
                                                BotanicalSprig(color: p.highlight)
                                                    .frame(width: 11, height: 11)
                                                Text(w.word)
                                                    .font(.lexiconDisplay(11))
                                                    .foregroundStyle(p.foreground)
                                                    .lineLimit(1)
                                                    .frame(maxWidth: .infinity, alignment: .leading)
                                                Text(w.partOfSpeech.displayValue)
                                                    .font(.lexiconBody(8))
                                                    .foregroundStyle(p.mutedForeground)
                                                Image(systemName: "clock.arrow.circlepath")
                                                    .font(.system(size: 9.5))
                                                    .foregroundStyle(p.mutedForeground)
                                            }
                                            .padding(.horizontal, 8)
                                            .padding(.vertical, 6.5)
                                        }
                                        .buttonStyle(.plain)
                                    }
                                }
                            }
                            .background(p.card)
                            .clipShape(RoundedRectangle(cornerRadius: 13, style: .continuous))
                            .overlay(RoundedRectangle(cornerRadius: 13, style: .continuous).stroke(p.border, lineWidth: 1))
                        }

                        VStack(alignment: .leading, spacing: 6) {
                            HStack {
                                Text("Popular Words")
                                    .font(.lexiconDisplay(13))
                                    .foregroundStyle(p.foreground)
                                Spacer()
                                Button("View all →") { vm.navigate(to: .searchDiscovery) }
                                    .font(.lexiconBody(8.5))
                                    .foregroundStyle(p.mutedForeground)
                                    .buttonStyle(.plain)
                            }
                            VStack(spacing: 0) {
                                ForEach(Array(popularIds.enumerated()), id: \.offset) { idx, id in
                                    if let w = LexiconData.getWordById(id) {
                                        if idx > 0 { Rectangle().fill(p.border).frame(height: 1) }
                                        HStack(spacing: 5) {
                                            Text("\(idx + 1)")
                                                .font(.lexiconBody(8, weight: .medium))
                                                .foregroundStyle(p.foreground)
                                                .frame(width: 14, height: 14)
                                                .background(p.highlight.opacity(0.22))
                                                .clipShape(Circle())
                                            Button { vm.openWord(w) } label: {
                                                Text(w.word)
                                                    .font(.lexiconDisplay(11))
                                                    .foregroundStyle(p.foreground)
                                                    .lineLimit(1)
                                                    .frame(maxWidth: .infinity, alignment: .leading)
                                            }
                                            .buttonStyle(.plain)
                                            Button { vm.toggleSave(w.id) } label: {
                                                Image(systemName: vm.isSaved(w.id) ? "bookmark.fill" : "bookmark")
                                                    .font(.system(size: 10))
                                                    .foregroundStyle(vm.isSaved(w.id) ? p.highlight : p.mutedForeground)
                                            }
                                            .buttonStyle(.plain)
                                        }
                                        .padding(.horizontal, 8)
                                        .padding(.vertical, 6.5)
                                    }
                                }
                            }
                            .background(p.card)
                            .clipShape(RoundedRectangle(cornerRadius: 13, style: .continuous))
                            .overlay(RoundedRectangle(cornerRadius: 13, style: .continuous).stroke(p.border, lineWidth: 1))
                        }
                    }
                    .padding(.horizontal, 16)

                    // Random Word + Shuffle + Word Builder ('A' tile)
                    HStack(spacing: 6) {
                        Button { vm.discoverRandomWord() } label: {
                            HStack(spacing: 6) {
                                Image(countrysideImageName("ill-random-vase", isDark: isDark))
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 38, height: 38)
                                VStack(alignment: .leading, spacing: 1) {
                                    Text("Random Word")
                                        .font(.lexiconDisplay(11.5, weight: .medium))
                                        .foregroundStyle(p.foreground)
                                    Text("Discover a random word and expand your vocabulary.")
                                        .font(.lexiconBody(7.8))
                                        .foregroundStyle(p.mutedForeground)
                                        .lineLimit(2)
                                }
                            }
                            .frame(maxWidth: .infinity, alignment: .leading)
                        }
                        .buttonStyle(.plain)

                        Button { vm.discoverRandomWord() } label: {
                            Image(systemName: "shuffle")
                                .font(.system(size: 15, weight: .medium))
                                .foregroundStyle(isDark ? LexiconColors.backgroundDark : .white)
                                .frame(width: 36, height: 36)
                                .background(isDark ? LexiconColors.accentDark : LexiconColors.primary)
                                .clipShape(Circle())
                        }
                        .buttonStyle(.plain)

                        Button { vm.wordBuilderOpen = true } label: {
                            HStack(spacing: 6) {
                                VStack(alignment: .leading, spacing: 1) {
                                    Text("Word Builder")
                                        .font(.lexiconDisplay(11.5, weight: .medium))
                                        .foregroundStyle(p.foreground)
                                    Text("Learn words through roots, prefixes and suffixes.")
                                        .font(.lexiconBody(7.8))
                                        .foregroundStyle(p.mutedForeground)
                                        .lineLimit(2)
                                }
                                .frame(maxWidth: .infinity, alignment: .leading)
                                Image(countrysideImageName("ill-builder-tile", isDark: isDark))
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 36, height: 36)
                            }
                        }
                        .buttonStyle(.plain)
                    }
                    .padding(10)
                    .background(p.card)
                    .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                    .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).stroke(p.border, lineWidth: 1))
                    .padding(.horizontal, 16)
                }
                .padding(.bottom, 86)
            }
            .background(p.background)

            if vm.wordBuilderOpen {
                Color.black.opacity(0.45)
                    .ignoresSafeArea()
                    .onTapGesture { vm.wordBuilderOpen = false }
                VStack(alignment: .leading, spacing: 10) {
                    HStack {
                        Text("Word Builder · Classical Roots")
                            .font(.lexiconDisplay(17))
                            .foregroundStyle(p.foreground)
                        Spacer()
                        Button { vm.wordBuilderOpen = false } label: {
                            Image(systemName: "xmark")
                                .foregroundStyle(p.mutedForeground)
                        }
                        .buttonStyle(.plain)
                    }
                    ForEach(LexiconData.morphemeRoots) { mr in
                        VStack(alignment: .leading, spacing: 3) {
                            HStack(spacing: 6) {
                                Text(mr.root)
                                    .font(.lexiconDisplay(13, weight: .semibold))
                                    .foregroundStyle(p.primary)
                                Text("(\(mr.origin) — “\(mr.meaning)”)")
                                    .font(.lexiconBody(10.5).italic())
                                    .foregroundStyle(p.mutedForeground)
                            }
                            Text(mr.derivatives.joined(separator: " · "))
                                .font(.lexiconBody(11))
                                .foregroundStyle(p.foreground)
                        }
                        .padding(9)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(p.background.opacity(0.7))
                        .clipShape(RoundedRectangle(cornerRadius: 11, style: .continuous))
                        .overlay(RoundedRectangle(cornerRadius: 11, style: .continuous).stroke(p.border, lineWidth: 1))
                    }
                }
                .padding(18)
                .background(p.card)
                .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
                .overlay(RoundedRectangle(cornerRadius: 20, style: .continuous).stroke(p.border, lineWidth: 1))
                .padding(.horizontal, 24)
            }
        }
    }

    @ViewBuilder
    private func solitudeWotdCard(p: LexiconPalette) -> some View {
        let saved = vm.isSaved(solitude.id)
        GeometryReader { geo in
            let w = geo.size.width
            ZStack(alignment: .topTrailing) {
                Button {
                    vm.navigate(to: .wordOfTheDay, word: solitude)
                } label: {
                    ZStack(alignment: .leading) {
                        p.card
                        HStack(spacing: 0) {
                            Spacer(minLength: 0)
                            Image(countrysideImageName("ill-wotd-solitude", isDark: isDark))
                                .resizable()
                                .scaledToFill()
                                .frame(width: w * 0.62, height: 148)
                                .clipped()
                        }
                        LinearGradient(
                            stops: [
                                .init(color: p.card, location: 0.0),
                                .init(color: p.card.opacity(0.95), location: 0.42),
                                .init(color: .clear, location: 0.68),
                            ],
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                        VStack(alignment: .leading, spacing: 3) {
                            HStack(spacing: 4) {
                                Text("WORD OF THE DAY")
                                    .font(.lexiconBody(8.5, weight: .medium))
                                    .tracking(0.6)
                                    .foregroundStyle(p.highlight)
                                BotanicalSprig(color: p.highlight)
                                    .frame(width: 12, height: 12)
                            }
                            HStack(spacing: 5) {
                                Text(solitude.word)
                                    .font(.lexiconDisplay(22))
                                    .foregroundStyle(p.foreground)
                                Image(systemName: "speaker.wave.2")
                                    .font(.system(size: 12))
                                    .foregroundStyle(p.mutedForeground)
                            }
                            Text(solitude.phonetic)
                                .font(.lexiconBody(10.5))
                                .foregroundStyle(p.mutedForeground)
                            Text(solitude.partOfSpeech.displayValue)
                                .font(.lexiconBody(10.5, weight: .medium))
                                .foregroundStyle(p.foreground.opacity(0.85))
                            Text(solitude.definitions.first ?? "")
                                .font(.lexiconBody(10.5))
                                .foregroundStyle(p.foreground.opacity(0.85))
                                .lineLimit(2)
                                .frame(width: w * 0.48, alignment: .leading)
                            Spacer(minLength: 2)
                            HStack(spacing: 4) {
                                Text("View details")
                                Image(systemName: "arrow.right")
                                    .font(.system(size: 9.5, weight: .medium))
                            }
                            .font(.lexiconBody(10.5, weight: .medium))
                            .foregroundStyle(p.primary)
                        }
                        .padding(.leading, 15)
                        .padding(.vertical, 12)
                    }
                }
                .buttonStyle(.plain)

                Button {
                    vm.toggleSave(solitude.id)
                } label: {
                    Image(systemName: saved ? "bookmark.fill" : "bookmark")
                        .font(.system(size: 14))
                        .foregroundStyle(saved ? p.highlight : p.foreground.opacity(0.75))
                        .padding(11)
                }
                .buttonStyle(.plain)
            }
        }
        .frame(height: 148)
        .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 18, style: .continuous).stroke(p.border, lineWidth: 1))
    }
}
