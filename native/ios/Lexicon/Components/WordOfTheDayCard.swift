//
//  WordOfTheDayCard.swift
//  Lexicon
//

import SwiftUI

struct WordOfTheDayCard: View {
    let word: WordEntry
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme

    private var isDark: Bool { scheme == .dark }

    private var leftBg: Color {
        isDark ? LexiconColors.wotdLeftDark : LexiconColors.primary
    }

    private var rightBg: Color {
        isDark ? LexiconColors.wotdRightDark : LexiconColors.wotdRightLight
    }

    private var labelGold: Color {
        isDark ? LexiconColors.accentDark : LexiconColors.wotdGoldLight
    }

    private var wordColor: Color {
        isDark ? LexiconColors.foregroundDark : .white
    }

    private var phoneticColor: Color {
        isDark ? LexiconColors.mutedForegroundDark : .white.opacity(0.78)
    }

    private var defColor: Color {
        isDark ? LexiconColors.foregroundDark.opacity(0.90) : LexiconColors.foreground.opacity(0.88)
    }

    private var exploreColor: Color {
        isDark ? LexiconColors.accentDark : LexiconColors.primary
    }

    var body: some View {
        Button {
            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
            vm.openWord(word)
        } label: {
            GeometryReader { geo in
                let totalW = geo.size.width
                let leftW = totalW * 0.41
                let rightW = totalW - leftW - 1

                HStack(spacing: 0) {
                    VStack(alignment: .leading, spacing: 0) {
                        HStack(spacing: 4) {
                            Text("WORD OF THE DAY")
                                .font(.lexiconBody(8.5, weight: .medium))
                                .tracking(0.5)
                                .foregroundStyle(labelGold)
                                .lineLimit(1)
                                .fixedSize(horizontal: true, vertical: false)

                            BotanicalSprig(color: labelGold)
                                .frame(width: 13, height: 13)
                                .lexiconDecorative()
                        }

                        Spacer(minLength: 4)

                        HStack(spacing: 4) {
                            Text(word.word)
                                .font(.lexiconDisplay(22, weight: .regular))
                                .foregroundStyle(wordColor)
                                .lineLimit(1)
                                .minimumScaleFactor(0.78)

                            Image(systemName: "speaker.wave.2")
                                .font(.system(size: 11, weight: .regular))
                                .foregroundStyle(labelGold)
                                .accessibilityHidden(true)
                        }

                        Spacer(minLength: 4)

                        VStack(alignment: .leading, spacing: 2) {
                            Text(word.phonetic)
                                .font(.lexiconBody(10.5))
                                .foregroundStyle(phoneticColor)
                                .lineLimit(1)

                            Text(word.partOfSpeech.displayValue)
                                .font(.lexiconBody(11))
                                .foregroundStyle(labelGold)
                                .lineLimit(1)
                        }
                    }
                    .padding(.horizontal, 13)
                    .padding(.vertical, 12)
                    .frame(width: leftW, height: 132, alignment: .leading)
                    .background(leftBg)

                    Rectangle()
                        .fill(isDark ? LexiconColors.borderDark : LexiconColors.foreground.opacity(0.08))
                        .frame(width: 1, height: 132)

                    ZStack(alignment: .leading) {
                        rightBg

                        HStack(spacing: 0) {
                            Spacer(minLength: 0)
                            Image(isDark ? "wotd-bg-dark" : "wotd-bg-light")
                                .resizable()
                                .scaledToFill()
                                .frame(width: rightW * 0.54, height: 132)
                                .clipped()
                                .accessibilityHidden(true)
                        }

                        VStack(alignment: .leading, spacing: 0) {
                            Text(word.definitions.first ?? "")
                                .font(.lexiconBody(11.5))
                                .foregroundStyle(defColor)
                                .lineSpacing(2.5)
                                .lineLimit(3)
                                .frame(width: rightW * 0.68, alignment: .leading)

                            Spacer(minLength: 4)

                            HStack(spacing: 4) {
                                Text("Explore more")
                                Image(systemName: "arrow.right")
                                    .font(.system(size: 10, weight: .medium))
                            }
                            .font(.lexiconBody(11, weight: .medium))
                            .foregroundStyle(exploreColor)
                        }
                        .padding(.leading, 13)
                        .padding(.vertical, 13)
                    }
                    .frame(width: rightW, height: 132)
                }
            }
            .frame(height: 132)
            .frame(maxWidth: .infinity)
            .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 16, style: .continuous)
                    .stroke(lexiconPalette(scheme).border, lineWidth: 1)
            )
            .shadow(
                color: isDark ? .clear : LexiconColors.foreground.opacity(0.08),
                radius: 5,
                x: 0,
                y: 2
            )
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .combine)
        .accessibilityLabel(Text("Word of the Day. \(word.word). \(word.phonetic). \(word.partOfSpeech.displayValue). \(word.definitions.first ?? "")"))
        .accessibilityHint(Text("Opens the dictionary entry"))
        .accessibilityAddTraits(.isButton)
    }
}
