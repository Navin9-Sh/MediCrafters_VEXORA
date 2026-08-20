package com.mediwise.domain.repository

import com.mediwise.core.result.Result

interface PaymentRepository {
    suspend fun initiatePayment(appointmentId: String, amount: Double): Result<String> // returns gatewayOrderId
    suspend fun verifyPayment(orderId: String, paymentId: String, signature: String): Result<String> // returns paymentId
}
