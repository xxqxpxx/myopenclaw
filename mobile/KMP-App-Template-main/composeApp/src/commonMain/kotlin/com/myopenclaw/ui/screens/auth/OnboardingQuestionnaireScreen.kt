package com.myopenclaw.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myopenclaw.ui.theme.*
import com.myopenclaw.ui.viewmodel.auth.OnboardingAnswers

data class OnboardingQuestion(
    val id: Int,
    val question: String,
    val options: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingQuestionnaireScreen(
    userName: String = "there",
    onComplete: (OnboardingAnswers) -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var currentStep by remember { mutableStateOf(0) }
    var selectedAnswers by remember { mutableStateOf(OnboardingAnswers()) }

    val questions = remember {
        listOf(
            OnboardingQuestion(
                id = 0,
                question = "What's your biggest challenge in trading right now $userName?",
                options = listOf(
                    "Missing profitable opportunities",
                    "Not understanding charts",
                    "Making emotional decisions",
                    "Timing entries and exits",
                    "Just too overwhelming"
                )
            ),
            OnboardingQuestion(
                id = 1,
                question = "Be honest: How confident are you when placing a trade $userName?",
                options = listOf(
                    "Very confident",
                    "Somewhat confident",
                    "Guessing most of the time",
                    "Scared I'll lose money",
                    "Following others blindly"
                )
            ),
            OnboardingQuestion(
                id = 2,
                question = "How much time do you spend analysing before each trade",
                options = listOf(
                    "Hours (still unsure)",
                    "30-60 minutes",
                    "Quick 5-minute check",
                    "I don't analyze much",
                    "I follow signals/tips"
                )
            ),
            OnboardingQuestion(
                id = 3,
                question = "When staring at a chart, what goes through your mind",
                options = listOf(
                    "Is this the right moment?",
                    "What if I'm wrong?",
                    "I have no idea what this means",
                    "Should I wait longer?",
                    "I need expert confirmation"
                )
            ),
            OnboardingQuestion(
                id = 4,
                question = "If you could wave a magic wand, what would help most?",
                options = listOf(
                    "Clear buy/sell signals",
                    "Instant chart analysis",
                    "Expert guidance anytime",
                    "Understanding patterns better",
                    "All of the above"
                )
            ),
            OnboardingQuestion(
                id = 5,
                question = "What type of trader are you?",
                options = listOf(
                    "Day Trader",
                    "Swing Trader",
                    "Long-term Investor",
                    "Crypto Trader",
                    "Just getting started"
                )
            ),
            OnboardingQuestion(
                id = 6,
                question = "Which assets do you trade most",
                options = listOf(
                    "Stocks",
                    "Crypto",
                    "Forex",
                    "Commodities",
                    "Indices"
                )
            )
        )
    }

    val currentQuestion = questions[currentStep]
    var selectedOption by remember(currentStep) { mutableStateOf<String?>(null) }

    // Load existing answer when changing steps
    LaunchedEffect(currentStep) {
        selectedOption = when (currentStep) {
            0 -> selectedAnswers.biggestChallenge.takeIf { it.isNotEmpty() }
            1 -> selectedAnswers.confidenceLevel.takeIf { it.isNotEmpty() }
            2 -> selectedAnswers.analysisTime.takeIf { it.isNotEmpty() }
            3 -> selectedAnswers.chartMindset.takeIf { it.isNotEmpty() }
            4 -> selectedAnswers.desiredHelp.takeIf { it.isNotEmpty() }
            5 -> selectedAnswers.traderType.takeIf { it.isNotEmpty() }
            6 -> selectedAnswers.tradedAssets.takeIf { it.isNotEmpty() }
            else -> null
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
                onClick = {
                    if (currentStep > 0) {
                        currentStep--
                    } else {
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .size(32.dp)
                    .testTag("onboarding_back"),
                color = Color.White.copy(alpha = 0.1f),
                shape = androidx.compose.foundation.shape.CircleShape
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

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                questions.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .background(
                                color = if (index <= currentStep) Green2 else Color.White.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(2.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Question
            Text(
                text = currentQuestion.question,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Options
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(currentQuestion.options) { option ->
                    val optionIndex = currentQuestion.options.indexOf(option)
                    OptionCard(
                        option = option,
                        isSelected = selectedOption == option,
                        onClick = { selectedOption = option },
                        testTag = "questionnaire_q${currentQuestion.id}_option_$optionIndex"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Continue Button (Pill-shaped)
            Button(
                onClick = {
                    selectedOption?.let { answer ->
                        // Save answer
                        selectedAnswers = when (currentStep) {
                            0 -> selectedAnswers.copy(biggestChallenge = answer)
                            1 -> selectedAnswers.copy(confidenceLevel = answer)
                            2 -> selectedAnswers.copy(analysisTime = answer)
                            3 -> selectedAnswers.copy(chartMindset = answer)
                            4 -> selectedAnswers.copy(desiredHelp = answer)
                            5 -> selectedAnswers.copy(traderType = answer)
                            6 -> selectedAnswers.copy(tradedAssets = answer)
                            else -> selectedAnswers
                        }

                        // Navigate to next step or complete
                        if (currentStep < questions.size - 1) {
                            currentStep++
                        } else {
                            onComplete(selectedAnswers)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("questionnaire_continue_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green2,
                    disabledContainerColor = Green2.copy(alpha = 0.5f)
                ),
                shape = androidx.compose.foundation.shape.CircleShape,
                enabled = selectedOption != null
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
fun OptionCard(
    option: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String? = null
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .then(testTag?.let { Modifier.testTag(it) } ?: Modifier),
        color = Color(0xFF2C3544),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 0.dp,
            color = if (isSelected) Green2 else Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = option,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Radio button indicator with checkmark
            Box(
                modifier = Modifier.size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    // Green filled circle with checkmark
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Green2
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    // Empty circle outline
                    Surface(
                        modifier = Modifier.size(24.dp),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color.Transparent,
                        border = BorderStroke(
                            width = 2.dp,
                            color = Color.White.copy(alpha = 0.5f)
                        )
                    ) {}
                }
            }
        }
    }
}
