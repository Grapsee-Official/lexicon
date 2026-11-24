//
//  Hero.swift
//  Lexicon
//

import SwiftUI

struct Hero: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @State private var measuredWidth: CGFloat = 390

    private var adaptiveHeight: CGFloat {
        let compact = measuredWidth < 360
        let expanded = measuredWidth >= 600
        if expanded && dynamicTypeSize.isAccessibilitySize { return 425 }
        if expanded { return 365 }
        if dynamicTypeSize.isAccessibilitySize { return 330 }
        return compact ? 266 : 245
    }

    private var isDark: Bool { scheme == .dark }

    private var headlineMain: Color {
        isDark ? LexiconColors.foregroundDark : LexiconColors.foreground
    }

    private var headlineWords: Color {
        isDark ? LexiconColors.sageDark : LexiconColors.primary
    }

    private var sprigColor: Color {
        isDark ? LexiconColors.accentDark : LexiconColors.highlight
    }

    private var subtextColor: Color {
        isDark ? Color(hex: "D3C6B2") : LexiconColors.foreground.opacity(0.90)
    }

    var body: some View {
        GeometryReader { geo in
            let totalWidth = geo.size.width
            let compact = totalWidth < 360
            let enlargedText = dynamicTypeSize.isAccessibilitySize
            let expanded = totalWidth >= 600
            let heroHeight: CGFloat = expanded ? (enlargedText ? 425 : 365)
                : (enlargedText ? 330 : (compact ? 266 : 245))
            let textWidth = totalWidth * ((compact || enlargedText) ? 0.60 : 0.52)

            ZStack(alignment: .topLeading) {
                if isDark {
                    let readingEdge: CGFloat = (compact || enlargedText) ? 0.58 : 0.50
                    ZStack(alignment: .topTrailing) {
                        // The illustration supplies its own moonlight, lanterns
                        // and stars; do not scatter another layer over the scene.
                        RadialGradient(
                            colors: [LexiconColors.secondaryDark.opacity(0.30), .clear],
                            center: UnitPoint(x: 0.72, y: 0.42),
                            startRadius: 0,
                            endRadius: max(totalWidth, heroHeight) * 0.54
                        )

                        Image("hero-dark")
                            .resizable()
                            .scaledToFit()
                            .frame(width: totalWidth, height: heroHeight - 26, alignment: .topTrailing)

                        LinearGradient(
                            stops: [
                                .init(color: LexiconColors.backgroundDark, location: 0),
                                .init(color: LexiconColors.backgroundDark.opacity(0.97), location: 0.28),
                                .init(color: LexiconColors.backgroundDark.opacity(0.90), location: readingEdge - 0.10),
                                .init(color: LexiconColors.backgroundDark.opacity(0.65), location: readingEdge),
                                .init(color: .clear, location: readingEdge + 0.16),
                                .init(color: .clear, location: 1)
                            ],
                            startPoint: .leading,
                            endPoint: .trailing
                        )

                        // Dissolve naturally into the header and the next row.
                        LinearGradient(
                            stops: [
                                .init(color: LexiconColors.backgroundDark, location: 0),
                                .init(color: .clear, location: 0.10),
                                .init(color: .clear, location: 0.80),
                                .init(color: LexiconColors.backgroundDark.opacity(0.70), location: 0.94),
                                .init(color: LexiconColors.backgroundDark, location: 1)
                            ],
                            startPoint: .top,
                            endPoint: .bottom
                        )
                    }
                    .frame(width: totalWidth, height: heroHeight)
                    .allowsHitTesting(false)
                    .accessibilityHidden(true)
                } else {
                    HStack(spacing: 0) {
                        Spacer(minLength: 0)
                        Image("hero-light")
                            .resizable()
                            .scaledToFit()
                            .frame(width: totalWidth * (compact ? 0.82 : 0.88), height: heroHeight, alignment: .bottomTrailing)
                            .lexiconDecorative()
                    }
                    .frame(width: totalWidth, height: heroHeight)

                    LinearGradient(
                        stops: [
                            .init(color: LexiconColors.background.opacity(0.94), location: 0),
                            .init(color: LexiconColors.background.opacity(0.72), location: 0.42),
                            .init(color: .clear, location: 1)
                        ],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                    .frame(width: totalWidth * 0.60, height: heroHeight)
                    .allowsHitTesting(false)
                    .accessibilityHidden(true)
                }

                VStack(alignment: .leading, spacing: 6) {
                    headline
                    Text("A timeless dictionary crafted for curious minds. Explore meanings, origins, and stories behind every word.")
                        .font(.lexiconBody(11.5))
                        .frame(maxWidth: textWidth * 0.88, alignment: .leading)
                        .foregroundStyle(subtextColor)
                        .lineSpacing(2)
                        .fixedSize(horizontal: false, vertical: true)
                }
                .frame(width: textWidth, alignment: .leading)
                .padding(.leading, 18)
                .padding(.top, 2)

                VStack {
                    Spacer()
                    HStack(spacing: 0) {
                        FloatingSearchBar()
                            .frame(width: totalWidth * (compact ? 0.90 : 0.75))
                        Spacer(minLength: 0)
                    }
                    .padding(.leading, 16)
                    .padding(.bottom, 2)
                }
                .frame(width: totalWidth, height: heroHeight)
            }
        }
        .frame(height: adaptiveHeight)
        .background {
            GeometryReader { measurement in
                Color.clear
                    .onAppear { measuredWidth = measurement.size.width }
                    .onChange(of: measurement.size.width) { measuredWidth = $0 }
            }
        }
    }

    @ViewBuilder
    private var headline: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("Discover the")
                .foregroundStyle(headlineMain)
            Text("World of")
                .foregroundStyle(headlineMain)
            HStack(spacing: 8) {
                Text("Words")
                    .foregroundStyle(headlineWords)
                BotanicalSprig(color: sprigColor)
                    .frame(width: 23, height: 23)
                    .lexiconDecorative()
            }
        }
        .font(.lexiconDisplay(measuredWidth >= 600 ? 36 : 27, weight: .regular))
        .tracking(-0.3)
        .frame(maxWidth: .infinity, alignment: .leading)
        .lexiconCombinedElement()
        .accessibilityLabel(Text("Discover the World of Words"))
        .lexiconAccessibilityHeading(.h1)
    }
}

