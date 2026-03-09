package com.myopenclaw.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.myopenclaw.ui.viewmodel.auth.AuthState
import com.myopenclaw.ui.viewmodel.auth.SignInViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToHome: () -> Unit,
    viewModel: SignInViewModel = koinViewModel()
) {
    val authState by viewModel.state.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Handle auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                viewModel.resetState()
                onNavigateToHome()
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Sign In",
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("signin_back_button")) {
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
                .padding(Dimensions.paddingLarge)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(Dimensions.spacingXLarge))

            // Header
            Text(
                text = "Welcome Back",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingSmall))

            Text(
                text = "Sign in to access your signals",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingXXLarge))

            // Email Field
            SWTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                label = "Email",
                placeholder = "you@example.com",
                keyboardType = KeyboardType.Email,
                isError = emailError != null,
                errorMessage = emailError,
                testTag = "signin_email_field",
                leadingIcon = {
                    Icon(Icons.Default.Email, "Email")
                }
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

            // Password Field
            SWPasswordTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = null
                },
                label = "Password",
                isError = passwordError != null,
                errorMessage = passwordError,
                testTag = "signin_password_field"
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

            // Remember Me & Forgot Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("signin_remember_checkbox")
                    )
                    Text(
                        text = "Remember me",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                TextButton(onClick = onNavigateToForgotPassword) {
                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

            // Sign In Button
            SWPrimaryButton(
                text = "Sign In",
                onClick = {
                    // Validation
                    var hasError = false

                    if (email.isEmpty() || !email.contains("@")) {
                        emailError = "Please enter a valid email"
                        hasError = true
                    }

                    if (password.isEmpty()) {
                        passwordError = "Please enter your password"
                        hasError = true
                    }

                    if (!hasError) {
                        viewModel.signIn(email, password)
                    }
                },
                enabled = authState !is AuthState.Loading,
                isLoading = authState is AuthState.Loading,
                testTag = "signin_button"
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = "OR",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = Dimensions.spacingMedium),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(Dimensions.spacingLarge))

            // Social Sign In - Apple first per Apple HIG
            SWSocialButton(
                text = "Sign in with Apple",
                onClick = { viewModel.signInWithApple() },
                testTag = "signin_apple_button",
                icon = {
                    Icon(Icons.Default.PhoneIphone, "Apple", tint = MaterialTheme.colorScheme.onSurface)
                }
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingMedium))

            SWSocialButton(
                text = "Sign in with Google",
                onClick = { viewModel.signInWithGoogle() },
                testTag = "signin_google_button",
                icon = {
                    Icon(Icons.Default.Email, "Google", tint = MaterialTheme.colorScheme.primary)
                }
            )

            Spacer(modifier = Modifier.height(Dimensions.spacingXLarge))

            // Sign Up Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onNavigateToSignUp) {
                    Text(
                        text = "Sign Up",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
