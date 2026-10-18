//
//  Hero.swift
//  Lexicon
//
//  Translation of `src/components/lexicon/hero.tsx`.
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
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        HStack(alignment: .center, spacing: 8) {
            // ── LEFT column ──
            VStack(alignment: .leading, spacing: 8) {
                headline
                Text("A timeless dictionary crafted for curious minds.")
                    .font(.lexiconBody(11))
                    .foregroundStyle(lexiconPalette(scheme).foreground.opacity(0.6))
                    .lineSpacing(2)
            }
            .frame(maxWidth: .infinity, alignment: .leading)

            // ── RIGHT column ──
            // Image contained in its column; no mask, no scale.
            // P4-05 — the tap target opens search, so it needs a VoiceOver
            // label + `.isButton` trait + 44pt minimum hit area. The image
            // itself is decorative (the label conveys the action).
            HStack {
                Spacer()
                Image("lexicon-countryside")
                    .resizable()
                    .scaledToFit()
                    .frame(maxHeight: 200)
                    .lexiconDecorative()
            }
            .frame(maxWidth: .infinity)
            .contentShape(Rectangle())
            .onTapGesture {
                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                vm.openSearch()
            }
            .accessibilityLabel(Text(LexiconAccessibilityLabels.search))
            .accessibilityHint(Text("Opens the search overlay"))
            .accessibilityAddTraits(.isButton)
            .lexiconMinTouchTarget()
        }
        .padding(.horizontal, 16)
        .padding(.top, 8)
        .background(LexiconColors.background)
    }

    /// Headline "Discover the / World of Words [sprig]" — brand green serif.
    /// Marked as the h1 heading so VoiceOver users land on the hero
    /// headline first when the home screen appears.
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
                        // Decorative sprig — the headline text already
                        // conveys the meaning; the sprig is ornamental.
                        .lexiconDecorative()
                }
            }
        }
        .font(.lexiconDisplay(28, weight: .medium))
        .foregroundStyle(LexiconColors.primary)
        .tracking(-0.4)
        .lineSpacing(0)
        // Tight leading (1.05) — match web `leading-[1.05]`.
        .frame(maxWidth: .infinity, alignment: .leading)
        .lexiconCombinedElement()
        .accessibilityLabel(Text("Discover the World of Words"))
        .lexiconAccessibilityHeading(.h1)
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
                .fill(LexiconColors.primary.opacity(0.9))
                .frame(width: 9, height: 5.6)
                .offset(x: 0, y: -13)
        }
        .frame(width: 40, height: 40)
    }

    private var stem: some View {
        Rectangle()
            .fill(LexiconColors.primary)
            .frame(width: 0.8, height: 32)
            .offset(y: 0)
    }

    private var leaf: some View {
        Capsule()
            .fill(LexiconColors.primary.opacity(0.85))
    }
}

#Preview {
    Hero()
        .environmentObject(LexiconViewModel())
        .background(LexiconColors.background)
}
