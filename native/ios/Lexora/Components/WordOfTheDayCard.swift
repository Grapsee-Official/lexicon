//
//  WordOfTheDayCard.swift
//  Lexicon
//
//  Translation of `src/components/lexicon/word-of-the-day-card.tsx`.
//
//  STRICT 2-COLUMN SPLIT (40% left / 60% right):
//    LEFT:  solid #677554, white/cream text. "WORD OF THE DAY" badge SINGLE LINE.
//    RIGHT: solid #FAF3E9, text #677554. Image pinned to right edge with
//           a linear-gradient mask (transparent 0% → black 35%).
//
//  Card height: 140pt. No gradient blending between panels — only the image
//  mask gradient is allowed.
//

import SwiftUI

struct WordOfTheDayCard: View {
    let word: WordEntry
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        Button {
            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
            vm.openWord(word)
        } label: {
            HStack(spacing: 0) {
                // ── LEFT 40% — solid brand green, white/cream text ──
                VStack(alignment: .leading, spacing: 8) {
                    // "WORD OF THE DAY" badge — single line (no wrap).
                    HStack(spacing: 4) {
                        Image(systemName: "leaf")
                            .font(.system(size: 8, weight: .regular))
                            .foregroundStyle(LexiconColors.card.opacity(0.8))
                        Text("WORD OF THE DAY")
                            .font(.lexiconBody(8.5, weight: .semibold))
                            .tracking(0.7)
                            .foregroundStyle(LexiconColors.card.opacity(0.8))
                            .lineLimit(1)
                            .fixedSize(horizontal: true, vertical: false)
                    }

                    // Word title + audio button (single line).
                    // The speaker is a real Button (tappable for sighted
                    // users), but hidden from VoiceOver because (a) it's
                    // only 24×24 visually — below the 44pt minimum even
                    // with the `.lexiconMinTouchTarget()` expansion it
                    // would clip the card layout — and (b) pronunciation
                    // is available from the WordDetailSheet's 44×44
                    // speaker button, so the WOTD speaker is redundant.
                    // The card-level label includes "Tap to open and hear
                    // pronunciation" so the VoiceOver user knows how to
                    // get to the pronunciation.
                    HStack(spacing: 4) {
                        Text(word.word)
                            .font(.lexiconDisplay(17, weight: .semibold))
                            .foregroundStyle(.white)
                            .lineLimit(1)
                            .minimumScaleFactor(0.8)
                        Button {
                            UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        } label: {
                            Image(systemName: "speaker.wave.2")
                                .font(.system(size: 11, weight: .regular))
                                .foregroundStyle(.white.opacity(0.7))
                                .frame(width: 24, height: 24)
                                .lexiconMinTouchTarget()
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel(Text(LexiconAccessibilityLabels.listenTo + " " + word.word))
                        .accessibilityAddTraits(.isButton)
                        .accessibilityHidden(true)
                    }

                    // Phonetic (italic).
                    Text(word.phonetic)
                        .font(.lexiconBody(10).italic())
                        .foregroundStyle(.white.opacity(0.6))
                        .lineLimit(1)

                    // Part of speech (lowercased).
                    Text(word.partOfSpeech.displayValue)
                        .font(.lexiconBody(11))
                        .foregroundStyle(.white.opacity(0.7))
                        .lineLimit(1)
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 14)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(LexiconColors.primary)

                // ── RIGHT 60% — solid cream bg, brand-green text ──
                ZStack {
                    // Solid #FAF3E9 — the WOTD right panel is always cream
                    // (matches the locked web `bg-[#FAF3E9]`).
                    LexiconColors.background

                    // Image pinned to right edge with linear-gradient mask
                    // (transparent 0% → black 35%, fading toward left).
                    HStack {
                        Spacer()
                        Image(scheme == .dark ? "wotd-bg-dark" : "wotd-bg-light")
                            .resizable()
                            .scaledToFill()
                            .frame(width: 110, height: 140)
                            .clipped()
                            .mask(
                                LinearGradient(
                                    gradient: Gradient(stops: [
                                        .init(color: .black.opacity(0), location: 0.0),
                                        .init(color: .black, location: 0.35),
                                        .init(color: .black, location: 1.0),
                                    ]),
                                    startPoint: .leading,
                                    endPoint: .trailing
                                )
                            )
                            .accessibilityHidden(true)
                    }

                    // Definition + "Explore more →" overlay text.
                    VStack(alignment: .leading, spacing: 8) {
                        Text(word.definitions.first ?? "")
                            .font(.lexiconLong(13))
                            .foregroundStyle(LexiconColors.primary)
                            .lineSpacing(2)
                            .frame(maxWidth: .infinity, alignment: .leading)

                        HStack(spacing: 4) {
                            Text("Explore more")
                            Image(systemName: "arrow.right")
                                .font(.system(size: 10, weight: .semibold))
                        }
                        .font(.lexiconBody(11, weight: .medium))
                        .foregroundStyle(LexiconColors.primary)
                        .frame(maxWidth: .infinity, alignment: .trailing)
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 14)
                }
            }
            .frame(height: 140)
            .frame(maxWidth: .infinity)
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 16, style: .continuous)
                    .stroke(lexiconPalette(scheme).border, lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.06), radius: 6, x: 0, y: 2)
        }
        .buttonStyle(.plain)
        // P4-05 — combine the card into ONE VoiceOver element so the
        // screen-reader user hears the whole entry in one swipe:
        // "Word of the Day, Petrichor, /ˈpɛtrɪkɔːr/, noun, the pleasant
        // earthy scent produced when rain falls on dry soil. Double-tap
        // to open the entry." The inner speaker Image is hidden from
        // VoiceOver (see header) to avoid a confusing nested button.
        .accessibilityElement(children: .combine)
        .accessibilityLabel(Text("Word of the Day. \(word.word). \(word.phonetic). \(word.partOfSpeech.displayValue). \(word.definitions.first ?? "")"))
        .accessibilityHint(Text("Opens the dictionary entry"))
        .accessibilityAddTraits(.isButton)
        // Whole card is 140pt tall × full width — well above the 44pt
        // minimum, so no `.lexiconMinTouchTarget()` needed here.
    }
}

#Preview {
    WordOfTheDayCard(word: LexiconData.wordOfTheDay)
        .environmentObject(LexiconViewModel())
        .padding()
        .background(LexiconColors.background)
}
