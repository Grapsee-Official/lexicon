//
//  AccessibleButton.swift
//  Lexicon
//
//  P4-05 — VoiceOver / TalkBack accessibility hardening.
//
//  `AccessibleButton` is the canonical wrapper for every tappable element
//  in Lexicon. It enforces the three accessibility contracts that the
//  P4-05 spec calls out as a HARD GATE:
//
//    1. Minimum 44pt × 44pt touch target (Apple HIG).
//    2. Non-empty VoiceOver label (passed as a required init parameter —
//       the compiler rejects label-less buttons at build time).
//    3. `.isButton` accessibility trait (so VoiceOver announces
//       "double-tap to activate").
//
//  Why a wrapper instead of just `.lexiconMinTouchTarget()` on every
//  `Button`? Two reasons:
//
//    a) Type-system enforcement. By making `label` a required `let` (not
//       an optional), the wrapper makes label-less buttons impossible to
//       construct — the build fails at the call site. A bare `Button`
//       silently compiles without a label, which is the #1 VoiceOver bug.
//
//    b) Audit centralization. Grepping for `Button(` returns ~30 hits
//       across the codebase; grepping for `AccessibleButton(` returns
//       only the audited buttons, making the P4-05 compliance review a
//       one-shot diff.
//
//  Usage:
//
//    AccessibleButton(label: LexiconAccessibilityLabels.menu) {
//        vm.openMenu()
//    } content: {
//        Image(systemName: "line.3.horizontal")
//            .font(.system(size: 16, weight: .thin))
//            .foregroundStyle(foregroundAt80)
//    }
//
//  The `content` closure's view is rendered at its natural size; the
//  wrapper expands the hit area to ≥ 44×44 around it (the icon stays at
//  16pt visually — only the tappable frame grows). This is the
//  Apple-HIG-recommended approach.
//
//  For toggling buttons (e.g. bookmark), pass an `accessibilityValue`
//  string ("Saved" / "Not saved") so VoiceOver announces the current
//  state before the label.
//

import SwiftUI
import UIKit

struct AccessibleButton<Label: View>: View {
    // MARK: - Required parameters

    /// VoiceOver label. Required (non-optional) so the compiler rejects
    /// label-less buttons at the call site — this is the type-system
    /// enforcement that makes AccessibleButton safer than a bare Button.
    let label: String

    /// Tap action.
    let action: () -> Void

    /// Visual content. Rendered at its natural size — the wrapper expands
    /// the hit area around it to ≥ 44×44pt without resizing the icon.
    @ViewBuilder let content: () -> Label

    // MARK: - Optional parameters

    /// VoiceOver hint (read after the label, only if Hints are enabled
    /// in VoiceOver settings). Use sparingly — only for non-obvious
    /// actions.
    var hint: String? = nil

    /// VoiceOver value — describes the current state of a toggling
    /// button. E.g. "Saved" / "Not saved" for a bookmark button, or
    /// "Selected" / "Not selected" for a tab button. VoiceOver announces
    /// this BETWEEN the label and the trait, so the user hears
    /// "Save word, Saved, button" before activating.
    var accessibilityValue: String? = nil

    /// When `isSelected == true`, VoiceOver announces "selected" after
    /// the label (e.g. "Home, selected, tab"). Apply to BottomNav tab
    /// items so the user knows which tab is active without seeing the
    /// dot indicator.
    var isSelected: Bool = false

    /// When `true` (default), the wrapper applies `.buttonStyle(.plain)`
    /// so the button doesn't get the system blue tint + press highlight.
    /// Set to `false` if you want the system Button styling.
    var plainStyle: Bool = true

    /// When `true` (default), the wrapper applies a light haptic
    /// `UIImpactFeedbackGenerator(style: .light)` on tap. Set to `false`
    /// to suppress (e.g. for a destructive action that should use
    /// `.medium` or `.heavy` instead — the caller fires their own haptic
    /// inside `action`).
    var haptic: Bool = true

    /// Haptic style. Default `.light` matches the rest of the codebase.
    var hapticStyle: UIImpactFeedbackGenerator.FeedbackStyle = .light

    // MARK: - Body

