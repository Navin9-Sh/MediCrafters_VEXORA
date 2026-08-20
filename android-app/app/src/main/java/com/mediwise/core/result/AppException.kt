package com.mediwise.core.result

sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    // Network / connectivity
    class NetworkException(msg: String = "No internet connection.") : AppException(msg)
    class TimeoutException(msg: String = "Request timed out. Please try again.") : AppException(msg)

    // Auth
    class UnauthorizedException(msg: String = "Session expired. Please log in again.") : AppException(msg)
    class ForbiddenException(msg: String = "You don't have permission to do this.") : AppException(msg)

    // Business
    class SlotConflictException(msg: String = "This slot is no longer available.") : AppException(msg)
    class PaymentFailedException(msg: String = "Payment failed. Please retry.") : AppException(msg)
    class ValidationException(val field: String, msg: String) : AppException(msg)
    class ProfileRequiredException(msg: String = "Please complete your profile first.") : AppException(msg)

    // Server
    class ServerException(val code: Int, msg: String) : AppException(msg)

    // Unknown
    class UnknownException(msg: String = "Something went wrong. Please try again.") : AppException(msg)
}
