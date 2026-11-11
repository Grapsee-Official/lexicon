package com.lexicon.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lexicon.data.LexiconViewModel
import com.lexicon.data.TabKey
import com.lexicon.ui.components.BottomNav
import com.lexicon.ui.components.LexiconHeader
import com.lexicon.ui.components.SearchModal
import com.lexicon.ui.components.WordDetailSheet
import com.lexicon.ui.theme.DarkBackground
import com.lexicon.ui.theme.LexiconTextStyles
import com.lexicon.ui.theme.WarmCream

@Composable
fun RootScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activeTab = state.activeTab
    val bg = if (state.isDark) DarkBackground else WarmCream

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
    ) {
        AnimatedContent(
            targetState = activeTab,
            transitionSpec = {
                val enter = fadeIn(tween(220)) +
                    slideInVertically(tween(220)) { full -> full / 14 }
                val exit = fadeOut(tween(180)) +
                    slideOutVertically(tween(180)) { full -> -full / 14 }
                enter togetherWith exit
            },
            contentKey = { it },
            modifier = Modifier.fillMaxSize(),
            label = "tabTransition",
        ) { tab ->
            when (tab) {
                TabKey.HOME -> HomeScreen(viewModel = viewModel)

                TabKey.DICTIONARY -> NonHomeTab(
                    title = "Dictionary",
                    subtitle = "Browse the full lexicon",
                    viewModel = viewModel,
                ) {
                    DictionaryScreen(viewModel = viewModel)
                }

                TabKey.COLLECTIONS -> NonHomeTab(
                    title = "Collections",
                    subtitle = "Your saved words",
                    viewModel = viewModel,
                ) {
                    CollectionsScreen(viewModel = viewModel)
                }

                TabKey.MORE -> NonHomeTab(
                    title = "Settings",
                    subtitle = "Make Lexora yours",
                    viewModel = viewModel,
                ) {
                    MoreScreen(viewModel = viewModel)
                }

                TabKey.SEARCH -> HomeScreen(viewModel = viewModel)
            }
        }

        if (activeTab != TabKey.HOME) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            ) {
                BottomNav(viewModel = viewModel)
            }
        }

        WordDetailSheet(viewModel = viewModel)
        SearchModal(viewModel = viewModel)
    }
}

@Composable
private fun NonHomeTab(
    title: String,
    subtitle: String,
    viewModel: LexiconViewModel,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LexiconHeader(viewModel = viewModel)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            Text(
                text = title,
                style = LexiconTextStyles.SectionTitle,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = LexiconTextStyles.HeroParagraph,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            content()
        }
    }
}
