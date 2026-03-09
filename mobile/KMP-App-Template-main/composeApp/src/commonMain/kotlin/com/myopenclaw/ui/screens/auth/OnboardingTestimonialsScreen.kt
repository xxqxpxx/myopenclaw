package com.myopenclaw.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myopenclaw.ui.theme.*

data class Testimonial(
    val id: String,
    val name: String,
    val title: String,
    val rating: Int,
    val review: String
)

@Composable
fun OnboardingTestimonialsScreen(
    onContinue: () -> Unit = {}
) {
    val testimonials = remember {
        listOf(
            Testimonial(
                id = "1",
                name = "Marcus Chen",
                title = "Day Trader",
                rating = 5,
                review = "I was spending 2+ hours analysing charts daily. Now I snap a photo and get insights in seconds. My win rate jumped 35% in just 3 weeks."
            ),
            Testimonial(
                id = "2",
                name = "Sarah Williams",
                title = "Swing Trader",
                rating = 5,
                review = "The daily signals are a game-changer. Caught 4 profitable trades this week I would've completely missed."
            ),
            Testimonial(
                id = "3",
                name = "David Park",
                title = "Crypto Trader",
                rating = 5,
                review = "I finally understand what I'm looking at. The AI explains patterns in plain English."
            ),
            Testimonial(
                id = "4",
                name = "Jennifer Lopez",
                title = "Forex Trader",
                rating = 5,
                review = "Best trading tool I've used. The chart analysis alone paid for itself in one trade. My confidence went from 3/10 to 8/10"
            )
        )
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
            Spacer(modifier = Modifier.height(48.dp))

            // Header
            Text(
                text = "Thanks for trusting Signalwhisper",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Start,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Join 109k+ traders improving their win rate",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Testimonials List
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(testimonials) { testimonial ->
                    TestimonialCard(testimonial)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Continue Button (Pill-shaped)
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 0.dp)
                    .testTag("testimonials_continue_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green2
                ),
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Text(
                    "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun TestimonialCard(testimonial: Testimonial) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A2332),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stars
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(testimonial.rating) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = StarYellow,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Review Text
            Text(
                text = testimonial.review,
                fontSize = 14.sp,
                color = Color.White,
                lineHeight = 20.sp
            )

            // Author
            Text(
                text = "${testimonial.name}, ${testimonial.title}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
