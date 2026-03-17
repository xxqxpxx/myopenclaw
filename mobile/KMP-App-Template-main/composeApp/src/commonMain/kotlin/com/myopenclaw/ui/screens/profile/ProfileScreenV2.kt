package com.myopenclaw.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.myopenclaw.ui.components.SWCard
import com.myopenclaw.ui.theme.*

data class UserProfile(
    val name: String,
    val email: String,
    val initials: String,
    val joinedDate: String,
    val isPaidSubscriber: Boolean,
    val subscriptionMessage: String,
    val accountStatus: String,
    val appVersion: String
)

@Composable
fun ProfileScreenV2(
    onNavigateToSubscriptionDetails: () -> Unit = {},
    onNavigateToUpgrade: () -> Unit = {},
    onRateApp: () -> Unit = {},
    onShareApp: () -> Unit = {},
    onChangeLanguage: () -> Unit = {},
    onChatWithUs: () -> Unit = {},
    onTermsAndConditions: () -> Unit = {},
    onPrivacyPolicy: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onDeleteAccount: () -> Unit = {}
) {
    val userProfile = remember {
        UserProfile(
            name = "Blake Maddison",
            email = "Blakemaddisoncornrow@gmail.com",
            initials = "BM",
            joinedDate = "Joined Aug 24, 2025",
            isPaidSubscriber = false, // Change to true to see paid subscriber view
            subscriptionMessage = if (false) "Next billing 24th april 2025" else "Billing starts in 2 days",
            accountStatus = "Free",
            appVersion = "1.15"
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp) // Account for bottom nav
    ) {
        // Profile Header
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = Dimensions.spacingXLarge),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Avatar
                Surface(
                    color = if (userProfile.isPaidSubscriber) Primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.size(100.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = userProfile.initials,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (userProfile.isPaidSubscriber) BackgroundDark else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

                // Name
                Text(
                    text = userProfile.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Email
                Text(
                    text = userProfile.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Joined date
                Text(
                    text = userProfile.joinedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }

        // Subscription Status Card
        item {
            SWCard(
                onClick = if (userProfile.isPaidSubscriber) onNavigateToSubscriptionDetails else onNavigateToUpgrade,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimensions.paddingLarge, vertical = 8.dp),
                border = if (userProfile.isPaidSubscriber) BorderStroke(1.dp, Primary) else null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimensions.paddingLarge),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = if (userProfile.isPaidSubscriber) Primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    if (userProfile.isPaidSubscriber) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (userProfile.isPaidSubscriber) Primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (userProfile.isPaidSubscriber) "Paid subscriber" else "Free subscriber",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (userProfile.isPaidSubscriber) Primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = userProfile.subscriptionMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        }

        // Account Information
        item {
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(
                icon = Icons.Default.CheckCircle,
                label = "Account status",
                value = userProfile.accountStatus
            )
            InfoRow(
                icon = Icons.Default.Apps,
                label = "Version",
                value = userProfile.appVersion
            )
        }

        // Menu Items
        item {
            Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
            MenuItemCard(
                icon = Icons.Default.Star,
                title = "Rate my openClaw 5 stars",
                onClick = onRateApp
            )
            MenuItemCard(
                icon = Icons.Default.Share,
                title = "Share my openClaw app",
                onClick = onShareApp
            )
            MenuItemCard(
                icon = Icons.Default.Language,
                title = "Change language",
                value = "English",
                onClick = onChangeLanguage
            )
            MenuItemCard(
                icon = Icons.Default.Chat,
                title = "Chat with us",
                onClick = onChatWithUs
            )
            MenuItemCard(
                icon = Icons.Default.Description,
                title = "Terms and conditions",
                onClick = onTermsAndConditions
            )
            MenuItemCard(
                icon = Icons.Default.PrivacyTip,
                title = "Privacy policy",
                onClick = onPrivacyPolicy
            )
        }

        // Danger Zone
        item {
            Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
            MenuItemCard(
                icon = Icons.Default.Logout,
                title = "Signout",
                onClick = onSignOut,
                isDanger = false
            )
            MenuItemCard(
                icon = Icons.Default.Delete,
                title = "Delete account",
                onClick = onDeleteAccount,
                isDanger = true
            )
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(Dimensions.spacingLarge))
        }
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    SWCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.paddingLarge, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun MenuItemCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String? = null,
    onClick: () -> Unit,
    isDanger: Boolean = false
) {
    SWCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimensions.paddingLarge, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingLarge),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = if (isDanger) BearishRed.copy(alpha = 0.1f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (isDanger) BearishRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isDanger) BearishRed else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (value != null) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
        }
    }
}
