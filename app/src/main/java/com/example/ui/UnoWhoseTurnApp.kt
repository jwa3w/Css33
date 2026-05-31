package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnoWhoseTurnApp(viewModel: GameViewModel) {
    // Collect active Room database streams using collectAsStateWithLifecycle helper
    val playersList by viewModel.allPlayersStream.collectAsStateWithLifecycle(initialValue = emptyList())
    val statsList by viewModel.allSessionsStream.collectAsStateWithLifecycle(initialValue = emptyList())
    val rulesList by viewModel.wildcardRulesStream.collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121214)),
        contentWindowInsets = WindowInsets.safeDrawing // Respect system safe drawing regions
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Master navigation router matching screens 1 to 13
            when (viewModel.currentScreen) {
                1 -> WelcomeScreen(vm = viewModel)
                2 -> WifiConnectionScreen(vm = viewModel)
                3 -> PlayerRegistrationScreen(vm = viewModel, playersList = playersList)
                4 -> DealerSelectionScreen(vm = viewModel)
                5 -> WildcardConfigScreen(vm = viewModel, rules = rulesList)
                6 -> CalibrationScreen(vm = viewModel)
                7 -> GameDashboardScreen(vm = viewModel)
                8 -> CameraMonitorScreen(vm = viewModel)
                9 -> SensorLoggerScreen(vm = viewModel)
                10 -> VoiceSettingsScreen(vm = viewModel)
                11 -> StatsScreen(vm = viewModel, stats = statsList)
                12 -> SettingsScreen(vm = viewModel)
                13 -> WinLeaderboardScreen(vm = viewModel)
                else -> WelcomeScreen(vm = viewModel)
            }
        }
    }
}
