package com.mediwise.domain.usecase.profile

import com.mediwise.core.result.AppException
import com.mediwise.core.result.Result
import com.mediwise.domain.repository.ProfileRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(
        fullName: String,
        dob: String,
        bloodType: String,
        gender: String,
        address: String,
        emergencyContact: String
    ): Result<Unit> {
        val trimmedName = fullName.trim()
        if (trimmedName.isBlank()) {
            return Result.Error(AppException.ValidationException("fullName", "Full name is required"))
        }

        val trimmedDob = dob.trim()
        if (trimmedDob.isNotBlank()) {
            try {
                LocalDate.parse(trimmedDob, DateTimeFormatter.ISO_DATE)
            } catch (_: Exception) {
                return Result.Error(AppException.ValidationException("dob", "Date of birth must be in YYYY-MM-DD format"))
            }
        }

        return profileRepository.updateProfile(
            fullName = trimmedName,
            dob = trimmedDob.ifBlank { null },
            bloodType = bloodType.trim().ifBlank { null },
            gender = gender.trim().ifBlank { null },
            address = address.trim().ifBlank { null },
            emergencyContact = emergencyContact.trim().ifBlank { null }
        )
    }
}
