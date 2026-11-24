//
//  WordOfTheDayCard.swift
//  Lexicon
//

import SwiftUI

struct WordOfTheDayCard: View {
    let word: WordEntry
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @State private var measuredWidth: CGFloat = 390

    private var cardHeight: CGFloat {
        dynamicTypeSize.isAccessibilitySize ? 186 : (measuredWidth < 360 ? 144 : 118)
    }

    private var isDark: Bool { scheme == .dark }

    private var leftBg: Color {
        isDark ? .clear : LexiconColors.primary
    }

    private var rightBg: Color {
        isDark ? .clear : LexiconColors.wotdRightLight
    }

    private var cardBorder: Color {
        isDark ? Color(hex: "B6A486").opacity(0.19) : lexiconPalette(scheme).border
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
        Group {
            GeometryReader { geo in
                let totalW = geo.size.width
                let height = cardHeight
                let compact = totalW < 360
                let leftW = totalW * 0.39
                let rightW = totalW - leftW - 1

                HStack(spacing: 0) {
                    VStack(alignment: .leading, spacing: 0) {
                        HStack(spacing: 4) {
                            Text("WORD OF THE DAY")
                                .font(.lexiconBody(8.5, weight: .medium))
                                .tracking(0.5)
                                .foregroundStyle(labelGold)
                                .lineLimit(1)
                                .minimumScaleFactor(0.78)

                            if !compact {
                                BotanicalSprig(color: labelGold)
                                    .frame(width: 13, height: 13)
                                    .lexiconDecorative()
                            }
                        }

                        Spacer(minLength: 4)

                        HStack(spacing: 4) {
                            Text(word.word)
                                .font(.lexiconDisplay(totalW < 350 ? 18.5 : 21.5, weight: .regular))
                                .foregroundStyle(wordColor)
                                .lineLimit(dynamicTypeSize.isAccessibilitySize ? 2 : 1)
                                .minimumScaleFactor(0.70)

                            Button { vm.pronounce(word.word) } label: {
                                Image(systemName: "speaker.wave.2")
                                    .font(.system(size: 14, weight: .regular))
                                    .foregroundStyle(labelGold)
                                    .frame(width: 32, height: 32)
                                    .contentShape(Rectangle())
                            }
                            .buttonStyle(.plain)
                            .accessibilityLabel("Pronounce \(word.word)")
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
                    .padding(.vertical, 14)
                    .frame(width: leftW, height: height, alignment: .leading)
                    .background(leftBg)

                    Rectangle()
                        .fill(isDark ? cardBorder : LexiconColors.foreground.opacity(0.08))
                        .frame(width: 1, height: height)

                    ZStack(alignment: .leading) {
                        rightBg

                        if !isDark {
                            HStack(spacing: 0) {
                                Spacer(minLength: 0)
                                Image("wotd-bg-light")
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: rightW * 0.96, height: height, alignment: .bottomTrailing)
                                    .accessibilityHidden(true)
                            }
                            LinearGradient(
                                stops: [
                                    .init(color: rightBg.opacity(0.96), location: 0),
                                    .init(color: rightBg.opacity(0.72), location: 0.38),
                                    .init(color: .clear, location: 0.83)
                                ], startPoint: .leading, endPoint: .trailing
                            )
                            .frame(width: rightW, height: height)
                            .allowsHitTesting(false)
                        }

                        VStack(alignment: .leading, spacing: 0) {
                            Text(word.definitions.first ?? "")
                                .font(.lexiconBody(11.5))
                                .foregroundStyle(defColor)
                                .lineSpacing(2.5)
                                .lineLimit(4)
                                .frame(width: isDark ? max(1, rightW * (dynamicTypeSize.isAccessibilitySize ? 0.88 : 0.69) - 17) : rightW * 0.69, alignment: .leading)

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
                    .frame(width: rightW, height: height)
                }
                .background {
                    if isDark {
                        WordOfDayNightScene(largeText: dynamicTypeSize.isAccessibilitySize)
                    }
                }
            }
            .frame(height: cardHeight)
            .frame(maxWidth: .infinity)
            .background {
                GeometryReader { measure in
                    Color.clear
                        .onAppear { measuredWidth = measure.size.width }
                        .onChange(of: measure.size.width) { measuredWidth = $0 }
                }
            }
            .clipShape(RoundedRectangle(cornerRadius: isDark ? 12 : 16, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: isDark ? 12 : 16, style: .continuous)
                    .stroke(cardBorder, lineWidth: 1)
            )
            .shadow(
                color: isDark ? .clear : LexiconColors.foreground.opacity(0.08),
                radius: 5,
                x: 0,
                y: 2
            )
        }
        .contentShape(RoundedRectangle(cornerRadius: isDark ? 12 : 16))
        .onTapGesture {
            UIImpactFeedbackGenerator(style: .medium).impactOccurred()
            vm.navigate(to: .wordOfTheDay, word: word)
        }
        .accessibilityElement(children: .contain)
        .accessibilityLabel(Text("Word of the Day. \(word.word). \(word.phonetic). \(word.partOfSpeech.displayValue). \(word.definitions.first ?? "")"))
        .accessibilityHint(Text("Opens the dictionary entry"))
        .accessibilityAddTraits(.isButton)
    }
}


/// The entire dark card shares one painted reading surface.
private struct WordOfDayNightScene: View {
    let largeText: Bool

    var body: some View {
        GeometryReader { geo in
            let width = geo.size.width
            let height = geo.size.height
            ZStack(alignment: .trailing) {
                Color(hex: "0B141C")
                Image("wotd-home-dark")
                    .resizable()
                    .scaledToFill()
                    .frame(width: width * 0.70, height: height, alignment: .trailing)
                    .clipped()
                    .saturation(0.84)
                    .colorMultiply(Color(red: 0.90, green: 0.92, blue: 0.96))
                    .frame(width: width, height: height, alignment: .trailing)
                    .mask {
                        LinearGradient(stops: [
                            .init(color: .clear, location: 0),
                            .init(color: .clear, location: 0.28),
                            .init(color: .black.opacity(0.04), location: 0.40),
                            .init(color: .black.opacity(largeText ? 0.10 : 0.25), location: 0.54),
                            .init(color: .black.opacity(largeText ? 0.30 : 0.68), location: 0.70),
                            .init(color: .black.opacity(largeText ? 0.58 : 0.98), location: 0.88),
                            .init(color: .black.opacity(largeText ? 0.54 : 0.88), location: 1)
                        ], startPoint: .leading, endPoint: .trailing)
                    }
                    .mask {
                        LinearGradient(stops: [
                            .init(color: .black.opacity(0.52), location: 0),
                            .init(color: .black.opacity(0.90), location: 0.16),
                            .init(color: .black, location: 0.70),
                            .init(color: .black.opacity(0.54), location: 1)
                        ], startPoint: .top, endPoint: .bottom)
                    }
            }
        }
        .allowsHitTesting(false)
        .accessibilityHidden(true)
    }
}
