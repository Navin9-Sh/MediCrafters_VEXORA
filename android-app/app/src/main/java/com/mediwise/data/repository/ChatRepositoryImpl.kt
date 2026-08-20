package com.mediwise.data.repository

import com.mediwise.BuildConfig
import com.mediwise.core.datastore.SessionDataStore
import com.mediwise.core.network.StompClient
import com.mediwise.core.result.Result
import com.mediwise.domain.model.ChatMessage
import com.mediwise.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val stompClient: StompClient,
    private val sessionDataStore: SessionDataStore
) : ChatRepository {

    override fun connectWebSocket(roomId: String) {
        val token = runBlocking { sessionDataStore.accessToken.first() } ?: ""
        stompClient.connect(BuildConfig.WS_URL, token)
        stompClient.subscribe("/topic/chat/$roomId")
    }

    override fun observeMessages(roomId: String): Flow<List<ChatMessage>> {
        return stompClient.messages.map { text ->
            val json = JSONObject(text)
            val msg = ChatMessage(
                id = json.optString("id", ""),
                senderId = json.optString("senderId", ""),
                content = json.optString("content", ""),
                time = json.optString("sentAt", ""),
                isMe = false
            )
            listOf(msg)
        }
    }

    override suspend fun getMessageHistory(roomId: String, page: Int): Result<List<ChatMessage>> {
        return Result.Success(emptyList())
    }

    override suspend fun sendMessage(roomId: String, content: String): Result<Unit> {
        val payload = """{"roomId":"$roomId", "content":"$content"}"""
        stompClient.send("/app/chat.send", payload)
        return Result.Success(Unit)
    }

    override fun disconnectWebSocket() {
        stompClient.disconnect()
    }
}
