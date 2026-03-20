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
                name = "Alex Rivera",
                title = "Full-Stack Developer",
                rating = 5,
                review = "I use myOpenClaw on my commute to prototype ideas. Yesterday I built a REST API endpoint and tested it — all from my phone. The sandbox execution is a game-changer."
            ),
            Testimonial(
                id = "2",
                name = "Priya Sharma",
                title = "CS Student",
                rating = 5,
                review = "Studying algorithms has never been easier. I paste problems and get step-by-step solutions with working code. My grades went from B to A+ in one semester."
            ),
            Testimonial(
                id = "3",
                name = "James Chen",
                title = "Backend Engineer",
                rating = 5,
                review = "The code execution sandbox is incredible. I debug production issues right from my phone. It caught a race condition I'd been chasing for days."
            ),
            Testimonial(
                id = "4",
                name = "Maria Santos",
                title = "Indie Developer",
                rating = 5,
                review = "Built my entire side project's backend with myOpenClaw. The AI writes clean, well-tested code and explains every decision. Worth every credit."
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

            Text(
                text = "Developers love myOpenClaw",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Start,
                lineHeight = 36.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Join thousands of developers coding smarter with AI",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(32.dp))

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

            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
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

            Text(
                text = testimonial.review,
                fontSize = 14.sp,
                color = Color.White,
                lineHeight = 20.sp
            )

            Text(
                text = "${testimonial.name}, ${testimonial.title}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
