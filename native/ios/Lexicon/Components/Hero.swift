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
    var headerInset: CGFloat = 0

    var body: some View {
        GeometryReader { geo in
            let width = geo.size.width
            let height = geo.size.height
            let largeText = dynamicTypeSize.isAccessibilitySize
            let edge: CGFloat = largeText ? 0.67 : 0.47
            let header = min(height, max(0, headerInset))
            let contentHeight = max(1, height - header)
            let stretch: CGFloat = largeText ? 1 : 1.04
            let artWidth = min(width * (largeText ? 0.96 : 0.95), contentHeight * 1.5 / stretch * 1.08)
            let artHeight = artWidth / 1.5 * stretch
            let artX = max(width * 0.63 - artWidth * 0.54, width - artWidth * 0.95)
            let moonY = min(contentHeight * (extendsBehindTags ? 0.09 : 0.10), width * 0.055)
            let artY = header + (largeText ? contentHeight * 0.90 - artHeight : moonY - artHeight * 0.17)

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
                        .init(color: .black, location: 0.83),
                        .init(color: .black.opacity(0.12), location: 0.96),
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
    @State private var finished = false

    var body: some View {
        Group {
            if let image = prepared ?? HomeArtworkProcessor.cached(name: name, treatment: treatment) {
                Image(uiImage: image).resizable()
            } else if finished {
                Image(name).resizable()
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
        } else if treatment == .moonlit {
            gradeMoonlitPainting(&bytes, width: w, height: h)
        } else {
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
                let gain = min(1, max(0, 0.85 + 0.13 * highlight - 0.02 * warm * (1 - highlight)))
                let sat = 0.80 + 0.18 * highlight
                let shadow = 1 - smooth(0.09, 0.35, l)
                let emission = a * smooth(0.45, 0.83, l) * smooth(0.05, 0.22, r - b) * 0.42
                let pixel = i / 4
                lightField.add(x: pixel % w, y: pixel / w, value: emission)
                let alpha = a * (0.58 + 0.42 * smooth(0.035, 0.24, l))
                let rr = (l + (r - l) * sat) * gain * (1 - 0.10 * shadow)
                let gg = (l + (g - l) * sat) * gain
                let bb = (l + (b - l) * sat) * gain + 0.012 * shadow
                bytes[i] = byte(rr * alpha)
                bytes[i + 1] = byte(gg * alpha)
                bytes[i + 2] = byte(bb * alpha)
                bytes[i + 3] = byte(alpha)
            }
            lightField.compositeBehind(&bytes)
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
        var head = 0
        while head < queue.count {
            let p = queue[head]
            head += 1
            let i = p * 4
            let alpha = smooth(8, 26, Float(distance(p)))
            // Store premultiplied colors. Subtract the old matte's
            // contribution before placing the painting on a new surface.
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
            let x = p % w, y = p / w
            if x > 0 { offer(p - 1) }
            if x + 1 < w { offer(p + 1) }
            if y > 0 { offer(p - w) }
            if y + 1 < h { offer(p + w) }
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
        let sat: Float = 0.72 - 0.29 * paper + 0.24 * emission
        let outerX = s(0.13, 0.28, x) * (1 - s(0.86, 0.98, x))
        let outerY = s(0.02, 0.10, y) * (1 - s(0.88, 1, y))
        let edge: Float = 0.45 + 0.55 * s(0, 0.035, Float(distance[pixel]) / scale)
        let subject = max(max(paper * 0.96, crescent * 0.95), max(windows * 0.92, stack * 0.85))
        let shadowAlpha = (0.35 + 0.65 * s(0.035, 0.26, l)) * edge
        let pen = g(x, y, 0.73, 0.85, 0.13, 0.038)
        let car = g(x, y, 0.48, 0.50, 0.065, 0.035)
        let alpha = a * max(shadowAlpha, subject) * outerX * outerY * (1 - 0.65 * pen) * (1 - 0.70 * car)
        let rr = (l + (r - l) * sat) * gain * (1 - 0.05 * (1 - paper))
        let gg = (l + (green - l) * sat) * gain
        let bb = (l + (b - l) * sat) * gain + 0.009 * (1 - s(0.08, 0.40, l))
        // Clamp straight components before premultiplication.
        bytes[i] = byte(min(1, max(0, rr)) * alpha)
        bytes[i + 1] = byte(min(1, max(0, gg)) * alpha)
        bytes[i + 2] = byte(min(1, max(0, bb)) * alpha)
        bytes[i + 3] = byte(alpha)
    }
}
