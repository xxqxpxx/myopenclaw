package com.myopenclaw.ui.viewmodel.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myopenclaw.domain.models.*
import com.myopenclaw.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ChatState(
    val conversationId: String? = null,
    val conversationTitle: String = "New Chat",
    val messages: List<ChatUiMessage> = emptyList(),
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val creditsBalance: Int? = null,
    val error: String? = null
)

class ChatViewModel(
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    fun loadConversation(conversationId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, conversationId = conversationId) }
            try {
                val messages = conversationRepository.getMessages(conversationId)
                _state.update {
                    it.copy(
                        isLoading = false,
                        messages = messages.map { msg ->
                            ChatUiMessage(
                                id = msg.id,
                                role = msg.role,
                                content = msg.content,
                                model = msg.model
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun startNewConversation() {
        _state.value = ChatState()
    }

    fun sendMessage(content: String) {
        if (content.isBlank() || _state.value.isSending) return

        viewModelScope.launch {
            // Add user message to UI immediately
            val userMsg = ChatUiMessage(
                id = "local-${System.currentTimeMillis()}",
                role = MessageRole.USER,
                content = content
            )
            _state.update {
                it.copy(
                    messages = it.messages + userMsg,
                    isSending = true,
                    error = null
                )
            }

            try {
                // Create conversation if needed
                val convId = _state.value.conversationId ?: run {
                    val conv = conversationRepository.createConversation()
                    _state.update { it.copy(conversationId = conv.id, conversationTitle = conv.title) }
                    conv.id
                }

                // Add streaming placeholder for assistant
                val assistantMsg = ChatUiMessage(
                    id = "streaming",
                    role = MessageRole.ASSISTANT,
                    content = "",
                    isStreaming = true
                )
                _state.update { it.copy(messages = it.messages + assistantMsg) }

                // Stream the response
                val contentBuilder = StringBuilder()
                var lastModel: String? = null

                conversationRepository.streamChat(convId, content)
                    .collect { event ->
                        when (event.type) {
                            SSEEventType.TOKEN -> {
                                contentBuilder.append(event.content ?: "")
                                lastModel = event.model ?: lastModel
                                updateStreamingMessage(contentBuilder.toString(), lastModel)
                            }
                            SSEEventType.TOOL_START -> {
                                updateStreamingMessage(
                                    contentBuilder.toString(),
                                    lastModel,
                                    toolName = event.tool
                                )
                            }
                            SSEEventType.TOOL_RESULT -> {
                                updateStreamingMessage(
                                    contentBuilder.toString(),
                                    lastModel,
                                    toolOutput = event.output
                                )
                            }
                            SSEEventType.ERROR -> {
                                updateStreamingMessage(
                                    contentBuilder.toString(),
                                    lastModel,
                                    error = event.error
                                )
                            }
                            SSEEventType.DONE -> {
                                _state.update { s ->
                                    val updated = s.messages.map { msg ->
                                        if (msg.id == "streaming") msg.copy(
                                            id = "msg-${System.currentTimeMillis()}",
                                            isStreaming = false,
                                            content = contentBuilder.toString()
                                        ) else msg
                                    }
                                    s.copy(
                                        messages = updated,
                                        isSending = false,
                                        creditsBalance = event.creditsUsed?.let { used ->
                                            (s.creditsBalance ?: 0) - used
                                        } ?: s.creditsBalance
                                    )
                                }
                            }
                            SSEEventType.FILE -> {
                                // Handle file events in future
                            }
                        }
                    }
            } catch (e: Exception) {
                _state.update {
                    // Remove the streaming placeholder on error
                    val cleaned = it.messages.filter { msg -> msg.id != "streaming" }
                    it.copy(messages = cleaned, isSending = false, error = e.message)
                }
            }
        }
    }

    private fun updateStreamingMessage(
        content: String,
        model: String?,
        toolName: String? = null,
        toolOutput: String? = null,
        error: String? = null
    ) {
        _state.update { s ->
            val updated = s.messages.map { msg ->
                if (msg.id == "streaming") msg.copy(
                    content = content,
                    model = model,
                    toolName = toolName ?: msg.toolName,
                    toolOutput = toolOutput ?: msg.toolOutput,
                    error = error ?: msg.error
                ) else msg
            }
            s.copy(messages = updated)
        }
    }

    fun loadCredits() {
        viewModelScope.launch {
            try {
                val balance = conversationRepository.getCreditBalance()
                _state.update { it.copy(creditsBalance = balance.creditsBalance) }
            } catch (_: Exception) { }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}
