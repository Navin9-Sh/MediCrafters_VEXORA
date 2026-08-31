package com.mediwise.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body when a doctor marks a consultation as COMPLETED.
 * The doctor submits their clinical notes, diagnosis, and prescription
 * at the end of the call.
 *
 * All three fields are free-text. In a later phase, prescription
 * could become a structured list of medications with dosage, but
 * for MVP plain text is acceptable.
 */
@Data
public class CompleteAppointmentRequest {

    @NotBlank(message = "Clinical notes are required to complete an appointment")
    private String notes;

    // Optional — not every appointment results in a diagnosis or prescription
    private String diagnosis;
    private String prescription;
}
