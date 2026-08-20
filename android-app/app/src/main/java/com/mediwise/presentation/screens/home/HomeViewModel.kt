package com.mediwise.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediwise.core.datastore.SessionDataStore
import com.mediwise.domain.model.Appointment
import com.mediwise.domain.model.Doctor
import com.mediwise.core.result.onSuccess
import com.mediwise.core.result.onError
import com.mediwise.domain.usecase.appointment.GetMyAppointmentsUseCase
import com.mediwise.domain.usecase.doctor.GetDoctorsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = false,
    val userName: String = "",
    val unreadNotifications: Int = 0,
    val upcomingAppointments: List<Appointment> = emptyList(),
    val topDoctors: List<Doctor> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getDoctorsUseCase: GetDoctorsUseCase,
    private val getAppointmentsUseCase: GetMyAppointmentsUseCase,
    private val sessionDataStore: SessionDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeData()
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Load doctor list
            getDoctorsUseCase(page = 0, size = 5)
                .onSuccess { docs -> _uiState.update { it.copy(topDoctors = docs, isLoading = false) } }
                .onError { e -> _uiState.update { it.copy(error = e.message, isLoading = false) } }
        }
    }

    fun refresh() = loadHomeData()
}
