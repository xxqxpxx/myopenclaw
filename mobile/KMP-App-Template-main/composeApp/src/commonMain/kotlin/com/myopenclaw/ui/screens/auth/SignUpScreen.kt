package com.myopenclaw.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myopenclaw.ui.theme.*
import com.myopenclaw.util.UrlLauncher
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import myopenclaw.composeapp.generated.resources.Res
import myopenclaw.composeapp.generated.resources.appstore

@Composable
fun SignUpScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToSignIn: () -> Unit = {},
    onNavigateToEmailSignUp: () -> Unit = {},
    onNavigateToGoogleSignUp: () -> Unit = {},
    onNavigateToAppleSignUp: () -> Unit = {},
    urlLauncher: UrlLauncher = koinInject()
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0A0F1E)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Dimensions.paddingLarge),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // App Icon
            Image(
                painter = painterResource(Res.drawable.appstore),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Title
            Text(
                text = "myOpenClaw:",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp
                ),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Your AI-Powered Coding Assistant",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp
                ),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Description
            Text(
                text = "Write, debug, and ship code faster with Claude AI. Execute code in secure sandboxes, get instant help, and build projects from your phone.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Stats badges
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Code,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "50+",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Text(
                        text = "Languages",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Surface(
                    modifier = Modifier
                        .width(1.dp)
                        .height(24.dp),
                    color = Color.White.copy(alpha = 0.3f)
                ) {}

                Text(
                    text = "Powered by\nClaude AI",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Sign up with Apple Button
            Button(
                onClick = onNavigateToAppleSignUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_apple_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 18.dp)
            ) {
                Icon(
                    Icons.Default.PhoneIphone,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Sign up with Apple",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign up with Google Button
            Button(
                onClick = onNavigateToGoogleSignUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_google_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 18.dp)
            ) {
                Surface(
                    modifier = Modifier.size(20.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = Color.Transparent
                ) {
                    Icon(
                        Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = Color(0xFF4285F4)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Sign up with Google",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign up with Email Button
            Button(
                onClick = onNavigateToEmailSignUp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_email_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary
                ),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 18.dp)
            ) {
                Icon(
                    Icons.Default.Email,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Sign up with Email",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Sign In Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                TextButton(
                    onClick = onNavigateToSignIn,
                    modifier = Modifier.testTag("signup_signin_link")
                ) {
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Terms and Privacy
            val termsAnnotatedString = buildAnnotatedString {
                append("By continuing, you agree to our ")

                pushStringAnnotation(tag = "terms", annotation = "https://myopenclaw.com/terms")
                withStyle(
                    style = SpanStyle(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("Terms and conditions")
                }
                pop()

                append(" and ")

                pushStringAnnotation(tag = "privacy", annotation = "https://myopenclaw.com/privacy")
                withStyle(
                    style = SpanStyle(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("privacy policy")
                }
                pop()
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                ClickableText(
                    text = termsAnnotatedString,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    ),
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
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
