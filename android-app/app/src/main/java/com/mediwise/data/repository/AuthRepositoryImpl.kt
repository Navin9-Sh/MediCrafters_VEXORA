package com.mediwise.data.repository

import com.mediwise.core.datastore.SessionDataStore
import com.mediwise.core.network.safeApiCall
import com.mediwise.core.result.Result
import com.mediwise.data.remote.api.AuthApi
import com.mediwise.data.remote.dto.LoginRequestDto
import com.mediwise.data.remote.dto.RegisterRequestDto
import com.mediwise.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionDataStore: SessionDataStore
) : AuthRepository {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

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

    override suspend fun loginWithEmailPassword(email: String, password: String): Result<Unit> {
        return safeApiCall {
            val firebaseUser = firebaseAuth.signInWithEmailAndPassword(email.trim(), password).await().user
                ?: throw Exception("Firebase user was not returned")
//            if (!firebaseUser.isEmailVerified) {
//                throw Exception("Please verify your email before logging in.")
//            }
            val token = firebaseUser.getIdToken(true).await().token
                ?: throw Exception("Firebase ID token was not returned")
            val response = api.login(LoginRequestDto(token))
            val data = response.data ?: throw Exception("Empty response")
            sessionDataStore.saveSession(data.accessToken, data.refreshToken, data.user.id, data.user.role, data.user.email)
        }
    }

    override suspend fun loginWithPhoneCredential(credential: com.google.firebase.auth.PhoneAuthCredential): Result<Unit> {
        return safeApiCall {
            val firebaseUser = firebaseAuth.signInWithCredential(credential).await().user
                ?: throw Exception("Firebase user was not returned")
            val token = firebaseUser.getIdToken(true).await().token
                ?: throw Exception("Firebase ID token was not returned")
            val response = api.login(LoginRequestDto(token))
            val data = response.data ?: throw Exception("Account not found. Please register first.")
            sessionDataStore.saveSession(data.accessToken, data.refreshToken, data.user.id, data.user.role, data.user.email)
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

    override suspend fun registerWithEmailPassword(fullName: String, email: String, password: String, phone: String?, dateOfBirth: String?): Result<Unit> {
        return safeApiCall {
            val firebaseUser = firebaseAuth.createUserWithEmailAndPassword(email.trim(), password).await().user
                ?: throw Exception("Firebase user was not returned")
            firebaseUser.sendEmailVerification().await()
            val token = firebaseUser.getIdToken(true).await().token
                ?: throw Exception("Firebase ID token was not returned")
            val response = api.register(
                RegisterRequestDto(
                    firebaseIdToken = token,
                    email = email.trim(),
                    phone = phone,
                    role = "PATIENT",
                    fullName = fullName.trim(),
                    dateOfBirth = dateOfBirth
                )
            )
            val data = response.data ?: throw Exception("Empty response")
            sessionDataStore.saveSession(data.accessToken, data.refreshToken, data.user.id, data.user.role, data.user.email)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return safeApiCall {
            firebaseAuth.sendPasswordResetEmail(email.trim()).await()
        }
    }

    override suspend fun logout(): Result<Unit> {
        return safeApiCall {
            firebaseAuth.signOut()
            sessionDataStore.clearSession()
        }
    }

    override suspend fun refreshToken(): Result<Unit> {
        // TODO: implement refresh
        return Result.Success(Unit)
    }
}
