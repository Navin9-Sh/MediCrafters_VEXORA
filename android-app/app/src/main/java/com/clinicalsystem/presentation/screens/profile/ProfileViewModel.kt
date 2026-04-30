package com.clinicalsystem.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clinicalsystem.core.result.Result
import com.clinicalsystem.domain.model.PatientProfile
import com.clinicalsystem.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: PatientProfile? = null,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // For now, loading a stubbed profile from a UseCase or Repository.
            // Ideally we fetch from ProfileRepository.
            val mockProfile = PatientProfile(
                id = "P-1001",
                fullName = "Alex Rider",
                email = "alex.rider@email.com",
                phone = "+1 555-0100",
                dateOfBirth = "1990-05-15",
                gender = "Male",
                bloodType = "O+",
                address = "123 Health St",
                emergencyContact = "911",
                profileImageUrl = null
            )
            
            _uiState.update { it.copy(isLoading = false, profile = mockProfile) }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
