package com.myopenclaw.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myopenclaw.ui.theme.*
import kotlinx.coroutines.delay

data class SetupTask(
    val id: Int,
    val title: String,
    var isCompleted: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingSetupScreen(
    userName: String = "there",
    onSetupComplete: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    var tasks by remember {
        mutableStateOf(
            listOf(
                SetupTask(0, "Configuring AI models", false),
                SetupTask(1, "Setting up code sandbox", false),
                SetupTask(2, "Preparing tool integrations", false),
                SetupTask(3, "Initializing your workspace", false)
            )
        )
    }

    var currentProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        tasks.forEachIndexed { index, _ ->
            delay(800)
            tasks = tasks.mapIndexed { i, task ->
                if (i == index) task.copy(isCompleted = true) else task
            }
            currentProgress = (index + 1).toFloat() / tasks.size
        }

        delay(500)
        onSetupComplete()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Dark1
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Surface(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.Start),
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

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "Setting up your AI workspace, $userName...",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LinearProgressIndicator(
                    progress = { currentProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = Green2,
                    trackColor = Color.White.copy(alpha = 0.2f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${(currentProgress * 100).toInt()}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Green2
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Dark6,
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    tasks.forEach { task ->
                        SetupTaskRow(task)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green2,
                    disabledContainerColor = Green2.copy(alpha = 0.5f)
                ),
                shape = androidx.compose.foundation.shape.CircleShape,
                enabled = false
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
fun SetupTaskRow(task: SetupTask) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = task.title,
            fontSize = 16.sp,
            color = if (task.isCompleted) Color.White else Color.White.copy(alpha = 0.6f),
            fontWeight = if (task.isCompleted) FontWeight.Medium else FontWeight.Normal
        )

        if (task.isCompleted) {
            Surface(
                modifier = Modifier.size(24.dp),
                color = Green2,
                shape = androidx.compose.foundation.shape.CircleShape
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
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
            Surface(
                modifier = Modifier.size(24.dp),
                color = Color.Transparent,
                shape = androidx.compose.foundation.shape.CircleShape,
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.3f)
                )
            ) {}
        }
    }
}
