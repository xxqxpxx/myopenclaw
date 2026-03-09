package com.myopenclaw.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.myopenclaw.ui.components.*
import com.myopenclaw.ui.theme.Dimensions
import com.myopenclaw.ui.viewmodel.auth.ForgotPasswordViewModel
import com.myopenclaw.ui.viewmodel.auth.PasswordResetState
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    
    val state by viewModel.state.collectAsState()
    
    val isLoading = state is PasswordResetState.Loading
    val emailSent = state is PasswordResetState.Success
    val errorState = state as? PasswordResetState.Error
    val errorMessage = errorState?.message

    // Show error dialog if there's an error
    errorState?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.resetState() },
            title = { Text("Failed to Send Reset Email") },
            text = { Text(error.message) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetState() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reset Password",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("forgot_password_back_link")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimensions.paddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Dimensions.spacingXLarge))

            if (emailSent) {
                // Success State
                Column(
                    modifier = Modifier.testTag("forgot_password_success_message"),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        modifier = Modifier.size(80.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

                    Text(
                        text = "Check Your Email",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

                Text(
                    text = "We've sent password reset instructions to:\n$email",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

                Text(
                    text = "Please check your inbox and follow the instructions to reset your password. The link will expire in 1 hour.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingXLarge))

                SWPrimaryButton(
                    text = "Back to Sign In",
                    onClick = onNavigateBack
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

                SWTextButton(
                    text = "Resend Email",
                    onClick = {
                        viewModel.resetState()
                        email = "" // Clear email to allow re-entry
                    }
                )
                }
            } else {
                // Input State
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

                Text(
                    text = "No worries! Enter your email address and we'll send you a link to reset your password.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingXLarge))

                SWTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                        // Clear error state when user starts typing
                        if (state is PasswordResetState.Error) {
                            viewModel.resetState()
                        }
                    },
                    label = "Email",
                    placeholder = "you@example.com",
                    keyboardType = KeyboardType.Email,
                    testTag = "forgot_password_email_field",
                    isError = emailError != null || errorMessage != null,
                    errorMessage = emailError ?: errorMessage,
                    leadingIcon = {
                        Icon(Icons.Default.Email, "Email")
                    }
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

                SWPrimaryButton(
                    text = "Send Reset Link",
                    onClick = {
                        // Clear previous errors
                        emailError = null
                        viewModel.resetState()
                        
                        // Validate email
                        if (email.isEmpty() || !email.contains("@")) {
                            emailError = "Please enter a valid email address"
                        } else {
                            // Send password reset email
                            viewModel.sendResetEmail(email)
                        }
                    },
                    testTag = "forgot_password_send_button",
                    enabled = !isLoading,
                    isLoading = isLoading
                )

                Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

                SWTextButton(
                    text = "Back to Sign In",
                    onClick = onNavigateBack
                )
            }
        }
    }
}
