package com.myopenclaw.ui.screens.profile

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
import com.myopenclaw.ui.components.SWCard
import com.myopenclaw.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit = {},
    onSaveProfile: () -> Unit = {}
) {
    var name by remember { mutableStateOf("John Trader") }
    var email by remember { mutableStateOf("john.trader@example.com") }
    var phone by remember { mutableStateOf("+1 (555) 123-4567") }
    var bio by remember { mutableStateOf("Professional trader with 5+ years of experience in options and equity trading.") }
    var isLoading by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    // Track if form has changes
    val hasChanges = remember(name, email, phone, bio) {
        name != "John Trader" ||
        email != "john.trader@example.com" ||
        phone != "+1 (555) 123-4567" ||
        bio != "Professional trader with 5+ years of experience in options and equity trading."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Edit Profile",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (hasChanges) {
                            showDiscardDialog = true
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark
                )
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(Dimensions.paddingLarge),
            verticalArrangement = Arrangement.spacedBy(Dimensions.spacingLarge)
        ) {
            // Profile Picture
            item {
                SWCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.paddingLarge),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Dimensions.spacingMedium)
                    ) {
                        Box {
                            Surface(
                                color = Primary.copy(alpha = 0.2f),
                                shape = CircleShape,
                                modifier = Modifier.size(120.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(70.dp),
                                        tint = Primary
                                    )
                                }
                            }

                            Surface(
                                color = Primary,
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(36.dp)
                                    .align(Alignment.BottomEnd),
                                onClick = { /* Coming Soon: Upload photo */ }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.CameraAlt,
                                        contentDescription = "Change photo",
                                        tint = BackgroundDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        TextButton(onClick = { /* Coming Soon: Remove photo */ }) {
                            Text("Remove Photo", color = BearishRed)
                        }
                    }
                }
            }

            // Personal Information
            item {
                Text(
                    text = "Personal Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            // Name Field
            item {
                SWCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.paddingMedium)
                    ) {
                        Text(
                            text = "Full Name",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter your name") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                focusedContainerColor = BackgroundDark,
                                unfocusedContainerColor = BackgroundDark
                            ),
                            singleLine = true
                        )
                    }
                }
            }

            // Email Field
            item {
                SWCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.paddingMedium)
                    ) {
                        Text(
                            text = "Email Address",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter your email") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                focusedContainerColor = BackgroundDark,
                                unfocusedContainerColor = BackgroundDark
                            ),
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
                            )
                        )
                    }
                }
            }

            // Phone Field
            item {
                SWCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.paddingMedium)
                    ) {
                        Text(
                            text = "Phone Number",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter your phone") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                focusedContainerColor = BackgroundDark,
                                unfocusedContainerColor = BackgroundDark
                            ),
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone
                            )
                        )
                    }
                }
            }

            // Bio Field
            item {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            item {
                SWCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Dimensions.paddingMedium)
                    ) {
                        Text(
                            text = "Bio",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            placeholder = { Text("Tell us about yourself") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                focusedContainerColor = BackgroundDark,
                                unfocusedContainerColor = BackgroundDark
                            ),
                            maxLines = 6
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${bio.length}/500 characters",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }

            // Additional Information
            item {
                Text(
                    text = "Trading Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TradingPreferenceItem(
                        icon = Icons.Default.TrendingUp,
                        title = "Trading Experience",
                        value = "Intermediate",
                        onClick = { /* Coming Soon: Experience selector */ }
                    )

                    TradingPreferenceItem(
                        icon = Icons.Default.AccountBalance,
                        title = "Account Type",
                        value = "Individual Brokerage",
                        onClick = { /* Coming Soon: Account type selector */ }
                    )

                    TradingPreferenceItem(
                        icon = Icons.Default.Interests,
                        title = "Investment Interests",
                        value = "Options, Stocks, ETFs",
                        onClick = { /* Coming Soon: Interests selector */ }
                    )
                }
            }

            // Save Button
            item {
                Button(
                    onClick = {
                        isLoading = true
                        // Save profile
                        onSaveProfile()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = hasChanges && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        disabledContainerColor = Primary.copy(alpha = 0.3f)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = BackgroundDark,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        "Save Changes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Spacer for bottom padding
            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Discard Changes Dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard Changes?") },
            text = { Text("You have unsaved changes. Are you sure you want to go back?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = BearishRed
                    )
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun TradingPreferenceItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
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

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = value,
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
