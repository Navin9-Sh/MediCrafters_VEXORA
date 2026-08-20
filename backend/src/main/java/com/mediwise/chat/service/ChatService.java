package com.mediwise.chat.service;

import com.mediwise.chat.dto.SendMessageRequest;
import com.mediwise.chat.model.ChatMessage;
import com.mediwise.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository messageRepository;
    
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    private static final int RECENT_CACHE_SIZE = 50;
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    public ChatMessage saveMessage(SendMessageRequest request, String senderId, String senderRole) {
        ChatMessage message = ChatMessage.builder()
                .roomId(request.getRoomId())
                .senderId(senderId)
                .senderRole(senderRole)
                .content(request.getContent())
                .contentType(request.getContentType())
                .mediaUrl(request.getMediaUrl())
                .read(false)
                .deleted(false)
                .build();

        message = messageRepository.save(message);

        // Invalidate cache for this room
        if (redisTemplate != null) {
            redisTemplate.delete("chat_recent:" + request.getRoomId());
        }

        log.debug("Message saved to room {}", request.getRoomId());
        return message;
    }

    public Page<ChatMessage> getMessages(String roomId, int page, int size) {
        return messageRepository.findByRoomIdAndDeletedFalseOrderBySentAtDesc(
                roomId, PageRequest.of(page, size, Sort.by("sentAt").descending()));
    }

    public void markAsRead(String roomId, String readerId) {
        // Mark unread messages from the other party as read
        messageRepository.findByRoomIdAndDeletedFalseOrderBySentAtDesc(
                roomId, PageRequest.of(0, RECENT_CACHE_SIZE))
                .getContent()
                .stream()
                .filter(m -> !m.getSenderId().equals(readerId) && !m.isRead())
                .forEach(m -> {
                    m.setRead(true);
                    m.setReadAt(java.time.Instant.now());
                    messageRepository.save(m);
                });
        if (redisTemplate != null) {
            redisTemplate.delete("chat_recent:" + roomId);
        }
    }
}
