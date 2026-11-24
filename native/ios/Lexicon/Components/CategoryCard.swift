import SwiftUI

/// An illustrated bookplate. Artwork uses aspect fit to preserve every
/// transparent watercolor edge rather than cropping botanicals to a square.
struct CategoryCard: View {
    let category: LexiconCategory
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    private var isDark: Bool { scheme == .dark }
    private var largeText: Bool { dynamicTypeSize.isAccessibilitySize }
    // One original image per category, irrespective of theme.
    private var artName: String { category.illustration }

    var body: some View {
        let p = lexiconPalette(scheme)
        let surface = isDark ? Color(hex: "101B23").opacity(0.46) : LexiconColors.card
        let lowerSurface = isDark ? Color(hex: "101B23").opacity(0.12) : Color(hex: "F8F1E6")
        let border = isDark ? Color(hex: "B6A486").opacity(0.10) : LexiconColors.foreground.opacity(0.10)
        Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            vm.setActiveTab(.dictionary)
        } label: {
            VStack(spacing: 0) {
                // Keep the foreground illustration identical in both themes.
                // Its own softened pigments give the dark card a subtle
                // underpainting rather than a rectangular thumbnail surround.
                PreparedArtworkImage(name: artName, treatment: .bookplate)
                    .scaledToFit()
                    .frame(maxWidth: .infinity)
                    .frame(height: largeText ? 100 : 74)
                    .background {
                        if isDark {
                            GeometryReader { g in
                                RadialGradient(
                                    colors: [Color(hex: "28343B").opacity(0.20), .clear],
                                    center: UnitPoint(x: 0.5, y: 0.48),
                                    startRadius: 0,
                                    endRadius: max(1, max(g.size.width, g.size.height) * 0.63)
                                )
                            }
                        }
                    }
                    .accessibilityHidden(true)

                Spacer(minLength: 0).frame(height: isDark && !largeText ? 0 : 5)

                VStack(spacing: 2) {
                    Text(category.title)
                        .font(.lexiconDisplay(isDark && !largeText ? 11.5 : 12))
                        .foregroundStyle(p.foreground)
                        .fixedSize(horizontal: false, vertical: true)
                    Text(category.subtitle)
                        .font(.lexiconBody(isDark && !largeText ? 8.5 : 9.5))
                        .foregroundStyle(isDark ? Color(hex: "B4AD9E") : p.mutedForeground)
                        .fixedSize(horizontal: false, vertical: true)
                }
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
            }
            .padding(.horizontal, largeText ? 7 : 3)
            .padding(.vertical, isDark && !largeText ? 4 : 8)
            .frame(maxWidth: .infinity)
            .frame(minHeight: largeText ? 190 : (isDark ? 112 : 130), alignment: .top)
            .background {
                if isDark {
                    LinearGradient(stops: [
                        .init(color: surface, location: 0),
                        .init(color: Color(hex: "101B23").opacity(0.22), location: 0.66),
                        .init(color: lowerSurface, location: 1)
                    ], startPoint: .top, endPoint: .bottom)
                } else {
                    LinearGradient(colors: [surface, lowerSurface], startPoint: .top, endPoint: .bottom)
                }
            }
            .clipShape(RoundedRectangle(cornerRadius: isDark ? 10 : 12, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: isDark ? 10 : 12, style: .continuous)
                    .stroke(border, lineWidth: 1)
            )
            .contentShape(RoundedRectangle(cornerRadius: isDark ? 10 : 12))
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .combine)
        .accessibilityLabel(Text("\(category.title). \(category.subtitle)"))
        .accessibilityHint(Text("Browse \(category.title)"))
    }
}
