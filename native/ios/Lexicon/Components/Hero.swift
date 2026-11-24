//
//  Hero.swift
//  Lexicon
//

import SwiftUI
import UIKit
import CoreGraphics

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
            let moonY = min(height * (extendsBehindTags ? 0.09 : 0.10), width * 0.055)
            let artY = largeText ? height * 0.90 - artHeight : moonY - artHeight * 0.17

            PreparedArtworkImage(name: "hero-dark", treatment: .moonlit)
                .scaledToFit()
                .frame(width: artWidth, height: artHeight)
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
                        .init(color: .black.opacity(0.91), location: 1)
                    ], startPoint: .leading, endPoint: .trailing)
                }
                .mask {
                    LinearGradient(stops: [
                        .init(color: .clear, location: 0),
                        .init(color: .black.opacity(0.65), location: 0.025),
                        .init(color: .black, location: 0.065),
                        .init(color: .black.opacity(0.93), location: 0.35),
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


// Same non-destructive artwork recipe as Compose. Native image decoding and
// pixel processing occur off the main actor, with a bounded decoded-image cache.
enum ArtworkTreatment: String, Sendable {
    case moonlit, story, bookplate
}

struct PreparedArtworkImage: View {
    let name: String
    let treatment: ArtworkTreatment
    @State private var prepared: UIImage?

    var body: some View {
        Group {
            if let prepared {
                Image(uiImage: prepared).resizable()
            } else {
                Image(name).resizable()
            }
        }
        .task(id: name + treatment.rawValue) {
            let result = await Task.detached(priority: .userInitiated) {
                HomeArtworkProcessor.prepare(name: name, treatment: treatment)
            }.value
            if !Task.isCancelled { prepared = result }
        }
    }
}

private enum HomeArtworkProcessor {
    private static let cache: NSCache<NSString, UIImage> = {
        let result = NSCache<NSString, UIImage>()
        result.totalCostLimit = 24 * 1024 * 1024
        return result
    }()

    private static func smooth(_ lo: Float, _ hi: Float, _ x: Float) -> Float {
        let t = min(1, max(0, (x - lo) / (hi - lo)))
        return t * t * (3 - 2 * t)
    }

    private static func byte(_ x: Float) -> UInt8 {
        UInt8(min(255, max(0, (x * 255).rounded())))
    }

    static func prepare(name: String, treatment: ArtworkTreatment) -> UIImage? {
        let key = (name + ":" + treatment.rawValue) as NSString
        if let cached = cache.object(forKey: key) { return cached }
        guard let original = UIImage(named: name)?.cgImage,
              let space = CGColorSpace(name: CGColorSpace.sRGB) else { return nil }

        let limit = treatment == .bookplate ? 1280 : 1536
        let scale = min(1, Double(limit) / Double(max(original.width, original.height)))
        let w = max(1, Int((Double(original.width) * scale).rounded()))
        let h = max(1, Int((Double(original.height) * scale).rounded()))
        let info = CGBitmapInfo.byteOrder32Big.rawValue | CGImageAlphaInfo.premultipliedLast.rawValue
        var bytes = [UInt8](repeating: 0, count: w * h * 4)
        let drawn = bytes.withUnsafeMutableBytes { buffer -> Bool in
            guard let context = CGContext(data: buffer.baseAddress, width: w, height: h,
                                          bitsPerComponent: 8, bytesPerRow: w * 4,
                                          space: space, bitmapInfo: info) else { return false }
            context.interpolationQuality = .high
            context.draw(original, in: CGRect(x: 0, y: 0, width: w, height: h))
            return true
        }
        guard drawn else { return nil }

        var left = w, top = h, right = -1, bottom = -1, transparent = 0
        if treatment == .bookplate {
            for y in 0..<h {
                for x in 0..<w {
                    let alpha = bytes[(y * w + x) * 4 + 3]
                    if alpha < 12 { transparent += 1 }
                    if alpha >= 16 {
                        left = min(left, x); top = min(top, y)
                        right = max(right, x); bottom = max(bottom, y)
                    }
                }
            }
        } else {
            let moon = treatment == .moonlit
            for i in stride(from: 0, to: bytes.count, by: 4) {
                let a = Float(bytes[i + 3]) / 255
                if a == 0 { continue }
                // CGContext stores premultiplied RGBA. Grade straight RGB and
                // premultiply again using the new alpha to avoid colored halos.
                let r = min(1, Float(bytes[i]) / 255 / a)
                let g = min(1, Float(bytes[i + 1]) / 255 / a)
                let b = min(1, Float(bytes[i + 2]) / 255 / a)
                let l = r * 0.2126 + g * 0.7152 + b * 0.0722
                let highlight = smooth(0.63, 0.95, l)
                let warm = smooth(0.06, 0.24, r - b)
                let gain: Float = (moon ? 0.76 : 0.85) + (moon ? 0.22 : 0.13) * highlight
                    - (moon ? 0.045 : 0.02) * warm * (1 - highlight)
                let sat = 0.80 + 0.18 * highlight
                let shadow = 1 - smooth(0.09, 0.35, l)
                let alpha = a * (0.58 + 0.42 * smooth(0.035, 0.24, l))
                let rr = (l + (r - l) * sat) * gain * (1 - 0.10 * shadow)
                let gg = (l + (g - l) * sat) * gain
                let bb = (l + (b - l) * sat) * gain + 0.012 * shadow
                bytes[i] = byte(rr * alpha)
                bytes[i + 1] = byte(gg * alpha)
                bytes[i + 2] = byte(bb * alpha)
                bytes[i + 3] = byte(alpha)
            }
        }

        guard let provider = CGDataProvider(data: Data(bytes) as CFData),
              var result = CGImage(width: w, height: h, bitsPerComponent: 8,
                                   bitsPerPixel: 32, bytesPerRow: w * 4,
                                   space: space, bitmapInfo: CGBitmapInfo(rawValue: info),
                                   provider: provider, decode: nil, shouldInterpolate: true,
                                   intent: .defaultIntent) else { return nil }

        if treatment == .bookplate && right >= left && bottom >= top && transparent > w * h / 8 {
            let pad = Int((Double(max(right - left + 1, bottom - top + 1)) * 0.055).rounded())
            left = max(0, left - pad); top = max(0, top - pad)
            right = min(w - 1, right + pad); bottom = min(h - 1, bottom + pad)
            result = result.cropping(to: CGRect(x: left, y: top,
                                               width: right - left + 1,
                                               height: bottom - top + 1)) ?? result
        }
        if treatment == .bookplate && max(result.width, result.height) > 640 {
            let factor = 640.0 / Double(max(result.width, result.height))
            let rw = max(1, Int((Double(result.width) * factor).rounded()))
            let rh = max(1, Int((Double(result.height) * factor).rounded()))
            if let ctx = CGContext(data: nil, width: rw, height: rh, bitsPerComponent: 8,
                                   bytesPerRow: rw * 4, space: space, bitmapInfo: info) {
                ctx.interpolationQuality = .high
                ctx.draw(result, in: CGRect(x: 0, y: 0, width: rw, height: rh))
                result = ctx.makeImage() ?? result
            }
        }
        let image = UIImage(cgImage: result)
        cache.setObject(image, forKey: key, cost: result.width * result.height * 4)
        return image
    }
}
