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

/**
 * RootScreen — top-level orchestrator for the Lexicon app.
 *
 * Direct translation of /home/z/my-project/src/app/page.tsx:
 *  - LexiconHeader always visible (on HOME tab HomeScreen renders its own header).
 *  - AnimatedContent transitions between the four primary tabs.
 *  - Sub-header (title + subtitle) renders above non-home screens.
 *  - BottomNav floats above the content, always reachable.
 *  - WordDetailSheet + SearchModal overlays observe ViewModel state.
 *  - Bottom padding clears the floating nav (pb-24 ≈ 96dp).
 *
 * @param viewModel the shared [LexiconViewModel]; same instance is bound to the
 *                  Activity's [androidx.lifecycle.ViewModelStore] so every
 *                  composable in the tree observes the same state.
 */
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
        // --- Main content area with animated tab transitions -----------------
        AnimatedContent(
            targetState = activeTab,
            transitionSpec = {
                val enter = fadeIn(tween(220)) +
                    slideInVertically(tween(220)) { full -> full / 12 }
                val exit = fadeOut(tween(180)) +
                    slideOutVertically(tween(180)) { full -> -full / 12 }
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
                    DictionaryScreen(vm = viewModel)
                }

                TabKey.COLLECTIONS -> NonHomeTab(
                    title = "Collections",
                    subtitle = "Your saved words",
                    viewModel = viewModel,
                ) {
                    CollectionsScreen(vm = viewModel)
                }

                TabKey.MORE -> NonHomeTab(
                    title = "Settings",
                    subtitle = "Make Lexicon yours",
                    viewModel = viewModel,
                ) {
                    MoreScreen(vm = viewModel)
                }

                // SEARCH tab is never entered — the centre FAB opens the
                // SearchModal overlay instead. Fall back to Home so the
                // screen is never blank.
                TabKey.SEARCH -> HomeScreen(viewModel = viewModel)
            }
        }

        // --- Floating BottomNav overlay --------------------------------------
        // HomeScreen renders its own BottomNav when activeTab == HOME; to
        // avoid duplicate navigation bars we only render the overlay for
        // the other tabs.
        if (activeTab != TabKey.HOME) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
            ) {
                BottomNav(viewModel = viewModel)
            }
        }

        // --- Global overlays -------------------------------------------------
        // WordDetailSheet — observes vm.uiState.selectedWord internally and
        // animates in/out based on whether a word is selected.
        WordDetailSheet(vm = viewModel)

        // SearchModal — observes vm.uiState.searchOpen internally.
        SearchModal(vm = viewModel)
    }
}

/**
 * Layout for non-home tabs: a top [LexiconHeader] followed by a sub-header
 * (title + subtitle) and the screen content. The content area gets a
 * bottom padding of 96dp so the last item is never hidden behind the
 * floating [BottomNav].
 */
@Composable
private fun NonHomeTab(
    title: String,
    subtitle: String,
    viewModel: LexiconViewModel,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header (matches web — always visible above the sub-header).
        LexiconHeader(viewModel = viewModel)

        // Sub-header: title + subtitle (mirrors the web's tabMeta block).
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
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
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            )
        }

        // Content — weight(1f) so it fills the remaining space above the nav.
        // Bottom padding clears the floating BottomNav (96dp ≈ Tailwind pb-24).
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = 96.dp),
        ) {
            content()
        }
    }
}
