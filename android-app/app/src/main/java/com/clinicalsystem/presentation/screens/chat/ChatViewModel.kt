package com.clinicalsystem.presentation.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clinicalsystem.core.datastore.SessionDataStore
import com.clinicalsystem.domain.model.ChatMessage
import com.clinicalsystem.domain.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val otherPartyName: String = "Doctor",
    val myUserId: String = "",
    val isOtherPartyTyping: Boolean = false
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentRoomId: String? = null

    init {
        viewModelScope.launch {
            val myId = sessionDataStore.userId.first() ?: ""
            _uiState.update { it.copy(myUserId = myId) }
        }
    }

    fun initRoom(roomId: String) {
        if (currentRoomId == roomId) return
        currentRoomId = roomId
        
        repository.connectWebSocket(roomId)
        
        viewModelScope.launch {
            repository.observeMessages(roomId).collect { newMessages ->
                _uiState.update { currentState ->
                    val updatedMessages = currentState.messages.toMutableList()
                    // Basic duplicate prevention
                    newMessages.forEach { newMsg ->
                        if (updatedMessages.none { it.id == newMsg.id }) {
                            updatedMessages.add(newMsg)
                        }
                    }
                    currentState.copy(messages = updatedMessages)
                }
            }
        }
    }

    fun sendMessage(roomId: String, text: String) {
        viewModelScope.launch {
            // Optimistic update
            val tempMsg = ChatMessage(
                id = "temp-${System.currentTimeMillis()}",
                senderId = _uiState.value.myUserId,
                content = text,
                time = "Just now",
                isMe = true
            )
            
            _uiState.update { it.copy(messages = it.messages + tempMsg) }
            
            // Send to repo
            repository.sendMessage(roomId, text)
        }
    }

    fun sendTypingIndicator(roomId: String, isTyping: Boolean) {
        // Feature stub - in real app sends a specific STOMP frame or API call
    }

    override fun onCleared() {
        super.onCleared()
        repository.disconnectWebSocket()
    }
}