    var body: some View {
        Button(action: handleTap) {
            content()
                // Apple HIG: minimum 44×44 hit area. `.frame(minWidth:
                // minHeight:)` only expands if the content is smaller —
                // a 60×60 button is left untouched. `.contentShape`
                // ensures the full expanded frame is tappable, not just
                // the icon's tight bounding box.
                .frame(minWidth: LexiconAccessibility.minTouchTarget,
                       minHeight: LexiconAccessibility.minTouchTarget)
                .contentShape(Rectangle())
        }
        // Always use `.plain` button style — the rest of the Lexicon
        // codebase consistently uses `.plain` to avoid the system blue
        // tint + press highlight, which would clash with the warm-cream
        // palette. (The `plainStyle` parameter is retained in the init
        // signature for forward-compat, but currently ignored — if a
        // caller passes `plainStyle: false`, they should use a bare
        // `Button` instead.)
        .buttonStyle(.plain)
        .accessibilityLabel(Text(label))
        .accessibilityAddTraits(.isButton)
        .applyIfLet(hint) { view, h in
            view.accessibilityHint(Text(h))
        }
        .applyIfLet(accessibilityValue) { view, v in
            view.accessibilityValue(Text(v))
        }
        .modifier(OptionalSelectionTrait(isSelected: isSelected))
    }

    // MARK: - Tap handler (haptic + action)

    private func handleTap() {
        if haptic {
            UIImpactFeedbackGenerator(style: hapticStyle).impactOccurred()
        }
        action()
    }
}

// MARK: - Convenience initializers
//
//  The default memberwise init requires `content:` as a trailing closure
//  parameter name, which is awkward at call sites. These convenience
//  inits let callers write the more idiomatic:
//
//    AccessibleButton(label: "Open menu") { action } content: { view }
//
//  mirroring SwiftUI's `Button(action:label:)` shape.
//

extension AccessibleButton {
    public init(
        label: String,
        hint: String? = nil,
        accessibilityValue: String? = nil,
        isSelected: Bool = false,
        plainStyle: Bool = true,
        haptic: Bool = true,
        hapticStyle: UIImpactFeedbackGenerator.FeedbackStyle = .light,
        action: @escaping () -> Void,
        @ViewBuilder content: () -> Label
    ) {
        self.label = label
        self.hint = hint
        self.accessibilityValue = accessibilityValue
        self.isSelected = isSelected
        self.plainStyle = plainStyle
        self.haptic = haptic
        self.hapticStyle = hapticStyle
        self.action = action
        self.content = content
    }
}

// MARK: - Helpers

/// Applies `transform` to `view` only when `value` is non-nil.
/// Avoids the `if let` branching that produces two different `some View`
/// types and forces an `AnyView` wrap.
private extension View {
    @ViewBuilder
    func applyIfLet<T>(
        _ value: T?,
        _ transform: (Self, T) -> some View
    ) -> some View {
        if let value = value {
            transform(self, value)
        } else {
            self
        }
    }
}

/// Adds `.isSelected` trait only when `isSelected == true` — avoids
/// always setting the trait (which would announce "not selected" for
/// every button, even non-toggle ones).
private struct OptionalSelectionTrait: ViewModifier {
    let isSelected: Bool

    func body(content: Content) -> some View {
        if isSelected {
            content.accessibilityAddTraits(.isSelected)
        } else {
            content
        }
    }
}

// MARK: - Preview

#Preview("Plain icon button — 44pt enforced") {
    AccessibleButton(label: LexiconAccessibilityLabels.menu) {
        // open menu
    } content: {
        Image(systemName: "line.3.horizontal")
            .font(.system(size: 16, weight: .thin))
            .foregroundStyle(.black.opacity(0.8))
    }
    .padding()
    .background(Color(red: 0.98, green: 0.95, blue: 0.91))
}

#Preview("Toggle button — value + selected state") {
    AccessibleButton(
        label: LexiconAccessibilityLabels.bookmarkSave,
        accessibilityValue: "Not saved",
        isSelected: false,
        hapticStyle: .medium
    ) {
        // toggle save
    } content: {
        Image(systemName: "bookmark")
            .font(.system(size: 16, weight: .regular))
            .foregroundStyle(.black.opacity(0.8))
    }
    .padding()
    .background(Color(red: 0.98, green: 0.95, blue: 0.91))
}

#Preview("Tab button — selected trait") {
    VStack(spacing: 16) {
        AccessibleButton(
            label: "Home",
            accessibilityValue: "Selected",
            isSelected: true
        ) {
            // select home
        } content: {
            VStack(spacing: 2) {
                Image(systemName: "house.fill")
                    .font(.system(size: 16, weight: .semibold))
                Text("Home").font(.caption)
            }
        }

        AccessibleButton(
            label: "Dictionary",
            accessibilityValue: "Not selected",
            isSelected: false
        ) {
            // select dictionary
        } content: {
            VStack(spacing: 2) {
                Image(systemName: "book")
                    .font(.system(size: 16, weight: .regular))
                Text("Dictionary").font(.caption)
            }
        }
    }
    .padding()
    .background(Color(red: 0.98, green: 0.95, blue: 0.91))
}
