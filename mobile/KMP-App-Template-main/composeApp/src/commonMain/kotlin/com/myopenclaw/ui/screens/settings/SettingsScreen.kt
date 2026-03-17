package com.myopenclaw.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.myopenclaw.data.local.PreferencesManager
import com.myopenclaw.ui.components.SWCard
import com.myopenclaw.ui.theme.*
import com.myopenclaw.util.UrlLauncher
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

// App URLs
private object AppUrls {
    const val PRIVACY_POLICY = "https://myopenclaw.com/privacy"
    const val TERMS_OF_SERVICE = "https://myopenclaw.com/terms"
    const val HELP_CENTER = "https://myopenclaw.com/help"
    const val SUPPORT_EMAIL = "support@myopenclaw.com"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToSubscription: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    urlLauncher: UrlLauncher = koinInject(),
    preferencesManager: PreferencesManager = koinInject()
) {
    var darkModeEnabled by remember { mutableStateOf(true) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var biometricsEnabled by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }
    var showLicensesDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Clear cache confirmation dialog
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            title = { Text("Clear Cache") },
            text = { Text("This will clear all cached data. You may need to download some content again.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearCacheDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar("Cache cleared successfully")
                        }
                    }
                ) {
                    Text("Clear", color = BearishRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("Cancel")
                }
            },
            containerColor = SurfaceDark
        )
    }

    // Licenses dialog
    if (showLicensesDialog) {
        AlertDialog(
            onDismissRequest = { showLicensesDialog = false },
            title = { Text("Open Source Licenses") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("This app uses the following open source libraries:")
                    Text("- Kotlin Multiplatform", style = MaterialTheme.typography.bodySmall)
                    Text("- Jetpack Compose", style = MaterialTheme.typography.bodySmall)
                    Text("- Ktor", style = MaterialTheme.typography.bodySmall)
                    Text("- Koin", style = MaterialTheme.typography.bodySmall)
                    Text("- Supabase SDK", style = MaterialTheme.typography.bodySmall)
                    Text("- kotlinx.serialization", style = MaterialTheme.typography.bodySmall)
                    Text("- kotlinx.datetime", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { showLicensesDialog = false }) {
                    Text("Close")
                }
            },
            containerColor = SurfaceDark
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(Dimensions.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
        ) {
            // Account Section
            item {
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsMenuItem(
                        icon = Icons.Default.Person,
                        title = "Edit Profile",
                        subtitle = "Update your personal information",
                        onClick = onNavigateToEditProfile
                    )

                    SettingsMenuItem(
                        icon = Icons.Default.WorkspacePremium,
                        title = "Subscription",
                        subtitle = "Manage your premium subscription",
                        onClick = onNavigateToSubscription,
                        showBadge = true
                    )

                    SettingsMenuItem(
                        icon = Icons.Default.Password,
                        title = "Change Password",
                        subtitle = "Update your account password",
                        onClick = onNavigateToChangePassword
                    )
                }
            }

            // Preferences Section
            item {
                Text(
                    text = "Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsSwitchItem(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Mode",
                        subtitle = "Use dark theme across the app",
                        checked = darkModeEnabled,
                        onCheckedChange = { darkModeEnabled = it }
                    )

                    SettingsSwitchItem(
                        icon = Icons.Default.Notifications,
                        title = "Push Notifications",
                        subtitle = "Receive alerts and updates",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )

                    SettingsMenuItem(
                        icon = Icons.Default.NotificationsActive,
                        title = "Notification Settings",
                        subtitle = "Customize notification preferences",
                        onClick = onNavigateToNotifications
                    )

                    SettingsMenuItem(
                        icon = Icons.Default.Language,
                        title = "Language",
                        subtitle = "English (US)",
                        onClick = { showLanguageDialog = true }
                    )
                }
            }

            // Security Section
            item {
                Text(
                    text = "Security",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsSwitchItem(
                        icon = Icons.Default.Fingerprint,
                        title = "Biometric Authentication",
                        subtitle = "Use fingerprint or face ID",
                        checked = biometricsEnabled,
                        onCheckedChange = { biometricsEnabled = it }
                    )

                    SettingsMenuItem(
                        icon = Icons.Default.Security,
                        title = "Privacy & Security",
                        subtitle = "Manage your privacy settings",
                        onClick = onNavigateToPrivacy
                    )

                    SettingsMenuItem(
                        icon = Icons.Default.VerifiedUser,
                        title = "Two-Factor Authentication",
                        subtitle = "Add an extra layer of security",
                        onClick = { /* Coming Soon: 2FA setup */ }
                    )
                }
            }

            // Data & Storage Section
            item {
                Text(
                    text = "Data & Storage",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsMenuItem(
                        icon = Icons.Default.Storage,
                        title = "Data Usage",
                        subtitle = "Monitor app data consumption",
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Data usage feature coming soon")
                            }
                        }
                    )

                    SettingsMenuItem(
                        icon = Icons.Default.CloudDownload,
                        title = "Download Settings",
                        subtitle = "Manage offline data storage",
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Download settings coming soon")
                            }
                        }
                    )

                    SettingsMenuItem(
                        icon = Icons.Default.Delete,
                        title = "Clear Cache",
                        subtitle = "Free up storage space",
                        onClick = { showClearCacheDialog = true }
                    )
                }
            }

            // Support Section
            item {
                Text(
                    text = "Support",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsMenuItem(
                        icon = Icons.Default.Help,
                        title = "Help Center",
                        subtitle = "FAQs and tutorials",
                        onClick = { urlLauncher.openUrl(AppUrls.HELP_CENTER) }
                    )

                    SettingsMenuItem(
                        icon = Icons.Default.ContactSupport,
                        title = "Contact Support",
                        subtitle = "Get help from our team",
                        onClick = {
                            urlLauncher.openEmail(
                                email = AppUrls.SUPPORT_EMAIL,
                                subject = "myOpenClaw Support Request"
                            )
                        }
                    )

                    SettingsMenuItem(
                        icon = Icons.Default.BugReport,
                        title = "Report a Bug",
                        subtitle = "Help us improve the app",
                        onClick = {
                            urlLauncher.openEmail(
                                email = AppUrls.SUPPORT_EMAIL,
                                subject = "Bug Report - myOpenClaw",
                                body = "Please describe the bug:\n\nSteps to reproduce:\n\nExpected behavior:\n\nActual behavior:\n\nDevice info:\n"
                            )
                        }
                    )

                    SettingsMenuItem(
                        icon = Icons.Default.RateReview,
                        title = "Rate the App",
                        subtitle = "Share your feedback",
                        onClick = { urlLauncher.openAppStore() }
                    )
                }
            }

            // About Section
            item {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsMenuItem(
                        icon = Icons.Default.Info,
                        title = "About my openClaw",
                        subtitle = "Learn more about us",
                        onClick = onNavigateToAbout
                    )

                    SettingsMenuItem(
                        icon = Icons.Default.Policy,
                        title = "Privacy Policy",
                        subtitle = "How we protect your data",
                        onClick = { urlLauncher.openUrl(AppUrls.PRIVACY_POLICY) }
                    )

                    SettingsMenuItem(
                        icon = Icons.Default.Gavel,
                        title = "Terms of Service",
                        subtitle = "User agreement and terms",
                        onClick = { urlLauncher.openUrl(AppUrls.TERMS_OF_SERVICE) }
                    )

                    SettingsMenuItem(
                        icon = Icons.Default.Code,
                        title = "Open Source Licenses",
                        subtitle = "Third-party libraries",
                        onClick = { showLicensesDialog = true }
                    )
                }
            }

            // App Version
            item {
                SWCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.paddingMedium),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "my openClaw",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Version 1.0.0 (Build 100)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "© 2024 myOpenClaw",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }

            // Spacer for bottom padding
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    if (showLanguageDialog) {
        com.myopenclaw.ui.components.LanguageSelectionDialog(
            currentLanguage = "English",
            onLanguageSelected = { /* save preference */ },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
fun SettingsMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showBadge: Boolean = false
) {
    SWCard(onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimensions.paddingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Primary.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (showBadge) {
                            Surface(
                                color = PremiumGold.copy(alpha = 0.2f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "PRO",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PremiumGold
                                )
                            }
                        }
                    }
                    Text(
                        text = subtitle,
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

@Composable
fun SettingsSwitchItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SWCard {
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
                Surface(
                    color = Primary.copy(alpha = 0.2f),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BackgroundDark,
                    checkedTrackColor = Primary,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    uncheckedTrackColor = SurfaceDark
                )
            )
        }
    }
}
