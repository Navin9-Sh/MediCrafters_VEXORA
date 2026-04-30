package com.clinicalsystem.ai.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class AiReportResponse {

    private String id;
    private UUID patientId;
    private UUID appointmentId;

    /** e.g. "symptom_triage_v2" */
    private String modelName;
    private String modelVersion;

    /** 0–100. Higher = more urgent */
    private int urgencyScore;

    /** e.g. "Cardiology", "General Medicine" */
    private String suggestedSpecialty;

    /** 0.0–1.0 */
    private double confidence;

    /** Human-readable explanation from the model */
    private String recommendation;

    /** Flagged risk factors */
    private List<String> riskFactors;

    private Instant createdAt;
}
