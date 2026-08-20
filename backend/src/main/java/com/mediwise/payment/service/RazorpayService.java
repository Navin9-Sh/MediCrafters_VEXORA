package com.mediwise.payment.service;

import com.mediwise.common.exception.PaymentException;
import com.mediwise.payment.dto.InitiatePaymentRequest;
import com.mediwise.payment.dto.PaymentResponse;
import com.mediwise.payment.dto.VerifyPaymentRequest;
import com.mediwise.payment.model.Payment;
import com.mediwise.payment.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
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

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;

    @Value("${application.razorpay.key-secret}")
    private String keySecret;

    @Value("${application.razorpay.webhook-secret}")
    private String webhookSecret;

    private static final int MAX_RETRIES = 3;

    @Transactional
    public PaymentResponse initiatePayment(InitiatePaymentRequest request, UUID patientId) {
        // Check if there's an existing INITIATED payment for this appointment (retry)
        Payment existingPayment = paymentRepository
                .findByAppointmentIdAndStatus(request.getAppointmentId(), Payment.PaymentStatus.INITIATED)
                .orElse(null);

        if (existingPayment != null && existingPayment.getRetryCount() >= MAX_RETRIES) {
            throw new PaymentException("Maximum payment retries exceeded. Please contact support.");
        }

        // Reuse existing Razorpay order if available
        if (existingPayment != null && existingPayment.getGatewayOrderId() != null) {
            existingPayment.setRetryCount(existingPayment.getRetryCount() + 1);
            paymentRepository.save(existingPayment);
            return PaymentResponse.from(existingPayment);
        }

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", request.getAmount().multiply(BigDecimal.valueOf(100)).intValue());
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "rcpt_" + request.getAppointmentId());
            orderRequest.put("payment_capture", 1);

            Order order = razorpayClient.orders.create(orderRequest);

            Payment payment = Payment.builder()
                    .appointmentId(request.getAppointmentId())
                    .patientId(patientId)
                    .amount(request.getAmount())
                    .currency("INR")
                    .status(Payment.PaymentStatus.INITIATED)
                    .gateway("RAZORPAY")
                    .gatewayOrderId(order.get("id"))
                    .build();

            payment = paymentRepository.save(payment);
            log.info("Payment initiated: {} | Razorpay order: {}", payment.getId(), payment.getGatewayOrderId());
            return PaymentResponse.from(payment);

        } catch (RazorpayException e) {
            throw new PaymentException("Failed to create payment order: " + e.getMessage());
        }
    }

    @Transactional
    public PaymentResponse verifyAndConfirmPayment(VerifyPaymentRequest request) {
        Payment payment = paymentRepository.findByGatewayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new PaymentException("Payment order not found."));

        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            return PaymentResponse.from(payment); // Idempotent
        }

        // Verify HMAC signature
        String generatedSignature = generateSignature(
                request.getRazorpayOrderId() + "|" + request.getRazorpayPaymentId());

        if (!generatedSignature.equals(request.getRazorpaySignature())) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setLastError("Signature mismatch — possible tampering");
            paymentRepository.save(payment);
            throw new PaymentException("Payment verification failed. Please contact support.");
        }

        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setGatewayPaymentId(request.getRazorpayPaymentId());
        payment.setGatewaySignature(request.getRazorpaySignature());
        paymentRepository.save(payment);

        log.info("Payment {} verified successfully", payment.getId());
        return PaymentResponse.from(payment);
    }

    public boolean verifyWebhookSignature(String payload, String signature) {
        String computed = generateHmac(payload, webhookSecret);
        return computed.equals(signature);
    }

    private String generateSignature(String data) {
        return generateHmac(data, keySecret);
    }

    private String generateHmac(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new PaymentException("Failed to generate HMAC: " + e.getMessage());
        }
    }
}
