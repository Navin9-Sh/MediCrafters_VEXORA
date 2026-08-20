package com.mediwise.presentation.screens.appointment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediwise.core.result.Result
import com.mediwise.domain.model.Appointment
import com.mediwise.domain.repository.AppointmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppointmentUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val appointments: List<Appointment> = emptyList(),
    val selectedStatus: String = "Upcoming",
    val isCancelling: Boolean = false
)

@HiltViewModel
class AppointmentViewModel @Inject constructor(
    private val repository: AppointmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppointmentUiState())
    val uiState: StateFlow<AppointmentUiState> = _uiState.asStateFlow()

    init {
        loadAppointments()
    }

    fun loadAppointments() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val statusMap = mapOf(
                "Upcoming" to "PENDING,CONFIRMED",
                "Past" to "COMPLETED",
                "Cancelled" to "CANCELLED"
            )
            val backendStatus = statusMap[_uiState.value.selectedStatus]
            
            when (val result = repository.getMyAppointments(backendStatus)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, appointments = result.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun onTabSelected(status: String) {
        _uiState.update { it.copy(selectedStatus = status) }
        loadAppointments()
    }

    fun cancelAppointment(appointmentId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true) }
            val result = repository.cancelAppointment(appointmentId)
            _uiState.update { it.copy(isCancelling = false) }
            
            if (result is Result.Success) {
                loadAppointments() // reload after cancellation
            } else if (result is Result.Error) {
                _uiState.update { it.copy(error = result.exception.message) }
            }
        }
    }
}
