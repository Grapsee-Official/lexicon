//
//  LexiconApp.swift
//  Lexicon
//
//  @main App entry point. Owns the shared ViewModel + Theme state and
//  applies the Lexicon background / preferred color scheme at the root.
//
//  Mirrors the web root layout: `<main className="...bg-background">`
//  wraps the entire app, with state provided via React Context.
//  On iOS, the equivalent is `.environmentObject(vm)` injected at the
//  WindowGroup root and read in any view with `@EnvironmentObject`.
//

import SwiftUI

@main
struct LexiconApp: App {
    // MARK: - Root state (single source of truth)
    //
    // LexiconViewModel translates the Zustand store 1:1 (activeTab, selectedWord,
    // searchOpen, savedWordIds, recentSearches). LexiconThemeState drives the
    // appearance-aware palette + persists the user's color-scheme choice.
    //
    // Both are created here as @StateObject so they live for the app's lifetime
    // and are propagated to every screen via .environmentObject.
    @StateObject private var viewModel = LexiconViewModel()
    @StateObject private var themeState = LexiconThemeState()

    var body: some Scene {
        WindowGroup {
            ContentView()
                // Inject the shared ViewModel + ThemeState into the environment
                // so any descendant view can read them with @EnvironmentObject.
                .environmentObject(viewModel)
                .environmentObject(themeState)
                // Apply the Lexicon background + preferred color scheme at the root.
                .lexiconTheme()
        }
    }
}
