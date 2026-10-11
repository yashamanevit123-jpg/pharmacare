package com.pharmacare.app.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pharmacare.app.data.model.DashboardData
import com.pharmacare.app.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val data: DashboardData? = null,
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val repository: DashboardRepository = DashboardRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = repository.getDashboard()
            result.onSuccess { data ->
                _uiState.value = DashboardUiState(isLoading = false, data = data)
            }.onFailure { error ->
                _uiState.value = DashboardUiState(
                    isLoading = false,
                    errorMessage = error.message ?: "Failed to load dashboard data"
                )
            }
        }
    }
}
