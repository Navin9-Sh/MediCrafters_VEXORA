package com.mediwise.presentation.screens.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ScheduleUiState(
    val isLoading: Boolean = false,
    val timeSlots: List<String> = emptyList(),
    val bookedSlots: Set<String> = emptySet(),
    val error: String? = null
)

@HiltViewModel
class ScheduleViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState: StateFlow<ScheduleUiState> = _uiState.asStateFlow()

    fun loadSlotsForDate(date: LocalDate, doctorId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            // In a real app, this would call repository.getSlotsForDate(doctorId, date)
            val slots = listOf("09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM",
                               "11:00 AM", "02:00 PM", "02:30 PM", "03:00 PM",
                               "03:30 PM", "04:00 PM", "04:30 PM", "05:00 PM")
            val booked = setOf("09:30 AM", "11:00 AM", "03:00 PM")
            
            _uiState.update { 
                it.copy(isLoading = false, timeSlots = slots, bookedSlots = booked) 
            }
        }
    }
}
