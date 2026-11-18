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
    var heroHeight: CGFloat = 146

    @Environment(\.colorScheme) private var scheme
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    private var isDark: Bool { scheme == .dark }
    private var responsiveHeight: CGFloat {
        if dynamicTypeSize.isAccessibilitySize { return heroHeight + 64 }
        let phoneSize = UIScreen.main.bounds.width < 600 && UIScreen.main.bounds.height < 940
        return phoneSize ? max(112, heroHeight - 20) : heroHeight
    }

    var body: some View {
        GeometryReader { geo in
            let w = geo.size.width
            ZStack(alignment: .topLeading) {
                HStack(spacing: 0) {
                    Spacer(minLength: 0)
                    Image(countrysideImageName(illustrationBase, isDark: isDark))
                        .resizable()
                        .scaledToFit()
                        .frame(width: w * 0.66, height: responsiveHeight, alignment: .bottomTrailing)
                        .offset(x: 6, y: 4)
                        .accessibilityHidden(true)
                }
                .frame(width: w, height: responsiveHeight)

                LinearGradient(
                    stops: [
                        .init(color: isDark ? LexiconColors.backgroundDark.opacity(0.84)
                            : LexiconColors.background.opacity(0.78), location: 0),
                        .init(color: isDark ? LexiconColors.backgroundDark.opacity(0.46)
                            : LexiconColors.background.opacity(0.42), location: 0.38),
                        .init(color: .clear, location: 0.78),
                    ],
                    startPoint: .leading, endPoint: .trailing
                )
                .frame(width: w, height: responsiveHeight)
                .allowsHitTesting(false)

                if isDark {
                    NocturneAtmosphere()
                        .frame(width: w, height: responsiveHeight)
                    if illustrationBase.replacingOccurrences(of: "_", with: "-") == "ill-hero-cottage" {
                        CottageWindowGlow()
                            .frame(width: w, height: responsiveHeight)
                    }
                }

                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 8) {
                        Button(action: onLogoToggleTheme) {
                            BotanicalSprig(color: isDark ? LexiconColors.accentDark : LexiconColors.primary)
                                .frame(width: 25, height: 25)
                        }
                        .buttonStyle(.plain)

                        Text(title)
                            .font(.lexiconDisplay(w < 390 || title.count > 14 ? 24 : 28, weight: .regular))
                            .tracking(-0.3)
                            .foregroundStyle(lexiconPalette(scheme).foreground)
                            .lineLimit(dynamicTypeSize.isAccessibilitySize ? 3 : 2)
                    }

                    Text(subtitle)
                        .font(.lexiconLong(11.5))
                        .foregroundStyle(lexiconPalette(scheme).mutedForeground)
                        .lineSpacing(2)
                        .lineLimit(dynamicTypeSize.isAccessibilitySize ? 4 : 2)
                }
                .frame(width: w * ((w < 390 || dynamicTypeSize.isAccessibilitySize) ? 0.76 : 0.58), alignment: .leading)
                .padding(.leading, 16)
                .padding(.top, 12)

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
        .frame(height: responsiveHeight)
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
    @FocusState private var searchFocused: Bool
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
                .focused($searchFocused)
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
                Button {
                    searchFocused = true
                } label: {
                    Image(systemName: "mic")
                        .font(.system(size: 16))
                        .foregroundStyle(iconTint)
                        .frame(width: 26, height: 28)
                }
                .buttonStyle(.plain)
                .accessibilityLabel("Open keyboard for voice dictation")
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


/// Responsive night-sky ornament, drawn as real editable SwiftUI vectors.
/// Matches the normalized star positions in Android's NocturneAtmosphere.
struct NocturneAtmosphere: View {
    private let sparks: [(CGFloat, CGFloat, CGFloat)] = [
        (0.54, 0.10, 0.90), (0.60, 0.22, 0.68),
        (0.67, 0.08, 0.58), (0.76, 0.18, 1.00),
        (0.86, 0.09, 0.70), (0.94, 0.26, 0.66),
        (0.57, 0.37, 0.52), (0.71, 0.33, 0.79),
        (0.83, 0.39, 0.55), (0.96, 0.47, 0.85),
        (0.64, 0.53, 0.74), (0.76, 0.60, 0.56),
        (0.89, 0.66, 0.88), (0.53, 0.72, 0.48),
        (0.69, 0.82, 0.75), (0.82, 0.89, 0.52)
    ]

    var body: some View {
        Canvas { context, size in
            let lantern = Color(hex: "F0D89A")
            let moonlitBlue = Color(hex: "BBD0D0")
            for (index, spark) in sparks.enumerated() {
                let center = CGPoint(x: size.width * spark.0, y: size.height * spark.1)
                let strength = Double(spark.2)
                let radius: CGFloat = index.isMultiple(of: 4) ? 1.5 : 0.85
                let glow = Path(ellipseIn: CGRect(
                    x: center.x - radius * 7, y: center.y - radius * 7,
                    width: radius * 14, height: radius * 14
                ))
                context.fill(glow, with: .color(lantern.opacity(0.045 * strength)))
                let point = Path(ellipseIn: CGRect(
                    x: center.x - radius, y: center.y - radius,
                    width: radius * 2, height: radius * 2
                ))
                let ink = index.isMultiple(of: 3) ? lantern : moonlitBlue
                context.fill(point, with: .color(ink.opacity(0.54 * strength)))
                if index.isMultiple(of: 4) {
                    let arm = 5 * spark.2
                    var glimmer = Path()
                    glimmer.move(to: CGPoint(x: center.x - arm, y: center.y))
                    glimmer.addLine(to: CGPoint(x: center.x + arm, y: center.y))
                    glimmer.move(to: CGPoint(x: center.x, y: center.y - arm))
                    glimmer.addLine(to: CGPoint(x: center.x, y: center.y + arm))
                    context.stroke(glimmer, with: .color(lantern.opacity(0.36)),
                                   style: StrokeStyle(lineWidth: 0.6, lineCap: .round))
                }
            }

            // Star positions use the same normalized mathematics as Compose.
            for index in 0..<64 {
                let x = 0.48 + CGFloat((index * 37) % 97) / 96 * 0.50
                let y = 0.04 + CGFloat((index * 41) % 89) / 88 * 0.78
                let radius = 0.32 + CGFloat(index % 4) * 0.11
                let dot = Path(ellipseIn: CGRect(
                    x: size.width * x - radius, y: size.height * y - radius,
                    width: radius * 2, height: radius * 2
                ))
                let tone = index.isMultiple(of: 5) ? lantern : moonlitBlue
                context.fill(dot, with: .color(tone.opacity(0.14 + Double(index % 3) * 0.055)))
            }
        }
        .accessibilityHidden(true)
        .allowsHitTesting(false)
    }
}


/// Cross-platform vector material: paper/ink wash, never a repeating bitmap.
struct CountrysideMaterialOverlay: View {
    let isDark: Bool

    var body: some View {
        Canvas { context, size in
            let pigment = isDark ? Color(hex: "F0D89A") : Color(hex: "8A7152")
            let wash = isDark ? Color(hex: "334A5E") : Color(hex: "C98267")
            let side = min(size.width, size.height)

            let radiusA = side * 0.27
            let centerA = CGPoint(x: size.width * 0.04, y: size.height * 0.16)
            context.fill(Path(ellipseIn: CGRect(
                x: centerA.x - radiusA, y: centerA.y - radiusA,
                width: radiusA * 2, height: radiusA * 2)),
                with: .color(wash.opacity(isDark ? 0.025 : 0.022))
            )
            let radiusB = side * 0.34
            let centerB = CGPoint(x: size.width * 0.96, y: size.height * 0.77)
            context.fill(Path(ellipseIn: CGRect(
                x: centerB.x - radiusB, y: centerB.y - radiusB,
                width: radiusB * 2, height: radiusB * 2)),
                with: .color(wash.opacity(isDark ? 0.025 : 0.018))
            )
            for i in 0..<190 {
                let x = CGFloat((i * 73 + 19) % 257) / 257
                let y = CGFloat((i * 101 + 37) % 263) / 263
                let radius = 0.4 + CGFloat(i % 4) * 0.19
                context.fill(Path(ellipseIn: CGRect(
                    x: size.width * x - radius, y: size.height * y - radius,
                    width: radius * 2, height: radius * 2)),
                    with: .color(pigment.opacity(isDark ? 0.048 : 0.037))
                )
            }
        }
        .allowsHitTesting(false)
        .accessibilityHidden(true)
    }
}

/// Native-vector lantern halos: consistent with the Android night scene.
struct CottageWindowGlow: View {
    var body: some View {
        Canvas { context, size in
            let points: [(CGFloat, CGFloat)] = [
                (0.80, 0.69), (0.855, 0.67), (0.88, 0.74)
            ]
            for (x, y) in points {
                let c = CGPoint(x: size.width * x, y: size.height * y)
                let rings: [(CGFloat, Color, Double)] = [
                    (15, Color(hex: "F0D89A"), 0.085),
                    (5, Color(hex: "D4A45A"), 0.23),
                    (1.25, Color(hex: "F0D89A"), 0.72)
                ]
                for (radius, pigment, alpha) in rings {
                    context.fill(
                        Path(ellipseIn: CGRect(
                            x: c.x - radius, y: c.y - radius,
                            width: radius * 2, height: radius * 2)),
                        with: .color(pigment.opacity(alpha))
                    )
                }
            }
        }
        .allowsHitTesting(false)
        .accessibilityHidden(true)
    }
}
