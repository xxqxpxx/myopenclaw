package com.myopenclaw.ui.screens.home

import androidx.compose.runtime.Composable
import com.myopenclaw.data.local.PreferencesManager

@Composable
fun HomeScreen(
    preferencesManager: PreferencesManager? = null,
    onNavigateToChartAnalysis: () -> Unit = {},
    onNavigateToAIChat: () -> Unit = {},
    onNavigateToMarketSignals: () -> Unit = {},
    onNavigateToSignalDetails: (String) -> Unit = {},
    onNavigateToAnalysisDetails: (String) -> Unit = {},
    onNavigateToInsiderTrading: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {}
) {
    DashboardScreen(
        preferencesManager = preferencesManager,
        onNavigateToChartAnalysis = onNavigateToChartAnalysis,
        onNavigateToAIChat = onNavigateToAIChat,
        onNavigateToMarketSignals = onNavigateToMarketSignals,
        onNavigateToSignalDetails = onNavigateToSignalDetails,
        onNavigateToAnalysisDetails = onNavigateToAnalysisDetails,
        onNavigateToInsiderTrading = onNavigateToInsiderTrading,
        onNavigateToSearch = onNavigateToSearch
    )
}
