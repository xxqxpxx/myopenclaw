package com.myopenclaw.domain.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MessageRole {
    @SerialName("user") USER,
    @SerialName("assistant") ASSISTANT,
    @SerialName("tool") TOOL,
    @SerialName("system") SYSTEM
}

@Serializable
enum class SSEEventType {
    @SerialName("token") TOKEN,
    @SerialName("tool_start") TOOL_START,
    @SerialName("tool_result") TOOL_RESULT,
    @SerialName("file") FILE,
    @SerialName("error") ERROR,
    @SerialName("done") DONE
}

@Serializable
data class Conversation(
    val id: String,
    val title: String = "New Chat",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    @SerialName("last_message") val lastMessage: String? = null
)

@Serializable
data class Message(
    val id: String,
    val role: MessageRole,
    val content: String,
    val model: String? = null,
    @SerialName("tokens_used") val tokensUsed: Int = 0,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class SSEEvent(
    val type: SSEEventType,
    val content: String? = null,
    val model: String? = null,
    val tool: String? = null,
    val input: Map<String, String>? = null,
    val output: String? = null,
    val filename: String? = null,
    val url: String? = null,
    val size: Int? = null,
    @SerialName("total_tokens") val totalTokens: Int? = null,
    @SerialName("credits_used") val creditsUsed: Int? = null,
    val error: String? = null
)

@Serializable
data class CreateConversationRequest(
    val title: String = "New Chat"
)

@Serializable
data class SendMessageRequest(
    val content: String,
    val model: String? = null
)

@Serializable
data class CreditBalance(
    @SerialName("credits_balance") val creditsBalance: Int,
    @SerialName("subscription_tier") val subscriptionTier: String
)

/** Local UI state for a chat message (includes streaming state). */
data class ChatUiMessage(
    val id: String,
    val role: MessageRole,
    val content: String,
    val isStreaming: Boolean = false,
    val model: String? = null,
    val toolName: String? = null,
    val toolOutput: String? = null,
    val error: String? = null
)
