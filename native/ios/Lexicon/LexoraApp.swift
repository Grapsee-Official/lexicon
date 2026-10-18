//
//  LexoraApp.swift
//  Lexora
//
//  @main App entry point. Owns the shared ViewModel + Theme state and
//  applies the Lexora background / preferred color scheme at the root.
//
//  Mirrors the web root layout: `<main className="...bg-background">`
//  wraps the entire app, with state provided via React Context.
//  On iOS, the equivalent is `.environmentObject(vm)` injected at the
//  WindowGroup root and read in any view with `@EnvironmentObject`.
//

import SwiftUI

@main
struct LexoraApp: App {
    // MARK: - Root state (single source of truth)
    //
    // LexoraViewModel translates the Zustand store 1:1 (activeTab, selectedWord,
    // searchOpen, savedWordIds, recentSearches). LexoraThemeState drives the
    // appearance-aware palette + persists the user's color-scheme choice.
    //
    // Both are created here as @StateObject so they live for the app's lifetime
    // and are propagated to every screen via .environmentObject.
    @StateObject private var viewModel = LexoraViewModel()
    @StateObject private var themeState = LexoraThemeState()

    var body: some Scene {
        WindowGroup {
            ContentView()
                // Inject the shared ViewModel + ThemeState into the environment
                // so any descendant view can read them with @EnvironmentObject.
                .environmentObject(viewModel)
                .environmentObject(themeState)
                // Apply the Lexora background + preferred color scheme at the root.
                .lexiconTheme()
        }
    }
}
