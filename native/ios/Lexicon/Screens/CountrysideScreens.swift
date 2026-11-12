//
//  CountrysideScreens.swift
//  Lexicon
//
//  Implements:
//    - Page 4: CollectionsHubScreen
//    - Page 5: MyNotesScreen
//    - Page 6: StudyListsScreen
//    - Page 7: QuickTemplatesScreen
//    - Page 8: SearchDiscoveryScreen
//

import SwiftUI

// MARK: - Page 4: Collections Hub
struct CollectionsHubScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @EnvironmentObject private var theme: LexiconThemeState
    @Environment(\.colorScheme) private var scheme

    @State private var query: String = ""
    @State private var filter: String = "All"
    private var isDark: Bool { scheme == .dark }

    var body: some View {
        let p = lexiconPalette(scheme)

        ScrollView {
            VStack(spacing: 11) {
                CountrysideHeroHeader(
                    title: "Collections",
                    subtitle: "Save, organize and revisit your favorite words.",
                    illustrationBase: "ill-hero-cottage",
                    onHistoryClick: { vm.navigate(to: .searchDiscovery) },
                    onLogoToggleTheme: { theme.toggle() },
                    heroHeight: 140
                )

                CountrysideSearchPill(
                    text: $query,
                    placeholder: "Search in your collections...",
                    showFilter: true
                )
                .padding(.horizontal, 16)

                CountrysideFilterPills(
                    items: [
                        ("All", nil),
                        ("Favorites", "heart"),
                        ("Bookmarks", "bookmark"),
                        ("Notes", "doc.text"),
                        ("Study Lists", "graduationcap"),
                    ],
                    selected: filter,
                    onSelect: { sel in
                        filter = sel
                        if sel == "Notes" { vm.navigate(to: .myNotes) }
                        if sel == "Study Lists" { vm.navigate(to: .studyLists) }
                    }
                )

                // My Collections 2x2 Grid
                VStack(spacing: 8) {
                    BotanicalSectionTitle(
                        title: "My Collections",
                        sprigAfterTitle: true,
                        actionLabel: "View all",
                        onAction: { vm.navigate(to: .studyLists) }
                    )
                    LazyVGrid(columns: [GridItem(.flexible(), spacing: 8), GridItem(.flexible(), spacing: 8)], spacing: 8) {
                        collectionQuadCard(title: "Favorites", count: "\(20 + vm.favoriteWordIds.count) words", icon: "heart", art: "ill-col-favorites", p: p) {
                            filter = "Favorites"
                        }
                        collectionQuadCard(title: "Bookmarks", count: "\(15 + vm.savedWordIds.count) words", icon: "bookmark", art: "ill-col-bookmarks", p: p) {
                            filter = "Bookmarks"
                        }
                        collectionQuadCard(title: "My Notes", count: "\(8 + vm.notes.count) words", icon: "doc.text", art: "ill-col-notes", p: p) {
                            vm.navigate(to: .myNotes)
                        }
                        collectionQuadCard(title: "Study List", count: "30 words", icon: "graduationcap", art: "ill-col-studylist", p: p) {
                            vm.navigate(to: .studyLists)
                        }
                    }
                    .padding(.horizontal, 16)
                }

                // Recently Added
                VStack(spacing: 8) {
                    BotanicalSectionTitle(
                        title: "Recently Added",
                        actionLabel: "View all",
                        onAction: { vm.navigate(to: .expandedExploration) }
                    )
                    let recents: [(String, String)] = [
                        ("serendipity", "ill-thumb-lavender"),
                        ("ethereal", "ill-thumb-cottage"),
                        ("tranquility", "ill-thumb-bridge"),
                    ]
                    VStack(spacing: 7) {
                        ForEach(recents, id: \.0) { (id, thumb) in
                            if let entry = LexiconData.getWordById(id),
                               query.isEmpty || entry.word.localizedCaseInsensitiveContains(query) {
                                let fav = vm.isFavorite(entry.id)
                                HStack(spacing: 10) {
                                    Image(countrysideImageName(thumb, isDark: isDark))
                                        .resizable()
                                        .scaledToFill()
                                        .frame(width: 56, height: 42)
                                        .clipShape(RoundedRectangle(cornerRadius: 9, style: .continuous))

                                    Button { vm.openWord(entry) } label: {
                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(entry.word)
                                                .font(.lexiconDisplay(14.5))
                                                .foregroundStyle(p.foreground)
                                            Text("\(entry.partOfSpeech.displayValue)  •  \(entry.phonetic)")
                                                .font(.lexiconBody(10.5).italic())
                                                .foregroundStyle(p.mutedForeground)
                                        }
                                        .frame(maxWidth: .infinity, alignment: .leading)
                                    }
                                    .buttonStyle(.plain)

                                    Button { vm.toggleFavorite(entry.id) } label: {
                                        Image(systemName: fav ? "heart.fill" : "heart")
                                            .font(.system(size: 14))
                                            .foregroundStyle(fav ? p.highlight : p.mutedForeground)
                                    }
                                    .buttonStyle(.plain)

                                    Image(systemName: "chevron.right")
                                        .font(.system(size: 12))
                                        .foregroundStyle(p.mutedForeground)
                                }
                                .padding(7)
                                .background(p.card)
                                .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                                .overlay(RoundedRectangle(cornerRadius: 14, style: .continuous).stroke(p.border, lineWidth: 1))
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                }

                CountrysideQuoteBanner(
                    quote: "A collection of words is a collection of worlds.",
                    attribution: "Lexicon",
                    illustrationBase: "ill-banner-bridge"
                )
                .padding(.horizontal, 16)
            }
            .padding(.bottom, 88)
        }
        .background(p.background)
    }

    private func collectionQuadCard(title: String, count: String, icon: String, art: String, p: LexiconPalette, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            ZStack(alignment: .bottomTrailing) {
                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        p.card
                        HStack(spacing: 0) {
                            Spacer(minLength: 0)
                            Image(countrysideImageName(art, isDark: isDark))
                                .resizable()
                                .scaledToFill()
                                .frame(width: geo.size.width * 0.72, height: 104)
                                .clipped()
                        }
                        VStack(alignment: .leading, spacing: 0) {
                            Image(systemName: icon)
                                .font(.system(size: 12))
                                .foregroundStyle(p.foreground)
                                .frame(width: 28, height: 28)
                                .background(p.background.opacity(0.86))
                                .clipShape(Circle())
                                .overlay(Circle().stroke(p.border, lineWidth: 1))
                            Spacer()
                            Text(title).font(.lexiconDisplay(14.5)).foregroundStyle(p.foreground)
                            Text(count).font(.lexiconBody(10)).foregroundStyle(p.mutedForeground)
                        }
                        .padding(11)
                    }
                }
                Image(systemName: "arrow.right")
                    .font(.system(size: 10, weight: .medium))
                    .foregroundStyle(p.foreground)
                    .frame(width: 24, height: 24)
                    .background(p.card.opacity(0.92))
                    .clipShape(Circle())
                    .overlay(Circle().stroke(p.border, lineWidth: 1))
                    .padding(9)
            }
            .frame(height: 104)
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).stroke(p.border, lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Page 5: My Notes
struct MyNotesScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @EnvironmentObject private var theme: LexiconThemeState
    @Environment(\.colorScheme) private var scheme

    @State private var query: String = ""
    @State private var selectedCat: String = "All"
    private var isDark: Bool { scheme == .dark }

    var body: some View {
        let p = lexiconPalette(scheme)
        let filtered = vm.notes.filter { n in
            let catOk = selectedCat == "All"
                || (selectedCat == "Word Notes" && n.category == .wordNote)
                || (selectedCat == "Personal" && n.category == .personal)
                || (selectedCat == "Study" && n.category == .study)
                || (selectedCat == "Ideas" && n.category == .idea)
            let qOk = query.isEmpty || n.title.localizedCaseInsensitiveContains(query) || n.body.localizedCaseInsensitiveContains(query)
            return catOk && qOk
        }

        ScrollView {
            VStack(spacing: 11) {
                CountrysideHeroHeader(
                    title: "Notes",
                    subtitle: "Capture thoughts, meanings and your language journey.",
                    illustrationBase: "ill-hero-notes",
                    onHistoryClick: { vm.navigate(to: .collections) },
                    onLogoToggleTheme: { theme.toggle() },
                    heroHeight: 142
                )

                CountrysideSearchPill(text: $query, placeholder: "Search your notes...", showFilter: true)
                    .padding(.horizontal, 16)

                CountrysideFilterPills(
                    items: [
                        ("All", nil),
                        ("Personal", "person"),
                        ("Word Notes", "book"),
                        ("Study", "graduationcap"),
                        ("Ideas", "lightbulb"),
                    ],
                    selected: selectedCat,
                    onSelect: { selectedCat = $0 }
                )

                VStack(spacing: 8) {
                    BotanicalSectionTitle(
                        title: "My Notes",
                        sprigAfterTitle: true,
                        actionLabel: "View all",
                        onAction: { selectedCat = "All"; query = "" }
                    )
                    LazyVGrid(columns: [GridItem(.flexible(), spacing: 8), GridItem(.flexible(), spacing: 8)], spacing: 8) {
                        ForEach(filtered.prefix(4)) { note in
                            ZStack(alignment: .bottomTrailing) {
                                Image(countrysideImageName(note.illustration ?? "ill-note-flowers", isDark: isDark))
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 78, height: 104, alignment: .bottomTrailing)

                                VStack(alignment: .leading, spacing: 0) {
                                    HStack {
                                        Text(note.category.rawValue)
                                            .font(.lexiconBody(8.5))
                                            .foregroundStyle(p.foreground)
                                            .padding(.horizontal, 8)
                                            .padding(.vertical, 2.5)
                                            .background(p.highlight.opacity(0.22))
                                            .clipShape(Capsule())
                                        Spacer()
                                        Image(systemName: "ellipsis")
                                            .font(.system(size: 12))
                                            .foregroundStyle(p.mutedForeground)
                                    }
                                    Spacer(minLength: 4)
                                    Text(note.title)
                                        .font(.lexiconDisplay(14))
                                        .foregroundStyle(p.foreground)
                                        .lineLimit(1)
                                    Text(note.body)
                                        .font(.lexiconBody(9.5))
                                        .foregroundStyle(p.foreground.opacity(0.8))
                                        .lineLimit(2)
                                        .padding(.trailing, 32)
                                    Spacer(minLength: 4)
                                    HStack {
                                        HStack(spacing: 4) {
                                            Image(systemName: "calendar")
                                                .font(.system(size: 9))
                                                .foregroundStyle(p.mutedForeground)
                                            Text(note.dateLabel)
                                                .font(.lexiconBody(8.5))
                                                .foregroundStyle(p.mutedForeground)
                                        }
                                        Spacer()
                                        Button { vm.toggleNoteBookmark(note.id) } label: {
                                            Image(systemName: note.bookmarked ? "bookmark.fill" : "bookmark")
                                                .font(.system(size: 11.5))
                                                .foregroundStyle(note.bookmarked ? p.highlight : p.mutedForeground)
                                        }
                                        .buttonStyle(.plain)
                                    }
                                }
                                .padding(11)
                            }
                            .frame(height: 130)
                            .background(p.card)
                            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                            .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).stroke(p.border, lineWidth: 1))
                        }
                    }
                    .padding(.horizontal, 16)
                }

                // Quick Templates strip
                VStack(alignment: .leading, spacing: 6) {
                    BotanicalSectionTitle(
                        title: "Quick Templates",
                        actionLabel: "View all",
                        onAction: { vm.navigate(to: .quickTemplates) }
                    )
                    Text("Use a template to start a new note.")
                        .font(.lexiconBody(10.5))
                        .foregroundStyle(p.mutedForeground)
                        .padding(.horizontal, 16)

                    let starters: [(String, String, NoteCategory)] = [
                        ("Word Note", "book", .wordNote),
                        ("Study Note", "graduationcap", .study),
                        ("Idea", "lightbulb", .idea),
                        ("Favourite", "star", .personal),
                    ]
                    HStack(spacing: 7) {
                        ForEach(starters, id: \.0) { s in
                            Button {
                                vm.addNote(category: s.2, title: "New \(s.0)", body: "Added from Quick Templates.")
                            } label: {
                                VStack(spacing: 4) {
                                    Image(systemName: s.1)
                                        .font(.system(size: 14))
                                        .foregroundStyle(p.primary)
                                    Text(s.0)
                                        .font(.lexiconBody(9.5))
                                        .foregroundStyle(p.foreground)
                                        .lineLimit(1)
                                }
                                .frame(maxWidth: .infinity)
                                .frame(height: 60)
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
                    quote: "A well-written note is a small step towards wisdom.",
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

// MARK: - Page 6: Study Lists
struct StudyListsScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @EnvironmentObject private var theme: LexiconThemeState
    @Environment(\.colorScheme) private var scheme

    @State private var query: String = ""
    private var isDark: Bool { scheme == .dark }

    var body: some View {
        let p = lexiconPalette(scheme)
        let totalWords = vm.studyLists.reduce(0) { $0 + $1.totalWords }
        let completedWords = vm.studyLists.reduce(0) { $0 + $1.completedWords }
        let overallPct = totalWords > 0 ? (completedWords * 100) / totalWords : 78
        let ringProgress = Double(vm.todayStudiedWords) / Double(max(1, vm.todayGoalWords))

        ScrollView {
            VStack(spacing: 11) {
                CountrysideHeroHeader(
                    title: "Study List",
                    subtitle: "Learn, practice and grow one word at a time.",
                    illustrationBase: "ill-hero-cottage",
                    onHistoryClick: { vm.navigate(to: .quickTemplates) },
                    onLogoToggleTheme: { theme.toggle() },
                    heroHeight: 140
                )

                CountrysideSearchPill(
                    text: $query,
                    placeholder: "Search your study lists...",
                    showFilter: true,
                    onFilterClick: { vm.navigate(to: .quickTemplates) }
                )
                .padding(.horizontal, 16)

                // 4 Stat Cards
                HStack(spacing: 7) {
                    statTile("book", "\(vm.studyLists.count + 1)", "Active Lists", p: p)
                    statTile("checkmark.circle", "\(totalWords + 32)", "Total Words", p: p)
                    statTile("chart.bar", "\(overallPct)%", "Overall Progress", p: p)
                    statTile("star", "\(vm.dayStreak)", "Day Streak", p: p)
                }
                .padding(.horizontal, 16)

                // Today's Study Card
                VStack(spacing: 10) {
                    HStack {
                        HStack(spacing: 6) {
                            BotanicalSprig(color: p.highlight).frame(width: 16, height: 16)
                            Text("Today’s Study")
                                .font(.lexiconDisplay(17))
                                .foregroundStyle(p.foreground)
                        }
                        Spacer()
                        Button { vm.advanceStudyList("sl1") } label: {
                            HStack(spacing: 4) {
                                Text("View details").font(.lexiconBody(11))
                                Image(systemName: "arrow.right").font(.system(size: 10))
                            }
                            .foregroundStyle(p.highlight)
                        }
                        .buttonStyle(.plain)
                    }
                    .padding(.horizontal, 14)
                    HStack(spacing: 12) {
                        ZStack {
                            Circle()
                                .stroke(p.border, lineWidth: 7)
                            Circle()
                                .trim(from: 0, to: ringProgress)
                                .stroke(p.primary, style: StrokeStyle(lineWidth: 7, lineCap: .round))
                                .rotationEffect(.degrees(-90))
                            VStack(spacing: 0) {
                                Text("\(vm.todayStudiedWords)/\(vm.todayGoalWords)")
                                    .font(.lexiconBody(15, weight: .semibold))
                                    .foregroundStyle(p.foreground)
                                Text("words")
                                    .font(.lexiconBody(9))
                                    .foregroundStyle(p.mutedForeground)
                            }
                        }
                        .frame(width: 80, height: 80)

                        Rectangle().fill(p.border).frame(width: 1, height: 60)

                        VStack(spacing: 4) {
                            Text("Keep going!")
                                .font(.lexiconDisplay(15))
                                .foregroundStyle(p.foreground)
                            Text("“Small steps\nlead to big progress.”")
                                .font(.lexiconLong(10.5))
                                .foregroundStyle(p.mutedForeground)
                                .multilineTextAlignment(.center)
                        }
                        .frame(maxWidth: .infinity)

                        Image(countrysideImageName("ill-books-daisies", isDark: isDark))
                            .resizable()
                            .scaledToFill()
                            .frame(width: 86, height: 72)
                            .clipShape(RoundedRectangle(cornerRadius: 10, style: .continuous))
                    }
                    .padding(.horizontal, 14)
                    .padding(.bottom, 4)
                }
                .padding(.vertical, 12)
                .background(p.card)
                .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                .overlay(RoundedRectangle(cornerRadius: 18, style: .continuous).stroke(p.border, lineWidth: 1))
                .padding(.horizontal, 16)

                // Your Study Lists
                VStack(spacing: 8) {
                    BotanicalSectionTitle(
                        title: "Your Study Lists",
                        actionLabel: "View all",
                        onAction: { vm.navigate(to: .quickTemplates) }
                    )
                    VStack(spacing: 7) {
                        ForEach(vm.studyLists.filter { query.isEmpty || $0.title.localizedCaseInsensitiveContains(query) }) { sl in
                            HStack(spacing: 10) {
                                Image(countrysideImageName(sl.thumbnail, isDark: isDark))
                                    .resizable()
                                    .scaledToFill()
                                    .frame(width: 56, height: 42)
                                    .clipShape(RoundedRectangle(cornerRadius: 9, style: .continuous))

                                Button {
                                    if let firstId = sl.wordIds.first, let entry = LexiconData.getWordById(firstId) {
                                        vm.openWord(entry)
                                    }
                                } label: {
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(sl.title)
                                            .font(.lexiconDisplay(13.5))
                                            .foregroundStyle(p.foreground)
                                            .lineLimit(1)
                                        Text("\(sl.totalWords) words • \(sl.percentComplete)% complete")
                                            .font(.lexiconBody(9.5))
                                            .foregroundStyle(p.mutedForeground)
                                    }
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                }
                                .buttonStyle(.plain)

                                GeometryReader { barGeo in
                                    ZStack(alignment: .leading) {
                                        Capsule().fill(p.border)
                                        Capsule()
                                            .fill(p.primary)
                                            .frame(width: barGeo.size.width * CGFloat(sl.percentComplete) / 100.0)
                                    }
                                }
                                .frame(width: 64, height: 6)

                                Button { vm.advanceStudyList(sl.id) } label: {
                                    Image(systemName: "play.fill")
                                        .font(.system(size: 10))
                                        .foregroundStyle(isDark ? LexiconColors.backgroundDark : .white)
                                        .frame(width: 28, height: 28)
                                        .background(p.primary)
                                        .clipShape(Circle())
                                }
                                .buttonStyle(.plain)

                                Image(systemName: "ellipsis")
                                    .rotationEffect(.degrees(90))
                                    .font(.system(size: 11))
                                    .foregroundStyle(p.mutedForeground)
                            }
                            .padding(8)
                            .background(p.card)
                            .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
                            .overlay(RoundedRectangle(cornerRadius: 14, style: .continuous).stroke(p.border, lineWidth: 1))
                        }
                    }
                    .padding(.horizontal, 16)
                }

                CountrysideQuoteBanner(
                    quote: "Knowledge is a journey, not a destination.",
                    attribution: "Lexicon",
                    illustrationBase: "ill-banner-bridge"
                )
                .padding(.horizontal, 16)
            }
            .padding(.bottom, 88)
        }
        .background(p.background)
    }

    private func statTile(_ icon: String, _ num: String, _ label: String, p: LexiconPalette) -> some View {
        VStack(spacing: 3) {
            Image(systemName: icon)
                .font(.system(size: 14))
                .foregroundStyle(p.primary)
            Text(num)
                .font(.lexiconBody(14, weight: .semibold))
                .foregroundStyle(p.foreground)
            Text(label)
                .font(.lexiconBody(8.5))
                .foregroundStyle(p.mutedForeground)
                .lineLimit(1)
                .minimumScaleFactor(0.8)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 10)
        .background(p.card)
        .clipShape(RoundedRectangle(cornerRadius: 14, style: .continuous))
        .overlay(RoundedRectangle(cornerRadius: 14, style: .continuous).stroke(p.border, lineWidth: 1))
    }
}

// MARK: - Page 7: Quick Templates
struct QuickTemplatesScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @EnvironmentObject private var theme: LexiconThemeState
    @Environment(\.colorScheme) private var scheme

    @State private var query: String = ""
    @State private var filter: String = "All"
    private var isDark: Bool { scheme == .dark }

    var body: some View {
        let p = lexiconPalette(scheme)
        let filtered = vm.templates.filter { t in
            (filter == "All" || t.category.rawValue == filter) &&
                (query.isEmpty || t.title.localizedCaseInsensitiveContains(query))
        }

        ScrollView {
            VStack(spacing: 11) {
                CountrysideHeroHeader(
                    title: "Quick Templates",
                    subtitle: "Start with a template, or create your own.",
                    illustrationBase: "ill-hero-cottage",
                    onHistoryClick: { vm.navigate(to: .myNotes) },
                    onLogoToggleTheme: { theme.toggle() },
                    heroHeight: 140
                )

                CountrysideSearchPill(text: $query, placeholder: "Search templates...", showFilter: true)
                    .padding(.horizontal, 16)

                CountrysideFilterPills(
                    items: [
                        ("All", nil),
                        ("Learning", "book"),
                        ("Writing", "pencil"),
                        ("Exam", "graduationcap"),
                        ("Daily Life", "lightbulb"),
                    ],
                    selected: filter,
                    onSelect: { filter = $0 }
                )

                VStack(spacing: 8) {
                    BotanicalSectionTitle(
                        title: "Popular Templates",
                        actionLabel: "View all",
                        onAction: { filter = "All"; query = "" }
                    )
                    LazyVGrid(columns: [GridItem(.flexible(), spacing: 8), GridItem(.flexible(), spacing: 8)], spacing: 8) {
                        ForEach(filtered.prefix(4)) { tpl in
                            VStack(spacing: 0) {
                                ZStack(alignment: .topTrailing) {
                                    Image(countrysideImageName(tpl.illustration, isDark: isDark))
                                        .resizable()
                                        .scaledToFill()
                                        .frame(height: 76)
                                        .clipped()
                                    Button { vm.toggleTemplateStar(tpl.id) } label: {
                                        Image(systemName: tpl.starred ? "star.fill" : "star")
                                            .font(.system(size: 11))
                                            .foregroundStyle(p.highlight)
                                            .frame(width: 22, height: 22)
                                            .background(p.card.opacity(0.88))
                                            .clipShape(Circle())
                                            .padding(6)
                                    }
                                    .buttonStyle(.plain)
                                }

                                Button {
                                    vm.addNote(category: .study, title: tpl.title, body: tpl.subtitle)
                                    vm.navigate(to: .myNotes)
                                } label: {
                                    HStack {
                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(tpl.title)
                                                .font(.lexiconDisplay(13))
                                                .foregroundStyle(p.foreground)
                                                .lineLimit(1)
                                            Text(tpl.subtitle)
                                                .font(.lexiconBody(9.5))
                                                .foregroundStyle(p.mutedForeground)
                                                .lineLimit(2)
                                                .multilineTextAlignment(.leading)
                                        }
                                        Spacer(minLength: 4)
                                        Image(systemName: "arrow.right")
                                            .font(.system(size: 10))
                                            .foregroundStyle(p.foreground)
                                            .frame(width: 22, height: 22)
                                            .overlay(Circle().stroke(p.border, lineWidth: 1))
                                    }
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 9)
                                }
                                .buttonStyle(.plain)
                            }
                            .background(p.card)
                            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                            .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).stroke(p.border, lineWidth: 1))
                        }
                    }
                    .padding(.horizontal, 16)
                }

                // Create Your Own
                VStack(spacing: 8) {
                    BotanicalSectionTitle(
                        title: "Create Your Own",
                        actionLabel: "View all",
                        onAction: { vm.createCustomTemplate(title: "Countryside Reading", subtitle: "Collect poetic nature words.", category: .writing) }
                    )
                    Button {
                        vm.createCustomTemplate(title: "Botanical Lexicon", subtitle: "Custom village field-notes template.", category: .learning)
                    } label: {
                        ZStack(alignment: .trailing) {
                            Image(countrysideImageName("ill-note-flowers", isDark: isDark))
                                .resizable()
                                .scaledToFill()
                                .frame(width: 100, height: 74)
                                .clipped()

                            HStack(spacing: 12) {
                                Image(systemName: "plus")
                                    .font(.system(size: 18))
                                    .foregroundStyle(p.foreground)
                                    .frame(width: 44, height: 44)
                                    .overlay(
                                        RoundedRectangle(cornerRadius: 11, style: .continuous)
                                            .stroke(p.highlight, style: StrokeStyle(lineWidth: 1, dash: [4, 3]))
                                    )
                                VStack(alignment: .leading, spacing: 2) {
                                    Text("New Template").font(.lexiconDisplay(15)).foregroundStyle(p.foreground)
                                    Text("Create a custom template for your needs.").font(.lexiconBody(10.5)).foregroundStyle(p.mutedForeground)
                                }
                                Spacer()
                            }
                            .padding(.horizontal, 14)
                        }
                        .frame(height: 74)
                        .background(p.card)
                        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                        .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).stroke(p.border, lineWidth: 1))
                    }
                    .buttonStyle(.plain)
                    .padding(.horizontal, 16)
                }

                CountrysideQuoteBanner(
                    quote: "Better words. A brighter mind.",
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

// MARK: - Page 8: Search & Discovery
struct SearchDiscoveryScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @EnvironmentObject private var theme: LexiconThemeState
    @Environment(\.colorScheme) private var scheme

    @State private var draft: String = ""

    var body: some View {
        let p = lexiconPalette(scheme)
        let trimmed = draft.trimmingCharacters(in: .whitespacesAndNewlines)

        ScrollView {
            VStack(spacing: 11) {
                CountrysideHeroHeader(
                    title: "Search",
                    subtitle: "Find any word, explore its meaning, and discover more.",
                    illustrationBase: "ill-hero-cottage",
                    onHistoryClick: { vm.navigate(to: .expandedExploration) },
                    onLogoToggleTheme: { theme.toggle() },
                    heroHeight: 140
                )

                CountrysideSearchPill(
                    text: $draft,
                    placeholder: "Search for a word...",
                    onSubmit: { vm.submitSearch(trimmed.isEmpty ? "serendipity" : trimmed) },
                    showMicOnly: true
                )
                .padding(.horizontal, 16)

                CountrysideFilterPills(
                    items: [
                        ("All", nil),
                        ("Words", "book"),
                        ("Meanings", "list.bullet"),
                        ("Examples", "quote.opening"),
                        ("Idioms", "star"),
                    ],
                    selected: vm.searchScopeTab,
                    onSelect: { vm.searchScopeTab = $0 }
                )

                if !trimmed.isEmpty {
                    let matches = vm.searchWords(trimmed, limit: 6)
                    VStack(spacing: 8) {
                        BotanicalSectionTitle(
                            title: "Suggestions (\(matches.count))",
                            actionLabel: "Full Results",
                            onAction: { vm.submitSearch(trimmed) }
                        )
                        VStack(spacing: 0) {
                            ForEach(Array(matches.enumerated()), id: \.element.id) { idx, w in
                                if idx > 0 { Rectangle().fill(p.border).frame(height: 1) }
                                Button { vm.submitSearch(w.word) } label: {
                                    HStack {
                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(w.word.lowercased()).font(.lexiconDisplay(15)).foregroundStyle(p.foreground)
                                            Text("\(w.partOfSpeech.displayValue)  •  \(w.phonetic)").font(.lexiconBody(10.5).italic()).foregroundStyle(p.mutedForeground)
                                        }
                                        Spacer()
                                        Image(systemName: "chevron.right").font(.system(size: 12)).foregroundStyle(p.mutedForeground)
                                    }
                                    .padding(.horizontal, 14)
                                    .padding(.vertical, 10)
                                }
                                .buttonStyle(.plain)
                            }
                        }
                        .background(p.card)
                        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                        .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).stroke(p.border, lineWidth: 1))
                        .padding(.horizontal, 16)
                    }
                } else if vm.searchScopeTab == "Idioms" {
                    VStack(spacing: 8) {
                        BotanicalSectionTitle(
                            title: "Countryside Idioms & Phrases",
                            actionLabel: "All Words",
                            onAction: { vm.searchScopeTab = "All" }
                        )
                        VStack(spacing: 0) {
                            ForEach(Array(LexiconData.idiomsAndPhrases.enumerated()), id: \.element.id) { idx, ip in
                                if idx > 0 { Rectangle().fill(p.border).frame(height: 1) }
                                VStack(alignment: .leading, spacing: 3) {
                                    Text("\(ip.phrase) (\(ip.kind))").font(.lexiconDisplay(14)).foregroundStyle(p.foreground)
                                    Text(ip.meaning).font(.lexiconBody(11)).foregroundStyle(p.mutedForeground)
                                }
                                .padding(12)
                                .frame(maxWidth: .infinity, alignment: .leading)
                            }
                        }
                        .background(p.card)
                        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                        .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).stroke(p.border, lineWidth: 1))
                        .padding(.horizontal, 16)
                    }
                } else {
                    VStack(spacing: 8) {
                        BotanicalSectionTitle(
                            title: "Recent Searches",
                            actionLabel: vm.recentSearches.isEmpty ? nil : "Clear All",
                            isClearAction: true,
                            onAction: { vm.clearRecentSearches() }
                        )
                        VStack(spacing: 0) {
                            if vm.recentSearches.isEmpty {
                                Text("Your search history is clear. Tap any filter below to explore.")
                                    .font(.lexiconBody(12))
                                    .foregroundStyle(p.mutedForeground)
                                    .padding(16)
                            } else {
                                ForEach(Array(vm.recentSearches.prefix(5).enumerated()), id: \.offset) { idx, term in
                                    let entry = LexiconData.getWordById(term)
                                    if idx > 0 { Rectangle().fill(p.border).frame(height: 1) }
                                    Button { vm.submitSearch(term) } label: {
                                        HStack(spacing: 12) {
                                            Image(systemName: "clock.arrow.circlepath")
                                                .font(.system(size: 15))
                                                .foregroundStyle(p.mutedForeground)
                                            VStack(alignment: .leading, spacing: 2) {
                                                Text(term.lowercased())
                                                    .font(.lexiconDisplay(15))
                                                    .foregroundStyle(p.foreground)
                                                Text("\(entry?.partOfSpeech.displayValue ?? "noun")  •  \(entry?.phonetic ?? "/ˈlɛksɪkɒn/")")
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
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                        }
                        .background(p.card)
                        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                        .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).stroke(p.border, lineWidth: 1))
                        .padding(.horizontal, 16)
                    }
                }

                // 4 Search Filters
                VStack(spacing: 8) {
                    BotanicalSectionTitle(title: "Search Filters", actionLabel: nil)
                    HStack(spacing: 7) {
                        filterTile("globe", "Language", vm.filterLanguage, key: "language", p: p)
                        filterTile("book", "Part of Speech", vm.filterPos, key: "pos", p: p)
                        filterTile("chart.bar", "Difficulty", vm.filterDifficulty, key: "difficulty", p: p)
                        filterTile("star", "Sort By", vm.filterSort, key: "sort", p: p)
                    }
                    .padding(.horizontal, 16)
                }

                CountrysideQuoteBanner(
                    quote: "A word is a doorway to a bigger world.",
                    attribution: "Lexicon",
                    illustrationBase: "ill-banner-bridge"
                )
                .padding(.horizontal, 16)
            }
            .padding(.bottom, 88)
        }
        .background(p.background)
    }

    private func filterTile(_ icon: String, _ title: String, _ value: String, key: String, p: LexiconPalette) -> some View {
        Button {
            vm.cycleSearchFilter(key)
        } label: {
            VStack(spacing: 3) {
                Image(systemName: icon)
                    .font(.system(size: 14))
                    .foregroundStyle(p.primary)
                Text(title)
                    .font(.lexiconBody(8.5))
                    .foregroundStyle(p.mutedForeground)
                    .lineLimit(1)
                    .minimumScaleFactor(0.8)
                Text("\(value) ⌄")
                    .font(.lexiconBody(9, weight: .medium))
                    .foregroundStyle(p.foreground)
                    .lineLimit(1)
                    .minimumScaleFactor(0.75)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 9)
            .background(p.card)
            .clipShape(RoundedRectangle(cornerRadius: 13, style: .continuous))
            .overlay(RoundedRectangle(cornerRadius: 13, style: .continuous).stroke(p.border, lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}
