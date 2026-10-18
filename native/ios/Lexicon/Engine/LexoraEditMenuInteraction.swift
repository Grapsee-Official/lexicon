//  LexiconEditMenuInteraction.swift
//  Lexicon
//
//  Per spec: "Do use UIEditMenuInteraction (iOS 16+). Do not use UIMenuController (deprecated).
//  Conform a shared UIApplicationDelegate to UIEditMenuInteractionDelegate.
//  Implement editMenuInteraction(_:menuFor:suggestedActions:) to inject a UICommand
//  with a 'Lexicon' action alongside Copy/Paste."
//

import UIKit
import SwiftUI

/// LexiconEditMenuDelegate — injects "Lexicon" into the iOS text selection menu.
///
/// Usage: attach `.editMenuInteraction(LexiconEditMenuInteraction())` to any
/// view that should show the Lexicon action when text is selected.
/// Or set it globally on the UIApplication's key window.
@available(iOS 16.0, *)
public class LexiconEditMenuInteraction: NSObject, UIEditMenuInteractionDelegate {

    private var engine: LexiconEngine?

    public init(engine: LexiconEngine? = nil) {
        self.engine = engine
        super.init()
    }

    public func setEngine(_ engine: LexiconEngine) {
        self.engine = engine
    }

    /// Called by the system when the text selection menu is about to appear.
    /// We inject a "Lexicon" action alongside Copy/Paste/Share.
    public func editMenuInteraction(
        _ interaction: UIEditMenuInteraction,
        menuFor configuration: UIEditMenuConfiguration,
        suggestedActions: [UIMenuElement]
    ) -> UIMenu? {

        // Get the selected text
        let selectedText = configuration.identifier as? String
            ?? extractSelectedText(from: configuration)

        // If no single word is selected, return the default menu
        guard let word = selectedText?.trimmingCharacters(in: .whitespacesAndNewlines),
              !word.isEmpty,
              !word.contains(" ") // single word only
        else {
            return UIMenu(children: suggestedActions)
        }

        // Create the Lexicon lookup action
        let lexiconAction = UIAction(
            title: "Lexicon: \(word)",
            image: UIImage(systemName: "book"),
            identifier: UIAction.Identifier("com.lexicon.lookup"),
            state: .off,
            handler: { [weak self] _ in
                self?.performLookup(word: word)
            }
        )

        // Create a submenu or inline action
        let lexiconMenu = UIMenu(
            title: "Lexicon",
            image: UIImage(systemName: "book.closed"),
            identifier: UIMenu.Identifier("com.lexicon.menu"),
            options: [.displayInline],
            children: [lexiconAction]
        )

        // Insert Lexicon at the beginning, followed by the system's suggested actions
        return UIMenu(children: [lexiconMenu] + suggestedActions)
    }

    /// Perform the dictionary lookup and present the result.
    private func performLookup(word: String) {
        // Quick lookup — must be fast (millisecond budget for background wake)
        guard let engine = engine else {
            print("[Lexicon] Engine not initialized")
            return
        }

        let entry = engine.lookup(word: word)
        if let entry = entry {
            // Post a notification to present the WordDetailSheet
            DispatchQueue.main.async {
                NotificationCenter.default.post(
                    name: .lexiconWordSelected,
                    object: nil,
                    userInfo: ["word": entry]
                )
            }
        } else {
            // Word not found — show a brief alert
            DispatchQueue.main.async {
                NotificationCenter.default.post(
                    name: .lexiconWordNotFound,
                    object: nil,
                    userInfo: ["word": word]
                )
            }
        }
    }

    /// Extract the selected text from the edit menu configuration.
    /// In iOS 16+, the configuration's identifier can carry the selected text
    /// when set up by a UITextView/WKWebView.
    private func extractSelectedText(from configuration: UIEditMenuConfiguration) -> String? {
        // For UITextView, the selected text comes from the text view's selectedTextRange
        // For WKWebView, it comes from the JS selection
        // The configuration object carries context about the source
        return nil // Override in subclass for specific text source integration
    }
}

// MARK: - Notification Names

extension Notification.Name {
    static let lexiconWordSelected = Notification.Name("LexiconWordSelected")
    static let lexiconWordNotFound = Notification.Name("LexiconWordNotFound")
}

// MARK: - SwiftUI View Extension

extension View {
    /// Attaches the Lexicon edit menu interaction to this view.
    /// When the user selects text and the context menu appears, a "Lexicon"
    /// action is injected alongside Copy/Paste.
    @available(iOS 16.0, *)
    func lexiconEditMenu(engine: LexiconEngine? = nil) -> some View {
        modifier(LexiconEditMenuModifier(engine: engine))
    }
}

@available(iOS 16.0, *)
struct LexiconEditMenuModifier: ViewModifier {
    let engine: LexiconEngine?
    @State private var interaction: UIEditMenuInteraction?

    func body(content: Content) -> some View {
        content
            .background(
                LexiconEditMenuAnchor(engine: engine, interaction: $interaction)
                    .frame(width: 0, height: 0)
            )
    }
}

@available(iOS 16.0, *)
struct LexiconEditMenuAnchor: UIViewRepresentable {
    let engine: LexiconEngine?
    @Binding var interaction: UIEditMenuInteraction?

    func makeUIView(context: Context) -> UIView {
        let view = UIView()
        let delegate = LexiconEditMenuInteraction(engine: engine)
        let interaction = UIEditMenuInteraction(delegate: delegate)
        view.addInteraction(interaction)
        self.interaction = interaction
        return view
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        // Update engine if needed
    }
}
