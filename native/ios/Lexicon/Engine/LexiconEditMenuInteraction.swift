//  LexiconEditMenuInteraction.swift
//  Lexicon

import UIKit
import SwiftUI

@available(iOS 16.0, *)
public class LexiconEditMenuInteraction: NSObject, UIEditMenuInteractionDelegate {

    private var engine: LexiconEngine?

    public init(engine: LexiconEngine? = nil) {
        self.engine = engine
        super.init()
    }

    public func setEngine(_ engine: LexiconEngine?) {
        self.engine = engine
    }

    public func editMenuInteraction(
        _ interaction: UIEditMenuInteraction,
        menuFor configuration: UIEditMenuConfiguration,
        suggestedActions: [UIMenuElement]
    ) -> UIMenu? {
        let selectedText = configuration.identifier as? String
        guard let word = selectedText?.trimmingCharacters(in: .whitespacesAndNewlines),
              !word.isEmpty,
              !word.contains(" ")
        else {
            return UIMenu(children: suggestedActions)
        }

        let lexiconAction = UIAction(
            title: "Lexicon: \(word)",
            image: UIImage(systemName: "book"),
            identifier: UIAction.Identifier("com.lexicon.lookup"),
            state: .off,
            handler: { [weak self] _ in
                self?.performLookup(word: word)
            }
        )

        let lexiconMenu = UIMenu(
            title: "Lexicon",
            image: UIImage(systemName: "book.closed"),
            identifier: UIMenu.Identifier("com.lexicon.menu"),
            options: [.displayInline],
            children: [lexiconAction]
        )

        return UIMenu(children: [lexiconMenu] + suggestedActions)
    }

    private func performLookup(word: String) {
        guard let engine = engine else { return }
        if let entry = engine.lookup(word: word) {
            DispatchQueue.main.async {
                NotificationCenter.default.post(
                    name: .lexiconWordSelected,
                    object: nil,
                    userInfo: ["word": entry]
                )
            }
        } else {
            DispatchQueue.main.async {
                NotificationCenter.default.post(
                    name: .lexiconWordNotFound,
                    object: nil,
                    userInfo: ["word": word]
                )
            }
        }
    }
}

extension Notification.Name {
    static let lexiconWordSelected = Notification.Name("LexiconWordSelected")
    static let lexiconWordNotFound = Notification.Name("LexiconWordNotFound")
}

extension View {
    @available(iOS 16.0, *)
    func lexiconEditMenu(engine: LexiconEngine? = nil) -> some View {
        modifier(LexiconEditMenuModifier(engine: engine))
    }
}

@available(iOS 16.0, *)
struct LexiconEditMenuModifier: ViewModifier {
    let engine: LexiconEngine?

    func body(content: Content) -> some View {
        content
            .background(
                LexiconEditMenuAnchor(engine: engine)
                    .frame(width: 0, height: 0)
            )
    }
}

@available(iOS 16.0, *)
struct LexiconEditMenuAnchor: UIViewRepresentable {
    let engine: LexiconEngine?

    final class Coordinator {
        let delegate: LexiconEditMenuInteraction
        init(engine: LexiconEngine?) {
            self.delegate = LexiconEditMenuInteraction(engine: engine)
        }
    }

    func makeCoordinator() -> Coordinator {
        Coordinator(engine: engine)
    }

    func makeUIView(context: Context) -> UIView {
        let view = UIView()
        let interaction = UIEditMenuInteraction(delegate: context.coordinator.delegate)
        view.addInteraction(interaction)
        return view
    }

    func updateUIView(_ uiView: UIView, context: Context) {
        context.coordinator.delegate.setEngine(engine)
    }
}
