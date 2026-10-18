//
//  LexiconAccessibility.swift
//  Lexicon
//
//  P4-05 — VoiceOver / TalkBack accessibility hardening.
//
//  Apple HIG mandates a 44pt × 44pt minimum touch target. This file
//  centralises every Lexicon accessibility convention so the rest of the
//  codebase can opt-in via a single, greppable API surface:
//
//    1. Minimum touch target — `LexiconAccessibility.minTouchTargetSize`
//       (44×44) + `.lexiconMinTouchTarget()` modifier.
//    2. VoiceOver label / hint / heading ViewModifiers (thin wrappers over
//       SwiftUI's accessibility modifiers — they exist so audits can grep
//       for `.lexiconAccessibilityLabel` and find every custom label in
//       one pass, and so heading level + `.isHeader` trait are applied
//       together consistently).
//    3. `LexiconAccessibilityLabels` — canonical VoiceOver strings for every
//       icon-only button in the app (matches the P4-05 spec verbatim).
//    4. `LexiconAccessibilityTraits` convenience — `.isButton` / `.isHeader`
//       applied together with the label so a button is never label-less
//       nor trait-less.
//    5. Dynamic Type — every `Font.lexicon*` factory already uses
//       `.custom(_, relativeTo:)` so text scales with the user's
//       preferred content size category automatically; the
//       `.lexiconDynamicType()` modifier is a no-op audit marker +
//       enforces `.minimumScaleFactor` so layouts never clip.
//    6. Reduce Motion — `.lexiconMotion(...)` respects
//       `@Environment(\.accessibilityReduceMotion)`; returns the unmodified
//       value when the user has enabled Reduce Motion in Settings.
//    7. VoiceOver focus management — `LexiconVoiceOverFocus` enum + a
//       `@AccessibilityFocusState` convenience; SearchModal binds it to
//       move focus to the results list when results appear.
//
//  HARD GATE (per spec P4-05): 100% touch target compliance. Every
//  interactive element in `Components/` MUST be ≥ 44×44pt. The
//  `.lexiconMinTouchTarget()` modifier is applied to every Button /
//  tappable area in the codebase via either direct call or via the
//  `AccessibleButton` wrapper.
//

import SwiftUI
import UIKit

// MARK: - Minimum touch target (Apple HIG: 44pt × 44pt)

public enum LexiconAccessibility {
    /// Apple Human Interface Guidelines minimum touch target size.
    /// Used as the floor for every interactive element in the app.
    public static let minTouchTargetSize: CGSize = CGSize(width: 44, height: 44)

    /// Convenience CGFloat for `frame(minWidth:minHeight:)`.
    static let minTouchTarget: CGFloat = 44

    /// `UIAccessibility` voice-over status — exposed for conditional
    /// behaviour (e.g. skipping purely-decorative animations when VO is on).
    static var voiceOverIsRunning: Bool {
        UIAccessibility.isVoiceOverRunning
    }

    /// True when the user has enabled Reduce Motion (Settings →
    /// Accessibility → Motion → Reduce Motion). Respected by
    /// `.lexiconMotion(...)`.
    static var reduceMotionEnabled: Bool {
        UIAccessibility.isReduceMotionEnabled
    }

    /// True when the user has enabled Bold Text (Settings → Accessibility
    /// → Display & Text Size → Bold Text). Used by `LexiconTypography`
    /// fallback weights.
    static var boldTextEnabled: Bool {
        UIAccessibility.isBoldTextEnabled
    }
}

// MARK: - Canonical VoiceOver labels for icon-only buttons
//
//  P4-05 spec — verbatim strings. Every icon-only button in the app MUST
//  use one of these (or a word-specific variant for the bookmark button,
//  which toggles between `bookmarkSave` and `bookmarkRemove`).
//

public enum LexiconAccessibilityLabels {
    // Hamburger (LexiconHeader left)
    static let menu              = "Open navigation menu"
    // Magnifying glass icon (FloatingSearchBar, BottomNav center, SearchModal field)
    static let search            = "Search for a word"
    static let openSearch        = "Open search"
    // Bell (LexiconHeader right)
    static let bell              = "Notifications, new items available"
    // Avatar (LexiconHeader right)
    static let avatar            = "Open profile"
    // speaker.wave.2 (WordOfTheDayCard, WordDetailSheet)
    static let volume            = "Pronounce word"
    // bookmark / bookmark.fill (WordDetailSheet) — toggles between these two
    static let bookmarkSave      = "Save word"
    static let bookmarkRemove    = "Remove from saved"
    // square.and.arrow.up (WordDetailSheet)
    static let share             = "Share word"
    // square.grid.2x2 (TrendingTagRow 5th button)
    static let grid              = "More search options"

