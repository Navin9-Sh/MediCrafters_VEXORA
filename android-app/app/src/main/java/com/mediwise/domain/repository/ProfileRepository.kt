package com.mediwise.domain.repository

import com.mediwise.core.result.Result

interface ProfileRepository {
    suspend fun getProfile(): Result<Map<String, Any?>>
    suspend fun updateProfile(fullName: String?, dob: String?, bloodType: String?, gender: String?): Result<Unit>
    suspend fun uploadProfileImage(imageBytes: ByteArray, mimeType: String): Result<String>
}
