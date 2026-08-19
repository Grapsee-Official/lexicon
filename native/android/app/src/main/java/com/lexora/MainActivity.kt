package com.lexora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import com.lexora.data.LexoraViewModel
import com.lexora.ui.screens.HomeScreen
import com.lexora.ui.theme.DarkBackground
import com.lexora.ui.theme.LexoraTheme
import com.lexora.ui.theme.WarmCream

class MainActivity : ComponentActivity() {

    private val viewModel: LexoraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Edge-to-edge: status & nav bars are transparent, Compose draws into insets.
        enableEdgeToEdge()
        viewModel.attachPrefs(applicationContext)
        setContent {
            val state by viewModel.uiState.collectAsState()
            LexoraTheme(darkTheme = state.isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = if (state.isDark) DarkBackground else WarmCream,
                ) {
                    HomeScreen(viewModel = viewModel)
                }
            }
        }
    }
}
