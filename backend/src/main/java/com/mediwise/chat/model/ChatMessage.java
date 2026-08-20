package com.mediwise.chat.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "chat_messages")
@CompoundIndex(def = "{'roomId': 1, 'sentAt': -1}")
public class ChatMessage {

    @Id
    private String id;

    private String roomId;         // "appointment_<uuid>"
    private String senderId;
    private String senderRole;     // PATIENT | DOCTOR
    private String content;
    private ContentType contentType;
    private String mediaUrl;
    private boolean read;
    private Instant readAt;
    private boolean deleted;

    @Builder.Default
    private Instant sentAt = Instant.now();

    public enum ContentType {
        TEXT, IMAGE, FILE
    }
}