struct BotanicalSprig: View {
    var color: Color = LexiconColors.highlight

    var body: some View {
        Canvas { context, size in
            let s = min(size.width, size.height) / 32.0

            var stem = Path()
            stem.move(to: CGPoint(x: 5 * s, y: 27 * s))
            stem.addQuadCurve(
                to: CGPoint(x: 24 * s, y: 7 * s),
                control: CGPoint(x: 14 * s, y: 17 * s)
            )
            context.stroke(
                stem,
                with: .color(color),
                style: StrokeStyle(lineWidth: 1.35 * s, lineCap: .round)
            )

            drawLeaf(in: &context, cx: 11.5 * s, cy: 16.5 * s, len: 8.5 * s, wid: 3.6 * s, degrees: -72)
            drawLeaf(in: &context, cx: 17.5 * s, cy: 10.5 * s, len: 8.0 * s, wid: 3.4 * s, degrees: -66)
            drawLeaf(in: &context, cx: 15.5 * s, cy: 20.5 * s, len: 8.5 * s, wid: 3.6 * s, degrees: 12)
            drawLeaf(in: &context, cx: 21.0 * s, cy: 14.8 * s, len: 7.8 * s, wid: 3.3 * s, degrees: 8)
            drawLeaf(in: &context, cx: 25.0 * s, cy: 6.2 * s, len: 7.2 * s, wid: 3.1 * s, degrees: -38)
        }
    }

    private func drawLeaf(
        in context: inout GraphicsContext,
        cx: CGFloat,
        cy: CGFloat,
        len: CGFloat,
        wid: CGFloat,
        degrees: Double
    ) {
        var copy = context
        copy.translateBy(x: cx, y: cy)
        copy.rotate(by: .degrees(degrees))
        var p = Path()
        p.move(to: CGPoint(x: -len * 0.5, y: 0))
        p.addQuadCurve(to: CGPoint(x: len * 0.5, y: 0), control: CGPoint(x: 0, y: -wid))
        p.addQuadCurve(to: CGPoint(x: -len * 0.5, y: 0), control: CGPoint(x: 0, y: wid))
        p.closeSubpath()
        copy.fill(p, with: .color(color.opacity(0.92)))
    }
}
