package com.mediwise.payment.dto;

import com.mediwise.payment.model.Payment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data @Builder
public class PaymentResponse {
    private UUID id;
    private UUID appointmentId;
    private BigDecimal amount;
    private String currency;
    private Payment.PaymentStatus status;
    private String gatewayOrderId;
    private String gatewayPaymentId;
    private int retryCount;

    public static PaymentResponse from(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId())
                .appointmentId(p.getAppointmentId())
                .amount(p.getAmount())
                .currency(p.getCurrency())
                .status(p.getStatus())
                .gatewayOrderId(p.getGatewayOrderId())
                .gatewayPaymentId(p.getGatewayPaymentId())
                .retryCount(p.getRetryCount())
                .build();
    }
}
