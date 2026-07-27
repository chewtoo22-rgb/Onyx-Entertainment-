package com.onyx.avhub.feature.player.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Toggles a truly immersive fullscreen video: hides system bars (swipe-to-reveal), for the
 * "wow" of the video filling the entire screen edge-to-edge. Restores both bar visibility and
 * the window's original `systemBarsBehavior` (not just visibility) whenever immersive mode is
 * turned off or the screen is left while still in it, so later screens don't inherit
 * `BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE`.
 */
@Composable
fun ImmersiveToggleButton(isImmersive: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val activity = LocalContext.current.findActivity()
    var originalSystemBarsBehavior by remember(activity) {
        mutableIntStateOf(WindowInsetsControllerCompat.BEHAVIOR_DEFAULT)
    }

    LaunchedEffect(isImmersive, activity) {
        val window = activity?.window ?: return@LaunchedEffect
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (isImmersive) {
            originalSystemBarsBehavior = controller.systemBarsBehavior
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = originalSystemBarsBehavior
        }
    }

    DisposableEffect(activity) {
        onDispose {
            val window = activity?.window ?: return@onDispose
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.show(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = originalSystemBarsBehavior
        }
    }

    IconButton(onClick = onToggle, modifier = modifier) {
        Icon(
            imageVector = if (isImmersive) Icons.Filled.FullscreenExit else Icons.Filled.Fullscreen,
            contentDescription = if (isImmersive) "Exit fullscreen" else "Enter fullscreen",
        )
    }
}
