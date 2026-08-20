package com.mediwise.ai.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Document(collection = "symptom_logs")
@Getter
@Setter
public class SymptomLog {

    @Id
    private String id;

    @Indexed
    private UUID patientId;

    private UUID appointmentId;
    private List<String> symptoms;

    /** LOW | MODERATE | HIGH | CRITICAL */
    private String severity;

    private String notes;

    @Indexed
    private Instant loggedAt;
}
