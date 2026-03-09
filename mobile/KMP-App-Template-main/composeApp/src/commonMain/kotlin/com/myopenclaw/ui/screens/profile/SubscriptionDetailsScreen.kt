package com.myopenclaw.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myopenclaw.ui.theme.*

/**
 * Subscription Details Screen - Matches Figma design exactly (Node ID: 1-6633)
 * Shows detailed subscription information and upgrade button
 */
@Composable
fun SubscriptionDetailsScreen(
    fullName: String = "Blake Maddison",
    emailAddress: String = "Blakemad@gmail.co..",
    nextBillingDate: String = "Jul 17, 2025",
    subscriptionRenews: String = "Every week",
    billingInfo: String = "Next billing 24th april 2025",
    onUpgradeClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Dark1)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp) // Space for bottom nav
        ) {
            // Subscription Status Card
            item {
                PaidSubscriptionCard(
                    billingInfo = billingInfo
                )
            }

            // Subscription Details Section
            item {
                SubscriptionDetailsSection(
                    fullName = fullName,
                    emailAddress = emailAddress,
                    nextBillingDate = nextBillingDate,
                    subscriptionRenews = subscriptionRenews
                )
            }

            // Upgrade Button
            item {
                UpgradeButton(
                    onClick = onUpgradeClick
                )
            }
        }
    }
}

/**
 * Paid subscription status card (fixed at top)
 */
@Composable
private fun PaidSubscriptionCard(
    billingInfo: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 16.dp),
        color = InputBackground,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, Green2)
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
                // Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = Green2.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = Green2,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Text
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Paid subscriber",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Green2
                    )
                    Text(
                        text = billingInfo,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = Green2.copy(alpha = 0.8f)
                    )
                }
            }

            // Chevron
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = White.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Subscription details section with 4 rows
 */
@Composable
private fun SubscriptionDetailsSection(
    fullName: String,
    emailAddress: String,
    nextBillingDate: String,
    subscriptionRenews: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp)
    ) {
        // Full name
        SubscriptionDetailRow(
            label = "Full name",
            value = fullName
        )

        // Email address
        SubscriptionDetailRow(
            label = "Email address",
            value = emailAddress
        )

        // Next billing date
        SubscriptionDetailRow(
            label = "Next billing date",
            value = nextBillingDate
        )

        // Subscription renews
        SubscriptionDetailRow(
            label = "Subscription renews",
            value = subscriptionRenews
        )
    }
}

/**
 * Individual detail row
 */
@Composable
private fun SubscriptionDetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            ),
            color = White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            ),
            color = White
        )
    }
}

/**
 * Upgrade subscription button
 */
@Composable
private fun UpgradeButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 32.dp)
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Green2
        ),
        shape = CircleShape
    ) {
        Text(
            text = "Upgrade subscription",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = White
        )
    }
}
