package com.myopenclaw.ui.viewmodel.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myopenclaw.domain.models.Conversation
import com.myopenclaw.domain.repository.ConversationRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ConversationListState(
    val conversations: List<Conversation> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ConversationListViewModel(
    private val conversationRepository: ConversationRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ConversationListState())
    val state: StateFlow<ConversationListState> = _state.asStateFlow()

    fun loadConversations() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val conversations = conversationRepository.getConversations()
                _state.update { it.copy(conversations = conversations, isLoading = false) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun deleteConversation(id: String) {
        viewModelScope.launch {
            try {
                conversationRepository.deleteConversation(id)
                _state.update { s ->
                    s.copy(conversations = s.conversations.filter { it.id != id })
                }
            } catch (_: Exception) { }
        }
    }
}
