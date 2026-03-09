package com.myopenclaw.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myopenclaw.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun OTPVerificationScreen(
    email: String = "example@gmail.com",
    onVerify: (String) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onResendOTP: () -> Unit = {}
) {
    var otpValues by remember { mutableStateOf(List(6) { "" }) }
    var resendTimer by remember { mutableIntStateOf(28) }
    var canResend by remember { mutableStateOf(false) }

    // Timer for resend
    LaunchedEffect(Unit) {
        while (resendTimer > 0) {
            delay(1000)
            resendTimer--
        }
        canResend = true
    }

    // Auto-submit when all fields filled
    LaunchedEffect(otpValues) {
        if (otpValues.all { it.isNotEmpty() }) {
            val otp = otpValues.joinToString("")
            onVerify(otp)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Dark1
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Back Button
            Surface(
                onClick = onNavigateBack,
                modifier = Modifier.size(32.dp),
                color = Color.White.copy(alpha = 0.1f),
                shape = CircleShape
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Confirm your Email address",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "Please enter the OTP code we sent to your email address $email",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // OTP Input Boxes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                otpValues.forEachIndexed { index, value ->
                    OTPBox(
                        value = value,
                        onValueChange = { newValue ->
                            if (newValue.length <= 1 && (newValue.isEmpty() || newValue.all { it.isDigit() })) {
                                val newValues = otpValues.toMutableList()
                                newValues[index] = newValue
                                otpValues = newValues
                            }
                        },
                        isFirst = index == 0,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Continue Button
            Button(
                onClick = {
                    val otp = otpValues.joinToString("")
                    if (otp.length == 6) {
                        onVerify(otp)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green2,
                    disabledContainerColor = Green2.copy(alpha = 0.5f)
                ),
                shape = CircleShape,
                enabled = otpValues.all { it.isNotEmpty() }
            ) {
                Text(
                    "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Resend Code Text
            Text(
                text = if (canResend) {
                    "Resend code"
                } else {
                    "Resend code in ${resendTimer} Sec"
                },
                fontSize = 16.sp,
                color = if (canResend) Green2 else Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .run {
                        if (canResend) {
                            this.then(Modifier.padding(8.dp))
                        } else {
                            this
                        }
                    }
            )

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTPBox(
    value: String,
    onValueChange: (String) -> Unit,
    isFirst: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .aspectRatio(1f),
        color = Color(0xFF2C3544),
        shape = RoundedCornerShape(12.dp),
        border = if (isFirst && value.isEmpty()) {
            BorderStroke(2.dp, Green2)
        } else if (value.isNotEmpty()) {
            BorderStroke(1.dp, Green2)
        } else {
            null
        }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = "",
                                fontSize = 24.sp,
                                color = Color.White.copy(alpha = 0.3f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}
