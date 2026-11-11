//
//  LexiconApp.swift
//  Lexicon
//

import SwiftUI

@main
struct LexiconApp: App {
    @StateObject private var viewModel = LexiconViewModel()
    @StateObject private var themeState = LexiconThemeState()

    var body: some Scene {
        WindowGroup {
            ContentView()
                // IMPORTANT: Modifier order in SwiftUI goes bottom-up (outside-in).
                // `.lexiconTheme()` MUST sit INSIDE `.environmentObject(themeState)`
                // so `LexiconThemeModifier` receives `themeState` from the outer environment!
                .lexiconTheme()
                .environmentObject(viewModel)
                .environmentObject(themeState)
        }
    }
}
