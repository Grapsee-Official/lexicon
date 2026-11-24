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
            } else if dynamicTypeSize.isAccessibilitySize {
                accessibleLightHero
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
                    .font(.lexiconBody(enlargedText ? 11.5 : 10))
                    .foregroundStyle(subtextColor)
                    .lineSpacing(1.5)
                    .fixedSize(horizontal: false, vertical: true)
                    .padding(.trailing, enlargedText ? 0 : 24)
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

    private var accessibleLightHero: some View {
        NightHeroLayout(enlargedText: true) {
            VStack(alignment: .leading, spacing: 6) {
                headline
                Text("A timeless dictionary crafted for curious minds. Explore meanings, origins, and stories behind every word.")
                    .font(.lexiconBody(11.5))
                    .foregroundStyle(subtextColor)
                    .fixedSize(horizontal: false, vertical: true)
            }
            FloatingSearchBar()
        }
        .background(alignment: .bottomTrailing) {
            Image("hero-light")
                .resizable().scaledToFit()
                .frame(height: 180)
                .accessibilityHidden(true)
                .allowsHitTesting(false)
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
        .font(.lexiconDisplay(isDark ? min(30, max(19, measuredWidth * 0.058)) : (measuredWidth >= 600 ? 36 : 27), weight: .regular))
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
    var headerInset: CGFloat = 0
    var measuredHeroHeight: CGFloat = 0

    var body: some View {
        GeometryReader { geo in
            let width = geo.size.width
            let height = geo.size.height
            let largeText = dynamicTypeSize.isAccessibilitySize
            let edge: CGFloat = largeText ? 0.32 : 0.47
            let header = min(height, max(0, headerInset))
            let contentHeight = max(1, height - header)
            let hero = measuredHeroHeight > 0 ? min(measuredHeroHeight, contentHeight)
                : contentHeight * (extendsBehindTags ? 0.86 : 1)
            let artWidth = min(width * (largeText ? 0.85 : 0.99), largeText ? 190 * 1.5 : contentHeight * 1.5 * 1.08)
            let artX = width * 0.63 - artWidth * 0.539
            let moonY = min(hero * 0.10, width * 0.055)
            let bookBaseline = hero - 4 - width * 0.040
            let naturalHeight = artWidth / 1.5
            let artHeight = largeText ? naturalHeight
                : min(naturalHeight * 1.14, max(naturalHeight * 0.88, (bookBaseline - moonY) / (0.88 - 0.17)))
            let artY = header + (largeText ? bookBaseline - artHeight * 0.97 : moonY - artHeight * 0.17)
            let fadeStart = min(0.95, max(0.55, (header + hero - width * 0.025) / max(1, height)))
            let fadeEnd = min(0.99, fadeStart + (1 - fadeStart) * 0.75)

            PreparedArtworkImage(name: "hero-dark", treatment: .moonlit)
                // Resizable image fills this deliberately adjusted scene aspect.
                .frame(width: artWidth, height: artHeight)
                .position(x: artX + artWidth / 2, y: artY + artHeight / 2)
                .frame(width: width, height: height)
                .clipped()
                .mask {
                    LinearGradient(stops: [
                        .init(color: .clear, location: 0),
                        .init(color: .clear, location: edge - 0.18),
                        .init(color: .black.opacity(0.40), location: edge - 0.08),
                        .init(color: .black, location: edge + 0.07),
                        .init(color: .black, location: 1)
                    ], startPoint: .topLeading,
                       endPoint: UnitPoint(x: 1, y: contentHeight * 0.24 / max(1, height)))
                }
                .mask {
                    LinearGradient(stops: [
                        .init(color: .black, location: 0),
                        .init(color: .black, location: fadeStart),
                        .init(color: .black.opacity(0.15), location: fadeEnd),
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
    private var top: CGFloat { enlargedText ? 8 : 16 }

    private func minimumHeight(for width: CGFloat) -> CGFloat {
        if enlargedText { return 0 }
        return min(350, max(195, width * 0.60))
    }

    private func copyWidth(for width: CGFloat) -> CGFloat {
        max(1, width * (enlargedText ? 1 : 0.51) - leading - (enlargedText ? 20 : 0))
    }

    private func searchWidth(for width: CGFloat) -> CGFloat {
        max(1, (width - leading) * (enlargedText ? 0.90 : 0.76))
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
                        top + copySize.height + minimumGap + (enlargedText ? min(210, max(135, width * 0.48)) : 0) + searchSize.height + bottom)
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
    case moonlit, dailyLandscape, story, bookplate, darkBookplate
}

struct PreparedArtworkImage: View {
    let name: String
    let treatment: ArtworkTreatment
    var underpainting: Bool = false
    @State private var prepared: UIImage?
    @State private var finished = false

    var body: some View {
        Group {
            if let image = prepared ?? HomeArtworkProcessor.cached(name: name, treatment: treatment) {
                painting(Image(uiImage: image))
            } else if finished {
                painting(Image(name))
            } else {
                Color.clear
            }
        }
        .task(id: name + treatment.rawValue) {
            let result = await Task.detached(priority: .userInitiated) { [name, treatment] in
                HomeArtworkProcessor.prepare(name: name, treatment: treatment)
            }.value
            if !Task.isCancelled {
                prepared = result
                finished = true
            }
        }
    }

    private func painting(_ image: Image) -> some View {
        image.resizable()
            .background {
                if underpainting {
                    // Reuse the decoded image instead of launching another task.
                    image.resizable().blur(radius: 12).opacity(0.13)
                }
            }
    }
}

private enum HomeArtworkProcessor {
    private static let cache: NSCache<NSString, UIImage> = {
        let result = NSCache<NSString, UIImage>()
        result.totalCostLimit = 24 * 1024 * 1024
        return result
    }()

    static func cached(name: String, treatment: ArtworkTreatment) -> UIImage? {
        cache.object(forKey: (name + ":" + treatment.rawValue) as NSString)
    }

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

        let isBookplate = treatment == .bookplate || treatment == .darkBookplate
        let limit = isBookplate ? 1024 : (treatment == .story ? 512 : (treatment == .dailyLandscape ? 1024 : 1536))
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
        if isBookplate {
            // Match the Android pipeline: only a provably uniform, opaque
            // neutral perimeter is treated as a matte, never the painting.
            removeNeutralBookplateMatte(&bytes, width: w, height: h)
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
            if treatment == .darkBookplate {
                // Navy ambient light in the shadows; gold and paper highlights
                // remain warm. Work in straight RGB, then premultiply.
                for i in stride(from: 0, to: bytes.count, by: 4) {
                    let a = Float(bytes[i + 3]) / 255
                    if a == 0 { continue }
                    let r = min(1, Float(bytes[i]) / 255 / a)
                    let g = min(1, Float(bytes[i + 1]) / 255 / a)
                    let b = min(1, Float(bytes[i + 2]) / 255 / a)
                    let l = r * 0.2126 + g * 0.7152 + b * 0.0722
                    let shade = 1 - smooth(0.12, 0.55, l)
                    let highlight = smooth(0.58, 0.90, l)
                    let gain = 0.82 + 0.15 * highlight
                    let ink = 0.42 * shade
                    let edge = (1 - smooth(0.12, 0.90, a)) * shade
                    let alpha = a * (1 - 0.30 * edge)
                    bytes[i] = byte((r * gain * (1 - ink) + 0.035 * ink) * alpha)
                    bytes[i + 1] = byte((g * gain * (1 - ink) + 0.085 * ink) * alpha)
                    bytes[i + 2] = byte((b * gain * (1 - ink) + 0.14 * ink) * alpha)
                    bytes[i + 3] = byte(alpha)
                }
            }
        } else if treatment == .moonlit {
            let sky = OriginalSkyPatch(bytes, width: w, height: h)
            refinePaintedStars(&bytes, width: w, height: h)
            gradeMoonlitPainting(&bytes, width: w, height: h)
            bytes = reframeMoonlitSubjects(bytes, width: w, height: h)
            sky.extendBehind(&bytes, width: w, height: h)
        } else {
            let dailyLandscape = treatment == .dailyLandscape
            var lightField = ArtworkLightField(width: w, height: h)
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
                let gain: Float = dailyLandscape ? 0.88 + 0.10 * highlight
                    : min(1, max(0, 0.85 + 0.13 * highlight - 0.02 * warm * (1 - highlight)))
                let sat: Float = dailyLandscape ? 0.90 + 0.08 * highlight : 0.80 + 0.18 * highlight
                let shadow = 1 - smooth(0.09, 0.35, l)
                if !dailyLandscape {
                    let emission = a * smooth(0.45, 0.83, l) * smooth(0.05, 0.22, r - b) * 0.42
                    let pixel = i / 4
                    lightField.add(x: pixel % w, y: pixel / w, value: emission)
                }
                let inkMix: Float = dailyLandscape ? 0.72 * (1 - smooth(0.03, 0.24, l)) : 0
                let alphaFloor: Float = dailyLandscape ? 0.78 : 0.58
                let alpha = a * (alphaFloor + (1 - alphaFloor) * smooth(0.035, 0.24, l))
                let rr = (l + (r - l) * sat) * gain * (1 - 0.10 * shadow) * (1 - inkMix) + (8.0 / 255.0) * inkMix
                let gg = (l + (g - l) * sat) * gain * (1 - inkMix) + (18.0 / 255.0) * inkMix
                let bb = ((l + (b - l) * sat) * gain + 0.012 * shadow) * (1 - inkMix) + (26.0 / 255.0) * inkMix
                bytes[i] = byte(rr * alpha)
                bytes[i + 1] = byte(gg * alpha)
                bytes[i + 2] = byte(bb * alpha)
                bytes[i + 3] = byte(alpha)
            }
            if !dailyLandscape { lightField.compositeBehind(&bytes) }
        }

        guard let provider = CGDataProvider(data: Data(bytes) as CFData),
              var result = CGImage(width: w, height: h, bitsPerComponent: 8,
                                   bitsPerPixel: 32, bytesPerRow: w * 4,
                                   space: space, bitmapInfo: CGBitmapInfo(rawValue: info),
                                   provider: provider, decode: nil, shouldInterpolate: true,
                                   intent: .defaultIntent) else { return nil }

        if isBookplate && right >= left && bottom >= top && transparent > w * h / 8 {
            // Keep the real silhouette aspect ratio, without a square frame.
            let pad = Int((Double(max(right - left + 1, bottom - top + 1)) * 0.025).rounded())
            left = max(0, left - pad); top = max(0, top - pad)
            right = min(w - 1, right + pad); bottom = min(h - 1, bottom + pad)
            if let cropped = result.cropping(to: CGRect(x: left, y: top,
                                                         width: right - left + 1,
                                                         height: bottom - top + 1)) {
                result = cropped
            }
        }
        if isBookplate && max(result.width, result.height) > 640 {
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

    private static func removeNeutralBookplateMatte(_ bytes: inout [UInt8], width w: Int, height h: Int) {
        guard w >= 32, h >= 32 else { return }
        for i in stride(from: 3, to: bytes.count, by: 4) {
            if bytes[i] < 250 { return }
        }
        let mr = Int(bytes[0]), mg = Int(bytes[1]), mb = Int(bytes[2])
        let high = max(mr, max(mg, mb)), low = min(mr, min(mg, mb))
        guard high - low <= 16, high <= 38 || low >= 218 else { return }
        func distance(_ p: Int) -> Int {
            let i = p * 4
            return max(abs(Int(bytes[i]) - mr), max(abs(Int(bytes[i + 1]) - mg), abs(Int(bytes[i + 2]) - mb)))
        }
        let inset = max(1, Int((Double(min(w, h)) * 0.018).rounded()))
        for ring in [0, inset] {
            for sample in 0...24 {
                let x = ring + (w - 1 - 2 * ring) * sample / 24
                let y = ring + (h - 1 - 2 * ring) * sample / 24
                if distance(ring * w + x) > 9 ||
                    distance((h - 1 - ring) * w + x) > 9 ||
                    distance(y * w + ring) > 9 ||
                    distance(y * w + w - 1 - ring) > 9 { return }
            }
        }
        var visited = [UInt8](repeating: 0, count: w * h)
        var queue = [Int]()
        queue.reserveCapacity(w * h / 4)
        func offer(_ i: Int) {
            guard visited[i] == 0 else { return }
            visited[i] = 1
            if distance(i) < 26 { queue.append(i) }
        }
        for x in 0..<w { offer(x); offer((h - 1) * w + x) }
        for y in 0..<h { offer(y * w); offer(y * w + w - 1) }
        // Complete the region analysis before modifying the RGBA buffer.
        var head = 0
        while head < queue.count {
            let p = queue[head]
            head += 1
            let x = p % w, y = p / w
            if x > 0 { offer(p - 1) }
            if x + 1 < w { offer(p + 1) }
            if y > 0 { offer(p - w) }
            if y + 1 < h { offer(p + w) }
        }
        let area = Double(w * h)
        guard Double(queue.count) >= area * 0.08,
              Double(queue.count) <= area * 0.94 else { return }
        var clearCount = 0
        for p in queue where distance(p) <= 8 { clearCount += 1 }
        guard Double(clearCount) >= Double(queue.count) * 0.82 else { return }

        for p in queue {
            let i = p * 4
            let alpha = smooth(8, 26, Float(distance(p)))
            // Store premultiplied colors. Subtract the old matte before
            // placing the painting on either native theme's surface.
            if alpha <= 0.001 {
                bytes[i] = 0; bytes[i + 1] = 0; bytes[i + 2] = 0; bytes[i + 3] = 0
            } else {
                let r = Float(bytes[i]) / 255 - Float(mr) / 255 * (1 - alpha)
                let g = Float(bytes[i + 1]) / 255 - Float(mg) / 255 * (1 - alpha)
                let b = Float(bytes[i + 2]) / 255 - Float(mb) / 255 * (1 - alpha)
                bytes[i] = byte(min(alpha, max(0, r)))
                bytes[i + 1] = byte(min(alpha, max(0, g)))
                bytes[i + 2] = byte(min(alpha, max(0, b)))
                bytes[i + 3] = byte(alpha)
            }
        }
    }

}


/// A low-resolution field derived solely from the original warm highlight
/// pixels, matched to the native Compose processor. It lives underneath the
/// painting, not on top of the text. The byte buffer is premultiplied RGBA.
private struct ArtworkLightField {
    let width: Int
    let height: Int
    let columns: Int
    let rows: Int
    var values: [Float]

    init(width: Int, height: Int) {
        self.width = width
        self.height = height
        columns = min(96, width)
        rows = min(height, max(1, Int((96.0 * Double(height) / Double(width)).rounded())))
        values = [Float](repeating: 0, count: columns * rows)
    }

    mutating func add(x: Int, y: Int, value: Float) {
        values[(y * rows / height) * columns + x * columns / width] += value
    }

    func compositeBehind(_ bytes: inout [UInt8]) {
        var normalized = values
        for i in normalized.indices {
            let x = i % columns, y = i / columns
            let cw = ((x + 1) * width + columns - 1) / columns - (x * width + columns - 1) / columns
            let ch = ((y + 1) * height + rows - 1) / rows - (y * height + rows - 1) / rows
            normalized[i] /= Float(max(1, cw * ch))
        }
        var blurred = normalized
        for _ in 0..<2 { blurred = blur(blur(blurred, horizontal: true), horizontal: false) }
        func byte(_ value: Float) -> UInt8 {
            UInt8(min(255, max(0, (value * 255).rounded())))
        }
        for y in 0..<height {
            let fy = min(Float(rows - 1), max(0, (Float(y) + 0.5) * Float(rows) / Float(height) - 0.5))
            let y0 = Int(fy), y1 = min(rows - 1, y0 + 1)
            let dy = fy - Float(y0)
            for x in 0..<width {
                let fx = min(Float(columns - 1), max(0, (Float(x) + 0.5) * Float(columns) / Float(width) - 0.5))
                let x0 = Int(fx), x1 = min(columns - 1, x0 + 1)
                let dx = fx - Float(x0)
                let top = blurred[y0 * columns + x0] * (1 - dx) + blurred[y0 * columns + x1] * dx
                let bottom = blurred[y1 * columns + x0] * (1 - dx) + blurred[y1 * columns + x1] * dx
                let field = (top * (1 - dy) + bottom * dy) * 1.3
                if field < 0.002 { continue }
                let i = (y * width + x) * 4
                let a = Float(bytes[i + 3]) / 255
                let halo = min(0.18, field) * (1 - a)
                let out = a + halo
                if out < 0.004 { continue }
                bytes[i] = byte(Float(bytes[i]) / 255 + 0.63 * halo)
                bytes[i + 1] = byte(Float(bytes[i + 1]) / 255 + 0.43 * halo)
                bytes[i + 2] = byte(Float(bytes[i + 2]) / 255 + 0.20 * halo)
                bytes[i + 3] = byte(out)
            }
        }
    }

    private func blur(_ input: [Float], horizontal: Bool) -> [Float] {
        var result = [Float](repeating: 0, count: input.count)
        for y in 0..<rows {
            for x in 0..<columns {
                var sum: Float = 0
                for d in -2...2 {
                    let sx = horizontal ? min(columns - 1, max(0, x + d)) : x
                    let sy = horizontal ? y : min(rows - 1, max(0, y + d))
                    sum += input[sy * columns + sx]
                }
                result[y * columns + x] = sum / 5
            }
        }
        return result
    }
}


/// Matched to the source-specific Compose recipe. Preserve original colors
/// in the uploaded file; this only affects the dark Home presentation.
private func gradeMoonlitPainting(_ bytes: inout [UInt8], width w: Int, height h: Int) {
    var distance = [Int32](repeating: 32767, count: w * h)
    for i in distance.indices where bytes[i * 4 + 3] <= 25 { distance[i] = 0 }
    for y in 0..<h {
        for x in 0..<w {
            let i = y * w + x
            var d = distance[i]
            if x > 0 { d = min(d, distance[i - 1] + 3) }
            if y > 0 {
                d = min(d, distance[i - w] + 3)
                if x > 0 { d = min(d, distance[i - w - 1] + 4) }
                if x + 1 < w { d = min(d, distance[i - w + 1] + 4) }
            }
            distance[i] = d
        }
    }
    for y in stride(from: h - 1, through: 0, by: -1) {
        for x in stride(from: w - 1, through: 0, by: -1) {
            let i = y * w + x
            var d = distance[i]
            if x + 1 < w { d = min(d, distance[i + 1] + 3) }
            if y + 1 < h {
                d = min(d, distance[i + w] + 3)
                if x > 0 { d = min(d, distance[i + w - 1] + 4) }
                if x + 1 < w { d = min(d, distance[i + w + 1] + 4) }
            }
            distance[i] = d
        }
    }
    let scale = 3 * Float(min(w, h))
    var atmosphere = NightPigmentField(width: w, height: h)
    func s(_ lo: Float, _ hi: Float, _ v: Float) -> Float {
        let t = min(1, max(0, (v - lo) / (hi - lo)))
        return t * t * (3 - 2 * t)
    }
    func g(_ x: Float, _ y: Float, _ cx: Float, _ cy: Float, _ rx: Float, _ ry: Float) -> Float {
        let dx = (x - cx) / rx
        let dy = (y - cy) / ry
        return exp(-1.2 * (dx * dx + dy * dy))
    }
    func byte(_ v: Float) -> UInt8 { UInt8(min(255, max(0, (v * 255).rounded()))) }
    for pixel in distance.indices {
        let i = pixel * 4
        let a = Float(bytes[i + 3]) / 255
        if a == 0 { continue }
        let r = min(1, Float(bytes[i]) / 255 / a)
        let green = min(1, Float(bytes[i + 1]) / 255 / a)
        let b = min(1, Float(bytes[i + 2]) / 255 / a)
        let l = 0.2126 * r + 0.7152 * green + 0.0722 * b
        let x = Float(pixel % w) / Float(max(1, w - 1))
        let y = Float(pixel / w) / Float(max(1, h - 1))
        let paperX = s(0.37, 0.48, x) * (1 - s(0.80, 0.88, x))
        let paperY = s(0.50, 0.57, y) * (1 - s(0.82, 0.90, y))
        let paper = paperX * paperY
        let crescent = g(x, y, 0.539, 0.17, 0.055, 0.075)
        let roof = g(x, y, 0.70, 0.31, 0.20, 0.14)
        let windowX = s(0.51, 0.57, x) * (1 - s(0.86, 0.93, x))
        let windowY = s(0.29, 0.37, y) * (1 - s(0.50, 0.55, y))
        let windows = windowX * windowY
        let stack = g(x, y, 0.34, 0.72, 0.17, 0.18)
        let lantern = g(x, y, 0.35, 0.43, 0.045, 0.067)
        let emission = s(0.52, 0.86, l) * s(0.10, 0.32, r - b) * max(crescent, max(windows, lantern))
        let gainA: Float = 0.51 + 0.25 * paper + 0.10 * stack
        let gainB: Float = 0.06 * roof + 0.06 * windows + 0.27 * crescent + 0.28 * emission
        let gain = gainA + gainB
        // Neutral aged paper and darker botanical pigment are separate
        // tonal subjects; match the same source-local treatment as Compose.
        let pagePigment = paper * s(0.30, 0.56, l) * s(0.025, 0.10, r - b)
        let botanical = paper * (1 - s(0.24, 0.42, l))
        let sat: Float = 0.72 - 0.29 * paper - 0.10 * pagePigment
            + 0.13 * botanical + 0.24 * emission
        let outerX = s(0.13, 0.28, x) * (1 - 0.78 * s(0.88, 1, x))
        let outerY = s(0.02, 0.10, y) * (1 - s(0.88, 1, y))
        let edge: Float = 0.45 + 0.55 * s(0, 0.035, Float(distance[pixel]) / scale)
        let subject = max(max(paper * 0.96, crescent * 0.95), max(windows * 0.92, stack * 0.85))
        let shadowAlpha = (0.35 + 0.65 * s(0.035, 0.26, l)) * edge
        let pen = g(x, y, 0.73, 0.85, 0.13, 0.038)
        let car = g(x, y, 0.48, 0.50, 0.065, 0.035)
        let alpha = a * max(shadowAlpha, subject) * outerX * outerY * (1 - 0.65 * pen) * (1 - 0.70 * car)
        let protect = max(max(paper, crescent), max(windows, stack * 0.65))
        let inkMix: Float = 0.58 * (1 - s(0.04, 0.23, l)) * (1 - 0.75 * protect)
        let paperLift: Float = 0.025 * paper * s(0.18, 0.60, l)
        let neutralIvory: Float = 0.025 * pagePigment
        let rawR = (l + (r - l) * sat) * gain * (1 - 0.05 * (1 - paper))
        let rawG = (l + (green - l) * sat) * gain
        let rawB = (l + (b - l) * sat) * gain + 0.009 * (1 - s(0.08, 0.40, l))
        let rr = rawR * (1 - inkMix) + (5.0 / 255.0) * inkMix + paperLift - 0.20 * neutralIvory
        let gg = rawG * (1 - inkMix) + (15.0 / 255.0) * inkMix + paperLift + neutralIvory
        let bb = rawB * (1 - inkMix) + (24.0 / 255.0) * inkMix + paperLift + 0.12 * neutralIvory
        let weight = a * (1 - 0.85 * protect) * (1 - s(0.28, 0.72, l))
        atmosphere.add(x: pixel % w, y: pixel / w, weight: weight, r: rr, g: gg, b: bb)
        bytes[i] = byte(min(1, max(0, rr)) * alpha)
        bytes[i + 1] = byte(min(1, max(0, gg)) * alpha)
        bytes[i + 2] = byte(min(1, max(0, bb)) * alpha)
        bytes[i + 3] = byte(alpha)
    }
    atmosphere.compositeBehind(&bytes)
}


/// Same bounded source-pigment field as Compose. The buffer stores
/// premultiplied RGBA; diffuse color is composited behind existing pixels.
private struct NightPigmentField {
    let width: Int
    let height: Int
    let cols: Int
    let rows: Int
    var field: [[Float]]

    init(width: Int, height: Int) {
        self.width = width; self.height = height
        cols = min(64, width)
        rows = min(height, max(1, Int((64.0 * Double(height) / Double(width)).rounded())))
        field = Array(repeating: Array(repeating: 0, count: cols * rows), count: 4)
    }
    mutating func add(x: Int, y: Int, weight: Float, r: Float, g: Float, b: Float) {
        let i = (y * rows / height) * cols + x * cols / width
        field[0][i] += weight
        field[1][i] += weight * min(1, max(0, r))
        field[2][i] += weight * min(1, max(0, g))
        field[3][i] += weight * min(1, max(0, b))
    }
    func compositeBehind(_ bytes: inout [UInt8]) {
        var blurred = field
        for c in 0..<4 {
            var input = field[c]
            for i in input.indices {
                let x = i % cols, y = i / cols
                let cw = ((x + 1) * width + cols - 1) / cols - (x * width + cols - 1) / cols
                let ch = ((y + 1) * height + rows - 1) / rows - (y * height + rows - 1) / rows
                input[i] /= Float(max(1, cw * ch))
            }
            blurred[c] = blur(blur(input, horizontal: true), horizontal: false)
        }
        func byte(_ v: Float) -> UInt8 { UInt8(min(255, max(0, (v * 255).rounded()))) }
        for y in 0..<height {
            let fy = min(Float(rows - 1), max(0, (Float(y) + 0.5) * Float(rows) / Float(height) - 0.5))
            let y0 = Int(fy), y1 = min(rows - 1, y0 + 1)
            let dy = fy - Float(y0)
            for x in 0..<width {
                let fx = min(Float(cols - 1), max(0, (Float(x) + 0.5) * Float(cols) / Float(width) - 0.5))
                let x0 = Int(fx), x1 = min(cols - 1, x0 + 1)
                let dx = fx - Float(x0)
                func sample(_ c: Int) -> Float {
                    let v = blurred[c]
                    let t = v[y0 * cols + x0] * (1 - dx) + v[y0 * cols + x1] * dx
                    let b = v[y1 * cols + x0] * (1 - dx) + v[y1 * cols + x1] * dx
                    return t * (1 - dy) + b * dy
                }
                let coverage = sample(0)
                if coverage < 0.003 { continue }
                let i = (y * width + x) * 4
                let a = Float(bytes[i + 3]) / 255
                let haze = min(0.21, coverage * 0.42) * (1 - a)
                let out = a + haze
                if out < 0.004 { continue }
                for c in 0..<3 {
                    bytes[i + c] = byte(Float(bytes[i + c]) / 255 + sample(c + 1) / coverage * haze)
                }
                bytes[i + 3] = byte(out)
            }
        }
    }
    private func blur(_ input: [Float], horizontal: Bool) -> [Float] {
        var output = [Float](repeating: 0, count: input.count)
        let weights: [Float] = [1, 4, 6, 4, 1]
        for y in 0..<rows {
            for x in 0..<cols {
                var sum: Float = 0
                for d in -2...2 {
                    let sx = horizontal ? x + d : x
                    let sy = horizontal ? y : y + d
                    if sx >= 0 && sx < cols && sy >= 0 && sy < rows {
                        sum += input[sy * cols + sx] * weights[d + 2]
                    }
                }
                output[y * cols + x] = sum / 16
            }
        }
        return output
    }
}


/// Matched inverse warp using the original premultiplied RGBA pixels.
/// Keep spatial falloff and subject positions identical to Compose.
private func reframeMoonlitSubjects(_ source: [UInt8], width w: Int, height h: Int) -> [UInt8] {
    var result = [UInt8](repeating: 0, count: source.count)
    let lastX = Float(max(1, w - 1))
    let lastY = Float(max(1, h - 1))
    let regions: [[Float]] = [
        [0.539, 0.17, 0.085, 0.095, 0.76, 0, 0],
        [0.72, 0.34, 0.23, 0.20, 0.94, 0.010, 0.008],
        [0.62, 0.70, 0.26, 0.19, 0.98, 0.025, 0.010]
    ]
    for y in 0..<h {
        for x in 0..<w {
            let u = Float(x) / lastX
            let v = Float(y) / lastY
            var du: Float = 0
            var dv: Float = 0
            for r in regions {
                let nx = (u - r[0]) / r[2]
                let ny = (v - r[1]) / r[3]
                let xx = nx * nx
                let yy = ny * ny
                let influence = exp(-1.8 * (xx * xx + yy * yy))
                let inverse = 1 / r[4] - 1
                du += influence * ((u - r[0]) * inverse - r[5] / r[4])
                dv += influence * ((v - r[1]) * inverse - r[6] / r[4])
            }
            let sx = (u + du) * lastX
            let sy = (v + dv) * lastY
            guard sx >= 0, sy >= 0, sx <= Float(w - 1), sy <= Float(h - 1) else { continue }
            let x0 = Int(sx), y0 = Int(sy)
            let x1 = min(w - 1, x0 + 1), y1 = min(h - 1, y0 + 1)
            let fx = sx - Float(x0), fy = sy - Float(y0)
            let p0 = (y0 * w + x0) * 4
            let p1 = (y0 * w + x1) * 4
            let p2 = (y1 * w + x0) * 4
            let p3 = (y1 * w + x1) * 4
            let a = (1 - fx) * (1 - fy)
            let b = fx * (1 - fy)
            let c = (1 - fx) * fy
            let d = fx * fy
            let out = (y * w + x) * 4
            for channel in 0..<4 {
                let top = Float(source[p0 + channel]) * a + Float(source[p1 + channel]) * b
                let bottom = Float(source[p2 + channel]) * c + Float(source[p3 + channel]) * d
                result[out + channel] = UInt8(min(255, max(0, (top + bottom).rounded())))
            }
        }
    }
    return result
}


/// Shrink only disconnected upper-sky ornaments, retaining their original
/// premultiplied pigment and protecting the connected moon and landscape.
private func refinePaintedStars(_ bytes: inout [UInt8], width w: Int, height h: Int) {
    let topH = max(1, min(h, Int(Float(h) * 0.32)))
    var seen = [Bool](repeating: false, count: w * topH)
    var queue = [Int](repeating: 0, count: w * topH)
    for start in seen.indices {
        if seen[start] || bytes[start * 4 + 3] <= 8 { continue }
        var head = 0, tail = 1
        queue[0] = start; seen[start] = true
        var left = start % w, right = start % w
        var top = start / w, bottom = start / w
        while head < tail {
            let i = queue[head]; head += 1
            let x = i % w, y = i / w
            left = min(left, x); right = max(right, x)
            top = min(top, y); bottom = max(bottom, y)
            for dy in -1...1 {
                for dx in -1...1 {
                    let nx = x + dx, ny = y + dy
                    if nx < 0 || nx >= w || ny < 0 || ny >= topH { continue }
                    let n = ny * w + nx
                    if !seen[n] && bytes[n * 4 + 3] > 8 {
                        seen[n] = true
                        queue[tail] = n; tail += 1
                    }
                }
            }
        }
        let bw = right - left + 1, bh = bottom - top + 1
        let fraction = max(Float(bw) / Float(w), Float(bh) / Float(h))
        if bottom >= topH - 1 || Float(left) < Float(w) * 0.30 ||
            Float(bw) > Float(w) * 0.055 || Float(bh) > Float(h) * 0.085 ||
            tail < 20 || fraction < 0.016 { continue }
        if Float(left) < Float(w) * 0.58 && Float(right) > Float(w) * 0.50 &&
            Float(top) < Float(h) * 0.22 && Float(bottom) > Float(h) * 0.10 { continue }
        let scale: Float = fraction > 0.03 ? 0.43 : 0.68
        let cx = Float(left + right) / 2, cy = Float(top + bottom) / 2
        var sum = [Float](repeating: 0, count: bw * bh * 4)
        for j in 0..<tail {
            let p = queue[j], i = p * 4
            let nx = min(right, max(left, Int((cx + (Float(p % w) - cx) * scale).rounded())))
            let ny = min(bottom, max(top, Int((cy + (Float(p / w) - cy) * scale).rounded())))
            let n = ((ny - top) * bw + nx - left) * 4
            for c in 0..<4 {
                sum[n + c] += Float(bytes[i + c]) * scale * scale
                bytes[i + c] = 0
            }
        }
        for n in 0..<(bw * bh) {
            let a = sum[n * 4 + 3]
            if a < 0.765 { continue }
            let i = ((top + n / bw) * w + left + n % bw) * 4
            if bytes[i + 3] > 8 { continue }
            seen[i / 4] = true
            let normalization = min(1, 255 / a)
            for c in 0..<4 {
                bytes[i + c] = UInt8(min(255, max(0, (sum[n * 4 + c] * normalization).rounded())))
            }
        }
    }
}

/// Source-derived sky texture behind the vignette. All pixel operations
/// match the Compose recipe, with premultiplied RGBA storage on iOS.
private struct OriginalSkyPatch {
    private let side = 48
    private let texture: [Float]

    init(_ bytes: [UInt8], width w: Int, height h: Int) {
        let side = 48
        texture = (0..<(side * side)).map { i in
            let u = 0.49 + 0.065 * Float(i % side) / Float(side - 1)
            let v = 0.25 + 0.060 * Float(i / side) / Float(side - 1)
            let x = Int((u * Float(w - 1)).rounded())
            let y = Int((v * Float(h - 1)).rounded())
            let p = (y * w + x) * 4
            let a = Float(bytes[p + 3]) / 255
            if a < 200.0 / 255.0 { return 0 }
            let r = min(1, Float(bytes[p]) / 255 / a)
            let g = min(1, Float(bytes[p + 1]) / 255 / a)
            let b = min(1, Float(bytes[p + 2]) / 255 / a)
            if b <= r || g <= r { return 0 }
            let l = 0.2126 * r + 0.7152 * g + 0.0722 * b
            return min(0.065, max(-0.045, l - 0.12))
        }
    }

    private func mirror(_ value: Float) -> Float {
        let v = value.truncatingRemainder(dividingBy: 2)
        return v <= 1 ? v : 2 - v
    }

    func extendBehind(_ bytes: inout [UInt8], width w: Int, height h: Int) {
        func smooth(_ lo: Float, _ hi: Float, _ v: Float) -> Float {
            let t = min(1, max(0, (v - lo) / (hi - lo)))
            return t * t * (3 - 2 * t)
        }
        func byte(_ v: Float) -> UInt8 { UInt8(min(255, max(0, (v * 255).rounded()))) }
        for y in 0..<Int(Float(h) * 0.57) {
            let v = Float(y) / Float(max(1, h - 1))
            for x in 0..<w {
                let u = Float(x) / Float(max(1, w - 1))
                let sx = smooth(0.23, 0.47, u) * (1 - smooth(0.91, 1, u))
                let sy = smooth(0, 0.075, v) * (1 - smooth(0.35, 0.57, v))
                let shape = sx * sy
                if shape < 0.002 { continue }
                let tx = Int((mirror(u * 3.1 + v * 0.55) * Float(side - 1)).rounded())
                let ty = Int((mirror(v * 3.7 + u * 0.35) * Float(side - 1)).rounded())
                let grain = texture[ty * side + tx] * 0.30
                let i = (y * w + x) * 4
                let a = Float(bytes[i + 3]) / 255
                let behind = 0.68 * shape * (1 - a)
                let out = a + behind
                if out < 0.004 { continue }
                bytes[i] = byte(Float(bytes[i]) / 255 + min(1, max(0, 0.029 + grain)) * behind)
                bytes[i + 1] = byte(Float(bytes[i + 1]) / 255 + min(1, max(0, 0.064 + grain)) * behind)
                bytes[i + 2] = byte(Float(bytes[i + 2]) / 255 + min(1, max(0, 0.111 + grain)) * behind)
                bytes[i + 3] = byte(out)
            }
        }
    }
}
