package com.mediwise.data.repository

import com.mediwise.core.network.safeApiCall
import com.mediwise.core.result.Result
import com.mediwise.data.remote.api.PaymentApi
import com.mediwise.data.remote.dto.*
import com.mediwise.domain.repository.PaymentRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val api: PaymentApi
) : PaymentRepository {
    override suspend fun initiatePayment(appointmentId: String, amount: Double): Result<String> {
        return safeApiCall {
            val response = api.initiatePayment(InitiatePaymentRequestDto(appointmentId, amount.toString()))
            response.data?.gatewayOrderId ?: throw Exception("Failed to initiate payment")
        }
    }

    override suspend fun verifyPayment(orderId: String, paymentId: String, signature: String): Result<String> {
        return safeApiCall {
            val response = api.verifyPayment(VerifyPaymentRequestDto(orderId, paymentId, signature))
            if (response.success) paymentId else throw Exception("Payment verification failed")
        }
    }
}
