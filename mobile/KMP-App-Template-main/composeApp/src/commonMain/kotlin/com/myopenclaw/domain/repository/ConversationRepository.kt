package com.myopenclaw.domain.repository

import com.myopenclaw.domain.models.*
import kotlinx.coroutines.flow.Flow

interface ConversationRepository {
    suspend fun getConversations(): List<Conversation>
    suspend fun createConversation(title: String = "New Chat"): Conversation
    suspend fun deleteConversation(id: String)
    suspend fun getMessages(conversationId: String): List<Message>
    fun streamChat(conversationId: String, content: String, model: String? = null): Flow<SSEEvent>
    suspend fun getCreditBalance(): CreditBalance
}
