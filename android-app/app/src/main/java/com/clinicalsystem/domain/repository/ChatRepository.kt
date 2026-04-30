package com.clinicalsystem.domain.repository

import com.clinicalsystem.core.result.Result
import com.clinicalsystem.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeMessages(roomId: String): Flow<List<ChatMessage>>
    suspend fun getMessageHistory(roomId: String, page: Int): Result<List<ChatMessage>>
    suspend fun sendMessage(roomId: String, content: String): Result<Unit>
    fun connectWebSocket(roomId: String)
    fun disconnectWebSocket()
}
