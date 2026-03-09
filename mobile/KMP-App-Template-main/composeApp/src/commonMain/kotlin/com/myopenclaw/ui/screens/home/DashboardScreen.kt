package com.myopenclaw.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myopenclaw.data.local.PreferencesManager
import com.myopenclaw.domain.models.InsiderTrade
import com.myopenclaw.domain.models.CongressTrade
import com.myopenclaw.ui.components.SWCard
import com.myopenclaw.ui.theme.*
import com.myopenclaw.ui.viewmodel.home.HomeViewModel
import com.myopenclaw.ui.viewmodel.home.HomeState
import org.koin.compose.viewmodel.koinViewModel

data class MarketSignal(
    val id: String,
    val pair: String,
    val action: String, // BUY or SELL
    val price: String,
    val time: String,
    val changePercent: Double
)

data class TradeAnalysis(
    val id: String,
    val pair: String,
    val action: String,
    val entryPrice: String,
    val targetPrice: String,
    val confidence: Double, // 0-10
    val timeframe: String,
    val status: String // Active, Completed, Stopped
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: HomeViewModel = koinViewModel(),
    preferencesManager: PreferencesManager? = null,
    onNavigateToChartAnalysis: () -> Unit = {},
    onNavigateToAIChat: () -> Unit = {},
    onNavigateToMarketSignals: () -> Unit = {},
    onNavigateToSignalDetails: (String) -> Unit = {},
    onNavigateToAnalysisDetails: (String) -> Unit = {},
    onNavigateToInsiderTrading: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToAlerts: () -> Unit = {},
    onNavigateToAnalysisHistory: () -> Unit = {}
) {
    val homeState by viewModel.homeState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    // Get personalization data from onboarding
    val userName = preferencesManager?.getUserName() ?: "Trader"
    val traderType = preferencesManager?.getTraderType() ?: ""
    val tradedAssets = preferencesManager?.getTradedAssets() ?: ""
    val confidenceLevel = preferencesManager?.getConfidenceLevel() ?: ""

    // Generate personalized welcome message
    val welcomeMessage = when {
        traderType.isNotEmpty() -> "Welcome back, ${userName}!"
        confidenceLevel.isNotEmpty() -> "Let's keep building your confidence, ${userName}!"
        else -> "Welcome back!"
    }

    // Convert insider trades to market signals for display
    val insiderTradesAsSignals = remember(homeState) {
        when (val state = homeState) {
            is HomeState.Success -> state.data.recentInsiderTrades.map { trade ->
                MarketSignal(
                    id = "${trade.ticker}-${trade.transactionDate}",
                    pair = trade.ticker,
                    action = if (trade.transactionType.lowercase().contains("buy") ||
                        trade.transactionType.lowercase().contains("purchase")) "BUY" else "SELL",
                    price = formatCurrency(trade.pricePerShare ?: 0.0),
                    time = trade.transactionDate,
                    changePercent = trade.performance ?: 0.0
                )
            }
            else -> emptyList()
        }
    }

    // Convert to trade analyses
    val tradeAnalyses = remember(homeState) {
        when (val state = homeState) {
            is HomeState.Success -> state.data.recentInsiderTrades.take(3).map { trade ->
                TradeAnalysis(
                    id = "${trade.ticker}-${trade.transactionDate}",
                    pair = trade.ticker,
                    action = if (trade.transactionType.lowercase().contains("buy") ||
                        trade.transactionType.lowercase().contains("purchase")) "BUY" else "SELL",
                    entryPrice = formatCurrency(trade.pricePerShare ?: 0.0),
                    targetPrice = formatCurrency((trade.pricePerShare ?: 0.0) * 1.1), // Example target
                    confidence = (trade.performance?.let { (it + 10).coerceIn(0.0, 10.0) } ?: 7.0),
                    timeframe = "1D",
                    status = "Active"
                )
            }
            else -> emptyList()
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark),
            contentPadding = PaddingValues(bottom = 80.dp) // Account for bottom nav
        ) {
            // Header Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.paddingLarge)
                ) {
                    Column {
                        Text(
                            text = "Dashboard",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = welcomeMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                        // Show trader type badge if available
                        if (traderType.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                color = Primary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = traderType,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Primary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // This row was duplicated, keeping only one and merging content
                        // The welcome message is now handled above.
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = onNavigateToSearch,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceDark)
                                    .testTag("home_search_button")
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = onNavigateToAlerts,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceDark)
                            ) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Dimensions.paddingLarge)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Analyse Trade Chart Button
                        Button(
                            onClick = onNavigateToChartAnalysis,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("home_chart_analysis_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Primary
                            ),
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.ShowChart,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Analyse Trade chart",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Ask Questions Button
                        OutlinedButton(
                            onClick = onNavigateToAIChat,
                            modifier = Modifier.weight(1f).testTag("home_ai_chat_button"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Primary
                            ),
                            border = BorderStroke(1.dp, Primary),
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(vertical = 16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.QuestionAnswer,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "Ask questions",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(Dimensions.spacingXLarge))
            }

            // Loading State
            when (homeState) {
                is HomeState.Loading -> {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Dimensions.paddingXLarge),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Primary)
                        }
                    }
                }

                is HomeState.Error -> {
                    item {
                        ErrorStateCard(
                            message = (homeState as HomeState.Error).message,
                            onRetry = { viewModel.loadDashboardData() }
                        )
                    }
                }

                else -> {
                    // Today's Market Signals Section (Recent Insider Trades)
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimensions.paddingLarge)
                                .testTag("home_market_signals_section")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent Insider Trades",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                TextButton(onClick = onNavigateToInsiderTrading, modifier = Modifier.testTag("home_signals_view_all")) {
                                    Text(
                                        text = "See all",
                                        color = Primary,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(Dimensions.spacingMedium))
                    }

                    // Today's Signals List
                    if (insiderTradesAsSignals.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = "No recent trades",
                                subtitle = "Check back later for new insider trading activity"
                            )
                        }
                    } else {
                        items(insiderTradesAsSignals.take(5)) { signal ->
                            MarketSignalCard(
                                signal = signal,
                                onClick = { onNavigateToSignalDetails(signal.id) },
                                testTag = "home_signal_card_${signal.pair}"
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(Dimensions.spacingXLarge))
                    }

                    // Trade Analysis Section
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimensions.paddingLarge)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Trade analysis",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )

                                TextButton(onClick = onNavigateToAnalysisHistory) {
                                    Text(
                                        text = "See all",
                                        color = Primary,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(Dimensions.spacingMedium))
                    }

                    // Trade Analysis List
                    if (tradeAnalyses.isEmpty()) {
                        item {
                            EmptyStateCard(
                                title = "No active analyses",
                                subtitle = "Upload a chart to get AI-powered analysis"
                            )
                        }
                    } else {
                        items(tradeAnalyses) { analysis ->
                            TradeAnalysisCard(
                                analysis = analysis,
                                onClick = { onNavigateToAnalysisDetails(analysis.id) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
                    }
                }
            }
        }
    }
}

