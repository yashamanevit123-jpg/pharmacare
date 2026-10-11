package com.pharmacare.app.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pharmacare.app.data.model.BatchItem
import com.pharmacare.app.data.model.CategoryItem
import com.pharmacare.app.data.model.MedicineItem
import com.pharmacare.app.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class InventoryUiState(
    val isLoading: Boolean = false,
    val medicines: List<MedicineItem> = emptyList(),
    val categories: List<CategoryItem> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryId: Int? = null,
    val selectedStatus: String? = null,
    val selectedMedicine: MedicineItem? = null,
    val batches: List<BatchItem> = emptyList(),
    val isBatchesLoading: Boolean = false,
    val errorMessage: String? = null
)

class InventoryViewModel(
    private val repository: InventoryRepository = InventoryRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState(isLoading = true))
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            val catResult = repository.getCategories()
            val categories = catResult.getOrDefault(emptyList())

            val medResult = repository.getMedicines(
                search = _uiState.value.searchQuery.ifBlank { null },
                categoryId = _uiState.value.selectedCategoryId,
                status = _uiState.value.selectedStatus
            )

            medResult.onSuccess { meds ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    medicines = meds,
                    categories = categories
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    categories = categories,
                    errorMessage = err.message ?: "Failed to load inventory"
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadMedicines()
    }

    fun onCategorySelect(categoryId: Int?) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = categoryId)
        loadMedicines()
    }

    fun onStatusSelect(status: String?) {
        _uiState.value = _uiState.value.copy(selectedStatus = status)
        loadMedicines()
    }

    private fun loadMedicines() {
        viewModelScope.launch {
            val result = repository.getMedicines(
                search = _uiState.value.searchQuery.ifBlank { null },
                categoryId = _uiState.value.selectedCategoryId,
                status = _uiState.value.selectedStatus
            )
            result.onSuccess { meds ->
                _uiState.value = _uiState.value.copy(medicines = meds)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(errorMessage = err.message)
            }
        }
    }

    fun selectMedicineForBatches(medicine: MedicineItem?) {
        _uiState.value = _uiState.value.copy(
            selectedMedicine = medicine,
            batches = emptyList(),
            isBatchesLoading = medicine != null
        )

        if (medicine != null) {
            viewModelScope.launch {
                val result = repository.getAvailableBatches(medicine.medicine_id)
                result.onSuccess { batches ->
                    _uiState.value = _uiState.value.copy(
                        batches = batches,
                        isBatchesLoading = false
                    )
                }.onFailure {
                    _uiState.value = _uiState.value.copy(isBatchesLoading = false)
                }
            }
        }
    }
}
