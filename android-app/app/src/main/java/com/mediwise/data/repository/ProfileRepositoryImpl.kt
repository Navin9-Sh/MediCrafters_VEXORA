package com.mediwise.data.repository

import com.mediwise.core.network.safeApiCall
import com.mediwise.core.result.Result
import com.mediwise.data.remote.api.ProfileApi
import com.mediwise.data.remote.dto.UpdateProfileRequestDto
import com.mediwise.domain.repository.ProfileRepository
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(private val api: ProfileApi) : ProfileRepository {
    override suspend fun getProfile(): Result<Map<String, Any?>> =
        safeApiCall {
            val d = api.getProfile().data
            mapOf("fullName" to d?.fullName, "dob" to d?.dob, "bloodType" to d?.bloodType, "gender" to d?.gender, "profileImage" to d?.profileImage)
        }
    override suspend fun updateProfile(fullName: String?, dob: String?, bloodType: String?, gender: String?): Result<Unit> =
        safeApiCall { api.updateProfile(UpdateProfileRequestDto(fullName, dob, bloodType, gender)) }
    override suspend fun uploadProfileImage(imageBytes: ByteArray, mimeType: String): Result<String> =
        Result.Success("") // Multipart handled in ViewModel
}
