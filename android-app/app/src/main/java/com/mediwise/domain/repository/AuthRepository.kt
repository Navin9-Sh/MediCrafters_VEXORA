package com.mediwise.domain.repository

import com.mediwise.core.result.Result
import com.google.firebase.auth.PhoneAuthCredential

interface AuthRepository {
    suspend fun login(firebaseIdToken: String): Result<Unit>
    suspend fun loginWithEmailPassword(email: String, password: String): Result<Unit>
    suspend fun loginWithPhoneCredential(credential: PhoneAuthCredential): Result<Unit>
    suspend fun register(firebaseIdToken: String, email: String, phone: String, role: String): Result<Unit>
    suspend fun registerWithEmailPassword(fullName: String, email: String, password: String, phone: String?, dateOfBirth: String?): Result<Unit>
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun refreshToken(): Result<Unit>
}
