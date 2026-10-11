package com.pharmacare.app.ui.screens.prescriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pharmacare.app.data.model.PrescriptionItemData
import com.pharmacare.app.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PrescriptionsUiState(
    val isLoading: Boolean = false,
    val prescriptions: List<PrescriptionItemData> = emptyList(),
    val searchQuery: String = "",
    val errorMessage: String? = null
)

class PrescriptionsViewModel(
    private val repository: DashboardRepository = DashboardRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrescriptionsUiState(isLoading = true))
    val uiState: StateFlow<PrescriptionsUiState> = _uiState.asStateFlow()

    init {
        loadPrescriptions()
    }

    fun loadPrescriptions() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = repository.getPrescriptions()
            result.onSuccess { data ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    prescriptions = data
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Failed to fetch prescriptions"
                )
            }
        }
    }

    fun onSearchChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
}
