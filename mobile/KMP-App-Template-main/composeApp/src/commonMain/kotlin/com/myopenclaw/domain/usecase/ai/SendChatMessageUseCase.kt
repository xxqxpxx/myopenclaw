package com.myopenclaw.domain.usecase.ai

import com.myopenclaw.domain.models.ChatContext
import com.myopenclaw.domain.models.ChatResponse
import com.myopenclaw.domain.repository.MarketDataRepository

/**
 * Use case for sending chat messages to AI assistant
 */
class SendChatMessageUseCase(
    private val marketDataRepository: MarketDataRepository
) {
    suspend operator fun invoke(
        message: String,
        conversationId: String? = null,
        context: ChatContext? = null
    ): Result<ChatResponse> {
        return marketDataRepository.sendChatMessage(
            message = message,
            conversationId = conversationId,
            context = context
        )
    }
}
