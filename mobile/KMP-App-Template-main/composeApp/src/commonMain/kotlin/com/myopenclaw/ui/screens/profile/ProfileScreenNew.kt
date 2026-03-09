package com.myopenclaw.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myopenclaw.ui.theme.*
import com.myopenclaw.util.localizedString
import com.myopenclaw.util.StringKeys

/**
 * Profile Screen - Matches Figma design exactly (Node IDs: 1-6547, 1-6590)
 * Two states: Free subscriber (gray avatar) and Paid subscriber (green avatar)
 */
@Composable
fun ProfileScreenNew(
    isPaidSubscriber: Boolean = false,
    userName: String = "Blake Maddison",
    userEmail: String = "Blakemaddisoncornrow@gmail.com",
    joinedDate: String = "Joined Aug 24, 2025",
    billingInfo: String = if (isPaidSubscriber) "Next billing 24th april 2025" else "Billing starts in 2 days",
    appVersion: String = "1.15",
    currentLanguage: String = "English",
    onSubscriptionClick: () -> Unit = {},
    onRateAppClick: () -> Unit = {},
    onShareAppClick: () -> Unit = {},
    onChangeLanguageClick: () -> Unit = {},
    onChatClick: () -> Unit = {},
    onTermsClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onDeleteAccountClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Dark1)
            .statusBarsPadding()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp) // Space for bottom nav
        ) {
            // Profile Header
            item {
                ProfileHeader(
                    isPaidSubscriber = isPaidSubscriber,
                    userName = userName,
                    userEmail = userEmail,
                    joinedDate = joinedDate
                )
            }

            // Subscription Card
            item {
                SubscriptionCard(
                    isPaidSubscriber = isPaidSubscriber,
                    billingInfo = billingInfo,
                    currentLanguage = currentLanguage,
                    onClick = onSubscriptionClick
                )
            }

            // Account Status and Version
            item {
                AccountInfoSection(
                    accountStatus = if (isPaidSubscriber) 
                        localizedString(StringKeys.PROFILE_PREMIUM, currentLanguage) 
                    else 
                        localizedString(StringKeys.PROFILE_FREE, currentLanguage),
                    appVersion = appVersion,
                    currentLanguage = currentLanguage
                )
            }

            // Menu Items
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Rate App
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Star,
                    title = localizedString(StringKeys.PROFILE_RATE_APP, currentLanguage),
                    onClick = onRateAppClick
                )
            }

            // Share App
            item {
                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Filled.Send,
                    title = localizedString(StringKeys.PROFILE_SHARE_APP, currentLanguage),
                    onClick = onShareAppClick
                )
            }

            // Change Language
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Language,
                    title = localizedString(StringKeys.PROFILE_CHANGE_LANGUAGE, currentLanguage),
                    trailingText = currentLanguage,
                    onClick = onChangeLanguageClick
                )
            }

            // Chat with us
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Chat,
                    title = localizedString(StringKeys.PROFILE_CHAT_WITH_US, currentLanguage),
                    onClick = onChatClick
                )
            }

            // Terms and conditions
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Description,
                    title = localizedString(StringKeys.PROFILE_TERMS_AND_CONDITIONS, currentLanguage),
                    onClick = onTermsClick
                )
            }

            // Privacy policy
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Description,
                    title = localizedString(StringKeys.PROFILE_PRIVACY_POLICY, currentLanguage),
                    onClick = onPrivacyClick
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Sign out
            item {
                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    title = localizedString(StringKeys.PROFILE_SIGNOUT, currentLanguage),
                    onClick = onSignOutClick,
                    testTag = "profile_signout_button"
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Delete account
            item {
                ProfileMenuItem(
                    icon = Icons.Default.Delete,
                    title = localizedString(StringKeys.PROFILE_DELETE_ACCOUNT, currentLanguage),
                    iconTint = BearishRed,
                    onClick = onDeleteAccountClick
                )
            }
        }
    }
}

/**
 * Profile header with avatar, name, email, and joined date
 */
@Composable
private fun ProfileHeader(
    isPaidSubscriber: Boolean,
    userName: String,
    userEmail: String,
    joinedDate: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar with initials
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = if (isPaidSubscriber) Green2 else Color(0xFF5A6372),
                    shape = RoundedCornerShape(20.dp)
                )
                .testTag("profile_avatar"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = getInitials(userName),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // User Name
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            ),
            color = White,
            modifier = Modifier.testTag("profile_user_name")
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Email
        Text(
            text = userEmail,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 15.sp,
                fontWeight = FontWeight.Normal
            ),
            color = White.copy(alpha = 0.7f),
            modifier = Modifier.testTag("profile_user_email")
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Joined Date
        Text(
            text = joinedDate,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            ),
            color = White.copy(alpha = 0.6f)
        )
    }
}

/**
 * Subscription status card
 */
@Composable
private fun SubscriptionCard(
    isPaidSubscriber: Boolean,
    billingInfo: String,
    currentLanguage: String = "English",
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp)
            .testTag("profile_subscription_status"),
        color = InputBackground,
        shape = RoundedCornerShape(16.dp),
        border = if (isPaidSubscriber) {
            androidx.compose.foundation.BorderStroke(2.dp, Green2)
        } else {
            null
        }
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
                            color = if (isPaidSubscriber) Green2.copy(alpha = 0.2f) else White.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = if (isPaidSubscriber) Green2 else White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Text
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = if (isPaidSubscriber) 
                            localizedString(StringKeys.PROFILE_PAID_SUBSCRIBER, currentLanguage) 
                        else 
                            localizedString(StringKeys.PROFILE_FREE_SUBSCRIBER, currentLanguage),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isPaidSubscriber) Green2 else White
                    )
                    Text(
                        text = billingInfo,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = if (isPaidSubscriber) Green2.copy(alpha = 0.8f) else White.copy(alpha = 0.7f)
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
 * Account status and version info section
 */
@Composable
private fun AccountInfoSection(
    accountStatus: String,
    appVersion: String,
    currentLanguage: String = "English"
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 24.dp)
    ) {
        // Account Status Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = localizedString(StringKeys.PROFILE_ACCOUNT_STATUS, currentLanguage),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = White
                )
            }
            Text(
                text = accountStatus,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = White.copy(alpha = 0.7f)
            )
        }

        // Version Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "A",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = White
                )
                Text(
                    text = localizedString(StringKeys.PROFILE_VERSION, currentLanguage),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = White
                )
            }
            Text(
                text = appVersion,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = White.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Individual menu item
 */
@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    trailingText: String? = null,
    iconTint: Color = White,
    onClick: () -> Unit,
    testTag: String? = null
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(vertical = 4.dp)
            .then(testTag?.let { Modifier.testTag(it) } ?: Modifier),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = White
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (trailingText != null) {
                    Text(
                        text = trailingText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal
                        ),
                        color = White.copy(alpha = 0.7f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = White.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Get initials from full name
 */
private fun getInitials(fullName: String): String {
    val words = fullName.trim().split(" ")
    return when {
        words.isEmpty() -> ""
        words.size == 1 -> words[0].take(1).uppercase()
        else -> "${words[0].take(1)}${words[1].take(1)}".uppercase()
    }
}
