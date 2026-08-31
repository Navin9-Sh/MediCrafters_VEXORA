package com.mediwise.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

/**
 * Request to initiate a Razorpay payment order.
 *
 * SECURITY: The client sends ONLY the appointmentId.
 * The backend derives the authoritative amount from the doctor's
 * consultationFee stored in the database — the client can never
 * supply or tamper with the payment amount.
 */
@Data
public class InitiatePaymentRequest {

    @NotNull(message = "Appointment ID is required")
    private UUID appointmentId;
}
