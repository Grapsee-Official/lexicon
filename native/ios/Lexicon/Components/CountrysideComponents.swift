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
    var actionKind: String = "history"
    var actionLabel: String = "Search history"
    var onLogoToggleTheme: () -> Void = {}
    var heroHeight: CGFloat = 146

    @Environment(\.colorScheme) private var scheme
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @State private var measuredWidth: CGFloat = 390

    private var isDark: Bool { scheme == .dark }
    private var responsiveHeight: CGFloat {
        // Give the full 508x345 painted vignette its natural space.
        // A narrow iPad window uses its real container width, not UIScreen.
        let completePainting = min(540, max(218, measuredWidth * 0.68))
        let textAllowance = dynamicTypeSize.isAccessibilitySize
            ? heroHeight + 106 : heroHeight + 18
        return max(completePainting, textAllowance)
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
                        // The watercolor receives the complete page width;
                        // alpha edges belong to the painter, not a picture box.
                        .frame(width: w, height: responsiveHeight, alignment: .bottomTrailing)
                        // The transparent lower watercolor may overlap the
                        // next search capsule; it is not chopped at the header.
                        .offset(y: 24)
                        .accessibilityHidden(true)
                }
                .frame(width: w, height: responsiveHeight)

                // Blend only the reading margin. The right-hand cottages
                // and flowers keep their original watercolor colors.
                LinearGradient(
                    stops: [
                        .init(color: isDark ? LexiconColors.backgroundDark.opacity(0.72)
                            : LexiconColors.background.opacity(0.72), location: 0),
                        .init(color: isDark ? LexiconColors.backgroundDark.opacity(0.24)
                            : LexiconColors.background.opacity(0.24), location: 0.55),
                        .init(color: .clear, location: 1.0),
                    ],
                    startPoint: .leading, endPoint: .trailing
                )
                .frame(width: w * 0.68, height: responsiveHeight)
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
                                .frame(width: 44, height: 44)
                                .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel("Toggle appearance")

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
                        Image(systemName: actionKind == "back" ? "chevron.left" :
                            actionKind == "notes" ? "note.text" :
                            actionKind == "templates" ? "square.stack" : "clock.arrow.circlepath")
                            .font(.system(size: 15, weight: .regular))
                            .foregroundStyle(isDark ? LexiconColors.accentDark : LexiconColors.foreground.opacity(0.78))
                            .frame(width: 44, height: 44)
                            .contentShape(Rectangle())
                            
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel(actionLabel)
                }
                .padding(.top, 8)
                .padding(.trailing, 16)
            }
        }
        .frame(height: responsiveHeight)
        .background {
            GeometryReader { measurement in
                Color.clear
                    .onAppear { measuredWidth = measurement.size.width }
                    .onChange(of: measurement.size.width) { measuredWidth = $0 }
            }
        }
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
                        .frame(width: 44, height: 44)
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel("Clear search")
            }

            if showCameraMic {
                // Decorative until OCR and native speech permissions are wired.
                Image(systemName: "camera")
                    .font(.system(size: 14.5))
                    .foregroundStyle(iconTint)
                    .accessibilityHidden(true)
                Image(systemName: "mic")
                    .font(.system(size: 14.5))
                    .foregroundStyle(iconTint)
                    .accessibilityHidden(true)
            } else if showMicOnly {
                Button {
                    searchFocused = true
                } label: {
                    Image(systemName: "mic")
                        .font(.system(size: 16))
                        .foregroundStyle(iconTint)
                        .frame(width: 44, height: 44)
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel("Focus search; keyboard dictation may be available")
            }

            if showFilter {
                Rectangle()
                    .fill(lexiconPalette(scheme).border)
                    .frame(width: 1, height: 15)
                Button(action: onFilterClick) {
                    Image(systemName: "slider.horizontal.3")
                        .font(.system(size: 14.5))
                        .foregroundStyle(iconTint)
                        .frame(width: 44, height: 44)
                        .contentShape(Rectangle())
                }
                .buttonStyle(.plain)
                .accessibilityLabel("Change search filters")
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

/// A considered literary epigraph in the Library, not a repeated footer.
/// The source painting is never cropped, and the words are never ellipsized.
struct CountrysideQuoteBanner: View {
    let quote: String
    var attribution: String = "Lexicon"
    var illustrationBase: String = "ill-banner-bridge"

    @Environment(\.colorScheme) private var scheme
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @State private var measuredWidth: CGFloat = 390

    private var isDark: Bool { scheme == .dark }
    private var naturalArtHeight: CGFloat {
        let asset = countrysideImageName(illustrationBase, isDark: isDark)
        let size = UIImage(named: asset)?.size ?? CGSize(width: 3, height: 1)
        // The painted vignette occupies the RIGHT half of the banner.
        return measuredWidth * 0.52 / max(0.01, size.width / max(1, size.height))
    }
    private var expandedInk: Bool {
        dynamicTypeSize.isAccessibilitySize || measuredWidth < 350 || quote.count > 70
    }
    private var epigraph: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text("“\(quote)”")
                .font(.lexiconDisplay(16).italic())
                .foregroundStyle(lexiconPalette(scheme).foreground)
                .fixedSize(horizontal: false, vertical: true)
            Text("— \(attribution) —")
                .font(.lexiconBody(11))
                .tracking(0.3)
                .foregroundStyle(lexiconPalette(scheme).highlight)
        }
        .padding(.leading, 18)
        .padding(.trailing, 14)
        .padding(.vertical, 6)
    }

    var body: some View {
        let p = lexiconPalette(scheme)
        // The bordered watercolor's vertical dimension is dictated only by
        // the source artwork. Long and accessible copy lives on parchment
        // before the bookplate rather than stretching its image container.
        VStack(alignment: .leading, spacing: expandedInk ? 5 : 0) {
            if expandedInk {
                epigraph
            }
            ZStack(alignment: .leading) {
                p.card
                Image(countrysideImageName(illustrationBase, isDark: isDark))
                    .resizable()
                    .scaledToFit()
                    .frame(width: measuredWidth * 0.52,
                           height: naturalArtHeight, alignment: .bottomTrailing)
                    .frame(maxWidth: .infinity, alignment: .trailing)
                    .accessibilityHidden(true)

                if !expandedInk {
                    epigraph
                        .frame(width: measuredWidth * 0.57, alignment: .leading)
                }
            }
            .frame(height: naturalArtHeight)
            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 18, style: .continuous)
                    .stroke(p.border, lineWidth: 1)
            )
        }
        .background {
            GeometryReader { measured in
                Color.clear
                    .onAppear { measuredWidth = measured.size.width }
                    .onChange(of: measured.size.width) { measuredWidth = $0 }
            }
        }
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
            // Fine, non-repeating print/paper granulation. All marks remain
            // editable native geometry and share coordinates with the SVGs.
            for i in 0..<620 {
                let x = CGFloat((i * 73 + 19) % 997) / 997
                let y = CGFloat((i * 101 + 37) % 991) / 991
                let radius = 0.36 + CGFloat(i % 5) * 0.13
                context.fill(Path(ellipseIn: CGRect(
                    x: size.width * x - radius, y: size.height * y - radius,
                    width: radius * 2, height: radius * 2)),
                    with: .color(pigment.opacity(isDark ? 0.045 : 0.062))
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

/// Underpainting for Search's original Cotswolds countryside vignette.
/// Normalised editable native paths keep the hills, sky, and wildflowers
/// responsive; the approved hand-painted cottage sits above these washes.
struct SearchDreamscapeWash: View {
    let isDark: Bool

    var body: some View {
        Canvas { context, size in
            let w = size.width
            let h = size.height
            let sky = Color(hex: isDark ? "334A5E" : "7C9297")
            let meadow = Color(hex: "657D5A")
            let light = Color(hex: isDark ? "F0D89A" : "F3EBDD")

            func pigment(_ color: Color, _ opacity: Double,
                         x: CGFloat, y: CGFloat, radius: CGFloat) {
                let wash = Path(ellipseIn: CGRect(
                    x: x - radius, y: y - radius,
                    width: radius * 2, height: radius * 2
                ))
                context.fill(wash, with: .color(color.opacity(opacity)))
            }

            pigment(sky, isDark ? 0.18 : 0.10,
                    x: w * 0.69, y: h * 0.13, radius: w * 0.36)
            pigment(sky, isDark ? 0.12 : 0.065,
                    x: w * 0.40, y: h * 0.22, radius: w * 0.27)
            pigment(light, isDark ? 0.055 : 0.32,
                    x: w * 0.76, y: h * 0.15, radius: w * 0.18)
            pigment(sky, 0.07,
                    x: w * 0.92, y: h * 0.36, radius: w * 0.22)

            var farHills = Path()
            farHills.move(to: CGPoint(x: -w * 0.08, y: h * 0.69))
            farHills.addCurve(to: CGPoint(x: w * 0.51, y: h * 0.70),
                              control1: CGPoint(x: w * 0.17, y: h * 0.58),
                              control2: CGPoint(x: w * 0.34, y: h * 0.62))
            farHills.addCurve(to: CGPoint(x: w * 1.08, y: h * 0.63),
                              control1: CGPoint(x: w * 0.72, y: h * 0.54),
                              control2: CGPoint(x: w * 0.91, y: h * 0.61))
            farHills.addLine(to: CGPoint(x: w * 1.08, y: h))
            farHills.addLine(to: CGPoint(x: -w * 0.08, y: h))
            farHills.closeSubpath()
            context.fill(farHills, with: .color(meadow.opacity(isDark ? 0.14 : 0.085)))

            var nearHills = Path()
            nearHills.move(to: CGPoint(x: -w * 0.08, y: h * 0.83))
            nearHills.addCurve(to: CGPoint(x: w * 0.58, y: h * 0.87),
                               control1: CGPoint(x: w * 0.20, y: h * 0.71),
                               control2: CGPoint(x: w * 0.43, y: h * 0.84))
            nearHills.addCurve(to: CGPoint(x: w * 1.08, y: h * 0.83),
                               control1: CGPoint(x: w * 0.78, y: h * 0.73),
                               control2: CGPoint(x: w * 0.94, y: h * 0.77))
            nearHills.addLine(to: CGPoint(x: w * 1.08, y: h))
            nearHills.addLine(to: CGPoint(x: -w * 0.08, y: h))
            nearHills.closeSubpath()
            context.fill(nearHills, with: .color(meadow.opacity(isDark ? 0.12 : 0.07)))

            let petals = Color(hex: isDark ? "F0D89A" : "FFF8E9")
            let pollen = Color(hex: isDark ? "D4A45A" : "A58A61")
            let flowers: [(CGFloat, CGFloat)] = [
                (0.09, 0.88), (0.16, 0.95), (0.28, 0.90),
                (0.76, 0.93), (0.88, 0.86), (0.95, 0.95)
            ]
            for (index, p) in flowers.enumerated() {
                let center = CGPoint(x: w * p.0, y: h * p.1)
                let radius: CGFloat = index.isMultiple(of: 2) ? 1.5 : 1.1
                for petal in 0..<5 {
                    let angle = Double(petal) * 2 * Double.pi / 5
                    let x = center.x + CGFloat(cos(angle)) * radius * 1.6
                    let y = center.y + CGFloat(sin(angle)) * radius * 1.6
                    let oval = Path(ellipseIn: CGRect(x: x - radius, y: y - radius,
                                                       width: radius * 2, height: radius * 2))
                    context.fill(oval, with: .color(petals.opacity(isDark ? 0.40 : 0.55)))
                }
                pigment(pollen, 0.48, x: center.x, y: center.y, radius: radius * 0.67)
            }
        }
        .allowsHitTesting(false)
        .accessibilityHidden(true)
    }
}
