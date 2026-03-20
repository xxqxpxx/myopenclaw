package com.myopenclaw.ui.screens.subscription

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.myopenclaw.ui.theme.*

data class SubscriptionFeature(
    val title: String,
    val description: String
)

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val price: String,
    val description: String,
    val isBestDeal: Boolean = false,
    val savePercentage: String? = null,
    val priceDetails: String? = null
)

@Composable
fun SubscriptionPlansScreenNew(
    onContinue: (String) -> Unit = {},
    onClose: () -> Unit = {}
) {
    var selectedPlanId by remember { mutableStateOf("yearly") }

    val features = remember {
        listOf(
            SubscriptionFeature(
                "Unlimited AI Coding",
                "Write, debug, and optimize code in 50+ languages with Claude AI assistance."
            ),
            SubscriptionFeature(
                "Code Execution Sandbox",
                "Run code in secure cloud sandboxes directly from your conversations."
            ),
            SubscriptionFeature(
                "AI Code Reviews",
                "Get instant code reviews, refactoring suggestions, and best practice guidance."
            ),
            SubscriptionFeature(
                "File Generation",
                "Generate code files, CSVs, PDFs, and more — download or share instantly."
            )
        )
    }

    val plans = remember {
        listOf(
            SubscriptionPlan(
                id = "weekly",
                name = "Weekly plan",
                price = "$6.99 per week",
                description = "Payment in 3 days, Cancel anytime"
            ),
            SubscriptionPlan(
                id = "monthly",
                name = "Monthly plan",
                price = "$19.99 per month",
                description = "Billed monthly",
                priceDetails = "$19.99 per month. Billed monthly"
            ),
            SubscriptionPlan(
                id = "yearly",
                name = "Yearly access ($4.99/mo)",
                price = "$59.99 for the first year",
                description = "then $59.99 yearly",
                isBestDeal = true,
                savePercentage = "Save 80%",
                priceDetails = "$399.99 for the first year, then $59.99 Yearly"
            )
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackgroundDark
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(Dimensions.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
        ) {
            // Title
            item {
                Text(
                    text = "Upgrade to Pro, Get 3 Days Free, Then 80% Off Annual",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Features
            items(features) { feature ->
                FeatureRow(feature)
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Plans
            items(plans) { plan ->
                PlanCard(
                    plan = plan,
                    isSelected = plan.id == selectedPlanId,
                    onClick = { selectedPlanId = plan.id }
                )
            }

            // Selected plan details
            item {
                val selectedPlan = plans.find { it.id == selectedPlanId }
                if (selectedPlan != null && selectedPlan.priceDetails != null) {
                    Text(
                        text = selectedPlan.priceDetails!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Continue Button
            item {
                Button(
                    onClick = { onContinue(selectedPlanId) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    ),
                    shape = MaterialTheme.shapes.large,
                    contentPadding = PaddingValues(vertical = 18.dp)
                ) {
                    Text(
                        "Continue",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
            }
        }
    }
}

@Composable
fun FeatureRow(feature: SubscriptionFeature) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            color = Primary.copy(alpha = 0.2f),
            shape = RoundedCornerShape(50),
            modifier = Modifier.size(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                lineHeight = MaterialTheme.typography.bodySmall.lineHeight
            )
        }
    }
}

@Composable
fun PlanCard(
    plan: SubscriptionPlan,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = SurfaceDark,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) Primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimensions.paddingLarge),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = plan.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (plan.savePercentage != null) {
                        Surface(
                            color = Primary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = plan.savePercentage!!,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = BackgroundDark
                            )
                        }
                    }
                }

                Text(
                    text = plan.price,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Text(
                    text = plan.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Best Deal Badge
            if (plan.isBestDeal) {
                Surface(
                    color = Primary,
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "BEST DEAL",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BackgroundDark
                    )
                }
            }
        }
    }
}
