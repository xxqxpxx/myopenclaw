package com.myopenclaw.data.remote

import com.myopenclaw.domain.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class ApiService(
    private val httpClient: HttpClient,
    private val baseUrl: String = ApiConfig.BASE_URL
) {
    private val json = Json { ignoreUnknownKeys = true }

    // ── Conversations ─────────────────────────────────────────────

    suspend fun getConversations(): List<Conversation> {
        return httpClient.get("$baseUrl/api/v1/conversations/").body()
    }

    suspend fun createConversation(title: String): Conversation {
        return httpClient.post("$baseUrl/api/v1/conversations/") {
            contentType(ContentType.Application.Json)
            setBody(CreateConversationRequest(title))
        }.body()
    }

    suspend fun deleteConversation(id: String) {
        httpClient.delete("$baseUrl/api/v1/conversations/$id")
    }

    suspend fun getMessages(conversationId: String): List<Message> {
        return httpClient.get("$baseUrl/api/v1/conversations/$conversationId/messages").body()
    }

    // ── SSE Chat Streaming ────────────────────────────────────────

    fun streamChat(
        conversationId: String,
        content: String,
        model: String? = null
    ): Flow<SSEEvent> = flow {
        val response: HttpResponse = httpClient.post(
            "$baseUrl/api/v1/conversations/$conversationId/chat/stream"
        ) {
            contentType(ContentType.Application.Json)
            setBody(SendMessageRequest(content, model))
            timeout {
                requestTimeoutMillis = ApiConfig.STREAM_TIMEOUT
                socketTimeoutMillis = ApiConfig.STREAM_TIMEOUT
            }
        }

        val channel: ByteReadChannel = response.bodyAsChannel()

        while (!channel.isClosedForRead) {
            val line = try {
                channel.readUTF8Line()
            } catch (_: Exception) {
                null
            } ?: break

            if (line.startsWith("data: ")) {
                val data = line.removePrefix("data: ").trim()
                if (data.isNotEmpty()) {
                    try {
                        val event = json.decodeFromString<SSEEvent>(data)
                        emit(event)
                    } catch (_: Exception) {
                        // Skip malformed events
                    }
                }
            }
        }
    }

    // ── User / Credits ────────────────────────────────────────────

    suspend fun getCreditBalance(): CreditBalance {
        return httpClient.get("$baseUrl/api/v1/users/me/credits").body()
    }

    // ── Subscription (kept for RevenueCat flow) ───────────────────

    suspend fun getSubscription(): SubscriptionResponse {
        return httpClient.get("$baseUrl/api/subscription").body()
    }

    suspend fun createCheckoutSession(priceId: String): CheckoutSession {
        return httpClient.post("$baseUrl/api/create-checkout-session") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("priceId" to priceId))
        }.body()
    }

    suspend fun cancelSubscription(reason: String?, feedback: String?): Map<String, Any> {
        return httpClient.post("$baseUrl/api/subscription/cancel") {
            contentType(ContentType.Application.Json)
            setBody(mapOf("reason" to (reason ?: ""), "feedback" to (feedback ?: "")))
        }.body()
    }

    suspend fun reactivateSubscription(): Map<String, Any> {
        return httpClient.post("$baseUrl/api/subscription/reactivate").body()
    }
}
