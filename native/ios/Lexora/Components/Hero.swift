//
//  Hero.swift
//  Lexora
//
//  Translation of `src/components/lexora/hero.tsx`.
//
//  SIMPLE 2-COLUMN HStack:
//    LEFT:  headline + paragraph (text)
//    RIGHT: countryside image (contained, no bleed, no scale, no mask)
//
//  Exact brand colors:
//    - Background:    #FAF3E9 (warm cream)
//    - Headline text: #677554 (brand green)
//    - Leaf sprig:    #677554
//

import SwiftUI

struct Hero: View {
    @EnvironmentObject private var vm: LexoraViewModel
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        HStack(alignment: .center, spacing: 8) {
            // ── LEFT column ──
            VStack(alignment: .leading, spacing: 8) {
                headline
                Text("A timeless dictionary crafted for curious minds.")
                    .font(.lexoraBody(11))
                    .foregroundStyle(lexoraPalette(scheme).foreground.opacity(0.6))
                    .lineSpacing(2)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            // ── RIGHT column ──
            // Image contained in its column; no mask, no scale.
            HStack {
                Spacer()
                Image("lexora-countryside")
                    .resizable()
                    .scaledToFit()
                    .frame(maxHeight: 200)
                    .accessibilityHidden(true)
            }
            .frame(maxWidth: .infinity)
            .contentShape(Rectangle())
            .onTapGesture {
                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                vm.openSearch()
            }
        }
        .padding(.horizontal, 16)
        .padding(.top, 8)
        .background(LexoraColors.background)
    }

    /// Headline "Discover the / World of Words [sprig]" — brand green serif.
    @ViewBuilder
    private var headline: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("Discover the")
            HStack(spacing: 4) {
                Text("World of")
                HStack(spacing: 4) {
                    Text("Words")
                    BotanicalSprig()
                        .frame(width: 20, height: 20)
                }
            }
        }
        .font(.lexoraDisplay(28, weight: .medium))
        .foregroundStyle(LexoraColors.primary)
        .tracking(-0.4)
        .lineSpacing(0)
        // Tight leading (1.05) — match web `leading-[1.05]`.
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

/// 3-leaf botanical sprig SVG — brand green #677554.
/// Faithful translation of the inline SVG from `hero.tsx`.
struct BotanicalSprig: View {
    var body: some View {
        // 40×40 logical viewBox; SwiftUI GeometryReader-free for clarity.
        ZStack {
            // Central stem
            stem
            // Lower-left leaf (rotated -35°)
            leaf
                .frame(width: 16, height: 6.4)
                .rotationEffect(.degrees(-35))
                .offset(x: -9, y: 14)
            // Upper-right leaf (rotated +35°)
            leaf
                .frame(width: 16, height: 6.4)
                .rotationEffect(.degrees(35))
                .offset(x: 9, y: -3)
            // Top crown leaf
            Capsule()
                .fill(LexoraColors.primary.opacity(0.9))
                .frame(width: 9, height: 5.6)
                .offset(x: 0, y: -13)
        }
        .frame(width: 40, height: 40)
    }

    private var stem: some View {
        Rectangle()
            .fill(LexoraColors.primary)
            .frame(width: 0.8, height: 32)
            .offset(y: 0)
    }

    private var leaf: some View {
        Capsule()
            .fill(LexoraColors.primary.opacity(0.85))
    }
}

#Preview {
    Hero()
        .environmentObject(LexoraViewModel())
        .background(LexoraColors.background)
}
