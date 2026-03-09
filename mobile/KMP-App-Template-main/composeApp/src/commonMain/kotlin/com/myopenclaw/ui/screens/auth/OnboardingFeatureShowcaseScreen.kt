package com.myopenclaw.ui.screens.auth

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myopenclaw.ui.theme.*

enum class FeatureShowcaseSlide {
    AI_CODING,
    SMART_TOOLS,
    AI_ASSISTANT
}

@Composable
fun OnboardingFeatureShowcaseScreen(
    currentSlide: FeatureShowcaseSlide = FeatureShowcaseSlide.AI_CODING,
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
                    FeatureShowcaseSlide.AI_CODING -> AICodingSlide()
                    FeatureShowcaseSlide.SMART_TOOLS -> SmartToolsSlide()
                    FeatureShowcaseSlide.AI_ASSISTANT -> AIAssistantSlide()
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Continue Button (fixed at bottom, pill-shaped)
            Button(
                onClick = {
                    when (slide) {
                        FeatureShowcaseSlide.AI_CODING -> slide = FeatureShowcaseSlide.SMART_TOOLS
                        FeatureShowcaseSlide.SMART_TOOLS -> slide = FeatureShowcaseSlide.AI_ASSISTANT
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
fun AICodingSlide() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Code block preview
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A2332),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Write a Python function to sort a list",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // AI Response
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1A2332),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Here's an efficient sorting function:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF0D1117),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "def quick_sort(arr):\n    if len(arr) <= 1:\n        return arr\n    pivot = arr[0]\n    left = [x for x in arr[1:] if x <= pivot]\n    right = [x for x in arr[1:] if x > pivot]\n    return quick_sort(left) + [pivot] + quick_sort(right)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7EE787),
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InsightCard(
                icon = Icons.Default.Code,
                label = "Languages",
                value = "50+",
                modifier = Modifier.weight(1f),
                iconColor = Primary
            )
            InsightCard(
                icon = Icons.Default.Speed,
                label = "Response",
                value = "Instant",
                modifier = Modifier.weight(1f),
                iconColor = BullishGreen
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "AI-Powered Coding Assistant",
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Write, debug, and optimize code with AI assistance",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun SmartToolsSlide() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        // Tool execution card
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        color = Primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Build,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "Code Execution",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Running Python script...",
                            style = MaterialTheme.typography.bodySmall,
                            color = BullishGreen
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Results Card
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
                    icon = Icons.Default.Terminal,
                    label = "Tool",
                    value = "code_execute",
                    valueColor = Primary
                )
                DetailRow(
                    icon = Icons.Default.CheckCircle,
                    label = "Status",
                    value = "Success",
                    valueColor = BullishGreen
                )
                DetailRow(
                    icon = Icons.Default.Timer,
                    label = "Runtime",
                    value = "0.3s",
                    valueColor = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Capabilities
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
                    icon = Icons.Default.Code,
                    label = "Execute code",
                    value = "Python, JS",
                    valueColor = Color.White
                )
                DetailRow(
                    icon = Icons.Default.Search,
                    label = "Web search",
                    value = "Real-time",
                    valueColor = Color.White
                )
                DetailRow(
                    icon = Icons.Default.Description,
                    label = "File generation",
                    value = "PDF, CSV",
                    valueColor = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Smart Tool Execution",
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Run code, search the web, and generate files — all in chat",
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

        // User Message Bubble
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
                    text = "Help me debug this API timeout issue",
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
                    text = "I see a few potential causes. Let me walk you through the most common ones and how to fix them:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 20.sp
                )

                Text(
                    text = "Common causes:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "• Missing timeout configuration",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "• Connection pool exhaustion",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "• Unhandled retry logic",
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
                    text = "Show me the retry logic fix",
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
            text = "Your personal AI assistant, always available",
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