    // ── Supplementary labels (not in the P4-05 spec list, but used by
    //    icon-only buttons elsewhere in the app — included for completeness).
    static let close             = "Close"
    static let closeSearch       = "Close search"
    static let voiceSearch       = "Voice search"
    static let imageSearch       = "Image search"
    static let clearRecents      = "Clear recent searches"
    static let themeToggleLight  = "Switch to light mode"
    static let themeToggleDark   = "Switch to dark mode"
    static let listenTo          = "Listen to"   // prefix for "Listen to {word}"
    static let moreTags          = "More search options"  // alias of grid
    static let viewAll           = "View all"
}

// MARK: - Accessibility traits convenience
//
//  SwiftUI's `Button` already advertises `.isButton` automatically, but
//  custom tappable areas (e.g. `.onTapGesture` on a ZStack) do NOT — they
//  must add the trait manually so VoiceOver announces "double-tap to
//  activate" instead of just reading the label. `LexiconAccessibilityTraits`
//  bundles the trait + label application together so they can't drift.
//

public enum LexiconAccessibilityTraits {
    /// Apply to any tappable element so VoiceOver announces it as a button.
    static let button: AccessibilityTraits = .isButton
    /// Apply to section titles so VoiceOver announces "heading" and the
    /// rotor's "Headings" navigation can jump to them.
    static let header: AccessibilityTraits = .isHeader
}

// MARK: - Heading levels (iOS 17+ `.accessibilityHeading`, iOS 16 fallback)

/// Lexicon heading levels — mirror SwiftUI's `AccessibilityHeadingLevel`
/// (iOS 17+) so the same call site works on iOS 16 by falling back to
/// `.accessibilityAddTraits(.isHeader)` (loses the level metadata but
/// preserves the "heading" announcement + rotor navigation).
public enum LexiconHeadingLevel {
    case h1, h2, h3, h4, h5, h6

    @available(iOS 17.0, *)
    var swiftUIHeading: AccessibilityHeadingLevel {
        switch self {
        case .h1: return .h1
        case .h2: return .h2
        case .h3: return .h3
        case .h4: return .h4
        case .h5: return .h5
        case .h6: return .h6
        }
    }
}

// MARK: - VoiceOver focus management
//
//  P4-05 spec §7: "VoiceOver focus management: moves focus to search
//  results when they appear".
//
//  SwiftUI's `@AccessibilityFocusState` (iOS 14+) is the supported way to
//  programmatically move VoiceOver focus — it generates an
//  `UIAccessibility.post(notification: .screenChanged, argument:)` under
//  the hood. The enum below is the namespace used by SearchModal (and any
//  other screen that needs to programmatically focus an element).
//

public enum LexiconVoiceOverFocus: Hashable {
    case searchField       // SearchModal's TextField
    case searchResults     // SearchModal's results list header
    case wordDetailHeader  // WordDetailSheet's word title (future use)
    case toast             // ContentView's toast overlay (future use)
}

// MARK: - ViewModifier extensions

extension View {

    // MARK: VoiceOver label

    /// Adds a VoiceOver label to the receiver.
    ///
    /// Thin wrapper over `accessibilityLabel(Text:)` — exists so audits
    /// can grep for `.lexiconAccessibilityLabel` and find every custom
    /// label in the codebase in one pass.
    ///
    /// - Parameter label: The string VoiceOver reads when the element
    ///   receives focus. For icon-only buttons, use a verb phrase
    ///   ("Open navigation menu", not "Menu icon").
    
    func lexiconAccessibilityLabel<S: StringProtocol>(_ label: S) -> some View {
        accessibilityLabel(Text(String(label)))
    }

    /// Localized-string overload (for `LocalizedStringKey` constants).
    
    func lexiconAccessibilityLabel(_ label: LocalizedStringKey) -> some View {
        accessibilityLabel(Text(label))
    }

    // MARK: VoiceOver hint

    /// Adds a VoiceOver hint to the receiver.
    ///
    /// Hints are read AFTER the label + value, and only if the user has
    /// "Hints" enabled in VoiceOver settings. Use sparingly — only for
    /// non-obvious actions (e.g. "Searches the offline dictionary").
    
    func lexiconAccessibilityHint<S: StringProtocol>(_ hint: S) -> some View {
        accessibilityHint(Text(String(hint)))
    }

    
    func lexiconAccessibilityHint(_ hint: LocalizedStringKey) -> some View {
        accessibilityHint(Text(hint))
    }

