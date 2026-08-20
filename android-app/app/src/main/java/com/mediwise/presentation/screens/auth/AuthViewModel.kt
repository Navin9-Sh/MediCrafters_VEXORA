package com.mediwise.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.mediwise.core.result.Result
import com.mediwise.domain.repository.AuthRepository
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

    private val firebaseAuth = FirebaseAuth.getInstance()
    private var phoneVerificationId: String? = null

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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            updateState(repository.loginWithEmailPassword(email, pass))
        }
    }

    fun loginWithGoogle() {
        login("mock_google_firebase_token")
    }

    fun showError(message: String) {
        _uiState.update { it.copy(error = message) }
    }

    fun startPhoneLogin(activity: Activity, phone: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: com.google.firebase.auth.PhoneAuthCredential) {
                viewModelScope.launch {
                    updateState(repository.loginWithPhoneCredential(credential))
                }
            }

            override fun onVerificationFailed(exception: FirebaseException) {
                showError(exception.localizedMessage ?: "Could not send verification code.")
                _uiState.update { it.copy(isLoading = false) }
            }

            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                phoneVerificationId = id
                _uiState.update { it.copy(isLoading = false) }
            }
        }
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L, java.util.concurrent.TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyPhoneCode(code: String) {
        val id = phoneVerificationId
        if (id.isNullOrBlank()) {
            showError("Verification expired. Please request a new code.")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            updateState(repository.loginWithPhoneCredential(PhoneAuthProvider.getCredential(id, code)))
        }
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

    fun registerWithEmailPassword(fullName: String, email: String, password: String, phone: String?, dateOfBirth: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            updateState(repository.registerWithEmailPassword(fullName, email, password, phone, dateOfBirth))
        }
    }

    fun sendPasswordResetEmail(email: String, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            when (val result = repository.sendPasswordResetEmail(email)) {
                is Result.Success -> onComplete(true, null)
                is Result.Error -> onComplete(false, result.exception.message)
                Result.Loading -> Unit
            }
        }
    }

    private fun updateState(result: Result<Unit>) {
        if (result is Result.Success) {
            _uiState.update { it.copy(isLoading = false, isSuccess = true, navigateToHome = true) }
        } else if (result is Result.Error) {
            _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
        }
    }

    fun resetState() {
        _uiState.update { AuthUiState() }
    }
}
