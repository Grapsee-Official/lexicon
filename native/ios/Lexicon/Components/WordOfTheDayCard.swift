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
        if dynamicTypeSize.isAccessibilitySize { return 186 }
        if (word.definitions.first ?? "").count > 95 { return max(140, measuredWidth * 175 / 531 * 1.18) }
        return max(96, measuredWidth * 175 / 531)
    }

    private var isDark: Bool { scheme == .dark }
    private var stacked: Bool { dynamicTypeSize >= .xxLarge || measuredWidth < 250 || (word.definitions.first ?? "").count > 155 || word.word.count > 16 }

    private var leftBg: Color {
        isDark ? .clear : LexiconColors.primary
    }

    private var rightBg: Color {
        isDark ? .clear : LexiconColors.wotdRightLight
    }

    private var cardBorder: Color {
        isDark ? Color(hex: "B6A486").opacity(0.17) : Color(hex: "A58A61").opacity(0.28)
    }

    private var labelGold: Color {
        isDark ? Color(hex: "D0AD75") : LexiconColors.wotdGoldLight
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
        isDark ? Color(hex: "D0AD75") : LexiconColors.primary
    }

    var body: some View {
        Group {
          if stacked {
            accessibleReadingCard
                .background {
                    GeometryReader { measure in
                        Color.clear.onAppear { measuredWidth = measure.size.width }
                            .onChange(of: measure.size.width) { measuredWidth = $0 }
                    }
                }
          } else {
            GeometryReader { geo in
                let totalW = geo.size.width
                let height = cardHeight
                let compact = totalW < (isDark ? 328 : 360)
                let leftW = totalW * 0.39
                let rightW = totalW - leftW - 1
                let editorial = !dynamicTypeSize.isAccessibilitySize
                let shortDefinition = editorial && (word.definitions.first ?? "").count <= 72

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
                                .lineLimit(2)
                                .minimumScaleFactor(0.70)

                            Button { vm.pronounce(word.word) } label: {
                                Image(systemName: "speaker.wave.2")
                                    .font(.system(size: 14, weight: .regular))
                                    .foregroundStyle(labelGold)
                                    .frame(width: isDark && !dynamicTypeSize.isAccessibilitySize ? 23 : 32,
                                           height: isDark && !dynamicTypeSize.isAccessibilitySize ? 23 : 32)
                                    .contentShape(Rectangle())
                            }
                            .buttonStyle(.plain)
                            .accessibilityLabel("Pronounce \(word.word)")
                        }

                        Spacer(minLength: 4)

                        VStack(alignment: .leading, spacing: 2) {
                            Text(word.phonetic)
                                .font(.lexiconBody(editorial ? 9.5 : 10.5))
                                .foregroundStyle(phoneticColor)
                                .lineLimit(1)

                            Text(word.partOfSpeech.displayValue)
                                .font(.lexiconBody(editorial ? 10 : 11))
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
                            CountrysidePainting(name: countrysideImageName("wotd-bg-light", isDark: false, scene: "home-daily"),
                                fadeLeft: 0.25, fadeBottom: 0)
                                .frame(width: rightW * 0.96, height: height)
                                .frame(maxWidth: .infinity, alignment: .trailing)
                        }

                        VStack(alignment: .leading, spacing: shortDefinition ? 10 : 0) {
                            Text(word.definitions.first ?? "")
                                .font(.lexiconBody(editorial ? 9.5 : 11.5))
                                .foregroundStyle(defColor)
                                .lineSpacing(editorial ? 1.5 : 2.5)
                                .lineLimit(6)
                                .frame(width: isDark ? max(1, rightW * (dynamicTypeSize.isAccessibilitySize ? 0.88 : 0.79) - 17) : rightW * 0.69, alignment: .leading)

                            if !shortDefinition { Spacer(minLength: 4) }

                            HStack(spacing: 4) {
                                Text("Explore more")
                                Image(systemName: "arrow.right")
                                    .font(.system(size: 10, weight: .medium))
                            }
                            .font(.lexiconBody(editorial ? 9.5 : 11, weight: .medium))
                            .foregroundStyle(exploreColor)
                        }
                        .padding(.leading, 13)
                        .padding(.vertical, editorial ? 11 : 13)
                        .frame(maxHeight: .infinity, alignment: shortDefinition ? .center : .top)
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

    // Accessibility-sized text gets a real reading column, not a taller
    // version of the narrow reference spread. Never truncate the definition.
    private var accessibleReadingCard: some View {
        let p = lexiconPalette(scheme)
        let accent = isDark ? labelGold : LexiconColors.primary
        return VStack(spacing: 0) {
            VStack(alignment: .leading, spacing: 8) {
                Text("WORD OF THE DAY").font(.lexiconBody(10, weight: .medium)).foregroundStyle(accent)
                HStack {
                    Text(word.word).font(.lexiconDisplay(22)).foregroundStyle(p.foreground)
                        .fixedSize(horizontal: false, vertical: true)
                    Spacer(minLength: 4)
                    Button { vm.pronounce(word.word) } label: {
                        Image(systemName: "speaker.wave.2").foregroundStyle(accent)
                            .frame(width: 44, height: 44).contentShape(Rectangle())
                    }.buttonStyle(.plain).accessibilityLabel("Pronounce \(word.word)")
                }
                Text(word.phonetic).font(.lexiconBody(11)).foregroundStyle(p.mutedForeground)
                Text(word.partOfSpeech.displayValue).font(.lexiconBody(11)).foregroundStyle(accent)
                Text(word.definitions.first ?? "").font(.lexiconBody(13)).foregroundStyle(p.foreground)
                    .fixedSize(horizontal: false, vertical: true)
                Label("Explore more", systemImage: "arrow.right")
                    .font(.lexiconBody(12)).foregroundStyle(accent)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(16)
            if isDark {
                WordOfDayNightScene(largeText: false, fullWidth: true).frame(height: 128)
            } else {
                Image(countrysideImageName("wotd-bg-light", isDark: false, scene: "home-daily")).resizable().scaledToFit()
                    .frame(height: 128).frame(maxWidth: .infinity, alignment: .trailing)
                    .accessibilityHidden(true)
            }
        }
        .background(isDark ? Color(hex: "08121A") : LexiconColors.wotdRightLight)
        .clipShape(RoundedRectangle(cornerRadius: isDark ? 12 : 16))
        .overlay(RoundedRectangle(cornerRadius: isDark ? 12 : 16).stroke(cardBorder, lineWidth: 1))
    }
}


/// The entire dark card shares one painted reading surface.
private struct WordOfDayNightScene: View {
    let largeText: Bool
    var fullWidth: Bool = false

    var body: some View {
        GeometryReader { geo in
            ZStack(alignment: .trailing) {
                Color(hex: "08121A")
                let width = geo.size.width * (fullWidth ? 1 : (largeText ? 0.57 : 0.61))
                let artWidth = max(width, geo.size.height * 323 / 174)
                Image("ref-home-daily")
                    .resizable().renderingMode(.original)
                    .frame(width: artWidth, height: artWidth * 174 / 323)
                    .frame(width: width, height: geo.size.height, alignment: .bottomTrailing)
                    .clipped()
            }
        }
        .allowsHitTesting(false)
        .accessibilityHidden(true)
    }
}
