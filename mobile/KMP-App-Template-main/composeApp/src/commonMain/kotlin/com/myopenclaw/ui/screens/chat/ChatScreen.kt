package com.myopenclaw.ui.screens.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myopenclaw.domain.models.ChatUiMessage
import com.myopenclaw.domain.models.MessageRole
import com.myopenclaw.ui.theme.*
import com.myopenclaw.ui.viewmodel.ai.ChatViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String? = null,
    onBack: () -> Unit,
    chatViewModel: ChatViewModel = koinViewModel()
) {
    val state by chatViewModel.state.collectAsState()

    LaunchedEffect(conversationId) {
        if (conversationId != null) {
            chatViewModel.loadConversation(conversationId)
        } else {
            chatViewModel.startNewConversation()
        }
        chatViewModel.loadCredits()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            state.conversationTitle,
                            style = MaterialTheme.typography.titleMedium
                        )
                        state.creditsBalance?.let { credits ->
                            Text(
                                "$credits credits",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (credits > 10) Green2 else BearishRed
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark
                )
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val listState = rememberLazyListState()

            LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.content) {
                if (state.messages.isNotEmpty()) {
                    listState.animateScrollToItem(state.messages.size - 1)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.messages.isEmpty() && !state.isLoading) {
                    item {
                        EmptyState()
                    }
                }

                items(state.messages, key = { "${it.id}-${it.content.length}" }) { message ->
                    MessageBubble(message)
                }

                if (state.isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Primary
                            )
                        }
                    }
                }
            }

            // Error banner
            AnimatedVisibility(state.error != null) {
                state.error?.let { error ->
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            error,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Input bar
            ChatInputBar(
                isSending = state.isSending,
                onSend = { chatViewModel.sendMessage(it) }
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            color = Primary.copy(alpha = 0.15f),
            shape = RoundedCornerShape(14.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.Code,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Text(
            "myOpenClaw",
            style = MaterialTheme.typography.headlineMedium,
            color = Primary
        )
        Text(
            "Ask anything. Write code. Get help.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f)
        )

        // Quick prompt suggestions
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SuggestionChip("Write a function")
            SuggestionChip("Debug my code")
            SuggestionChip("Explain a concept")
        }
    }
}

@Composable
private fun SuggestionChip(text: String) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Primary
        )
    }
}

@Composable
private fun MessageBubble(message: ChatUiMessage) {
    val isUser = message.role == MessageRole.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        // Tool execution card (rendered above the message)
        message.toolName?.let { tool ->
            ToolExecutionCard(
                toolName = tool,
                toolOutput = message.toolOutput,
                isStreaming = message.isStreaming
            )
            Spacer(Modifier.height(4.dp))
        }

        // Message bubble
        if (message.content.isNotEmpty() || (message.isStreaming && message.toolName == null)) {
            val bgColor = if (isUser) Primary else SurfaceDark
            val textColor = if (isUser) Color.White else Color.White.copy(alpha = 0.9f)

            Surface(
                color = bgColor,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                modifier = Modifier.widthIn(max = 320.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Render content with basic code block support
                    MessageContent(
                        content = message.content.ifEmpty { "..." },
                        textColor = textColor
                    )

                    // Streaming indicator
                    if (message.isStreaming && message.content.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(2.dp),
                            color = if (isUser) Color.White.copy(alpha = 0.5f) else Primary.copy(alpha = 0.5f)
                        )
                    }

                    // Model label
                    if (!isUser && message.model != null && !message.isStreaming) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            message.model,
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor.copy(alpha = 0.5f)
                        )
                    }

                    // Error display
                    message.error?.let { err ->
                        Spacer(Modifier.height(4.dp))
                        Text(
                            err,
                            style = MaterialTheme.typography.labelSmall,
                            color = BearishRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageContent(content: String, textColor: Color) {
    // Simple code block detection for basic markdown rendering
    val parts = content.split("```")

    if (parts.size <= 1) {
        // No code blocks, render as plain text
        Text(
            text = content,
            color = textColor,
            style = MaterialTheme.typography.bodyMedium
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 0) {
                // Regular text
                if (part.isNotBlank()) {
                    Text(
                        text = part.trim(),
                        color = textColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                // Code block
                val lines = part.lines()
                val language = lines.firstOrNull()?.trim() ?: ""
                val code = if (language.isNotEmpty() && !language.contains(" "))
                    lines.drop(1).joinToString("\n")
                else
                    part

                Surface(
                    color = Color(0xFF0D1117),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        if (language.isNotEmpty() && !language.contains(" ")) {
                            Surface(
                                color = Color(0xFF161B22),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    language,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            }
                        }
                        Text(
                            text = code.trim(),
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace
                            ),
                            color = Color(0xFF7EE787)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolExecutionCard(
    toolName: String,
    toolOutput: String?,
    isStreaming: Boolean
) {
    val (icon, label) = when (toolName) {
        "code_execute" -> Icons.Default.Code to "Code Execution"
        "web_search" -> Icons.Default.Search to "Web Search"
        "file_read" -> Icons.Default.Description to "Reading File"
        "file_write" -> Icons.Default.Save to "Writing File"
        else -> Icons.Default.Build to toolName
    }

    Surface(
        color = Color(0xFF1A2332),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.widthIn(max = 320.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    color = Primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                if (isStreaming && toolOutput == null) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = Green2
                    )
                } else {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Green2,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Tool output preview
            toolOutput?.let { output ->
                if (output.isNotBlank()) {
                    Surface(
                        color = Color(0xFF0D1117),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (output.length > 200) output.take(200) + "..." else output,
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            ),
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 6
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    isSending: Boolean,
    onSend: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    Surface(
        tonalElevation = 3.dp,
        color = SurfaceDark,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Ask anything...", color = Color.White.copy(alpha = 0.4f)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                enabled = !isSending,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (text.isNotBlank() && !isSending) {
                            onSend(text.trim())
                            text = ""
                        }
                    }
                )
            )

            IconButton(
                onClick = {
                    if (text.isNotBlank() && !isSending) {
                        onSend(text.trim())
                        text = ""
                    }
                },
                enabled = text.isNotBlank() && !isSending,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (text.isNotBlank() && !isSending)
                            Green2
                        else
                            SurfaceVariant
                    )
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Primary
                    )
                } else {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (text.isNotBlank()) Color.White else Color.White.copy(alpha = 0.4f)
                    )
                }
            }
        }
    }
}
