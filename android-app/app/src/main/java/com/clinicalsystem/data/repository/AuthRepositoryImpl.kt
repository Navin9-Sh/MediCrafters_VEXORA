package com.clinicalsystem.data.repository

import com.clinicalsystem.core.datastore.SessionDataStore
import com.clinicalsystem.core.network.safeApiCall
import com.clinicalsystem.core.result.Result
import com.clinicalsystem.data.remote.api.AuthApi
import com.clinicalsystem.data.remote.dto.LoginRequestDto
import com.clinicalsystem.data.remote.dto.RegisterRequestDto
import com.clinicalsystem.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionDataStore: SessionDataStore
) : AuthRepository {

    override suspend fun login(firebaseIdToken: String): Result<Unit> {
        return safeApiCall {
            val response = api.login(LoginRequestDto(firebaseIdToken))
            val data = response.data ?: throw Exception("Empty response")
            sessionDataStore.saveSession(
                data.accessToken, data.refreshToken,
                data.user.id, data.user.role, data.user.email
            )
        }
    }

    override suspend fun register(firebaseIdToken: String, email: String, phone: String, role: String): Result<Unit> {
        return safeApiCall {
            val response = api.register(RegisterRequestDto(firebaseIdToken, email, phone, role))
            val data = response.data ?: throw Exception("Empty response")
            sessionDataStore.saveSession(
                data.accessToken, data.refreshToken,
                data.user.id, data.user.role, data.user.email
            )
        }
    }

    override suspend fun logout(): Result<Unit> {
        return safeApiCall {
            sessionDataStore.clearSession()
        }
    }

    override suspend fun refreshToken(): Result<Unit> {
        // TODO: implement refresh
        return Result.Success(Unit)
    }
}
