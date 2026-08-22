package com.mediwise.domain.repository

import com.mediwise.core.result.Result
import com.mediwise.domain.model.PatientProfile

interface ProfileRepository {
    suspend fun getProfile(): Result<PatientProfile>
    suspend fun updateProfile(
        fullName: String?,
        dob: String?,
        bloodType: String?,
        gender: String?,
        address: String? = null,
        emergencyContact: String? = null
    ): Result<Unit>
    suspend fun uploadProfileImage(imageBytes: ByteArray, mimeType: String): Result<String>
}


