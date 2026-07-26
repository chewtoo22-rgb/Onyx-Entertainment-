package com.onyx.avhub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.onyx.avhub.app.navigation.OnyxNavHost
import com.onyx.avhub.core.common.theme.OnyxAvHubTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OnyxAvHubTheme {
                OnyxNavHost()
            }
        }
    }
}
