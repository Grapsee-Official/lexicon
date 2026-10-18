//
//  CategoryCard.swift
//  Lexicon
//
//  Translation of `src/components/lexicon/category-card.tsx`.
//
//  Borderless, seamless, bg clear. Square image.
//  Title + subtitle centered below. Width ~96pt.
//

import SwiftUI

struct CategoryCard: View {
    let category: LexiconCategory
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            vm.setActiveTab(.dictionary)
        } label: {
            VStack(spacing: 8) {
                // Square image — no border, no shadow, bg matches image.
                // Marked decorative so the card-level label below is the
                // single VoiceOver element (the image conveys no info
                // beyond what the title already provides).
                Image(category.illustration)
                    .resizable()
                    .scaledToFill()
                    .frame(width: 80, height: 80)
                    .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                    .lexiconDecorative()

                // Title + subtitle perfectly centered.
                VStack(spacing: 2) {
                    Text(category.title)
                        .font(.lexiconDisplay(11, weight: .semibold))
                        .foregroundStyle(lexiconPalette(scheme).foreground)
                        .lineLimit(1)
                    Text(category.subtitle)
                        .font(.lexiconBody(8))
                        .foregroundStyle(lexiconPalette(scheme).mutedForeground)
                        .lineLimit(1)
                }
                .frame(maxWidth: .infinity, alignment: .center)
            }
            .padding(8)
            .frame(width: 96)
            // Card is ~120pt tall × 96pt wide — meets the 44pt minimum
            // in both dimensions. No `.lexiconMinTouchTarget()` needed.
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        // P4-05 — combine the card into ONE VoiceOver element so the
        // user hears "Literature, {subtitle}, button" in one swipe.
        .accessibilityElement(children: .combine)
        .accessibilityLabel(Text("\(category.title). \(category.subtitle)"))
        .accessibilityHint(Text("Browse the \(category.title) category"))
        .accessibilityAddTraits(.isButton)
    }
}

#Preview {
    CategoryCard(category: LexiconData.categories[0])
        .environmentObject(LexiconViewModel())
        .background(LexiconColors.background)
}
