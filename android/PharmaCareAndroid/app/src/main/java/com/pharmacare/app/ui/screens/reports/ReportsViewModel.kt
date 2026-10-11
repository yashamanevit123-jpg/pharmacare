package com.pharmacare.app.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pharmacare.app.data.model.MonthlySaleItem
import com.pharmacare.app.data.repository.DashboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportsUiState(
    val isLoading: Boolean = false,
    val monthlySales: List<MonthlySaleItem> = emptyList(),
    val errorMessage: String? = null
) {
    val totalRevenue: Double
        get() = monthlySales.sumOf { it.net_revenue }

    val totalInvoices: Int
        get() = monthlySales.sumOf { it.total_invoices }
}

class ReportsViewModel(
    private val repository: DashboardRepository = DashboardRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState(isLoading = true))
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadReports()
    }

    fun loadReports() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val result = repository.getMonthlySales()
            result.onSuccess { data ->
                _uiState.value = ReportsUiState(
                    isLoading = false,
                    monthlySales = data
                )
            }.onFailure { err ->
                _uiState.value = ReportsUiState(
                    isLoading = false,
                    errorMessage = err.message ?: "Failed to fetch sales reports"
                )
            }
        }
    }
}
