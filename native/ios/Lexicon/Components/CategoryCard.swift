import SwiftUI
import UIKit

/// An illustrated bookplate. Artwork uses aspect fit to preserve every
/// transparent watercolor edge rather than cropping botanicals to a square.
struct CategoryCard: View {
    let category: LexiconCategory
    var shelfArtworkHeight: CGFloat? = nil
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    private var isDark: Bool { scheme == .dark }
    private var largeText: Bool { dynamicTypeSize.isAccessibilitySize }
    // Prefer the new light-specific asset once supplied; keep the original
    // as a safe fallback. Never modify the original asset catalog.
    private var artName: String {
        if isDark {
            return "ref-home-" + category.id.replacingOccurrences(of: "_", with: "-")
        }
        let light = category.illustration + "-light"
        return !isDark && UIImage(named: light) != nil ? light : category.illustration
    }
    private var artHeight: CGFloat { shelfArtworkHeight ?? (largeText ? 100 : 74) }
    @ScaledMetric(relativeTo: .caption) private var captionHeight: CGFloat = 28

    var body: some View {
        let p = lexiconPalette(scheme)
        let surface = isDark ? Color(hex: "101B23").opacity(0.32) : LexiconColors.card
        let lowerSurface = isDark ? Color(hex: "101B23").opacity(0.12) : Color(hex: "F8F1E6")
        let border = isDark ? Color(hex: "B6A486").opacity(0.10) : LexiconColors.foreground.opacity(0.10)
        Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            vm.setActiveTab(.dictionary)
        } label: {
            VStack(spacing: 0) {
                Group {
                    if isDark {
                        Image(artName).resizable().renderingMode(.original)
                    } else {
                        PreparedArtworkImage(name: artName, treatment: .bookplate)
                    }
                }
                .scaledToFit()
                .frame(maxWidth: .infinity)
                .frame(height: artHeight)
                .accessibilityHidden(true)

                Spacer(minLength: 0).frame(height: isDark && !largeText ? 0 : 5)

                VStack(spacing: shelfArtworkHeight != nil && !largeText ? 1 : 2) {
                    Text(category.title)
                        .font(.lexiconDisplay(shelfArtworkHeight != nil && !largeText ? 10.5 : 12))
                        .foregroundStyle(p.foreground)
                        .fixedSize(horizontal: false, vertical: true)
                    Text(category.subtitle)
                        .font(.lexiconBody(isDark && shelfArtworkHeight != nil && !largeText ? 7.5 : (shelfArtworkHeight != nil ? 8.5 : 9.5)))
                        .foregroundStyle(isDark ? Color(hex: "B4AD9E") : p.mutedForeground)
                        .fixedSize(horizontal: false, vertical: true)
                }
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
            }
            .padding(.horizontal, largeText ? 7 : 3)
            .padding(.vertical, isDark && !largeText ? 4 : 8)
            .frame(maxWidth: .infinity)
            .frame(minHeight: artHeight + captionHeight + (shelfArtworkHeight != nil && !largeText ? 5 : 10), alignment: .top)
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
