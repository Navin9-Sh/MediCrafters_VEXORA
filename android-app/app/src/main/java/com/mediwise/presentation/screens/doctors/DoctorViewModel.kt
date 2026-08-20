package com.mediwise.presentation.screens.doctors

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediwise.core.result.Result
import com.mediwise.domain.model.Doctor
import com.mediwise.domain.repository.DoctorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DoctorListUiState(
    val isLoading: Boolean = false,
    val doctors: List<Doctor> = emptyList(),
    val favorites: Set<String> = emptySet(),
    val searchQuery: String = "",
    val selectedSpecialty: String = "All",
    val error: String? = null
)

@HiltViewModel
class DoctorViewModel @Inject constructor(
    private val repository: DoctorRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DoctorListUiState())
    val uiState: StateFlow<DoctorListUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    val specialties = listOf("All", "Cardiology", "Dermatology", "Neurology",
        "Orthopedics", "General Medicine", "Pediatrics", "Psychiatry")

    init {
        observeSearch()
        loadDoctors()
    }

    @OptIn(FlowPreview::class)
    private fun observeSearch() {
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .distinctUntilChanged()
                .collect { query ->
                    _uiState.update { it.copy(searchQuery = query) }
                    loadDoctors()
                }
        }
    }

    fun loadDoctors() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val currentState = _uiState.value
            val result = repository.getDoctors(
                specialty = currentState.selectedSpecialty,
                search = currentState.searchQuery
            )
            
            when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, doctors = result.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSpecialtySelected(specialty: String) {
        _uiState.update { it.copy(selectedSpecialty = specialty) }
        loadDoctors()
    }

    fun toggleFavorite(doctorId: String) {
        viewModelScope.launch {
            val currentFavorites = _uiState.value.favorites.toMutableSet()
            val isFavorite = currentFavorites.contains(doctorId)
            
            if (isFavorite) {
                currentFavorites.remove(doctorId)
            } else {
                currentFavorites.add(doctorId)
            }
            _uiState.update { it.copy(favorites = currentFavorites) }
            
            // Sync with backend
            repository.toggleFavorite(doctorId)
        }
    }
}
