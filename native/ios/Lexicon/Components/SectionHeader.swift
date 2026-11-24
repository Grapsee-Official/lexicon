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
    var homeEditorial: Bool = false
    var homeInset: CGFloat? = nil
    @Environment(\.colorScheme) private var scheme
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    private var editorial: Bool { scheme == .dark && homeEditorial }
    private var compactType: Bool { editorial && !dynamicTypeSize.isAccessibilitySize }

    var body: some View {
        HStack(spacing: 8) {
            Text(title)
                .font(.lexiconDisplay(compactType ? 14.5 : 16.5, weight: .regular))
                .tracking(-0.2)
                .foregroundStyle(lexiconPalette(scheme).foreground)
                .lineLimit(dynamicTypeSize.isAccessibilitySize ? 2 : 1)
                .layoutPriority(1)
                .lexiconAccessibilityHeading(.h2)

            if divider {
                Rectangle()
                    .fill(editorial ? Color(hex: "B6A486").opacity(0.13) : lexiconPalette(scheme).border)
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
                .font(.lexiconBody(compactType ? 10.5 : 12, weight: .regular))
                .foregroundStyle(editorial ? Color(hex: "D0AD75") : lexiconPalette(scheme).primary)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityLabel(Text(actionLabel))
            .accessibilityHint(Text("View all items in this section"))
            .accessibilityAddTraits(.isButton)
        }
        .padding(.horizontal, homeInset ?? (editorial ? 18 : 16))
    }
}
