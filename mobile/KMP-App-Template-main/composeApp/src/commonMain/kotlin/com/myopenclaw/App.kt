package com.myopenclaw

import androidx.compose.runtime.Composable
import com.myopenclaw.data.local.PreferencesManager
import com.myopenclaw.ui.navigation.AppNavigation
import com.myopenclaw.ui.theme.MyOpenClawTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    MyOpenClawTheme {
        val preferencesManager: PreferencesManager = koinInject()
        AppNavigation(preferencesManager = preferencesManager)
    }
}
