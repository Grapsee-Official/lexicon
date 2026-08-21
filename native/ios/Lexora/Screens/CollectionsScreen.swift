//
//  CollectionsScreen.swift
//  Lexora
//
//  Translation of `src/components/lexora/screens/collections-screen.tsx`.
//
//  Two states:
//    EMPTY  — watercolour empty state (cat-literature illustration, bookmark
//             badge, "Your collection awaits" headline, description, and a
//             "Saved words sync offline" pill).
//    LIST   — "Saved Words" title + count, then a single grouped card of rows.
//             Each row: filled bookmark icon (accent/highlight) · word title
//             (serif) · part of speech (italic, lowercase) · definition (1 line)
//             · volume icon · trash button. Rows animate in with a staggered
//             opacity + horizontal slide.
//
//  Tap row → vm.openWord(entry); tap trash → vm.toggleSave(word.id).
//  Observes vm.savedWordIds and filters the master dictionary.
//

import SwiftUI

struct CollectionsScreen: View {
    @EnvironmentObject private var vm: LexoraViewModel
    @Environment(\.colorScheme) private var scheme

    /// Drives the staggered mount-in animation.
    @State private var appeared: Bool = false

    /// Filter the master dictionary by saved ids, deduped + alpha sorted.
    private var savedWords: [WordEntry] {
        let savedSet = Set(vm.savedWordIds)
        return LexoraData.dictionary
            .filter { savedSet.contains($0.id) }
            .sorted { $0.word < $1.word }
    }

    var body: some View {
        Group {
            if savedWords.isEmpty {
                EmptyCollectionState()
                    .transition(.opacity.combined(with: .move(edge: .top)))
            } else {
                savedWordsList
                    .transition(.opacity)
            }
        }
        .animation(.easeInOut(duration: 0.25), value: savedWords.count)
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
        .background(LexoraColors.background)
    }

    // MARK: - Saved words list

    private var savedWordsList: some View {
        ScrollView {
            VStack(spacing: 12) {
                // Title row
                HStack(alignment: .firstTextBaseline) {
                    Text("Saved Words")
                        .font(.lexoraDisplay(24, weight: .semibold))
                        .foregroundStyle(lexoraPalette(scheme).foreground)
                    Spacer()
                    Text("\(savedWords.count) \(savedWords.count == 1 ? "word" : "words")")
                        .font(.lexoraBody(11))
                        .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                }
                .padding(.horizontal, 4)
                .opacity(appeared ? 1 : 0)

                // Grouped card with rows
                VStack(spacing: 0) {
                    ForEach(Array(savedWords.enumerated()), id: \.element.id) { idx, entry in
                        SavedRow(
                            entry: entry,
                            isFirst: idx == 0,
                            onOpen: { vm.openWord(entry) },
                            onRemove: { vm.toggleSave(entry.id) }
                        )
                        .opacity(appeared ? 1 : 0)
                        .offset(x: appeared ? 0 : -8)
                        .animation(
                            .easeOut(duration: 0.25).delay(Double(idx) * 0.04),
                            value: appeared
                        )
                    }
                }
                .background(lexoraPalette(scheme).card)
                .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                .overlay(
                    RoundedRectangle(cornerRadius: 16, style: .continuous)
                        .stroke(lexoraPalette(scheme).border, lineWidth: 1)
                )
                .shadow(color: Color.black.opacity(0.04), radius: 4, x: 0, y: 2)
            }
            .padding(.horizontal, 16)
            .padding(.top, 12)
            .padding(.bottom, 120) // clear floating bottom nav
        }
        .onAppear { appeared = true }
        .onDisappear { appeared = false }
    }
}

// MARK: - Empty state

