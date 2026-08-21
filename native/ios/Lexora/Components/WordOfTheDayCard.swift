//
//  WordOfTheDayCard.swift
//  Lexora
//
//  Translation of `src/components/lexora/word-of-the-day-card.tsx`.
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
    @EnvironmentObject private var vm: LexoraViewModel
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
                            .foregroundStyle(LexoraColors.card.opacity(0.8))
                        Text("WORD OF THE DAY")
                            .font(.lexoraBody(8.5, weight: .semibold))
                            .tracking(0.7)
                            .foregroundStyle(LexoraColors.card.opacity(0.8))
                            .lineLimit(1)
                            .fixedSize(horizontal: true, vertical: false)
                    }

                    // Word title + audio button (single line).
                    HStack(spacing: 4) {
                        Text(word.word)
                            .font(.lexoraDisplay(17, weight: .semibold))
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
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel(Text("Listen to \(word.word)"))
                    }

                    // Phonetic (italic).
                    Text(word.phonetic)
                        .font(.lexoraBody(10).italic())
                        .foregroundStyle(.white.opacity(0.6))
                        .lineLimit(1)

                    // Part of speech (lowercased).
                    Text(word.partOfSpeech.displayValue)
                        .font(.lexoraBody(11))
                        .foregroundStyle(.white.opacity(0.7))
                        .lineLimit(1)
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 14)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(LexoraColors.primary)

                // ── RIGHT 60% — solid cream bg, brand-green text ──
                ZStack {
                    // Solid #FAF3E9 — the WOTD right panel is always cream
                    // (matches the locked web `bg-[#FAF3E9]`).
                    LexoraColors.background

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
                            .font(.lexoraLong(13))
                            .foregroundStyle(LexoraColors.primary)
                            .lineSpacing(2)
                            .frame(maxWidth: .infinity, alignment: .leading)

                        HStack(spacing: 4) {
                            Text("Explore more")
                            Image(systemName: "arrow.right")
                                .font(.system(size: 10, weight: .semibold))
                        }
                        .font(.lexoraBody(11, weight: .medium))
                        .foregroundStyle(LexoraColors.primary)
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
                    .stroke(lexoraPalette(scheme).border, lineWidth: 1)
            )
            .shadow(color: Color.black.opacity(0.06), radius: 6, x: 0, y: 2)
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    WordOfTheDayCard(word: LexoraData.wordOfTheDay)
        .environmentObject(LexoraViewModel())
        .padding()
        .background(LexoraColors.background)
}
