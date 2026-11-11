package com.lexicon

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.data.LexiconData
import com.lexicon.data.LexiconViewModel
import com.lexicon.textclassifier.LexiconWordActionReceiver
import com.lexicon.ui.screens.RootScreen
import com.lexicon.ui.theme.DarkBackground
import com.lexicon.ui.theme.LexiconTheme
import com.lexicon.ui.theme.WarmCream

class MainActivity : ComponentActivity() {

    private val viewModel: LexiconViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.attachPrefs(applicationContext)
        handleIncomingIntent(intent)

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(incoming: Intent?) {
        if (incoming == null) return

        incoming.getStringExtra("lexora_theme")?.lowercase()?.let { mode ->
            when (mode) {
                "dark" -> viewModel.setDarkMode(true)
                "light" -> viewModel.setDarkMode(false)
            }
        }

        incoming.getStringExtra("lexora_tab")?.lowercase()?.let { tab ->
            when (tab) {
                "home" -> viewModel.setActiveTab(com.lexicon.data.TabKey.HOME)
                "dictionary" -> viewModel.setActiveTab(com.lexicon.data.TabKey.DICTIONARY)
                "collections" -> viewModel.setActiveTab(com.lexicon.data.TabKey.COLLECTIONS)
                "more" -> viewModel.setActiveTab(com.lexicon.data.TabKey.MORE)
            }
        }

        val rawWord = incoming.getStringExtra(LexiconWordActionReceiver.EXTRA_WORD)?.trim()
        if (rawWord.isNullOrEmpty()) return
        val match = viewModel.searchWords(rawWord, limit = 1).firstOrNull()
        if (match != null) {
            viewModel.openWord(match)
        } else {
            viewModel.openSearch()
        }
    }
}
