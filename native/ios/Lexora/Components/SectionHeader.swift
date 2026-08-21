//
//  SectionHeader.swift
//  Lexora
//
//  Translation of `src/components/lexora/section-header.tsx`.
//
//  Title left + "View all →" right (highlight color). NO divider line.
//

import SwiftUI

struct SectionHeader: View {
    let title: String
    var actionLabel: String = "View all"
    var onAction: (() -> Void)? = nil
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        HStack(spacing: 8) {
            Text(title)
                .font(.lexoraDisplay(15, weight: .semibold))
                .tracking(-0.2)
                .foregroundStyle(lexoraPalette(scheme).foreground)
                .lineLimit(1)
                .layoutPriority(1)
                // Mark as h2 heading so VoiceOver users can navigate
                // between sections (Explore by Category, Trending Now,
                // Recent Articles, etc.) via the Headings rotor.
                .lexoraAccessibilityHeading(.h2)

            Spacer(minLength: 4)

            Button {
                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                onAction?()
            } label: {
                HStack(spacing: 4) {
                    Text(actionLabel)
                    Image(systemName: "arrow.right")
                        .font(.system(size: 11, weight: .semibold))
                }
                .font(.lexoraBody(11, weight: .medium))
                .foregroundStyle(lexoraPalette(scheme).highlight)
                // The text-only "View all →" button is ~16pt tall — well
                // below the 44pt minimum. Expand the hit area without
                // resizing the visual label.
                .lexoraMinTouchTarget()
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel(Text(actionLabel))
            .accessibilityHint(Text("View all items in this section"))
            .accessibilityAddTraits(.isButton)
        }
    }
}

#Preview {
    SectionHeader(title: "Explore by Category", actionLabel: "View all")
        .padding()
        .background(LexoraColors.background)
}
