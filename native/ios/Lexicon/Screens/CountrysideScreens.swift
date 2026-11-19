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
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    @State private var query: String = ""
    @State private var filter: String = "All"
    @State private var showAllRecent = false
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
                    showFilter: true,
                    onFilterClick: {
                        let options = ["All", "Favorites", "Bookmarks"]
                        filter = options[((options.firstIndex(of: filter) ?? 0) + 1) % options.count]
                    }
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
                        actionLabel: nil
                    )
                    LazyVGrid(columns: [GridItem(.adaptive(minimum: dynamicTypeSize.isAccessibilitySize ? 260 : 165), spacing: 8)], spacing: 8) {
                        collectionQuadCard(title: "Favorites", count: "\(vm.favoriteWordIds.count) words", icon: "heart", art: "ill-col-favorites", p: p) {
                            filter = "Favorites"
                        }
                        collectionQuadCard(title: "Bookmarks", count: "\(vm.savedWordIds.count) words", icon: "bookmark", art: "ill-col-bookmarks", p: p) {
                            filter = "Bookmarks"
                        }
                        collectionQuadCard(title: "My Notes", count: "\(vm.notes.count) notes", icon: "doc.text", art: "ill-col-notes", p: p) {
                            vm.navigate(to: .myNotes)
                        }
                        collectionQuadCard(title: "Study List", count: "\(vm.studyLists.reduce(0) { $0 + $1.totalWords }) words", icon: "graduationcap", art: "ill-col-studylist", p: p) {
                            vm.navigate(to: .studyLists)
                        }
                    }
                    .padding(.horizontal, 16)
                }

                // Recently Added
                VStack(spacing: 8) {
                    BotanicalSectionTitle(
                        title: "Recently Added",
                        actionLabel: showAllRecent ? "Show less" : "View all",
                        onAction: { showAllRecent.toggle() }
                    )
                    let ids = filter == "Favorites" ? vm.favoriteWordIds
                        : filter == "Bookmarks" ? vm.savedWordIds
                        : (vm.savedWordIds + vm.favoriteWordIds).reduce(into: [String]()) { acc, id in
                            if !acc.contains(id) { acc.append(id) }
                        }
                    let thumbs = ["ill-thumb-lavender", "ill-thumb-cottage", "ill-thumb-bridge"]
                    let recents: [(String, String)] = ids.enumerated().map { index, id in
                        (id, thumbs[index % thumbs.count])
                    }
                    VStack(spacing: 7) {
                        ForEach(showAllRecent ? recents : Array(recents.prefix(3)), id: \.0) { (id, thumb) in
                            if let entry = LexiconData.getWordById(id),
                               query.isEmpty || entry.word.localizedCaseInsensitiveContains(query) {
                                let fav = vm.isFavorite(entry.id)
                                HStack(spacing: 10) {
                                    Image(countrysideImageName(thumb, isDark: isDark))
                                        .resizable()
                                        .scaledToFit()
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
                    attribution: "Lexora",
                    illustrationBase: "ill-banner-bridge"
                )
                .padding(.horizontal, 16)
            }
            .padding(.bottom, 160)
        }
        .background(p.background)
        .onAppear { filter = vm.collectionFilter }
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
                                .scaledToFit()
                                .frame(width: geo.size.width * 0.86, height: dynamicTypeSize.isAccessibilitySize ? 146 : 112, alignment: .bottomTrailing)
                        }
                        VStack(alignment: .leading, spacing: 0) {
                            Image(systemName: icon)
                                .font(.system(size: 12))
                                .foregroundStyle(p.foreground)
                                .frame(width: 28, height: 28)
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
                    .padding(9)
            }
            .frame(height: dynamicTypeSize.isAccessibilitySize ? 146 : 112)
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
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    @State private var query: String = ""
    @State private var selectedCat: String = "All"
    @State private var showAllNotes = false
    @State private var editingNote: NoteItem? = nil
    @State private var editorTitle = ""
    @State private var editorBody = ""
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

                CountrysideSearchPill(
                    text: $query, placeholder: "Search your notes...", showFilter: true,
                    onFilterClick: {
                        let options = ["All", "Personal", "Word Notes", "Study", "Ideas"]
                        selectedCat = options[((options.firstIndex(of: selectedCat) ?? 0) + 1) % options.count]
                    }
                )
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
                        actionLabel: showAllNotes ? "Show less" : "View all",
                        onAction: { selectedCat = "All"; query = ""; showAllNotes.toggle() }
                    )
                    LazyVGrid(columns: [GridItem(.adaptive(minimum: dynamicTypeSize.isAccessibilitySize ? 260 : 165), spacing: 8)], spacing: 8) {
                        if filtered.isEmpty {
                            Text("No notes match your search. Choose a Quick Template to start.")
                                .font(.lexiconBody(13))
                                .foregroundStyle(p.mutedForeground)
                                .padding(12)
                        }
                        ForEach(showAllNotes ? filtered : Array(filtered.prefix(4))) { note in
                            ZStack(alignment: .bottomTrailing) {
                                Image(countrysideImageName(note.illustration ?? "ill-note-flowers", isDark: isDark))
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 110, height: 114, alignment: .bottomTrailing)

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
                                        .font(.lexiconDisplay(16))
                                        .foregroundStyle(p.foreground)
                                        .lineLimit(1)
                                    Text(note.body)
                                        .font(.lexiconBody(11.5))
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
                                                .font(.lexiconBody(10))
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
                            .frame(minHeight: dynamicTypeSize.isAccessibilitySize ? 218 : 128)
                            .background(p.card)
                            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                            .overlay(RoundedRectangle(cornerRadius: 16, style: .continuous).stroke(p.border, lineWidth: 1))
                            .onTapGesture {
                                editorTitle = note.title
                                editorBody = note.body
                                editingNote = note
                            }
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
                                editorTitle = "New \(s.0)"
                                editorBody = ""
                                editingNote = NoteItem(
                                    id: "__new_note__", category: s.2,
                                    title: editorTitle, body: "", dateLabel: ""
                                )
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
                    attribution: "Lexora",
                    illustrationBase: "ill-banner-bridge"
                )
                .padding(.horizontal, 16)
            }
            .padding(.bottom, 160)
        }
        .background(p.background)
        .sheet(item: $editingNote) { note in
            NavigationStack {
                Form {
                    TextField("Title", text: $editorTitle)
                    TextEditor(text: $editorBody)
                        .frame(minHeight: 180)
                    if note.id != "__new_note__" {
                        Button("Delete Note", role: .destructive) {
                            vm.deleteNote(note.id)
                            editingNote = nil
                        }
                    }
                }
                .navigationTitle(note.id == "__new_note__" ? "Create note" : "Edit note")
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancel") { editingNote = nil }
                    }
                    ToolbarItem(placement: .confirmationAction) {
                        Button("Save") {
                            if note.id == "__new_note__" {
                                vm.addNote(category: note.category, title: editorTitle, body: editorBody)
                            } else {
                                vm.updateNote(note.id, title: editorTitle, body: editorBody)
                            }
                            editingNote = nil
                        }
                    }
                }
            }
            .presentationDragIndicator(.visible)
        }
    }
}

