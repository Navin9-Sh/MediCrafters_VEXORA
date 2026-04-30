package com.clinicalsystem.core.network

import com.clinicalsystem.core.result.AppException
import okhttp3.Interceptor
import okhttp3.Response
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class ErrorInterceptor @Inject constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return try {
            val response = chain.proceed(chain.request())
            response // actual handling is in safeApiCall
        } catch (e: UnknownHostException) {
            throw AppException.NetworkException()
        } catch (e: SocketTimeoutException) {
            throw AppException.TimeoutException()
        }
    }
}
