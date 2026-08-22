package com.mediwise.domain.usecase.profile

import com.mediwise.core.datastore.SessionDataStore
import com.mediwise.core.result.Result
import com.mediwise.domain.model.PatientProfile
import com.mediwise.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val sessionDataStore: SessionDataStore
) {
    suspend operator fun invoke(): Result<PatientProfile> {
        val result = profileRepository.getProfile()
        return when (result) {
            is Result.Success -> {
                val email = sessionDataStore.userEmail.firstOrNull() ?: ""
                val profile = if (result.data.email.isBlank() && email.isNotBlank()) {
                    result.data.copy(email = email)
                } else {
                    result.data
                }
                Result.Success(profile)
            }
            is Result.Error -> result
            is Result.Loading -> Result.Loading
        }
    }
}
