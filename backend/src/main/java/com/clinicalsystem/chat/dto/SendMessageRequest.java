package com.clinicalsystem.chat.dto;

import com.clinicalsystem.chat.model.ChatMessage;
import lombok.Data;

@Data
public class SendMessageRequest {
    private String roomId;
    private String content;
    private ChatMessage.ContentType contentType = ChatMessage.ContentType.TEXT;
    private String mediaUrl;
}
