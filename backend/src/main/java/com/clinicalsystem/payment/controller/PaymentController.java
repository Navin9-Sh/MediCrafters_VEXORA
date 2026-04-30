package com.clinicalsystem.payment.controller;

import com.clinicalsystem.auth.model.User;
import com.clinicalsystem.common.response.ApiResponse;
import com.clinicalsystem.payment.dto.InitiatePaymentRequest;
import com.clinicalsystem.payment.dto.PaymentResponse;
import com.clinicalsystem.payment.dto.VerifyPaymentRequest;
import com.clinicalsystem.payment.service.RazorpayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Razorpay payment initiation, verification and webhook")
public class PaymentController {

    private final RazorpayService razorpayService;

    @PostMapping("/initiate")
    @Operation(summary = "Initiate Razorpay order")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @Valid @RequestBody InitiatePaymentRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(razorpayService.initiatePayment(request, user.getId())));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify payment after Razorpay checkout")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(razorpayService.verifyAndConfirmPayment(request)));
    }

    @PostMapping("/webhook")
    @Operation(summary = "Razorpay webhook (HMAC verified, no JWT auth)")
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        if (!razorpayService.verifyWebhookSignature(payload, signature)) {
            log.warn("Razorpay webhook signature mismatch");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // TODO: parse event, reconcile payment status
        return ResponseEntity.ok().build();
    }
}
