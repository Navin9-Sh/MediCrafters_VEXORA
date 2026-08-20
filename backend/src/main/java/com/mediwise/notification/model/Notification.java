package com.mediwise.notification.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @Column(nullable = false, length = 50)
    private String type;

    @Column(name = "ref_id")
    private UUID refId;

    @Column(name = "is_read")
    @Builder.Default
    private boolean read = false;

    @Column(name = "sent_at")
    @Builder.Default
    private Instant sentAt = Instant.now();
}
