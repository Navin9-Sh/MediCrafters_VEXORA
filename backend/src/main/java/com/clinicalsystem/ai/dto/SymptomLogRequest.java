package com.clinicalsystem.ai.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class SymptomLogRequest {

    @NotNull(message = "Patient ID is required")
    private UUID patientId;

    private UUID appointmentId;

    @NotEmpty(message = "At least one symptom is required")
    private List<String> symptoms;

    /** LOW, MODERATE, HIGH, CRITICAL */
    private String severity;

    /** Vitals snapshot — all optional */
    private Integer heartRate;
    private Integer systolicBp;
    private Integer diastolicBp;
    private Double spo2;
    private Double temperature;

    /** Free-text additional notes */
    private String notes;
}
