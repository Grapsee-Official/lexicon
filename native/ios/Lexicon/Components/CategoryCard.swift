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
    private var artName: String {
        isDark ? "\(category.illustration)-dark" : category.illustration
    }

    var body: some View {
        let p = lexiconPalette(scheme)
        Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            vm.setActiveTab(.dictionary)
        } label: {
            VStack(spacing: 0) {
                Image(artName)
                    .resizable()
                    .scaledToFit()
                    .frame(maxWidth: .infinity)
                    .frame(height: largeText ? 84 : 57)
                    .accessibilityHidden(true)

                Spacer(minLength: 2)

                VStack(spacing: 2) {
                    Text(category.title)
                        .font(.lexiconDisplay(12.5))
                        .foregroundStyle(p.foreground)
                        .lineLimit(1)
                        .minimumScaleFactor(0.76)
                    Text(category.subtitle)
                        .font(.lexiconBody(9))
                        .foregroundStyle(p.mutedForeground)
                        .lineLimit(1)
                        .minimumScaleFactor(0.75)
                }
                .multilineTextAlignment(.center)
                .frame(maxWidth: .infinity)
            }
            .padding(.horizontal, 6)
            .padding(.vertical, 8)
            .frame(maxWidth: .infinity)
            .frame(height: largeText ? 144 : 105)
            .background(p.card)
            .clipShape(RoundedRectangle(cornerRadius: 15, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 15, style: .continuous)
                    .stroke(p.border, lineWidth: 1)
            )
            .contentShape(RoundedRectangle(cornerRadius: 15))
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .combine)
        .accessibilityLabel(Text("\(category.title). \(category.subtitle)"))
        .accessibilityHint(Text("Browse \(category.title)"))
    }
}
