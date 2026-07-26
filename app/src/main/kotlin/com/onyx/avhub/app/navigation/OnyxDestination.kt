package com.onyx.avhub.app.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.ui.graphics.vector.ImageVector
import com.onyx.avhub.app.R

enum class OnyxDestination(val route: String, val labelRes: Int, val icon: ImageVector) {
    AUDIO_HUB("audio_hub", R.string.nav_audio_hub, Icons.Filled.GraphicEq),
    PLAYER("player", R.string.nav_player, Icons.Filled.PlayCircle),
}
