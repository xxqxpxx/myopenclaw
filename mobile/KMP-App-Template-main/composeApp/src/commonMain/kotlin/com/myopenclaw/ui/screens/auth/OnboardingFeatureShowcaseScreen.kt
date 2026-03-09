package com.myopenclaw.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myopenclaw.ui.theme.*
import myopenclaw.composeapp.generated.resources.Res
import myopenclaw.composeapp.generated.resources.ethusd
import org.jetbrains.compose.resources.painterResource

enum class FeatureShowcaseSlide {
    CHART_ANALYSIS,
    MARKET_SIGNALS,
    AI_ASSISTANT
}

@Composable
fun OnboardingFeatureShowcaseScreen(
    currentSlide: FeatureShowcaseSlide = FeatureShowcaseSlide.CHART_ANALYSIS,
    onContinue: () -> Unit = {}
) {
    var slide by remember { mutableStateOf(currentSlide) }
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Dark1
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress bar (fixed at top)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 48.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeatureShowcaseSlide.values().forEachIndexed { index, slideType ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(
                                color = if (slideType.ordinal <= slide.ordinal) Green2 else Color.White.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            }

            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (slide) {
                    FeatureShowcaseSlide.CHART_ANALYSIS -> ChartAnalysisSlide()
                    FeatureShowcaseSlide.MARKET_SIGNALS -> MarketSignalsSlide()
                    FeatureShowcaseSlide.AI_ASSISTANT -> AIAssistantSlide()
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Continue Button (fixed at bottom, pill-shaped)
            Button(
                onClick = {
                    when (slide) {
                        FeatureShowcaseSlide.CHART_ANALYSIS -> slide = FeatureShowcaseSlide.MARKET_SIGNALS
                        FeatureShowcaseSlide.MARKET_SIGNALS -> slide = FeatureShowcaseSlide.AI_ASSISTANT
                        FeatureShowcaseSlide.AI_ASSISTANT -> onContinue()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 0.dp)
                    .testTag("feature_showcase_continue_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green2
                ),
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Text(
                    "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun ChartAnalysisSlide() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Chart preview with ETH/USD image
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            color = Color(0xFF1A2332),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // ETH/USD chart image
                Image(
                    painter = painterResource(Res.drawable.ethusd),
                    contentDescription = "ETH/USD Chart",
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.Center),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Signal Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A2332),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Primary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Coin icon
                    Surface(
                        modifier = Modifier.size(40.dp),
                        color = Color(0xFFFF9800), // Orange
                        shape = RoundedCornerShape(50)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "E",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "ETH/USD",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Crypto",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "BUY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BullishGreen
                    )
                    Text(
                        text = "75% confidence",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Key Insights Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InsightCard(
                icon = Icons.Default.TrendingUp,
                label = "Trend",
                value = "Bearish",
                modifier = Modifier.weight(1f),
                iconColor = BearishRed
            )
            InsightCard(
                icon = Icons.Default.Warning,
                label = "Trend",
                value = "Bearish",
                modifier = Modifier.weight(1f),
                iconColor = Color(0xFFFFA500)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InsightCard(
                icon = Icons.Default.Shield,
                label = "Risk level",
                value = "Medium",
                modifier = Modifier.weight(1f),
                iconColor = Primary
            )
            InsightCard(
                icon = Icons.Default.BarChart,
                label = "Volume",
                value = "Medium",
                modifier = Modifier.weight(1f),
                iconColor = Primary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title and Description
        Text(
            text = "Snap & Analyze Trade charts",
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Take a photo any chart, get instant insights on that chart",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun MarketSignalsSlide() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Signal Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A2332),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Primary)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            color = Primary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "A",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Primary
                                )
                            }
                        }

                        Column {
                            Text(
                                text = "AUD / USD",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Currency",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$5 → $34",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "BUY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = BullishGreen
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Details Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A2332),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(
                    icon = Icons.Default.TrendingUp,
                    label = "Stock signal",
                    value = "Closed win",
                    valueColor = BullishGreen
                )
                DetailRow(
                    icon = Icons.Default.AttachMoney,
                    label = "Entry price",
                    value = "$602.43",
                    valueColor = Color.White
                )
                DetailRow(
                    icon = Icons.Default.AttachMoney,
                    label = "Closing price",
                    value = "$602.43",
                    valueColor = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // P/L Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A2332),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DetailRow(
                    icon = Icons.Default.TrendingUp,
                    label = "Total P/L",
                    value = "+$9.20",
                    valueColor = BullishGreen
                )
                DetailRow(
                    icon = Icons.Default.TrendingUp,
                    label = "% Return",
                    value = "+1.53%",
                    valueColor = BullishGreen
                )
                DetailRow(
                    icon = Icons.Default.GpsFixed,
                    label = "Take profit",
                    value = "+1.53%",
                    valueColor = BullishGreen
                )
                DetailRow(
                    icon = Icons.Default.Warning,
                    label = "Stop loss",
                    value = "$576.53",
                    valueColor = BearishRed
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title and Description
        Text(
            text = "Morning Market Signals",
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Know exactly what to buy, hold, or sell today",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun AIAssistantSlide() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Chart Image Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            color = Color(0xFF1A2332),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // ETH/USD chart image
                Image(
                    painter = painterResource(Res.drawable.ethusd),
                    contentDescription = "ETH/USD Chart",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // AI Message Bubble
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Primary.copy(alpha = 0.15f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Give me a detailed chart analysis on this",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Response Bubble
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A2332),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "You've got a downtrend that's losing momentum. Notice how the price dropped sharply, then formed a potential double bottom around \$[price]? That's buyers stepping in.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 20.sp
                )

                Text(
                    text = "The key right now:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "• Strong support at \$234.4",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "• Resistance around \$832.6",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "• Volume is picking up",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Follow-up Question
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A2332),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Primary)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Explain why volume is picking up",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White
                )
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title and Description
        Text(
            text = "24/7 AI Assistant",
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your personal trading expert, always available",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun InsightCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    iconColor: Color = Primary
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF1A2332),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
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
                icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}
