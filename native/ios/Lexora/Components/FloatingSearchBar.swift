//
//  FloatingSearchBar.swift
//  Lexora
//
//  Translation of `src/components/lexora/floating-search-bar.tsx`.
//
//  Capsule shape: search icon + "Search for a word..." placeholder
//  + camera icon + mic icon. All thin strokes.
//

import SwiftUI

struct FloatingSearchBar: View {
    @EnvironmentObject private var vm: LexoraViewModel
    @Environment(\.colorScheme) private var scheme

    var placeholder: String = "Search for a word..."

    var body: some View {
        Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            vm.openSearch()
        } label: {
            HStack(spacing: 10) {
                Image(systemName: "magnifyingglass")
                    .font(.system(size: 14, weight: .regular))
                    .foregroundStyle(lexoraPalette(scheme).foreground.opacity(0.5))

                Text(placeholder)
                    .font(.lexoraBody(13))
                    .foregroundStyle(lexoraPalette(scheme).mutedForeground.opacity(0.7))
                    .frame(maxWidth: .infinity, alignment: .leading)

                Image(systemName: "camera")
                    .font(.system(size: 14, weight: .regular))
                    .foregroundStyle(lexoraPalette(scheme).foreground.opacity(0.4))

                Image(systemName: "mic")
                    .font(.system(size: 14, weight: .regular))
                    .foregroundStyle(lexoraPalette(scheme).foreground.opacity(0.4))
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(lexoraPalette(scheme).card)
            .clipShape(Capsule())
            .overlay(Capsule().stroke(lexoraPalette(scheme).border, lineWidth: 1))
            .shadow(color: Color.black.opacity(0.06), radius: 6, x: 0, y: 2)
        }
        .buttonStyle(.plain)
        .accessibilityLabel(Text("Open search"))
    }
}

#Preview {
    FloatingSearchBar()
        .environmentObject(LexoraViewModel())
        .padding()
        .background(LexoraColors.background)
}