private fun formatCurrency(value: Double): String {
    return if (value >= 1000) {
        "$${((value / 1000) * 100).toInt() / 100.0}K"
    } else {
        "$${(value * 100).toInt() / 100.0}"
    }
}

@Composable
fun MarketSignalCard(
    signal: MarketSignal,
    onClick: () -> Unit,
    testTag: String? = null
) {
    val isBuy = signal.action == "BUY"
    val actionColor = if (isBuy) BullishGreen else BearishRed

    SWCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.paddingLarge, vertical = 6.dp)
            .then(testTag?.let { Modifier.testTag(it) } ?: Modifier),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Currency pair icon
                Surface(
                    color = Primary.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = signal.pair.take(3),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }

                Column {
                    Text(
                        text = signal.pair,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = signal.price,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                        Text(
                            text = signal.time,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Action badge
                Surface(
                    color = actionColor.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = signal.action,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 13.sp
                        ),
                        fontWeight = FontWeight.Bold,
                        color = actionColor
                    )
                }

                // Change percentage
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (signal.changePercent >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = null,
                        tint = if (signal.changePercent >= 0) BullishGreen else BearishRed,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${if (signal.changePercent >= 0) "+" else ""}${(signal.changePercent * 100).toInt() / 100.0}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (signal.changePercent >= 0) BullishGreen else BearishRed
                    )
                }
            }
        }
    }
}

@Composable
fun TradeAnalysisCard(
    analysis: TradeAnalysis,
    onClick: () -> Unit
) {
    val isBuy = analysis.action == "BUY"
    val actionColor = if (isBuy) BullishGreen else BearishRed

    // Confidence score color
    val confidenceColor = when {
        analysis.confidence >= 7.0 -> BullishGreen
        analysis.confidence >= 5.0 -> Primary
        else -> BearishRed
    }

    SWCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.paddingLarge, vertical = 6.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Currency pair icon
                    Surface(
                        color = actionColor.copy(alpha = 0.15f),
                        shape = CircleShape,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = analysis.pair.take(3),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = actionColor
                            )
                        }
                    }

                    Column {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = analysis.pair,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Action badge
                            Surface(
                                color = actionColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = analysis.action,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = actionColor
                                )
                            }
                        }

                        Text(
                            text = "Entry: ${analysis.entryPrice} • Target: ${analysis.targetPrice}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Confidence score
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${(analysis.confidence * 10).toInt() / 10.0}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = confidenceColor
                    )
                    Text(
                        text = "/10",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Timeframe and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = analysis.timeframe,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Status badge
                Surface(
                    color = when (analysis.status) {
                        "Active" -> Primary.copy(alpha = 0.15f)
                        "Completed" -> BullishGreen.copy(alpha = 0.15f)
                        else -> BearishRed.copy(alpha = 0.15f)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = analysis.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = when (analysis.status) {
                            "Active" -> Primary
                            "Completed" -> BullishGreen
                            else -> BearishRed
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String
) {
    SWCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.paddingLarge)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingXLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun ErrorStateCard(
    message: String,
    onRetry: () -> Unit
) {
    SWCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.paddingLarge)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingXLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = BearishRed
            )
            Text(
                text = "Something went wrong",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Retry")
            }
        }
    }
}