package com.mediwise.payment.service;

import com.mediwise.appointment.model.Appointment;
import com.mediwise.appointment.repository.AppointmentRepository;
import com.mediwise.common.exception.BusinessException;
import com.mediwise.common.exception.PaymentException;
import com.mediwise.common.exception.ResourceNotFoundException;
import com.mediwise.doctor.model.Doctor;
import com.mediwise.doctor.repository.DoctorRepository;
import com.mediwise.payment.dto.InitiatePaymentRequest;
import com.mediwise.payment.dto.PaymentResponse;
import com.mediwise.payment.dto.VerifyPaymentRequest;
import com.mediwise.payment.model.Payment;
import com.mediwise.payment.repository.PaymentRepository;
import com.razorpay.RazorpayException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RazorpayService {

    private final RazorpayOrderGateway razorpayOrderGateway;
    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository; // to confirm appointment
    private final DoctorRepository doctorRepository; // to get authoritative fee

    @Value("${application.razorpay.key-secret}")
    private String keySecret;

    @Value("${application.razorpay.webhook-secret}")
    private String webhookSecret;

    private static final int MAX_RETRIES = 3;

    // ── Step 1: Android calls this. Backend creates a Razorpay order ──────────
    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentRequest request, UUID patientId) {

        // 1. Load the appointment — ensures it exists and belongs to a real booking
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", request.getAppointmentId().toString()));

        // 2. Reject if appointment is already confirmed or cancelled
        if (appointment.getStatus() == Appointment.AppointmentStatus.CONFIRMED) {
            throw new BusinessException("ALREADY_PAID", "This appointment has already been paid for.");
        }
        if (appointment.getStatus() == Appointment.AppointmentStatus.CANCELLED) {
            throw new BusinessException("APPOINTMENT_CANCELLED", "Cannot pay for a cancelled appointment.");
        }

        // 3. SECURITY: fetch the authoritative fee from the Doctor entity.
        // The client cannot influence this amount in any way.
        Doctor doctor = doctorRepository.findById(appointment.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor", appointment.getDoctorId().toString()));

        BigDecimal authorizedAmount = doctor.getConsultationFee();
        if (authorizedAmount == null || authorizedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_FEE", "Doctor has not set a valid consultation fee.");
        }

        // 4. Idempotency: reuse an existing INITIATED order if the user retries
        // (e.g., app crashed mid-checkout, user tapped back and came again)
        Payment existingPayment = paymentRepository
                .findByAppointmentIdAndStatus(request.getAppointmentId(), Payment.PaymentStatus.INITIATED)
                .orElse(null);

        if (existingPayment != null) {
            if (existingPayment.getRetryCount() >= MAX_RETRIES) {
                throw new PaymentException("Maximum payment retries exceeded. Please contact support.");
            }
            // Return the SAME Razorpay order — user resumes existing checkout
            existingPayment.setRetryCount(existingPayment.getRetryCount() + 1);
            paymentRepository.save(existingPayment);
            log.info("Reusing existing Razorpay order {} for appointment {}",
                    existingPayment.getGatewayOrderId(), request.getAppointmentId());
            return PaymentResponse.from(existingPayment);
        }

        // 5. Create a fresh Razorpay order via the gateway (not the SDK directly)
        try {
            String razorpayOrderId = razorpayOrderGateway.createOrder(
                    request.getAppointmentId(), authorizedAmount);

            // 6. Persist our own Payment record (maps our system to Razorpay's order)
            Payment payment = Payment.builder()
                    .appointmentId(request.getAppointmentId())
                    .patientId(appointment.getPatientId())
                    .amount(authorizedAmount) // server-authoritative amount
                    .currency("INR")
                    .status(Payment.PaymentStatus.INITIATED)
                    .gateway("RAZORPAY")
                    .gatewayOrderId(razorpayOrderId) // e.g. "order_Mx2ABCDEF"
                    .build();

            payment = paymentRepository.save(payment);
            log.info("Payment {} initiated | amount=₹{} | Razorpay order={}",
                    payment.getId(), authorizedAmount, payment.getGatewayOrderId());

            return PaymentResponse.from(payment);

        } catch (RazorpayException e) {
            throw new PaymentException("Failed to create payment order: " + e.getMessage());
        } catch (Exception e) {
            throw new PaymentException("Unexpected error creating payment order: " + e.getMessage());
        }
    }

    // ── Step 2: Android sends back the 3 values from Razorpay's onSuccess ────
    @Transactional
    public PaymentResponse verifyAndConfirmPayment(VerifyPaymentRequest request) {

        // 1. Find our internal Payment record by Razorpay's orderId
        Payment payment = paymentRepository.findByGatewayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new PaymentException("Payment order not found."));

        // 2. Idempotency: already verified? Return without re-processing.
        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            log.info("Payment {} already verified — returning cached response", payment.getId());
            return PaymentResponse.from(payment);
        }

        // 3. SECURITY: Recompute HMAC SHA256 on the server.
        // Formula: HMAC(orderId + "|" + paymentId, secretKey)
        // If signatures match → Razorpay genuinely processed this payment.
        // If not → someone fabricated the paymentId/signature on the client.
        String expectedSignature = computeHmac(
                request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId(),
                keySecret);

        if (!expectedSignature.equals(request.getRazorpaySignature())) {
            // Mark as failed and log — do NOT confirm the appointment
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setLastError("Signature mismatch — possible tampering attempt");
            paymentRepository.save(payment);
            log.error("Payment signature mismatch for order {} — POSSIBLE TAMPERING",
                    request.getRazorpayOrderId());
            throw new PaymentException("Payment verification failed. Please contact support.");
        }

        // 4. Signature matched → mark payment SUCCESS
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setGatewayPaymentId(request.getRazorpayPaymentId());
        payment.setGatewaySignature(request.getRazorpaySignature());
        paymentRepository.save(payment);

        // 5. THE CRITICAL STEP: Transition Appointment PENDING → CONFIRMED
        // Only now is the booking considered real and locked in.
        appointmentRepository.findById(payment.getAppointmentId()).ifPresent(appointment -> {
            if (appointment.getStatus() == Appointment.AppointmentStatus.PENDING) {
                appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
                appointmentRepository.save(appointment);
                log.info("Appointment {} CONFIRMED after payment {}", appointment.getId(), payment.getId());
            }
        });

        log.info("Payment {} verified successfully for ₹{}", payment.getId(), payment.getAmount());
        return PaymentResponse.from(payment);
    }

    // ── Step 3: Razorpay calls our webhook as a server-side safety net ────────
    // Even if the Android app crashes before calling /verify, this reconciles.
    @Transactional
    public void processWebhookEvent(String payload) {
        try {
            JSONObject event = new JSONObject(payload);
            String eventType = event.optString("event");

            // We only care about successful captures
            if (!"payment.captured".equals(eventType)) {
                log.debug("Ignoring webhook event: {}", eventType);
                return;
            }

            JSONObject paymentEntity = event
                    .getJSONObject("payload")
                    .getJSONObject("payment")
                    .getJSONObject("entity");

            String razorpayOrderId = paymentEntity.getString("order_id");
            String razorpayPaymentId = paymentEntity.getString("id");

            paymentRepository.findByGatewayOrderId(razorpayOrderId).ifPresent(payment -> {
                if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
                    payment.setStatus(Payment.PaymentStatus.SUCCESS);
                    payment.setGatewayPaymentId(razorpayPaymentId);
                    paymentRepository.save(payment);

                    // Confirm the appointment if not already confirmed
                    appointmentRepository.findById(payment.getAppointmentId()).ifPresent(appointment -> {
                        if (appointment.getStatus() == Appointment.AppointmentStatus.PENDING) {
                            appointment.setStatus(Appointment.AppointmentStatus.CONFIRMED);
                            appointmentRepository.save(appointment);
                            log.info("Appointment {} confirmed via webhook", appointment.getId());
                        }
                    });
                }
            });

        } catch (Exception e) {
            log.error("Failed to process webhook payload: {}", e.getMessage(), e);
            // Don't re-throw — Razorpay will retry webhooks; we don't want it to
            // mark us as unavailable for a JSON parse error
        }
    }

    // ── Webhook HMAC verification (called by PaymentController) ──────────────
    public boolean verifyWebhookSignature(String payload, String signature) {
        String computed = computeHmac(payload, webhookSecret);
        return computed.equals(signature);
    }

    // ── HMAC SHA256 helper ────────────────────────────────────────────────────
    // Used for both payment signature verification and webhook signature check.
    // The key difference: payment uses keySecret, webhook uses webhookSecret.
    private String computeHmac(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash); // converts byte[] → hex string
        } catch (Exception e) {
            throw new PaymentException("HMAC computation failed: " + e.getMessage());
        }
    }
}
