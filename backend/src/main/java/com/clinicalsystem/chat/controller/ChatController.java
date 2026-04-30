package com.clinicalsystem.chat.controller;

import com.clinicalsystem.auth.model.User;
import com.clinicalsystem.chat.dto.SendMessageRequest;
import com.clinicalsystem.chat.dto.TypingEvent;
import com.clinicalsystem.chat.model.ChatMessage;
import com.clinicalsystem.chat.service.ChatService;
import com.clinicalsystem.common.response.ApiResponse;
import com.clinicalsystem.common.response.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequiredArgsConstructor
@Tag(name = "Chat", description = "Real-time chat via WebSocket/STOMP + REST history")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // ── WebSocket STOMP endpoints ──────────────────────────────────────────

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload SendMessageRequest request, Principal principal) {
        // principal.getName() = userId from JWT
        // Determine role from header or store (simplified: extracted from STOMP header in config)
        ChatMessage saved = chatService.saveMessage(request, principal.getName(), "UNKNOWN");
        messagingTemplate.convertAndSend("/topic/chat/" + request.getRoomId(), saved);
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload TypingEvent event, Principal principal) {
        event.setSenderId(principal.getName());
        // NOT saved to DB — ephemeral
        messagingTemplate.convertAndSend("/topic/chat/" + event.getRoomId() + "/typing", event);
    }

    @MessageMapping("/chat.read")
    public void markRead(@Payload String roomId, Principal principal) {
        chatService.markAsRead(roomId, principal.getName());
        messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/read", principal.getName());
    }

    // ── REST endpoint for message history ─────────────────────────────────

    @GetMapping("/api/v1/chat/{roomId}/messages")
    @Operation(summary = "Get paginated message history for a chat room")
    public ResponseEntity<ApiResponse<PagedResponse<ChatMessage>>> getMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(chatService.getMessages(roomId, page, size))));
    }
}
