//
//  SectionHeader.swift
//  Lexicon
//

import SwiftUI

struct SectionHeader: View {
    let title: String
    var actionLabel: String = "View all"
    var divider: Bool = false
    var onAction: (() -> Void)? = nil
    @Environment(\.colorScheme) private var scheme

    var body: some View {
        HStack(spacing: 8) {
            Text(title)
                .font(.lexiconDisplay(16.5, weight: .regular))
                .tracking(-0.2)
                .foregroundStyle(lexiconPalette(scheme).foreground)
                .lineLimit(1)
                .layoutPriority(1)
                .lexiconAccessibilityHeading(.h2)

            if divider {
                Rectangle()
                    .fill(lexiconPalette(scheme).border)
                    .frame(height: 1)
                    .frame(maxWidth: .infinity)
            } else {
                Spacer(minLength: 4)
            }

            Button {
                UIImpactFeedbackGenerator(style: .light).impactOccurred()
                onAction?()
            } label: {
                HStack(spacing: 4) {
                    Text(actionLabel)
                    Image(systemName: "arrow.right")
                        .font(.system(size: 10, weight: .medium))
                }
                .font(.lexiconBody(11, weight: .regular))
                .foregroundStyle(lexiconPalette(scheme).highlight)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel(Text(actionLabel))
            .accessibilityHint(Text("View all items in this section"))
            .accessibilityAddTraits(.isButton)
        }
        .padding(.horizontal, 16)
    }
}
