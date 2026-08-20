package com.mediwise.domain.repository

import com.mediwise.core.result.Result
import com.mediwise.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(roomId: String): Flow<List<ChatMessage>>
    suspend fun getMessageHistory(roomId: String, page: Int): Result<List<ChatMessage>>
    suspend fun sendMessage(roomId: String, content: String): Result<Unit>
    fun connectWebSocket(roomId: String)
    fun disconnectWebSocket()
}
