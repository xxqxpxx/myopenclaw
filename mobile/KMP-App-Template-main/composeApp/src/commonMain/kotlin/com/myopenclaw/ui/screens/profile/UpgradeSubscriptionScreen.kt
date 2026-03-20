package com.myopenclaw.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myopenclaw.ui.theme.*

/**
 * Subscription plan options
 */
enum class SubscriptionPlan {
    WEEKLY, MONTHLY, YEARLY
}

/**
 * Upgrade Subscription Screen - Matches Figma design exactly (Node IDs: 1-8917, 1-8949)
 * Shows pricing options and premium features
 */
@Composable
fun UpgradeSubscriptionScreen(
    selectedPlan: SubscriptionPlan? = null,
    onPlanSelected: (SubscriptionPlan) -> Unit = {},
    onContinueClick: () -> Unit = {}
) {
    var currentSelectedPlan by remember { mutableStateOf(selectedPlan) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Dark1)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 32.dp, horizontal = 16.dp)
        ) {
            // Title
            item {
                Text(
                    text = "Upgrade to Pro, Get 3 Days Free, Then 80% Off Annual",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp
                    ),
                    color = White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Features List
            item {
                PremiumFeaturesList()
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Weekly Plan
            item {
                PricingPlanCard(
                    plan = SubscriptionPlan.WEEKLY,
                    title = "Weekly plan",
                    subtitle = "\$6.99 per week. Payment in 3 days, Cancel anytime",
                    isSelected = currentSelectedPlan == SubscriptionPlan.WEEKLY,
                    onClick = {
                        currentSelectedPlan = SubscriptionPlan.WEEKLY
                        onPlanSelected(SubscriptionPlan.WEEKLY)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Monthly Plan
            item {
                PricingPlanCard(
                    plan = SubscriptionPlan.MONTHLY,
                    title = "Monthly plan",
                    subtitle = "\$19.99 per month. Billed monthly",
                    isSelected = currentSelectedPlan == SubscriptionPlan.MONTHLY,
                    onClick = {
                        currentSelectedPlan = SubscriptionPlan.MONTHLY
                        onPlanSelected(SubscriptionPlan.MONTHLY)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Yearly Plan (Best Deal)
            item {
                PricingPlanCard(
                    plan = SubscriptionPlan.YEARLY,
                    title = "Yearly access (\$4.99/mo)",
                    subtitle = "\$59.99 for the first year, then \$59.99 yearly",
                    badge = "BEST DEAL",
                    badgeColor = Green2,
                    discount = "Save 80%",
                    isSelected = currentSelectedPlan == SubscriptionPlan.YEARLY,
                    onClick = {
                        currentSelectedPlan = SubscriptionPlan.YEARLY
                        onPlanSelected(SubscriptionPlan.YEARLY)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Bottom Price Text
            item {
                Text(
                    text = when (currentSelectedPlan) {
                        SubscriptionPlan.MONTHLY -> "\$19.99 per month. Billed monthly"
                        SubscriptionPlan.YEARLY -> "\$59.99 for the first year, then \$59.99 yearly"
                        else -> "\$399.99 for the first year, then \$59.99 Yearly"
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Continue Button
            item {
                Button(
                    onClick = onContinueClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green2
                    ),
                    shape = CircleShape
                ) {
                    Text(
                        text = "Continue",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = White
                    )
                }
            }
        }
    }
}

/**
 * Premium features list with checkmarks
 */
@Composable
private fun PremiumFeaturesList() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PremiumFeatureItem(
            title = "Unlimited AI Coding",
            description = "Write, debug, and optimize code in 50+ languages with Claude AI assistance."
        )

        PremiumFeatureItem(
            title = "Code Execution Sandbox",
            description = "Run code in secure cloud sandboxes directly from your conversations."
        )

        PremiumFeatureItem(
            title = "AI Code Reviews",
            description = "Get instant code reviews, refactoring suggestions, and best practice guidance."
        )

        PremiumFeatureItem(
            title = "File Generation",
            description = "Get notified the moment key levels break or signals trigger on your watchlist."
        )
    }
}

/**
 * Individual premium feature item with checkmark
 */
@Composable
private fun PremiumFeatureItem(
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Checkmark Icon
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Green2,
            modifier = Modifier.size(24.dp)
        )

        // Text
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = White
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    lineHeight = 20.sp
                ),
                color = White.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Pricing plan card
 */
@Composable
private fun PricingPlanCard(
    plan: SubscriptionPlan,
    title: String,
    subtitle: String,
    badge: String? = null,
    badgeColor: Color? = null,
    discount: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = if (isSelected) Green2 else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
        color = InputBackground,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Badge (Top-left)
            if (badge != null && badgeColor != null) {
                Surface(
                    color = badgeColor,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-8).dp, y = (-8).dp)
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Discount Badge (Top-right)
            if (discount != null) {
                Surface(
                    color = Green2,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 8.dp, y = (-8).dp)
                ) {
                    Text(
                        text = discount,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (badge != null) 12.dp else 0.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
