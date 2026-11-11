//
//  CategoryCard.swift
//  Lexicon
//

import SwiftUI

struct CategoryCard: View {
    let category: LexiconCategory
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme

    private var isDark: Bool { scheme == .dark }

    private var imageName: String {
        isDark ? "\(category.illustration)-dark" : category.illustration
    }

    var body: some View {
        Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            vm.setActiveTab(.dictionary)
        } label: {
            VStack(spacing: 5) {
                GeometryReader { geo in
                    Image(imageName)
                        .resizable()
                        .scaledToFill()
                        .frame(width: geo.size.width, height: geo.size.width)
                        .clipShape(RoundedRectangle(cornerRadius: 9, style: .continuous))
                }
                .aspectRatio(1, contentMode: .fit)
                .lexiconDecorative()

                VStack(spacing: 1.5) {
                    Text(category.title)
                        .font(.lexiconDisplay(11.5, weight: .medium))
                        .foregroundStyle(lexiconPalette(scheme).foreground)
                        .lineLimit(1)
                        .minimumScaleFactor(0.85)
                    Text(category.subtitle)
                        .font(.lexiconBody(8))
                        .foregroundStyle(lexiconPalette(scheme).mutedForeground)
                        .lineLimit(1)
                        .minimumScaleFactor(0.85)
                }
                .frame(maxWidth: .infinity, alignment: .center)
            }
            .padding(.horizontal, 6)
            .padding(.vertical, 7)
            .frame(maxWidth: .infinity)
            .background(lexiconPalette(scheme).card)
            .clipShape(RoundedRectangle(cornerRadius: 13, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 13, style: .continuous)
                    .stroke(lexiconPalette(scheme).border, lineWidth: 1)
            )
            .shadow(
                color: isDark ? .clear : LexiconColors.foreground.opacity(0.06),
                radius: 4,
                x: 0,
                y: 2
            )
            .contentShape(RoundedRectangle(cornerRadius: 13, style: .continuous))
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .combine)
        .accessibilityLabel(Text("\(category.title). \(category.subtitle)"))
        .accessibilityHint(Text("Browse the \(category.title) category"))
        .accessibilityAddTraits(.isButton)
    }
}
