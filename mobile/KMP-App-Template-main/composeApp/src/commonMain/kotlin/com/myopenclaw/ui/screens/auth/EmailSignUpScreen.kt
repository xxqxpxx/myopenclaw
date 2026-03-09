package com.myopenclaw.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myopenclaw.ui.theme.*
import com.myopenclaw.util.UrlLauncher
import org.koin.compose.koinInject

@Composable
fun EmailSignUpScreen(
    onContinue: (String, String) -> Unit = { _, _ -> },
    onNavigateBack: () -> Unit = {},
    urlLauncher: UrlLauncher = koinInject()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Dark1
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Back button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .padding(start = 8.dp, top = 16.dp)
                    .align(Alignment.TopStart)
                    .testTag("email_signup_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Go back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top drag handle indicator
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .background(Color.White, RoundedCornerShape(2.dp))
                )

                Spacer(modifier = Modifier.height(48.dp))

            // Email Icon Container
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = Color(0xFF2C3544),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "Enter your email address",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Email Input Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_signup_email_field"),
                placeholder = {
                    Text(
                        "Your email address",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 16.sp
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF2C3544),
                    unfocusedContainerColor = Color(0xFF2C3544),
                    focusedBorderColor = Green2,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Green2,
                    errorBorderColor = BearishRed,
                    errorTextColor = Color.White,
                    errorContainerColor = Color(0xFF2C3544)
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                isError = emailError != null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
            )

            if (emailError != null) {
                Text(
                    text = emailError!!,
                    color = BearishRed,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Continue Button (Pill-shaped)
            Button(
                onClick = {
                    // Validation
                    var hasError = false
                    if (email.isBlank()) {
                        emailError = "Please enter your email address"
                        hasError = true
                    } else if (!email.contains("@") || !email.contains(".")) {
                        emailError = "Please enter a valid email address"
                        hasError = true
                    }

                    if (!hasError) {
                        // Use email as name for now (single field design)
                        // println("EmailSignUpScreen: onContinue called with email='$email'")
                        onContinue(email, email.substringBefore("@"))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("email_signup_continue_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green2
                ),
                shape = CircleShape
            ) {
                Text(
                    "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Terms and Privacy
            val termsAnnotatedString = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                ) {
                    append("By continuing, you agree to our ")
                }
                pushStringAnnotation(tag = "terms", annotation = "https://myopenclaw.com/terms")
                withStyle(
                    style = SpanStyle(
                        color = Color.White,
                        fontSize = 14.sp,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Terms and conditions")
                }
                pop()
                withStyle(
                    style = SpanStyle(
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                ) {
                    append(" and ")
                }
                pushStringAnnotation(tag = "privacy", annotation = "https://myopenclaw.com/privacy")
                withStyle(
                    style = SpanStyle(
                        color = Color.White,
                        fontSize = 14.sp,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("privacy policy")
                }
                pop()
            }

            ClickableText(
                text = termsAnnotatedString,
                onClick = { offset ->
                    termsAnnotatedString.getStringAnnotations(
                        tag = "terms",
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let {
                        urlLauncher.openUrl(it.item)
                        return@ClickableText
                    }
                    termsAnnotatedString.getStringAnnotations(
                        tag = "privacy",
                        start = offset,
                        end = offset
                    ).firstOrNull()?.let {
                        urlLauncher.openUrl(it.item)
                    }
                },
                style = TextStyle(
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}