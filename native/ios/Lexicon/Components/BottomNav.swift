//
//  BottomNav.swift
//  Lexicon
//

import SwiftUI

struct BottomNav: View {
    @EnvironmentObject private var vm: LexiconViewModel
    @Environment(\.colorScheme) private var scheme

    private var isDark: Bool { scheme == .dark }

    private var activeColor: Color {
        isDark ? LexiconColors.accentDark : LexiconColors.primary
    }

    private var inactiveColor: Color {
        isDark ? LexiconColors.accentDark.opacity(0.78) : LexiconColors.foreground.opacity(0.88)
    }

    private var dotColor: Color {
        isDark ? LexiconColors.highlightDark : LexiconColors.primary
    }

    var body: some View {
        VStack {
            Spacer()
            navCapsule
                .padding(.horizontal, 16)
                .padding(.bottom, 2)
        }
    }

    private var navCapsule: some View {
        HStack(spacing: 0) {
            navItem(.home, label: "Home", symbol: .home)
            navItem(.dictionary, label: "Dictionary", symbol: .dictionary)

            Button {
                UIImpactFeedbackGenerator(style: .medium).impactOccurred()
                vm.openSearch()
            } label: {
                HeritageNavIcon(symbol: .search, size: 24, color: isDark ? LexiconColors.highlightDark : LexiconColors.primaryForeground)
                    .frame(width: 54, height: 54)
                    .background(isDark ? Color(hex: "101620") : LexiconColors.primary)
                    .clipShape(Circle())
                    .overlay(
                        Circle()
                            .stroke(
                                isDark ? LexiconColors.accentDark.opacity(0.70) : lexiconPalette(scheme).card,
                                lineWidth: isDark ? 1.5 : 3
                            )
                    )
            }
            .buttonStyle(.plain)
            .shadow(color: isDark ? .clear : LexiconColors.foreground.opacity(0.16), radius: 6, y: 2)
            .offset(y: -10)
            .accessibilityLabel(Text(LexiconAccessibilityLabels.search))
            .accessibilityAddTraits(.isButton)
            .frame(maxWidth: .infinity)

            navItem(.collections, label: "Collections", symbol: .collections)
            navItem(.more, label: "More", symbol: .more)
        }
        .padding(.horizontal, 8)
        .padding(.vertical, 7)
        .background {
            Capsule()
                .fill(lexiconPalette(scheme).card.opacity(0.96))
                .overlay(Capsule().stroke(lexiconPalette(scheme).border, lineWidth: 1))
        }
        .shadow(
            color: isDark ? .clear : LexiconColors.foreground.opacity(0.12),
            radius: 8,
            x: 0,
            y: 3
        )
    }

    @ViewBuilder
    private func navItem(_ tab: LexiconTab, label: String, symbol: HeritageNavSymbol) -> some View {
        let active = vm.activeTab == tab
        Button {
            UIImpactFeedbackGenerator(style: .light).impactOccurred()
            vm.setActiveTab(tab)
        } label: {
            VStack(spacing: 2) {
                HeritageNavIcon(symbol: symbol, size: 19, color: active ? activeColor : inactiveColor)

                Text(label)
                    .font(.lexiconBody(9.5, weight: active ? .medium : .regular))
                    .foregroundStyle(active ? activeColor : inactiveColor)
                    .lineLimit(1)
                    .minimumScaleFactor(0.75)

                Circle()
                    .fill(active ? dotColor : .clear)
                    .frame(width: 4, height: 4)
                    .lexiconDecorative()
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 2)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .ignore)
        .accessibilityLabel(Text(label))
        .accessibilityHint(Text("Switch to \(label) tab"))
        .accessibilityAddTraits(active ? [.isButton, .isSelected] : .isButton)
    }
}


// MARK: - Lexora heritage navigation icon family
// Canonical 24×24 paths match public/icons/heritage/*.svg and Android vectors.
// No bitmap scaling or SF Symbols; ink color follows the existing theme.
private enum HeritageNavSymbol {
    case home, dictionary, search, collections, more
}

private struct HeritageNavIcon: View {
    let symbol: HeritageNavSymbol
    let size: CGFloat
    let color: Color

    var body: some View {
        HeritageNavShape(symbol: symbol)
            .stroke(color, style: StrokeStyle(lineWidth: 1.65 * size / 24, lineCap: .round, lineJoin: .round))
            .frame(width: size, height: size)
            .accessibilityHidden(true)
    }
}

private struct HeritageNavShape: Shape {
    let symbol: HeritageNavSymbol

