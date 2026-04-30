package com.clinicalsystem.domain.repository

import com.clinicalsystem.core.result.Result

interface AuthRepository {
    suspend fun login(firebaseIdToken: String): Result<Unit>
    suspend fun register(firebaseIdToken: String, email: String, phone: String, role: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun refreshToken(): Result<Unit>
}
