package com.clinicalsystem.ai.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Document(collection = "ai_reports")
@Getter
@Setter
public class AiReport {

    @Id
    private String id;

    @Indexed
    private UUID patientId;

    private UUID appointmentId;
    private String modelName;
    private String modelVersion;
    private int urgencyScore;
    private String suggestedSpecialty;
    private double confidence;
    private String recommendation;
    private List<String> riskFactors;

    @Indexed
    private Instant createdAt;
}
