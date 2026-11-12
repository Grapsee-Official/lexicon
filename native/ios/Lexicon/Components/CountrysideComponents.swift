//
//  CountrysideComponents.swift
//  Lexicon
//

import SwiftUI

func countrysideImageName(_ base: String, isDark: Bool) -> String {
    let clean = base.replacingOccurrences(of: "_", with: "-")
    return isDark ? "\(clean)-dark" : clean
}

struct CountrysideHeroHeader: View {
    let title: String
    let subtitle: String
    let illustrationBase: String
    let onHistoryClick: () -> Void
    var onLogoToggleTheme: () -> Void = {}
    var heroHeight: CGFloat = 144

    @Environment(\.colorScheme) private var scheme
    private var isDark: Bool { scheme == .dark }

    var body: some View {
        GeometryReader { geo in
            let w = geo.size.width
            ZStack(alignment: .topLeading) {
                HStack(spacing: 0) {
                    Spacer(minLength: 0)
                    Image(countrysideImageName(illustrationBase, isDark: isDark))
                        .resizable()
                        .scaledToFit()
                        .frame(width: w * 0.66, height: heroHeight, alignment: .bottomTrailing)
                        .offset(x: 6, y: 4)
                        .accessibilityHidden(true)
                }
                .frame(width: w, height: heroHeight)

                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 8) {
                        Button(action: onLogoToggleTheme) {
                            BotanicalSprig(color: isDark ? LexiconColors.accentDark : LexiconColors.primary)
                                .frame(width: 25, height: 25)
                        }
                        .buttonStyle(.plain)

                        Text(title)
                            .font(.lexiconDisplay(title.count > 14 ? 23 : 27, weight: .regular))
                            .tracking(-0.3)
                            .foregroundStyle(lexiconPalette(scheme).foreground)
                            .lineLimit(2)
                    }

                    Text(subtitle)
                        .font(.lexiconLong(11.5))
                        .foregroundStyle(lexiconPalette(scheme).mutedForeground)
                        .lineSpacing(2)
                        .lineLimit(2)
                }
                .frame(width: w * 0.58, alignment: .leading)
                .padding(.leading, 16)
                .padding(.top, 10)

                HStack {
                    Spacer()
                    Button(action: onHistoryClick) {
                        Image(systemName: "clock.arrow.circlepath")
                            .font(.system(size: 15, weight: .regular))
                            .foregroundStyle(isDark ? LexiconColors.accentDark : LexiconColors.foreground.opacity(0.78))
                            .frame(width: 34, height: 34)
                            .background(lexiconPalette(scheme).searchPill.opacity(0.76))
                            .clipShape(Circle())
                            .overlay(Circle().stroke(lexiconPalette(scheme).border, lineWidth: 1))
                    }
                    .buttonStyle(.plain)
                }
                .padding(.top, 8)
                .padding(.trailing, 16)
            }
        }
        .frame(height: heroHeight)
    }
}

struct CountrysideSearchPill: View {
    @Binding var text: String
    let placeholder: String
    var onSubmit: () -> Void = {}
    var showCameraMic: Bool = false
    var showFilter: Bool = false
    var showMicOnly: Bool = false
    var onFilterClick: () -> Void = {}

    @Environment(\.colorScheme) private var scheme
    private var isDark: Bool { scheme == .dark }

    var body: some View {
        let iconTint = isDark ? LexiconColors.accentDark : LexiconColors.foreground.opacity(0.65)

        HStack(spacing: 9) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 15, weight: .regular))
                .foregroundStyle(iconTint)

            TextField(placeholder, text: $text)
                .font(.lexiconBody(13.5))
                .foregroundStyle(lexiconPalette(scheme).foreground)
                .submitLabel(.search)
                .onSubmit { onSubmit() }

            if !text.isEmpty {
                Button {
                    text = ""
                } label: {
                    Image(systemName: "xmark")
                        .font(.system(size: 9.5, weight: .semibold))
                        .foregroundStyle(lexiconPalette(scheme).foreground)
                        .frame(width: 19, height: 19)
                        .background(lexiconPalette(scheme).border)
                        .clipShape(Circle())
                }
                .buttonStyle(.plain)
            }

            if showCameraMic {
                Image(systemName: "camera")
                    .font(.system(size: 14.5))
                    .foregroundStyle(iconTint)
                Image(systemName: "mic")
                    .font(.system(size: 14.5))
                    .foregroundStyle(iconTint)
            } else if showMicOnly {
                Image(systemName: "mic")
                    .font(.system(size: 15))
                    .foregroundStyle(iconTint)
            }

            if showFilter {
                Rectangle()
                    .fill(lexiconPalette(scheme).border)
                    .frame(width: 1, height: 15)
                Button(action: onFilterClick) {
                    Image(systemName: "slider.horizontal.3")
                        .font(.system(size: 14.5))
                        .foregroundStyle(iconTint)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal, 15)
        .padding(.vertical, 9.5)
        .background(lexiconPalette(scheme).searchPill)
        .clipShape(Capsule())
        .overlay(Capsule().stroke(lexiconPalette(scheme).border, lineWidth: 1))
        .shadow(color: isDark ? .clear : LexiconColors.foreground.opacity(0.09), radius: 5, x: 0, y: 2)
    }
}

