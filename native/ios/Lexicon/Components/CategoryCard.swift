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
        let surface = isDark ? Color(hex: "15212A").opacity(0.22) : LexiconColors.card
        let lowerSurface = isDark ? Color(hex: "15212A").opacity(0.47) : Color(hex: "F8F1E6")
        let border = isDark ? Color(hex: "B6A486").opacity(0.12) : LexiconColors.foreground.opacity(0.10)
        Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            vm.setActiveTab(.dictionary)
        } label: {
            VStack(spacing: 0) {
                PreparedArtworkImage(name: artName, treatment: .bookplate)
                    .scaledToFit()
                    .frame(maxWidth: .infinity)
                    .frame(height: largeText ? 88 : (isDark ? 64 : 65))
                    .accessibilityHidden(true)

                Spacer(minLength: 0).frame(height: isDark ? 3 : 5)

                VStack(spacing: 2) {
                    Text(category.title)
                        .font(.lexiconDisplay(12))
                        .foregroundStyle(p.foreground)
                        .fixedSize(horizontal: false, vertical: true)
                    Text(category.subtitle)
                        .font(.lexiconBody(isDark ? 9 : 9.5))
                        .foregroundStyle(p.mutedForeground)
                        .fixedSize(horizontal: false, vertical: true)
                }
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
            }
            .padding(.horizontal, isDark ? 5 : 7)
            .padding(.vertical, 8)
            .frame(maxWidth: .infinity)
            .frame(minHeight: largeText ? 190 : (isDark ? 122 : 130), alignment: .top)
            .background(LinearGradient(colors: [surface, lowerSurface], startPoint: .top, endPoint: .bottom))
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 12, style: .continuous)
                    .stroke(border, lineWidth: 1)
            )
            .contentShape(RoundedRectangle(cornerRadius: 12))
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .combine)
        .accessibilityLabel(Text("\(category.title). \(category.subtitle)"))
        .accessibilityHint(Text("Browse \(category.title)"))
    }
}
