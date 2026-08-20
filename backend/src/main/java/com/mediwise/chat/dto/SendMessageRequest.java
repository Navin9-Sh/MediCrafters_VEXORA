package com.mediwise.chat.dto;

import com.mediwise.chat.model.ChatMessage;
import lombok.Data;

@Data
public class SendMessageRequest {
    private String roomId;
    private String content;
    private ChatMessage.ContentType contentType = ChatMessage.ContentType.TEXT;
    private String mediaUrl;
}
