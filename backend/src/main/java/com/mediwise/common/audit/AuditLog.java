package com.mediwise.common.audit;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Document(collection = "audit_events")
public class AuditLog {

    @Id
    private String id;

    private String actorId;
    private String actorRole;
    private String action;
    private String resourceType;
    private String resourceId;
    private String ipHash;
    private String userAgent;
    private String outcome;   // SUCCESS | FAILURE
    private String errorCode;

    @Indexed(expireAfterSeconds = 7776000) // 90 days TTL
    private Instant timestamp;

    private Map<String, Object> metadata;
}
