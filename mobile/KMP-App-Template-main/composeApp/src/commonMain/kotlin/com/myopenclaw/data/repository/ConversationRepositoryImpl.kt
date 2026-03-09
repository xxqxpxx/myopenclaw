package com.myopenclaw.data.repository

import com.myopenclaw.data.remote.ApiService
import com.myopenclaw.domain.models.*
import com.myopenclaw.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.Flow

class ConversationRepositoryImpl(
    private val apiService: ApiService
) : ConversationRepository {

    override suspend fun getConversations(): List<Conversation> {
        return apiService.getConversations()
    }

    override suspend fun createConversation(title: String): Conversation {
        return apiService.createConversation(title)
    }

    override suspend fun deleteConversation(id: String) {
        apiService.deleteConversation(id)
    }

    override suspend fun getMessages(conversationId: String): List<Message> {
        return apiService.getMessages(conversationId)
    }

    override fun streamChat(
        conversationId: String,
        content: String,
        model: String?
    ): Flow<SSEEvent> {
        return apiService.streamChat(conversationId, content, model)
    }

    override suspend fun getCreditBalance(): CreditBalance {
        return apiService.getCreditBalance()
    }
}