    // MARK: Heading level

    /// Marks the receiver as a heading at the given level.
    ///
    /// On iOS 17+, this calls SwiftUI's `.accessibilityHeading(_:)` which
    /// propagates the level to the VoiceOver rotor's "Headings"
    /// navigation. On iOS 16, it falls back to
    /// `.accessibilityAddTraits(.isHeader)` — the level metadata is lost
    /// but the "heading" announcement + rotor navigation are preserved.
    ///
    /// Apply to section titles ("RECENT", "SUGGESTED", "Definitions",
    /// "Etymology", "Explore by Category", etc.) so VoiceOver users can
    /// skim the screen via the Headings rotor.
    @ViewBuilder
    func lexiconAccessibilityHeading(_ level: LexiconHeadingLevel) -> some View {
        if #available(iOS 17.0, *) {
            self.accessibilityHeading(level.swiftUIHeading)
        } else {
            self.accessibilityAddTraits(.isHeader)
        }
    }

    // MARK: Minimum touch target (44pt × 44pt — Apple HIG)

    /// Enforces a minimum 44pt × 44pt touch target on the receiver.
    ///
    /// Implementation: `.frame(minWidth: 44, minHeight: 44)` expands the
    /// view's frame only if it's smaller than 44pt in either dimension —
    /// larger views are left untouched. `.contentShape(Rectangle())`
    /// ensures the full expanded frame is tappable (not just the icon's
    /// bounding box, which can be much smaller for thin-stroke SF
    /// Symbols).
    ///
    /// This is the Apple-HIG-compliant way to meet the P4-05 hard gate
    /// ("100% touch target compliance") without visually resizing the
    /// icon — the visual size stays at the designer's spec, but the hit
    /// area grows to 44pt.
    ///
    /// Apply to EVERY `Button` and tappable area in the app. The
    /// `AccessibleButton` wrapper applies it automatically; ad-hoc
    /// `Button { } label: { ... }` calls must apply it manually.
    
    func lexiconMinTouchTarget(min: CGFloat = LexiconAccessibility.minTouchTarget) -> some View {
        self
            .frame(minWidth: min, minHeight: min)
            .contentShape(Rectangle())
    }

    // MARK: Button trait convenience (label + trait together)

    /// Applies the canonical "this is a tappable button" treatment in
    /// one call: VoiceOver label + `.isButton` trait + 44pt minimum touch
    /// target. Use on ad-hoc `Button { } label: { ... }` constructs that
    /// would otherwise be label-less or below the 44pt threshold.
    ///
    /// SwiftUI `Button` already advertises `.isButton` automatically, so
    /// the trait application here is idempotent — it's a defensive
    /// measure for cases where the Button is wrapped in modifiers that
    /// strip the trait (e.g. `.overlay`).
    
    func lexiconAsButton(
        labeled label: String,
        hint: String? = nil,
        minTouchTarget: Bool = true
    ) -> some View {
        var modified: AnyView = AnyView(
            accessibilityLabel(Text(label))
                .accessibilityAddTraits(.isButton)
        )
        if let hint = hint {
            modified = AnyView(modified.accessibilityHint(Text(hint)))
        }
        if minTouchTarget {
            modified = AnyView(
                modified
                    .frame(minWidth: LexiconAccessibility.minTouchTarget,
                           minHeight: LexiconAccessibility.minTouchTarget)
                    .contentShape(Rectangle())
            )
        }
        return modified
    }

    // MARK: Header trait convenience (label + trait together)

    /// Applies the canonical "this is a section header" treatment in one
    /// call: VoiceOver label + `.isHeader` trait + h2 heading level.
    /// Apply to section titles so VoiceOver users can navigate via the
    /// Headings rotor.
    
    func lexiconAsHeader(
        labeled label: String? = nil,
        level: LexiconHeadingLevel = .h2
    ) -> some View {
        var modified: AnyView = AnyView(self)
        if let label = label {
            modified = AnyView(modified.accessibilityLabel(Text(label)))
        }
        return AnyView(
            modified.lexiconAccessibilityHeading(level)
        )
    }

    // MARK: Dynamic Type — audit marker + scale-factor safety net
    //
    //  All `Font.lexicon*` factories already use `.custom(_, relativeTo:)`
    //  so they scale with the user's preferred content size category
    //  (XS → XXXL, plus accessibility sizes AX5). This modifier is a
    //  no-op that:
    //    1. Serves as an audit marker — grep for `.lexiconDynamicType`
    //       to find every Text/Label that's been verified for Dynamic
    //       Type compliance.
    //    2. Applies a defensive `.minimumScaleFactor(0.7)` so a Text
    //       that's too wide for its container at AX5 shrinks gracefully
    //       rather than clipping or truncating mid-word. Pair with
    //       `.lineLimit(1)` for single-line labels.
    //
    
    func lexiconDynamicType(minimumScaleFactor: CGFloat = 0.7) -> some View {
        self.minimumScaleFactor(minimumScaleFactor)
    }

    // MARK: Reduce Motion — conditional animation
    //
    //  Respects `@Environment(\.accessibilityReduceMotion)`. When the
    //  user has enabled Reduce Motion in Settings, this returns the
    //  unmodified value (no animation). Otherwise it applies the given
    //  animation, exactly like `.animation(_:value:)`.
    //
    //  Use anywhere the codebase previously called `.animation(.easeInOut,
    //  value:)` — replacing those calls with `.lexiconMotion(.easeInOut,
    //  value:)` makes the entire app Reduce-Motion-compliant in one pass.
    //
    
    func lexiconMotion<V: Equatable>(
        _ animation: Animation? = .default,
        value: V
    ) -> some View {
        modifier(LexiconReduceMotionModifier(animation: animation, value: value))
    }

    // MARK: Decorative-element marker
    //
    //  Marks the receiver as decorative — VoiceOver skips it entirely.
    //  Apply to purely-visual elements (background images, ornamental
    //  shapes, the BotanicalSprig in Hero, the WOTD image mask) so the
    //  screen-reader user doesn't hear "image, image, image" while
    //  navigating.
    //
    
    func lexiconDecorative() -> some View {
        accessibilityHidden(true)
    }

    // MARK: Group composite views into a single VoiceOver element
    //
    //  Composite controls (e.g. LexiconHeader's brand lockup = mark + title
    //  + tagline = 3 separate Text elements) become ONE VoiceOver element
    //  whose label is the concatenation of the children. Without this,
    //  VoiceOver reads each Text separately, forcing 3 swipe gestures to
    //  navigate what is conceptually one piece of information.
    //
    //  `.accessibilityElement(children: .combine)` is the SwiftUI
    //  equivalent of `UIAccessibility.accessibilityElements = [combined]`.
    //
    
    func lexiconCombinedElement() -> some View {
        accessibilityElement(children: .combine)
    }

    // MARK: VoiceOver focus binding convenience
    //
    //  P4-05 spec §7. Use `@AccessibilityFocusState` directly in any
    //  view that needs to programmatically move VoiceOver focus:
    //
    //    @AccessibilityFocusState private var voFocus: LexiconVoiceOverFocus?
    //    ...
    //    someView.accessibilityFocused($voFocus, equals: .searchResults)
    //
    //  Then set `voFocus = .searchResults` to programmatically move
    //  VoiceOver focus to the tagged element.
    //
}

