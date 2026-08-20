package com.mediwise.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponseDto<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val code: String? = null,
    @SerialName("correlationId") val correlationId: String? = null
)

@Serializable
data class PagedResponseDto<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean
)

@Serializable
data class RegisterRequestDto(
    val firebaseIdToken: String,
    val email: String,
    val phone: String? = null,
    val role: String = "PATIENT",
    val fullName: String? = null,
    val dateOfBirth: String? = null
)
@Serializable data class LoginRequestDto(val firebaseIdToken: String)
@Serializable data class AuthResponseDto(val accessToken: String, val refreshToken: String, val expiresIn: Long, val user: UserInfoDto)
@Serializable data class UserInfoDto(val id: String, val email: String, val phone: String? = null, val role: String)

@Serializable
data class DoctorDto(
    val id: String, val userId: String, val fullName: String,
    val bio: String? = null, val specialty: String,
    val experienceYears: Int? = null,
    val consultationFee: String? = null,
    val profileImage: String? = null,
    val avgRating: String? = null,
    val totalReviews: Int = 0,
    val available: Boolean = true,
    val verified: Boolean = false
)

@Serializable
data class AppointmentDto(
    val id: String, val patientId: String, val doctorId: String,
    val slotId: String, val slotDate: String? = null,
    val slotStartTime: String? = null, val slotEndTime: String? = null,
    val status: String, val type: String,
    val chiefComplaint: String? = null, val notes: String? = null,
    val cancelReason: String? = null, val createdAt: String? = null
)
@Serializable data class BookAppointmentRequestDto(val slotId: String, val doctorId: String, val type: String = "ONLINE", val chiefComplaint: String? = null)
@Serializable data class CancelRequestDto(val reason: String? = null)

@Serializable
data class PaymentDto(val id: String, val appointmentId: String, val amount: String, val status: String, val gatewayOrderId: String? = null, val gatewayPaymentId: String? = null)
@Serializable data class InitiatePaymentRequestDto(val appointmentId: String, val amount: String)
@Serializable data class VerifyPaymentRequestDto(val razorpayOrderId: String, val razorpayPaymentId: String, val razorpaySignature: String)

@Serializable data class ProfileDto(val id: String? = null, val userId: String? = null, val fullName: String? = null, val dob: String? = null, val bloodType: String? = null, val gender: String? = null, val profileImage: String? = null)
@Serializable data class UpdateProfileRequestDto(val fullName: String? = null, val dob: String? = null, val bloodType: String? = null, val gender: String? = null)

@Serializable data class NotificationDto(val id: String, val title: String, val body: String? = null, val type: String, val read: Boolean = false, val sentAt: String? = null)

@Serializable data class ChatMessageDto(val id: String? = null, val roomId: String, val senderId: String, val senderRole: String, val content: String, val contentType: String = "TEXT", val sentAt: String? = null, val read: Boolean = false)

fun DoctorDto.toDomain() = com.mediwise.domain.model.Doctor(id = id, fullName = fullName, specialty = specialty, consultationFee = consultationFee ?: "0", rating = avgRating?.toDoubleOrNull() ?: 0.0, reviewCount = totalReviews, profileImage = profileImage, available = available)
fun AppointmentDto.toDomain() = com.mediwise.domain.model.Appointment(id = id, patientId = patientId, doctorId = doctorId, slotId = slotId, date = slotDate ?: "", time = "$slotStartTime - $slotEndTime", status = status, type = type)
fun AuthResponseDto.toDomain() = com.mediwise.domain.model.AuthResult(accessToken = accessToken, refreshToken = refreshToken, user = user.toDomain())
fun UserInfoDto.toDomain() = com.mediwise.domain.model.User(id = id, email = email, role = role)
fun PaymentDto.toDomain() = com.mediwise.domain.model.Payment(id = id, appointmentId = appointmentId, amount = amount, status = status)
fun ProfileDto.toDomain() = com.mediwise.domain.model.UserProfile(fullName = fullName ?: "", dob = dob ?: "", bloodType = bloodType ?: "", gender = gender ?: "", profileImage = profileImage)
fun NotificationDto.toDomain() = com.mediwise.domain.model.Notification(id = id, title = title, body = body ?: "", type = type, isRead = read, time = sentAt ?: "")
fun ChatMessageDto.toDomain() = com.mediwise.domain.model.ChatMessage(id = id ?: "", senderId = senderId, content = content, time = sentAt ?: "", isMe = false)
