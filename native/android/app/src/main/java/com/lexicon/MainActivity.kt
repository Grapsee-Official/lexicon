package com.lexicon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import com.lexicon.data.LexiconViewModel
import com.lexicon.ui.screens.RootScreen
import com.lexicon.ui.theme.DarkBackground
import com.lexicon.ui.theme.LexiconTheme
import com.lexicon.ui.theme.WarmCream

/**
 * MainActivity — single Activity entry point.
 *
 *  - enableEdgeToEdge(): status & nav bars transparent; Compose draws into
 *    the insets (status bar icon appearance is flipped in [LexiconTheme]).
 *  - The shared [LexiconViewModel] is created once via `by viewModels()` and
 *    prefs are attached before composition so the first frame already
 *    reflects the persisted dark-mode flag / saved-word list.
 *  - setContent hosts [LexiconTheme] → [RootScreen]. Everything else
 *    (header, tab content, bottom nav, overlays) is composed by RootScreen.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: LexiconViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge: status & nav bars are transparent, Compose draws
        // into the insets. LexiconTheme flips the icon appearance to match
        // the active colour scheme.
        enableEdgeToEdge()

        // Bind SharedPreferences-backed persistence before the first
        // composition so the UI hydrates with the user's last state.
        viewModel.attachPrefs(applicationContext)

        setContent {
            val state by viewModel.uiState.collectAsStateWithLifecycle()
            LexiconTheme(darkTheme = state.isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (state.isDark) DarkBackground else WarmCream,
                ) {
                    RootScreen(viewModel = viewModel)
                }
            }
        }
    }
}
