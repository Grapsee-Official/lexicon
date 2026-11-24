package com.lexicon.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.widthIn
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
import com.lexicon.data.ScreenRoute
import com.lexicon.ui.components.BottomNav
import com.lexicon.ui.components.CountrysideMaterialOverlay
import com.lexicon.ui.components.LexiconHeader
import com.lexicon.ui.components.SearchModal
import com.lexicon.ui.components.WordDetailSheet
import com.lexicon.ui.theme.DarkBackground
import com.lexicon.ui.theme.LexiconTextStyles
import com.lexicon.ui.theme.WarmCream

@Composable
fun RootScreen(viewModel: LexiconViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val route = state.route
    val bg = if (state.isDark) DarkBackground else WarmCream

    BackHandler(enabled = state.routeHistory.isNotEmpty()) {
        viewModel.navigateBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
    ) {
        if (route != ScreenRoute.HOME) {
            CountrysideMaterialOverlay(isDark = state.isDark, modifier = Modifier.fillMaxSize())
        }
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedContent(
            targetState = route,
            transitionSpec = { fadeIn(tween(180)) togetherWith fadeOut(tween(150)) },
            contentKey = { it },
            modifier = Modifier.weight(1f).then(if (route == ScreenRoute.HOME) Modifier else Modifier.widthIn(max = 640.dp)).fillMaxWidth(),
            label = "routeTransition",
        ) { currentRoute ->
            when (currentRoute) {
                ScreenRoute.HOME -> HomeScreen(viewModel = viewModel)
                ScreenRoute.DICTIONARY_HOME -> DictionaryHomeScreen(viewModel = viewModel)
                ScreenRoute.WORD_OF_THE_DAY -> WordOfTheDayScreen(viewModel = viewModel)
                ScreenRoute.WORD_DETAIL -> WordDetailProfileScreen(viewModel = viewModel)
                ScreenRoute.COLLECTIONS -> CollectionsHubScreen(viewModel = viewModel)
                ScreenRoute.MY_NOTES -> MyNotesScreen(viewModel = viewModel)
                ScreenRoute.STUDY_LISTS -> StudyListsScreen(viewModel = viewModel)
                ScreenRoute.QUICK_TEMPLATES -> QuickTemplatesScreen(viewModel = viewModel)
                ScreenRoute.SEARCH_DISCOVERY -> SearchDiscoveryScreen(viewModel = viewModel)
                ScreenRoute.SEARCH_RESULTS -> SearchResultsScreen(viewModel = viewModel)
                ScreenRoute.EXPANDED_EXPLORATION -> ExpandedExplorationScreen(viewModel = viewModel)
                ScreenRoute.MORE -> NonHomeTab(
                    title = "Settings",
                    subtitle = "Make Lexora yours",
                    viewModel = viewModel,
                ) {
                    MoreScreen(viewModel = viewModel)
                }
            }
        }

        // All non-Home destinations reserve the dock's actual measured height.
        // Home owns its own dock; no duplicate navigation while scrolling.
        if (route != ScreenRoute.HOME) {
            BottomNav(viewModel = viewModel, modifier = Modifier.widthIn(max = 640.dp))
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
