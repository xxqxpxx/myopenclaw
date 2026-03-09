package com.myopenclaw.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myopenclaw.ui.components.SWPasswordTextField
import com.myopenclaw.ui.components.SWPrimaryButton
import com.myopenclaw.ui.theme.*
import com.myopenclaw.ui.viewmodel.auth.AuthState
import com.myopenclaw.ui.viewmodel.auth.SignUpViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PasswordSetupScreen(
    email: String,
    displayName: String,
    onAccountCreated: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    viewModel: SignUpViewModel = koinViewModel()
) {
    // Log received parameters
    LaunchedEffect(email, displayName) {
        // println("PasswordSetupScreen: Received email='$email', displayName='$displayName'")
    }

    val authState by viewModel.state.collectAsState()
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    // Handle auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                viewModel.resetState()
                onAccountCreated()
            }
            else -> {}
        }
    }

    // Show error dialog
    if (authState is AuthState.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.resetState() },
            title = { Text("Oops! Something went wrong") },
            text = { Text((authState as AuthState.Error).message) },
            confirmButton = {
                TextButton(onClick = { viewModel.resetState() }) {
                    Text("OK")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0A0F1E) // Dark navy background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.paddingLarge),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top drag handle indicator
            Surface(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .width(40.dp)
                    .height(4.dp),
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(2.dp)
            ) {}

            Spacer(modifier = Modifier.height(48.dp))

            // Lock Icon
            Surface(
                modifier = Modifier.size(80.dp),
                color = Color(0xFF1A2332), // Dark blue-gray
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "Create a password",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 24.sp
                ),
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your password must be at least 8 characters",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Password Input Field
            SWPasswordTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = "Password",
                isError = passwordError != null,
                errorMessage = passwordError,
                modifier = Modifier.fillMaxWidth(),
                testTag = "password_setup_password_field"
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password Input Field
            SWPasswordTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = null
                },
                label = "Confirm Password",
                isError = confirmPasswordError != null,
                errorMessage = confirmPasswordError,
                modifier = Modifier.fillMaxWidth(),
                testTag = "password_setup_confirm_field"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Create Account Button
            SWPrimaryButton(
                text = "Create Account",
                onClick = {
                    // Validation
                    var hasError = false

                    if (password.length < 8) {
                        passwordError = "Password must be at least 8 characters"
                        hasError = true
                    }

                    if (password != confirmPassword) {
                        confirmPasswordError = "Passwords don't match"
                        hasError = true
                    }

                    if (!hasError) {
                        // println("PasswordSetupScreen: Calling signUp with email='$email', password='[REDACTED]', displayName='$displayName'")
                        viewModel.signUp(email, password, displayName)
                    }
                },
                enabled = authState !is AuthState.Loading,
                isLoading = authState is AuthState.Loading,
                modifier = Modifier.fillMaxWidth(),
                testTag = "password_setup_create_button"
            )

            Spacer(modifier = Modifier.weight(1f))

            // Terms and Privacy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "By continuing, you agree to our ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Terms and conditions",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = " and ",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "privacy policy",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
