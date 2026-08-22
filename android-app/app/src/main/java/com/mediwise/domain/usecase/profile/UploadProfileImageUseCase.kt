package com.mediwise.domain.usecase.profile

import com.mediwise.core.result.AppException
import com.mediwise.core.result.Result
import com.mediwise.domain.repository.ProfileRepository
import javax.inject.Inject

class UploadProfileImageUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(imageBytes: ByteArray, mimeType: String = "image/jpeg"): Result<String> {
        if (imageBytes.isEmpty()) {
            return Result.Error(AppException.ValidationException("file", "Image file cannot be empty"))
        }
        if (imageBytes.size > 10 * 1024 * 1024) { // 10 MB limit
            return Result.Error(AppException.ValidationException("file", "Image size cannot exceed 10 MB"))
        }
        return profileRepository.uploadProfileImage(imageBytes, mimeType)
    }
}
