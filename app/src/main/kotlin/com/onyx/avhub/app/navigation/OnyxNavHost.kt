package com.onyx.avhub.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.onyx.avhub.feature.audiohub.ui.AudioHubScreen
import com.onyx.avhub.feature.audiohub.ui.AudioHubViewModel
import com.onyx.avhub.feature.player.ui.PlayerScreen
import com.onyx.avhub.feature.player.ui.PlayerViewModel

/**
 * Above this width, Audio Hub and Player are shown side-by-side instead of behind bottom
 * navigation — a simple configuration-width breakpoint rather than full hinge-aware
 * `androidx.window` foldable posture detection, which would need a new dependency for a
 * comparatively small additional benefit over this on tablets/unfolded foldables/landscape.
 */
private const val TWO_PANE_MIN_WIDTH_DP = 600

@Composable
fun OnyxNavHost() {
    val isWideScreen = LocalConfiguration.current.screenWidthDp >= TWO_PANE_MIN_WIDTH_DP

    // Hoisted here — directly under the Activity's composition root — so switching between
    // the compact (NavBackStackEntry-scoped) and wide (plain composition-scoped) layouts below
    // doesn't tear down and recreate these ViewModels, which would lose the EQ/hub state and
    // release/recreate the ExoPlayer out from under the user.
    val audioHubViewModel: AudioHubViewModel = hiltViewModel()
    val playerViewModel: PlayerViewModel = hiltViewModel()

    if (isWideScreen) {
        TwoPaneLayout(audioHubViewModel, playerViewModel)
    } else {
        CompactNavHost(audioHubViewModel, playerViewModel)
    }
}

@Composable
private fun TwoPaneLayout(audioHubViewModel: AudioHubViewModel, playerViewModel: PlayerViewModel) {
    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())) {
            AudioHubScreen(viewModel = audioHubViewModel)
        }
        Box(modifier = Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())) {
            PlayerScreen(viewModel = playerViewModel)
        }
    }
}

@Composable
private fun CompactNavHost(audioHubViewModel: AudioHubViewModel, playerViewModel: PlayerViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            NavigationBar {
                OnyxDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(destination.icon, contentDescription = null) },
                        label = { Text(stringResource(destination.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = OnyxDestination.AUDIO_HUB.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(OnyxDestination.AUDIO_HUB.route) { AudioHubScreen(viewModel = audioHubViewModel) }
            composable(OnyxDestination.PLAYER.route) { PlayerScreen(viewModel = playerViewModel) }
        }
    }
}
