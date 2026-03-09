package com.myopenclaw.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.myopenclaw.ui.theme.Dark1

/**
 * Preview/Demo screen to showcase both states of HomeScreenNew
 * This is useful for testing and development
 */
@Composable
fun HomeScreenPreview() {
    var showEmptyState by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Dark1)
    ) {
        // Toggle button at the top
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A2332),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showEmptyState) "Empty State (No Analysis)" else "With Trade Analysis",
                    color = Color.White
                )
                Switch(
                    checked = !showEmptyState,
                    onCheckedChange = { showEmptyState = !it },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Color(0xFF30CD8F),
                        checkedThumbColor = Color.White
                    )
                )
            }
        }

        // The actual HomeScreenNew
        HomeScreenNew(
            signals = if (showEmptyState) emptyList() else listOf(),
            analyses = if (showEmptyState) emptyList() else listOf(),
            onAnalyseTradeClick = {  },
            onAskQuestionsClick = {  },
            onSignalClick = {  signal -> },
            onAnalysisClick = {  analysisId -> },
            onViewAllSignalsClick = {  },
            onViewAllAnalysisClick = {  },
            onNavigateToSignals = {  },
            onNavigateToChat = {  },
            onNavigateToProfile = {  },
            onScanClick = {  }
        )
    }
}

/**
 * Simple demo to show the screen directly without toggle
 */
@Composable
fun HomeScreenEmptyStateDemo() {
    HomeScreenNew(
        signals = emptyList(),
        analyses = emptyList(),
        onScanClick = {  }
    )
}

@Composable
fun HomeScreenWithDataDemo() {
    HomeScreenNew(
        signals = listOf(),
        analyses = listOf(),
        onAnalysisClick = {  analysisId ->
            }
    )
}
