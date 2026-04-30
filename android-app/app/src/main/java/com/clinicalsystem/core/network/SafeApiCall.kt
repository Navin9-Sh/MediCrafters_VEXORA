package com.clinicalsystem.core.network

import com.clinicalsystem.core.result.AppException
import com.clinicalsystem.core.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Wraps every API call. Maps all throwable types to [Result.Error] with typed [AppException].
 * Never lets raw exceptions propagate upward.
 */
suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> {
    return withContext(Dispatchers.IO) {
        try {
            Result.Success(block())
        } catch (e: AppException) {
            Result.Error(e)
        } catch (e: HttpException) {
            val error = when (e.code()) {
                401 -> AppException.UnauthorizedException()
                403 -> AppException.ForbiddenException()
                409 -> AppException.SlotConflictException()
                402 -> AppException.PaymentFailedException()
                in 500..599 -> AppException.ServerException(e.code(), "Server error. Please try again later.")
                else -> AppException.ServerException(e.code(), e.message())
            }
            Result.Error(error)
        } catch (e: UnknownHostException) {
            Result.Error(AppException.NetworkException())
        } catch (e: SocketTimeoutException) {
            Result.Error(AppException.TimeoutException())
        } catch (e: Exception) {
            Result.Error(AppException.UnknownException(e.localizedMessage ?: "Unknown error"))
        }
    }
}
