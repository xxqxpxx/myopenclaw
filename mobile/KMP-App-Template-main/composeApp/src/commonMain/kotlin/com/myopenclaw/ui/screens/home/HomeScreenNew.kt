package com.myopenclaw.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import myopenclaw.composeapp.generated.resources.Res
import myopenclaw.composeapp.generated.resources.appstore
import com.myopenclaw.domain.models.ChartAnalysis
import com.myopenclaw.domain.models.MarketSignal
import com.myopenclaw.domain.models.SignalAction
import com.myopenclaw.domain.usecase.signals.MarketSignalAccess
import com.myopenclaw.ui.components.SubscriptionUpsellBanner
import com.myopenclaw.ui.theme.*
import kotlin.math.roundToInt

// Simplified view model for trade analysis display
data class TradeAnalysisView(
    val id: String,
    val pair: String,
    val type: String,
    val confidence: Int,
    val action: String, // "BUY", "SELL", "HOLD"
    val iconLetter: String,
    val iconColor: Color
)

@Composable
fun HomeScreenNew(
    signals: List<MarketSignal> = emptyList(),
    analyses: List<ChartAnalysis> = emptyList(),
    isLoadingSignals: Boolean = false,
    isLoadingAnalyses: Boolean = false,
    signalAccess: MarketSignalAccess = MarketSignalAccess.InTrial(24),
    onAnalyseTradeClick: () -> Unit = {},
    onAskQuestionsClick: () -> Unit = {},
    onSignalClick: (MarketSignal) -> Unit = {},
    onAnalysisClick: (String) -> Unit = {}, // Takes analysis ID
    onViewAllSignalsClick: () -> Unit = {},
    onViewAllAnalysisClick: () -> Unit = {},
    onNavigateToSignals: () -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onScanClick: () -> Unit = {},
    onSubscribeClick: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Dark1)
            .statusBarsPadding(),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Header Section
        item {
            HomeHeader()
        }

        // Action Cards Section
        item {
            ActionCardsSection(
                onAnalyseTradeClick = onAnalyseTradeClick,
                onAskQuestionsClick = onAskQuestionsClick
            )
        }

        // Market Signals Section
        item {
            MarketSignalsSection(
                signals = signals,
                isLoading = isLoadingSignals,
                signalAccess = signalAccess,
                onViewAllClick = onViewAllSignalsClick,
                onSignalClick = onSignalClick,
                onSubscribeClick = onSubscribeClick
            )
        }

        // Trade Analysis Section or Empty State
        if (analyses.isNotEmpty() || isLoadingAnalyses) {
            item {
                TradeAnalysisSection(
                    analyses = analyses,
                    isLoading = isLoadingAnalyses,
                    onViewAllClick = onViewAllAnalysisClick,
                    onAnalysisClick = onAnalysisClick
                )
            }
        } else {
            item {
                EmptyStateSection(
                    onScanClick = onScanClick
                )
            }
        }
    }
}

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: App icon + name + Pro badge
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            androidx.compose.foundation.Image(
                painter = painterResource(Res.drawable.appstore),
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            // App Name
            Text(
                text = "Signalwhisper",
                color = White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )

            // Pro Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF2C3544))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "Pro",
                    color = White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Right side: Currency/Language selector
        Box(
            modifier = Modifier
                .height(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF2C3544))
                .clickable { /* Language selector */ }
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // US Flag icon (using placeholder)
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF5252)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🇺🇸",
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "En",
                        color = White,
                        fontSize = 10.sp
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Dropdown",
                        tint = White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionCardsSection(
    onAnalyseTradeClick: () -> Unit,
    onAskQuestionsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Analyse Trade chart card
        ActionCard(
            icon = Icons.Default.TrendingUp,
            label = "Analyse Trade chart",
            onClick = onAnalyseTradeClick,
            modifier = Modifier.weight(1f)
        )

        // Ask questions card
        ActionCard(
            icon = Icons.Default.QuestionAnswer,
            label = "Ask questions",
            onClick = onAskQuestionsClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(88.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2C3544))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = White,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = label,
                    color = White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Go",
                tint = White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun MarketSignalsSection(
    signals: List<MarketSignal>,
    isLoading: Boolean,
    signalAccess: MarketSignalAccess,
    onViewAllClick: () -> Unit,
    onSignalClick: (MarketSignal) -> Unit,
    onSubscribeClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Today's Market signals",
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                // Show trial badge if in trial
                if (signalAccess is MarketSignalAccess.InTrial) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Green2.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${signalAccess.hoursRemaining}h left",
                            color = Green2,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            // Only show View All if user has access
            if (signalAccess !is MarketSignalAccess.TrialExpired) {
                Text(
                    text = "View All",
                    color = Green2,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onViewAllClick)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Check access state
        when (signalAccess) {
            is MarketSignalAccess.TrialExpired -> {
                // Show upsell banner when trial expired
                SubscriptionUpsellBanner(
                    onSubscribeClick = onSubscribeClick
                )
            }
            else -> {
                // Show signals for subscribed or in-trial users
                when {
                    isLoading -> {
                        // Show loading indicator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = Green2,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    signals.isEmpty() -> {
                        // Show empty state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp, horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No signals available at the moment",
                                color = White.copy(alpha = 0.6f),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    else -> {
                        // Show signals
                        signals.take(3).forEach { signal ->
                            SignalCard(
                                signal = signal,
                                onClick = { onSignalClick(signal) },
                                testTag = "home_signal_item_${signal.pair}"
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SignalCard(
    signal: MarketSignal,
    onClick: () -> Unit,
    testTag: String? = null
) {
     Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2C3544))
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag(testTag ?: "")
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
                // Icon Circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(signal.iconColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = signal.iconLetter,
                        color = White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Info
                Column {
                    Text(
                        text = signal.pair,
                        color = White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = signal.type,
                        color = White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            // Right side: Price range and action
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${signal.priceFrom} → ${signal.priceTo}",
                    color = White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                // Action Badge - Hard-coded to always show text
                Box(
                    modifier = Modifier
                        .background(
                            color = when (signal.action) {
                                SignalAction.BUY -> Color(0xFF22C55E)
                                SignalAction.SELL -> Color(0xFFEF4444)
                                SignalAction.HOLD -> Color(0xFFFBBF24)
                            },
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (signal.action) {
                            SignalAction.BUY -> "Buy"
                            SignalAction.SELL -> "Sell"
                            SignalAction.HOLD -> "Hold"
                        },
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun TradeAnalysisSection(
    analyses: List<ChartAnalysis>,
    isLoading: Boolean,
    onViewAllClick: () -> Unit,
    onAnalysisClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Trade analysis",
                color = White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "View All",
                color = Green2,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onViewAllClick)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Analysis Cards
        when {
            isLoading -> {
                // Show loading indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Green2,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            analyses.isEmpty() -> {
                // Show empty state (shouldn't happen as we check before calling this)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No analysis available",
                        color = White.copy(alpha = 0.6f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            else -> {
                // Show analyses (top 3)
                analyses.take(3).forEach { analysis ->
                    AnalysisCardFromChartAnalysis(
                        analysis = analysis,
                        onClick = { onAnalysisClick(analysis.id) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun AnalysisCardFromChartAnalysis(
    analysis: ChartAnalysis,
    onClick: () -> Unit
) {
    // Extract data from ChartAnalysis
    // Use gameplan overview as title if asset is invalid
    val assetLower = analysis.asset.lowercase()
    val isValidAsset = analysis.asset.isNotEmpty() &&
        analysis.asset != "Unknown" &&
        !assetLower.contains("[object") &&
        !assetLower.contains("object]") &&
        !assetLower.contains("undefined") &&
        !assetLower.contains("null")

    val overviewLower = analysis.gameplan.overview.lowercase()
    val isValidOverview = analysis.gameplan.overview.isNotEmpty() &&
        analysis.gameplan.overview != "No analysis data available" &&
        !overviewLower.contains("[object") &&
        !overviewLower.contains("object]") &&
        !overviewLower.contains("undefined")

    val displayTitle = when {
        isValidAsset -> analysis.asset
        isValidOverview -> {
            // Extract first meaningful part of overview as title
            // Look for "Setup:" pattern or take first sentence
            val overview = analysis.gameplan.overview
            when {
                overview.contains("Setup:") -> {
                    overview.substringAfter("Setup:").trim().take(25).let {
                        if (it.length == 25) "$it..." else it
                    }
                }
                overview.contains(".") -> overview.substringBefore(".").take(30).trim()
                else -> overview.take(30).let { if (it.length == 30) "$it..." else it }
            }
        }
        else -> "Chart Analysis"
    }
    // Use type, or derive from trend if type is generic
    val type = when {
        analysis.type.isNotEmpty() && analysis.type != "Stock" && analysis.type != "Unknown" -> analysis.type
        analysis.keyInsights.trend.value.isNotEmpty() -> analysis.keyInsights.trend.value
        else -> "Analysis"
    }
    val confidencePercent = (analysis.confidenceScore * 10).roundToInt() // Convert 0-10 to 0-100

    // Determine action from key insights
    val action = analysis.keyInsights.trend.value.uppercase().let { trend ->
        when {
            trend.contains("BULLISH") || trend.contains("UP") -> "Buy"
            trend.contains("BEARISH") || trend.contains("DOWN") -> "Sell"
            else -> "Hold"
        }
    }

    // Get icon letter (first letter of displayTitle, skip common prefixes and invalid text)
    val iconLetter = displayTitle
        .removePrefix("Chart ")
        .removePrefix("Analysis ")
        .filter { it.isLetter() }
        .firstOrNull()?.uppercase() ?: "C"

    // Determine icon color based on action
    val iconColor = when (action) {
        "Buy" -> Color(0xFF22C55E) // Green
        "Sell" -> Color(0xFFEF4444) // Red
        else -> Color(0xFFFBBF24) // Yellow
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2C3544))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon Circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(iconColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = iconLetter,
                        color = White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Info
                Column {
                    Text(
                        text = displayTitle,
                        color = White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = type,
                        color = White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            // Right side: Confidence and action
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "$confidencePercent% confidence",
                    color = White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                // Badge with solid color background for better visibility
                val badgeBgColor = when (action) {
                    "Buy" -> Color(0xFF22C55E) // Solid green
                    "Sell" -> Color(0xFFEF4444) // Solid red
                    "Hold" -> Color(0xFFFBBF24) // Solid yellow
                    else -> Color(0xFFFBBF24)
                }
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .background(
                            color = badgeBgColor,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = action,
                        color = White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun AnalysisCard(
    analysis: TradeAnalysisView,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2C3544))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon Circle
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(analysis.iconColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = analysis.iconLetter,
                        color = White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Info
                Column {
                    Text(
                        text = analysis.pair,
                        color = White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = analysis.type,
                        color = White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            }

            // Right side: Confidence and action
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${analysis.confidence}% confidence",
                    color = White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when (analysis.action) {
                                "BUY" -> BullishGreen
                                "SELL" -> BearishRed
                                "HOLD" -> Warning
                                else -> Warning
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = analysis.action,
                        color = White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateSection(
    onScanClick: () -> Unit
) {
    // Animated arrow rotation
    val infiniteTransition = rememberInfiniteTransition()
    val arrowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Scan icon in circle
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color(0xFF2C3544)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QrCodeScanner,
                contentDescription = "Scan",
                tint = Green2,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title
        Text(
            text = "No trade analysis done",
            color = White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Description
        Text(
            text = "You have not analysed any trade. please click on the button below to start analysing your trade",
            color = White.copy(alpha = 0.7f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Dotted arrow (animated)
        Icon(
            imageVector = Icons.Default.ArrowDownward,
            contentDescription = "Arrow down",
            tint = White.copy(alpha = 0.3f),
            modifier = Modifier
                .size(32.dp)
                .rotate(arrowRotation)
        )
    }
}