// MARK: - Page 6: Study Lists
struct StudyListsScreen: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @EnvironmentObject private var theme: LexiconThemeState
    @Environment(\.colorScheme) private var scheme

    @State private var query: String = ""
    @State private var practicingList: StudyListItem? = nil
    @State private var answerRevealed = false
    @State private var showCreateList = false
    @State private var newListTitle = ""
    @State private var newListWords = ""
    @State private var listError = false
    private var isDark: Bool { scheme == .dark }

    var body: some View {
        let p = lexiconPalette(scheme)
        let totalWords = vm.studyLists.reduce(0) { $0 + $1.totalWords }
        let completedWords = vm.studyLists.reduce(0) { $0 + $1.completedWords }
        let overallPct = totalWords > 0 ? (completedWords * 100) / totalWords : 0
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
                    showFilter: false
                )
                .padding(.horizontal, 16)

                // 4 Stat Cards
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 7) {
                    statTile("book", "\(vm.studyLists.count)", "Active Lists", p: p)
                    statTile("checkmark.circle", "\(totalWords)", "Total Words", p: p)
                    statTile("chart.bar", "\(overallPct)%", "Overall Progress", p: p)
                    statTile("star", "\(vm.dayStreak)", "Day Streak", p: p)
                }
                    .padding(.horizontal, 16)
                }

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
                        Button {
                            practicingList = vm.studyLists.first(where: {
                                !$0.wordIds.isEmpty && $0.completedWords < $0.wordIds.count
                            })
                            answerRevealed = false
                        } label: {
                            HStack(spacing: 4) {
                                Text("View details").font(.lexiconBody(11))
                                Image(systemName: "arrow.right").font(.system(size: 10))
                            }
                            .foregroundStyle(p.highlight)
                        }
                        .buttonStyle(.plain)
                        .disabled(!vm.studyLists.contains(where: {
                            !$0.wordIds.isEmpty && $0.completedWords < $0.wordIds.count
                        }))
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
                            .scaledToFit()
                            .frame(width: 95, height: 82)
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
                        actionLabel: "New list",
                        onAction: { listError = false; showCreateList = true }
                    )
                    VStack(spacing: 7) {
                        if !query.isEmpty && !vm.studyLists.contains(where: { $0.title.localizedCaseInsensitiveContains(query) }) {
                            Text("No study lists match your search.")
                                .font(.lexiconBody(13))
                                .foregroundStyle(p.mutedForeground)
                                .padding(12)
                        }
                        ForEach(vm.studyLists.filter { query.isEmpty || $0.title.localizedCaseInsensitiveContains(query) }) { sl in
                            HStack(spacing: 10) {
                                Image(countrysideImageName(sl.thumbnail, isDark: isDark))
                                    .resizable()
                                    .scaledToFit()
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

                                Button { practicingList = sl; answerRevealed = false } label: {
                                    Image(systemName: "play.fill")
                                        .font(.system(size: 10))
                                        .foregroundStyle(isDark ? LexiconColors.backgroundDark : .white)
                                        .frame(width: 28, height: 28)
                                        .background(p.primary)
                                        .clipShape(Circle())
                                }
                                .buttonStyle(.plain)

                                Menu {
                                    Button("Reset progress") { vm.resetStudyList(sl.id) }
                                    if sl.id.hasPrefix("sl_custom_") {
                                        Button("Delete list", role: .destructive) { vm.deleteStudyList(sl.id) }
                                    }
                                } label: {
                                    Image(systemName: "ellipsis")
                                        .rotationEffect(.degrees(90))
                                        .font(.system(size: 14))
                                        .foregroundStyle(p.mutedForeground)
                                        .frame(width: 30, height: 36)
                                }
                                .accessibilityLabel("Manage \(sl.title)")
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
                    attribution: "Lexora",
                    illustrationBase: "ill-banner-bridge"
                )
                .padding(.horizontal, 16)
            }
            .padding(.bottom, 160)
        }
        .background(p.background)
        .sheet(item: $practicingList) { list in
            reviewSheet(list)
                .presentationDragIndicator(.visible)
        }
        .sheet(isPresented: $showCreateList) {
            NavigationStack {
                Form {
                    Section {
                        TextField("List name", text: $newListTitle)
                        TextField("Words, separated by commas", text: $newListWords, axis: .vertical)
                            .lineLimit(2...4)
                        Text("Leave words blank to use bookmarks. Only known offline words are added.")
                            .font(.lexiconBody(12))
                            .foregroundStyle(lexiconPalette(scheme).mutedForeground)
                        if listError {
                            Text("No matching words found. Try Serendipity or Wanderlust.")
                                .font(.lexiconBody(12)).foregroundStyle(.red)
                        }
                    } header: { Text("Your vocabulary notebook") }
                }
                .navigationTitle("New study list")
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancel") { showCreateList = false }
                    }
                    ToolbarItem(placement: .confirmationAction) {
                        Button("Create") {
                            if vm.createStudyList(title: newListTitle, words: newListWords) {
                                showCreateList = false
                                newListTitle = ""
                                newListWords = ""
                                query = ""
                            } else {
                                listError = true
                            }
                        }
                        .disabled(newListTitle.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                    }
                }
            }
            .presentationDragIndicator(.visible)
        }

    }

    private func reviewSheet(_ list: StudyListItem) -> some View {
        let p = lexiconPalette(scheme)
        let current = vm.studyLists.first(where: { $0.id == list.id }) ?? list
        let nextId = current.wordIds.dropFirst(current.completedWords).first
        let entry = nextId.flatMap { LexiconData.getWordById($0) }
        return NavigationStack {
            VStack(alignment: .leading, spacing: 18) {
                if let entry {
                    Text(entry.word)
                        .font(.lexiconDisplay(30))
                        .foregroundStyle(p.foreground)
                    Text(entry.phonetic)
                        .font(.lexiconBody(16))
                        .foregroundStyle(p.mutedForeground)
                    if answerRevealed {
                        Text(entry.definitions.first ?? "")
                            .font(.lexiconLong(18))
                            .foregroundStyle(p.foreground)
                        if let example = entry.examples.first {
                            Text(example)
                                .font(.lexiconLong(15).italic())
                                .foregroundStyle(p.mutedForeground)
                        }
                    } else {
                        Text("Recall the meaning, then reveal the answer.")
                            .font(.lexiconBody(16))
                            .foregroundStyle(p.mutedForeground)
                    }
                } else {
                    Text("All available words reviewed. Beautiful work!")
                        .font(.lexiconDisplay(20))
                        .foregroundStyle(p.foreground)
                }
                Spacer()
                Button(entry == nil ? "Done" : answerRevealed ? "Mark learned" : "Reveal meaning") {
                    guard entry != nil else { practicingList = nil; return }
                    if !answerRevealed {
                        answerRevealed = true
                    } else {
                        vm.advanceStudyList(current.id)
                        answerRevealed = false
                        if current.completedWords + 1 >= current.wordIds.count { practicingList = nil }
                    }
                }
                .buttonStyle(.borderedProminent)
                .tint(p.primary)
            }
            .padding(24)
            .background(p.background)
            .navigationTitle(current.title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { practicingList = nil; answerRevealed = false }
                }
            }
        }
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
                .font(.lexiconBody(10.5))
                .foregroundStyle(p.mutedForeground)
                .lineLimit(1)
                .minimumScaleFactor(0.8)
        }
        .frame(width: 106)
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
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    @State private var query: String = ""
    @State private var filter: String = "All"
    @State private var showAllTemplates = false
    @State private var showCreateTemplate = false
    @State private var templateForNote: VocabTemplate? = nil
    @State private var templateNoteTitle = ""
    @State private var templateNoteBody = ""
    @State private var newTemplateTitle = ""
    @State private var newTemplateDescription = ""
    @State private var newTemplateCategory: TemplateCategory = .learning
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

                CountrysideSearchPill(
                    text: $query, placeholder: "Search templates...", showFilter: true,
                    onFilterClick: {
                        let options = ["All", "Learning", "Writing", "Exam", "Daily Life"]
                        filter = options[((options.firstIndex(of: filter) ?? 0) + 1) % options.count]
                    }
                )
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
                        actionLabel: showAllTemplates ? "Show less" : "View all",
                        onAction: { filter = "All"; query = ""; showAllTemplates.toggle() }
                    )
                    LazyVGrid(columns: [GridItem(.adaptive(minimum: dynamicTypeSize.isAccessibilitySize ? 270 : 165), spacing: 8)], spacing: 8) {
                        if filtered.isEmpty {
                            Text("No templates match your search or category.")
                                .font(.lexiconBody(13))
                                .foregroundStyle(p.mutedForeground)
                                .padding(12)
                        }
                        ForEach(showAllTemplates ? filtered : Array(filtered.prefix(4))) { tpl in
                            VStack(spacing: 0) {
                                ZStack(alignment: .topTrailing) {
                                    Image(countrysideImageName(tpl.illustration, isDark: isDark))
                                        .resizable()
                                        .scaledToFit()
                                        .frame(height: dynamicTypeSize.isAccessibilitySize ? 106 : 82)
                                        .frame(maxWidth: .infinity)
                                        .accessibilityHidden(true)
                                    Button { vm.toggleTemplateStar(tpl.id) } label: {
                                        Image(systemName: tpl.starred ? "star.fill" : "star")
                                            .font(.system(size: 11))
                                            .foregroundStyle(p.highlight)
                                            .frame(width: 22, height: 22)
                                            .padding(6)
                                    }
                                    .buttonStyle(.plain)
                                }

                                Button {
                                    templateNoteTitle = tpl.title
                                    templateNoteBody = tpl.sampleWords.isEmpty
                                        ? tpl.subtitle : tpl.sampleWords.joined(separator: ", ")
                                    templateForNote = tpl
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
                        onAction: { showCreateTemplate = true }
                    )
                    Button {
                        showCreateTemplate = true
                    } label: {
                        ZStack(alignment: .trailing) {
                            Image(countrysideImageName("ill-note-flowers", isDark: isDark))
                                .resizable()
                                .scaledToFit()
                                .frame(width: 132, height: 74)

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
                    attribution: "Lexora",
                    illustrationBase: "ill-banner-bridge"
                )
                .padding(.horizontal, 16)
            }
            .padding(.bottom, 160)
        }
        .background(p.background)
        .sheet(isPresented: $showCreateTemplate) {
            NavigationStack {
                Form {
                    TextField("Template name", text: $newTemplateTitle)
                    TextField("Description", text: $newTemplateDescription, axis: .vertical)
                    Picker("Category", selection: $newTemplateCategory) {
                        ForEach(TemplateCategory.allCases, id: \.self) { cat in
                            Text(cat.rawValue).tag(cat)
                        }
                    }
                }
                .navigationTitle("New template")
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancel") { showCreateTemplate = false }
                    }
                    ToolbarItem(placement: .confirmationAction) {
                        Button("Create") {
                            vm.createCustomTemplate(title: newTemplateTitle, subtitle: newTemplateDescription, category: newTemplateCategory)
                            newTemplateTitle = ""
                            newTemplateDescription = ""
                            filter = "All"
                            query = ""
                            showCreateTemplate = false
                        }
                        .disabled(newTemplateTitle.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                    }
                }
            }
            .presentationDragIndicator(.visible)
        }
        .sheet(item: $templateForNote) { tpl in
            NavigationStack {
                Form {
                    Text(tpl.subtitle)
                        .foregroundStyle(lexiconPalette(scheme).mutedForeground)
                    TextField("Note title", text: $templateNoteTitle)
                    TextEditor(text: $templateNoteBody)
                        .frame(minHeight: 180)
                }
                .navigationTitle("Write from template")
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancel") { templateForNote = nil }
                    }
                    ToolbarItem(placement: .confirmationAction) {
                        Button("Save note") {
                            let category: NoteCategory
                            switch tpl.category {
                            case .learning, .exam: category = .study
                            case .writing: category = .wordNote
                            case .dailyLife: category = .personal
                            }
                            vm.addNote(category: category, title: templateNoteTitle, body: templateNoteBody)
                            templateForNote = nil
                            vm.navigate(to: .myNotes)
                        }
                        .disabled(templateNoteTitle.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                    }
                }
            }
            .presentationDragIndicator(.visible)
        }
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
                    onHistoryClick: { vm.navigate(to: .dictionaryHome) },
                    onLogoToggleTheme: { theme.toggle() },
                    heroHeight: 140
                )

                CountrysideSearchPill(
                    text: $draft,
                    placeholder: "Search for a word...",
                    onSubmit: { if !trimmed.isEmpty { vm.submitSearch(trimmed) } },
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

                if !trimmed.isEmpty && vm.searchScopeTab != "Idioms" {
                    let matches = vm.searchWords(trimmed, limit: 6)
                    VStack(spacing: 8) {
                        BotanicalSectionTitle(
                            title: "Suggestions (\(matches.count))",
                            actionLabel: "Full Results",
                            onAction: { vm.submitSearch(trimmed) }
                        )
                        VStack(spacing: 0) {
                            if matches.isEmpty {
                                Text("No offline match for “\(trimmed)” with current filters.")
                                    .font(.lexiconBody(13))
                                    .foregroundStyle(p.mutedForeground)
                                    .padding(16)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                            }
                            ForEach(Array(matches.enumerated()), id: \.element.id) { idx, w in
                                if idx > 0 { Rectangle().fill(p.border).frame(height: 1) }
                                Button { vm.addRecentSearch(w.word); vm.openWord(w) } label: {
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
                            let idioms = LexiconData.idiomsAndPhrases.filter { ip in
                                trimmed.isEmpty
                                    || ip.phrase.localizedCaseInsensitiveContains(trimmed)
                                    || ip.meaning.localizedCaseInsensitiveContains(trimmed)
                                    || ip.example.localizedCaseInsensitiveContains(trimmed)
                            }
                            if idioms.isEmpty {
                                Text("No matching idioms. Try another expression.")
                                    .font(.lexiconBody(13))
                                    .foregroundStyle(p.mutedForeground)
                                    .padding(14)
                            }
                            ForEach(Array(idioms.enumerated()), id: \.element.id) { idx, ip in
                                if idx > 0 { Rectangle().fill(p.border).frame(height: 1) }
                                Button { vm.submitSearch(ip.phrase) } label: {
                                    VStack(alignment: .leading, spacing: 3) {
                                        Text("\(ip.phrase) (\(ip.kind))").font(.lexiconDisplay(14)).foregroundStyle(p.foreground)
                                        Text(ip.meaning).font(.lexiconBody(11)).foregroundStyle(p.mutedForeground)
                                        Text(ip.example).font(.lexiconBody(10.5).italic()).foregroundStyle(p.mutedForeground)
                                    }
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                    .padding(12)
                                }
                                .buttonStyle(.plain)
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
                                                Text(entry.map { "\($0.partOfSpeech.displayValue)  •  \($0.phonetic)" } ?? "Search again for word details")
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
                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 7) {
                        filterTile("globe", "Language", vm.filterLanguage, key: "language", p: p)
                        filterTile("book", "Part of Speech", vm.filterPos, key: "pos", p: p)
                        filterTile("chart.bar", "Difficulty", vm.filterDifficulty, key: "difficulty", p: p)
                        filterTile("star", "Sort By", vm.filterSort, key: "sort", p: p)
                    }
                        .padding(.horizontal, 16)
                    }
                }

                CountrysideQuoteBanner(
                    quote: "A word is a doorway to a bigger world.",
                    attribution: "Lexora",
                    illustrationBase: "ill-banner-bridge"
                )
                .padding(.horizontal, 16)
            }
            .padding(.bottom, 160)
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
                    .font(.lexiconBody(10.5))
                    .foregroundStyle(p.mutedForeground)
                    .lineLimit(1)
                    .minimumScaleFactor(0.8)
                Text("\(value) ⌄")
                    .font(.lexiconBody(10.5, weight: .medium))
                    .foregroundStyle(p.foreground)
                    .lineLimit(1)
                    .minimumScaleFactor(0.75)
            }
            .frame(width: 112)
            .padding(.vertical, 9)
            .background(p.card)
            .clipShape(RoundedRectangle(cornerRadius: 13, style: .continuous))
            .overlay(RoundedRectangle(cornerRadius: 13, style: .continuous).stroke(p.border, lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}
