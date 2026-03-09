package com.myopenclaw.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myopenclaw.ui.theme.*

@Composable
fun OnboardingFinalCTAScreen(
    onTakePhoto: () -> Unit = {},
    onGetMarketTrends: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Dark1
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Simple icon placeholder - you can replace with actual app icon
                Icon(
                    Icons.Default.ShowChart,
                    contentDescription = null,
                    tint = Green2,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Title
            Text(
                text = "Let's get your first insight in 30 seconds",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(80.dp))

            // Take Photo Button (Primary, Pill-shaped)
            Button(
                onClick = onTakePhoto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green2
                ),
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Text(
                    "Take a pic of your Trade chart",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Get Market Trends Button (Secondary, Pill-shaped)
            Button(
                onClick = onGetMarketTrends,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Text(
                    "Get Market trend analysis",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Grey900
                )
            }
        }
    }
}
