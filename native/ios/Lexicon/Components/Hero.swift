//
//  Hero.swift
//  Lexicon
//

import SwiftUI

struct Hero: View {
    @Environment(\.colorScheme) private var scheme
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @State private var measuredWidth: CGFloat = 390
    private let sharedNightScene: Bool

    init(sharedNightScene: Bool = false) {
        self.sharedNightScene = sharedNightScene
    }

    private var adaptiveHeight: CGFloat {
        let compact = measuredWidth < 360
        let expanded = measuredWidth >= 600
        if expanded && dynamicTypeSize.isAccessibilitySize { return 425 }
        if expanded { return 365 }
        if dynamicTypeSize.isAccessibilitySize { return 330 }
        return compact ? (isDark ? 250 : 266) : (isDark ? 242 : 245)
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
        Group {
            if isDark {
                darkHero
            } else {
                lightHero
            }
        }
        .background {
            GeometryReader { measurement in
                Color.clear
                    .onAppear { measuredWidth = measurement.size.width }
                    .onChange(of: measurement.size.width) { measuredWidth = $0 }
            }
        }
    }

    private var darkHero: some View {
        let enlargedText = dynamicTypeSize.isAccessibilitySize
        // Measure both children at their final width, then place Search at
        // the bottom of the minimum hero or below the copy, whichever is lower.
        return NightHeroLayout(enlargedText: enlargedText) {
            VStack(alignment: .leading, spacing: 6) {
                headline
                Text("A timeless dictionary crafted for curious minds. Explore meanings, origins, and stories behind every word.")
                    .font(.lexiconBody(enlargedText ? 11.5 : 10.5))
                    .foregroundStyle(subtextColor)
                    .lineSpacing(2)
                    .fixedSize(horizontal: false, vertical: true)
                    .padding(.trailing, enlargedText ? 0 : 12)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            FloatingSearchBar(sceneIntegrated: true)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .background {
            if !sharedNightScene {
                HomeNightScene()
            }
        }
    }

    private var lightHero: some View {
        GeometryReader { geo in
            let totalWidth = geo.size.width
            let compact = totalWidth < 360
            let enlargedText = dynamicTypeSize.isAccessibilitySize
            let heroHeight = geo.size.height
            let textWidth = totalWidth * ((compact || enlargedText) ? 0.60 : 0.52)

            ZStack(alignment: .topLeading) {
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
        .font(.lexiconDisplay(measuredWidth >= 600 ? 36 : (isDark && !dynamicTypeSize.isAccessibilitySize ? (measuredWidth < 360 ? 24 : 25) : 27), weight: .regular))
        .tracking(-0.3)
        .frame(maxWidth: .infinity, alignment: .leading)
        .lexiconCombinedElement()
        .accessibilityLabel(Text("Discover the World of Words"))
        .lexiconAccessibilityHeading(.h1)
    }
}

// ScrollView proposes an unbounded height. A Spacer alone cannot guarantee
// bottom placement in that situation. This layout measures both controls
// before calculating the hero height, without changing the light-mode path.
// Shared painting layer for the whole Home masthead. The original image
// and its alpha are preserved; only the presentation is color graded.
struct HomeNightScene: View {
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    var extendsBehindTags: Bool = false

    var body: some View {
        GeometryReader { geo in
            let width = geo.size.width
            let height = geo.size.height
            let largeText = dynamicTypeSize.isAccessibilitySize
            let readingEdge: CGFloat = largeText ? 0.66 : (width < 360 ? 0.49 : 0.45)
            let artWidth = min(width * (largeText ? 0.96 : 1.08), height * 1.5 * 1.08)
            let artHeight = artWidth / 1.5
            let artX = max(width * 0.63 - artWidth * 0.54, width - artWidth * 0.95)
            let moonY = height * (extendsBehindTags ? 0.075 : 0.09)
            let artY = largeText ? height * 0.90 - artHeight : moonY - artHeight * 0.17

            Image("hero-dark")
                .resizable()
                .scaledToFit()
                .frame(width: artWidth, height: artHeight)
                .saturation(0.78)
                .colorMultiply(Color(red: 0.82, green: 0.86, blue: 0.96))
                .position(x: artX + artWidth / 2, y: artY + artHeight / 2)
                .frame(width: width, height: height)
                .clipped()
                .mask {
                    LinearGradient(stops: [
                        .init(color: .clear, location: 0),
                        .init(color: .clear, location: readingEdge - 0.24),
                        .init(color: .black.opacity(0.18), location: readingEdge - 0.11),
                        .init(color: .black.opacity(0.60), location: readingEdge),
                        .init(color: .black.opacity(0.96), location: readingEdge + 0.13),
                        .init(color: .black, location: 0.84),
                        .init(color: .black.opacity(0.83), location: 0.96),
                        .init(color: .black.opacity(0.73), location: 1)
                    ], startPoint: .leading, endPoint: .trailing)
                }
                .mask {
                    LinearGradient(stops: [
                        .init(color: .clear, location: 0),
                        .init(color: .black.opacity(0.54), location: 0.035),
                        .init(color: .black.opacity(0.86), location: 0.085),
                        .init(color: .black.opacity(0.81), location: 0.35),
                        .init(color: .black, location: 0.58),
                        .init(color: .black.opacity(0.96), location: 0.78),
                        .init(color: .black.opacity(0.57), location: 0.91),
                        .init(color: .clear, location: 1)
                    ], startPoint: .top, endPoint: .bottom)
                }
        }
        .allowsHitTesting(false)
        .accessibilityHidden(true)
    }
}

private struct NightHeroLayout: Layout {
    let enlargedText: Bool

    private let leading: CGFloat = 20
    private let bottom: CGFloat = 4
    private let minimumGap: CGFloat = 10
    private var top: CGFloat { enlargedText ? 8 : 15 }

    private func minimumHeight(for width: CGFloat) -> CGFloat {
        if width >= 600 { return enlargedText ? 425 : 365 }
        if enlargedText { return 330 }
        return width < 360 ? 250 : 242
    }

    private func copyWidth(for width: CGFloat) -> CGFloat {
        max(1, width * (enlargedText ? 0.70 : 0.51) - leading)
    }

    private func searchWidth(for width: CGFloat) -> CGFloat {
        max(1, (width - leading) * (enlargedText ? 0.85 : 0.73))
    }

    func sizeThatFits(
        proposal: ProposedViewSize, subviews: Subviews, cache: inout ()
    ) -> CGSize {
        let width = proposal.width ?? 390
        guard subviews.count == 2 else { return CGSize(width: width, height: 0) }
        let copySize = subviews[0].sizeThatFits(
            ProposedViewSize(width: copyWidth(for: width), height: nil)
        )
        let searchSize = subviews[1].sizeThatFits(
            ProposedViewSize(width: searchWidth(for: width), height: nil)
        )
        return CGSize(
            width: width,
            height: max(minimumHeight(for: width),
                        top + copySize.height + minimumGap + searchSize.height + bottom)
        )
    }

    func placeSubviews(
        in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()
    ) {
        guard subviews.count == 2 else { return }
        let copyProposal = ProposedViewSize(width: copyWidth(for: bounds.width), height: nil)
        let searchProposal = ProposedViewSize(width: searchWidth(for: bounds.width), height: nil)
        let copySize = subviews[0].sizeThatFits(copyProposal)
        let searchSize = subviews[1].sizeThatFits(searchProposal)
        let searchY = max(bounds.minY + top + copySize.height + minimumGap,
                          bounds.maxY - bottom - searchSize.height)

        subviews[0].place(
            at: CGPoint(x: bounds.minX + leading, y: bounds.minY + top),
            anchor: .topLeading, proposal: copyProposal
        )
        subviews[1].place(
            at: CGPoint(x: bounds.minX + leading, y: searchY),
            anchor: .topLeading, proposal: searchProposal
        )
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
