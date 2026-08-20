package com.mediwise.core.network

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.*
import okio.ByteString
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StompClient @Inject constructor(
    private val client: OkHttpClient
) {
    private var webSocket: WebSocket? = null
    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages

    fun connect(url: String, token: String) {
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                // Send STOMP CONNECT frame
                val connectFrame = "CONNECT\naccept-version:1.1,1.0\n\n\u0000"
                webSocket.send(connectFrame)
                Log.d("StompClient", "Connected to WebSocket")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("StompClient", "Message received: $text")
                if (text.startsWith("CONNECTED")) {
                    Log.d("StompClient", "STOMP Connected")
                } else if (text.startsWith("MESSAGE")) {
                    // Very rudimentary STOMP parsing
                    val bodyIndex = text.indexOf("\n\n")
                    if (bodyIndex != -1) {
                        val body = text.substring(bodyIndex + 2).trimEnd('\u0000')
                        _messages.tryEmit(body)
                    }
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                // Not expecting binary frames for standard STOMP text
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
                Log.d("StompClient", "Closing: $code / $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("StompClient", "Error: ${t.message}", t)
            }
        })
    }

    fun subscribe(topic: String) {
        val subId = "sub-${UUID.randomUUID()}"
        val subscribeFrame = "SUBSCRIBE\nid:$subId\ndestination:$topic\n\n\u0000"
        webSocket?.send(subscribeFrame)
        Log.d("StompClient", "Subscribed to $topic")
    }

    fun send(destination: String, body: String) {
        val sendFrame = "SEND\ndestination:$destination\ncontent-type:application/json\n\n$body\u0000"
        webSocket?.send(sendFrame)
        Log.d("StompClient", "Sent to $destination: $body")
    }

    fun disconnect() {
        val disconnectFrame = "DISCONNECT\n\n\u0000"
        webSocket?.send(disconnectFrame)
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        Log.d("StompClient", "Disconnected")
    }
}
