package com.mediwise.chat.dto;

import lombok.Data;

@Data
public class TypingEvent {
    private String roomId;
    private String senderId;
    private boolean typing;
}
