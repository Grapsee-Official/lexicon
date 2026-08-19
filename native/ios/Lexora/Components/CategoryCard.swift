//
//  CategoryCard.swift
//  Lexora
//
//  Translation of `src/components/lexora/category-card.tsx`.
//
//  Borderless, seamless, bg clear. Square image.
//  Title + subtitle centered below. Width ~96pt.
//

import SwiftUI

struct CategoryCard: View {
    let category: LexoraCategory
    @EnvironmentObject private var vm: LexoraViewModel
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            vm.setActiveTab(.dictionary)
        } label: {
            VStack(spacing: 8) {
                // Square image — no border, no shadow, bg matches image.
                Image(category.illustration)
                    .resizable()
                    .scaledToFill()
                    .frame(width: 80, height: 80)
                    .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                    .accessibilityLabel(Text("\(category.title) illustration"))

                // Title + subtitle perfectly centered.
                VStack(spacing: 2) {
                    Text(category.title)
                        .font(.lexoraDisplay(11, weight: .semibold))
                        .foregroundStyle(lexoraPalette(scheme).foreground)
                        .lineLimit(1)
                    Text(category.subtitle)
                        .font(.lexoraBody(8))
                        .foregroundStyle(lexoraPalette(scheme).mutedForeground)
                        .lineLimit(1)
                }
                .frame(maxWidth: .infinity, alignment: .center)
            }
            .padding(8)
            .frame(width: 96)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}

#Preview {
    CategoryCard(category: LexoraData.categories[0])
        .environmentObject(LexoraViewModel())
        .background(LexoraColors.background)
}
