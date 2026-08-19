//
//  LexoraApp.swift
//  Lexora
//
//  @main App entry point. Owns the shared ViewModel + Theme state and
//  applies the Lexora background / preferred color scheme at the root.
//

import SwiftUI

@main
struct LexoraApp: App {
    @StateObject private var viewModel = LexoraViewModel()
    @StateObject private var themeState = LexoraThemeState()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(viewModel)
                .environmentObject(themeState)
                .lexoraTheme()
        }
    }
}