    func path(in rect: CGRect) -> Path {
        var path = Path()
        func point(_ x: CGFloat, _ y: CGFloat) -> CGPoint {
            CGPoint(x: rect.minX + rect.width * x / 24, y: rect.minY + rect.height * y / 24)
        }

        switch symbol {
        case .home:
            path.move(to: point(3, 11))
            path.addLine(to: point(12, 3.8))
            path.addLine(to: point(21, 11))
            path.move(to: point(5.3, 9.8))
            path.addLine(to: point(5.3, 20.2))
            path.addQuadCurve(to: point(6.1, 21), control: point(5.3, 21))
            path.addLine(to: point(17.9, 21))
            path.addQuadCurve(to: point(18.7, 20.2), control: point(18.7, 21))
            path.addLine(to: point(18.7, 9.8))
            path.move(to: point(9.6, 21))
            path.addLine(to: point(9.6, 14.5))
            path.addQuadCurve(to: point(10.4, 13.7), control: point(9.6, 13.7))
            path.addLine(to: point(13.6, 13.7))
            path.addQuadCurve(to: point(14.4, 14.5), control: point(14.4, 13.7))
            path.addLine(to: point(14.4, 21))
            path.move(to: point(16, 6))
            path.addLine(to: point(16, 3.8))
            path.addLine(to: point(18.2, 3.8))
            path.addLine(to: point(18.2, 7.7))
            path.move(to: point(7, 12))
            path.addLine(to: point(8.7, 12))
            path.addLine(to: point(8.7, 14))
            path.addLine(to: point(7, 14))
            path.closeSubpath()
            path.move(to: point(15.4, 12))
            path.addLine(to: point(17.1, 12))
            path.addLine(to: point(17.1, 14))
            path.addLine(to: point(15.4, 14))
            path.closeSubpath()
        case .dictionary:
            path.move(to: point(12, 5.2))
            path.addQuadCurve(to: point(3.2, 4.8), control: point(8.3, 3.2))
            path.addLine(to: point(3.2, 18.5))
            path.addQuadCurve(to: point(12, 19.7), control: point(8, 17.1))
            path.addQuadCurve(to: point(20.8, 18.5), control: point(16, 17.1))
            path.addLine(to: point(20.8, 4.8))
            path.addQuadCurve(to: point(12, 5.2), control: point(15.7, 3.2))
            path.closeSubpath()
            path.move(to: point(12, 5.2))
            path.addLine(to: point(12, 19.7))
            path.move(to: point(5.5, 8.5))
            path.addQuadCurve(to: point(10, 8.6), control: point(8, 7.6))
            path.move(to: point(14, 8.6))
            path.addQuadCurve(to: point(18.5, 8.5), control: point(16, 7.6))
            path.move(to: point(5.7, 11.7))
            path.addQuadCurve(to: point(10, 12), control: point(8.2, 11))
            path.move(to: point(14, 12))
            path.addQuadCurve(to: point(18.3, 11.7), control: point(15.8, 11))
            path.move(to: point(5.8, 15.2))
            path.addQuadCurve(to: point(10, 15.6), control: point(8.5, 14.5))
            path.move(to: point(14, 15.6))
            path.addQuadCurve(to: point(18.2, 15.2), control: point(15.5, 14.5))
        case .search:
            path.move(to: point(16.8, 10.2))
            path.addCurve(to: point(10.3, 16.8), control1: point(16.8, 13.8), control2: point(13.9, 16.8))
            path.addCurve(to: point(3.8, 10.2), control1: point(6.7, 16.8), control2: point(3.8, 13.8))
            path.addCurve(to: point(10.3, 3.6), control1: point(3.8, 6.6), control2: point(6.7, 3.6))
            path.addCurve(to: point(16.8, 10.2), control1: point(13.9, 3.6), control2: point(16.8, 6.6))
            path.closeSubpath()
            path.move(to: point(14.9, 14.9))
            path.addLine(to: point(20.5, 20.5))
            path.move(to: point(7.1, 7.8))
            path.addQuadCurve(to: point(10.4, 6.3), control: point(8.5, 6.3))
            path.move(to: point(18.4, 3.1))
            path.addLine(to: point(18.8, 4.4))
            path.addLine(to: point(20.1, 4.8))
            path.addLine(to: point(18.8, 5.2))
            path.addLine(to: point(18.4, 6.5))
            path.addLine(to: point(18, 5.2))
            path.addLine(to: point(16.7, 4.8))
            path.addLine(to: point(18, 4.4))
            path.closeSubpath()
        case .collections:
            path.move(to: point(6.3, 3.1))
            path.addLine(to: point(17.7, 3.1))
            path.addQuadCurve(to: point(18.5, 3.9), control: point(18.5, 3.1))
            path.addLine(to: point(18.5, 20.8))
            path.addLine(to: point(12, 16.3))
            path.addLine(to: point(5.5, 20.8))
            path.addLine(to: point(5.5, 3.9))
            path.addQuadCurve(to: point(6.3, 3.1), control: point(5.5, 3.1))
            path.closeSubpath()
            path.move(to: point(8.2, 6.6))
            path.addLine(to: point(15.8, 6.6))
            path.move(to: point(12, 8.6))
            path.addLine(to: point(12, 14.3))
            path.move(to: point(12, 12.2))
            path.addQuadCurve(to: point(9.6, 9.3), control: point(9.6, 11.1))
            path.addQuadCurve(to: point(12, 11.2), control: point(11.8, 9.4))
            path.move(to: point(12, 12.2))
            path.addQuadCurve(to: point(14.4, 9.3), control: point(14.4, 11.1))
            path.addQuadCurve(to: point(12, 11.2), control: point(12.2, 9.4))
            path.move(to: point(10.1, 14.2))
            path.addQuadCurve(to: point(13.9, 14.2), control: point(12, 14.8))
        case .more:
            // Four-pane leaded window: Appearance, Dictionary, Privacy & Data, About.
            path.move(to: point(4.5, 20.2))
            path.addLine(to: point(4.5, 9.2))
            path.addQuadCurve(to: point(12, 3.5), control: point(4.5, 5))
            path.addQuadCurve(to: point(19.5, 9.2), control: point(19.5, 5))
            path.addLine(to: point(19.5, 20.2))
            path.closeSubpath()
            path.move(to: point(12, 4))
            path.addLine(to: point(12, 20.2))
            path.move(to: point(4.5, 12.4))
            path.addLine(to: point(19.5, 12.4))
            path.move(to: point(3.4, 21.1))
            path.addLine(to: point(20.6, 21.1))

        }

        return path
    }
}