private struct EmptyCollectionState: View {
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        VStack(spacing: 0) {
            Spacer(minLength: 48)

            // Image + bookmark badge
            ZStack(alignment: .bottomTrailing) {
                // Soft glow behind the illustration (web `bg-primary/10 blur-2xl`)
                Circle()
                    .fill(lexoraPalette(scheme).primary.opacity(0.10))
                    .frame(width: 160, height: 160)
                    .blur(radius: 28)

                // Illustration (cat-literature) with rounded corners + fade edges
                Image("cat-literature")
                    .resizable()
                    .scaledToFill()
                    .frame(width: 160, height: 160)
                    .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                    .opacity(scheme == .dark ? 0.60 : 0.85)
                    .accessibilityHidden(true)

                // Bookmark badge (overlapping bottom-right corner)
                Image(systemName: "bookmark.fill")
                    .font(.system(size: 18, weight: .regular))
                    .foregroundStyle(lexoraPalette(scheme).highlight)
                    .frame(width: 44, height: 44)
                    .background(lexoraPalette(scheme).card)
                    .clipShape(Circle())
                    .overlay(
                        Circle().stroke(lexoraPalette(scheme).border, lineWidth: 1)
                    )
                    .shadow(color: Color.black.opacity(0.08), radius: 4, x: 0, y: 2)
                    .offset(x: 8, y: 8)
            }
            .frame(width: 180, height: 180)
            .padding(.bottom, 20)

            // Headline
            Text("Your collection awaits")
                .font(.lexoraDisplay(20, weight: .semibold))
                .foregroundStyle(lexoraPalette(scheme).foreground)
                .multilineTextAlignment(.center)

            // Description
            Text("Tap the bookmark icon on any word to save it here. Build your own private herbarium of language.")
                .font(.lexoraBody(13))
                .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                .multilineTextAlignment(.center)
                .lineSpacing(3)
                .padding(.horizontal, 32)
                .padding(.top, 8)

            Spacer(minLength: 28)

            // Offline-sync pill
            HStack(spacing: 6) {
                Image(systemName: "sparkles")
                    .font(.system(size: 11, weight: .regular))
                    .foregroundStyle(lexoraPalette(scheme).highlight)
                Text("Saved words sync offline across all your devices")
                    .font(.lexoraBody(11))
                    .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                    .lineLimit(1)
                    .minimumScaleFactor(0.85)
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(lexoraPalette(scheme).card)
            .clipShape(Capsule())
            .overlay(Capsule().stroke(lexoraPalette(scheme).border, lineWidth: 1))

            Spacer(minLength: 0)
        }
        .frame(maxWidth: .infinity)
        .padding(.horizontal, 32)
    }
}

// MARK: - Saved word row

private struct SavedRow: View {
    let entry: WordEntry
    let isFirst: Bool
    let onOpen: () -> Void
    let onRemove: () -> Void
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            onOpen()
        } label: {
            HStack(spacing: 12) {
                // Filled bookmark icon (accent)
                Image(systemName: "bookmark.fill")
                    .font(.system(size: 13, weight: .regular))
                    .foregroundStyle(lexoraPalette(scheme).highlight)
                    .accessibilityHidden(true)

                VStack(alignment: .leading, spacing: 2) {
                    HStack(alignment: .firstTextBaseline, spacing: 6) {
                        Text(entry.word)
                            .font(.lexoraDisplay(15, weight: .semibold))
                            .foregroundStyle(lexoraPalette(scheme).foreground)
                            .lineLimit(1)
                        Text(entry.partOfSpeech.displayValue)
                            .font(.lexoraBody(10).italic())
                            .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                            .lineLimit(1)
                    }
                    Text(entry.definitions.first ?? "")
                        .font(.lexoraLong(12))
                        .foregroundStyle(lexoraPalette(scheme).foreground.opacity(0.7))
                        .lineLimit(1)
                }

                Spacer(minLength: 4)

                // Volume icon
                Image(systemName: "speaker.wave.2")
                    .font(.system(size: 13, weight: .regular))
                    .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                    .accessibilityHidden(true)

                // Remove (trash) button
                Button {
                    UIImpactFeedbackGenerator(style: .light).impactOccurred()
                    onRemove()
                } label: {
                    Image(systemName: "trash")
                        .font(.system(size: 14, weight: .regular))
                        .foregroundStyle(lexoraPalette(scheme).mutedForeground.opacity(0.6))
                        .frame(width: 28, height: 28)
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel(Text("Remove \(entry.word)"))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
            .frame(maxWidth: .infinity, alignment: .leading)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .overlay(alignment: .top) {
            if !isFirst {
                Rectangle()
                    .fill(lexoraPalette(scheme).border)
                    .frame(height: 1)
                    .padding(.horizontal, 16)
            }
        }
    }
}

#Preview {
    CollectionsScreen()
        .environmentObject(LexoraViewModel())
}
