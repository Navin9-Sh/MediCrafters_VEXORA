package com.mediwise.presentation.screens.doctors

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediwise.core.result.Result
import com.mediwise.domain.model.Doctor
import com.mediwise.domain.repository.DoctorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DoctorDetailUiState(
    val isLoading: Boolean = false,
    val doctor: Doctor? = null,
    val error: String? = null
)

@HiltViewModel
class DoctorDetailViewModel @Inject constructor(
    private val repository: DoctorRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorDetailUiState())
    val uiState: StateFlow<DoctorDetailUiState> = _uiState.asStateFlow()

    init {
        val doctorId = savedStateHandle.get<String>("doctorId")
        if (doctorId != null) {
            loadDoctor(doctorId)
        } else {
            _uiState.update { it.copy(error = "Invalid Doctor ID") }
        }
    }

    private fun loadDoctor(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getDoctorById(id)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, doctor = result.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                }
                is Result.Loading -> {}
            }
        }
    }
}
