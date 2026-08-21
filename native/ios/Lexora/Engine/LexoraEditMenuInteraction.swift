//  LexoraEditMenuInteraction.swift
//  Lexora
//
//  Per spec: "Do use UIEditMenuInteraction (iOS 16+). Do not use UIMenuController (deprecated).
//  Conform a shared UIApplicationDelegate to UIEditMenuInteractionDelegate.
//  Implement editMenuInteraction(_:menuFor:suggestedActions:) to inject a UICommand
//  with a 'Lexora' action alongside Copy/Paste."
//

import UIKit
import SwiftUI

/// LexoraEditMenuDelegate — injects "Lexora" into the iOS text selection menu.
///
/// Usage: attach `.editMenuInteraction(LexoraEditMenuInteraction())` to any
/// view that should show the Lexora action when text is selected.
/// Or set it globally on the UIApplication's key window.
@available(iOS 16.0, *)
public class LexoraEditMenuInteraction: NSObject, UIEditMenuInteractionDelegate {

    private var engine: LexoraEngine?

    public init(engine: LexoraEngine? = nil) {
        self.engine = engine
        super.init()
    }

    public func setEngine(_ engine: LexoraEngine) {
        self.engine = engine
    }

    /// Called by the system when the text selection menu is about to appear.
    /// We inject a "Lexora" action alongside Copy/Paste/Share.
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

        // Create the Lexora lookup action
        let lexoraAction = UIAction(
            title: "Lexora: \(word)",
            image: UIImage(systemName: "book"),
            identifier: UIAction.Identifier("com.lexora.lookup"),
            state: .off,
            handler: { [weak self] _ in
                self?.performLookup(word: word)
            }
        )

        // Create a submenu or inline action
        let lexoraMenu = UIMenu(
            title: "Lexora",
            image: UIImage(systemName: "book.closed"),
            identifier: UIMenu.Identifier("com.lexora.menu"),
            options: [.displayInline],
            children: [lexoraAction]
        )

        // Insert Lexora at the beginning, followed by the system's suggested actions
        return UIMenu(children: [lexoraMenu] + suggestedActions)
    }

    /// Perform the dictionary lookup and present the result.
    private func performLookup(word: String) {
        // Quick lookup — must be fast (millisecond budget for background wake)
        guard let engine = engine else {
            print("[Lexora] Engine not initialized")
            return
        }

        let entry = engine.lookup(word: word)
        if let entry = entry {
            // Post a notification to present the WordDetailSheet
            DispatchQueue.main.async {
                NotificationCenter.default.post(
                    name: .lexoraWordSelected,
                    object: nil,
                    userInfo: ["word": entry]
                )
            }
        } else {
            // Word not found — show a brief alert
            DispatchQueue.main.async {
                NotificationCenter.default.post(
                    name: .lexoraWordNotFound,
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
    static let lexoraWordSelected = Notification.Name("LexoraWordSelected")
    static let lexoraWordNotFound = Notification.Name("LexoraWordNotFound")
}

// MARK: - SwiftUI View Extension

extension View {
    /// Attaches the Lexora edit menu interaction to this view.
    /// When the user selects text and the context menu appears, a "Lexora"
    /// action is injected alongside Copy/Paste.
    @available(iOS 16.0, *)
    func lexoraEditMenu(engine: LexoraEngine? = nil) -> some View {
        modifier(LexoraEditMenuModifier(engine: engine))
    }
}

@available(iOS 16.0, *)
struct LexoraEditMenuModifier: ViewModifier {
    let engine: LexoraEngine?
    @State private var interaction: UIEditMenuInteraction?

    func body(content: Content) -> some View {
        content
            .background(
                LexoraEditMenuAnchor(engine: engine, interaction: $interaction)
                    .frame(width: 0, height: 0)
            )
    }
}

@available(iOS 16.0, *)
struct LexoraEditMenuAnchor: UIViewRepresentable {
    let engine: LexoraEngine?
    @Binding var interaction: UIEditMenuInteraction?

    func makeUIView(context: Context) -> UIView {
        let view = UIView()
        let delegate = LexoraEditMenuInteraction(engine: engine)
        let interaction = UIEditMenuInteraction(delegate: delegate)
        view.addInteraction(interaction)
        self.interaction = interaction
        return view
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        // Update engine if needed
    }
}
