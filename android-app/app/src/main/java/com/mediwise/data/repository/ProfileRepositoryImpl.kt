package com.mediwise.data.repository

import com.mediwise.core.network.safeApiCall
import com.mediwise.core.result.Result
import com.mediwise.data.remote.api.ProfileApi
import com.mediwise.data.remote.dto.UpdateProfileRequestDto
import com.mediwise.data.remote.dto.toPatientProfile
import com.mediwise.domain.model.PatientProfile
import com.mediwise.domain.repository.ProfileRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(private val api: ProfileApi) : ProfileRepository {
    override suspend fun getProfile(): Result<PatientProfile> =
        safeApiCall {
            val d = api.getProfile().data
            d?.toPatientProfile() ?: PatientProfile()
        }

    override suspend fun updateProfile(
        fullName: String?,
        dob: String?,
        bloodType: String?,
        gender: String?,
        address: String?,
        emergencyContact: String?
    ): Result<Unit> =
        safeApiCall {
            api.updateProfile(
                UpdateProfileRequestDto(
                    fullName = fullName,
                    dob = dob,
                    bloodType = bloodType,
                    gender = gender,
                    address = address,
                    emergencyContact = emergencyContact
                )
            )
        }

    override suspend fun uploadProfileImage(imageBytes: ByteArray, mimeType: String): Result<String> =
        safeApiCall {
            val requestBody = imageBytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", "profile.jpg", requestBody)
            api.uploadProfileImage(part).data ?: ""
        }
}

