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
import com.lexicon.data.ScreenRoute
import com.lexicon.data.TabKey
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

        val args = incoming.extras
        if (args != null) {
            args.getString("lexora_theme")?.lowercase()?.let { mode ->
                when (mode) {
                    "dark" -> viewModel.setDarkMode(true)
                    "light" -> viewModel.setDarkMode(false)
                }
            }
            args.getString("lexora_tab")?.lowercase()?.let { tab ->
                when (tab) {
                    "home" -> viewModel.setActiveTab(TabKey.HOME)
                    "dictionary" -> viewModel.setActiveTab(TabKey.DICTIONARY)
                    "search" -> viewModel.setActiveTab(TabKey.SEARCH)
                    "collections" -> viewModel.setActiveTab(TabKey.COLLECTIONS)
                    "more" -> viewModel.setActiveTab(TabKey.MORE)
                }
            }
            args.getString("lexicon_screen")?.lowercase()?.let { scr ->
                when (scr) {
                    "1", "dictionary_home", "dict_home" -> viewModel.navigate(ScreenRoute.DICTIONARY_HOME)
                    "2", "wotd", "word_of_the_day" -> viewModel.navigate(ScreenRoute.WORD_OF_THE_DAY, word = LexiconData.serendipityEntry)
                    "3", "word_detail", "detail" -> viewModel.navigate(ScreenRoute.WORD_DETAIL, word = LexiconData.serendipityEntry)
                    "4", "collections" -> viewModel.navigate(ScreenRoute.COLLECTIONS)
                    "5", "notes", "my_notes" -> viewModel.navigate(ScreenRoute.MY_NOTES)
                    "6", "study_lists", "study" -> viewModel.navigate(ScreenRoute.STUDY_LISTS)
                    "7", "templates", "quick_templates" -> viewModel.navigate(ScreenRoute.QUICK_TEMPLATES)
                    "8", "search", "search_discovery" -> viewModel.navigate(ScreenRoute.SEARCH_DISCOVERY)
                    "9", "results", "search_results" -> {
                        viewModel.setSearchQuery("serendipity")
                        viewModel.navigate(ScreenRoute.SEARCH_RESULTS, word = LexiconData.serendipityEntry)
                    }
                    "10", "expanded", "expanded_exploration" -> viewModel.navigate(ScreenRoute.EXPANDED_EXPLORATION, word = LexiconData.serendipityEntry, expand36 = false)
                    "10_expanded", "translations" -> viewModel.navigate(ScreenRoute.EXPANDED_EXPLORATION, word = LexiconData.serendipityEntry, expand36 = true)
                    "home" -> viewModel.setActiveTab(TabKey.HOME)
                }
            }
            val rawWord = (
                args.getString(LexiconWordActionReceiver.EXTRA_WORD)
                    ?: args.getString("lexora_word")
                    ?: incoming.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT)?.toString()
                )?.trim()
            if (!rawWord.isNullOrEmpty()) {
                val match = viewModel.searchWords(rawWord, limit = 1).firstOrNull()
                if (match != null) {
                    viewModel.openWord(match)
                } else {
                    viewModel.openSearch()
                }
            }
        }
    }
}