// MARK: - Reduce Motion ViewModifier

/// Backing ViewModifier for `View.lexiconMotion(_:value:)`.
///
/// Reads `@Environment(\.accessibilityReduceMotion)` and conditionally
/// applies the animation. When Reduce Motion is enabled, the view is
/// returned without animation — SwiftUI's default instantaneous state
/// change is used instead.
public struct LexiconReduceMotionModifier<V: Equatable>: ViewModifier {
    let animation: Animation?
    let value: V

    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    func body(content: Content) -> some View {
        if reduceMotion {
            content
        } else {
            content.animation(animation, value: value)
        }
    }
}

// MARK: - Usage notes
//
//  VoiceOver focus management (P4-05 §7):
//
//  Use `@AccessibilityFocusState` directly in any view that needs to
//  programmatically move VoiceOver focus. The `LexiconVoiceOverFocus`
//  enum (declared above) is the canonical focus-target namespace.
//  Example from `SearchModal.swift`:
//
//    @AccessibilityFocusState private var voFocus: LexiconVoiceOverFocus?
//
//    var body: some View {
//        resultsList
//            .accessibilityFocused($voFocus, equals: .searchResults)
//    }
//
//    func onSubmit() {
//        voFocus = .searchResults  // moves VoiceOver focus to resultsList
//    }
//
//  The `.accessibilityFocused(_:equals:)` modifier is SwiftUI's built-in
//  API — it posts `UIAccessibility.post(notification: .screenChanged,
//  argument: element)` under the hood, which is the supported way to
//  programmatically move VoiceOver focus.