struct CountrysideFilterPills: View {
    let items: [(label: String, systemIcon: String?)]
    let selected: String
    let onSelect: (String) -> Void

    @Environment(\.colorScheme) private var scheme
    private var isDark: Bool { scheme == .dark }

    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 7) {
                ForEach(items, id: \.label) { item in
                    let active = item.label == selected
                    let bg: Color = active
                        ? (isDark ? LexiconColors.accentDark : LexiconColors.primary)
                        : lexiconPalette(scheme).card
                    let fg: Color = active
                        ? (isDark ? LexiconColors.backgroundDark : .white)
                        : lexiconPalette(scheme).foreground.opacity(0.86)

                    Button {
                        UIImpactFeedbackGenerator(style: .light).impactOccurred()
                        onSelect(item.label)
                    } label: {
                        HStack(spacing: 5) {
                            if let ic = item.systemIcon {
                                Image(systemName: ic)
                                    .font(.system(size: 11, weight: .regular))
                            }
                            Text(item.label)
                                .font(.lexiconBody(11.5, weight: active ? .medium : .regular))
                        }
                        .foregroundStyle(fg)
                        .padding(.horizontal, 13)
                        .padding(.vertical, 6)
                        .background(bg)
                        .clipShape(Capsule())
                        .overlay(
                            Capsule().stroke(
                                active ? Color.clear : lexiconPalette(scheme).border,
                                lineWidth: 1
                            )
                        )
                    }
                    .buttonStyle(.plain)
                }
            }
            .padding(.horizontal, 16)
        }
    }
}

struct BotanicalSectionTitle: View {
    let title: String
    var sprigAfterTitle: Bool = false
    var actionLabel: String? = "View all"
    var isClearAction: Bool = false
    var onAction: () -> Void = {}

    @Environment(\.colorScheme) private var scheme
    private var isDark: Bool { scheme == .dark }

    var body: some View {
        let sprigColor = isDark ? LexiconColors.accentDark : LexiconColors.highlight
        let actionColor = isDark ? LexiconColors.accentDark : LexiconColors.foreground.opacity(0.76)

        HStack {
            HStack(spacing: 6) {
                if !sprigAfterTitle {
                    BotanicalSprig(color: sprigColor)
                        .frame(width: 16, height: 16)
                }
                Text(title)
                    .font(.lexiconDisplay(17, weight: .regular))
                    .tracking(-0.2)
                    .foregroundStyle(lexiconPalette(scheme).foreground)
                if sprigAfterTitle {
                    BotanicalSprig(color: sprigColor)
                        .frame(width: 16, height: 16)
                }
            }

            Spacer()

            if let action = actionLabel {
                Button(action: onAction) {
                    HStack(spacing: 4) {
                        Text(action)
                            .font(.lexiconBody(11))
                        Image(systemName: isClearAction ? "trash" : "arrow.right")
                            .font(.system(size: 10.5, weight: .medium))
                    }
                    .foregroundStyle(actionColor)
                }
                .buttonStyle(.plain)
            }
        }
        .padding(.horizontal, 16)
    }
}

struct CountrysideQuoteBanner: View {
    let quote: String
    var attribution: String = "Lexicon"
    var illustrationBase: String = "ill-banner-bridge"

    @Environment(\.colorScheme) private var scheme
    private var isDark: Bool { scheme == .dark }

    var body: some View {
        let cardBg = lexiconPalette(scheme).card
        GeometryReader { geo in
            let w = geo.size.width
            ZStack(alignment: .leading) {
                cardBg

                HStack(spacing: 0) {
                    Spacer(minLength: 0)
                    Image(countrysideImageName(illustrationBase, isDark: isDark))
                        .resizable()
                        .scaledToFill()
                        .frame(width: w * 0.58, height: 92)
                        .clipped()
                }

                LinearGradient(
                    stops: [
                        .init(color: cardBg, location: 0.0),
                        .init(color: cardBg.opacity(0.94), location: 0.46),
                        .init(color: .clear, location: 0.74),
                    ],
                    startPoint: .leading,
                    endPoint: .trailing
                )

                VStack(alignment: .leading, spacing: 4) {
                    Text("“\(quote)”")
                        .font(.lexiconDisplay(13, weight: .regular).italic())
                        .foregroundStyle(lexiconPalette(scheme).foreground)
                        .lineLimit(2)
                    Text("— \(attribution) —")
                        .font(.lexiconBody(10))
                        .tracking(0.5)
                        .foregroundStyle(lexiconPalette(scheme).highlight)
                }
                .frame(width: w * 0.58, alignment: .leading)
                .padding(.leading, 16)
            }
        }
        .frame(height: 92)
        .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
        .overlay(
            RoundedRectangle(cornerRadius: 16, style: .continuous)
                .stroke(lexiconPalette(scheme).border, lineWidth: 1)
        )
    }
}
