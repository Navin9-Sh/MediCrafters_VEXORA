package com.clinicalsystem.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clinicalsystem.core.result.Result
import com.clinicalsystem.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val navigateToHome: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(firebaseToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.login(firebaseToken)
            if (result is Result.Success) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true, navigateToHome = true) }
            } else if (result is Result.Error) {
                _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
            }
        }
    }

    fun loginWithEmailPassword(email: String, pass: String) {
        // In real app: Firebase auth -> get token -> call login(token)
        login("mock_firebase_token_for_${email}")
    }

    fun loginWithGoogle() {
        login("mock_google_firebase_token")
    }

    fun register(firebaseToken: String, email: String, phone: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.register(firebaseToken, email, phone, "PATIENT")
            if (result is Result.Success) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true, navigateToHome = true) }
            } else if (result is Result.Error) {
                _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
            }
        }
    }

    fun resetState() {
        _uiState.update { AuthUiState() }
    }
}
