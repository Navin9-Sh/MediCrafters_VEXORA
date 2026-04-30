package com.clinicalsystem.schedule.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data @Builder
public class SlotLockResponse {
    private UUID slotId;
    private boolean locked;
    private Instant expiresAt;
    private long ttlMinutes;
}
